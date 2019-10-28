package org.jlab.rec.cvt.services;

//import cnuphys.magfield.MagneticFields;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.detector.cnd.CNDFactory;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.CentralTracker;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.track.TrackListFinder;
import org.jlab.rec.cvt.track.TrackSeederCA;
import org.jlab.rec.cvt.track.fit.KFitter;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

/**
 * 
 * @author mdefurne
 * @author fbossu
 * 
 */

public class CVTRecHandler {

    RecoBankWriter rbc = new RecoBankWriter();
    org.jlab.rec.cvt.svt.Geometry SVTGeom;
    org.jlab.rec.cvt.bmt.Geometry BMTGeom;
    CTOFGeant4Factory CTOFGeom;
    Detector          CNDGeom ;
    SVTStripFactory svtIdealStripFactory;
    CentralTracker CVT;
    
    public CVTRecHandler() {
    	DatabaseConstantProvider cp=new DatabaseConstantProvider(10, "default");
        org.jlab.detector.geant4.v2.SVT.SVTConstants.connect(cp);
        SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
        BMTGeom = new org.jlab.rec.cvt.bmt.Geometry();
    }

    public CVTRecHandler(org.jlab.rec.cvt.svt.Geometry svtg, org.jlab.rec.cvt.bmt.Geometry bmtg, CTOFGeant4Factory ctofg, Detector cndg) {
        SVTGeom  = svtg;
        BMTGeom  = bmtg;
        CTOFGeom = ctofg;
        CNDGeom  = cndg;
        CVT=new CentralTracker();
    }
    
    String FieldsConfig = "";
    int Run = -1;
  
    public void setRunConditionsParameters(DataEvent event, String Fields, int iRun, boolean addMisAlignmts, String misAlgnFile) {
        // -----------------------------------------------------------------------------------------------------
        // deprecated method, here only for debug mode purposes. Please use the CVTReconstruction method instead
        // -----------------------------------------------------------------------------------------------------
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return;
        }

        int Run = iRun;

        boolean isCosmics = false;
        DataBank bank = event.getBank("RUN::config");
        //System.out.println(bank.getInt("Event")[0]);
        if (bank.getByte("type", 0) == 0) {
        }
        if (bank.getByte("mode", 0) == 1) {
            isCosmics = true;
        }

        boolean isSVTonly = false;

        // Load the fields
        //-----------------
        String newConfig = "SOLENOID" + bank.getFloat("solenoid", 0);

        if (Fields.equals(newConfig) == false) {
            // Load the Constants
            
            System.out.println("  CHECK CONFIGS..............................." + FieldsConfig + " = ? " + newConfig);
            Constants.Load(isCosmics, isSVTonly, (double) bank.getFloat("solenoid", 0));
        }
        this.setFieldsConfig(newConfig);

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);

        if (Run != newRun) {
        	// load new HV settings for BMT
        	CCDBConstantsLoader.loadHVsettings(new DatabaseConstantProvider(bank.getInt("run", 0), "default"));
            this.setRun(newRun);
        }
      
        Run = newRun;
        this.setRun(Run);
    }

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    public String getFieldsConfig() {
        return FieldsConfig;
    }

    public void setFieldsConfig(String fieldsConfig) {
        FieldsConfig = fieldsConfig;
    }
    
    List<Cluster> clusters    = null;
    
    public org.jlab.rec.cvt.bmt.Geometry getBMTGeom() {
		return BMTGeom;
	}

	public void setBMTGeom(org.jlab.rec.cvt.bmt.Geometry bMTGeom) {
		BMTGeom = bMTGeom;
	}

	public org.jlab.rec.cvt.svt.Geometry getSVTGeom() {
		return SVTGeom;
	}

	public void setSVTGeom(org.jlab.rec.cvt.svt.Geometry sVTGeom) {
		SVTGeom = sVTGeom;
	}

	public CentralTracker getCVT() {
		return CVT;
	}
	
    public boolean loadClusters(DataEvent event) {
        clusters = new ArrayList<Cluster>();
        ADCConvertor adcConv = new ADCConvertor();
        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, adcConv, -1, -1, SVTGeom);
        hitRead.fetch_BMTHits(event, adcConv, BMTGeom);

        List<Hit> hits = new ArrayList<Hit>();
        //I) get the hits
        List<Hit> svt_hits = hitRead.get_SVTHits();
        if (svt_hits != null && svt_hits.size() > 0) {
            hits.addAll(svt_hits);
        }

        List<Hit> bmt_hits = hitRead.get_BMTHits();
        if (bmt_hits != null && bmt_hits.size() > 0) {
            hits.addAll(bmt_hits);
        }

        //II) process the hits		
        //1) exit if hit list is empty
        if (hits.size() == 0) {
            return false;
        }
        
        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        clusters.addAll(clusFinder.findClusters(svt_hits, BMTGeom));
        
        clusters.addAll(clusFinder.findClusters(bmt_hits, BMTGeom)); 
        
        if (clusters.size() == 0) {
            return false;
        }
        
        // fill the fitted hits list.
        if (clusters.size() != 0) {
            for (int i = 0; i < clusters.size(); i++) {
            	CVT.addCluster(clusters.get(i));
            }
        }
        
    	return true;
    }
    
    List<ArrayList<Cross>> crosses = null;
    public void loadCrosses() {
    	 crosses = new ArrayList<ArrayList<Cross>>();
         CrossMaker crossMake = new CrossMaker();
         crosses = crossMake.findCrosses(clusters, SVTGeom);
    }
    
    public List<ArrayList<Cross>> getCrosses() {
		return crosses;
	}

	public void setCrosses(List<ArrayList<Cross>> crosses) {
		this.crosses = crosses;
	}

	List<Track> trks = null;
	public List<Track> getTrks() {
		return trks;
	}
	public void setTrks(List<Track> trks) {
		this.trks = trks;
	}

	
	public List<StraightTrack> cosmicsTracking(){
		if( this.crosses == null ) return null;
		
        StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, SVTGeom,
                BMTGeom, 3);
        if (crosslist == null || crosslist.size() == 0) {
            return null;
        }
        
        List<StraightTrack> cosmics = new ArrayList<StraightTrack>();

        TrackCandListFinder trkcandFinder = new TrackCandListFinder();
        cosmics = trkcandFinder.getStraightTracks(crosslist, crosses.get(1), SVTGeom, BMTGeom);
        
        //------------------------
        if (cosmics.size() == 0) {
            this.CleanupSpuriousCrosses(crosses, null) ;
            return null;
        }

        for (int k1 = 0; k1 < cosmics.size(); k1++) {
        	cosmics.get(k1).set_Id(k1 + 1);
        	for (int k2 = 0; k2 < cosmics.get(k1).size(); k2++) {
        		cosmics.get(k1).get(k2).set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate crosses
        		if (cosmics.get(k1).get(k2).get_Cluster1() != null) {
        			cosmics.get(k1).get(k2).get_Cluster1().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster1 in cross
        		}
        		if (cosmics.get(k1).get(k2).get_Cluster2() != null) {
        			cosmics.get(k1).get(k2).get_Cluster2().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster2 in cross	
        		}
        		if (cosmics.get(k1).get(k2).get_Cluster1() != null) {
        			for (int k3 = 0; k3 < cosmics.get(k1).get(k2).get_Cluster1().size(); k3++) { //associate hits
        				cosmics.get(k1).get(k2).get_Cluster1().get(k3).set_AssociatedTrackID(cosmics.get(k1).get_Id());
        			}
        		}
        		if (cosmics.get(k1).get(k2).get_Cluster2() != null) {
        			for (int k4 = 0; k4 < cosmics.get(k1).get(k2).get_Cluster2().size(); k4++) { //associate hits
        				cosmics.get(k1).get(k2).get_Cluster2().get(k4).set_AssociatedTrackID(cosmics.get(k1).get_Id());
        			}
        		}
        	}
        	//trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), SVTGeom, BMTGeom, true, cosmics.get(k1).get_Trajectory(), k1 + 1);
        }
        this.CleanupSpuriousCrosses(crosses, null) ;
        //4)  ---  write out the banks			

        return cosmics;
	}
	
	public List<Track> beamTracking(Swim swimmer){
		
		if( this.crosses == null ) return null;
		TrackSeederCA trseed = new TrackSeederCA();
        
        KFitter kf;
        List<Track> trks = new ArrayList<Track>();
        
        //List<Seed> seeds = trseed.findSeed(SVTclusters, SVTGeom, crosses.get(1), BMTGeom);
        List<Seed> seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, swimmer, CVT);
//      System.out.println("  BEAM TRACKING    seeds " + seeds.size());
        for (Seed seed : seeds) { 
        	kf = new KFitter(seed, SVTGeom, swimmer );
            kf.runFitter(SVTGeom, BMTGeom, swimmer);
            //System.out.println(" OUTPUT SEED......................");
            trks.add(kf.OutputTrack(seed, SVTGeom, BMTGeom, swimmer));
            if (kf.setFitFailed == false) {
                trks.get(trks.size() - 1).set_TrackingStatus(2);
           } else {
                trks.get(trks.size() - 1).set_TrackingStatus(1);
           }
        }

        TrackListFinder trkFinder = new TrackListFinder();
        trkFinder.removeBadTracks(trks); // If chi2 is very bad, we delete it before doing the overlapping track study

        // overlapping tracks are searched first in the XY plane and then, to remove remaining duplicates for BMTC matching in "ZR"
        trkFinder.removeOverlappingTracks(trks, "XY"); //Determine which track is the best if they share two measurements
        trkFinder.removeOverlappingTracks(trks, "ZR"); //Determine which track is the best if they share two measurements

        trkFinder.updateCrosses(trks, crosses); //Once we have kept only good tracks, we can update the cross.
 
        trkFinder.FinalizeTrackToCTOF_CND(trks,CTOFGeom,CNDGeom,swimmer); //Get Intersection with CTOF and CND

        return trks;
    }


    private void CleanupSpuriousCrosses(List<ArrayList<Cross>> crosses, List<Track> trks) {
        List<Cross> rmCrosses = new ArrayList<Cross>();
        
        if( crosses.get(0) != null ) {
	        for(Cross c : crosses.get(0)) {
	            double z = SVTGeom.transformToFrame(c.get_Sector(), c.get_Region()*2, c.get_Point().x(), c.get_Point().y(),c.get_Point().z(), "local", "middle").z();
	            if(z<-0.1 || z>SVTConstants.MODULELEN) {
	                rmCrosses.add(c);
	            }
	        }
        
        
	        for(int j = 0; j<crosses.get(0).size(); j++) {
	            for(Cross c : rmCrosses) {
	                if(crosses.get(0).get(j).get_Id()==c.get_Id())
	                    crosses.get(0).remove(j);
	            }
	        } 
        }
       
        if(trks!=null && rmCrosses!=null) {
            List<Track> rmTrks = new ArrayList<Track>();
            for(Track t:trks) {
                boolean rmFlag=false;
                for(Cross c: rmCrosses) {
                    if(c!=null && t!=null && c.get_AssociatedTrackID()==t.get_Id())
                        rmFlag=true;
                }
                if(rmFlag==true)
                    rmTrks.add(t);
            }
            trks.removeAll(rmTrks);
        }
    }

    public boolean init() {
        System.out.println(" ........................................ trying to connect to db ");
        CVTReconstruction cvtrec = new CVTReconstruction();
        cvtrec.init();
        return true;
    }


}
