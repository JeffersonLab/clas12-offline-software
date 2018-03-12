package org.jlab.rec.dc.trajectory;

import cnuphys.magfield.MagneticFields;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.FastMath;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.utils.CLASResources;


import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.geant4.v2.PCALGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.geometry.prim.Line3d;

public class TrackDictionaryMaker {

    private static int count(List<Integer> Wi) {
        int count = 0;
        for(int i =0; i< Wi.size(); i++) {
            if(Wi.get(i)>0)
                count++;
        }
        return count;
    }

    public TrackDictionaryMaker() {
        // TODO Auto-generated constructor stub
    }
/*
    public Point3D rotateToTiltedCoordSys(Point3D labFramePars) {
        double rz = labFramePars.x() * Math.sin(Math.toRadians(25.)) + labFramePars.z() * Math.cos(Math.toRadians(25.));
        double rx = labFramePars.x() * Math.cos(Math.toRadians(25.)) - labFramePars.z() * Math.sin(Math.toRadians(25.));
        return new Point3D(rx, labFramePars.y(), rz);
    }

    
    public Point3D getCoordsInSector(double X, double Y, double Z) {
        double rz = -X * Math.sin(Math.toRadians(25.)) + Z * Math.cos(Math.toRadians(25.));
        double rx = X * Math.cos(Math.toRadians(25.)) + Z * Math.sin(Math.toRadians(25.));

        return new Point3D(rx, Y, rz);
    }
    */
    public static Vector3d rotateToSectorCoordSys(double x, double y, double z) {
        double rz = -x * Math.sin(Math.toRadians(25.)) + z * Math.cos(Math.toRadians(25.));
        double rx = x * Math.cos(Math.toRadians(25.)) + z * Math.sin(Math.toRadians(25.));
        return new Vector3d(rx, y, rz);
    }
     public Point3D rotateToTiltedCoordSys(Point3D labFramePars){
        double[] XinSec = new double[3];
        double[] XinTiltSec = new double[3];

        int sector = this.getSector(labFramePars.x(), labFramePars.y(), labFramePars.z());

        if ((sector < 1) || (sector > 6)) {
            return new Point3D(0,0,0);
        }
        if (sector == 1) {
            XinSec[0] = labFramePars.x();
            XinSec[1] = labFramePars.y();
        } else {

            double midPlanePhi = Math.toRadians(60 * (sector - 1));
            double cosPhi = Math.cos(midPlanePhi);
            double sinPhi = Math.sin(midPlanePhi);
            XinSec[0] = cosPhi * labFramePars.x() + sinPhi * labFramePars.y();
            XinSec[1] = -sinPhi * labFramePars.x() + cosPhi * labFramePars.y();
        }

        //z coordinates are the same
        XinSec[2] = labFramePars.z();

        // rotate in tilted sector
        XinTiltSec[2] = XinSec[0] * Math.sin(Math.toRadians(25.)) + XinSec[2] * Math.cos(Math.toRadians(25.));
        XinTiltSec[0] = XinSec[0] * Math.cos(Math.toRadians(25.)) - XinSec[2] * Math.sin(Math.toRadians(25.));
        XinTiltSec[1] = XinSec[1];

        return new Point3D(XinTiltSec[0],XinTiltSec[1],XinTiltSec[2]);
    }

    private int getSector(double x, double y, double z) {
        double phi = Math.toDegrees(FastMath.atan2(y, x));
        double ang = phi + 30;
        while (ang < 0) {
            ang += 360;
        }
        int sector = 1 + (int) (ang / 60.);

        if (sector == 7) {
            sector = 6;
        }

        if ((sector < 1) || (sector > 6)) {
            System.err.println("Track sector not found....");
        }
        return sector;
    }
    public DCTDC ProcessTrack(int q, double px, double py, double pz, double vx, double vy, double vz, DCGeant4Factory dcDetector, TrackDictionaryMaker tw, DCSwimmer sw) {
        
        double[] swimVal = new double[8];
       
        sw.SetSwimParameters(vx, vy, vz, px, py, pz, q);
        swimVal = sw.SwimToPlaneLab(175.);

        //Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(px, py, pz));
        Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(swimVal[3], swimVal[4], swimVal[5]));
        Point3D rotatedX = tw.rotateToTiltedCoordSys(new Point3D(swimVal[0], swimVal[1], swimVal[2]));
        int sector = this.getSector(swimVal[0], swimVal[1], swimVal[2]);
//System.out.println(" sector in TrackDictionary "+sector);
        List<Integer> Wi = new ArrayList<Integer>();
        List<Integer> Di = new ArrayList<Integer>();
        int index=0;
        DCTDC DCtdc = new DCTDC();
        for (int sl = 0; sl < 6; sl++) {
            for (int l = 0; l < 6; l++) {
                Wi.clear();
                Di.clear();
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                swimtoLayer(l, sl, Wi, Di, dcDetector, sw);             
                for(int i=0; i<Wi.size(); i++) {
                    DCtdc.sector.add(index, (int) sector);
                    DCtdc.layer.add(index, (int) (l+1));
                    DCtdc.superlayer.add(index, (int) (sl+1));
                    DCtdc.component.add(index, (int) Wi.get(i));
                    DCtdc.TDC.add(index, (int) Di.get(i));
                    index++;
                }
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
            }
        }
        
        return DCtdc;
    }

    private static void addAdjacentHits(int sl, int l, int i, List<Integer> Wi, List<Integer> Di, DCGeant4Factory dcDetector, double wMax, double tx, double ty, double tz) {
        eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sl, l, i);
        eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sl, l, i);
        Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
        double min = wl.distance(new Point3D(tx, ty, tz)).length();
        if(min<wMax*1.05) {
            Wi.add(i + 1); //System.out.println("min "+min); ? one strip off
            Di.add((int)min);
        }
    }
    public class DCTDC  {
        public List<Integer> sector = new ArrayList<Integer>();
        public List<Integer> superlayer = new ArrayList<Integer>();
        public List<Integer> layer = new ArrayList<Integer>();
        public List<Integer> component = new ArrayList<Integer>();
        public List<Integer> TDC = new ArrayList<Integer>();
        
    }
    public static void ProcessTracks(PrintWriter pw, DCGeant4Factory dcDetector, FTOFGeant4Factory ftofDetector, PCALGeant4Factory pcalDetector, TrackDictionaryMaker tw, DCSwimmer sw) {
        double[] swimVal = new double[8];
        for(int i = 0; i < 2; i++) {
            int q = (int) Math.pow(-1, i);
            double invPMin = 1. / 1.;
            double invPMax = 1./0.500;
            double invPRange = invPMax - invPMin;
            double invPBinSize = 0.05;
            int nBinsinvP = (int) (invPRange / invPBinSize) + 1;

            double phiMin = -30;
            double phiMax = 30;
            double phiRange = phiMax - phiMin;
            double phiBinSize = 2.;
            int nBinsPhi = (int) (phiRange / phiBinSize) + 1;
            double thetaMin = 5;
            double thetaMax = 41;
            double thetaRange = thetaMax - thetaMin;
            double thetaBinSize = 0.5;
            int nBinsTheta = (int) (thetaRange / thetaBinSize) + 1;

            for (int nip = 0; nip < nBinsinvP; nip++) {
                double invP = invPMin + (double) nip * invPBinSize;
                double p = 1. / invP;

                for (int nth = 0; nth < nBinsTheta; nth++) {

                    double theta = thetaMin + (double) nth * thetaBinSize;

                    for (int nph = 0; nph < nBinsPhi; nph++) {

                        double phi = phiMin + (double) nph * phiBinSize; 
                        double px = p * Math.cos(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
                        double py = p * Math.sin(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
                        double pz = p * Math.cos(Math.toRadians(theta));
                        sw.SetSwimParameters(0, 0, 0, px, py, pz, q);
                        swimVal = sw.SwimToPlaneLab(175.);
                        

                        //Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(px, py, pz));
                        Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(swimVal[3], swimVal[4], swimVal[5]));
                        Point3D rotatedX = tw.rotateToTiltedCoordSys(new Point3D(swimVal[0], swimVal[1], swimVal[2]));

                        
                        List<Integer> Wi = new ArrayList<Integer>();
                        List<Integer> Wf = new ArrayList<Integer>();

                        for (int sl = 0; sl < 6; sl++) {
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            swimtoLayer(0, sl, Wi, dcDetector, sw);             
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            swimtoLayer(5, sl, Wf, dcDetector, sw);

                            /*
                            double[] trk = sw.SwimToPlane(dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(0).getMidpoint().z());
                            Line3D trkLine = new Line3D(new Point3D(trk[0], trk[1], trk[2]), new Vector3D(trk[3], trk[4], trk[5]));
                            double wMax = Math.abs(dcDetector.getSector(0).getSuperlayer(sl).getLayer(0).getComponent(0).getMidpoint().x()
                                    - dcDetector.getSector(0).getSuperlayer(sl).getLayer(0).getComponent(1).getMidpoint().x()) / 2.;

                            double min = 1000;
                            int w = -1;
                            for (int i = 0; i < 112; i++) {
                                Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
                                if (trkLine.distance(wl).length() < min) {
                                    min = trkLine.distance(wl).length();
                                    w = i;
                                }
                            }

                            if (min < wMax) {
                                Wi.add(w + 1);
                            }
                            */
                        }
                        double[] trkTOF = sw.SwimToPlane(668.1);
                        double[] trkPCAL = sw.SwimToPlane(698.8);


                        Line3d trkLine = new Line3d(rotateToSectorCoordSys(trkTOF[0],trkTOF[1],trkTOF[2]), rotateToSectorCoordSys(trkPCAL[0], trkPCAL[1], trkPCAL[2])) ;

                        List<DetHit> hits = ftofDetector.getIntersections(trkLine);
                        List<DetHit> hits2 = pcalDetector.getIntersections(trkLine);

                        if(hits.size()==0) {
                            for(int ii =0; ii<3; ii++)
                                trkTOF[ii]=0;
                        }
                        if(hits2.size()==0) {
                            for(int ii =0; ii<3; ii++)
                                trkPCAL[ii]=0;
                        }
                        if (count(Wi) >4) {
                            pw.printf("%d\t%.1f\t %.1f\t %.1f\t %d\t %d\t %d\t %d\t %d\t %d\t %d\t %d\t %d\t %d\t %d\t %d\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\t\n", q, p, theta, phi, Wi.get(0), Wf.get(0), Wi.get(1), Wf.get(1), Wi.get(2), Wf.get(2),Wi.get(3), Wf.get(3), Wi.get(4), Wf.get(4), Wi.get(5), Wf.get(5), trkTOF[0], trkTOF[1], trkTOF[2], trkPCAL[0], trkPCAL[1], trkPCAL[2]);
                            //System.out.printf("%d\t\t%.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t\n", q, p, theta, phi, Wi.get(0), Wf.get(0), Wi.get(1), Wf.get(1), Wi.get(2), Wf.get(2),Wi.get(3), Wf.get(3), Wi.get(4), Wf.get(4), Wi.get(5), Wf.get(5), trkTOF[0], trkTOF[1], trkTOF[2], trkPCAL[0], trkPCAL[1], trkPCAL[2]);
                            
                            //System.out.printf("%d\t\t %.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t\n",q, p, theta, phi, Wi.get(0), Wi.get(1), Wi.get(2), Wi.get(3), Wi.get(4), Wi.get(5));

                        }

                    }
                }
            }
        }
    }

    public static void ProcessCosmics(PrintWriter pw, DCGeant4Factory dcDetector, TrackDictionaryMaker tw, DCSwimmer sw) {

        double XMin = 35.;
        double XMax = 350;
        double XRange = XMax - XMin;
        double XBinSize = 1;
        int nBinsX = (int) (XRange / XBinSize) + 1;

        double ZMin = 175.;
        double ZMax = 175;
        double ZRange = ZMax - ZMin;
        double ZBinSize = 1;
        int nBinsZ = (int) (ZRange / ZBinSize) + 1;

        double phiMin = -30;
        double phiMax = 30;
        double phiRange = phiMax - phiMin;
        double phiBinSize = 2.;
        int nBinsPhi = (int) (phiRange / phiBinSize) + 1;

        double thetaMin = -20;
        double thetaMax = 60;
        double thetaRange = thetaMax - thetaMin;
        double thetaBinSize = 0.5;
        int nBinsTheta = (int) (thetaRange / thetaBinSize) + 1;

        for (int nx = 0; nx < nBinsX; nx++) {
            double x = XMin + (double) nx * XBinSize;
            for (int nz = 0; nz < nBinsZ; nz++) {
                double z = ZMin + (double) nz * ZBinSize;
                double y = 0;

                double p = 100.;

                for (int nth = 0; nth < nBinsTheta; nth++) {

                    double theta = thetaMin + (double) nth * thetaBinSize;

                    for (int nph = 0; nph < nBinsPhi; nph++) {

                        double phi = phiMin + (double) nph * phiBinSize;
                        double px = p * Math.cos(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
                        double py = p * Math.sin(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
                        double pz = p * Math.cos(Math.toRadians(theta));

                        Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(px, py, pz));

                        Point3D rotatedX = tw.rotateToTiltedCoordSys(new Point3D(x, y, z));

                        sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), 1);

                        List<Integer> W = new ArrayList<Integer>();

                        for (int sl = 0; sl < 6; sl++) {

                            double[] trk = sw.SwimToPlane(dcDetector.getWireMidpoint(sl, 0, 0).z);
                            double norm = Math.sqrt(trk[3] * trk[3] + trk[4] * trk[4] + trk[5] * trk[5]);
                            Line3D trkLine = new Line3D(new Point3D(trk[0], trk[1], trk[2]), new Vector3D(trk[3] / norm, trk[4] / norm, trk[5] / norm));
                            double wMax = Math.abs(dcDetector.getWireMidpoint(sl, 0, 0).x
                                    - dcDetector.getWireMidpoint(sl, 0, 1).x) / 2.;

                            double min = 1000;
                            int w = -1;
                            for (int i = 0; i < 112; i++) {
                                eu.mihosoft.vrl.v3d.Vector3d dir3d = dcDetector.getWireDirection(sl, 0, i);
                                eu.mihosoft.vrl.v3d.Vector3d p3d = dcDetector.getWireMidpoint(sl, 0, i);
                                Line3D wl = new Line3D(new Point3D(p3d.x - 100 * dir3d.x, p3d.y - 100 * dir3d.y, p3d.z - 100 * dir3d.z), new Point3D(p3d.x + 100 * dir3d.x, p3d.y + 100 * dir3d.y, p3d.z + 100 * dir3d.z));
                                //Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
                                if (trkLine.distance(wl).length() < min) {
                                    min = trkLine.distance(wl).length();
                                    w = i;
                                }
                            }

                            if (min < wMax) {
                                W.add(w + 1 );
                            } else {
                                W.add(0);
                            }

                        }

                    }
                }
            }
        }
    }
    
    public static void swimtoLayer(int l, int sl, List<Integer> Wi, List<Integer> Di, DCGeant4Factory dcDetector,  DCSwimmer sw) {
        //double[] trk = sw.SwimToPlane(dcDetector.getSector(0).getSuperlayer(sl).getLayer(l).getComponent(0).getMidpoint().z());
        double[] trk = sw.SwimToPlane(dcDetector.getWireMidpoint(sl, l, 0).z); 
       
       // Line3D trkLine = new Line3D(new Point3D(trk[0], trk[1], trk[2]), new Vector3D(trk[3], trk[4], trk[5]).asUnit());
        double wMax = Math.abs(dcDetector.getWireMidpoint(sl, 0, 0).x
                - dcDetector.getWireMidpoint(sl, 0, 1).x) / 2.;

        double min = 1000;
        int w = -1;
        for (int i = 0; i < 112; i++) {
            eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sl, l, i);
            eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sl, l, i);
            Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
            //Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
           
            if (wl.distance(new Point3D(trk[0], trk[1], trk[2])).length() < min) { 
                min = wl.distance(new Point3D(trk[0], trk[1], trk[2])).length();
                w = i; //System.out.println(" min "+min+" wire "+(i+1)+" sl "+sl+" l "+l+" trk "+trk[0]+", "+trk[1]+", "+trk[2]+" mp "+dcDetector.getWireMidpoint(sl, l, i)+" : "+dcDetector.getWireMidpoint(sl, l, 0).z);
            } 
        }

        if (min < wMax*1.01) {
            Wi.add(w + 1); //System.out.println("min "+min);
            Di.add((int)min);
            addAdjacentHits(sl, l, w+1, Wi, Di, dcDetector, wMax, trk[0], trk[1], trk[2]);
            addAdjacentHits(sl, l, w-1, Wi, Di, dcDetector, wMax, trk[0], trk[1], trk[2]);
                
        } else {
            Wi.add(0);
            Di.add((int)10000);
        }
    }
    public static void swimtoLayer(int l, int sl, List<Integer> Wi, DCGeant4Factory dcDetector,  DCSwimmer sw) {
        //double[] trk = sw.SwimToPlane(dcDetector.getSector(0).getSuperlayer(sl).getLayer(l).getComponent(0).getMidpoint().z());
        double[] trk = sw.SwimToPlane(dcDetector.getWireMidpoint(sl, l, 0).z); 
       
       // Line3D trkLine = new Line3D(new Point3D(trk[0], trk[1], trk[2]), new Vector3D(trk[3], trk[4], trk[5]).asUnit());
        double wMax = Math.abs(dcDetector.getWireMidpoint(sl, 0, 0).x
                - dcDetector.getWireMidpoint(sl, 0, 1).x) / 2.;

        double min = 1000;
        int w = -1;
        for (int i = 0; i < 112; i++) {
            eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sl, l, i);
            eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sl, l, i);
            Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
            //Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
           
            if (wl.distance(new Point3D(trk[0], trk[1], trk[2])).length() < min) { 
                min = wl.distance(new Point3D(trk[0], trk[1], trk[2])).length();
                w = i; //System.out.println(" min "+min+" wire "+(i+1)+" sl "+sl+" l "+l+" trk "+trk[0]+", "+trk[1]+", "+trk[2]+" mp "+dcDetector.getWireMidpoint(sl, l, i)+" : "+dcDetector.getWireMidpoint(sl, l, 0).z);
            } 
        }

        if (min < wMax*1.01) {
            Wi.add(w + 1); //System.out.println("min "+min);
        } else {
            Wi.add(0);
        }
    }
    public static void main(String arg[]) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(new File("/Users/ziegler/Workdir/Files/Dictionaries/DC/DCTracks.txt"));
        // PrintWriter pw = new PrintWriter(new File("/Users/ziegler/DC/DCdictionaryCosmics.txt"));

        TrackDictionaryMaker tw = new TrackDictionaryMaker();
        
        DCGeant4Factory dcDetector = new DCGeant4Factory(GeometryFactory.getConstants(DetectorType.DC, 11, "default"), DCGeant4Factory.MINISTAGGERON);
       
        FTOFGeant4Factory ftofDetector = new FTOFGeant4Factory(GeometryFactory.getConstants(DetectorType.FTOF, 11, "default"));
        
        PCALGeant4Factory pcalDetector = new PCALGeant4Factory(GeometryFactory.getConstants(DetectorType.ECAL, 11, "default"));

       // 

        //will read mag field assuming we are in a 
        //location relative to clasJLib. This will
        //have to be modified as appropriate.

        String clasDictionaryPath = CLASResources.getResourcePath("etc");

        String torusFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-torus.dat";
        String solenoidFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-solenoid.dat";
        
        MagneticFields.getInstance().initializeMagneticFields();
        MagneticFields.getInstance().getTorus().setScaleFactor(-1.0);
        MagneticFields.getInstance().getSolenoid().setScaleFactor(1.0);
        System.out.println("Rotated Composite "+MagneticFields.getInstance().getRotatedCompositeField().getName());
       
        System.out.println(" version "+MagneticFields.getInstance().getVersion());
        
        DCSwimmer sw = new DCSwimmer();
        //ProcessCosmics(pw, dcDetector, tw, sw);
        ProcessTracks( pw,  dcDetector, ftofDetector, pcalDetector, tw,  sw);

        pw.close();

        System.out.println(" End ");
    }

    
}
