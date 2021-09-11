/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.services;
import Jama.Matrix;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.fit.CosmicFitter;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.track.TrackListFinder;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;
/**
 *
 * @author ziegler
 */

public class CosmicTracksRec {
    
    private RecUtilities recUtil = new RecUtilities();
    
    public boolean processEvent(DataEvent event,  
            List<FittedHit> SVThits, List<FittedHit> BMThits, 
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses,
            org.jlab.rec.cvt.svt.Geometry SVTGeom, org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom,
            CTOFGeant4Factory CTOFGeom, Detector CNDGeom,
            RecoBankWriter rbc,
            double zShift, boolean exLayrs, Swim swimmer) {
        // make list of crosses consistent with a track candidate
        StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, SVTGeom,
                BMTGeom, 3);
        if (crosslist == null || crosslist.size() == 0) {
            // create the clusters and fitted hits banks
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, zShift);

            return true;
        } 
        TrackCandListFinder trkcandFinder = new TrackCandListFinder();
        List<StraightTrack> cosmics = trkcandFinder.getStraightTracks(crosslist, crosses.get(1), SVTGeom, BMTGeom);
        List<Track> trkcands = new ArrayList<Track>();
        //REMOVE THIS
        //crosses.get(0).addAll(crosses.get(1));
        //------------------------
        if (cosmics.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, zShift);

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
            List<Cross> bmtCrosses = new ArrayList<Cross>();
            List<Cross> bmtCrossesRm = new ArrayList<Cross>();
            
            
            
            for (int k1 = 0; k1 < cosmics.size(); k1++) { 
                cosmics.get(k1).set_Id(k1 + 1);
                for (int k2 = 0; k2 < cosmics.get(k1).size(); k2++) {
                    if(cosmics.get(k1).get(k2).get_Detector().equalsIgnoreCase("SVT")==true)
                        continue;
                    bmtCrossesRm.add(cosmics.get(k1).get(k2));
                    cosmics.get(k1).get(k2).set_AssociatedTrackID(k1+1);
                    bmtCrosses.add(cosmics.get(k1).get(k2));
                }    
            }
            crosses.get(1).removeAll(bmtCrossesRm);
            crosses.get(1).addAll(bmtCrosses);
            
            org.jlab.clas.tracking.trackrep.Helix hlx = null ;
            org.jlab.clas.tracking.kalmanfilter.straight.KFitter kf = null;
            
            for (int k1 = 0; k1 < cosmics.size(); k1++) {
                Ray ray = cosmics.get(k1).get_ray();
                
                Point3D xp = new Point3D(ray.get_yxinterc(),0,ray.get_yzinterc() );
                Vector3D u = new Vector3D(ray.get_yxslope(), 1, ray.get_yzslope()).asUnit();
                Line3D trL = new Line3D(xp, u);
                Point3D docVtx = trL.distance(new Point3D(0,0,0)).origin();
                double xr = cosmics.get(k1).get_ray().get_refPoint().x();
                double yr = cosmics.get(k1).get_ray().get_refPoint().y();
                double zr = cosmics.get(k1).get_ray().get_refPoint().z();
                
                double pt = 10000;
                double pz = pt*u.z();
                double px = pt*u.x();
                double py = pt*u.y();
                int charge = 1;
                
                Matrix cov = new Matrix(5, 5);
                cov.set(0, 0,ray.get_yxintercErr());
                cov.set(1, 1,ray.get_yzintercErr());
                cov.set(2, 2,ray.get_yxslopeErr());
                cov.set(3, 3,ray.get_yzslopeErr());
                cov.set(4, 4,1);
                kf = new org.jlab.clas.tracking.kalmanfilter.straight.KFitter( ray.get_yxinterc(),ray.get_yzinterc(),
                        ray.get_yxslope(),ray.get_yzslope(), 10.0, cov, kf,
                    recUtil.setMeasVecs(cosmics.get(k1), SVTGeom, BMTGeom, swimmer )) ;
                kf.filterOn=true;
                kf.runFitter(swimmer);
                Map<Integer, org.jlab.clas.tracking.kalmanfilter.straight.KFitter.HitOnTrack> traj 
                        = kf.TrjPoints;
                List<Integer> keys = new ArrayList<Integer>();
                traj.forEach((key,value) -> keys.add(key));
                List<org.jlab.clas.tracking.kalmanfilter.straight.KFitter.HitOnTrack> trkTraj = new ArrayList<org.jlab.clas.tracking.kalmanfilter.straight.KFitter.HitOnTrack>();
                traj.forEach((key,value) -> trkTraj.add(value));
                
                double y_ref = 0; // calc the ref point at the plane y =0
                double x_ref = kf.yx_interc;
                double z_ref = kf.yz_interc;
                Point3D refPoint = new Point3D(x_ref, y_ref, z_ref);
                Vector3D refDir = new Vector3D(kf.yx_slope, 1, kf.yz_slope).asUnit();
                
                Ray the_ray = new Ray(refPoint, refDir);
                the_ray.set_yxslope(kf.yx_slope);
                the_ray.set_yzslope(kf.yz_slope);
                the_ray.set_yxinterc(kf.yx_interc);
                the_ray.set_yzinterc(kf.yz_interc);
                
                cosmics.get(k1).set_ray(the_ray);
                
                cosmics.get(k1).update_Crosses(cosmics.get(k1).get_ray().get_yxslope(), cosmics.get(k1).get_ray().get_yxinterc(), SVTGeom);
                double chi2 = cosmics.get(k1).calc_straightTrkChi2(); 
                cosmics.get(k1).set_chi2(chi2);
                
                TrajectoryFinder trjFind = new TrajectoryFinder();
                
                ////Trajectory traj = trjFind.findTrajectory(passedcands.get(ic).get_Id(), trkRay, passedcands.get(ic), svt_geo, bmt_geo);
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
                    
                    if (cl.get_Detector()==1 ) {
                        
                        if (cl.get_DetectorType()==1) { //Z-detector measuring phi
                            Cylindrical3D cyl = BMTGeom.getCylinder(cl.get_Layer(), cl.get_Sector()); 
                            Line3D cln = cl.getCylAxis();
                            double r = BMTGeom.getRadius(layer)+org.jlab.rec.cvt.bmt.Constants.hStrip2Det;
                            cl.set_CentroidResidual(resi*r);
                            cl.setN(cl.getNFromTraj(tj.x(),tj.y(),tj.z(),cln));
                            cl.setS(cl.getL().cross(cl.getN()).asUnit());    

                        }
                    }
                }
                
            }
    
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, cosmics, 0);
        }
        return true;
        }
    
}