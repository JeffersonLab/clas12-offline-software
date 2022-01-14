package org.jlab.rec.cvt.services;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.straight.KFitter;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;
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
import org.jlab.rec.cvt.fit.CosmicFitter;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.Measurements;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

import cnuphys.magfield.MagneticFields;
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
        
	MagneticFields.getInstance().getSolenoid().setScaleFactor(1e-7);
        MagneticFields.getInstance().getTorus().setScaleFactor(1e-7); 
        // make list of crosses consistent with a track candidate using SVT only first
        StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, 3);
        if (crosslist == null || crosslist.isEmpty()) {
            // create the clusters and fitted hits banks
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);

            return true;
        } 
        // refit track based on SVT only and then add BMT and refit again
        TrackCandListFinder trkcandFinder = new TrackCandListFinder();
        List<StraightTrack> cosmicCands = trkcandFinder.getStraightTracks(crosslist, crosses.get(1));
        List<Track> trkcands = new ArrayList<>();
        //REMOVE THIS
        //crosses.get(0).addAll(crosses.get(1));
        //------------------------
        if (cosmicCands.isEmpty()) {
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
            return true;
        }
        
        if(Constants.excludeLayers==true) {
            CosmicFitter fitTrk = new CosmicFitter();
            cosmicCands = recUtil.reFit(cosmicCands, fitTrk,  trkcandFinder);
        }
        
        if (cosmicCands.size() > 0) {
            for (int k1 = 0; k1 < cosmicCands.size(); k1++) {
                cosmicCands.get(k1).set_Id(k1 + 1);
                for (int k2 = 0; k2 < cosmicCands.get(k1).size(); k2++) { 
                    cosmicCands.get(k1).get(k2).set_AssociatedTrackID(cosmicCands.get(k1).get_Id()); // associate crosses
                    if (cosmicCands.get(k1).get(k2).get_Cluster1() != null) {
                        cosmicCands.get(k1).get(k2).get_Cluster1().set_AssociatedTrackID(cosmicCands.get(k1).get_Id()); // associate cluster1 in cross
                    }
                    if (cosmicCands.get(k1).get(k2).get_Cluster2() != null) {
                        cosmicCands.get(k1).get(k2).get_Cluster2().set_AssociatedTrackID(cosmicCands.get(k1).get_Id()); // associate cluster2 in cross	
                    }
                }
                trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), true,
                        cosmicCands.get(k1).get_Trajectory(), k1 + 1);
            }
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
            
            KFitter kf = new KFitter(Constants.KFFILTERON, Constants.KFITERATIONS, Constants.kfBeamSpotConstraint(), Constants.kfMatLib);
            Measurements measures = new Measurements(true, null);
            List<StraightTrack> cosmics = new ArrayList<>();
            for (int k1 = 0; k1 < cosmicCands.size(); k1++) {
                Ray ray = cosmicCands.get(k1).get_ray();
                
                if(Constants.INITFROMMC) {
                    double[] pars = recUtil.MCtrackPars(event);
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
                kf.init(ray.get_yxinterc(),ray.get_yzinterc(),
                        ray.get_yxslope(), ray.get_yzslope(), 10.0, cov,
                        measures.getMeasurements(cosmicCands.get(k1)));
                kf.mv.setDelta_d_a(new double[]{0.1, 0.1, 0.0001, 0.0001, 1});
                kf.runFitter();
                if (kf.setFitFailed == false && kf.NDF>0 && kf.finalStateVec!=null) { 
                    StraightTrack cosmic = cosmicCands.get(k1);
                    cosmic.update(kf);
                    
                    //refit adding missing clusters
                    List<Cluster> clsOnTrack = recUtil.FindClustersOnTrack(SVTclusters, cosmic);
                    if(clsOnTrack.size()>0) {
                        List<Cross> pseudoCrosses = new ArrayList<>();
                        for(Cluster cl : clsOnTrack) {
                            cl.set_AssociatedTrackID(k1 + 1);
                            //make pseudo-cross
                            Cross this_cross = new Cross(DetectorType.BST, BMTType.UNDEFINED, cl.get_Sector(), cl.get_Region(), -1);
                            // cluster1 is the inner layer cluster
                            if(cl.get_Layer()%2==1) {
                                this_cross.set_Cluster1(cl);
                            } else {
                            // cluster2 is the outer layer cluster
                                this_cross.set_Cluster2(cl);
                            }
                            this_cross.set_Point0(cl.getTrakInters());
                            this_cross.set_Point(cl.getTrakInters());
                            pseudoCrosses.add(this_cross);
                        }
                        cosmic.addAll(pseudoCrosses); //VZ check for additional clusters, and only then re-run KF adding new clusters                    
                        //refit
                        kf.init(cosmic.get_ray().get_yxinterc(),cosmic.get_ray().get_yzinterc(),
                                cosmic.get_ray().get_yxslope(), cosmic.get_ray().get_yzslope(), 10.0, cov,
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
                    c.set_AssociatedTrackID(-1);
                }
            }
            for(Cluster c : SVTclusters) {
                c.set_AssociatedTrackID(-1);
            }
            for(Cluster c : BMTclusters) {
                c.set_AssociatedTrackID(-1);
            }
            if(!cosmics.isEmpty()) {
                for(int k = 0; k < cosmics.size(); k++) {
                    cosmics.get(k).set_Id(k + 1);
                    cosmics.get(k).updateCrosses();
                    cosmics.get(k).updateClusters();
                }
            }
            for(int det = 0; det<2; det++) {
                for(Cross c : crosses.get(det)) {
                    if(c.get_AssociatedTrackID()==-1) {
                        c.reset();
                    }
                }
            }
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, cosmics);
        }
        return true;
        }
    
}
