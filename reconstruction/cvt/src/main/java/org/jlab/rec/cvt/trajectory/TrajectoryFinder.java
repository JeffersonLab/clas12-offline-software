package org.jlab.rec.cvt.trajectory;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTGeometry;

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

    public Trajectory findTrajectory(int id, Ray ray, ArrayList<Cross> candCrossList) {
        Trajectory traj = new Trajectory(ray);
        traj.setId(id);

        if (candCrossList.isEmpty()) {
            System.err.print("Trajectory Error:  cross list is empty");
            return traj;
        }
        ArrayList<Cross> SVTCrossList = new ArrayList<>();
        ArrayList<Cross> BMTCrossList = new ArrayList<>();

        Map<String, Double> ClsMap = new HashMap<>();
        for (Cross c : candCrossList) {
            if (c.getDetector()==DetectorType.BST) {
                String svtSt1 = "1.";
                svtSt1+=c.getCluster1().getSector();
                svtSt1 += ".";
                svtSt1+=c.getCluster1().getLayer();
                ClsMap.put(svtSt1, c.getCluster1().getCentroid());
                String svtSt2 = "1.";
                svtSt2+=c.getCluster2().getSector();
                svtSt2 += ".";
                svtSt2+=c.getCluster2().getLayer();
                ClsMap.put(svtSt2, c.getCluster2().getCentroid());
                SVTCrossList.add(c);
            } else {
                String bmtSt1 = "2.";
                bmtSt1+=c.getCluster1().getSector();
                bmtSt1 += ".";
                bmtSt1+=c.getCluster1().getLayer();
                ClsMap.put(bmtSt1, c.getCluster1().getCentroid());
                BMTCrossList.add(c);
            }
        }

        traj.addAll(candCrossList);

        ArrayList<StateVec> stateVecs = new ArrayList<>();

        double[][][] SVTIntersections = calcTrackIntersSVT(ray);

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

                    StateVec stVec = new StateVec(XtrackIntersPlane, YtrackIntersPlane, ZtrackIntersPlane, ray.getDirVec().x(), ray.getDirVec().y(), ray.getDirVec().z());
                    stVec.setID(id);
                    stVec.setSurfaceLayer(LayerTrackIntersPlane);
                    stVec.setSurfaceSector(SectorTrackIntersPlane);
                    stVec.setTrkPhiAtSurface(PhiTrackIntersPlane);
                    stVec.setTrkThetaAtSurface(ThetaTrackIntersPlane);
                    stVec.setTrkToModuleAngle(trkToMPlnAngl);
                    stVec.setCalcCentroidStrip(CalcCentroidStrip);
                    String svtSt1 = "1.";
                    svtSt1+=SectorTrackIntersPlane;
                    svtSt1 += ".";
                    svtSt1+=LayerTrackIntersPlane;
                    if(ClsMap.get(svtSt1)!=null) { 
                        double cent = ClsMap.get(svtSt1);
                        stVec.setCalcCentroidStrip(cent); 
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
                            clsOnTrk = c.getCluster1(); 
                        }
                        if (l % 2 == 1) {
                            clsOnTrk = c.getCluster2(); 
                        }

                        if (clsOnTrk != null && clsOnTrk.getLayer() == l + 1) {

                            setHitResolParams("SVT", clsOnTrk.getSector(), clsOnTrk.getLayer(), clsOnTrk,
                                    stVec, traj.isFinal);

                        }
                    }
                }
            }
        }
        
        double[][][] BMTIntersections = calcTrackIntersBMT(ray, BMTConstants.STARTINGLAYR);

        for (int l = BMTConstants.STARTINGLAYR - 1; l < 6; l++) {
            //hemisphere 1-2
            for (int h = 0; h < 2; h++) {

                if (BMTIntersections[l][h][0] != -999) {

                    int LayerTrackIntersSurf = (l + 1);
                    double XtrackIntersSurf = BMTIntersections[l][h][0];
                    double YtrackIntersSurf = BMTIntersections[l][h][1];
                    double ZtrackIntersSurf = BMTIntersections[l][h][2];
                    //int SectorTrackIntersSurf = bmt_geo.isInSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf), Math.toRadians(BMTConstants.ISINSECTORJITTER));
                    int SectorTrackIntersSurf = Constants.getInstance().BMTGEOMETRY.getSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf));
                    double PhiTrackIntersSurf = BMTIntersections[l][h][3];
                    double ThetaTrackIntersSurf = BMTIntersections[l][h][4];
                    double trkToMPlnAngl = BMTIntersections[l][h][5];
                    double CalcCentroidStrip = BMTIntersections[l][h][6];

                    StateVec stVec = new StateVec(XtrackIntersSurf, YtrackIntersSurf, ZtrackIntersSurf, ray.getDirVec().x(), ray.getDirVec().y(), ray.getDirVec().z());

                    stVec.setID(id);
                    stVec.setSurfaceLayer(LayerTrackIntersSurf);
                    stVec.setSurfaceSector(SectorTrackIntersSurf);
                    stVec.setTrkPhiAtSurface(PhiTrackIntersSurf);
                    stVec.setTrkThetaAtSurface(ThetaTrackIntersSurf);
                    stVec.setTrkToModuleAngle(trkToMPlnAngl);
                    stVec.setCalcCentroidStrip(CalcCentroidStrip); 
                    String bmtSt1 = "2.";
                    bmtSt1+=SectorTrackIntersSurf;
                    bmtSt1 += ".";
                    bmtSt1+=LayerTrackIntersSurf;
                    if(ClsMap.get(bmtSt1)!=null) { 
                        double cent = ClsMap.get(bmtSt1);
                        stVec.setCalcCentroidStrip(cent); 
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
                        if (c.getRegion() != (int) (l / 2) + 1) {
                            continue;
                        }
                        
                        if (this.matchCrossToStateVec(c, stVec, l + 1, c.getSector()) == false) {
                            continue;
                        } 
                        
                        if (c.getType()==BMTType.C) { //C-detector measuring Z
                            //if(traj.isFinal) { // reset the cross only for final trajectory

//                            c.setPoint(new Point3D(XtrackIntersSurf, YtrackIntersSurf, c.getPoint().z()));
//                            c.setDir(ray.getDirVec());
                            //}

                            // calculate the hit residuals // CHECK THIS ........
                            this.setHitResolParams("BMT", c.getSector(), c.getCluster1().getLayer(), c.getCluster1(),
                                    stVec, traj.isFinal);

                        }
                        if (c.getType()==BMTType.Z) { //Z-detector measuring phi
                            //if(traj.isFinal) {

//                            c.setPoint(new Point3D(c.getPoint().x(), c.getPoint().y(), ZtrackIntersSurf));
//                            c.setDir(ray.getDirVec());
//                           //}

                            // calculate the hit residuals
                            this.setHitResolParams("BMT", c.getCluster1().getSector(), c.getCluster1().getLayer(), c.getCluster1(),
                                    stVec, traj.isFinal);

                        }
                    }

                }
            }
        }

        //Collections.sort(stateVecs);
        
        stateVecs.sort(Comparator.comparing(StateVec::y));
        for (int l = 0; l < stateVecs.size(); l++) {
            stateVecs.get(l).setPlaneIdx(l);
        }
        traj.setTrajectory(stateVecs);
        traj.setSVTIntersections(SVTIntersections);
        traj.setBMTIntersections(BMTIntersections);
        return traj;
    }

    private boolean matchCrossToStateVec(Cross c, StateVec stVec, int layer, int sector) {
        
        if (c.getDetector()==DetectorType.BST) {
            int l = layer - 1;
            
            if (c.getRegion() != (int) (l / 2) + 1) {
                return false;	// require same region
            }
            if (c.getSector() != sector) {
                return false;		// same sector 
            } 
            double deltaXt = Math.sqrt((stVec.x() - c.getPoint().x()) * (stVec.x() - c.getPoint().x()) + (stVec.y() - c.getPoint().y()) * (stVec.y() - c.getPoint().y()));
            if (deltaXt > SVTGeometry.getActiveSensorWidth() / 2) {
                return false; // within 1/2 module width
            }
        }

        if (c.getDetector()==DetectorType.BMT) { // BMT
            
            double Rsv = Math.sqrt(stVec.x()*stVec.x()+stVec.y()*stVec.y());
            double Rcs = Math.sqrt(c.getPoint().x()*c.getPoint().x()+c.getPoint().y()*c.getPoint().y());

            if(Math.abs(Rsv-Rcs)>1.e-01) {
                return false;
            } 
            
            if(new Vector3D(stVec.x(), stVec.y(), stVec.z()).asUnit().dot(c.getPoint().toVector3D().asUnit())<0.8){
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
     * @param trajFinal
     */
    public void setHitResolParams(String detector, int sector, int layer, Cluster cluster,
            StateVec stVec, boolean trajFinal) {

        if (detector.equalsIgnoreCase("SVT") ) {
            double doca2Cls = cluster.residual(new Point3D(stVec.x(), stVec.y(), stVec.z()));
            double doca2Seed = cluster.get(0).residual(new Point3D(stVec.x(), stVec.y(), stVec.z()));
            cluster.setSeedResidual(doca2Seed); 
            cluster.setCentroidResidual(doca2Cls);
            cluster.setTrakInters(new Point3D(stVec.x(), stVec.y(), stVec.z()));
//            Point3D endPt1 = cluster.getEndPoint1();
//            Point3D endPt2 = cluster.getEndPoint2();
//            Line3D l = new Line3D(endPt1,endPt2);
//            Plane3D pl = new Plane3D(endPt1, svt_geo.findBSTPlaneNormal(sector, layer));
//            double d = new Vector3D(stVec.x(), stVec.y(), stVec.z()).dot(pl.normal())-pl.point().toVector3D().dot(pl.normal());
//            System.out.println(d+" calc "+l.distance(new Point3D(stVec.x(), stVec.y(), stVec.z())).length()+" d "+doca2Cls);
            for (Hit hit : cluster) {
                double doca1 = hit.residual(new Point3D(stVec.x(), stVec.y(), stVec.z()));
                double sigma1 = Constants.getInstance().SVTGEOMETRY.getSingleStripResolution(layer, hit.getStrip().getStrip(), stVec.z());
                hit.setstripResolutionAtDoca(sigma1);
                hit.setdocaToTrk(doca1);  
                hit.setTrkgStatus(1);
                if (trajFinal) {
                    hit.setTrkgStatus(2);
                }
            }
        }
        if (detector.equalsIgnoreCase("BMT")) { 
            cluster.setTrakInters(new Point3D(stVec.x(), stVec.y(), stVec.z()));
            Point3D    offset = Constants.getInstance().BMTGEOMETRY.getOffset(cluster.getLayer(), cluster.getSector()); 
            Vector3D rotation = Constants.getInstance().BMTGEOMETRY.getRotation(cluster.getLayer(), cluster.getSector());
            double ce = cluster.getCentroid();    
            Point3D p = new Point3D(stVec.x(), stVec.y(), stVec.z());
            if (BMTGeometry.getDetectorType(layer) == BMTType.C) { //C-detector measuring z
                cluster.setCentroidResidual(p);
                cluster.setSeedResidual(p);
                for (Hit h1 : cluster) {
                    // calculate the hit residuals
                    h1.setTrkgStatus(1);
                    h1.setdocaToTrk(p);
                    h1.setTrkgStatus(1);
                    if (trajFinal) {
                        h1.setTrkgStatus(2);
                    }
                }
                
            }
            if (BMTGeometry.getDetectorType(layer) == BMTType.Z) { //Z-detector measuring phi
                int bsector = cluster.getSector();
                int blayer = cluster.getLayer();
                double cxh = Math.cos(cluster.getPhi())*Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(blayer);
                double cyh = Math.sin(cluster.getPhi())*Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(blayer);
                double phic = Constants.getInstance().BMTGEOMETRY.getPhi(blayer, bsector, new Point3D(cxh,cyh,0));
                double phit = Constants.getInstance().BMTGEOMETRY.getPhi(blayer, bsector, p);
                double doca2Cls = (phic-phit)*Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(blayer);
                cluster.setCentroidResidual(doca2Cls);

                for (Hit h1 : cluster) {
                    double xh = Math.cos(h1.getStrip().getPhi())*Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(blayer);
                    double yh = Math.sin(h1.getStrip().getPhi())*Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(blayer);
                    double hphic = Constants.getInstance().BMTGEOMETRY.getPhi(blayer, bsector, new Point3D(xh,yh,0));
                    double hphit = Constants.getInstance().BMTGEOMETRY.getPhi(blayer, bsector, p);
                    double doca1 = (hphic-hphit)*Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(blayer);

                    if(h1.getStrip().getStrip()==cluster.getSeedStrip().getStrip())
                        cluster.setSeedResidual(doca1); 
                    
                    h1.setTrkgStatus(1);
                    if (trajFinal) {
                        h1.setTrkgStatus(2);
                    }
                }
            }
        }
    }

    public static double[][][] calcTrackIntersBMT(Ray ray, int start_layer) {

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
            for(int s=0; s<Constants.getInstance().BMTGEOMETRY.getNSectors(); s++) {
              int layer = l+1;
                int sector = s+1;
                
                List<Point3D> trajs = new ArrayList<>();
                int ntraj = Constants.getInstance().BMTGEOMETRY.getTileSurface(layer, sector).intersection(line, trajs);
//                System.out.println(layer + " " + sector);
                if(ntraj>0) {
                    for(int i=0; i<trajs.size(); i++) {    
                        Point3D traj = trajs.get(i);
                        // define hemisphere
                        int h = (int) Math.signum(traj.y());
                        if(h<0) h =0;                             
                        // get track direction in local frame
                        Vector3D local = new Vector3D(line.direction()).asUnit();
                        Constants.getInstance().BMTGEOMETRY.toLocal(layer, sector).apply(local);
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
                        result[l][h][6] = Constants.getInstance().BMTGEOMETRY.getStrip(layer, sector, traj);
//                        System.out.println("New " + local.x() + " " + local.y() + " " + local.z() + " " 
//                                                  + result[l][h][0] + " " + result[l][h][1] + " " + result[l][h][2] + " "
//                                                  + result[l][h][3] + " " + result[l][h][4] + " " + result[l][h][5] + " " + result[l][h][6]);
                    }
                } 
            }
        }
        return result;
    }

    public static double[][][] calcTrackIntersSVT(Ray ray) {
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
                int ntraj = Constants.getInstance().SVTGEOMETRY.getPlane(layer, sector).intersection(line, traj);
                if(ntraj==1 && Constants.getInstance().SVTGEOMETRY.isInFiducial(layer, sector, traj)) {
                    Vector3D local = new Vector3D(line.direction()).asUnit();
                    local = Constants.getInstance().SVTGEOMETRY.toLocal(layer, sector, local);
                    if(traj.y()>0) local.negative();
                    local.rotateX(Math.toRadians(90));
                    result[l][s][0] = traj.x();
                    result[l][s][1] = traj.y();
                    result[l][s][2] = traj.z();
                    result[l][s][3] = Math.toDegrees(local.phi());
                    result[l][s][4] = Math.toDegrees(local.theta());
                    result[l][s][5] = Math.toDegrees(Math.acos(local.x()));
                    result[l][s][6] = Constants.getInstance().SVTGEOMETRY.calcNearestStrip(traj.x(), traj.y(), traj.z(), layer, sector);
                }
            }
        }
        return result;
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

            int w = hit.getStrip().getStrip();
            int l = hit.getLayer();
            int s = hit.getSector();

            HitArray[l - 1][s - 1][w - 1] = hit;

        }
    }
    
}
