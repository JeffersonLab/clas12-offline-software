package org.jlab.service.fvt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.io.base.*;

import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.banks.RecoBankWriter;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fmt.hit.Hit;
import org.jlab.rec.fmt.CCDBConstantsLoader;
import org.jlab.rec.fvt.track.Track;
import org.jlab.rec.fvt.track.Trajectory;
import org.jlab.rec.fvt.track.fit.KFitter;

/**
 * Service to return reconstructed track candidates - the output is in hipo format
 *
 * @author ziegler, benkel, devita
 */
public class FVTEngine extends ReconstructionEngine {

    boolean debug = false;
    boolean dropBanks = false;
    boolean alreadyDroppedBanks = false;

    public FVTEngine() {
        super("FVT", "ziegler", "4.0");
    }

    @Override
    public boolean init() {
        
        if (this.getEngineConfigString("dropBanks")!=null &&
            this.getEngineConfigString("dropBanks").equals("true")) {
            dropBanks=true;
        }

        
        // Get the constants for the correct variation
        String geoVariation = this.getEngineConfigString("variation");
        if (geoVariation!=null) {
            System.out.println("["+this.getName()+"] " +
                    "run with FMT geometry variation based on yaml = " + geoVariation);
        }
        else {
            geoVariation = "default";
            System.out.println("["+this.getName()+"] run with FMT default geometry");
        }
        

        String[] tables = new String[]{
            "/geometry/beam/position",
            "/geometry/fmt/alignment"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation(geoVariation);

        // Load the geometry
        int run = 10;
        double[][] shiftsArray =
                CCDBConstantsLoader.loadAlignmentTable(run, this.getConstantsManager());

        CCDBConstantsLoader.Load(run, geoVariation);
        Constants.saveAlignmentTable(shiftsArray);
        Constants.applyZShifts();
        Constants.Load();
        Constants.applyXYShifts();


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
                
                
        // Set swimmer.
        Swim swimmer = new Swim();

        // Set beam shift. NOTE: Set to zero for the time being, when beam alignment is done
        //                       uncomment this code.
        IndexedTable beamOffset =
                this.getConstantsManager().getConstants(run, "/geometry/beam/position");
        double xB = 0; // beamOffset.getDoubleValue("x_offset", 0,0,0);
        double yB = 0; // beamOffset.getDoubleValue("y_offset", 0,0,0);

        // === HITS ================================================================================
        List<Hit> hits = Hit.fetchHits(event);
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
        Map<Integer,Track> tracks = Track.getDCTracks(event);
        if(tracks.size()==0) return true;
        
        // === SEEDS =============================================================================
        for(Entry<Integer,Track> entry: tracks.entrySet()) {
            Track track = entry.getValue();                
           
            double[] doca = new double[Constants.FVT_Nlayers];
            for(int i=0; i<Constants.FVT_Nlayers; i++) doca[i]=Constants.CIRCLECONFUSION;
                
            for(int j=0; j<clusters.size(); j++) {
                    
                Cluster cluster = clusters.get(j);                    
                
                Trajectory trj = track.getDCTraj(cluster.get_Layer());
                if (trj==null) continue; // Match the layers from traj.

                double d = cluster.distance(trj.getPosition());
//                System.out.println(trj.toString());
//                System.out.println(cluster.toStringBrief());
//                System.out.println(d);
                if (d < doca[cluster.get_Layer()-1]) {
                    track.setCluster(cluster);
                    track.getCluster(cluster.get_Layer()).set_Doca(d);
                    doca[cluster.get_Layer()-1] = d;
//                    Cross cross = new Cross(clusters.get(j).get_Sector(),
//                            clusters.get(j).get_Layer(), crosses.size()+1);
//                    cross.set_Point(clusters.get(j).calcCross(trj.getPosition()));
//                    cross.set_AssociatedTrackID(track.getId());
//                    cross.set_Cluster1(clusters.get(j));
//                    cross.get_Cluster1().set_Doca(d);
//                    cross.get_Cluster1().set_TrackID(track.getId());
//                    crosses.add(cross);
//                    // add to seed
//                    if (crossesMap.get(track.getId()) == null) {
//                        crossesMap.put(track.getId(), new ArrayList<Cross>());
//                        crossesMap.get(track.getId()).add(cross);
//                    } else {
//                        crossesMap.get(track.getId()).add(cross);
//                    }
                }
            }
            
            for(int i=0; i<Constants.FVT_Nlayers; i++) {
                if(track.getCluster(i+1)!=null) {
                    if(track.getCluster(i+1).get_TrackIndex()<=0) {
                        track.getCluster(i+1).set_TrackIndex(track.getIndex());
                    }
                    else System.out.println("WARNING: double cluster assignment");
                }
            }
        }

        // === TRACKS ==============================================================================
        KFitter kf = null;
        double[] pars = new double[6];
        
        // Iterate on hashmap to run the fit.
        for(Entry<Integer,Track> entry: tracks.entrySet()) {
            Track track = entry.getValue();                
            int   id    = entry.getKey();
            
            for (int p = 0; p < 6; p++) pars[p] = 0;
                
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
            track.status = trackClusters.size();
            if (track.status == 0) continue;

            kf = new KFitter(track, swimmer, 0);
            kf.runFitter(track.getSector());

            // Do one last KF pass with filtering off to get the final Chi^2.
            kf.totNumIter = 1;
            kf.filterOn   = false;
            kf.runFitter(track.getSector());

            if (kf.finalStateVec != null) {
                // Set the track parameters.
                track.setQ((int)Math.signum(kf.finalStateVec.Q));
                track.setChi2(kf.chi2);
                pars = track.getLabPars(kf.finalStateVec);
                swimmer.SetSwimParameters(pars[0],pars[1],pars[2],-pars[3],-pars[4],-pars[5],
                        -track.getQ());

                double[] Vt = swimmer.SwimToBeamLine(xB, yB);
                if (Vt == null) continue;

                track.setX(Vt[0]);
                track.setY(Vt[1]);
                track.setZ(Vt[2]);
                track.setPx(-Vt[3]);
                track.setPy(-Vt[4]);
                track.setPz(-Vt[5]);
            }
        }
        
        // propagate fit information to cluster/hit and write banks
        for(Entry<Integer,Track> entry : tracks.entrySet()) {
            Track track = entry.getValue();
            for(int layer=1; layer<=Constants.FVT_Nlayers; layer++) {
                if(track.getFMTtraj(layer)!=null) {
                    double localY = track.getFMTtraj(layer).getLocalY();
                    track.getCluster(layer).set_CentroidResidual(localY);
                }
            }
            if(debug) System.out.println("Track " + track.toString());
        }
        
        RecoBankWriter rbc = new RecoBankWriter();
        rbc.appendFMTBanks(event, fittedhits, clusters, tracks);

        return true;
   }
    
    public void dropBanks(DataEvent de) {
        if (this.alreadyDroppedBanks==false) {
            System.out.println("["+this.getName()+"]  dropping FMTRec banks!\n");
            this.alreadyDroppedBanks=true;
        }
        de.removeBank("FMTRec::Hits");
        de.removeBank("FMTRec::Clusters");
        de.removeBank("FMTRec::Crosses");
        de.removeBank("FMTRec::Tracks");
        
    }

}
