package org.jlab.service.dc;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.cross.CrossListFinder;
import org.jlab.rec.dc.cross.CrossMaker;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.segment.SegmentFinder;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.RoadFinder;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

public class DCHBEngine extends ReconstructionEngine {

	public DCHBEngine() {
		super("DCHB","ziegler","3.0");
	}

	String FieldsConfig="";
	int Run = -1;
	
	@Override
	public boolean init() {
		
		// Load the Fields 
		DCSwimmer.getMagneticFields();
		
		return true;
	}

	
	@Override
	public boolean processDataEvent(DataEvent event) {
		setRunConditionsParameters( event) ;
		 // init SNR 
	    Clas12NoiseResult results = new Clas12NoiseResult(); 
		Clas12NoiseAnalysis noiseAnalysis = new Clas12NoiseAnalysis();

		int[] rightShifts = Constants.SNR_RIGHTSHIFTS;
		int[] leftShifts  = Constants.SNR_LEFTSHIFTS;
		NoiseReductionParameters parameters = new NoiseReductionParameters (
				2,leftShifts,
				rightShifts);
		//System.out.println("RUNING HITBASED_________________________________________");
	  
		ClusterFitter cf = new ClusterFitter();
	    ClusterCleanerUtilities ct = new ClusterCleanerUtilities();
	    
	    List<FittedHit> fhits = new ArrayList<FittedHit>();
		List<FittedCluster> clusters = new ArrayList<FittedCluster>();
		List<Segment> segments = new ArrayList<Segment>();
		List<Cross> crosses = new ArrayList<Cross>();
		
		List<Track> trkcands = new ArrayList<Track>();
		
		//instantiate bank writer
		RecoBankWriter rbc = new RecoBankWriter();
		
		//if(Constants.DEBUGCROSSES)
		//	event.appendBank(rbc.fillR3CrossfromMCTrack(event));
		
		HitReader hitRead = new HitReader();
		hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results);

		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		hits = hitRead.get_DCHits();
		
		//II) process the hits
		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return true;
		}

		fhits = rbc.createRawHitList(hits);
				
		
		//2) find the clusters from these hits
		ClusterFinder clusFinder = new ClusterFinder();
		clusters = clusFinder.FindHitBasedClusters(hits, ct, cf);
		
		
		if(clusters.size()==0) {				
			rbc.fillAllHBBanks(event, rbc, fhits, null, null, null, null);
			return true;
		}
	
		rbc.updateListsListWithClusterInfo(fhits, clusters);
		
		//3) find the segments from the fitted clusters
		SegmentFinder segFinder = new SegmentFinder();
		segments =  segFinder.get_Segments(clusters, event);
 
		if(segments.size()==0) { // need 6 segments to make a trajectory			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, null, null, null);
			return true;
		}
		//RoadFinder
		//
		
		RoadFinder pcrossLister = new RoadFinder();
		List<Segment> pSegments =pcrossLister.findRoads(segments);
		segments.addAll(pSegments);
		
		//
		//System.out.println("nb trk segs "+pSegments.size());
		CrossMaker crossMake = new CrossMaker();
		crosses = crossMake.find_Crosses(segments);
 
		if(crosses.size()==0 ) {			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, null, null);
			return true;
		}
		
		CrossListFinder crossLister = new CrossListFinder();
		
		List<List<Cross>> CrossesInSector = crossLister.get_CrossesInSectors(crosses);
		for(int s =0; s< 6; s++) {
			if(CrossesInSector.get(s).size()>Constants.MAXNBCROSSES) {
				return true;
			}
		}
		
		CrossList crosslist = crossLister.candCrossLists(crosses, false);
		
		if(crosslist.size()==0) {
			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null);
			return true;
		}

		//6) find the list of  track candidates
		TrackCandListFinder trkcandFinder = new TrackCandListFinder("HitBased");
		trkcands = trkcandFinder.getTrackCands(crosslist) ;
		 
		if(trkcands.size()==0) {
			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
			return true;
		}
		// track found	
		int trkId = 1;
		for(Track trk: trkcands) {						
			for(Cross c : trk) { 
				for(FittedHit h1 : c.get_Segment1())
					h1.set_AssociatedHBTrackID(trk.get_Id());
			  	for(FittedHit h2 : c.get_Segment2())
			  		h2.set_AssociatedHBTrackID(trk.get_Id());	
			}
			
		}
	  
		trkcandFinder.removeOverlappingTracks(trkcands);		// remove overlaps
		
		for(Track trk: trkcands) {		
			// reset the id
			trk.set_Id(trkId);
			for(Cross c : trk) { 
				for(FittedHit h1 : c.get_Segment1())
					h1.set_AssociatedHBTrackID(trk.get_Id());
			  	for(FittedHit h2 : c.get_Segment2())
			  		h2.set_AssociatedHBTrackID(trk.get_Id());	
			}
			trkId++;
		}
	  
		rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands);

		return true;
	}

	public void setRunConditionsParameters(DataEvent event) {
		if(event.hasBank("RUN::config")==false) {
			System.err.println("RUN CONDITIONS NOT READ!");
			return;
		}
		boolean isMC = false;
		boolean isCosmics = false;
		
	
        DataBank bank = event.getBank("RUN::config");	
		if(bank.getByte("type", 0)==0)
			isMC = true;
		if(bank.getByte("mode", 0)==1)
			isCosmics = true;
		// force cosmics
		//isCosmics = true;
		//System.out.println(bank.getInt("Event")[0]);
		boolean isCalib = isCosmics;  // all cosmics runs are for calibration right now
		//
		
		
		// Load the constants
		//-------------------
		int newRun = bank.getInt("run", 0);
		boolean T2DCalc = false;
		
		if(Run!=newRun) {
			//if(newRun>751 && newRun<912) {
                        if(newRun>99) {
				T2DCalc = true;
				Constants.setT0(true);		
				Constants.setUseMiniStagger(true);
			}
			T2DCalc = true;	
			Constants.setUseMiniStagger(true);
			
			System.out.println("   SETTING RUN-DEPENDENT CONSTANTS, T0 = "+Constants.getT0()+ " use ministagger "+Constants.getUseMiniStagger());
			CalibrationConstantsLoader.Load(newRun, "default");
			
			TableLoader.Fill();
			
			GeometryLoader.Load(newRun, "default");
		}
		Run = newRun;

		
		// Load the fields
		//-----------------
		String newConfig = "SOLENOID"+bank.getFloat("solenoid",0)+"TORUS"+bank.getFloat("torus",0)+"RUN"+bank.getInt("run", 0);		
		//System.out.println(" fields "+newConfig);
		if (FieldsConfig.equals(newConfig)==false) {
			// Load the Constants
			double TorScale = (double)bank.getFloat("torus",0);
			//TorScale = -0.5;
			
			Constants.Load(T2DCalc, isCalib, TorScale); // set the T2D Grid for Cosmics data only so far....
			// Load the Fields
			//DCSwimmer.setMagneticFieldsScales(1.0, bank.getFloat("Torus")[0]); // something changed in the configuration ... 
			DCSwimmer.setMagneticFieldsScales((double)bank.getFloat("solenoid",0), TorScale); // something changed in the configuration ... 
		}
		FieldsConfig = newConfig;
		
	}
	public static void main(String[] args) throws FileNotFoundException, EvioException {

        //String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/clas_000767_000.hipo";
        //String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/clas12_000797_a00000.hipo";
        //String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/e2to6hipo.hipo";
        // String inputFile="/Users/ziegler/Downloads/out.hipo";
        //String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/Run758.hipo";
        //String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/old/RaffaNew.hipo";
        //String inputFile = args[0];
        //String outputFile = args[1];
        String inputFile="/Users/ziegler/Workdir//Files/GEMC/TestDCOnlyE3.hipo";
        //String inputFile="/Users/ziegler/Workdir/Files/test/electron_fd_t0.8torus.hipo";
       // String inputFile = "/Users/ziegler/Workdir/Files/Data/DecodedData/DC/big.806.pass4.2trackstdc.hipo";
        //System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        DCHBEngine en = new DCHBEngine();
        en.init();
        DCTBEngine en2 = new DCTBEngine();
        en2.init();

        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        //String outputFile="/Users/ziegler/Workdir/Distribution/DCTest_797D.hipo";
       // String outputFile="/Users/ziegler/Workdir/Files/test/electron_fd_rcF3.hipo";
        String outputFile = "/Users/ziegler/Workdir//Files/GEMC/TestDCOnlyE2.rec3.test.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent()) {

            counter++;

            DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }

            en.processDataEvent(event);

            // Processing TB   
            en2.processDataEvent(event);
            System.out.println("  EVENT "+counter);
            if (counter > 160000) {
                break;
            }
            //event.show();
            //if(counter%100==0)
            System.out.println("*************************************************************run " + counter + " events");
            if(event.hasBank("TimeBasedTrkg::TBTracks")) {
               // event.show();
                writer.writeEvent(event);
            }
        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
    }
}