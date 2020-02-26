package org.jlab.rec.cvt.trajectory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.Constants;
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
     * @param candCrossList the input list of crosses used in determining a
     * trajectory
     * @param isFinal
     * @return a trajectory object
     */
    public Trajectory findTrajectory(int id, Track trk, 
                                    org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo, 
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

        traj.set_Id(id);

        if (candCrossList.size() == 0) {
            System.err.print("Trajectory Error:  cross list is empty");
            return traj;
        }

        ArrayList<Cross> SVTCrossList = new ArrayList<Cross>();
        ArrayList<Cross> BMTCrossList = new ArrayList<Cross>();

        for (Cross c : candCrossList) {
            if (c.get_Detector().equalsIgnoreCase("SVT")) {
                SVTCrossList.add(c);
            } else {
                BMTCrossList.add(c);
            }
        }

        traj.addAll(SVTCrossList);

        int[] Sectors = new int[org.jlab.rec.cvt.svt.Constants.NLAYR];
        for (int k = 0; k < SVTCrossList.size(); k++) {
            int l = SVTCrossList.get(k).get_Region() * 2 - 1;
            Sectors[l - 1] = SVTCrossList.get(k).get_Sector();
            Sectors[l] = SVTCrossList.get(k).get_Sector();
        }

        for (int a = 0; a < Sectors.length; a++) {
            if (Sectors[a] == 0) {

                Point3D I = helix.getPointAtRadius(org.jlab.rec.cvt.svt.Constants.MODULERADIUS[a][0]);

                int sec = svt_geo.findSectorFromAngle(a + 1, I);
                Sectors[a] = sec;
            }

        }
        traj.set_SVTSector(Sectors);

        ArrayList<StateVec> stateVecs = new ArrayList<StateVec>();
        // SVT
        for (int l = 0; l < org.jlab.rec.cvt.svt.Constants.NLAYR; l++) {
            int layer = l + 1;
            int sector = Sectors[l];

            Point3D helixInterWithBstPlane = svt_geo.intersectionOfHelixWithPlane(layer, sector, helix);
            double R = Math.sqrt(helixInterWithBstPlane.x() * helixInterWithBstPlane.x() + helixInterWithBstPlane.y() * helixInterWithBstPlane.y());

            Vector3D trkDir = helix.getTrackDirectionAtRadius(R);

            StateVec stVec = new StateVec(helixInterWithBstPlane.x(), helixInterWithBstPlane.y(), helixInterWithBstPlane.z(),
                    trkDir.x(), trkDir.y(), trkDir.z());
            stVec.set_planeIdx(l);
            stVec.set_SurfaceDetector(DetectorType.CVT.getDetectorId());
            stVec.set_SurfaceLayer(layer);
            stVec.set_SurfaceSector(sector);
            stVec.set_CalcCentroidStrip(svt_geo.calcNearestStrip(helixInterWithBstPlane.x(), helixInterWithBstPlane.y(), helixInterWithBstPlane.z(), layer, sector));

            this.fill_HelicalTrkAngleWRTSVTPlane(sector, layer, trkDir, svt_geo, stVec);
            stVec.set_ID(id);
            stateVecs.add(stVec);

            // loops over the crosses to get the strip resolutions
            for (Cross c : SVTCrossList) {
                if (matchCrossToStateVec(c, stVec, layer, sector) == false) {
                    continue;
                }

                // set the cross dir
                c.set_Dir(trkDir);

                Cluster clsOnTrk = null;
                if (l % 2 == 0) {
                    clsOnTrk = c.get_Cluster1();
                }
                if (l % 2 == 1) {
                    clsOnTrk = c.get_Cluster2();
                }

                if (clsOnTrk != null && clsOnTrk.get_Layer() == layer) {
                    setHitResolParams("SVT", clsOnTrk.get_Sector(), clsOnTrk.get_Layer(), clsOnTrk,
                            stVec, svt_geo, bmt_geo, traj.isFinal);

                }
            }

        }
        //BMT
        for (int l = org.jlab.rec.cvt.svt.Constants.NLAYR; l < org.jlab.rec.cvt.svt.Constants.NLAYR + 2 * org.jlab.rec.cvt.bmt.Constants.NREGIONS; l++) {
            int BMTRegIdx = (l - org.jlab.rec.cvt.svt.Constants.NLAYR) / 2;

            if (org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[BMTRegIdx] == 0) {
                continue; // Use the correctly defined geometry
            }
            double R = 0;

            if (org.jlab.rec.cvt.bmt.Geometry.getZorC(l + 1-6) == 1) {
                R = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[BMTRegIdx] + org.jlab.rec.cvt.bmt.Constants.LYRTHICKN;
            }
            if (org.jlab.rec.cvt.bmt.Geometry.getZorC(l + 1-6) == 0) {
                R = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[BMTRegIdx] + org.jlab.rec.cvt.bmt.Constants.LYRTHICKN;
            }
            Point3D InterPoint = helix.getPointAtRadius(R);
            Vector3D trkDir = helix.getTrackDirectionAtRadius(R);
            
            StateVec stVec = new StateVec(InterPoint.x(), InterPoint.y(), InterPoint.z(),
                    trkDir.x(), trkDir.y(), trkDir.z());
            
            stVec.set_planeIdx(l);  
            double phiPos = Math.atan2(stVec.y(),stVec.x());
            int sector = bmt_geo.isInSector(BMTRegIdx+1,phiPos, 0);
            stVec.set_SurfaceDetector(DetectorType.CVT.getDetectorId());
            stVec.set_SurfaceSector(sector);
            stVec.set_SurfaceLayer(l+1); 
            stVec.set_ID(id);
            //stateVecs.add(stVec);
            // calculate crosses on BMT layers using track information.  These are used in the event display
            for (Cross c : BMTCrossList) {
                if (matchCrossToStateVec(c, stVec, l + 1, 0) == false) {
                    continue;
                }
                if (c.get_DetectorType().equalsIgnoreCase("C")) { //C-detector measuring Z
                    double x = InterPoint.x();
                    double y = InterPoint.y();
                    if (traj.isFinal) {
                        c.set_Point(new Point3D(x, y, c.get_Point().z()));
                        c.set_Dir(trkDir);
                    }

                    // calculate the hit residuals
                    this.setHitResolParams("BMT", c.get_Cluster1().get_Sector(), c.get_Cluster1().get_Layer(), c.get_Cluster1(),
                            stVec, svt_geo, bmt_geo, traj.isFinal);
                    
//                    StateVec stVecC = new StateVec(InterPoint.x(), InterPoint.y(), InterPoint.z(),
//                    trkDir.x(), trkDir.y(), trkDir.z());
            
//                    stVecC.set_planeIdx(l);
                    //C-detector measuring z                                       
                    stVec.set_CalcCentroidStrip(bmt_geo.getCStrip(BMTRegIdx+1, stVec.z()));
//                    this.fill_HelicalTrkAngleWRTBMTTangentPlane(trkDir, stVec);
                }
//                if (c.get_DetectorType().equalsIgnoreCase("Z")) { //Z-detector measuring phi
                else { //Z-detector measuring phi
                    double z = InterPoint.z();
                    if (traj.isFinal) {
                        c.set_Point(new Point3D(c.get_Point().x(), c.get_Point().y(), z));
                        c.set_Dir(trkDir); 
                    }

                    // calculate the hit residuals
                    this.setHitResolParams("BMT", c.get_Cluster1().get_Sector(), c.get_Cluster1().get_Layer(), c.get_Cluster1(),
                            stVec, svt_geo, bmt_geo, traj.isFinal);
//                    StateVec stVecZ = new StateVec(InterPoint.x(), InterPoint.y(), InterPoint.z(),
//                    trkDir.x(), trkDir.y(), trkDir.z());
//                    stVecZ.set_planeIdx(l);
                    //Z-detector measuring phi   
//                    double phiPos = Math.atan2(stVec.y(),stVec.x());
                    stVec.set_CalcCentroidStrip(bmt_geo.getZStrip(BMTRegIdx+1,  phiPos));
//                    int sector = bmt_geo.isInSector(BMTRegIdx+1,phiPos, 0);
//                    stVecZ.set_SurfaceSector(sector);
                    //Layer starting at 7
//                    stVecZ.set_SurfaceLayer(l+1);
//                    stVecZ.set_ID(id);
//                    stateVecs.add(stVecZ);           
                }
            }

            this.fill_HelicalTrkAngleWRTBMTTangentPlane(trkDir, stVec);
            stateVecs.add(stVec);
                
        }
//        // CTOF
//        if(ctof_geo!=null) {
//            double radius = ctof_geo.getRadius(1);
//            int charge = trk.get_Q();
//            double maxPathLength = 5.0;//very loose cut 
//            swimmer.SetSwimParameters(trk.get_helix().xdca() / 10, trk.get_helix().ydca() / 10, trk.get_helix().get_Z0() / 10, 
//                    Math.toDegrees(trk.get_helix().get_phi_at_dca()), Math.toDegrees(Math.acos(trk.get_helix().costheta())),
//                    trk.get_P(), charge, 
//                    maxPathLength) ;
//            double[] inters = swimmer.SwimToCylinder(radius);
//            StateVec stVec = new StateVec(inters[0]*10, inters[1]*10, inters[2]*10, inters[3], inters[4], inters[5]);
//            stVec.set_SurfaceDetector(DetectorType.CTOF.getDetectorId());
//            stVec.set_SurfaceSector(1);
//            stVec.set_SurfaceLayer(1); 
//            stVec.set_ID(id);
//            stVec.set_TrkPhiAtSurface(Math.atan2(inters[4], inters[3]));
//            stVec.set_TrkThetaAtSurface(Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5])));
//            stVec.set_TrkToModuleAngle(0);
//            stVec.set_Path(inters[6]*10);
//            stateVecs.add(stVec);
//        }
//        // CND
//        for(int ilayer=0; ilayer<cnd_geo.getSector(0).getSuperlayer(0).getNumLayers(); ilayer++) {
//            Point3D center = cnd_geo.getSector(0).getSuperlayer(0).getLayer(ilayer).getComponent(0).getMidpoint();
//            double radius         = Math.sqrt(center.x()*center.x()+center.y()*center.y());
//            double[] inters = swimmer.SwimToCylinder(radius);
//            StateVec stVec = new StateVec(inters[0]*10, inters[1]*10, inters[2]*10, inters[3], inters[4], inters[5]);
//            stVec.set_SurfaceDetector(DetectorType.CND.getDetectorId());
//            stVec.set_SurfaceSector(1);
//            stVec.set_SurfaceLayer(ilayer+1); 
//            stVec.set_ID(id);
//            stVec.set_TrkPhiAtSurface(Math.atan2(inters[4], inters[3]));
//            stVec.set_TrkThetaAtSurface(Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5])));
//            stVec.set_TrkToModuleAngle(0);
//            stVec.set_Path(inters[6]*10);
//            stateVecs.add(stVec);
//        }
               
            
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
        Vector3D trkDir, org.jlab.rec.cvt.svt.Geometry svt_geo, StateVec stVec) {
        Vector3D n = svt_geo.findBSTPlaneNormal(sector, layer);
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

    public Trajectory findTrajectory(int id, Ray ray, ArrayList<Cross> candCrossList, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        Trajectory traj = new Trajectory(ray);
        traj.set_Id(id);

        if (candCrossList.size() == 0) {
            System.err.print("Trajectory Error:  cross list is empty");
            return traj;
        }
        ArrayList<Cross> SVTCrossList = new ArrayList<Cross>();
        ArrayList<Cross> BMTCrossList = new ArrayList<Cross>();

        for (Cross c : candCrossList) {
            if (c.get_Detector().equalsIgnoreCase("SVT")) {
                SVTCrossList.add(c); 
            } else {
                BMTCrossList.add(c);
            }
        }

        traj.addAll(candCrossList);

        ArrayList<StateVec> stateVecs = new ArrayList<StateVec>();

        double[][][] SVTIntersections = calc_trackIntersSVT(ray, svt_geo);

        for (int l = 0; l < Constants.NLAYR; l++) {
            for (int s = 0; s < Constants.NSECT[l]; s++) {

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

                    stateVecs.add(stVec);
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

        double[][][] BMTIntersections = calc_trackIntersBMT(ray, bmt_geo, org.jlab.rec.cvt.bmt.Constants.STARTINGLAYR);

        for (int l = org.jlab.rec.cvt.bmt.Constants.STARTINGLAYR - 1; l < 6; l++) {
            //hemisphere 1-2
            for (int h = 0; h < 2; h++) {

                if (BMTIntersections[l][h][0] != -999) {

                    int LayerTrackIntersSurf = (l + 1);
                    double XtrackIntersSurf = BMTIntersections[l][h][0];
                    double YtrackIntersSurf = BMTIntersections[l][h][1];
                    double ZtrackIntersSurf = BMTIntersections[l][h][2];
                    int SectorTrackIntersSurf = bmt_geo.isInSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf), Math.toRadians(org.jlab.rec.cvt.bmt.Constants.isInSectorJitter));
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

                    stateVecs.add(stVec);

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
                        if (c.get_DetectorType().equalsIgnoreCase("C")) { //C-detector measuring Z
                            //if(traj.isFinal) { // reset the cross only for final trajectory

                            c.set_Point(new Point3D(XtrackIntersSurf, YtrackIntersSurf, c.get_Point().z()));
                            c.set_Dir(ray.get_dirVec());
                            //}

                            // calculate the hit residuals // CHECK THIS ........
                            this.setHitResolParams("BMT", c.get_Sector(), c.get_Cluster2().get_Layer(), c.get_Cluster2(),
                                    stVec, svt_geo, bmt_geo, traj.isFinal);

                        }
                        if (c.get_DetectorType().equalsIgnoreCase("Z")) { //Z-detector measuring phi
                            //if(traj.isFinal) {

                            c.set_Point(new Point3D(c.get_Point().x(), c.get_Point().y(), ZtrackIntersSurf));
                            c.set_Dir(ray.get_dirVec());
                            //}

                            // calculate the hit residuals
                            this.setHitResolParams("BMT", c.get_Cluster1().get_Sector(), c.get_Cluster1().get_Layer(), c.get_Cluster1(),
                                    stVec, svt_geo, bmt_geo, traj.isFinal);

                        }
                    }

                }
            }
        }
        Collections.sort(stateVecs);
        for (int l = 0; l < stateVecs.size(); l++) {
            stateVecs.get(l).set_planeIdx(l);
        }
        traj.set_Trajectory(stateVecs);
        traj.set_SVTIntersections(SVTIntersections);
        traj.set_BMTIntersections(BMTIntersections);
        return traj;
    }

    private boolean matchCrossToStateVec(Cross c, StateVec stVec, int layer, int sector) {
        boolean value = false;
        if (c.get_Detector().equalsIgnoreCase("SVT")) {
            int l = layer - 1;
            value = true;
            if (c.get_Region() != (int) (l / 2) + 1) {
                value = false;	// require same region
            }
            if (c.get_Sector() != sector) {
                value = false;		// same sector 
            } 
            double deltaXt = Math.sqrt((stVec.x() - c.get_Point().x()) * (stVec.x() - c.get_Point().x()) + (stVec.y() - c.get_Point().y()) * (stVec.y() - c.get_Point().y()));
            if (deltaXt > org.jlab.rec.cvt.svt.Constants.ACTIVESENWIDTH / 2) {
                value = false; // within 1/2 module width
            }
            
        }

        if (c.get_Detector().equalsIgnoreCase("BMT")) { // BMT
            value = true;
            //int l = layer - 9;
            int l = layer - 7;
            if (c.get_Region() != (int) (l / 2) + 1) {
                value = false;	// reauire same region
            }
            if (c.get_DetectorType().equalsIgnoreCase("C")) { //C-detector measuring Z
                if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 1) { //Z-detector measuring phi
                    value = false;
                }
            	
                if (Math.abs(stVec.z() - c.get_Point0().z()) > Constants.interTol) {
                    value = false;
                }
            }
            if (c.get_DetectorType().equalsIgnoreCase("Z")) { //Z-detector measuring phi
                if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 0) { //C-detector 
                    value = false;
                }
                double deltaXt = Math.sqrt((stVec.x() - c.get_Point().x()) * (stVec.x() - c.get_Point().x()) + (stVec.y() - c.get_Point().y()) * (stVec.y() - c.get_Point().y()));
                if (deltaXt > 2*Constants.interTol) {
                    value = false;
                }
            }
        }

        return value;
    }

    /**
     *
     * @param detector
     * @param superlayer 0,1 (bottom, top)
     * @param Cluster
     * @param stVec stateVec
     */
    public void setHitResolParams(String detector, int sector, int layer, Cluster cluster,
            StateVec stVec, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo, boolean trajFinal) {

        if (detector.equalsIgnoreCase("SVT") ) {
            double doca2Cls = svt_geo.getDOCAToStrip(sector, layer, cluster.get_Centroid(), new Point3D(stVec.x(), stVec.y(), stVec.z()));
            double doca2Seed = svt_geo.getDOCAToStrip(sector, layer, (double) cluster.get_SeedStrip(), new Point3D(stVec.x(), stVec.y(), stVec.z()));
            cluster.set_SeedResidual(doca2Seed); 
            cluster.set_CentroidResidual(doca2Cls);
            
            for (FittedHit hit : cluster) {
                double doca1 = svt_geo.getDOCAToStrip(sector, layer, (double) hit.get_Strip().get_Strip(), new Point3D(stVec.x(), stVec.y(), stVec.z()));
                double sigma1 = svt_geo.getSingleStripResolution(layer, hit.get_Strip().get_Strip(), stVec.z());
                hit.set_stripResolutionAtDoca(sigma1);
                hit.set_docaToTrk(doca1);
                if (trajFinal) {
                    hit.set_TrkgStatus(2);
                }
            }
        }
        if (detector.equalsIgnoreCase("BMT")) {
            if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 0) { //C-detector measuring z
                for (FittedHit h1 : cluster) {
                    // calculate the hit residuals
                    double docaToTrk = stVec.z() - h1.get_Strip().get_Z();
                    double stripResol = h1.get_Strip().get_ZErr();
                    h1.set_docaToTrk(docaToTrk);
                    h1.set_stripResolutionAtDoca(stripResol);
                    if (trajFinal) {
                        h1.set_TrkgStatus(2);
                    }
                }
            }
            if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 1) { //Z-detector measuring phi
                // calculate the hit residuals
                for (FittedHit h1 : cluster) {
                    double StripX = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[(cluster.get_Layer() + 1) / 2 - 1] * Math.cos(h1.get_Strip().get_Phi());
                    double StripY = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[(cluster.get_Layer() + 1) / 2 - 1] * Math.sin(h1.get_Strip().get_Phi());

                    double Sign = Math.signum(Math.atan2(StripY - stVec.y(), StripX - stVec.x()));
                    double docaToTrk = Sign * Math.sqrt((StripX - stVec.x()) * (StripX - stVec.x()) + (StripY - stVec.y()) * (StripY - stVec.y()));
                    double stripResol = h1.get_Strip().get_PhiErr();
                    h1.set_docaToTrk(docaToTrk);
                    h1.set_stripResolutionAtDoca(stripResol);
                    if (trajFinal) {
                        h1.set_TrkgStatus(2);
                    }
                }
            }
        }
    }

    private double[][][] calc_trackIntersBMT(Ray ray, org.jlab.rec.cvt.bmt.Geometry bmt_geo, int start_layer) {
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
        for (int l = start_layer - 1; l < 6; l++) {
            double[][] trkIntersCombinedInf = this.getIntersectionTrackWithBMTModules(l, ray.get_yxinterc(), ray.get_yxslope(), ray.get_yzinterc(), ray.get_yzslope(), bmt_geo);

            //hemisphere 1-2
            for (int h = 0; h < 2; h++) {
                double[] trkIntersInf = trkIntersCombinedInf[0];

                Point3D p = new Point3D(trkIntersInf[0], trkIntersInf[1], trkIntersInf[2]);

                if (p.toVector3D().mag() == 0) {
                    continue;
                }

                Vector3D n = new Vector3D(Math.cos(Math.atan2(p.y(), p.x())), Math.sin(Math.atan2(p.y(), p.x())), 0);
                Vector3D ui = new Vector3D(n.y(), -n.x(), 0); //longitudinal vector along the local x direction of the module

                Vector3D uj = ui.cross(n); //longitudinal vector along the local z direction of the module

                double norm = Math.sqrt(ray.get_yxslope() * ray.get_yxslope() + ray.get_yzslope() * ray.get_yzslope() + 1);

                Vector3D u = new Vector3D(ray.get_yxslope() / norm, 1 / norm, ray.get_yzslope() / norm);

                if (p.y() < 0) {
                    u = new Vector3D(-ray.get_yxslope() / norm, -1 / norm, -ray.get_yzslope() / norm);
                }

                double trkToMPlnAngl = Math.acos(u.dot(ui));

                double zl = u.dot(n);
                double xl = u.dot(ui);
                double yl = u.dot(uj);

                double phi = Math.atan2(yl, xl);
                double theta = Math.acos(zl);

                result[l][h][0] = p.x();
                result[l][h][1] = p.y();
                result[l][h][2] = p.z();
                result[l][h][3] = Math.toDegrees(phi);
                result[l][h][4] = Math.toDegrees(theta);
                result[l][h][5] = Math.toDegrees(trkToMPlnAngl);
                result[l][h][6] = trkIntersInf[3];
            }
        }
        return result;
    }

    private double[][][] calc_trackIntersSVT(Ray ray, org.jlab.rec.cvt.svt.Geometry svt_geo) {
        //[l][s], [0,1,2,3,4]=x,y,z,phi,theta,estimated centroid strip
        double[][][] result = new double[org.jlab.rec.cvt.svt.Constants.NLAYR][org.jlab.rec.cvt.svt.Constants.NSECT[org.jlab.rec.cvt.svt.Constants.NLAYR - 1]][7];
        for (int l = 0; l < org.jlab.rec.cvt.svt.Constants.NLAYR; l++) {
            for (int s = 0; s < org.jlab.rec.cvt.svt.Constants.NSECT[l]; s++) {
                result[l][s][0] = -999;
                result[l][s][1] = -999;
                result[l][s][2] = -999;
                result[l][s][3] = -999;
                result[l][s][4] = -999;
            }
        }
        //Layer 1-8:
        for (int l = 0; l < org.jlab.rec.cvt.svt.Constants.NLAYR; l++) {
            for (int s = 0; s < org.jlab.rec.cvt.svt.Constants.NSECT[l]; s++) {

                double[] trkIntersInf = this.getIntersectionTrackWithSVTModule(s, l, ray.get_yxinterc(), ray.get_yxslope(), ray.get_yzinterc(), ray.get_yzslope(), svt_geo);

                Point3D p = new Point3D(trkIntersInf[0], trkIntersInf[1], trkIntersInf[2]);

                if (p.toVector3D().mag() == 0) {
                    continue;
                }

                if ((Math.sqrt(p.x() * p.x() + p.y() * p.y()) <= Math.sqrt(0.25 * Constants.ACTIVESENLEN * Constants.ACTIVESENWIDTH + Constants.MODULERADIUS[l][0] * Constants.MODULERADIUS[l][0]))) {

                    Vector3D n = svt_geo.findBSTPlaneNormal(s + 1, l + 1);
                    Vector3D ui = new Vector3D(n.y(), -n.x(), 0); //longitudinal vector along the local x direction of the module

                    Vector3D uj = ui.cross(n); //longitudinal vector along the local z direction of the module

                    double norm = Math.sqrt(ray.get_yxslope() * ray.get_yxslope() + ray.get_yzslope() * ray.get_yzslope() + 1);

                    Vector3D u = new Vector3D(ray.get_yxslope() / norm, 1 / norm, ray.get_yzslope() / norm);

                    if (p.y() < 0) {
                        u = new Vector3D(-ray.get_yxslope() / norm, -1 / norm, -ray.get_yzslope() / norm);
                    }

                    double trkToMPlnAngl = Math.acos(u.dot(ui));

                    double zl = u.dot(n);
                    double xl = u.dot(ui);
                    double yl = u.dot(uj);

                    double phi = Math.atan2(yl, xl);
                    double theta = Math.acos(zl);

                    result[l][s][0] = p.x();
                    result[l][s][1] = p.y();
                    result[l][s][2] = p.z();
                    result[l][s][3] = Math.toDegrees(phi);
                    result[l][s][4] = Math.toDegrees(theta);
                    result[l][s][5] = Math.toDegrees(trkToMPlnAngl);
                    result[l][s][6] = trkIntersInf[3];
                }
            }
        }
        return result;
    }

    private double[] getIntersectionTrackWithSVTModule(int s, int l,
            double _yxinterc2, double _yxslope2, double _yzinterc2,
            double _yzslope2, org.jlab.rec.cvt.svt.Geometry geo) {
        // array [][][][] =[x][y][z][stripCentroid]
        double[] inters = new double[4];
        inters[0] = Double.NaN;
        inters[1] = Double.NaN;
        inters[2] = Double.NaN;
        inters[3] = Double.NaN;

        double epsilon = 1e-6;

        Vector3D n = geo.findBSTPlaneNormal(s + 1, l + 1);

        double dot = (n.x() * _yxslope2 + n.y());

        if (Math.abs(dot) > epsilon) {
            //threeVec w = new threeVec(_yxinterc2+Constants.MODULERADIUS[l][0]*Math.sin(angle), -Constants.MODULERADIUS[l][0]*Math.cos(angle), _yzinterc2);
            Vector3D w = new Vector3D(_yxinterc2 - Constants.MODULERADIUS[l][0] * n.x(), -Constants.MODULERADIUS[l][0] * n.y(), _yzinterc2);
            double y = -(n.x() * w.x() + n.y() * w.y() + n.z() * w.z()) / dot;
            //threeVec Delt = new threeVec(y*_yxslope2+_yxinterc2+Constants.MODULERADIUS[l][0]*Math.sin(angle),y-Constants.MODULERADIUS[l][0]*Math.cos(angle),0);
            Vector3D Delt = new Vector3D(y * _yxslope2 + _yxinterc2 - Constants.MODULERADIUS[l][0] * n.x(), y - Constants.MODULERADIUS[l][0] * n.y(), 0);

            if (Delt.mag() < Constants.ACTIVESENWIDTH / 2 + Constants.TOLTOMODULEEDGE) {
                inters[0] = y * _yxslope2 + _yxinterc2;
                inters[1] = y;
                inters[2] = y * _yzslope2 + _yzinterc2;
                inters[3] = geo.calcNearestStrip(inters[0], inters[1], inters[2], l + 1, s + 1);
            }
            return inters;
        }

        return inters;
    }

    private double[][] getIntersectionTrackWithBMTModules(int l,
            double _yxinterc2, double _yxslope2, double _yzinterc2,
            double _yzslope2, org.jlab.rec.cvt.bmt.Geometry geo) {
        // array [][][][] =[x][y][z][stripCentroid]
        double[][] inters = new double[2][4];
        double[] inters_top = new double[4];
        inters_top[0] = Double.NaN;
        inters_top[1] = Double.NaN;
        inters_top[2] = Double.NaN;
        inters_top[3] = Double.NaN;

        double[] inters_bottom = new double[4];
        inters_bottom[0] = Double.NaN;
        inters_bottom[1] = Double.NaN;
        inters_bottom[2] = Double.NaN;
        inters_bottom[3] = Double.NaN;

        double R = 0;
        if (org.jlab.rec.cvt.bmt.Geometry.getZorC(l + 1) == 0) {
            R = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[l / 2] + org.jlab.rec.cvt.bmt.Constants.LYRTHICKN;
        }
        if (org.jlab.rec.cvt.bmt.Geometry.getZorC(l + 1) == 1) {
            R = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[l / 2] + org.jlab.rec.cvt.bmt.Constants.LYRTHICKN;
        }
        // solve for intersection of line with cylinder of radius R
        // x = _yxslope2*y +_yxinterc2; x^2+y^2 = R^2
        double Delta = _yxslope2 * _yxslope2 * _yxinterc2 * _yxinterc2 + (R * R - _yxinterc2 * _yxinterc2) * (_yxslope2 * _yxslope2 + 1);
        double y_plus = (-_yxslope2 * _yxinterc2 + Math.sqrt(Delta)) / (_yxslope2 * _yxslope2 + 1);
        double y_minus = (-_yxslope2 * _yxinterc2 - Math.sqrt(Delta)) / (_yxslope2 * _yxslope2 + 1);

        double x_plus = _yxslope2 * y_plus + _yxinterc2;
        double x_minus = _yxslope2 * y_minus + _yxinterc2;

        double z_plus = _yzslope2 * y_plus + _yzinterc2;
        double z_minus = _yzslope2 * y_minus + _yzinterc2;

        if (geo.isInFiducial(x_plus, y_plus, z_plus, l + 1)) {
            inters_top[0] = x_plus;
            inters_top[1] = y_plus;
            inters_top[2] = z_plus;
            if (l % 2 == 1) {
                inters_top[3] = geo.getCStrip(l + 1, z_plus);
            }
            if (l % 2 == 0) {
                inters_top[3] = geo.getZStrip(l + 1, Math.atan2(y_plus, x_plus));
            }
        }
        if (geo.isInFiducial(x_minus, y_minus, z_minus, l + 1)) {
            inters_bottom[0] = x_minus;
            inters_bottom[1] = y_minus;
            inters_bottom[2] = z_minus;
            if (l % 2 == 1) {
                inters_bottom[3] = geo.getCStrip(l + 1, z_minus);
            }
            if (l % 2 == 0) {
                inters_bottom[3] = geo.getZStrip(l + 1, Math.atan2(y_minus, x_minus));
            }
        }

        inters[1] = inters_top;
        inters[0] = inters_bottom;

        return inters;
    }

    private Hit[][][] HitArray;

    public void hitsToArray(List<Hit> hits2) {
        if (hits2 == null) {
            return;
        }
        HitArray = new Hit[Constants.NLAYR][Constants.NSECT[Constants.NLAYR - 1]][Constants.NSTRIP];

        // initializing non-zero Hit Array entries
        // with valid hits
        for (Hit hit : hits2) {

            int w = hit.get_Strip().get_Strip();
            int l = hit.get_Layer();
            int s = hit.get_Sector();

            HitArray[l - 1][s - 1][w - 1] = hit;

        }
    }

}
