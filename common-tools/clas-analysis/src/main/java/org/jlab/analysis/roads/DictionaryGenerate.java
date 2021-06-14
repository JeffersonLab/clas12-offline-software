package org.jlab.analysis.roads;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.math3.util.FastMath;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author devita, ziegler
 */
public class DictionaryGenerate {
    
    private DCGeant4Factory   dcDetector   = null;
    private FTOFGeant4Factory ftofDetector = null;
    private Detector          ecalDetector = null;
    private MagFieldsEngine   magfield     = null;
    private Random rand = null;
    
    private String  variation = "default";
    private int     run = 11;
    private double  torus, solenoid;
    private long    seed = 0;
    private int     charge = -1;
    private double  pMin, pMax, thMin, thMax, phiMin, phiMax, vzMin, vzMax;
    private boolean duplicates = false;
     
    public DictionaryGenerate() {
    }
    
    public DictionaryGenerate(String variation, double torusScale, double solenoidScale, long seed) {
        this.init(variation, torusScale, solenoidScale, seed);
    }
    
    public void configure(String variation, double torusScale, double solenoidScale, 
            int charge, double pMin, double pMax, double thMin, double thMax, 
            double phiMin, double phiMax, double vzMin, double vzMax,
            long seed, int duplicates) {
        this.variation  = variation;
        this.torus      = torusScale;
        this.solenoid   = solenoidScale;
        this.charge     = charge;
        this.pMin       = pMin;
        this.pMax       = pMax;
        this.thMin      = thMin;
        this.thMax      = thMax;
        this.phiMin     = phiMin;
        this.phiMax     = phiMax;
        this.vzMin      = vzMin;
        this.vzMax      = vzMax;        
        this.seed       = seed;
        if(duplicates>0) this.duplicates = true;
        
        this.init(variation, torusScale, solenoidScale, seed);
        this.printConfiguration();
    }
    
    public void generate(int n) {
        String filename = "Dictionary_" 
               + "Seed"+String.valueOf(seed)
               + "n"+String.valueOf(n)
               + "Variation" + variation 
               + "Torus"+String.valueOf(torus)
               + "Solenoid"+String.valueOf(solenoid)
               + "Charge"+String.valueOf(charge)
               + "PMinGev" +String.valueOf(pMin)
               + "PMaxGeV" +String.valueOf(pMax)
               + "ThMinDeg" +String.valueOf(thMin)
               + "ThMaxDeg" +String.valueOf(thMax)
               + "PhiMinDeg" +String.valueOf(phiMin)
               + "PhiMaxDeg" +String.valueOf(phiMax)
               + "VzMinCm" +String.valueOf(vzMin) 
               + "VzMaxCm" +String.valueOf(vzMax)
               + "Duplicates" +String.valueOf(duplicates)+".txt";
    
        Swim swim = new Swim();
        Dictionary dictionary = new Dictionary();
        
        try {
            FileWriter writer = new FileWriter(filename, true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
 
            double invP     =1;
            double phiDeg   =1;
            double thetaDeg =1;
            double vzCm     =1;


            for (int i = 0; i < n; i++) {
                if(i%10000 == 0) System.out.println("\t" + i + " tracks generated, " + dictionary.size() + " roads found");

                // generate kinematics
                invP     =this.randomDouble(1./pMax, 1./pMin);
                phiDeg   =this.randomDouble(phiMax, phiMin);
                thetaDeg =this.randomDouble(thMin, thMax);
                vzCm     =this.randomDouble(vzMin, vzMax);
                double p = 1. / invP;

                Road road = this.getRoad(charge, p, thetaDeg, phiDeg, vzCm, swim);
                
                if(road.getSuperLayerHits(3)>=3 && road.getSuperLayers()==6) {
                    if(dictionary.containsKey(road.getRoad()) && duplicates)  {
                            dictionary.replace(road.getRoad(), road.getParticle());
                           // System.out.println(" Number of duplicate roads "+nRoad+" p "+p+" theta "+thetaDeg+" phi "+phiDeg+" vz "+vzCm);
                    }
                    else {
                        dictionary.put(road.getRoad(), road.getParticle());

                        bufferedWriter.write(road.toString());
                        bufferedWriter.newLine();
                    }
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Road getRoad(int charge, double p, double theta, double phi, double vz, Swim swim) {
        
        Road road = new Road();
                        
        double px = p * Math.cos(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
        double py = p * Math.sin(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
        double pz = p * Math.cos(Math.toRadians(theta));
        road.setParticle(new Particle(211*charge,px,py,pz,0,0,vz));

        //find sector
        swim.SetSwimParameters(0, 0, vz, px, py, pz, charge);
        double[] swimVal = new double[8];
        swimVal = swim.SwimToPlaneLab(175.);
        int sector = this.sector(swimVal[0], swimVal[1], swimVal[2]);
        road.setSector((byte) sector);

        Point3D rotatedP = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[3], swimVal[4], swimVal[5]));
        Point3D rotatedX = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[0], swimVal[1], swimVal[2]));

        swim.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), charge);
        for (int isl = 0; isl < 6; isl++) {
            for (int il = 0; il < 6; il++) {
                int wire = this.swimtoLayer(sector, il, isl, swim); 
                road.setWire(isl*6+il+1, (byte) wire);
            }
        }

        double[] trkTOF  = swim.SwimToPlaneTiltSecSys(sector, 668.1);
        double[] trkECAL = swim.SwimToPlaneTiltSecSys(sector, 800.0);
        Vector3d ftof  = rotateToSectorCoordSys(trkTOF[0],trkTOF[1],trkTOF[2]);
        Vector3d ecal  = rotateToSectorCoordSys(trkECAL[0], trkECAL[1], trkECAL[2]);

        Line3d trkLine = new Line3d(ftof,ecal) ;
        List<DetHit> ftofHits  = ftofDetector.getIntersections(trkLine);
        if (ftofHits != null && ftofHits.size() > 0) {
            for (DetHit hit : ftofHits) {
                FTOFDetHit fhit = new FTOFDetHit(hit);
                road.setPaddle(fhit.getLayer(), (byte) fhit.getPaddle());
            }
        }

        Path3D path = new Path3D(new Point3D(ftof.x, ftof.y, ftof.z), new Point3D(ecal.x,ecal.y,ecal.z));
        List<DetectorHit> ecalHits = ecalDetector.getHits(path);

        int pcalU=0; int pcalV=0; int pcalW=0;
        if (ecalHits != null && ecalHits.size() > 0) {
            for (DetectorHit ehit : ecalHits) {
                if(ehit.getSuperlayerId()+1==1) {
                    if(ehit.getLayerId()+1==1 && pcalU==0) 
                        pcalU = ehit.getComponentId()+1;
                    if(ehit.getLayerId()+1==2 && pcalV==0) 
                        pcalV = ehit.getComponentId()+1;
                    if(ehit.getLayerId()+1==3 && pcalW==0) 
                        pcalW = ehit.getComponentId()+1;
                }
            }
        }
        road.setStrip(1, (byte) pcalU);
        road.setStrip(2, (byte) pcalV);
        road.setStrip(3, (byte) pcalW);
        return road;
    }
    
    private void init(String variation, double torusScale, double solenoidScale, long seed){
        
        ConstantProvider providerDC = GeometryFactory.getConstants(DetectorType.DC, run, variation);
        dcDetector = new DCGeant4Factory(providerDC, true, true);
        ConstantProvider providerTG = GeometryFactory.getConstants(DetectorType.TARGET, run, variation);
        double targetPosition = providerTG.getDouble("/geometry/target/position",0);
        ConstantProvider providerFTOF = GeometryFactory.getConstants(DetectorType.FTOF, run, variation);
        ftofDetector = new FTOFGeant4Factory(providerFTOF);        
        ConstantProvider providerEC = GeometryFactory.getConstants(DetectorType.ECAL, run, variation);
        ecalDetector =  GeometryFactory.getDetector(DetectorType.ECAL, run, variation);

        magfield = new MagFieldsEngine();
        magfield.initializeMagneticFields();
        Swimmer.setMagneticFieldsScales(solenoidScale, torusScale, targetPosition);

        rand = new Random();
        rand.setSeed(seed);
    }

    public void printConfiguration() {
        System.out.println(" MAKING ROADS for: "
        +"\n Variation:\t\t"   + variation
        +"\n Torus:\t\t"       + String.valueOf(torus)
        +"\n Solenoid:\t"      + String.valueOf(solenoid)
        +"\n Charge:\t"        + String.valueOf(charge)
        +"\n P (GeV):\t"       + String.valueOf(pMin)   + "-" + String.valueOf(pMax)
        +"\n Theta (deg):\t"   + String.valueOf(thMin)  + "-" + String.valueOf(thMax)
        +"\n Phi (deg):\t"     + String.valueOf(phiMin) + "-" + String.valueOf(phiMax)
        +"\n Vz (cm):\t"       + String.valueOf(vzMin)  + "-" + String.valueOf(vzMax)
        +"\n Seed:\t\t"        + String.valueOf(seed)
        +"\n Duplicates:\t"    + String.valueOf(duplicates));
    }
    
    private double randomDouble(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        
        return min + (max - min) * rand.nextDouble();
    }

    public static Vector3d rotateToSectorCoordSys(double x, double y, double z) {
        Vector3d v = new Vector3d(x,y,z);
        v.rotateY(Math.toRadians(-25));
        return v;
    }

    public Point3D rotateToTiltedCoordSys(int sector, Point3D lab){

        if ((sector < 1) || (sector > 6)) {
            return new Point3D(0,0,0);
        }
        
        Point3D tilted = new Point3D(lab); 
        tilted.rotateZ(-Math.toRadians((sector-1)*60));
        tilted.rotateY(-Math.toRadians(25));
        return tilted;
    }

    private int sector(double x, double y, double z) {
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
    
    private int swimtoLayer(int sector, int l, int sl, Swim sw) {
        int wire = 0;
        
        double[] traj = sw.SwimToPlaneTiltSecSys(sector, dcDetector.getWireMidpoint(sector-1, sl, l, 0).z); 
       
        double wMax = Math.abs(dcDetector.getWireMidpoint(sector-1, sl, 0, 0).x
                             - dcDetector.getWireMidpoint(sector-1, sl, 0, 1).x) / 2.;

        double min = 10000;
        int w = -1;
        double wLeft  = 0;
        double wRight = 0;
        for (int i = 0; i < 112; i++) {
            Vector3d p3dl = dcDetector.getWireLeftend(sector-1, sl, l, i);
            Vector3d p3dr = dcDetector.getWireRightend(sector-1, sl, l, i);
            Line3D wl = new Line3D(new Point3D(p3dl.x, p3dl.y, p3dl.z), new Point3D(p3dr.x, p3dr.y, p3dr.z));
            if (wl.distance(new Point3D(traj[0], traj[1], traj[2])).length() < min) { 
                min = wl.distance(new Point3D(traj[0], traj[1], traj[2])).length();
                wLeft = p3dl.y;
                wRight= p3dr.y;
                w = i; 
            }  
        }

        if (min < wMax*1.01 && traj[1]>(wLeft-wMax) && traj[1]<(wRight+wMax)) {
            wire = w+1;
        }
        
        return wire;
    }

    
    public static void main(String[] args) {
    
        
        OptionParser parser = new OptionParser("dict-maker");

        parser.addRequired("-torus",   "torus scale");
        parser.addRequired("-solenoid","solenoid scale");
        parser.addRequired("-charge",  "particle charge");
        parser.addRequired("-n",       "number of roads");
        parser.addOption("-pmin",      "0.3");
        parser.addOption("-pmax",     "11.0");
        parser.addOption("-thmin",     "5.0");
        parser.addOption("-thmax",    "40.0");
        parser.addOption("-phimin",  "-30.0");
        parser.addOption("-phimax",   "30.0");
        parser.addOption("-seed",     "10");
        parser.addOption("-variation","default");
        parser.addOption("-vzmin",   "-5.0");
        parser.addOption("-vzmax",    "5.0");
        parser.addOption("-dupli",    "0","remove duplicates");
        parser.parse(args);
        
        
        double torus    = (float) parser.getOption("-torus").doubleValue();
        double solenoid = (float) parser.getOption("-solenoid").doubleValue();
        int    charge = parser.getOption("-charge").intValue();
        double pMin = (float) parser.getOption("-pmin").doubleValue();
        double pMax = (float) parser.getOption("-pmax").doubleValue();
        double thMin = (float) parser.getOption("-thmin").doubleValue();
        double thMax = (float) parser.getOption("-thmax").doubleValue();
        double phiMin = (float) parser.getOption("-phimin").doubleValue();
        double phiMax = (float) parser.getOption("-phimax").doubleValue();
        double vzMin = (float) parser.getOption("-vzmin").doubleValue();
        double vzMax = (float) parser.getOption("-vzmax").doubleValue();
        long   seed = (long)parser.getOption("-seed").intValue();        
        int    duplicates = parser.getOption("-dupli").intValue();
        String var = parser.getOption("-variation").stringValue();
        int    n = parser.getOption("-n").intValue();
        
        DictionaryGenerate maker = new DictionaryGenerate(var,torus,solenoid,seed);
        
        maker.configure(var, torus, solenoid, charge, pMin, pMax, thMin, thMax, phiMin, phiMax, vzMin, vzMax, seed, duplicates);
            

    }
    
    
}
