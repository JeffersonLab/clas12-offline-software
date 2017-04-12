package org.jlab.rec.cvt.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.cross.HelixCrossListFinder;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.track.TrackListFinder;
import org.jlab.rec.cvt.trajectory.TrkSwimmer;

/**
 * Service to return reconstructed BST track candidates- the output is in Evio format
 * 
 * @author ziegler
 *
 */

public class CVTReconstruction extends ReconstructionEngine {

    public CVTReconstruction() {
    	super("CVT", "ziegler", "3.0");
    }

    String FieldsConfig="";
	int Run = -1;
	CVTRecConfig config;
	
    @Override
	public boolean processDataEvent(DataEvent event) {
    	config.setRunConditionsParameters(event, FieldsConfig, Run, false, "");
    	
    	this.FieldsConfig=config.getFieldsConfig();
    	this.Run = config.getRun();
		org.jlab.rec.cvt.bmt.Geometry BMTGeom = new org.jlab.rec.cvt.bmt.Geometry();
	    org.jlab.rec.cvt.svt.Geometry SVTGeom = new org.jlab.rec.cvt.svt.Geometry( );
	    
		ADCConvertor adcConv = new ADCConvertor();
		
		RecoBankWriter rbc = new RecoBankWriter();
		
		HitReader hitRead = new HitReader();
		hitRead.fetch_SVTHits(event,adcConv,-1,-1, SVTGeom);
		hitRead.fetch_BMTHits(event, adcConv, BMTGeom);
		
		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		List<Hit>  svt_hits = hitRead.get_SVTHits();
		if(svt_hits.size()>0)
			hits.addAll(svt_hits);
		
		List<Hit>  bmt_hits = hitRead.get_BMTHits();
		if(bmt_hits.size()>0)
			hits.addAll(bmt_hits);
		
		
		//II) process the hits		
		List<FittedHit> SVThits = new ArrayList<FittedHit>();
		List<FittedHit> BMThits = new ArrayList<FittedHit>();
		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return true;
		}
		
		List<Cluster> clusters = new ArrayList<Cluster>();
		List<Cluster> SVTclusters = new ArrayList<Cluster>();
		List<Cluster> BMTclusters = new ArrayList<Cluster>();
		
		//2) find the clusters from these hits
		ClusterFinder clusFinder = new ClusterFinder();
		clusters = clusFinder.findClusters(hits);
		
		if(clusters.size()==0) {
			return true;
		}
		
		// fill the fitted hits list.
		if(clusters.size()!=0) {   			
   			for(int i = 0; i<clusters.size(); i++) {
   				if(clusters.get(i).get_Detector()=="SVT") {
   					SVTclusters.add(clusters.get(i));
   					SVThits.addAll(clusters.get(i)); 
   				}
   				if(clusters.get(i).get_Detector()=="BMT") {
   					BMTclusters.add(clusters.get(i));
   					BMThits.addAll(clusters.get(i));
   				}
   			}
		}
		//TrackSeeder seeder = new TrackSeeder();
		
		//seeder.findSeed(SVTclusters, SVTGeom);
		
		List<ArrayList<Cross>> crosses = new ArrayList<ArrayList<Cross>>();
		
		//3) find the crosses
		CrossMaker crossMake = new CrossMaker();

		crosses = crossMake.findCrosses(clusters,SVTGeom);
		
		if(clusters.size()==0 ) {
			
			return true; //exiting
		}
		//clean up svt crosses
		List<Cross> crossesToRm = crossMake.crossLooperCands(crosses);			
		crosses.get(0).removeAll(crossesToRm);
		
		if(crosses.size()==0) {
			// create the clusters and fitted hits banks
			rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, null, null);
			
			return true; //exiting
		}
		// if the looper finder kills all svt crosses save all crosses anyway
		if(crosses.get(0).size()==0) {
			List<ArrayList<Cross>> crosses2 = new ArrayList<ArrayList<Cross>>();
			crosses2.add(0,(ArrayList<Cross>) crossesToRm);
			crosses2.add(1, crosses.get(1));
			rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses2, null);
			
			return true;
		}
		
		//Find cross lists for Helix
		//4) make list of crosses consistent with a track candidate
		HelixCrossListFinder crossLister = new HelixCrossListFinder();
		CrossList crosslist = crossLister.findCandidateCrossLists(crosses);
		
		if(crosslist.size()==0) {
			rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
			
			return true;
		}
		
		//5) find the list of  track candidates
		TrackCandListFinder trkcandFinder = new TrackCandListFinder();
		List<Track> trkcands = new ArrayList<Track>();
		
		trkcands = trkcandFinder.getHelicalTrack(crosslist, SVTGeom, BMTGeom); 
			
		if(trkcands.size()==0) {
			rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
			return true;
		}
		
		
		//This last part does ELoss C
		TrackListFinder trkFinder = new TrackListFinder();
		List<Track> trks = new ArrayList<Track>();
		trks = trkFinder.getTracks(trkcands, SVTGeom) ;
		
		// set index associations
		if(trks.size()>0) {
			// create the clusters and fitted hits banks
			for(int k1 = 0; k1<trks.size(); k1++) {
				trks.get(k1).set_Id(k1+1);
				for(int k2 = 0; k2<trks.get(k1).size(); k2++) {
					trks.get(k1).get(k2).set_AssociatedTrackID(trks.get(k1).get_Id()); // associate crosses
					trks.get(k1).get(k2).get_Cluster1().set_AssociatedTrackID(trks.get(k1).get_Id()); // associate cluster1 in cross
					trks.get(k1).get(k2).get_Cluster2().set_AssociatedTrackID(trks.get(k1).get_Id()); // associate cluster2 in cross					
					for(int k3 = 0; k3<trks.get(k1).get(k2).get_Cluster1().size(); k3++) { //associate hits
						trks.get(k1).get(k2).get_Cluster1().get(k3).set_AssociatedTrackID(trks.get(k1).get_Id());
					}
					for(int k4 = 0; k4<trks.get(k1).get(k2).get_Cluster2().size(); k4++) { //associate hits
						trks.get(k1).get(k2).get_Cluster2().get(k4).set_AssociatedTrackID(trks.get(k1).get_Id());
					}
				}
			}
			rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, trks);
		}
		
		return true;
		
	}
	@Override
	public boolean init() {
		
		TrkSwimmer.getMagneticFields();
		config = new CVTRecConfig();
		return true;
	}

	public static void main(String[] args) throws FileNotFoundException, EvioException{
		
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add( "/Users/ziegler/Workdir/Files/GEMC/CVT/prot.hipo");
		//inputFiles.add( "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/svt123_decoded_000423t.hipo");
		CVTReconstruction en = new CVTReconstruction();
		en.init();
		
		int counter = 0;
		
		 //HipoDataSource reader = new HipoDataSource();
        // reader.open(inputFile);
		
         HipoDataSync writer = new HipoDataSync();
		//Writer
		 
         String outputFile="/Users/ziegler/Workdir/Distribution/svtVtx.hipo";
		 writer.open(outputFile);
		
		long t1=0;
		for(int k = 0; k < inputFiles.size(); k++) {
			HipoDataSource reader = new HipoDataSource();
		    reader.open(inputFiles.get(k));
		        
		    while(reader.hasEvent() ){		
			counter++;
		
			DataEvent event = reader.getNextEvent();
			if(counter>0)
				t1 = System.currentTimeMillis();
			
		
			
			en.processDataEvent(event);
			
			System.out.println("  EVENT "+counter);
			if(counter>11) break;
			//event.show();
			//if(counter%100==0)
			//System.out.println("run "+counter+" events");
			
					
			if(event.hasBank("BSTRec::Crosses") ) {
				DataBank bnk = event.getBank("BSTRec::Crosses");
				if(bnk.rows()>2) {
				
					//event.show();
					writer.writeEvent(event); 
					event.show();
				}
			}
		    }
		}
		writer.close();
		double t = System.currentTimeMillis()-t1;
		System.out.println(t1+" TOTAL  PROCESSING TIME = "+(t/(float)counter));
	 }
	

}
