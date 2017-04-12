package org.jlab.rec.cvt.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
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
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

/**
 * Service to return reconstructed BST track candidates- the output is in Evio format
 * 
 * @author ziegler
 *
 */

public class CVTCosmicsReconstruction extends ReconstructionEngine {

    public CVTCosmicsReconstruction() {
    	super("CVTCosmics", "ziegler", "3.0"); 	
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
	    org.jlab.rec.cvt.svt.Geometry SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
	    
		ADCConvertor adcConv = new ADCConvertor();
		
		RecoBankWriter rbc = new RecoBankWriter();
		
		HitReader hitRead = new HitReader();
		hitRead.fetch_SVTHits(event,adcConv,-1,-1, SVTGeom);
		hitRead.fetch_BMTHits(event, adcConv, BMTGeom);
		
		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		List<Hit>  svt_hits = hitRead.get_SVTHits();
		if(svt_hits!=null && svt_hits.size()>0)
			hits.addAll(svt_hits);
		
		List<Hit>  bmt_hits = hitRead.get_BMTHits();
		if(bmt_hits!=null && bmt_hits.size()>0)
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
		/*
		for(int j =0; j< crosses.get(0).size(); j++) {
			for(int j2 =0; j2< crossesToRm.size(); j2++) {
				if(crosses.get(0).get(j).get_Id()==crossesToRm.get(j2).get_Id())
					crosses.get(0).remove(j);
				
			}
		}
		*/
		
		if(crosses.size()==0 ) {
			// create the clusters and fitted hits banks
			rbc.appendCVTCosmicsBanks( event, SVThits, BMThits, SVTclusters, BMTclusters, null, null);
			return true; //exiting
		}
		// if the looper finder kills all svt crosses save all crosses anyway
		if(crosses.get(0).size()==0) {
			List<ArrayList<Cross>> crosses2 = new ArrayList<ArrayList<Cross>>();
			crosses2.add(0,(ArrayList<Cross>) crossesToRm);
			crosses2.add(1, crosses.get(1));
			rbc.appendCVTCosmicsBanks( event, SVThits, BMThits, SVTclusters, BMTclusters, crosses2, null);
			
			return true;
		}
		//Find cross lists for Cosmics
		//4) make list of crosses consistent with a track candidate
		StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
		CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, SVTGeom);
		
		if(crosslist==null || crosslist.size()==0) {
			// create the clusters and fitted hits banks
			rbc.appendCVTCosmicsBanks( event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
			
			return true;
		}
		
		//5) find the list of  track candidates
		List<StraightTrack> cosmics = new ArrayList<StraightTrack>();
		
		TrackCandListFinder trkcandFinder = new TrackCandListFinder();
		cosmics = trkcandFinder.getStraightTracks(crosslist, crosses.get(1), SVTGeom, BMTGeom);
		
				
		if(cosmics.size()==0) {
			rbc.appendCVTCosmicsBanks( event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
			
			return true;
		}
			
		if(cosmics.size()>0) {		
			for(int k1 = 0; k1<cosmics.size(); k1++) {
				cosmics.get(k1).set_Id(k1+1);
				for(int k2 = 0; k2<cosmics.get(k1).size(); k2++) {
					cosmics.get(k1).get(k2).set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate crosses
					cosmics.get(k1).get(k2).get_Cluster1().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster1 in cross
					cosmics.get(k1).get(k2).get_Cluster2().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster2 in cross					
					for(int k3 = 0; k3<cosmics.get(k1).get(k2).get_Cluster1().size(); k3++) { //associate hits
						cosmics.get(k1).get(k2).get_Cluster1().get(k3).set_AssociatedTrackID(cosmics.get(k1).get_Id());
					}
					for(int k4 = 0; k4<cosmics.get(k1).get(k2).get_Cluster2().size(); k4++) { //associate hits
						cosmics.get(k1).get(k2).get_Cluster2().get(k4).set_AssociatedTrackID(cosmics.get(k1).get_Id());
					}
				}
				trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), SVTGeom, BMTGeom, true,
						cosmics.get(k1).get_Trajectory(), k1+1);
			}
			
			//4)  ---  write out the banks			
			rbc.appendCVTCosmicsBanks( event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, cosmics);
		}
		return true;
	}
	

		

	
	public boolean init() {
		// Load the Constants
		config = new CVTRecConfig();
		return true;
	}
	
	public static void main(String[] args) throws FileNotFoundException, EvioException{
		
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add( "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/svt123_decoded_000423t.hipo");
		//inputFiles.add( "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/svt809_decoded0.hipo");
		//inputFiles.add( "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/svt809_decoded32.hipo");
		//inputFiles.add( "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/svt809_decoded33.hipo");
		//inputFiles.add( "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/svt809_decoded35.hipo");
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/Run758.hipo";
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/old/RaffaNew.hipo";
		//String inputFile = args[0];
		//String outputFile = args[1];
		
		//System.err.println(" \n[PROCESSING FILE] : " + inputFile);
		
		CVTCosmicsReconstruction en = new CVTCosmicsReconstruction();
		en.init();
		
		int counter = 0;
		
		 //HipoDataSource reader = new HipoDataSource();
        // reader.open(inputFile);
		
         HipoDataSync writer = new HipoDataSync();
		//Writer
		 //String outputFile="/Users/ziegler/Workdir/Distribution/svt809_decodedRange.rec.hipo";
         String outputFile="/Users/ziegler/Workdir/Distribution/svt123.hipo";
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
		//	if(counter>17) break;
			//event.show();
			//if(counter%100==0)
			//System.out.println("run "+counter+" events");
			
					
			//if(event.hasBank("BSTRec::Crosses") ) {
			//	DataBank bnk = event.getBank("BSTRec::Crosses");
				//if(bnk.rows()>2) {
				
					//event.show();
					writer.writeEvent(event); 
					event.show();
				//}
			//}
		    }
		}
		writer.close();
		double t = System.currentTimeMillis()-t1;
		System.out.println(t1+" TOTAL  PROCESSING TIME = "+(t/(float)counter));
	 }
	static int Sensor(int layer, int sector) {
        int[] shift = {0, 10, 20, 34, 48, 66, 84, 108};
        if (layer == 0 || sector == 0) {
            return -1;
        }
        return sector + shift[layer - 1] - 1;
    }
}
