package org.jlab.service.fmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.io.base.*;
import org.jlab.rec.fmt.Constants;

import org.jlab.rec.fmt.banks.RecoBankWriter;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fmt.hit.Hit;
import org.jlab.rec.fmt.track.Track;
import org.jlab.rec.fmt.track.Trajectory;
import org.jlab.rec.fmt.track.fit.KFitter;
import org.jlab.rec.fmt.track.fit.StateVecs.StateVec;

/**
 * Service to return reconstructed track candidates - the output is in hipo format
 *
 * @author ziegler, benkel, devita
 */
public class FMTEngine extends ReconstructionEngine {

    boolean debug = false;
    boolean dropBanks = false;
    boolean alreadyDroppedBanks = false;

    public FMTEngine() {
        super("FMT", "ziegler", "5.0");
    }

    @Override
    public boolean init() {
        
        if (this.getEngineConfigString("dropBanks")!=null &&
            this.getEngineConfigString("dropBanks").equals("true")) {
            dropBanks=true;
        }

        
        // Get the constants for the correct variation
        String variation = this.getEngineConfigString("variation");
        if (variation!=null) {
            System.out.println("["+this.getName()+"] " +
                    "run with FMT geometry variation based on yaml = " + variation);
        }
        else {
            variation = "default";
            System.out.println("["+this.getName()+"] run with FMT default geometry");
        }
        

        String[] tables = new String[]{
            "/geometry/beam/position",
            "/calibration/mvt/fmt_status"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation(variation);

        // Load the geometry
        int run = 10;
        Constants.setDetector(GeometryFactory.getDetector(DetectorType.FMT,run, variation));
//        System.out.println(Geometry.getZ(1) + " " + fmtDetector.getSector(0).getSuperlayer(0).getLayer(0).getPlane().point().toString());
//        for(int il=0; il<fmtDetector.getSector(0).getSuperlayer(0).getNumLayers(); il++) {
//            FMTLayer layer = (FMTLayer) fmtDetector.getSector(0).getSuperlayer(0).getLayer(il);
//            Transformation3D inverse = layer.getTransformation().inverse(); 
//            for(int is=0; is<layer.getNumComponents(); is++) {
//                TrackerStrip strip = layer.getComponent(is);
//                Line3D local = new Line3D(strip.getLine());
//                inverse.apply(local);
//                System.out.println("Layer/strip=" + (il+1) + "/" + (is+1) + " " + strip.getWidth() + " " + strip.getThickness());
//                System.out.println(strip.getLine().toString());
//                System.out.println(Geometry.stripLocal[is].toString());
//            }
//        }
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        // Initial setup.
        if(debug) System.out.println("\nNew event");
        
        if (this.dropBanks==true) this.dropBanks(event);        
        
        // Set run number.
        DataBank runConfig = event.getBank("RUN::config");
        if (runConfig == null || runConfig.rows() == 0) return true;
        int run         = runConfig.getInt("run", 0);
        int eventNumber = runConfig.getInt("event", 0);
                
        if (run<=0) {
            System.out.println("FMTEngine:  found no run number, CCDB constants not loaded, skipping event.");
            return false;
        }
                
        // Set swimmer.
        Swim swimmer = new Swim();

        // Set beam shift. NOTE: Set to zero for the time being, when beam alignment is done
        //                       uncomment this code.
        IndexedTable beamOffset = this.getConstantsManager().getConstants(run, "/geometry/beam/position");
        double xB = 0; // beamOffset.getDoubleValue("x_offset", 0,0,0);
        double yB = 0; // beamOffset.getDoubleValue("y_offset", 0,0,0);
        
        double xB = beamOffset.getDoubleValue("x_offset", 0,0,0);
        double yB = beamOffset.getDoubleValue("y_offset", 0,0,0);
        // get status table
        IndexedTable status = this.getConstantsManager().getConstants(run, "/calibration/mvt/fmt_status");

        // === HITS ================================================================================
        List<Hit> hits = Hit.fetchHits(event, status);
        if (hits.size() == 0) return true;
        
        // === CLUSTERS ============================================================================
        List<Cluster> clusters = Cluster.findClusters(hits);
        if(debug) for (int i = 0; i < clusters.size(); i++) System.out.println(clusters.get(i).toString());

        // === FITTED HITS =========================================================================
        List<FittedHit> fittedhits =  new ArrayList<FittedHit>();
        for (int i = 0; i < clusters.size(); i++) fittedhits.addAll(clusters.get(i)); 
        // set cluster seed indices
        for(int i=0; i<fittedhits.size(); i++) {
            FittedHit hit = fittedhits.get(i);
            if(hit.get_Strip()==clusters.get(hit.get_ClusterIndex()).get_SeedStrip())  
                clusters.get(hit.get_ClusterIndex()).setSeedIndex(i); 
        }
        
        // === DC TRACKS ===========================================================================
        List<Track> tracks = Track.getDCTracks(event);
        if(tracks.size()==0) return true;
        
        // === SEEDS =============================================================================
        for(int i=0; i<tracks.size(); i++) {
            Track track = tracks.get(i);                
           
            for(int j=0; j<clusters.size(); j++) {
                    
                Cluster cluster = clusters.get(j);                    
                
                Trajectory trj = track.getDCTraj(cluster.get_Layer());
                if (trj==null) continue; 

                // Match the layers from traj.
                double d = cluster.distance(trj.getPosition());
                if (d < Constants.CIRCLECONFUSION) {
                    track.addCluster(cluster);
//                    track.getCluster(cluster.get_Layer()).set_Doca(d);
//                    doca[cluster.get_Layer()-1] = d;
                }
            }
            
            if(debug) System.out.println("After first matching\n" + track.toString());
            
            track.filterClusters(0);
            
            if(debug) System.out.println("After cluster filtering\n" + track.toString());
        }
            
        // === Check for duplicate cluster assignment ============================================
        boolean clusterDoubleAssignment = false;
        for(int i=0; i<tracks.size(); i++) {
            Track track = tracks.get(i);                
           
            for(int layer=1; layer<=Constants.NLAYERS; layer++) {
                Cluster cluster = track.getCluster(layer);
                if(cluster!=null) {
                    double doca = cluster.distance(track.getDCTraj(layer).getPosition());
                    // cluster is not assigned
                    if(cluster.get_TrackIndex()<0) {
                        cluster.set_TrackIndex(track.getIndex());
                        cluster.set_Doca(doca);
                    }
                    // cluster is already assigned
                    else {
                        if(debug) System.out.println("WARNING: double cluster assignment for cluster "
                                  + cluster.get_Index() + " tracks " + cluster.get_TrackIndex() + "/" + track.getIndex());
                        clusterDoubleAssignment = debug;
                        Track other = tracks.get(cluster.get_TrackIndex());
                        if(track.getSeedQuality()>other.getSeedQuality()) {
                            other.clearClusters(layer);
                            cluster.set_TrackIndex(track.getIndex());
                            cluster.set_Doca(doca);                        
                        }
                    }
                }
            }
        }

        // === TRACKS ==============================================================================
        KFitter kf = null;
        
        // Iterate on list to run the fit.
        for(int i=0; i<tracks.size(); i++) {
            Track track = tracks.get(i);                
                            
//            List<Cross> crs = crossesMap.get(id);
//            for(Cross c : crs) {
//                // Filter clusters to use only the best cluster (minimum Tmin) per FMT layer.
//                int lyr = c.get_Cluster1().get_Layer();
////                System.out.println(c.get_Cluster1().get_Doca());
//                if (cls.get(lyr) == null || c.get_Cluster1().get_Doca()< cls.get(lyr).get_Doca())
//                    cls.put(lyr, c.get_Cluster1());
//            }

            // Set status and stop if there are no measurements to fit against.
            List<Cluster> trackClusters = track.getClusters();
            if (trackClusters.isEmpty()) continue;

            kf = new KFitter(track, swimmer, 0);
            kf.runFitter(track.getSector());

            // Do one last KF pass with filtering off to get the final Chi^2.
            kf.totNumIter = 1;
            kf.filterOn   = false;
            kf.runFitter(track.getSector());

            if (kf.finalStateVec != null) {
                StateVec sv = kf.finalStateVec;
                
                // swim to beamline to get vertex parameters
                int charge = (int)Math.signum(sv.Q);
                swimmer.SetSwimParameters(sv.x,sv.y,sv.z,-sv.getPx(),-sv.getPy(),-sv.getPz(),-charge);
                double[] Vt = swimmer.SwimToBeamLine(xB, yB);
                
                // if successful, save track parameters
                if(Vt == null || Vt[6]<Constants.MIN_SWIM_PATH) continue;
                track.setStatus(1);
                track.setNDF(trackClusters.size());
                track.setQ(charge);
                track.setChi2(kf.chi2);
                track.setX(Vt[0]);
                track.setY(Vt[1]);
                track.setZ(Vt[2]);
                track.setPx(-Vt[3]);
                track.setPy(-Vt[4]);
                track.setPz(-Vt[5]);
            }
        }
        
        // propagate fit information to cluster/hit and write banks
        for(int i=0; i<tracks.size(); i++) {
            Track track = tracks.get(i);                
            for(int layer=1; layer<=Constants.NLAYERS; layer++) {
                if(track.getFMTTraj(layer)!=null) {
                    double localY = track.getFMTTraj(layer).getLocalPosition().y();
                    track.getCluster(layer).set_CentroidResidual(localY);
                }
            }
            if(debug || clusterDoubleAssignment || track.getNDF()>3) System.out.println("Track " + track.toString());
        }
        if(clusterDoubleAssignment) for(int i=0; i<clusters.size(); i++) System.out.println(clusters.get(i).toStringBrief());
        
        RecoBankWriter rbc = new RecoBankWriter();
        rbc.appendFMTBanks(event, fittedhits, clusters, tracks);

        return true;
   }
    
    public void dropBanks(DataEvent de) {
        if (this.alreadyDroppedBanks==false) {
            System.out.println("["+this.getName()+"]  dropping FMT reconstruction banks!\n");
            this.alreadyDroppedBanks=true;
        }
        de.removeBank("FMT::Hits");
        de.removeBank("FMT::Clusters");
        de.removeBank("FMT::Crosses");
        de.removeBank("FMT::Tracks");
        
    }

}
