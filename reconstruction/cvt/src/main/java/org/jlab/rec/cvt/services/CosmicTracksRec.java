package org.jlab.rec.cvt.services;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.straight.KFitter;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.fit.CosmicFitter;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

import cnuphys.magfield.MagneticFields;
/**
 *
 * @author ziegler
 */

public class CosmicTracksRec {
    
    private final RecUtilities recUtil = new RecUtilities();
    
    public boolean processEvent(DataEvent event,  
            List<FittedHit> SVThits, List<FittedHit> BMThits, 
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses,
            SVTGeometry SVTGeom, BMTGeometry BMTGeom,
            CTOFGeant4Factory CTOFGeom, Detector CNDGeom,
            RecoBankWriter rbc,
            boolean exLayrs, Swim swimmer) {
    	MagneticFields.getInstance().getSolenoid().setScaleFactor(1e-7); 
    	MagneticFields.getInstance().getTorus().setScaleFactor(1e-7); 
        // make list of crosses consistent with a track candidate using SVT only first
        StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, SVTGeom,
                BMTGeom, 3);
        if (crosslist == null || crosslist.isEmpty()) {
            // create the clusters and fitted hits banks
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);

            return true;
        } 
        // refit track based on SVT only and then add BMT and refit again
        TrackCandListFinder trkcandFinder = new TrackCandListFinder();
        List<StraightTrack> cosmics = trkcandFinder.getStraightTracks(crosslist, crosses.get(1), SVTGeom, BMTGeom);
        List<Track> trkcands = new ArrayList<>();
        //REMOVE THIS
        //crosses.get(0).addAll(crosses.get(1));
        //------------------------
        if (cosmics.isEmpty()) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
            return true;
        }
        
        if(exLayrs==true) {
            CosmicFitter fitTrk = new CosmicFitter();
            cosmics = recUtil.reFit(cosmics, SVTGeom, fitTrk,  trkcandFinder);
        }
        
        if (cosmics.size() > 0) {
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
                trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), SVTGeom, BMTGeom, true,
                        cosmics.get(k1).get_Trajectory(), k1 + 1);
            }
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            //4)  ---  write out the banks
            List<Cross> bmtCrosses = new ArrayList<>();
            List<Cross> bmtCrossesRm = new ArrayList<>();
            
            KFitter kf = null;
            
            // uncomment to initiali KF with MC track parameters
//            double[] pars = recUtil.MCtrackPars(event);
//            Point3D v  = new Point3D(pars[0],pars[1],pars[2]);
//            Vector3D p = new Vector3D(pars[3],pars[4],pars[5]);
            for (int k1 = 0; k1 < cosmics.size(); k1++) {
                Ray ray = cosmics.get(k1).get_ray();
//                ray = new Ray(v,p);                
               
                double[][] cov = new double[5][5];
                
                cov[0][0]=ray.get_yxintercErr();
                cov[1][1]=ray.get_yzintercErr();
                cov[2][2]=ray.get_yxslopeErr();
                cov[3][3]=ray.get_yzslopeErr();
                cov[4][4]=1;
                kf = new KFitter( ray.get_yxinterc(),ray.get_yzinterc(),
                                  ray.get_yxslope(), ray.get_yzslope(), 10.0, cov, kf,
                                  recUtil.setMeasVecs(cosmics.get(k1), SVTGeom, BMTGeom, swimmer )) ;
//                kf.filterOn=false;
                kf.runFitter(swimmer);
                Map<Integer, KFitter.HitOnTrack> traj = kf.TrjPoints; 
                List<Integer> keys = new ArrayList<>();
                traj.forEach((key,value) -> keys.add(key));
                List<KFitter.HitOnTrack> trkTraj = new ArrayList<>();
                traj.forEach((key,value) -> trkTraj.add(value));
                                
                Ray the_ray = new Ray(kf.yx_slope, kf.yx_interc, kf.yz_slope, kf.yz_interc);                
                cosmics.get(k1).set_ray(the_ray);
                cosmics.get(k1).update_Crosses(cosmics.get(k1).get_ray(), SVTGeom);
                double chi2 = cosmics.get(k1).calc_straightTrkChi2(); 
                cosmics.get(k1).set_chi2(chi2);
                
                TrajectoryFinder trjFind = new TrajectoryFinder();
                
                Trajectory ntraj = trjFind.findTrajectory(k1+1, cosmics.get(k1).get_ray(), cosmics.get(k1), SVTGeom, BMTGeom);
                cosmics.get(k1).set_Trajectory(ntraj.get_Trajectory());
                trkcandFinder.upDateCrossesFromTraj(cosmics.get(k1), ntraj, SVTGeom);

                
                for(int i = 0; i< keys.size(); i++) {
                    double resi = trkTraj.get(i).resi;
                    Point3D tj = new Point3D(trkTraj.get(i).x,trkTraj.get(i).y,trkTraj.get(i).z);
                    Cluster cl = cosmics.get(k1).clsMap.get(keys.get(i));
                    int layer = cl.get_Layer();
                    int sector = cl.get_Sector();
                    
                    cosmics.get(k1).clsMap.get(keys.get(i)).setTrakInters(tj);
                    cl.set_CentroidResidual(resi);
                    if (cl.get_Detector()==DetectorType.BST) {
                        cl.update(k1 + 1, trkTraj.get(i), SVTGeom);
                    }
                    if (cl.get_Detector()==DetectorType.BMT) {
                        
                        if (cl.get_Type()==BMTType.Z) { //Z-detector measuring phi
                            Line3D cln = BMTGeom.getAxis(layer, sector);
                            double r = BMTGeom.getRadiusMidDrift(layer);
                            cl.set_CentroidResidual(resi*r);
                            cl.setN(cln.distance(ray.get_refPoint()).direction().asUnit());
                            cl.setS(cl.getL().cross(cl.getN()).asUnit());    

                        }
                    }
                }
                //refit adding missing clusters
                List<Cluster> clsOnTrack = recUtil.FindClustersOnTrack(SVTclusters, cosmics.get(k1), SVTGeom);
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
                    cosmics.get(k1).addAll(pseudoCrosses); //VZ check for additional clusters, and only then re-run KF adding new clusters                    
                    //refit
                    kf = new KFitter( the_ray.get_yxinterc(),the_ray.get_yzinterc(),
                                  the_ray.get_yxslope(), the_ray.get_yzslope(), 10.0, cov, kf,
                                  recUtil.setMeasVecs(cosmics.get(k1), SVTGeom, BMTGeom, swimmer )) ;
 //                kf.filterOn=false;
                    kf.runFitter(swimmer);
                    
                    Map<Integer, KFitter.HitOnTrack> traj2 = kf.TrjPoints; 
                    List<Integer> keys2 = new ArrayList<>();
                    traj2.forEach((key,value) -> keys2.add(key));
                    List<KFitter.HitOnTrack> trkTraj2 = new ArrayList<>();
                    traj2.forEach((key,value) -> trkTraj2.add(value));

                    the_ray = new Ray(kf.yx_slope, kf.yx_interc, kf.yz_slope, kf.yz_interc);                
                    cosmics.get(k1).set_ray(the_ray);
                    cosmics.get(k1).removeAll(pseudoCrosses);
                    cosmics.get(k1).update_Crosses(cosmics.get(k1).get_ray(), SVTGeom);
                    chi2 = cosmics.get(k1).calc_straightTrkChi2(); 
                    cosmics.get(k1).set_chi2(chi2);
                    
                    ntraj = trjFind.findTrajectory(k1+1, cosmics.get(k1).get_ray(), cosmics.get(k1), SVTGeom, BMTGeom);
                    cosmics.get(k1).set_Trajectory(ntraj.get_Trajectory());
                    trkcandFinder.upDateCrossesFromTraj(cosmics.get(k1), ntraj, SVTGeom);

                    for(int i = 0; i< keys2.size(); i++) {
                        double resi = trkTraj2.get(i).resi;
                        Point3D tj = new Point3D(trkTraj2.get(i).x,trkTraj2.get(i).y,trkTraj2.get(i).z);
                        Cluster cl = cosmics.get(k1).clsMap.get(keys2.get(i));
                        int layer = cl.get_Layer();
                        int sector = cl.get_Sector();

                        cosmics.get(k1).clsMap.get(keys2.get(i)).setTrakInters(tj);
                        cl.set_CentroidResidual(resi);
                        if (cl.get_Detector()==DetectorType.BST) {
                            cl.update(k1 + 1, trkTraj2.get(i), SVTGeom);
                        }
                        if (cl.get_Detector()==DetectorType.BMT) {

                            if (cl.get_Type()==BMTType.Z) { //Z-detector measuring phi
                                Line3D cln = BMTGeom.getAxis(layer, sector);
                                double r = BMTGeom.getRadiusMidDrift(layer);
                                cl.set_CentroidResidual(resi*r);
                                cl.setN(cln.distance(ray.get_refPoint()).direction().asUnit());
                                cl.setS(cl.getL().cross(cl.getN()).asUnit());    

                            }
                        }
                    }
                    
                }
                
            }
    
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, cosmics);
        }
        return true;
        }
    
}