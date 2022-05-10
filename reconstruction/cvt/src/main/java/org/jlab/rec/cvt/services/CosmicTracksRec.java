package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.clas.tracking.kalmanfilter.straight.KFitter;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.measurement.Measurements;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.StraightTrackCandListFinder;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;
/**
 *
 * @author ziegler
 */

public class CosmicTracksRec {
    
    private List<Cluster> SVTclusters;
    private List<Cluster> BMTclusters;
    private List<ArrayList<Cross>> CVTcrosses = new ArrayList<>();
    private List<StraightTrack> seeds;

    private final RecUtilities recUtil = new RecUtilities();
    private KFitter kf = null;
    private boolean initFromMc = false;    
    
    
    public void initKF(boolean initFromMc, boolean kfFilterOn, int kfIterations) {
        this.initFromMc = initFromMc;
        kf = new KFitter(kfFilterOn, kfIterations, Constants.KFDIR, Constants.getInstance().KFMatrixLibrary);
    }
    
    public List<StraightTrack> getSeeds(DataEvent event,   
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses) {
        
        this.SVTclusters = SVTclusters;
        this.BMTclusters = BMTclusters;
        this.CVTcrosses = crosses;   

        // make list of crosses consistent with a track candidate using SVT only first
        StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, 3);
        if (crosslist == null || crosslist.isEmpty()) 
            return null;

        // refit track based on SVT only and then add BMT and refit again
        StraightTrackCandListFinder trkcandFinder = new StraightTrackCandListFinder();
        seeds = trkcandFinder.getStraightTracks(crosslist, crosses.get(1));

        if (seeds.isEmpty()) {
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
            return null;
        }
        
//        if(Constants.getInstance().EXCLUDELAYERS==true) {
//            CosmicFitter fitTrk = new CosmicFitter();
//            cosmicCands = recUtil.reFit(cosmicCands, fitTrk,  trkcandFinder);
//        }
        
        if (seeds.size() > 0) {
            for (int k1 = 0; k1 < seeds.size(); k1++) {
                seeds.get(k1).setId(k1 + 1);
                for (int k2 = 0; k2 < seeds.get(k1).size(); k2++) { 
                    seeds.get(k1).get(k2).setAssociatedTrackID(seeds.get(k1).getId()); // associate crosses
                    if (seeds.get(k1).get(k2).getCluster1() != null) {
                        seeds.get(k1).get(k2).getCluster1().setAssociatedTrackID(seeds.get(k1).getId()); // associate cluster1 in cross
                    }
                    if (seeds.get(k1).get(k2).getCluster2() != null) {
                        seeds.get(k1).get(k2).getCluster2().setAssociatedTrackID(seeds.get(k1).getId()); // associate cluster2 in cross	
                    }
                }
                trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), true,
                        seeds.get(k1).getTrajectory(), k1 + 1);
            }
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
        }
        return seeds;
    }
        
        
    public List<StraightTrack> getTracks(DataEvent event, boolean initFromMc, boolean kfFilterOn, int kfIterations) { 
        if(this.seeds==null)
            return null;
        
        initKF(initFromMc, kfFilterOn, kfIterations);
        
        Measurements measures = new Measurements(true);
        
        List<StraightTrack> cosmics = new ArrayList<>();
        for (int k = 0; k < seeds.size(); k++) {
            Ray ray = seeds.get(k).getRay();

            if(this.initFromMc) {
                double[] pars = recUtil.mcTrackPars(event);
                Point3D  v = new Point3D(pars[0],pars[1],pars[2]);
                Vector3D p = new Vector3D(pars[3],pars[4],pars[5]);
                ray = new Ray(v,p);
            }                

            double[][] cov = Constants.COVCOSMIC;
            kf.init(ray.getYXInterc(),ray.getYZInterc(),
                    ray.getYXSlope(), ray.getYZSlope(), Units.MM, cov,
                    measures.getMeasurements(seeds.get(k)));
            kf.mv.setDelta_d_a(new double[]{0.1, 0.1, 0.0001, 0.0001, 1});
            kf.runFitter();
            if (kf.setFitFailed == false && kf.NDF>0 && kf.finalStateVec!=null) { 
                StraightTrack cosmic = new StraightTrack(seeds.get(k), kf);

                //refit adding missing clusters
                List<Cluster> clsOnTrack = recUtil.findClustersOnTrack(SVTclusters, cosmic);
                if(clsOnTrack.size()>0) {
                    List<Cross> pseudoCrosses = new ArrayList<>();
                    for(Cluster cl : clsOnTrack) {
                        cl.setAssociatedTrackID(k + 1);
                        //make pseudo-cross
                        Cross this_cross = new Cross(DetectorType.BST, BMTType.UNDEFINED, cl.getSector(), cl.getRegion(), -1);
                        // cluster1 is the inner layer cluster
                        if(cl.getLayer()%2==1) {
                            this_cross.setCluster1(cl);
                        } else {
                        // cluster2 is the outer layer cluster
                            this_cross.setCluster2(cl);
                        }
                        this_cross.setPoint0(cl.getTrakInters());
                        this_cross.setPoint(cl.getTrakInters());
                        pseudoCrosses.add(this_cross);
                    }
                    cosmic.addAll(pseudoCrosses); //VZ check for additional clusters, and only then re-run KF adding new clusters                    
                    //refit
                    kf.init(cosmic.getRay().getYXInterc(),cosmic.getRay().getYZInterc(),
                            cosmic.getRay().getYXSlope(), cosmic.getRay().getYZSlope(), Units.MM, cov,
                            measures.getMeasurements(cosmic)) ;
                    kf.runFitter();
                    if (kf.setFitFailed == false && kf.NDF>0 && kf.finalStateVec!=null) { 
                        cosmic.update(kf);
                    }
                }
                cosmics.add(cosmic);                    
            }
        }

        // reset cross and cluster IDs
        for(int det = 0; det<2; det++) {
            for(Cross c : CVTcrosses.get(det)) {
                c.setAssociatedTrackID(-1);
            }
        }
        for(Cluster c : SVTclusters) {
            c.setAssociatedTrackID(-1);
        }
        for(Cluster c : BMTclusters) {
            c.setAssociatedTrackID(-1);
        }
        if(!cosmics.isEmpty()) {
            for(int k = 0; k < cosmics.size(); k++) {
                cosmics.get(k).setId(k + 1);
                cosmics.get(k).updateCrosses();
                cosmics.get(k).updateClusters();
            }
        }
        for(int det = 0; det<2; det++) {
            for(Cross c : CVTcrosses.get(det)) {
                if(c.getAssociatedTrackID()==-1) {
                    c.reset();
                }
            }
        }
        return cosmics;
    }


    public List<Cluster> getSVTclusters() {
        if(SVTclusters==null || SVTclusters.isEmpty())
            return null;
        else
            return SVTclusters;
    }

    public List<Cluster> getBMTclusters() {
        if(BMTclusters==null || BMTclusters.isEmpty())
            return null;
        else
            return BMTclusters;
    }

    public List<Cross> getSVTcrosses() {
        if(CVTcrosses.get(0)==null || CVTcrosses.get(0).isEmpty())
            return null;
        else
            return CVTcrosses.get(0);
    }
    
    public List<Cross> getBMTcrosses() {
        if(CVTcrosses.get(1)==null || CVTcrosses.get(1).isEmpty())
            return null;
        else
            return CVTcrosses.get(1);
    }    
}