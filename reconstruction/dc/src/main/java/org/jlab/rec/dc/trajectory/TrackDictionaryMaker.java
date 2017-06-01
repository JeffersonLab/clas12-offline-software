package org.jlab.rec.dc.trajectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class TrackDictionaryMaker {

    public TrackDictionaryMaker() {
        // TODO Auto-generated constructor stub
    }

    public Point3D rotateToTiltedCoordSys(Point3D labFramePars) {
        double rz = labFramePars.x() * Math.sin(Math.toRadians(25.)) + labFramePars.z() * Math.cos(Math.toRadians(25.));
        double rx = labFramePars.x() * Math.cos(Math.toRadians(25.)) - labFramePars.z() * Math.sin(Math.toRadians(25.));
        return new Point3D(rx, labFramePars.y(), rz);
    }

    public static void ProcessTracks(PrintWriter pw, DCDetector dcDetector, TrackDictionaryMaker tw, DCSwimmer sw) {

        double invPMin = 1. / 5.;
        double invPMax = 1.;
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

                    Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(px, py, pz));

                    sw.SetSwimParameters(0, 0, 0, rotatedP.x(), rotatedP.y(), rotatedP.z(), -1);

                    List<Integer> W = new ArrayList<Integer>();

                    for (int sl = 0; sl < 6; sl++) {

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
                            W.add(w + 1);
                        }

                    }
                    if (W.size() == 6) {
                        pw.printf("%.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t\n", p, theta, phi, W.get(0), W.get(1), W.get(2), W.get(3), W.get(4), W.get(5));
                        //System.out.printf("%.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t\n", p, theta, phi, W.get(0), W.get(1), W.get(2), W.get(3), W.get(4), W.get(5));

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
                                W.add(w + 1);
                            }

                        }

                        if (W.size() == 6) {
                            //pw.printf("%.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t \n", x, z, theta, phi, W.get(0), W.get(1));
                            //System.out.printf("%.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t \n", x, z, theta, phi, W.get(0), W.get(1));
                            pw.printf("%.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t\n", z, x, theta, phi, W.get(0), W.get(1), W.get(2), W.get(3), W.get(4), W.get(5));

                        }

                    }
                }
            }
        }
    }

    public static void main(String arg[]) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(new File("/Users/ziegler/DC/DCdictionaryCosmics.txt"));

        TrackDictionaryMaker tw = new TrackDictionaryMaker();
        ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, "default");
        DCGeant4Factory dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON);

        DCSwimmer sw = new DCSwimmer();
        DCSwimmer.getMagneticFields();
        DCSwimmer.setMagneticFieldsScales(0, 0);

        ProcessCosmics(pw, dcDetector, tw, sw);

        pw.close();

        System.out.println(" End ");
    }
}
