package org.jlab.service.dc;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.dc.Constants;

import org.jlab.utils.options.OptionParser;

import java.util.Random;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Path3D;

public class TrackDictionaryMakerRNG extends DCEngine{

    private static int count(List<Integer> Wi) {
        int count = 0;
        for(int i =0; i< Wi.size(); i++) {
            if(Wi.get(i)>0)
                count++;
        }
        return count;
    }
    Random r = null;
    long seed= 10;
    private Map<ArrayList<Integer>, Integer> dictionary = null;
    
    public TrackDictionaryMakerRNG(){
        super("TDM");
    }
    
    @Override
    public boolean init() {
        r = new Random();
        MagFieldsEngine mf = new MagFieldsEngine();
        mf.initializeMagneticFields();
        super.init();
        return true;
    }
    public void processFile(int duplicates, float torScale, float solScale, int charge, int n, long seed,
            float pMin, float pMax, float thMin, float thMax, float phiMin, float phiMax, float vzMin, float vzMax) {
        
        Swimmer.setMagneticFieldsScales(solScale, torScale, -1.9);
        Swim sw = new Swim();
        PrintWriter pw = null;
        try {
            LOGGER.log(Level.INFO, " MAKING ROADS for: "
                    +"\n Torus:\t\t"   +String.valueOf(torScale)
                    +"\n Solenoid:\t"  +String.valueOf(solScale)
                    +"\n Charge:\t"    +String.valueOf(charge)
                    +"\n PMinGev:\t"   +String.valueOf(pMin)
                    +"\n PMaxGeV:\t"   +String.valueOf(pMax)
                    +"\n ThMinDeg:\t"  +String.valueOf(thMin)
                    +"\n ThMaxDeg:\t"  +String.valueOf(thMax)
                    +"\n PhiMinDeg:\t" +String.valueOf(phiMin)
                    +"\n PhiMaxDeg:\t" +String.valueOf(phiMax)
                    +"\n VzMinCm:\t"   +String.valueOf(vzMin)
                    +"\n VzMaxCm:\t"   +String.valueOf(vzMax)
                    +"\n Seed:\t\t"    +String.valueOf(seed)
                    +"\n NTracks:\t"   +String.valueOf(n)
                    +"\n Duplicates:\t"+String.valueOf(duplicates));
            String fileName = "TracksDicTorus"+String.valueOf(torScale)+"Solenoid"+String.valueOf(solScale)
                    +"Charge"+String.valueOf(charge)+"n"+String.valueOf(n)+"Seed"+String.valueOf(seed)
                    +"PMinGev" +String.valueOf(pMin)+"PMaxGeV" +String.valueOf(pMax)
                    +"ThMinDeg" +String.valueOf(thMin)+"ThMaxDeg" +String.valueOf(thMax)
                    +"PhiMinDeg" +String.valueOf(phiMin)+"PhiMaxDeg" +String.valueOf(phiMax)
                    +"VzMinCm" +String.valueOf(vzMin)+"VzMaxCm" +String.valueOf(vzMax)
                    +"Duplicates" +String.valueOf(duplicates)+".txt";
            pw = new PrintWriter(fileName);
            this.r.setSeed(seed);
            LOGGER.log(Level.INFO, "\n Random generator seed set to: " + seed);
            LOGGER.log(Level.INFO, "\n Dictionary file name: " + fileName + "\n");
            this.ProcessTracks(pw, Constants.getInstance().dcDetector, Constants.getInstance().ftofDetector, 
                                   Constants.getInstance().ecalDetector, sw, charge, n, pMin, pMax, thMin, thMax, phiMin, phiMax, vzMin, vzMax,duplicates);
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrackDictionaryMakerRNG.class.getName()).log(Level.SEVERE, null, ex);
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
    public Point3D rotateToTiltedCoordSys(int sector, Point3D labFramePars){
        double[] XinSec = new double[3];
        double[] XinTiltSec = new double[3];

//        int sector = this.getSector(labFramePars.x(), labFramePars.y(), labFramePars.z());

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
        int sector = this.getSector(swimVal[0], swimVal[1], swimVal[2]);
        
        Point3D rotatedP = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[3], swimVal[4], swimVal[5]));
        Point3D rotatedX = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[0], swimVal[1], swimVal[2]));
//LOGGER.log(Level.INFO, " sector in TrackDictionary "+sector);
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
            Wi.add(i + 1); //LOGGER.log(Level.INFO, "min "+min); ? one strip off
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
    private List<Integer> Wl1 = new ArrayList<Integer>();
    private List<Integer> Wl2 = new ArrayList<Integer>();
    private List<Integer> Wl3 = new ArrayList<Integer>();
    private List<Integer> Wl4 = new ArrayList<Integer>();
    private List<Integer> Wl5 = new ArrayList<Integer>();
    private List<Integer> Wl6 = new ArrayList<Integer>();
    private String entry;
    
    
    private double randomDouble(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        
        return min + (max - min) * r.nextDouble();
    }
    private void Clear(int[] wireArray) {
        for(int i=0; i<wireArray.length; i++) {
            wireArray[i]=0;
        }
    }
    public void ProcessTracks(PrintWriter pw,DCGeant4Factory dcDetector, 
            FTOFGeant4Factory ftofDetector, Detector ecalDetector,
            Swim sw, int q, int numRandoms, 
            float PMin, float PMax, 
            float ThMin, float ThMax, 
            float PhiMin, float PhiMax, 
            float VzMin, float VzMax,
            int duplicates) {
        double[] swimVal = new double[8];
        Map<ArrayList<Integer>, Integer> newDictionary = new HashMap<>();
        int[] wireArray = new int[36];
        
        double invP     =1;
        double phiDeg   =1;
        double thetaDeg =1;
        double vzCm     =1;

            
        for (int i = 0; i < numRandoms; i++) {
            if(i%10000 == 0) LOGGER.log(Level.INFO, "\t" + i + " tracks generated, " + newDictionary.size() + " roads found");
            Clear(wireArray);
            invP     =this.randomDouble((double)(1./PMax), (double) (1./PMin));
            phiDeg   =this.randomDouble((double) PhiMin, (double) PhiMax);
            thetaDeg =this.randomDouble(ThMin, ThMax);
            vzCm     =this.randomDouble(VzMin, VzMax);
            double p = 1. / invP;
            
            double px = p * Math.cos(Math.toRadians(phiDeg)) * Math.sin(Math.toRadians(thetaDeg));
            double py = p * Math.sin(Math.toRadians(phiDeg)) * Math.sin(Math.toRadians(thetaDeg));
            double pz = p * Math.cos(Math.toRadians(thetaDeg));
            sw.SetSwimParameters(0, 0, vzCm, px, py, pz, q);
            swimVal = sw.SwimToPlaneLab(175.);
            int sector = this.getSector(swimVal[0], swimVal[1], swimVal[2]);

            //Point3D rotatedP = tw.rotateToTiltedCoordSys(new Point3D(px, py, pz));
            Point3D rotatedP = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[3], swimVal[4], swimVal[5]));
            Point3D rotatedX = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[0], swimVal[1], swimVal[2]));

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
                TrackDictionaryMakerRNG.swimtoLayer(sector, 0, sl, Wl1, dcDetector, sw);        
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                TrackDictionaryMakerRNG.swimtoLayer(sector, 1, sl, Wl2, dcDetector, sw);        
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                TrackDictionaryMakerRNG.swimtoLayer(sector, 2, sl, Wl3, dcDetector, sw);        
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                TrackDictionaryMakerRNG.swimtoLayer(sector, 3, sl, Wl4, dcDetector, sw);        
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                TrackDictionaryMakerRNG.swimtoLayer(sector, 4, sl, Wl5, dcDetector, sw);        
                sw.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), q);
                TrackDictionaryMakerRNG.swimtoLayer(sector, 5, sl, Wl6, dcDetector, sw);

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
            
            //Wl1.get(0)= wire in SL1, L1;  Wl2.get(0)= wire in SL1, L2; Wl1.get(5)= wire in SL6, L1;  Wl2.get(5)= wire in SL6, L2; 
            //wireArray[0]=Wl1.get(0); wireArray[1]=Wl2.get(0); wireArray[2]=Wl3.get(0); wireArray[3]=Wl4.get(0); wireArray[4]=Wl5.get(0); wireArray[5]=Wl6.get(0); 
            for( int j =0; j<6; j++) {
                wireArray[0+6*j]=Wl1.get(j); wireArray[1+6*j]=Wl2.get(j); wireArray[2+6*j]=Wl3.get(j); wireArray[3+6*j]=Wl4.get(j); wireArray[4+6*j]=Wl5.get(j); wireArray[5+6*j]=Wl6.get(j); 
            }
            if (count(Wl3) >=3) {
                ArrayList<Integer> wires = new ArrayList<>();
                for (int k = 0; k < 6; k++) {
                    for (int l=0; l<1; l++) {
                        if(wireArray[k*6 +l] > 0) {
                           wires.add(wireArray[k*6+l]);
                           break;
                        }
                    }
                }
                if(wires.size()!=6)continue;
                wires.add(paddle1b);
                wires.add(paddle2);
                wires.add(pcalU);
                wires.add(pcalV);
                wires.add(pcalW);
                wires.add(htcc);
                wires.add(sector);
                if(newDictionary.containsKey(wires) && duplicates!=0)  {
                        int nRoad = newDictionary.get(wires) + 1;
                        newDictionary.replace(wires, nRoad);
                       // LOGGER.log(Level.INFO, " Number of duplicate roads "+nRoad+" p "+p+" theta "+thetaDeg+" phi "+phiDeg+" vz "+vzCm);
                }
                else {
                    newDictionary.put(wires, 1);
                /*    System.out.printf("%d\t%.2f\t%.2f\t%.2f\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%.2f\t%d\t%d\t%d\t%d\n",
                    q, p, thetaDeg, phiDeg,
                    Wl1.get(0), Wl2.get(0), Wl3.get(0), Wl4.get(0), Wl5.get(0), Wl6.get(0), 
                    Wl1.get(1), Wl2.get(1), Wl3.get(1), Wl4.get(1), Wl5.get(1), Wl6.get(1), 
                    Wl1.get(2), Wl2.get(2), Wl3.get(2), Wl4.get(2), Wl5.get(2), Wl6.get(2), 
                    Wl1.get(3), Wl2.get(3), Wl3.get(3), Wl4.get(3), Wl5.get(3), Wl6.get(3), 
                    Wl1.get(4), Wl2.get(4), Wl3.get(4), Wl4.get(4), Wl5.get(4), Wl6.get(4), 
                    Wl1.get(5), Wl2.get(5), Wl3.get(5), Wl4.get(5), Wl5.get(5), Wl6.get(5), 
                    paddle, vzCm, paddle2, pcalU, pcalV, pcalW);  */
                    pw.printf("%d\t%.2f\t%.2f\t%.2f\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%.2f\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t0.0\t0.0\t0.0\n",
                    q, p, thetaDeg, phiDeg,
                    Wl1.get(0), Wl2.get(0), Wl3.get(0), Wl4.get(0), Wl5.get(0), Wl6.get(0), 
                    Wl1.get(1), Wl2.get(1), Wl3.get(1), Wl4.get(1), Wl5.get(1), Wl6.get(1), 
                    Wl1.get(2), Wl2.get(2), Wl3.get(2), Wl4.get(2), Wl5.get(2), Wl6.get(2), 
                    Wl1.get(3), Wl2.get(3), Wl3.get(3), Wl4.get(3), Wl5.get(3), Wl6.get(3), 
                    Wl1.get(4), Wl2.get(4), Wl3.get(4), Wl4.get(4), Wl5.get(4), Wl6.get(4), 
                    Wl1.get(5), Wl2.get(5), Wl3.get(5), Wl4.get(5), Wl5.get(5), Wl6.get(5), 
                    paddle1b, vzCm, paddle2, pcalU, pcalV, pcalW, htcc, sector);
                }   
            }
        }
        LOGGER.log(Level.INFO, "\t" + numRandoms + " tracks generated, " + newDictionary.size() + " roads found");
    }

    public void ProcessCosmics(PrintWriter pw, DCGeant4Factory dcDetector, TrackDictionaryMakerRNG tw, Swim sw) {

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

                        int sector = this.getSector(x,y,z);
                        
                        Point3D rotatedP = tw.rotateToTiltedCoordSys(sector, new Point3D(px, py, pz));

                        Point3D rotatedX = tw.rotateToTiltedCoordSys(sector, new Point3D(x, y, z));

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
            eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sector-1, sl, l, i);
            eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sector-1, sl, l, i);
            Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
            //Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
           
            if (wl.distance(new Point3D(trk[0], trk[1], trk[2])).length() < min) { 
                min = wl.distance(new Point3D(trk[0], trk[1], trk[2])).length();
                w = i; //LOGGER.log(Level.INFO, " min "+min+" wire "+(i+1)+" sl "+sl+" l "+l+" trk "+trk[0]+", "+trk[1]+", "+trk[2]+" mp "+dcDetector.getWireMidpoint(sl, l, i)+" : "+dcDetector.getWireMidpoint(sl, l, 0).z);
            } 
        }

        if (min < wMax*1.01) {
            Wi.add(w + 1); //LOGGER.log(Level.INFO, "min "+min);
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
        double wLeft  = 0;
        double wRight = 0;
        for (int i = 0; i < 112; i++) {
            eu.mihosoft.vrl.v3d.Vector3d p3dl = dcDetector.getWireLeftend(sector-1, sl, l, i);
            eu.mihosoft.vrl.v3d.Vector3d p3dr = dcDetector.getWireRightend(sector-1, sl, l, i);
            Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
            //Line3D wl = dcDetector.getSector(0).getSuperlayer(sl).getLayer(2).getComponent(i).getLine();
           
            if (wl.distance(new Point3D(trk[0], trk[1], trk[2])).length() < min) { 
                min = wl.distance(new Point3D(trk[0], trk[1], trk[2])).length();
                wLeft = p3dl.y;
                wRight= p3dr.y;
                w = i; //LOGGER.log(Level.INFO, " min "+min+" wire "+(i+1)+" sl "+sl+" l "+l+" trk "+trk[0]+", "+trk[1]+", "+trk[2]+" mp "+dcDetector.getWireMidpoint(sl, l, i)+" : "+dcDetector.getWireMidpoint(sl, l, 0).z);
            }  
        }

        if (min < wMax*1.01 && trk[1]>(wLeft-wMax) && trk[1]<(wRight+wMax)) {
            Wi.add(w + 1); //LOGGER.log(Level.INFO, "min "+min);
 //           LOGGER.log(Level.INFO, w + " " + sl + " " + l + " " + wLeft + " " + wRight);
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
    
        
        OptionParser parser = new OptionParser("dict-maker");

        parser.addOption("-dupli","0","remove duplicates");
        parser.addOption("-t","-1.0");
        parser.addOption("-s","-1.0");
        parser.addOption("-q","-1");
        parser.addOption("-n","10000");
        parser.addOption("-pmin","0.3");
        parser.addOption("-pmax","11.0");
        parser.addOption("-thmin","5.0");
        parser.addOption("-thmax","40.0");
        parser.addOption("-phimin","-30.0");
        parser.addOption("-phimax","30.0");
        parser.addOption("-seed","10");
        parser.addOption("-var","default");
        parser.addOption("-vzmin","-5.0");
        parser.addOption("-vzmax","5.0");
        parser.parse(args);
        

        if(parser.hasOption("-t")==true && parser.hasOption("-s")==true){
            TrackDictionaryMakerRNG tm = new TrackDictionaryMakerRNG();        
            tm.init();
            float torus    = (float) parser.getOption("-t").doubleValue();
            float solenoid = (float) parser.getOption("-s").doubleValue();
            int charge = parser.getOption("-q").intValue();
            int n = parser.getOption("-n").intValue();
            int duplicates = parser.getOption("-dupli").intValue();
            float pMin = (float) parser.getOption("-pmin").doubleValue();
            float pMax = (float) parser.getOption("-pmax").doubleValue();
            float thMin = (float) parser.getOption("-thmin").doubleValue();
            float thMax = (float) parser.getOption("-thmax").doubleValue();
            float phiMin = (float) parser.getOption("-phimin").doubleValue();
            float phiMax = (float) parser.getOption("-phimax").doubleValue();
            float vzMin = (float) parser.getOption("-vzmin").doubleValue();
            float vzMax = (float) parser.getOption("-vzmax").doubleValue();
            long seed = (long)parser.getOption("-seed").intValue();
//            tm.r.setSeed(seed);
            String dcVar = parser.getOption("-var").stringValue();
            tm.resetGeom(dcVar);
            tm.processFile(duplicates,torus, solenoid, charge, n, seed, pMin, pMax, thMin, thMax, phiMin, phiMax, vzMin, vzMax);
        } else {
            LOGGER.log(Level.INFO, " FIELDS NOT SET");
        }
    }
    
    
}
