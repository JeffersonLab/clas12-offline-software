package org.jlab.service.ftof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.ftof.CalibrationConstantsLoader;
import org.jlab.rec.ftof.Constants;
import org.jlab.rec.ftof.GeometryConstantsLoader;
import org.jlab.rec.tof.banks.ftof.HitReader;
import org.jlab.rec.tof.banks.ftof.RecoBankWriter;
import org.jlab.rec.tof.banks.ftof.TrackReader;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.cluster.ClusterFinder;
import org.jlab.rec.tof.cluster.ftof.ClusterMatcher;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.ftof.Hit;
import org.jlab.geometry.prim.Line3d;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;

/**
 * 
 * @author ziegler
 *
 */
public class FTOFEngine extends ReconstructionEngine {

	public FTOFEngine() {
		super("FTOFRec", "golovach, gavalian, carman, ziegler", "0.2");
	}

	
	@Override
	public boolean init() {
		// Load the Constants
		if (Constants.CSTLOADED == false) {
			Constants.Load();
		}
		// Load the Calibration Constants
		if (CalibrationConstantsLoader.CSTLOADED == false) {
			CalibrationConstantsLoader.Load();
		}
		if (GeometryConstantsLoader.CSTLOADED == false) {
			GeometryConstantsLoader.Load();
		}
		return true;
	}

	
	@Override
	public boolean processDataEvent(DataEvent event) {
		FTOFGeant4Factory geometry = null;
		try {
			geometry = new FTOFGeant4Factory(GeometryConstantsLoader.dbprovider);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		// Get the list of track lines which will be used for matching the FTOF hit to the DC hit
		TrackReader trkRead = new TrackReader();
		List<Line3d> trkLines = trkRead.get_TrkLines();
		double[] paths = trkRead.get_Paths();
		
		List<Hit> hits		   = new ArrayList<Hit>();		// all hits
		List<Cluster> clusters = new ArrayList<Cluster>(); 	// all clusters
		// read in the hits for FTOF
		HitReader hitRead = new HitReader();		
		hitRead.fetch_Hits(event, geometry, trkLines, paths);
		
		//1) get the hits
		List<Hit> FTOF1AHits = hitRead.get_FTOF1AHits();
		List<Hit> FTOF1BHits = hitRead.get_FTOF1BHits();
		List<Hit> FTOF2Hits  = hitRead.get_FTOF2Hits();
		
		//1.1) exit if hit list is empty
		if(FTOF1AHits!=null)
			hits.addAll(FTOF1AHits);
		if(FTOF1BHits!=null)
			hits.addAll(FTOF1BHits);
		if(FTOF2Hits!=null)
			hits.addAll(FTOF2Hits);
		
		if(hits.size()==0 ) {
			return true;			
		}
		
		//1.2) Sort the hits for clustering
		Collections.sort(hits);
		if(Constants.DEBUGMODE) { // if running in DEBUG MODE print out the reconstructed info about the hits and the clusters
			System.out.println("=============== All Hits Before clustering ===============");
			for(Hit hit : hits)
				hit.printInfo();
		}
		//2) find the clusters from these hits
		ClusterFinder clusFinder = new ClusterFinder();
		int[] npaddles = Constants.NPAD ;
		int npanels = 3;
		int nsectors = 6;
		List<Cluster> FTOF1AClusters = null;
		List<Cluster> FTOF1BClusters = null;
		List<Cluster> FTOF2Clusters  = null;
		if(FTOF1AHits!=null && FTOF1AHits.size()>0)
			FTOF1AClusters = clusFinder.findClusters(FTOF1AHits, nsectors, npanels, npaddles);
		if(FTOF1BHits!=null && FTOF1BHits.size()>0)
			FTOF1BClusters = clusFinder.findClusters(FTOF1BHits, nsectors, npanels, npaddles);
		if(FTOF2Hits!=null && FTOF2Hits.size()>0)
			FTOF2Clusters  = clusFinder.findClusters(FTOF2Hits, nsectors, npanels, npaddles);
		
		// next write results to banks		
		if(FTOF1AClusters != null)
			clusters.addAll(FTOF1AClusters);
		if(FTOF1BClusters != null)
			clusters.addAll(FTOF1BClusters);
		if(FTOF2Clusters != null)
			clusters.addAll(FTOF2Clusters);
		//2.1) exit if cluster list is empty but save the hits
		if(clusters.size()==0 ) {
			RecoBankWriter.appendFTOFBanks((EvioDataEvent) event, hits, null, null);
			return true;
		}
		// continuing ... there are clusters
		if(Constants.DEBUGMODE) { // if running in DEBUG MODE print out the reconstructed info about the hits and the clusters
			System.out.println("=============== All Hits ===============");
			for(Hit hit : hits)
				hit.printInfo();
			System.out.println("==================================================");
			for(Cluster cls : clusters) {
				cls.printInfo();
				System.out.println("contains:");
				for(AHit hit : cls)
					hit.printInfo();
				System.out.println("---------------------------------------------");
			}			
		}
		
		
		// matching ... not used at this stage...
		ClusterMatcher clsMatch = new ClusterMatcher();
		ArrayList<ArrayList<Cluster>> matchedClusters =  clsMatch.MatchedClusters(clusters);
		if(matchedClusters.size()==0 ) {
			RecoBankWriter.appendFTOFBanks((EvioDataEvent) event, hits, clusters, null);
			return true;
		}
		
		RecoBankWriter.appendFTOFBanks((EvioDataEvent) event, hits, clusters, matchedClusters);
		
		return true;
	}

}
