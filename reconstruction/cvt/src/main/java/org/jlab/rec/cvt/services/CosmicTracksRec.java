package org.jlab.rec.cvt.services;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.straight.KFitter;
import org.jlab.clas.tracking.trackrep.Helix.Units;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.Hit;
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
    
    private final RecUtilities recUtil = new RecUtilities();
    
    public boolean processEvent(DataEvent event,  
            List<Hit> SVThits, List<Hit> BMThits, 
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses,
            RecoBankWriter rbc, Swim swimmer) {
        
        // make list of crosses consistent with a track candidate using SVT only first
        StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, 3);
        if (crosslist == null || crosslist.isEmpty()) {
            // create the clusters and fitted hits banks
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);

            return true;
        } 
        // refit track based on SVT only and then add BMT and refit again
        StraightTrackCandListFinder trkcandFinder = new StraightTrackCandListFinder();
        List<StraightTrack> cosmicCands = trkcandFinder.getStraightTracks(crosslist, crosses.get(1));

        if (cosmicCands.isEmpty()) {
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
            return true;
        }
        
//        if(Constants.EXCLUDELAYERS==true) {
//            CosmicFitter fitTrk = new CosmicFitter();
//            cosmicCands = recUtil.reFit(cosmicCands, fitTrk,  trkcandFinder);
//        }
        
        if (cosmicCands.size() > 0) {
            for (int k1 = 0; k1 < cosmicCands.size(); k1++) {
                cosmicCands.get(k1).setId(k1 + 1);
                for (int k2 = 0; k2 < cosmicCands.get(k1).size(); k2++) { 
                    cosmicCands.get(k1).get(k2).setAssociatedTrackID(cosmicCands.get(k1).getId()); // associate crosses
                    if (cosmicCands.get(k1).get(k2).getCluster1() != null) {
                        cosmicCands.get(k1).get(k2).getCluster1().setAssociatedTrackID(cosmicCands.get(k1).getId()); // associate cluster1 in cross
                    }
                    if (cosmicCands.get(k1).get(k2).getCluster2() != null) {
                        cosmicCands.get(k1).get(k2).getCluster2().setAssociatedTrackID(cosmicCands.get(k1).getId()); // associate cluster2 in cross	
                    }
                }
                trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), true,
                        cosmicCands.get(k1).getTrajectory(), k1 + 1);
            }
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
            
            KFitter kf = new KFitter(Constants.KFFILTERON, Constants.KFITERATIONS, Constants.KFDIR, Constants.kfBeamSpotConstraint(), Constants.KFMATLIB);
            Measurements measures = new Measurements(true, 0, 0);
            List<StraightTrack> cosmics = new ArrayList<>();
            for (int k1 = 0; k1 < cosmicCands.size(); k1++) {
                Ray ray = cosmicCands.get(k1).getRay();
                
                if(Constants.INITFROMMC) {
                    double[] pars = recUtil.mcTrackPars(event);
                    Point3D  v = new Point3D(pars[0],pars[1],pars[2]);
                    Vector3D p = new Vector3D(pars[3],pars[4],pars[5]);
                    ray = new Ray(v,p);
                }                
               
                double[][] cov = new double[5][5];                
                cov[0][0]=1;
                cov[1][1]=1;
                cov[2][2]=0.001; // ~2deg
                cov[3][3]=0.001;
                cov[4][4]=1;
                kf.init(ray.getYXInterc(),ray.getYZInterc(),
                        ray.getYXSlope(), ray.getYZSlope(), Units.MM, cov,
                        measures.getMeasurements(cosmicCands.get(k1)));
                kf.mv.setDelta_d_a(new double[]{0.1, 0.1, 0.0001, 0.0001, 1});
                kf.runFitter();
                if (kf.setFitFailed == false && kf.NDF>0 && kf.finalStateVec!=null) { 
                    StraightTrack cosmic = cosmicCands.get(k1);
                    cosmic.update(kf);
                    
                    //refit adding missing clusters
                    List<Cluster> clsOnTrack = recUtil.findClustersOnTrack(SVTclusters, cosmic);
                    if(clsOnTrack.size()>0) {
                        List<Cross> pseudoCrosses = new ArrayList<>();
                        for(Cluster cl : clsOnTrack) {
                            cl.setAssociatedTrackID(k1 + 1);
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
                                cosmic.getRay().getYXSlope(), cosmic.getRay().getYZSlope(), Units.CM, cov,
                                measures.getMeasurements(cosmicCands.get(k1))) ;
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
                for(Cross c : crosses.get(det)) {
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
                for(Cross c : crosses.get(det)) {
                    if(c.getAssociatedTrackID()==-1) {
                        c.reset();
                    }
                }
            }
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, cosmics);
        }
        return true;
        }
    
}