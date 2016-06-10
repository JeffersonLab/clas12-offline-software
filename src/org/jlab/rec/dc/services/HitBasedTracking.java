package org.jlab.rec.dc.services;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clasrec.main.DetectorReconstruction;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
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
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.DCSwimmer;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;


/**
 * Service to return reconstructed DC track candidates from Hit-based tracking.
 *
 * @author ziegler
 */
public class HitBasedTracking extends DetectorReconstruction {

    
    public HitBasedTracking() {
    	super("DCHB", "ziegler", "2.1");
    	
    }
    public void configure() {
        
        System.err.println("DCReconstruction::configure is not implemented.");
        
    } // end of configure()
    
    // init SNR 
    Clas12NoiseResult results = new Clas12NoiseResult(); 
	Clas12NoiseAnalysis noiseAnalysis = new Clas12NoiseAnalysis();

	
	
	int[] rightShifts = Constants.SNR_RIGHTSHIFTS;
	int[] leftShifts  = Constants.SNR_LEFTSHIFTS;
	NoiseReductionParameters parameters = new NoiseReductionParameters (
			2,leftShifts,
			rightShifts);
	
    int eventNb =0;
    int recNb = 0;
    
    ClusterFitter cf = new ClusterFitter();
    ClusterCleanerUtilities ct = new ClusterCleanerUtilities();
    
	@Override
	public void processEvent(EvioDataEvent event) {
		//if(event.hasBank("GenPart::true")==true)
		//	Constants.isSimulation = true;
		
		eventNb++;		
		List<FittedHit> fhits = new ArrayList<FittedHit>();
		List<FittedCluster> clusters = new ArrayList<FittedCluster>();
		List<Segment> segments = new ArrayList<Segment>();
		List<Cross> crosses = new ArrayList<Cross>();
		
		List<Track> trkcands = new ArrayList<Track>();
		
		//instantiate bank writer
		RecoBankWriter rbc = new RecoBankWriter();
		
		HitReader hitRead = new HitReader();
		hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results);

		if(Constants.DEBUGPRINTMODE==true)
			System.out.println("*********  HIT-BASED TRACKING  ********* \n event number "+eventNb);
		
		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		hits = hitRead.get_DCHits();
		
		if(Constants.DEBUGPRINTMODE==true)
			System.out.println("Nb of hits "+hits.size());
		//II) process the hits

		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return;
		}

		fhits = rbc.createRawHitList(hits);
				
		
		//2) find the clusters from these hits
			ClusterFinder clusFinder = new ClusterFinder();
			clusters = clusFinder.FindHitBasedClusters(hits, ct, cf);
			
			
			EvioDataBank effbank = (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::LayerEffs",0);
			
			if(Constants.DEBUGPRINTMODE==true)  
				System.out.println("Nb of clusters "+clusters.size());
			
			if(clusters.size()==0) {				
				rbc.fillAllHBBanks(event, rbc, fhits, null, null, null, null,effbank);
				return;
			}
		
			if(Constants.LAYEREFFS) 
				effbank = clusFinder.getLayerEfficiencies(clusters, hits, ct, cf, event);
			
			
			rbc.updateListsListWithClusterInfo(fhits, clusters);
			
			
			//3) find the segments from the fitted clusters
			SegmentFinder segFinder = new SegmentFinder();
			segments =  segFinder.get_Segments(clusters, event);
			
			if(Constants.DEBUGPRINTMODE==true)  
				System.out.println("Nb of segments "+segments.size());
			if(segments.size()==0) { // need 6 segments to make a trajectory
				
				rbc.fillAllHBBanks(event, rbc, fhits, clusters, null, null, null, effbank);
				return;
			}
								
			CrossMaker crossMake = new CrossMaker();
			crosses = crossMake.find_Crosses(segments);
			
			
			if(Constants.DEBUGPRINTMODE==true)  
				System.out.println("Nb of crosses "+crosses.size());
			if(crosses.size()==0 ) {
				
				rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, null, null, effbank);
				return;
			}
			
			
			//if(Constants.DEBUGPRINTMODE==true)  System.out.println("I found "+segments.size()+ " segments and "+crosses.size()+" crosses at event "+eventNumber);
			//5) make list of crosses consistent with a track candidate
			CrossListFinder crossLister = new CrossListFinder();
			
			List<List<Cross>> CrossesInSector = crossLister.get_CrossesInSectors(crosses);
			for(int s =0; s< 6; s++) {
				if(CrossesInSector.get(s).size()>Constants.MAXNBCROSSES) {
						//crosses.removeAll(CrossesInSector.get(s));
					if(Constants.DEBUGPRINTMODE==true) 
						System.err.println("Too many crosses in sector "+(s+1)+" -- high background event !!!");
					return;
				}
			}
			if(Constants.DEBUGPRINTMODE==true)  
				System.out.println("Nb of crosses "+crosses.size());
			
			CrossList crosslist = crossLister.candCrossLists(crosses);
			
			if(crosslist.size()==0) {
				if(Constants.DEBUGPRINTMODE==true)  
					System.out.println("No cross list found !!!");
				
				rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null, effbank);
				return;
			}

			
			
			
			
			//6) find the list of  track candidates
			TrackCandListFinder trkcandFinder = new TrackCandListFinder("HitBased");
			trkcands = trkcandFinder.getTrackCands(crosslist) ;
			
				
			if(Constants.DEBUGPRINTMODE==true)  
				System.out.println("Nb of tracks "+trkcands.size());
			if(trkcands.size()==0) {
				
				rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null,effbank); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
				return;
			}
			// track found
			
			for(Track trk: trkcands) {				
				for(Cross c : trk) {
					for(FittedHit h1 : c.get_Segment1())
						h1.set_AssociatedHBTrackID(trk.get_Id()+11);
				  	for(FittedHit h2 : c.get_Segment2())
				  		h2.set_AssociatedHBTrackID(trk.get_Id()+11);	
				}
			}
		
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands, effbank);
			if(Constants.DEBUGPRINTMODE==true)
				System.out.println("all DCHB banks should be appended !!!");
			
			recNb++;
			if(Constants.DEBUGPRINTMODE==true) {
				System.out.println("      *************************  ");
			    System.out.println("         Effciency  (%)  "+((float) recNb*100/(float)eventNb));
			    System.out.println("      *************************  ");
			}
		}

		@Override
		public void init() {
			// Load the Geometry
			if (GeometryLoader.isGeometryLoaded == false) {
				GeometryLoader.Load();
			}

			// Load the Constants
			if (Constants.areConstantsLoaded == false) {
				Constants.Load();
				System.out.println(" DB VARIATION LOADED AS "+Constants.DBVAR);
			}
			// Load the calibration constants
			if (CalibrationConstantsLoader.CSTLOADED == false) {
				CalibrationConstantsLoader.Load();
			}
			this.requireCalibration("DC");
			// Load the fields
			if (DCSwimmer.areFieldsLoaded == false) {
				DCSwimmer.getMagneticFields();
			}
			
		}
			@Override
			public void configure(ServiceConfiguration config) {
				
				System.out.println(" CONFIGURING SERVICE DCHB ************************************** ");
				config.show();
				if(config.hasItem("DATA", "mc")) {
					String isMC = config.asString("DATA", "mc");
					boolean isMCdata = Boolean.parseBoolean(isMC);
					Constants.isSimulation = isMCdata;
				}	
				
				if(config.hasItem("GEOM", "new")) {
					String inNewG = config.asString("GEOM", "new");
					boolean isNewGeom = Boolean.parseBoolean(inNewG);
					Constants.newGeometry = isNewGeom;
				}	
				
				if(config.hasItem("CCDB", "VAR")) {
					 String Variation = config.asString("CCDB", "VAR");
					 Constants.DBVAR = Variation.trim().toString();
				}	
				
				System.out.println(" CONFIGURING SERVICE DCHB **********MAG************** ");
				if(config.hasItem("MAG", "torus")) {
					Constants.TORSCALE = config.asDouble("MAG", "torus");
				}
				
				if(config.hasItem("MAG", "solenoid")) {
					Constants.SOLSCALE = config.asDouble("MAG", "solenoid")	;	System.out.println("************************* solenoid scale = "+Constants.SOLSCALE)		;	
				}
				
				
				
				if(config.hasItem("TIME", "T0")) {
					String TimeConf = config.asString("TIME", "T0");
					double t0 = Double.parseDouble(TimeConf);
					Constants.T0 = t0;
				}
				if(config.hasItem("LAYEREFFS", "on")) {
					String effs = config.asString("LAYEREFFS", "on");
					boolean layerE = Boolean.parseBoolean(effs);
					Constants.LAYEREFFS = layerE;
					if(layerE)
						Constants.isCalibrationRun = true;
					System.out.println("CALIBRATION BNKS "+Constants.isCalibrationRun);
				}
			
				
				
			}
}