package org.jlab.rec.dc.trajectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;

import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;

import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * A class to load the geometry constants used in the DC reconstruction. The
 * coordinate system used in the Tilted Sector coordinate system.
 *
 * @author ziegler
 *
 */
public class TrajectorySurfaces {

    public static Logger LOGGER = Logger.getLogger(TrajectorySurfaces.class.getName());

    private List<ArrayList<Surface>> _DetectorPlanes = new ArrayList<ArrayList<Surface>>();

    public List<ArrayList<Surface>> getDetectorPlanes() {
        return _DetectorPlanes;
    }

    public synchronized void setDetectorPlanes(List<ArrayList<Surface>> aDetectorPlanes) {
        _DetectorPlanes = aDetectorPlanes;
    }

    public void LoadSurfaces(double targetPosition, double targetLength, DCGeant4Factory dcDetector,
            FTOFGeant4Factory ftofDetector, Detector ecalDetector, Detector fmtDetector) {
        // creating Boundaries for MS
        Constants.getInstance().Z[0]= targetPosition;
        Constants.getInstance().Z[1]= dcDetector.getWireMidpoint(0, 0, 0, 0).z;
        Constants.getInstance().Z[2]= dcDetector.getWireMidpoint(0, 0, 5, 0).z;
        Constants.getInstance().Z[3]= dcDetector.getWireMidpoint(0, 1, 0, 0).z;
        Constants.getInstance().Z[4]= dcDetector.getWireMidpoint(0, 1, 5, 0).z;
        Constants.getInstance().Z[5]= dcDetector.getWireMidpoint(0, 2, 0, 0).z;
        Constants.getInstance().Z[6]= dcDetector.getWireMidpoint(0, 2, 5, 0).z;
        Constants.getInstance().Z[7]= dcDetector.getWireMidpoint(0, 3, 0, 0).z;
        Constants.getInstance().Z[8]= dcDetector.getWireMidpoint(0, 3, 5, 0).z;
        Constants.getInstance().Z[9]= dcDetector.getWireMidpoint(0, 4, 0, 0).z;
        Constants.getInstance().Z[10]= dcDetector.getWireMidpoint(0, 4, 5, 0).z;
        Constants.getInstance().Z[11]= dcDetector.getWireMidpoint(0, 5, 0, 0).z;
        Constants.getInstance().Z[12]= dcDetector.getWireMidpoint(0, 5, 5, 0).z;
        //DcDetector.getWireMidpoint(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).z;
        
        double d = 0;
        Vector3D n,P;
        for(int is =0; is<6; is++) {

            this._DetectorPlanes.add(new ArrayList<>());

            // Add target center and downstream wall
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.TARGET, DetectorLayer.TARGET_DOWNSTREAM, targetPosition+targetLength/2, 0., 0., 1.));
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.TARGET, DetectorLayer.TARGET_CENTER, targetPosition, 0., 0., 1.));

            // Add FMT layers
            for (int li=0; li<6; ++li) {
                P = fmtDetector.getSector(0).getSuperlayer(0).getLayer(li).getPlane().point().toVector3D();
                n = fmtDetector.getSector(0).getSuperlayer(0).getLayer(li).getPlane().normal();
                d = P.dot(n);
                this._DetectorPlanes.get(is).add(new Surface(DetectorType.FMT, li+1, d,  n.x(), n.y(), n.z()));
            }

            // Add DC
            //n = this.RotateFromTSCtoLabC(0,0,1, is+1).toVector3D();
            // don't rotate to the lab
            n = new Vector3D(0,0,1);
            for(int isup =0; isup<6; isup++) {
                for(int il =5; il<6; il++) { // include only layer 6
                    d = dcDetector.getWireMidpoint(is, isup, il, 0).z;
                    this._DetectorPlanes.get(is).add(new Surface(DetectorType.DC, isup*6+il+1,d, n.x(), n.y(), n.z()));

                }
            }
            //outer detectors
             //FTOF 2
            P = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF2).point().toVector3D();
            n = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF2).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.FTOF, DetectorLayer.FTOF2, -d, -n.x(), -n.y(), -n.z()));
            //FTOF 18
            P = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1B).point().toVector3D();
            n = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1B).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.FTOF, DetectorLayer.FTOF1B, -d, -n.x(), -n.y(), -n.z()));
            //FTOF 1A
            P = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1A).point().toVector3D();
            n = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1A).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.FTOF, DetectorLayer.FTOF1A, -d, -n.x(), -n.y(), -n.z()));
            //LTCC
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.LTCC,1, Constants.LTCCPLANE, -n.x(), -n.y(), -n.z())); 
            //PCAL
            int superLayer = (int) ((DetectorLayer.PCAL_V-1)/3);
            int localLayer = DetectorLayer.PCAL_Z+1;
            P = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().point().toVector3D();
            Vector3D P1 = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getComponent(1).getMidpoint().toVector3D();
            n = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().normal();
            d = P.dot(n);
//            LOGGER.log(Level.FINE, "PCAL " + d + " " + P1.dot(n));
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.ECAL, DetectorLayer.PCAL_U, d, n.x(), n.y(), n.z())); 
            //ECin
            superLayer = (int) ((DetectorLayer.EC_INNER_V-1)/3);
            localLayer = DetectorLayer.EC_INNER_Z+1;
            P = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().point().toVector3D();
            P1 = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getComponent(1).getMidpoint().toVector3D();
            n = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().normal();
            d = P.dot(n);
//            LOGGER.log(Level.FINE, "ECin " + d + " " + P1.dot(n));
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.ECAL, DetectorLayer.EC_INNER_U, d, n.x(), n.y(), n.z())); 
            //ECout
            superLayer = (int) ((DetectorLayer.EC_OUTER_V-1)/3);
            localLayer = DetectorLayer.EC_OUTER_Z+1;
            P = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().point().toVector3D();
            P1 = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getComponent(1).getMidpoint().toVector3D();
            n = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().normal();
            d = P.dot(n);
//            LOGGER.log(Level.FINE, "ECout " + d + " " + P1.dot(n));
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.ECAL, DetectorLayer.EC_OUTER_U, d, n.x(), n.y(), n.z())); 
        }
    }
//    private Point3D RotateFromTSCtoLabC(double X, double Y, double Z, int sector) {
//        double rzs = -X * Math.sin(Math.toRadians(25.)) + Z * Math.cos(Math.toRadians(25.));
//        double rxs = X * Math.cos(Math.toRadians(25.)) + Z * Math.sin(Math.toRadians(25.));
//
//        double rx = rxs * Math.cos((sector - 1) * Math.toRadians(60.)) - Y * Math.sin((sector - 1) * Math.toRadians(60.));
//        double ry = rxs * Math.sin((sector - 1) * Math.toRadians(60.)) + Y * Math.cos((sector - 1) * Math.toRadians(60.));
//
//        return new Point3D(rx,ry,rzs);
//    }

    public void checkDCGeometry(DCGeant4Factory dcDetector) throws FileNotFoundException {
        int is = 0;
        PrintWriter pw = new PrintWriter(new File("/Users/ziegler/WireEndPoints.txt"));

        pw.printf("superlayer"+"   "+"layer"+"   "+"wire"+"   "+"xL"+"   "+"yL"+"   "+
                            "xR"+"   "+"yR"+"   "+"z"
                            );
        for(int isup =0; isup<6; isup++) {
            for(int il =5; il<6; il++) {
                for(int ic =0; ic<112; ic++) { // include only layer 6
                    double z = dcDetector.getWireMidpoint(is, isup, il, ic).z;
                    double xL = dcDetector.getWireLeftend(is, isup, il, ic).x;
                    double xR = dcDetector.getWireRightend(is, isup, il, ic).x;
                    double yL = dcDetector.getWireLeftend(is, isup, il, ic).y;
                    double yR = dcDetector.getWireRightend(is, isup, il, ic).y;
                    pw.printf("%d\t %d\t %d\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\t\n", (isup+1),(il+1),(ic+1),xL,yL,xR,yR,z
                            );
                }
            }
        }
    }


}
