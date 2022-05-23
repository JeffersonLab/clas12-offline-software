package org.jlab.analysis.roads;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 * Trigger Roads Generator: it uses a fast-MC approach to generate the list 
 * of detector components 'hit' by a track.
 * 
 * Input parameters, defining the kinematics to be covered and detector 
 * configurations, are set via command line options. The found tracks are 
 * save to a text file.
 * 
 * Uses detector geometry packages and swimming.
 * 
 * @author devita
 * @author ziegler
 */
public class DictionaryGenerator {
    
    private DCGeant4Factory   dcDetector   = null;
    private FTOFGeant4Factory ftofDetector = null;
    private Detector          ecalDetector = null;
    private MagFieldsEngine   magfield     = null;
    private Random rand = null;
    
    private String  variation = "default";
    private int     run = 11;
    private double  torus, solenoid;
    private double  solShift;
    private long    seed = 0;
    private int     charge = -1;
    private double  pMin, pMax, thMin, thMax, phiMin, phiMax, vzMin, vzMax, vr;
    private boolean duplicates = false;
     
    public DictionaryGenerator() {
    }
    
    public DictionaryGenerator(String variation, double torusScale, double solenoidScale, long seed, int duplicates) {
        this.init(variation, torusScale, solenoidScale, seed, duplicates);
    }
    
    public void configure(int charge, double pMin, double pMax, double thMin, double thMax, 
                          double phiMin, double phiMax, double vzMin, double vzMax, double vr) {
//        this.variation  = variation;
//        this.torus      = torusScale;
//        this.solenoid   = solenoidScale;
        this.charge     = charge;
        this.pMin       = pMin;
        this.pMax       = pMax;
        this.thMin      = thMin;
        this.thMax      = thMax;
        this.phiMin     = phiMin;
        this.phiMax     = phiMax;
        this.vzMin      = vzMin;
        this.vzMax      = vzMax;        
        this.vr         = vr;        
        
        this.printConfiguration();
    }
    
    public void generate(int n) {
        String filename = "Dictionary" 
               + "_seed:"  + seed
               + "_n:"     + n
               + "_var:"   + variation 
               + "_t:"     + torus
               + "_s:"     + solenoid
               + "_q:"     + charge
               + "_p:"     + pMin + "-" + pMax
               + "_theta:" + thMin + "-" + thMax
               + "_phi:"   + phiMin + "-" + phiMax
               + "_z:"     + vzMin + "-" + vzMax
               + "_r:"     + vr
               + "_dup:"   + duplicates
               + ".txt";
    
        Swim swim = new Swim();
        Dictionary dictionary = new Dictionary();
        
        try {
            FileWriter writer = new FileWriter(filename, false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
 
            double invP     =1;
            double phiDeg   =1;
            double thetaDeg =1;
            double vxCm     =0;
            double vyCm     =0;
            double vzCm     =0;

            ProgressPrintout progress = new ProgressPrintout();

            for (int i = 0; i < n; i++) {
                
                // generate kinematics
                invP     = this.randomDouble(1./pMax, 1./pMin);
                phiDeg   = this.randomDouble(phiMin, phiMax);
                thetaDeg = this.randomDouble(thMin, thMax);
                vzCm     = this.randomDouble(vzMin, vzMax);
                if(vr>0) {
                    double r   = vr*Math.sqrt(this.randomDouble(0, 1));
                    double phi = this.randomDouble(-Math.PI, Math.PI);
                    vxCm = r*Math.cos(phi);
                    vyCm = r*Math.sin(phi);
                }
                double p = 1. / invP;

                Road road = this.getRoad(charge, p, thetaDeg, phiDeg, vxCm, vyCm, vzCm, swim);

                if(road.getLayerHits(3)>=3 && road.getSuperLayers()==6) {
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
                progress.setAsInteger("roads", dictionary.size());
                progress.updateStatus();
            }
            progress.showStatus();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Road getRoad(int charge, double p, double theta, double phi, double vx, double vy, double vz, Swim swim) {
        
        Road road = new Road();
                        
        double px = p * Math.cos(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
        double py = p * Math.sin(Math.toRadians(phi)) * Math.sin(Math.toRadians(theta));
        double pz = p * Math.cos(Math.toRadians(theta));
        road.setParticle(new Particle(211*charge,px,py,pz,vx, vy,vz));

        //find sector
        swim.SetSwimParameters(vx, vy, vz, px, py, pz, charge);

        double[] swimVal = new double[8];
        swimVal = swim.SwimToPlaneLab(175.);

        int sector = this.sector(swimVal[0], swimVal[1], swimVal[2]);
        road.setSector((byte) sector);

        Point3D rotatedP = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[3], swimVal[4], swimVal[5]));
        Point3D rotatedX = this.rotateToTiltedCoordSys(sector, new Point3D(swimVal[0], swimVal[1], swimVal[2]));

        for (int isl = 0; isl < 6; isl++) {
            for (int il = 0; il < 6; il++) {
                swim.SetSwimParameters(rotatedX.x(), rotatedX.y(), rotatedX.z(), rotatedP.x(), rotatedP.y(), rotatedP.z(), charge);
                int wire = this.swimtoLayer(sector, il, isl, swim); 
                road.setWire(isl*6+il+1, (byte) wire);
            }
        }

        double[] trkTOF  = swim.SwimToPlaneTiltSecSys(sector, 668.1);
        double[] trkECAL = swim.SwimToPlaneTiltSecSys(sector, 800.0);
        Vector3d ftof  = rotateToSectorCoordSys(trkTOF[0],trkTOF[1],trkTOF[2]);
        Vector3d ecal  = rotateToSectorCoordSys(trkECAL[0], trkECAL[1], trkECAL[2]);

        Line3d trkLine = new Line3d(ftof,ecal);
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
    
    private void init(String variation, double torusScale, double solenoidScale, long seed, int duplicates){
        
        this.variation = variation;
        ConstantProvider providerDC = GeometryFactory.getConstants(DetectorType.DC, run, variation);
        dcDetector = new DCGeant4Factory(providerDC, true, true);
        ConstantProvider providerTG = GeometryFactory.getConstants(DetectorType.TARGET, run, variation);
        solShift = providerTG.getDouble("/geometry/target/position",0);
        ConstantProvider providerFTOF = GeometryFactory.getConstants(DetectorType.FTOF, run, variation);
        ftofDetector = new FTOFGeant4Factory(providerFTOF);        
        ecalDetector =  GeometryFactory.getDetector(DetectorType.ECAL, run, variation);

        torus    = torusScale;
        solenoid = solenoidScale;
        magfield = new MagFieldsEngine();
        magfield.initializeMagneticFields();
        Swimmer.setMagneticFieldsScales(solenoid, torus, solShift);

        rand = new Random();
        rand.setSeed(seed);
        
        if(duplicates>0) this.duplicates = true;
    }

    public void printConfiguration() {
        System.out.println(" MAKING ROADS for: "
        +"\n Variation:\t"   + variation
        +"\n Torus:\t\t"       + torus
        +"\n Solenoid:\t"      + solenoid
        +"\n Charge:\t"        + charge
        +"\n P (GeV):\t"       + pMin   + "-" + pMax
        +"\n Theta (deg):\t"   + thMin  + "-" + thMax
        +"\n Phi (deg):\t"     + phiMin + "-" + phiMax
        +"\n Vz (cm):\t"       + vzMin  + "-" + vzMax
        +"\n Vr (cm):\t"       + vr
        +"\n Seed:\t\t"        + seed
        +"\n Duplicates:\t"    + duplicates);
    }
    
    private double randomDouble(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        
        return min + (max - min) * rand.nextDouble();
    }

    public static Vector3d rotateToSectorCoordSys(double x, double y, double z) {
        Vector3d v = new Vector3d(x,y,z);
        v.rotateY(Math.toRadians(25));
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
        parser.addOption("-pmin",       "0.3",     "minimum momentum in GeV");
        parser.addOption("-pmax",       "11.0",    "maximum momentum in GeV");
        parser.addOption("-thmin",      "5.0",     "minimum polar angle in degrees");
        parser.addOption("-thmax",      "40.0",    "maximum polar angle in degrees");
        parser.addOption("-phimin",    "-30.0",    "minimum azimuthal angle in degrees");
        parser.addOption("-phimax",     "30.0",    "maximum azimuthal angle in degrees");
        parser.addOption("-seed",       "10",      "random seed");
        parser.addOption("-variation",  "default", "geometry database variation");
        parser.addOption("-vzmin",     "-5.0",     "minimum vertex z coordinate in cm");
        parser.addOption("-vzmax",      "5.0",     "maximum vertex z coordinate in cm");
        parser.addOption("-vr",         "0.0",     "raster radius in cm");
        parser.addOption("-duplicates", "0",       "remove duplicates (1=on, 0=off)");
        parser.parse(args);
        
        
        double torus    = parser.getOption("-torus").doubleValue();
        double solenoid = parser.getOption("-solenoid").doubleValue();
        int    charge = parser.getOption("-charge").intValue();
        double pMin = parser.getOption("-pmin").doubleValue();
        double pMax = parser.getOption("-pmax").doubleValue();
        double thMin = parser.getOption("-thmin").doubleValue();
        double thMax = parser.getOption("-thmax").doubleValue();
        double phiMin = parser.getOption("-phimin").doubleValue();
        double phiMax = parser.getOption("-phimax").doubleValue();
        double vzMin = parser.getOption("-vzmin").doubleValue();
        double vzMax = parser.getOption("-vzmax").doubleValue();
        double vr = parser.getOption("-vr").doubleValue();
        long   seed = (long)parser.getOption("-seed").intValue();        
        int    duplicates = parser.getOption("-duplicates").intValue();
        String var = parser.getOption("-variation").stringValue();
        int    n = parser.getOption("-n").intValue();
        
        DictionaryGenerator maker = new DictionaryGenerator(var,torus,solenoid,seed, duplicates);
        
        maker.configure(charge, pMin, pMax, thMin, thMax, phiMin, phiMax, vzMin, vzMax, vr);
            
        maker.generate(n);
    }
    
    
}
