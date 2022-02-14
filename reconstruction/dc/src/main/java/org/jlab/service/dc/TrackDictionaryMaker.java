package org.jlab.service.dc;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.util.FastMath;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;

import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Path3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.dc.Constants;

import org.jlab.utils.options.OptionParser;

public class TrackDictionaryMaker extends DCEngine{

    private static int count(List<Integer> Wi) {
        int count = 0;
        for(int i =0; i< Wi.size(); i++) {
            if(Wi.get(i)>0)
                count++;
        }
        return count;
    }

    public TrackDictionaryMaker(){
        super("TDM");
    }
    @Override
    public boolean init() {
        MagFieldsEngine mf = new MagFieldsEngine();
        mf.initializeMagneticFields();
        super.init();
        return true;
    }
    public void processFile(float torScale, float solScale, int charge, float pBinSize, float phiMin, float phiMax, float vz) {
        Swimmer.setMagneticFieldsScales(torScale, solScale, -1.9);
        Swim sw = new Swim();
        PrintWriter pw = null;
        try {
            LOGGER.log(Level.INFO, " MAKING ROADS for "+"TracksDicTorus"+String.valueOf(torScale)+"Solenoid"+String.valueOf(solScale)
                    +"Charge"+String.valueOf(charge)+"InvPBinSizeiGeV"+String.valueOf(pBinSize)
                    +"PhiMinDeg" +String.valueOf(phiMin)+"PhiMaxDeg" +String.valueOf(phiMax)
                    +"VzCm" +String.valueOf(vz));
            pw = new PrintWriter("TracksDicTorus"+String.valueOf(torScale)+"Solenoid"+String.valueOf(solScale)
                    +"Charge"+String.valueOf(charge)+"InvPBinSizeiGeV"+String.valueOf(pBinSize)
                    +"PhiMinDeg" +String.valueOf(phiMin)+"PhiMaxDeg" +String.valueOf(phiMax)
                    +"VzCm" +String.valueOf(vz)+".txt");
            this.ProcessTracks(pw, Constants.getInstance().dcDetector, Constants.getInstance().ftofDetector, 
                                   Constants.getInstance().ecalDetector, sw, charge, pBinSize, phiMin, phiMax, vz);
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrackDictionaryMaker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //pwi.close();
            //pwo.close();
        }
    }
    
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
    public DCTDC ProcessTrack(int q, double px, double py, double pz, double vx, double vy, double vz, 
            DCGeant4Factory dcDetector, Swim sw) {
        
        double[] swimVal = new double[8];
       
        sw.SetSwimParameters(vx, vy, vz, px, py, pz, q);
        swimVal = sw.SwimToPlaneLab(175.);

        //Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(px, py, pz));
        Point3D rotatedP = this.rotateToTiltedCoordSys(new Point3D(swimVal[3], swimVal[4], swimVal[5]));
        Point3D rotatedX = this.rotateToTiltedCoordSys(new Point3D(swimVal[0], swimVal[1], swimVal[2]));
        int sector = this.getSector(swimVal[0], swimVal[1], swimVal[2]);
//LOGGER.log(Level.FINE, " sector in TrackDictionary "+sector);
        List<Integer> Wi = new ArrayList<>();
        List<Integer> Di = new ArrayList<>();
        int index=0;
        DCTDC DCtdc = new DCTDC();
        for (int sl = 0; sl < 6; sl++) {
            for (int l = 0; l < 6; l++) {
                Wi.clear();
                Di.clear();
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                swimtoLayer(sector, l, sl, Wi, Di, dcDetector, sw);             
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

    private static void addAdjacentHits(int sector, int sl, int l, int i, List<Integer> Wi, List<Integer> Di, DCGeant4Factory dcDetector, double wMax, double tx, double ty, double tz) {
        eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sector-1, sl, l, i);
        eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sector-1, sl, l, i);
        Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
        double min = wl.distance(new Point3D(tx, ty, tz)).length();
        if(min<wMax*1.05) {
            Wi.add(i + 1); //LOGGER.log(Level.FINE, "min "+min); ? one strip off
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
    private List<Integer> Wl1 = new ArrayList<>();
    private List<Integer> Wl2 = new ArrayList<>();
    private List<Integer> Wl3 = new ArrayList<>();
    private List<Integer> Wl4 = new ArrayList<>();
    private List<Integer> Wl5 = new ArrayList<>();
    private List<Integer> Wl6 = new ArrayList<>();
    private String entry;
    
    public void ProcessTracks(PrintWriter pw,DCGeant4Factory dcDetector, FTOFGeant4Factory ftofDetector, Detector ecalDetector, Swim sw, int q, float pBinSize, float PhiMin, float PhiMax, float Vz) {
        double[] swimVal = new double[8];
        //for(int i = 0; i < 2; i++) {
            //int q = (int) Math.pow(-1, i);
            if(Math.abs(q)==1) {
            double invPMin = 1. / 10.;
            double invPMax = 1./0.500;
            double invPRange = invPMax - invPMin;
            double invPBinSize = (double) pBinSize;
            int nBinsinvP = (int) (invPRange / invPBinSize) + 1;

            double phiMin = (double) PhiMin;
            double phiMax = (double) PhiMax;
            double phiRange = phiMax - phiMin;
            double phiBinSize = 2.;
            int nBinsPhi = (int) (phiRange / phiBinSize) + 1;
            double thetaMin = 5;
            double thetaMax = 41;
            double thetaRange = thetaMax - thetaMin;
            double thetaBinSize = 0.5;
            int nBinsTheta = (int) (thetaRange / thetaBinSize) + 1;
            //double vzMin = -2.5;
            //double vzMax = 2.5;
            //double vzRange = vzMax - vzMin;
            //double vzBinSize = 0.1;
            //int nBinsVz = (int) (vzRange / vzBinSize) + 1;
            double vz = (double) Vz;    
            //for (int nvz = 0; nvz < nBinsVz; nvz++) {
            //    double vz = vzMin + (double) nvz * vzBinSize; 
            
                
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
                        sw.SetSwimParameters(0, 0, vz, px, py, pz, q);
                        swimVal = sw.SwimToPlaneLab(175.);
                        int sector = this.getSector(swimVal[0], swimVal[1], swimVal[2]);

                        //Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(px, py, pz));
                        Point3D rotatedP = this.rotateToTiltedCoordSys(new Point3D(swimVal[3], swimVal[4], swimVal[5]));
                        Point3D rotatedX = this.rotateToTiltedCoordSys(new Point3D(swimVal[0], swimVal[1], swimVal[2]));

                        //List<Integer> Wi = new ArrayList<Integer>();
                        //List<Integer> Wf = new ArrayList<Integer>();
                        this.Wl1.clear();
                        this.Wl2.clear();
                        this.Wl3.clear();
                        this.Wl4.clear();
                        this.Wl5.clear();
                        this.Wl6.clear();
                        this.entry = "";
                        for (int sl = 0; sl < 6; sl++) {
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            TrackDictionaryMaker.swimtoLayer(sector, 0, sl, Wl1, dcDetector, sw);        
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            TrackDictionaryMaker.swimtoLayer(sector, 1, sl, Wl2, dcDetector, sw);        
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            TrackDictionaryMaker.swimtoLayer(sector, 2, sl, Wl3, dcDetector, sw);        
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            TrackDictionaryMaker.swimtoLayer(sector, 3, sl, Wl4, dcDetector, sw);        
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            TrackDictionaryMaker.swimtoLayer(sector, 4, sl, Wl5, dcDetector, sw);        
                            sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                            TrackDictionaryMaker.swimtoLayer(sector, 5, sl, Wl6, dcDetector, sw);

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
                        double[] trkTOF = sw.SwimToPlaneTiltSecSys(sector, 668.1);
                        double[] trkPCAL = sw.SwimToPlaneTiltSecSys(sector, 800.0);


                        Line3d trkLine = new Line3d(rotateToSectorCoordSys(trkTOF[0],trkTOF[1],trkTOF[2]), rotateToSectorCoordSys(trkPCAL[0], trkPCAL[1], trkPCAL[2])) ;

                        List<DetHit> hits  = ftofDetector.getIntersections(trkLine);
                        Vector3d tof3 = rotateToSectorCoordSys(trkTOF[0],trkTOF[1],trkTOF[2]);
                        Vector3d cal3 = rotateToSectorCoordSys(trkPCAL[0], trkPCAL[1], trkPCAL[2]);
                        Path3D path = new Path3D(new Point3D(tof3.x, tof3.y, tof3.z), new Point3D(cal3.x,cal3.y,cal3.z));
                        List<DetectorHit> hits3 = ecalDetector.getHits(path);
            

                        int paddle1b = 0; int paddle2 = 0; int pcalU =0; int pcalV=0; int pcalW=0; int htcc=0;
                        if (hits != null && hits.size() > 0) {
                            for (DetHit hit : hits) {
                                FTOFDetHit fhit = new FTOFDetHit(hit);
                                if(fhit.getLayer()==2)
                                    paddle1b = fhit.getPaddle();
                                if(fhit.getLayer()==3)
                                    paddle2 = fhit.getPaddle();
                            }
                        }
                        if (hits3 != null && hits3.size() > 0) {
                            for (DetectorHit hit3 : hits3) {
                                if(hit3.getSuperlayerId()+1==1) {
                                    if(hit3.getLayerId()+1==1 && pcalU==0) 
                                        pcalU = hit3.getComponentId()+1;
                                    if(hit3.getLayerId()+1==2 && pcalV==0) 
                                        pcalV = hit3.getComponentId()+1;
                                    if(hit3.getLayerId()+1==3 && pcalW==0) 
                                        pcalW = hit3.getComponentId()+1;
                                }
                            }
                        }
                        

                        if (count(Wl3) >4) {
                            if(String.valueOf(q)+
                                    String.valueOf(Wl1.get(0))+ String.valueOf(Wl2.get(0))+ String.valueOf(Wl3.get(0))+ String.valueOf(Wl4.get(0))+ String.valueOf(Wl5.get(0))+ String.valueOf(Wl6.get(0))+ 
                                    String.valueOf(Wl1.get(1))+ String.valueOf(Wl2.get(1))+ String.valueOf(Wl3.get(1))+ String.valueOf(Wl4.get(1))+ String.valueOf(Wl5.get(1))+ String.valueOf(Wl6.get(1))+ 
                                    String.valueOf(Wl1.get(2))+ String.valueOf(Wl2.get(2))+ String.valueOf(Wl3.get(2))+ String.valueOf(Wl4.get(2))+ String.valueOf(Wl5.get(2))+ String.valueOf(Wl6.get(2))+ 
                                    String.valueOf(Wl1.get(3))+ String.valueOf(Wl2.get(3))+ String.valueOf(Wl3.get(3))+ String.valueOf(Wl4.get(3))+ String.valueOf(Wl5.get(3))+ String.valueOf(Wl6.get(3))+ 
                                    String.valueOf(Wl1.get(4))+ String.valueOf(Wl2.get(4))+ String.valueOf(Wl3.get(4))+ String.valueOf(Wl4.get(4))+ String.valueOf(Wl5.get(4))+ String.valueOf(Wl6.get(4))+ 
                                    String.valueOf(Wl1.get(5))+ String.valueOf(Wl2.get(5))+ String.valueOf(Wl3.get(5))+ String.valueOf(Wl4.get(5))+ String.valueOf(Wl5.get(5))+ String.valueOf(Wl6.get(5))+
                                    String.valueOf(paddle1b)==entry)
                                continue;
                            entry = String.valueOf(q)+
                                    String.valueOf(Wl1.get(0))+ String.valueOf(Wl2.get(0))+ String.valueOf(Wl3.get(0))+ String.valueOf(Wl4.get(0))+ String.valueOf(Wl5.get(0))+ String.valueOf(Wl6.get(0))+ 
                                    String.valueOf(Wl1.get(1))+ String.valueOf(Wl2.get(1))+ String.valueOf(Wl3.get(1))+ String.valueOf(Wl4.get(1))+ String.valueOf(Wl5.get(1))+ String.valueOf(Wl6.get(1))+ 
                                    String.valueOf(Wl1.get(2))+ String.valueOf(Wl2.get(2))+ String.valueOf(Wl3.get(2))+ String.valueOf(Wl4.get(2))+ String.valueOf(Wl5.get(2))+ String.valueOf(Wl6.get(2))+ 
                                    String.valueOf(Wl1.get(3))+ String.valueOf(Wl2.get(3))+ String.valueOf(Wl3.get(3))+ String.valueOf(Wl4.get(3))+ String.valueOf(Wl5.get(3))+ String.valueOf(Wl6.get(3))+ 
                                    String.valueOf(Wl1.get(4))+ String.valueOf(Wl2.get(4))+ String.valueOf(Wl3.get(4))+ String.valueOf(Wl4.get(4))+ String.valueOf(Wl5.get(4))+ String.valueOf(Wl6.get(4))+ 
                                    String.valueOf(Wl1.get(5))+ String.valueOf(Wl2.get(5))+ String.valueOf(Wl3.get(5))+ String.valueOf(Wl4.get(5))+ String.valueOf(Wl5.get(5))+ String.valueOf(Wl6.get(5))+
                                    String.valueOf(paddle1b);
                            
                            pw.printf("%d\t%.1f\t %.1f\t %.1f\t "
                                + "%d\t %d\t %d\t %d\t %d\t %d\t "
                                + "%d\t %d\t %d\t %d\t %d\t %d\t "
                                + "%d\t %d\t %d\t %d\t %d\t %d\t "
                                + "%d\t %d\t %d\t %d\t %d\t %d\t "
                                + "%d\t %d\t %d\t %d\t %d\t %d\t "
                                + "%d\t %d\t %d\t %d\t %d\t %d\t "
                                +"%d\t %.1f\t\n",
                                //+ "%.1f\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\t\n", 
                                q, p, theta, phi,
                                Wl1.get(0), Wl2.get(0), Wl3.get(0), Wl4.get(0), Wl5.get(0), Wl6.get(0), 
                                Wl1.get(1), Wl2.get(1), Wl3.get(1), Wl4.get(1), Wl5.get(1), Wl6.get(1), 
                                Wl1.get(2), Wl2.get(2), Wl3.get(2), Wl4.get(2), Wl5.get(2), Wl6.get(2), 
                                Wl1.get(3), Wl2.get(3), Wl3.get(3), Wl4.get(3), Wl5.get(3), Wl6.get(3), 
                                Wl1.get(4), Wl2.get(4), Wl3.get(4), Wl4.get(4), Wl5.get(4), Wl6.get(4), 
                                Wl1.get(5), Wl2.get(5), Wl3.get(5), Wl4.get(5), Wl5.get(5), Wl6.get(5), 
                                //trkTOF[0], trkTOF[1], trkTOF[2], trkPCAL[0], trkPCAL[1], trkPCAL[2]);
                                paddle1b, vz);
                            
                            //System.out.printf("%d\t\t%.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t %.1f\t\t\n", q, p, theta, phi, Wi.get(0), Wf.get(0), Wi.get(1), Wf.get(1), Wi.get(2), Wf.get(2),Wi.get(3), Wf.get(3), Wi.get(4), Wf.get(4), Wi.get(5), Wf.get(5), trkTOF[0], trkTOF[1], trkTOF[2], trkPCAL[0], trkPCAL[1], trkPCAL[2]);

                            //System.out.printf("%d\t\t %.1f\t\t %.1f\t\t %.1f\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t\n",q, p, theta, phi, Wi.get(0), Wi.get(1), Wi.get(2), Wi.get(3), Wi.get(4), Wi.get(5));

                        }
                    }
                }
            }          
        }
    }

    public static void ProcessCosmics(PrintWriter pw, DCGeant4Factory dcDetector, TrackDictionaryMaker tw, Swim sw) {

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

                        List<Integer> W = new ArrayList<>();

                        for (int sl = 0; sl < 6; sl++) {

                            double[] trk = sw.SwimToPlaneTiltSecSys(1, dcDetector.getWireMidpoint(sl, 0, 0).z);
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
    
    public static void swimtoLayer(int sector, int l, int sl, List<Integer> Wi, List<Integer> Di, DCGeant4Factory dcDetector,  Swim sw) {
        //double[] trk = sw.SwimToPlane(dcDetector.getSector(0).getSuperlayer(sl).getLayer(l).getComponent(0).getMidpoint().z());
        double[] trk = sw.SwimToPlaneTiltSecSys(sector, dcDetector.getWireMidpoint(sector-1, sl, l, 0).z); 
       
       // Line3D trkLine = new Line3D(new Point3D(trk[0], trk[1], trk[2]), new Vector3D(trk[3], trk[4], trk[5]).asUnit());
        double wMax = Math.abs(dcDetector.getWireMidpoint(sector-1, sl, 0, 0).x
                - dcDetector.getWireMidpoint(sector-1, sl, 0, 1).x) / 2.;

        double min = 1000;
        int w = -1;
        for (int i = 0; i < 112; i++) {
            eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sector-1,sl, l, i);
            eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sector-1,sl, l, i);
            Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
            //Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
           
            if (wl.distance(new Point3D(trk[0], trk[1], trk[2])).length() < min) { 
                min = wl.distance(new Point3D(trk[0], trk[1], trk[2])).length();
                w = i; //LOGGER.log(Level.FINE, " min "+min+" wire "+(i+1)+" sl "+sl+" l "+l+" trk "+trk[0]+", "+trk[1]+", "+trk[2]+" mp "+dcDetector.getWireMidpoint(sl, l, i)+" : "+dcDetector.getWireMidpoint(sl, l, 0).z);
            } 
        }

        if (min < wMax*1.01) {
            Wi.add(w + 1); //LOGGER.log(Level.FINE, "min "+min);
            Di.add((int)min);
            addAdjacentHits(sector-1, sl, l, w+1, Wi, Di, dcDetector, wMax, trk[0], trk[1], trk[2]);
            addAdjacentHits(sector-1, sl, l, w-1, Wi, Di, dcDetector, wMax, trk[0], trk[1], trk[2]);
                
        } else {
            Wi.add(0);
            Di.add((int)10000);
        }
    }
    public static void swimtoLayer(int sector, int l, int sl, List<Integer> Wi, DCGeant4Factory dcDetector,  Swim sw) {
        //double[] trk = sw.SwimToPlane(dcDetector.getSector(0).getSuperlayer(sl).getLayer(l).getComponent(0).getMidpoint().z());
        double[] trk = sw.SwimToPlaneTiltSecSys(sector, dcDetector.getWireMidpoint(sector-1, sl, l, 0).z); 
       
       // Line3D trkLine = new Line3D(new Point3D(trk[0], trk[1], trk[2]), new Vector3D(trk[3], trk[4], trk[5]).asUnit());
        double wMax = Math.abs(dcDetector.getWireMidpoint(sector-1, sl, 0, 0).x
                - dcDetector.getWireMidpoint(sector-1, sl, 0, 1).x) / 2.;

        double min = 1000;
        int w = -1;
        for (int i = 0; i < 112; i++) {
            eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sector-1, sl, l, i);
            eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sector-1, sl, l, i);
            Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
            //Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
           
            if (wl.distance(new Point3D(trk[0], trk[1], trk[2])).length() < min) { 
                min = wl.distance(new Point3D(trk[0], trk[1], trk[2])).length();
                w = i; //LOGGER.log(Level.FINE, " min "+min+" wire "+(i+1)+" sl "+sl+" l "+l+" trk "+trk[0]+", "+trk[1]+", "+trk[2]+" mp "+dcDetector.getWireMidpoint(sl, l, i)+" : "+dcDetector.getWireMidpoint(sl, l, 0).z);
            } 
        }

        if (min < wMax*1.01) {
            Wi.add(w + 1); //LOGGER.log(Level.FINE, "min "+min);
        } else {
            Wi.add(0);
        }
    }
    

    /**
     * @return the Wl1
     */
    public List<Integer> getWl1() {
        return Wl1;
    }

    /**
     * @param Wl1 the Wl1 to set
     */
    public void setWl1(List<Integer> Wl1) {
        this.Wl1 = Wl1;
    }

    /**
     * @return the Wl2
     */
    public List<Integer> getWl2() {
        return Wl2;
    }

    /**
     * @param Wl2 the Wl2 to set
     */
    public void setWl2(List<Integer> Wl2) {
        this.Wl2 = Wl2;
    }

    /**
     * @return the Wl3
     */
    public List<Integer> getWl3() {
        return Wl3;
    }

    /**
     * @param Wl3 the Wl3 to set
     */
    public void setWl3(List<Integer> Wl3) {
        this.Wl3 = Wl3;
    }

    /**
     * @return the Wl4
     */
    public List<Integer> getWl4() {
        return Wl4;
    }

    /**
     * @param Wl4 the Wl4 to set
     */
    public void setWl4(List<Integer> Wl4) {
        this.Wl4 = Wl4;
    }

    /**
     * @return the Wl5
     */
    public List<Integer> getWl5() {
        return Wl5;
    }

    /**
     * @param Wl5 the Wl5 to set
     */
    public void setWl5(List<Integer> Wl5) {
        this.Wl5 = Wl5;
    }

    /**
     * @return the Wl6
     */
    public List<Integer> getWl6() {
        return Wl6;
    }

    /**
     * @param Wl6 the Wl6 to set
     */
    public void setWl6(List<Integer> Wl6) {
        this.Wl6 = Wl6;
    }

    private void resetGeom(String geomDBVar) {
        ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, Optional.ofNullable(geomDBVar).orElse("default"));
        Constants.getInstance().dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON, DCGeant4Factory.ENDPLATESBOWON);
        
        for(int l=0; l<6; l++) {
            Constants.getInstance().wpdist[l] = provider.getDouble("/geometry/dc/superlayer/wpdist", l);
            LOGGER.log(Level.INFO, "****************** WPDIST READ *********FROM RELOADED "+geomDBVar+"**** VARIATION ****** "+provider.getDouble("/geometry/dc/superlayer/wpdist", l));
        }
        
    }
    public static void main(String[] args) {
        TrackDictionaryMaker tm = new TrackDictionaryMaker();        
        tm.init();
        
        OptionParser parser = new OptionParser("dict-maker");

        parser.addOption("-t","-1.0");
        parser.addOption("-s","-1.0");
        parser.addOption("-q","-1");
        parser.addOption("-p","0.05");
        parser.addOption("-phimin","-30.0");
        parser.addOption("-phimax","30.0");
        parser.addOption("-vz","0.0");
        parser.addOption("-var","default");
        parser.parse(args);
        
        if(parser.hasOption("-t")==true && parser.hasOption("-s")==true){
            float torus    = (float) parser.getOption("-t").doubleValue();
            float solenoid = (float) parser.getOption("-s").doubleValue();
            int charge = parser.getOption("-q").intValue();
            float pBinSize = (float) parser.getOption("-p").doubleValue();
            float phiMin = (float) parser.getOption("-phimin").doubleValue();
            float phiMax = (float) parser.getOption("-phimax").doubleValue();
            float vz = (float) parser.getOption("-vz").doubleValue();
            String dcVar = parser.getOption("-var").stringValue();
            tm.resetGeom(dcVar);
            tm.processFile(torus, solenoid, charge, pBinSize, phiMin, phiMax, vz);
        } else {
            LOGGER.log(Level.INFO, " FIELDS NOT SET");
        }
    }
    
    
}
