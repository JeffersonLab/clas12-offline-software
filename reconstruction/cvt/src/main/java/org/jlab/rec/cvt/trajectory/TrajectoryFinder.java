package org.jlab.rec.cvt.trajectory;


import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.hits.CTOFDetHit;
import org.jlab.detector.hits.DetHit;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.track.Track;

/**
 * A driver class to find the trajectory of a track candidate. NOTE THAT THE
 * PATH TO FIELD MAPS IS SET BY THE CLARA_SERVICES ENVIRONMENT VAR.
 *
 * @author ziegler
 *
 */
public class TrajectoryFinder {

    public TrajectoryFinder() {

    }
    
    /**
     *
     * @param isFinal
     * @return a trajectory object
     */
    public Trajectory findTrajectory(Track trk, 
                                    SVTGeometry svt_geo, BMTGeometry bmt_geo, 
                                    CTOFGeant4Factory ctof_geo, Detector cnd_geo,
                                    Swim swimmer, String isFinal) {
        Helix helix = trk.get_helix(); 
        ArrayList<Cross> candCrossList = trk;
        if(helix ==null)
            return null;
        Trajectory traj = new Trajectory(helix);
        if (isFinal == "final") {
            traj.isFinal = true;
        }

        traj.set_Id(trk.get_Id());

        if (candCrossList.size() == 0) {
            System.err.print("Trajectory Error:  cross list is empty");
            return traj;
        }

        ArrayList<Cross> SVTCrossList = new ArrayList<Cross>();
        ArrayList<Cross> BMTCrossList = new ArrayList<Cross>();
        Map<String, Double> ClsMap = new HashMap<String, Double>();
        for (Cross c : candCrossList) {
            if (c.get_Detector()==DetectorType.BST) {
                String svtSt1 = "1.";
                svtSt1+=c.get_Cluster1().get_Sector();
                svtSt1 += ".";
                svtSt1+=c.get_Cluster1().get_Layer();
                ClsMap.put(svtSt1, c.get_Cluster1().get_Centroid());
                String svtSt2 = "1.";
                svtSt2+=c.get_Cluster2().get_Sector();
                svtSt2 += ".";
                svtSt2+=c.get_Cluster2().get_Layer();
                ClsMap.put(svtSt2, c.get_Cluster2().get_Centroid());
                SVTCrossList.add(c);
            } else {
                String bmtSt1 = "2.";
                bmtSt1+=c.get_Cluster1().get_Sector();
                bmtSt1 += ".";
                bmtSt1+=c.get_Cluster1().get_Layer();
                ClsMap.put(bmtSt1, c.get_Cluster1().get_Centroid());
                BMTCrossList.add(c);
            }
        }

        traj.addAll(SVTCrossList);

        int[] Sectors = new int[SVTGeometry.NLAYERS];
        for (int k = 0; k < SVTCrossList.size(); k++) {
            int l = SVTCrossList.get(k).get_Region() * 2 - 1;
            Sectors[l - 1] = SVTCrossList.get(k).get_Sector();
            Sectors[l] = SVTCrossList.get(k).get_Sector();
        }

        for (int a = 0; a < Sectors.length; a++) {
            if (Sectors[a] == 0) {

                Point3D I = helix.getPointAtRadius(svt_geo.getLayerRadius(a+1));

                int sec = svt_geo.getSector(a+1, I);
                
                Sectors[a] = sec;
            }

        }
        traj.set_SVTSector(Sectors);

        ArrayList<StateVec> stateVecs = new ArrayList<StateVec>();
        
        // initialize swimmer starting from the track vertex
        double maxPathLength = 1;  
        //swimmer.SetSwimParameters((trk.get_helix().xdca()+Constants.getXb()) / 10, (trk.get_helix().xdca()+Constants.getXb()) / 10, trk.get_helix().get_Z0() / 10, 
        //             Math.toDegrees(trk.get_helix().get_phi_at_dca()), Math.toDegrees(Math.acos(trk.get_helix().costheta())),
        //             trk.get_P(), trk.get_Q(), maxPathLength) ;
        double pz = trk.get_Pt()*trk.get_helix().get_tandip();
        double px = trk.get_Pt()*Math.cos(trk.get_helix().get_phi_at_dca());
        double py = trk.get_Pt()*Math.sin(trk.get_helix().get_phi_at_dca());
        double x = (trk.get_helix().xdca()+Constants.getXb()) / 10;
        double y = (trk.get_helix().ydca()+Constants.getYb()) / 10;
        double z = trk.get_helix().get_Z0() / 10;
        swimmer.SetSwimParameters(x,y,z,px,py,pz, trk.get_Q()) ;
        
        double[] inters = null;
        double     path = 0;
        // SVT
        for (int l = 0; l < SVTGeometry.NLAYERS; l++) {
            // reinitilize swimmer from last surface
            if(inters!=null) {
                //double intersPhi   = Math.atan2(inters[4], inters[3]);
                //double intersTheta = Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]));
                swimmer.SetSwimParameters(inters[0], inters[1], inters[2], inters[3], inters[4], inters[5], trk.get_Q()) ;
            }
            int layer = l + 1;
            int sector = Sectors[l];
            if(sector == -1)
                continue;
            
            Vector3D n = svt_geo.getNormal(layer, sector);
            Point3D  p = svt_geo.getModule(layer, sector).origin();
            Point3D pm = new Point3D(p.x()/10, p.y()/10, p.z()/10);
            inters = swimmer.SwimPlane(n, pm, Constants.SWIMACCURACYSVT/10); 
            if(inters==null) break;
            path  = path + inters[6];
            StateVec stVec = new StateVec(inters[0]*10, inters[1]*10, inters[2]*10, inters[3], inters[4], inters[5]);
            stVec.set_planeIdx(l);
            stVec.set_SurfaceDetector(DetectorType.CVT.getDetectorId());
            stVec.set_SurfaceLayer(layer);
            stVec.set_SurfaceSector(sector);
            Vector3D dir = new Vector3D(inters[3], inters[4], inters[5]).asUnit();
            this.fill_HelicalTrkAngleWRTSVTPlane(sector, layer, dir, svt_geo, stVec);
            stVec.set_CalcCentroidStrip(svt_geo.calcNearestStrip(inters[0]*10, inters[1]*10, inters[2]*10, layer, sector));
            stVec.set_Path(path*10);
            stVec.set_ID(trk.get_Id());
            stateVecs.add(stVec);
            String svtSt1 = "1.";
            svtSt1+=sector;
            svtSt1 += ".";
            svtSt1+=layer;
            if(ClsMap.get(svtSt1)!=null) { 
                double cent = ClsMap.get(svtSt1);
                stVec.set_CalcCentroidStrip(cent); 
            }
            // loops over the crosses to get the strip resolutions
        //    for (Cross c : SVTCrossList) {
        //        if (matchCrossToStateVec(c, stVec, layer, sector) == false) {
        //            continue;
        //        }
        //        Vector3D dir = new Vector3D(inters[3], inters[4], inters[5]).asUnit();
        //        this.fill_HelicalTrkAngleWRTSVTPlane(sector, layer, dir, svt_geo, stVec);
        //        // set the cross dir
        //        c.set_Dir(dir);
        //        //set z
        //        c.set_Point(new Point3D(c.get_Point().x(), c.get_Point().y(), stVec.z()));
        //    }

        }
        // reinitialize from vertex
        maxPathLength = 1.5;  
        swimmer.SetSwimParameters((trk.get_helix().xdca()+Constants.getXb()) / 10, (trk.get_helix().ydca()+Constants.getYb()) / 10, trk.get_helix().get_Z0() / 10, 
                     Math.toDegrees(trk.get_helix().get_phi_at_dca()), Math.toDegrees(Math.acos(trk.get_helix().costheta())),
                     trk.get_P(), trk.get_Q(), maxPathLength) ;
        inters = null;
        path = 0;
        //BMT
        for (int l = SVTGeometry.NLAYERS; l < SVTGeometry.NLAYERS + bmt_geo.getNLayers(); l++) {
            // re-initilize swimmer from last surface
            if(inters!=null) {
                double intersPhi   = Math.atan2(inters[4], inters[3]);
                double intersTheta = Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]));
                swimmer.SetSwimParameters(inters[0], inters[1], inters[2], Math.toDegrees(intersPhi), Math.toDegrees(intersTheta), trk.get_P(), trk.get_Q(), maxPathLength) ;
            }
            if(inters!=null || l==SVTGeometry.NLAYERS) { // don't swim if previous layers was not reached
                int layer  = l - SVTGeometry.NLAYERS + 1;
                int region = bmt_geo.getRegion(layer);
                
                // RDV: tried using this to determine the sector but t is giving bogus number
                //Point3D helixTrj = trk.get_helix().getPointAtRadius(bmt_geo.getRadiusMidDrift(layer)); 
                double radius  = bmt_geo.getRadiusMidDrift(layer)/10;                
                inters = swimmer.SwimRho(radius, Constants.SWIMACCURACYBMT/10);
                if(inters==null) break;
                int sector = bmt_geo.getSector(0, Math.atan2(inters[1],inters[0]));
                
                Line3D axis    = bmt_geo.getAxis(layer, sector);
                Point3D axisP1 = new Point3D(axis.origin().x()/10,axis.origin().y()/10, axis.origin().z()/10);
                Point3D axisP2 = new Point3D(axis.end().x()/10,axis.end().y()/10, axis.end().z()/10);
                    //            swimmer.SetSwimParameters((trk.get_helix().xdca()+org.jlab.rec.cvt.BMTConstants.getXb()) / 10, (trk.get_helix().ydca()+org.jlab.rec.cvt.BMTConstants.getYb()) / 10, trk.get_helix().get_Z0() / 10, 
    //                    Math.toDegrees(trk.get_helix().get_phi_at_dca()), Math.toDegrees(Math.acos(trk.get_helix().costheta())),
    //                    trk.get_P(), trk.get_Q(), 
    //                    5.0) ;
                inters = swimmer.SwimGenCylinder(axisP1, axisP2, radius, Constants.SWIMACCURACYBMT/10);
                if(inters==null) break;
                double r = Math.sqrt(inters[0]*inters[0]+inters[1]*inters[1]);
                path  = path + inters[6];
                //if(r>(radius - BMTConstants.LYRTHICKN)/10) {
                StateVec stVec = new StateVec(inters[0]*10, inters[1]*10, inters[2]*10, inters[3], inters[4], inters[5]);
                stVec.set_planeIdx(l);  
//                    double phiPos = Math.atan2(stVec.y(),stVec.x());
//                    //int sector = bmt_geo.isInSector(BMTRegIdx+1,phiPos, 0);
//                    int sector = bmt_geo.getSector(layer,phiPos);
                stVec.set_SurfaceDetector(DetectorType.CVT.getDetectorId());
                stVec.set_SurfaceSector(sector);
                stVec.set_SurfaceLayer(l+1); 
                stVec.set_ID(trk.get_Id());
                stVec.set_Path(path*10);
                Vector3D dir = new Vector3D(inters[3], inters[4], inters[5]).asUnit();
                this.fill_HelicalTrkAngleWRTBMTTangentPlane(dir, stVec);
                //stVec.set_CalcCentroidStrip(bmt_geo.getCStrip(BMTRegIdx+1, stVec.z()));
                stVec.set_CalcCentroidStrip(bmt_geo.getCstrip(region, 
                       new Point3D(stVec.x(),stVec.y(),stVec.z())));
                stVec.set_Path(path*10);
                stVec.set_ID(trk.get_Id());

                String bmtSt1 = "2.";
                bmtSt1+=sector;
                bmtSt1 += ".";
                bmtSt1+=(l+1);
                if(ClsMap.get(bmtSt1)!=null) { 
                    double cent = ClsMap.get(bmtSt1);
                    stVec.set_CalcCentroidStrip(cent); 
                }
                stateVecs.add(stVec);
                //}
                //else {
                //    inters=null;
                //}
            }
        }
        // CTOF
        if(ctof_geo!=null && inters!=null) {    //  don't swim to CTOF if swimming to BMT failed
            // reinitialize swimmer based on last BMT layer
            double intersPhi   = Math.atan2(inters[4], inters[3]);
            double intersTheta = Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]));
            swimmer.SetSwimParameters(inters[0], inters[1], inters[2], Math.toDegrees(intersPhi), Math.toDegrees(intersTheta), trk.get_P(), trk.get_Q(), maxPathLength) ;
            // swim to CTOF
            double radius = ctof_geo.getRadius(1);
            inters = swimmer.SwimGenCylinder(new Point3D(0,0,0), new Point3D(0,0,1), radius, Constants.SWIMACCURACYCD/10);
            if(inters!=null) {
                // update parameters
                double r = Math.sqrt(inters[0]*inters[0]+inters[1]*inters[1]);
                    intersPhi   = Math.atan2(inters[4], inters[3]);
                intersTheta = Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]));
                path  = path + inters[6];
                StateVec stVec = new StateVec(inters[0]*10, inters[1]*10, inters[2]*10, inters[3], inters[4], inters[5]);
                stVec.set_SurfaceDetector(DetectorType.CTOF.getDetectorId());
                stVec.set_SurfaceSector(1);
                stVec.set_SurfaceLayer(1); 
                stVec.set_ID(trk.get_Id());
                stVec.set_TrkPhiAtSurface(intersPhi);
                stVec.set_TrkThetaAtSurface(intersTheta);
                stVec.set_TrkToModuleAngle(0);
                stVec.set_Path(path*10);
                stateVecs.add(stVec);
            }
        }
        // CND
        if(cnd_geo!=null && inters!=null) {     //  don't swim to CND if swimming to CTOF failed
            for(int ilayer=0; ilayer<cnd_geo.getSector(0).getSuperlayer(0).getNumLayers(); ilayer++) {
                // reinitialize swimmer based on last BMT layer
                double intersPhi   = Math.atan2(inters[4], inters[3]);
                double intersTheta = Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]));
                swimmer.SetSwimParameters(inters[0], inters[1], inters[2], 
                        Math.toDegrees(intersPhi), Math.toDegrees(intersTheta),
                        trk.get_P(), trk.get_Q(), 
                        maxPathLength) ;
                // swim to CTOF
                Point3D center = cnd_geo.getSector(0).getSuperlayer(0).getLayer(ilayer).getComponent(0).getMidpoint();
                double radius  = Math.sqrt(center.x()*center.x()+center.y()*center.y());
                inters = swimmer.SwimGenCylinder(new Point3D(0,0,0), new Point3D(0,0,1), radius, Constants.SWIMACCURACYCD/10);
                if(inters==null) break;
                // update parameters
                double r = Math.sqrt(inters[0]*inters[0]+inters[1]*inters[1]);
                intersPhi   = Math.atan2(inters[4], inters[3]);
                intersTheta = Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]));
                path  = path + inters[6];
                StateVec stVec = new StateVec(inters[0]*10, inters[1]*10, inters[2]*10, inters[3], inters[4], inters[5]);
                stVec.set_SurfaceDetector(DetectorType.CND.getDetectorId());
                stVec.set_SurfaceSector(1);
                stVec.set_SurfaceLayer(ilayer+1); 
                stVec.set_ID(trk.get_Id());
                stVec.set_TrkPhiAtSurface(intersPhi);
                stVec.set_TrkThetaAtSurface(intersTheta);
                stVec.set_TrkToModuleAngle(0);
                stVec.set_Path(path*10);
                stateVecs.add(stVec);
            }
        }
               
            
        traj.set_Trajectory(stateVecs);

        traj.addAll(BMTCrossList);

        return traj;
    }
    private void fill_HelicalTrkAngleWRTBMTTangentPlane(Vector3D trkDir, StateVec stVec){
        double phiPos = Math.atan2(stVec.y(),stVec.x());
        Vector3D trkDirRot = trkDir.clone();
        trkDirRot.rotateZ(Math.PI/2-phiPos); // Bring the track direction vector in phi=0
        double thetaC = Math.abs( Math.toRadians(90) - Math.abs(Math.atan(trkDirRot.y()/trkDirRot.z())) );
        double thetaZ = Math.abs( Math.toRadians(90) - Math.abs(Math.atan(trkDirRot.y()/trkDirRot.x())) );
        if ( (trkDirRot.y()>0 && trkDirRot.x()>0)||(trkDirRot.y()<0 && trkDirRot.x()<0) ){
            thetaZ=-thetaZ;  //Negative thetaZ if track is going to negative phi
        }
        if (stVec.z()<0){
            thetaC=-thetaC;  //Negative thetaC if track is going to negative z
        }
        
        //Fill State Vector
        stVec.set_TrkPhiAtSurface(thetaZ); //Call "phi" the normal angle for Z detectors
        stVec.set_TrkThetaAtSurface(thetaC); //Call "theta" the normal angle for C detectors
    }
    
    private void fill_HelicalTrkAngleWRTSVTPlane(int sector, int layer,
        Vector3D trkDir, SVTGeometry svt_geo, StateVec stVec) {
        Vector3D n  = svt_geo.getNormal(layer,sector);
        Vector3D ui = new Vector3D(n.y(), -n.x(), 0); //longitudinal vector along the local x direction of the module			
        Vector3D uj = ui.cross(n); //longitudinal vector along the local z direction of the module		    
        Vector3D u = new Vector3D(trkDir.x(), trkDir.y(), trkDir.z());

        double trkToMPlnAngl = Math.acos(u.dot(ui));

        double zl = u.dot(n);
        double xl = u.dot(ui);
        double yl = u.dot(uj);

        double PhiTrackIntersPlane = Math.atan2(yl, xl);
        double ThetaTrackIntersPlane = Math.acos(zl);

        stVec.set_TrkPhiAtSurface(PhiTrackIntersPlane);
        stVec.set_TrkThetaAtSurface(ThetaTrackIntersPlane);
        stVec.set_TrkToModuleAngle(trkToMPlnAngl);

    }

    public Trajectory findTrajectory(int id, Ray ray, ArrayList<Cross> candCrossList, 
            SVTGeometry svt_geo, BMTGeometry bmt_geo) {
        Trajectory traj = new Trajectory(ray);
        traj.set_Id(id);

        if (candCrossList.size() == 0) {
            System.err.print("Trajectory Error:  cross list is empty");
            return traj;
        }
        ArrayList<Cross> SVTCrossList = new ArrayList<Cross>();
        ArrayList<Cross> BMTCrossList = new ArrayList<Cross>();

        Map<String, Double> ClsMap = new HashMap<String, Double>();
        for (Cross c : candCrossList) {
            if (c.get_Detector()==DetectorType.BST) {
                String svtSt1 = "1.";
                svtSt1+=c.get_Cluster1().get_Sector();
                svtSt1 += ".";
                svtSt1+=c.get_Cluster1().get_Layer();
                ClsMap.put(svtSt1, c.get_Cluster1().get_Centroid());
                String svtSt2 = "1.";
                svtSt2+=c.get_Cluster2().get_Sector();
                svtSt2 += ".";
                svtSt2+=c.get_Cluster2().get_Layer();
                ClsMap.put(svtSt2, c.get_Cluster2().get_Centroid());
                SVTCrossList.add(c);
            } else {
                String bmtSt1 = "2.";
                bmtSt1+=c.get_Cluster1().get_Sector();
                bmtSt1 += ".";
                bmtSt1+=c.get_Cluster1().get_Layer();
                ClsMap.put(bmtSt1, c.get_Cluster1().get_Centroid());
                BMTCrossList.add(c);
            }
        }

        traj.addAll(candCrossList);

        ArrayList<StateVec> stateVecs = new ArrayList<StateVec>();

        double[][][] SVTIntersections = calc_trackIntersSVT(ray, svt_geo);

        for (int l = 0; l < SVTGeometry.NLAYERS; l++) {
            for (int s = 0; s < SVTGeometry.NSECTORS[l]; s++) {

                if (SVTIntersections[l][s][0] != -999) {

                    int LayerTrackIntersPlane = (l + 1);
                    int SectorTrackIntersPlane = (s + 1);
                    double XtrackIntersPlane = SVTIntersections[l][s][0];
                    double YtrackIntersPlane = SVTIntersections[l][s][1];
                    double ZtrackIntersPlane = SVTIntersections[l][s][2];
                    double PhiTrackIntersPlane = SVTIntersections[l][s][3];
                    double ThetaTrackIntersPlane = SVTIntersections[l][s][4];
                    double trkToMPlnAngl = SVTIntersections[l][s][5];
                    double CalcCentroidStrip = SVTIntersections[l][s][6];

                    StateVec stVec = new StateVec(XtrackIntersPlane, YtrackIntersPlane, ZtrackIntersPlane, ray.get_dirVec().x(), ray.get_dirVec().y(), ray.get_dirVec().z());
                    stVec.set_ID(id);
                    stVec.set_SurfaceLayer(LayerTrackIntersPlane);
                    stVec.set_SurfaceSector(SectorTrackIntersPlane);
                    stVec.set_TrkPhiAtSurface(PhiTrackIntersPlane);
                    stVec.set_TrkThetaAtSurface(ThetaTrackIntersPlane);
                    stVec.set_TrkToModuleAngle(trkToMPlnAngl);
                    stVec.set_CalcCentroidStrip(CalcCentroidStrip);
                    String svtSt1 = "1.";
                    svtSt1+=SectorTrackIntersPlane;
                    svtSt1 += ".";
                    svtSt1+=LayerTrackIntersPlane;
                    if(ClsMap.get(svtSt1)!=null) { 
                        double cent = ClsMap.get(svtSt1);
                        stVec.set_CalcCentroidStrip(cent); 
                    }
                    if(stateVecs.size()>0 
                            && stateVecs.get(stateVecs.size()-1).x()==stVec.x()
                            && stateVecs.get(stateVecs.size()-1).y()==stVec.y()
                            && stateVecs.get(stateVecs.size()-1).z()==stVec.z()) {
                    } else {
                        stateVecs.add(stVec);
                    }
                    
                    /// Get the strip resolutions
                    // for the SVT
                    // loops over the crosses to get the strip resolutions
                    for (Cross c : SVTCrossList) {
                        if (matchCrossToStateVec(c, stVec, l + 1, s + 1) == false) {
                            continue;
                        }
                        
                        Cluster clsOnTrk = null;
                        if (l % 2 == 0) {
                            clsOnTrk = c.get_Cluster1(); 
                        }
                        if (l % 2 == 1) {
                            clsOnTrk = c.get_Cluster2(); 
                        }

                        if (clsOnTrk != null && clsOnTrk.get_Layer() == l + 1) {

                            setHitResolParams("SVT", clsOnTrk.get_Sector(), clsOnTrk.get_Layer(), clsOnTrk,
                                    stVec, svt_geo, bmt_geo, traj.isFinal);

                        }
                    }
                }
            }
        }
        
        double[][][] BMTIntersections = calc_trackIntersBMT(ray, bmt_geo, BMTConstants.STARTINGLAYR);

        for (int l = BMTConstants.STARTINGLAYR - 1; l < 6; l++) {
            //hemisphere 1-2
            for (int h = 0; h < 2; h++) {

                if (BMTIntersections[l][h][0] != -999) {

                    int LayerTrackIntersSurf = (l + 1);
                    double XtrackIntersSurf = BMTIntersections[l][h][0];
                    double YtrackIntersSurf = BMTIntersections[l][h][1];
                    double ZtrackIntersSurf = BMTIntersections[l][h][2];
                    //int SectorTrackIntersSurf = bmt_geo.isInSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf), Math.toRadians(BMTConstants.isInSectorJitter));
                    int SectorTrackIntersSurf = bmt_geo.getSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf));
                    double PhiTrackIntersSurf = BMTIntersections[l][h][3];
                    double ThetaTrackIntersSurf = BMTIntersections[l][h][4];
                    double trkToMPlnAngl = BMTIntersections[l][h][5];
                    double CalcCentroidStrip = BMTIntersections[l][h][6];

                    StateVec stVec = new StateVec(XtrackIntersSurf, YtrackIntersSurf, ZtrackIntersSurf, ray.get_dirVec().x(), ray.get_dirVec().y(), ray.get_dirVec().z());

                    stVec.set_ID(id);
                    stVec.set_SurfaceLayer(LayerTrackIntersSurf);
                    stVec.set_SurfaceSector(SectorTrackIntersSurf);
                    stVec.set_TrkPhiAtSurface(PhiTrackIntersSurf);
                    stVec.set_TrkThetaAtSurface(ThetaTrackIntersSurf);
                    stVec.set_TrkToModuleAngle(trkToMPlnAngl);
                    stVec.set_CalcCentroidStrip(CalcCentroidStrip); 
                    String bmtSt1 = "2.";
                    bmtSt1+=SectorTrackIntersSurf;
                    bmtSt1 += ".";
                    bmtSt1+=LayerTrackIntersSurf;
                    if(ClsMap.get(bmtSt1)!=null) { 
                        double cent = ClsMap.get(bmtSt1);
                        stVec.set_CalcCentroidStrip(cent); 
                    }
                     if(stateVecs.size()>0 
                            && stateVecs.get(stateVecs.size()-1).x()==stVec.x()
                            && stateVecs.get(stateVecs.size()-1).y()==stVec.y()
                            && stateVecs.get(stateVecs.size()-1).z()==stVec.z()) {
                    } else {
                        stateVecs.add(stVec);
                    }

                    /// Get the strip resolutions
                    // for the BMT
                    // loops over the crosses to get the strip resolutions
                    for (Cross c : BMTCrossList) {
                        if (c.get_Region() != (int) (l / 2) + 1) {
                            continue;
                        }
                        
                        if (this.matchCrossToStateVec(c, stVec, l + 1, c.get_Sector()) == false) {
                            continue;
                        } 
                        
                        if (c.get_Type()==BMTType.C) { //C-detector measuring Z
                            //if(traj.isFinal) { // reset the cross only for final trajectory

//                            c.set_Point(new Point3D(XtrackIntersSurf, YtrackIntersSurf, c.get_Point().z()));
//                            c.set_Dir(ray.get_dirVec());
                            //}

                            // calculate the hit residuals // CHECK THIS ........
                            this.setHitResolParams("BMT", c.get_Sector(), c.get_Cluster1().get_Layer(), c.get_Cluster1(),
                                    stVec, svt_geo, bmt_geo, traj.isFinal);

                        }
                        if (c.get_Type()==BMTType.Z) { //Z-detector measuring phi
                            //if(traj.isFinal) {

//                            c.set_Point(new Point3D(c.get_Point().x(), c.get_Point().y(), ZtrackIntersSurf));
//                            c.set_Dir(ray.get_dirVec());
//                           //}

                            // calculate the hit residuals
                            this.setHitResolParams("BMT", c.get_Cluster1().get_Sector(), c.get_Cluster1().get_Layer(), c.get_Cluster1(),
                                    stVec, svt_geo, bmt_geo, traj.isFinal);

                        }
                    }

                }
            }
        }

        //Collections.sort(stateVecs);
        
        stateVecs.sort(Comparator.comparing(StateVec::y));
        for (int l = 0; l < stateVecs.size(); l++) {
            stateVecs.get(l).set_planeIdx(l);
        }
        traj.set_Trajectory(stateVecs);
        traj.set_SVTIntersections(SVTIntersections);
        traj.set_BMTIntersections(BMTIntersections);
        return traj;
    }

    private boolean matchCrossToStateVec(Cross c, StateVec stVec, int layer, int sector) {
        
        if (c.get_Detector()==DetectorType.BST) {
            int l = layer - 1;
            
            if (c.get_Region() != (int) (l / 2) + 1) {
                return false;	// require same region
            }
            if (c.get_Sector() != sector) {
                return false;		// same sector 
            } 
            double deltaXt = Math.sqrt((stVec.x() - c.get_Point().x()) * (stVec.x() - c.get_Point().x()) + (stVec.y() - c.get_Point().y()) * (stVec.y() - c.get_Point().y()));
            if (deltaXt > SVTGeometry.getActiveSensorWidth() / 2) {
                return false; // within 1/2 module width
            }
        }

        if (c.get_Detector()==DetectorType.BMT) { // BMT
            
            double Rsv = Math.sqrt(stVec.x()*stVec.x()+stVec.y()*stVec.y());
            double Rcs = Math.sqrt(c.get_Point().x()*c.get_Point().x()+c.get_Point().y()*c.get_Point().y());

            if(Math.abs(Rsv-Rcs)>1.e-01) {
                return false;
            } 
            
            if(new Vector3D(stVec.x(), stVec.y(), stVec.z()).asUnit().dot(c.get_Point().toVector3D().asUnit())<0.8){
                return false;
            }
            
        }   
        return true;
    }

    /**
     *
     * @param detector
     * @param sector
     * @param layer
     * @param cluster
     * @param stVec stateVec
     * @param svt_geo
     * @param bmt_geo
     * @param trajFinal
     */
    public void setHitResolParams(String detector, int sector, int layer, Cluster cluster,
            StateVec stVec, SVTGeometry svt_geo, BMTGeometry bmt_geo, boolean trajFinal) {

        if (detector.equalsIgnoreCase("SVT") ) {
            double doca2Cls = cluster.residual(new Point3D(stVec.x(), stVec.y(), stVec.z()));
            double doca2Seed = cluster.get(0).residual(new Point3D(stVec.x(), stVec.y(), stVec.z()));
            cluster.set_SeedResidual(doca2Seed); 
            cluster.set_CentroidResidual(doca2Cls);
            cluster.setTrakInters(new Point3D(stVec.x(), stVec.y(), stVec.z()));
//            Point3D endPt1 = cluster.getEndPoint1();
//            Point3D endPt2 = cluster.getEndPoint2();
//            Line3D l = new Line3D(endPt1,endPt2);
//            Plane3D pl = new Plane3D(endPt1, svt_geo.findBSTPlaneNormal(sector, layer));
//            double d = new Vector3D(stVec.x(), stVec.y(), stVec.z()).dot(pl.normal())-pl.point().toVector3D().dot(pl.normal());
//            System.out.println(d+" calc "+l.distance(new Point3D(stVec.x(), stVec.y(), stVec.z())).length()+" d "+doca2Cls);
            for (FittedHit hit : cluster) {
                double doca1 = hit.residual(new Point3D(stVec.x(), stVec.y(), stVec.z()));
                double sigma1 = svt_geo.getSingleStripResolution(layer, hit.get_Strip().get_Strip(), stVec.z());
                hit.set_stripResolutionAtDoca(sigma1);
                hit.set_docaToTrk(doca1);  
                hit.set_TrkgStatus(1);
                if (trajFinal) {
                    hit.set_TrkgStatus(2);
                }
            }
        }
        if (detector.equalsIgnoreCase("BMT")) { 
            cluster.setTrakInters(new Point3D(stVec.x(), stVec.y(), stVec.z()));
            Point3D    offset = bmt_geo.getOffset(cluster.get_Layer(), cluster.get_Sector()); 
            Vector3D rotation = bmt_geo.getRotation(cluster.get_Layer(), cluster.get_Sector());
            double ce = cluster.get_Centroid();    
            Point3D p = new Point3D(stVec.x(), stVec.y(), stVec.z());
            if (BMTGeometry.getDetectorType(layer) == BMTType.C) { //C-detector measuring z
                cluster.set_CentroidResidual(p);
                cluster.set_SeedResidual(p);
                for (FittedHit h1 : cluster) {
                    // calculate the hit residuals
                    h1.set_TrkgStatus(1);
                    h1.set_docaToTrk(p);
                    h1.set_TrkgStatus(1);
                    if (trajFinal) {
                        h1.set_TrkgStatus(2);
                    }
                }
                
            }
            if (BMTGeometry.getDetectorType(layer) == BMTType.Z) { //Z-detector measuring phi
                int bsector = cluster.get_Sector();
                int blayer = cluster.get_Layer();
                double cxh = Math.cos(cluster.get_Phi())*bmt_geo.getRadiusMidDrift(blayer);
                double cyh = Math.sin(cluster.get_Phi())*bmt_geo.getRadiusMidDrift(blayer);
                double phic = bmt_geo.getPhi(blayer, bsector, new Point3D(cxh,cyh,0));
                double phit = bmt_geo.getPhi(blayer, bsector, p);
                double doca2Cls = (phic-phit)*bmt_geo.getRadiusMidDrift(blayer);
                cluster.set_CentroidResidual(doca2Cls);

                for (FittedHit h1 : cluster) {
                    double xh = Math.cos(h1.get_Strip().get_Phi())*bmt_geo.getRadiusMidDrift(blayer);
                    double yh = Math.sin(h1.get_Strip().get_Phi())*bmt_geo.getRadiusMidDrift(blayer);
                    double hphic = bmt_geo.getPhi(blayer, bsector, new Point3D(xh,yh,0));
                    double hphit = bmt_geo.getPhi(blayer, bsector, p);
                    double doca1 = (hphic-hphit)*bmt_geo.getRadiusMidDrift(blayer);

                    if(h1.get_Strip().get_Strip()==cluster.get_SeedStrip().get_Strip())
                        cluster.set_SeedResidual(doca1); 
                    
                    h1.set_TrkgStatus(1);
                    if (trajFinal) {
                        h1.set_TrkgStatus(2);
                    }
                }
            }
        }
    }

    private double[][][] calc_trackIntersBMT(Ray ray, BMTGeometry bmt_geo, int start_layer) {

        //[l][hemisphere], [0,1,2,3,4]=x,y,z,phi,theta,estimated centroid strip; hemisphere = [1]top or [0]bottom
        double[][][] result = new double[6][2][7];
        for (int l = start_layer - 1; l < 6; l++) {
            for (int h = 0; h < 2; h++) {
                result[l][h][0] = -999;
                result[l][h][1] = -999;
                result[l][h][2] = -999;
                result[l][h][3] = -999;
                result[l][h][4] = -999;
            }
        } 
        //Layer 1-6:
        Line3D line = ray.toLine();
        for (int l = start_layer - 1; l < 6; l++) {
            for(int s=0; s<bmt_geo.getNSectors(); s++) {
              int layer = l+1;
                int sector = s+1;
                
                List<Point3D> trajs = new ArrayList<>();
                int ntraj = bmt_geo.getTileSurface(layer, sector).intersection(line, trajs);
//                System.out.println(layer + " " + sector);
                if(ntraj>0) {
                    for(int i=0; i<trajs.size(); i++) {    
                        Point3D traj = trajs.get(i);
                        // define hemisphere
                        int h = (int) Math.signum(traj.y());
                        if(h<0) h =0;                             
                        // get track direction in local frame
                        Vector3D local = new Vector3D(line.direction()).asUnit();
                        bmt_geo.toLocal(layer, sector).apply(local);
                        // rotate to have y in the direction of the trajectory point
                        local.rotateZ(-traj.toVector3D().phi());
                        // y is along the cylinder axis
                        local.rotateX(Math.toRadians(90));
                        // RDV to be checked
                        local.rotateY(Math.toRadians(90));
                        if(traj.y()>0) local.negative();
                        result[l][h][0] = traj.x();
                        result[l][h][1] = traj.y();
                        result[l][h][2] = traj.z();
                        result[l][h][3] = Math.toDegrees(local.phi());
                        result[l][h][4] = Math.toDegrees(local.theta());
                        result[l][h][5] = Math.toDegrees(Math.acos(local.x()));
                        result[l][h][6] = bmt_geo.getStrip(layer, sector, traj);
//                        System.out.println("New " + local.x() + " " + local.y() + " " + local.z() + " " 
//                                                  + result[l][h][0] + " " + result[l][h][1] + " " + result[l][h][2] + " "
//                                                  + result[l][h][3] + " " + result[l][h][4] + " " + result[l][h][5] + " " + result[l][h][6]);
                    }
                } 
            }
//            double[][] trkIntersCombinedInf = this.getIntersectionTrackWithBMTModules(l, 
//                    ray.get_yxinterc(), ray.get_yxslope(), ray.get_yzinterc(), ray.get_yzslope(), bmt_geo);
//
//            //hemisphere 1-2
//            for (int h = 0; h < 2; h++) {
//                double[] trkIntersInf = trkIntersCombinedInf[h];
//                
//                if(Double.isNaN(trkIntersInf[0]) || Double.isNaN(trkIntersInf[1]) || Double.isNaN(trkIntersInf[2]) )
//                    continue;
//                
//                Point3D p = new Point3D(trkIntersInf[0], trkIntersInf[1], trkIntersInf[2]);
//                
//                if (p.toVector3D().mag() == 0 ) {
//                    continue;
//                }
//
//                Vector3D n = new Vector3D(Math.cos(Math.atan2(p.y(), p.x())), Math.sin(Math.atan2(p.y(), p.x())), 0);
//                Vector3D ui = new Vector3D(n.y(), -n.x(), 0); //longitudinal vector along the local x direction of the module
//
//                Vector3D uj = ui.cross(n); //longitudinal vector along the local z direction of the module
//
//                double norm = Math.sqrt(ray.get_yxslope() * ray.get_yxslope() + ray.get_yzslope() * ray.get_yzslope() + 1);
//
//                Vector3D u = new Vector3D(ray.get_yxslope() / norm, 1 / norm, ray.get_yzslope() / norm);
//
//                if (p.y() < 0) {
//                    u = new Vector3D(-ray.get_yxslope() / norm, -1 / norm, -ray.get_yzslope() / norm);
//                }
//
//                double trkToMPlnAngl = Math.acos(u.dot(ui));
//
//                double zl = u.dot(n);
//                double xl = u.dot(ui);
//                double yl = u.dot(uj);
//
//                double phi = Math.atan2(yl, xl);
//                double theta = Math.acos(zl);
//
//                result[l][h][0] = p.x();
//                result[l][h][1] = p.y();
//                result[l][h][2] = p.z();
//                result[l][h][3] = Math.toDegrees(phi);
//                result[l][h][4] = Math.toDegrees(theta);
//                result[l][h][5] = Math.toDegrees(trkToMPlnAngl);
//                result[l][h][6] = trkIntersInf[3]; 
//                        System.out.println("Old " + xl + " " + yl + " " + zl + " " 
//                                                  + result[l][h][0] + " " + result[l][h][1] + " " + result[l][h][2] + " "
//                                                  + result[l][h][3] + " " + result[l][h][4] + " " + result[l][h][5] + " " + result[l][h][6]);
//             }
        }
        return result;
    }

    private double[][][] calc_trackIntersSVT(Ray ray, SVTGeometry svt_geo) {
        //[l][s], [0,1,2,3,4]=x,y,z,phi,theta,estimated centroid strip
        double[][][] result = new double[SVTGeometry.NLAYERS][SVTGeometry.NSECTORS[SVTGeometry.NLAYERS-1]][7];
        for (int l = 0; l < SVTGeometry.NLAYERS; l++) {
            for (int s = 0; s < SVTGeometry.NSECTORS[l]; s++) {
                result[l][s][0] = -999;
                result[l][s][1] = -999;
                result[l][s][2] = -999;
                result[l][s][3] = -999;
                result[l][s][4] = -999;
            }
        }
        //Layer 1-8:
        Line3D line = ray.toLine();
        for (int l = 0; l < SVTGeometry.NLAYERS; l++) {
            for (int s = 0; s < SVTGeometry.NSECTORS[l]; s++) {
                int layer = l+1;
                int sector = s+1;
                
                Point3D traj = new Point3D();
                int ntraj = svt_geo.getPlane(layer, sector).intersection(line, traj);
                if(ntraj==1 && svt_geo.isInFiducial(layer, sector, traj)) {
                    Vector3D local = new Vector3D(line.direction()).asUnit();
                    local = svt_geo.toLocal(layer, sector, local);
                    if(traj.y()>0) local.negative();
                    local.rotateX(Math.toRadians(90));
                    result[l][s][0] = traj.x();
                    result[l][s][1] = traj.y();
                    result[l][s][2] = traj.z();
                    result[l][s][3] = Math.toDegrees(local.phi());
                    result[l][s][4] = Math.toDegrees(local.theta());
                    result[l][s][5] = Math.toDegrees(Math.acos(local.x()));
                    result[l][s][6] = svt_geo.calcNearestStrip(traj.x(), traj.y(), traj.z(), layer, sector);
                }
            }
        }
        return result;
    }

    
    @Deprecated
    public double[] getIntersectionTrackWithSVTModule(int s, int l,
            double _yxinterc2, double _yxslope2, double _yzinterc2,
            double _yzslope2, SVTGeometry geo) {
        // array [][][][] =[x][y][z][stripCentroid]
        double[] inters = new double[4];
        inters[0] = Double.NaN;
        inters[1] = Double.NaN;
        inters[2] = Double.NaN;
        inters[3] = Double.NaN;

        double y_minus = -300;
        double y_plus = 350;
        double x_plus = _yxslope2 * y_plus + _yxinterc2;
        double x_minus = _yxslope2 * y_minus + _yxinterc2;

        double z_plus = _yzslope2 * y_plus + _yzinterc2;
        double z_minus = _yzslope2 * y_minus + _yzinterc2; 

        Vector3D n = geo.getNormal(l+1, s+1);
        Line3D mod = geo.getModule(l+1, s+1);
        Point3D Or = mod.origin();
        Point3D En = mod.end();
        
        
        Vector3D u = new Vector3D(x_plus-x_minus,y_plus-y_minus,z_plus-z_minus).asUnit();
         
        double alpha = (En.y() - Or.y()) /
                (En.x() - Or.x());
        double t = (alpha*(x_minus-Or.x()) -(y_minus-Or.y()))/(u.y() - alpha*u.x());

        double x = x_minus+t*u.x();
        double y = y_minus+t*u.y();
        double z = z_minus+t*u.z();
        
        double strp = geo.calcNearestStrip(inters[0], inters[1], inters[2], l + 1, s + 1);

        if (strp>0 && strp<257) {
            inters[0] = y * _yxslope2 + _yxinterc2;
            inters[1] = y;
            inters[2] = y * _yzslope2 + _yzinterc2;
            inters[3] = strp;
        } 
            
        return inters;
        
    }

    @Deprecated
    private double[][] getIntersectionTrackWithBMTModules(int l,
            double _yxinterc2, double _yxslope2, double _yzinterc2,
            double _yzslope2, BMTGeometry geo) {
        
        // array [][][][] =[x][y][z][stripCentroid]
        double[][] inters = new double[2][4];
        double[] inters_top = new double[4];
        inters_top[0] = Double.NaN;
        inters_top[1] = Double.NaN;
        inters_top[2] = Double.NaN;
        inters_top[3] = 0;

        double[] inters_bottom = new double[4];
        inters_bottom[0] = Double.NaN;
        inters_bottom[1] = Double.NaN;
        inters_bottom[2] = Double.NaN;
        inters_bottom[3] = Double.NaN;
        inters_bottom[3]= 0;
        
        int lyer = l+1;
        
        double radius = geo.getRadiusMidDrift(lyer);
        
        double y_minus = -300;
        double y_plus = 350;
        double x_plus = _yxslope2 * y_plus + _yxinterc2;
        double x_minus = _yxslope2 * y_minus + _yxinterc2;

        double z_plus = _yzslope2 * y_plus + _yzinterc2;
        double z_minus = _yzslope2 * y_minus + _yzinterc2; 
        
        Point3D trkOr = new Point3D(x_plus,y_plus,z_plus);
        Point3D trkEn = new Point3D(x_minus,y_minus,z_minus);
        
        List<Point3D> intersNominal = this.getIntersBMT(lyer, radius, x_minus,y_minus, z_minus, trkOr,trkEn, null, null,geo);
        Point3D top = intersNominal.get(0);
        Point3D bottom = intersNominal.get(1);
        
        Point3D    offset = null;
        Vector3D rotation = null;
        Point3D    ioffset = null;
        Vector3D irotation = null;
        int topsec = 0;
        int bottomsec = 0;
        int topstp = 0;
        int bottomstp = 0;
        if(top.toVector3D().mag()>0 && geo.getSector(lyer, top.toVector3D().phi())>0) {
            topsec = geo.getSector(lyer, top.toVector3D().phi());
            trkOr = geo.toLocal(trkOr, lyer, topsec);
            trkEn = geo.toLocal(trkEn, lyer, topsec);
            intersNominal = this.getIntersBMT(lyer, radius, x_minus,y_minus, z_minus, trkOr,trkEn, offset, rotation, geo);
            top = intersNominal.get(0); 
            topstp = geo.getStrip( l + 1,  topsec,  top);
        }
        if(bottom.toVector3D().mag()>0 && geo.getSector(lyer, bottom.toVector3D().phi())>0) {
            bottomsec = geo.getSector(lyer, bottom.toVector3D().phi());
            trkOr = geo.toLocal(trkOr, lyer, bottomsec);
            trkEn = geo.toLocal(trkEn, lyer, bottomsec);
            intersNominal = this.getIntersBMT(lyer, radius, x_minus,y_minus, z_minus, trkOr,trkEn, offset, rotation, geo);
            bottom = intersNominal.get(1); 
            bottomstp = geo.getStrip( l + 1,  bottomsec,  bottom);
        }
        
        if(top.toVector3D().mag()>0) {
            inters_top[0] = top.x();
            inters_top[1] = top.y();
            inters_top[2] = top.z();
            inters_top[3] = geo.getStrip( l + 1,  bottomsec,  top);
        }
        if(bottom.toVector3D().mag()>0) {
            inters_bottom[0] = bottom.x();
            inters_bottom[1] = bottom.y();
            inters_bottom[2] = bottom.z();
            inters_bottom[3]= geo.getStrip( l + 1,  bottomsec,  bottom);
        }

        inters[0] = inters_top;
        inters[1] = inters_bottom;
        return inters;
    }

    private Hit[][][] HitArray;

    public void hitsToArray(List<Hit> hits2) {
        if (hits2 == null) {
            return;
        }
        HitArray = new Hit[SVTGeometry.NLAYERS][SVTGeometry.NSECTORS[SVTGeometry.NLAYERS - 1]][SVTGeometry.NSTRIPS];

        // initializing non-zero Hit Array entries
        // with valid hits
        for (Hit hit : hits2) {

            int w = hit.get_Strip().get_Strip();
            int l = hit.get_Layer();
            int s = hit.get_Sector();

            HitArray[l - 1][s - 1][w - 1] = hit;

        }
    }

    
    private List<StateVec> getTrkInMiddleOfBar(int id, double[] inters, CTOFGeant4Factory ctof_geo) {
        List<StateVec> stateVecs = new ArrayList<StateVec>();

        double p = Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]);
        double x    = inters[0];
        double y    = inters[1];
        double z    = inters[2];
        double ux   = inters[3]/p;
        double uy   = inters[4]/p;
        double uz   = inters[5]/p;
        double path = inters[6];
        
        Line3d line = new Line3d(new Vector3d(x,y,z), new Vector3d(x+5*ux,y+5*uy, z+5*uz));
        
        List<DetHit> trkHits = ctof_geo.getIntersections(line);
        if (trkHits != null && trkHits.size() > 0) {
            for (DetHit hit : trkHits) {
                CTOFDetHit trkHit = new CTOFDetHit(hit);
                //track extrp to a line, make sure you pick up the right hit
                if(Math.sqrt((trkHit.origin().x-x)*(trkHit.origin().x-x)+
                        (trkHit.origin().y-y)*(trkHit.origin().y-y))>3.5*2) //thickness*2
                    continue;
                int component = trkHit.getPaddle();
                // get the coordinates for the track hit, which is defined
                // as the mid-point between its entrance and its exit from
                // the bar
                path+=trkHit.origin().distance(trkHit.end());
                new Point3D(trkHit.mid().x,trkHit.mid().y, trkHit.mid().z);  
                Vector3d dir = trkHit.end().minus(trkHit.origin()).normalized();
                
                StateVec stVec = new StateVec(trkHit.mid().x*10, trkHit.mid().y*10, trkHit.mid().z*10, 
                        dir.x*p, dir.y*p, dir.z*p);
                stVec.set_SurfaceDetector(DetectorType.CTOF.getDetectorId());
                stVec.set_SurfaceSector(1);
                stVec.set_SurfaceLayer(1); 
                stVec.set_ID(id);
                stVec.set_SurfaceComponent(component);
                stVec.set_CalcCentroidStrip(component);
                stVec.set_TrkPhiAtSurface(Math.atan2(dir.y, dir.x));
                stVec.set_TrkThetaAtSurface(Math.acos(dir.z));
                stVec.set_TrkToModuleAngle(0);
                stVec.set_Path(path*10);
                stateVecs.add(stVec);
                
            }
        }
        return stateVecs;
    }

    private boolean checkBMTAcceptance(Point3D ipos, int lyer, BMTGeometry geo) {
        //1 mm tolerance
        if(ipos.z()<geo.getZmax(lyer)+1.0 && ipos.z()>geo.getZmin(lyer)+1.0 ) {
            return true;
        } else {
            return false;
        } 
    }

    @Deprecated
    private List<Point3D> getIntersBMT(int lyer, double radius, double x_minus, double y_minus, double z_minus, 
            Point3D trkOr, Point3D trkEn, Point3D offset, Vector3D rotation, BMTGeometry geo) {
        
        List<Point3D> result = new ArrayList<Point3D>();
        Line3D trk = new Line3D(trkOr, trkEn);
        Point3D top = new Point3D(0,0,0);
        Point3D bottom = new Point3D(0,0,0);
        
        double X = x_minus;
        double Y = y_minus;
        double Vx = trk.direction().asUnit().x();
        double Vy = trk.direction().asUnit().y();
        double Vz = trk.direction().asUnit().z();
        
        double Delta = (Vx*X+Vy*Y)*(Vx*X+Vy*Y) - (X*X+Y*Y-radius*radius)*(Vx*Vx+Vy*Vy);
        if(Delta<0) {
            result.add(top);
            result.add(bottom);
            return result;
        }
        double tpos = (-(Vx*X+Vy*Y) + Math.sqrt(Delta))/(Vx*Vx+Vy*Vy);
        double tneg = (-(Vx*X+Vy*Y) - Math.sqrt(Delta))/(Vx*Vx+Vy*Vy);
        
        Point3D ipos = new Point3D(x_minus+tneg*Vx,y_minus+tneg*Vy,z_minus+tneg*Vz);
        Point3D ineg = new Point3D(x_minus+tpos*Vx,y_minus+tpos*Vy,z_minus+tpos*Vz);
        
        if(this.checkBMTAcceptance(ipos, lyer,geo)) {
            if(offset!=null && rotation!=null)
                geo.putInFrame(ipos, offset, rotation, false);
        } else {
            ipos.set(0,0,0);
        }
        if(this.checkBMTAcceptance(ineg, lyer,geo)) {
            
            if(offset!=null && rotation!=null)
                geo.putInFrame(ineg, offset, rotation, false);
        } else {
            ineg.set(0,0,0);
        }
        
        if(ipos.toVector3D().mag()>0 && ineg.toVector3D().mag()>0) {
            if(ipos.y()<ineg.y()) {
                top.set(ineg.x(), ineg.y(), ineg.z());
                bottom.set(ipos.x(), ipos.y(), ipos.z());
            } else {
                bottom.set(ineg.x(), ineg.y(), ineg.z());
                top.set(ipos.x(), ipos.y(), ipos.z());
            }
        } else {
            if(ipos.toVector3D().mag()==0 && ineg.toVector3D().mag()==0) {
                result.add(top);
                result.add(bottom);
                return result;
            }
            if(ipos.toVector3D().mag()>0 && ineg.toVector3D().mag()==0) {
                if(ipos.y()<0) {
                bottom.set(ipos.x(), ipos.y(), ipos.z());
                } else {
                    top.set(ipos.x(), ipos.y(), ipos.z());
                }
            }
            if(ipos.toVector3D().mag()==0 && ineg.toVector3D().mag()>0) {
                if(ineg.y()>0) {
                top.set(ineg.x(), ineg.y(), ineg.z());
                } else {
                    bottom.set(ineg.x(), ineg.y(), ineg.z());
                }
            }
        }
        result.add(top);
        result.add(bottom);
        return result;
    }

    
}
