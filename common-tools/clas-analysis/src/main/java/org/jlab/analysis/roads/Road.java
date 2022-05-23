package org.jlab.analysis.roads;

import java.util.ArrayList;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita, ziegler
 */
public class Road {
    
    public final int length = 51;
    private byte     sector = 0;
    private byte[]   dcWires = new byte[36];
    private byte[]   ftofPaddles = new byte[3];
    private byte[]   ecalStrips  = new byte[3];
    private byte     htccMask = 0;
    private Particle particle = null;

    public Road() {
        this.particle = new Particle();
    }
     
    public Road(byte[] road, Particle particle) {
        if(road.length!=13) System.out.println("ROAD: error in initializing road from byte arry with length " +road.length);
        else {
            for(int i=0; i<6; i++) this.dcWires[i*6] = road[i];
            this.ftofPaddles[1] = road[6];
            this.ftofPaddles[2] = road[7];
            for(int i=0; i<3; i++) this.ecalStrips[i] = road[8+i];
            this.htccMask = road[11];
            this.sector   = road[12];
            this.particle=particle;
        }
    }
    
    public Road(String line) {
        String[] items = line.split("\t");
        if(items.length!=this.length) System.out.println("ROAD: error in initializing road from string");
        else {
            int charge   = Integer.parseInt(items[0]);
            double p     = Double.parseDouble(items[1]);
            double theta = Math.toRadians(Double.parseDouble(items[2]));
            double phi   = Math.toRadians(Double.parseDouble(items[3]));
            double vz    = Double.parseDouble(items[41]);
            double px    = p*Math.sin(theta)*Math.cos(phi);
            double py    = p*Math.sin(theta)*Math.sin(phi);
            double pz    = p*Math.cos(theta);
            Particle road = new Particle(211*charge, px, py, pz, 0, 0, vz);
            for(int i=0; i<3; i++) road.setProperty("ECALe"+(i*3+1), Double.parseDouble(items[48+i]));
            for(int i=0; i<36; i++) this.dcWires[i] = Byte.parseByte(items[4+i]);
            this.ftofPaddles[0] = Byte.parseByte(items[40]);                    
            this.ftofPaddles[1] = Byte.parseByte(items[42]);                    
            for(int i=0; i<3; i++) this.ecalStrips[i] = Byte.parseByte(items[43+i]);
            this.htccMask = Byte.parseByte(items[46]);                    
            this.sector   = Byte.parseByte(items[47]);                    
            this.ftofPaddles[2] = Byte.parseByte(items[42]);                    
        }
    }

    
    public static ArrayList<Road> getRoads(DataEvent event, int chargeSelect, int pidSelect, double thrs) {
        ArrayList<Road> roads = new ArrayList();
        DataBank runConfig       = null;
        DataBank recParticle     = null;
        DataBank recCalorimeter  = null;
        DataBank recScintillator = null;
        DataBank recCherenkov    = null;
        DataBank recTrack        = null;
        DataBank tbtTrack        = null;
        DataBank ecalCluster     = null;
        DataBank tbtHits         = null;
        DataBank htccRec         = null;
        DataBank htccADC         = null;
        if (event.hasBank("RUN::config")) {
            runConfig = event.getBank("RUN::config");
        }            
        if (event.hasBank("REC::Particle")) {
            recParticle = event.getBank("REC::Particle");
        }
        if (event.hasBank("REC::Scintillator")) {
            recScintillator = event.getBank("REC::Scintillator");
        }
        if (event.hasBank("REC::Calorimeter")) {
            recCalorimeter = event.getBank("REC::Calorimeter");
        }
        if (event.hasBank("REC::Cherenkov")) {
            recCherenkov = event.getBank("REC::Cherenkov");
        }
        if (event.hasBank("REC::Track")) {
            recTrack = event.getBank("REC::Track");
        }
        if (event.hasBank("TimeBasedTrkg::TBTracks")) {
            tbtTrack = event.getBank("TimeBasedTrkg::TBTracks");
        }
        if (event.hasBank("TimeBasedTrkg::TBHits")) {
            tbtHits = event.getBank("TimeBasedTrkg::TBHits");
        }
        if (event.hasBank("ECAL::clusters")) {
            ecalCluster = event.getBank("ECAL::clusters");
        }
        if (event.hasBank("HTCC::rec")) {
            htccRec = event.getBank("HTCC::rec");
        }
        if (event.hasBank("HTCC::adc")) {
            htccADC = event.getBank("HTCC::adc");
        }
        // add other banks
        if(recParticle!=null && recTrack != null && tbtTrack != null && tbtHits != null) {
            for (int i = 0; i < recTrack.rows(); i++) {
                Road road = new Road();
                // start from tracks
                int index    = recTrack.getShort("index", i);
                int pindex   = recTrack.getShort("pindex", i);
                int detector = recTrack.getByte("detector", i);
                // use only forward tracks
                if(detector == DetectorType.DC.getDetectorId()) {
                    int charge   = recParticle.getByte("charge", pindex);
                    int pid      = recParticle.getInt("pid", pindex);
                    // if charge or pid selectrion are set, then compare to the current track values
                    if(chargeSelect != 0 && chargeSelect!=charge) continue;
                    if(pidSelect != 0    && pidSelect!=pid)       continue;   
                    // save particle information
                    Particle part = new Particle(
                                    -11*charge,
                                    recParticle.getFloat("px", pindex),
                                    recParticle.getFloat("py", pindex),
                                    recParticle.getFloat("pz", pindex),
                                    recParticle.getFloat("vx", pindex),
                                    recParticle.getFloat("vy", pindex),
                                    recParticle.getFloat("vz", pindex)); 
                    if(part.p()<thrs) continue; //use only roads with momentum above the selected value
                    road.setParticle(part);
                    // get the DC wires' IDs
                    int trackSector = 0;
                    for (int j = 0; j < tbtHits.rows(); j++) {
                        if (tbtHits.getByte("trkID", j) == tbtTrack.getShort("id", index)) {
                            trackSector = tbtHits.getByte("sector", j);
                            int superlayer = tbtHits.getByte("superlayer", j);
                            int layer = tbtHits.getByte("layer", j);
                            int wire = tbtHits.getShort("wire", j);
                            road.setWire((superlayer - 1) * 6 + layer, (byte) wire);
                        }
                    }
                    road.setSector((byte) trackSector);
                    if(road.getSuperLayers()!=6) continue;
                    // now check other detectors
                     // check FTOF
                    if(recScintillator!=null) {
                        for(int j=0; j<recScintillator.rows(); j++) {
                            if(recScintillator.getShort("pindex",j) == pindex) {
                                detector   = recScintillator.getByte("detector", j);
                                int layer  = recScintillator.getByte("layer",j);
                                int paddle = recScintillator.getShort("component",j);
                                if(detector==DetectorType.FTOF.getDetectorId()) road.setPaddle(layer, (byte) paddle);
                            }
                        }
                    }
                    // check ECAL
                    if(recCalorimeter!=null && ecalCluster!=null) {
                        for(int j=0; j<recCalorimeter.rows(); j++) {
                            if(recCalorimeter.getShort("pindex",j) == pindex) {
                                detector      = recCalorimeter.getByte("detector", j);
                                index         = recCalorimeter.getShort("index",j);
                                int layer     = recCalorimeter.getByte("layer",j);
                                double energy = recCalorimeter.getFloat("energy",j);
                                // use pcal only
                                if(detector==DetectorType.ECAL.getDetectorId()) {
                                        if(layer==DetectorLayer.PCAL) {
                                        road.setStrip(1, (byte) ((ecalCluster.getInt("coordU",index)-4)/8+1));
                                        road.setStrip(2, (byte) ((ecalCluster.getInt("coordV",index)-4)/8+1));
                                        road.setStrip(3, (byte) ((ecalCluster.getInt("coordW",index)-4)/8+1));
                                    }
                                    road.setECALenergy(layer, energy);
                                }
                            }
                        }
                    }
                   // check HTCC
                    if (recCherenkov != null && htccRec != null && htccADC != null && false) {
                        int htcc_event;
//                                recCherenkov.show(); htccADC.show();
                        for (int j = 0; j < recCherenkov.rows(); j++) {
                            if (recCherenkov.getShort("pindex", j) == pindex) {
                                detector = recCherenkov.getByte("detector", j);
                                if (detector == DetectorType.HTCC.getDetectorId()) {
                                    int nhits = htccRec.getShort("nhits",recCherenkov.getShort("index", j));
                                    double x = recCherenkov.getFloat("x", j);
                                    double y = recCherenkov.getFloat("y", j);
                                    double z = recCherenkov.getFloat("z", j);                                            
                                    double thetaCheren = Math.acos(z/Math.sqrt(x*x+y*y+z*z));
                                    double phiCheren   = Math.atan2(y, x);
                                    thetaCheren = Math.toDegrees(thetaCheren);
                                    phiCheren   = Math.toDegrees(phiCheren );
                                    double phiCC   = Math.round(phiCheren); 
                                    if(phiCC<0) phiCC +=360;
                                    double thetaCC = ((double) Math.round(thetaCheren*100))/100.;
                                    ArrayList<int[]> htccPMTs        = htccPMT(thetaCC, phiCC);
                                    ArrayList<int[]> htccPMTsMatched = new ArrayList<int[]>();
//                                            System.out.println(thetaCheren + " " + thetaCC + " " + phiCheren + " " + phiCC + " " + htccPMTs.size());
                                    //The special case of 4 hits, where we need to check if the hits were not in fact only 3
                                    for(int iPMT = 0; iPMT < htccPMTs.size(); iPMT++) {
                                        int htccSector    = htccPMTs.get(iPMT)[0];
                                        int htccLayer     = htccPMTs.get(iPMT)[1];
                                        int htccComponent = htccPMTs.get(iPMT)[2];
                                        boolean found = false;
//                                                System.out.println(iPMT + " " + htccSector + " " + htccLayer + " " + htccComponent);
                                        for (int k = 0; k < htccADC.rows(); k++) {
                                            int sector    = htccADC.getByte("sector", k);
                                            int layer     = htccADC.getByte("layer", k);
                                            int component = htccADC.getShort("component", k);
//                                                    System.out.println(k + " " + sector + " " + layer + " " + component);
                                            if( htccSector    == sector && 
                                                htccLayer     == layer && 
                                                htccComponent == component) {
                                                found = true;
//                                                        System.out.println("Found match in adc bank");
                                            }                                                  
                                        }
                                        if(found) {
                                            htccPMTsMatched.add(htccPMTs.get(iPMT));
                                        }
                                    }
                                    if(htccPMTsMatched.size() != nhits) {
                                        System.out.println("Mismatch in HTCC cluster size " +  runConfig.getInt("event",0) + " " + nhits +"/"+htccPMTsMatched.size()+"/"+htccPMTs.size() + " " + thetaCC + " " +  phiCC + " " +((phiCC+30)%60-30));
                                    }
                                    road.setHtccMask(Road.htccMask(htccPMTsMatched));
                                }
                            }
                        }
                    }
                    roads.add(road);
                }
            }
        }
        return roads;
    }
                        
    private static byte htccMask(ArrayList<int[]> htccPMTS) {
        int[] htccMaskArray = new int[8];
        for(int iPMT = 0; iPMT < htccPMTS.size(); iPMT++) {
            int htccSector    = htccPMTS.get(iPMT)[0];
            int htccLayer     = htccPMTS.get(iPMT)[1];
            int htccComponent = htccPMTS.get(iPMT)[2];
            int ibit = (htccComponent-1) +(htccLayer-1)*4;
            htccMaskArray[ibit]=1;
        }
        byte htccMask=0;
//        System.out.println("Mask " + htccPMTS.size());
        for(int ibit=0; ibit<8; ibit++) {
            if(htccMaskArray[ibit]>0) {
                int imask = 1 << ibit;
                htccMask += imask;
//                System.out.println(ibit + " " + imask + " " + htccMask);
            }
        }
        return htccMask;
    }
    
    private static ArrayList<int[]> htccPMT(double th, double ph) {
        // phi is defined between 0 and 360
        double TableTheta1[] = { 8.75, 16.25, 23.75, 31.25};
        double TableTheta2[] = {12.50, 20.00, 27.50};
        double TableTheta3[] = {11.25, 18.75, 26.25};
        double TableTheta4[] = {13.75, 21.25, 28.75};

        ArrayList<int[]> htccPMTS = new ArrayList<int[]>();

        double p1, p2, ph_new;

        int sector=0;
        if (ph > 30.0 && ph <= 90.0) {
            sector = 2;
        } else if (ph > 90.0 && ph <= 150.0) {
            sector = 3;
        } else if (ph > 150.0 && ph <= 210.0) {
            sector = 4;
        } else if (ph > 210.0 && ph <= 270.0) {
            sector = 5;
        } else if (ph > 270.0 && ph <= 330.0) {
            sector = 6;
        } else if (ph <= 30.0 || ph > 330.0) {
            sector = 1;
        }
        double phSec = ((ph+30.0)%60)-30;

//   int sector[];
//   HTCC_Hits cherenkov_road = null;
        //First, find the number of hits, which table to use, and sector/layer/component
        for (int i = 0; i < 4; i++) {
            // 1 hit case
            if (th == TableTheta1[i] && (phSec==-15 || phSec==15)) {
                int htccPMT[] = new int[3];
                htccPMT[0] = sector;
                if (phSec == -15.0) {
                    htccPMT[1] = 1;
                } else if (phSec == 15.0) {
                    htccPMT[1] = 2;
                }              
                htccPMT[2] = i + 1;
                htccPMTS.add(htccPMT);  
            }
            // 2 hits over phi
            else if (th == TableTheta1[i] && phSec==0) {
                for(int k = 0; k < 2; k++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = k + 1;
                    htccPMT[2] = i + 1;
                    htccPMTS.add(htccPMT);  
                }                    
            }
            // 2 hits over sectors
            else if (th == TableTheta1[i] && phSec==-30) {
                for(int k = 0; k < 2; k++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + k; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - k;
                    htccPMT[2] = i + 1;
                    htccPMTS.add(htccPMT);  
                }                    
            }
        }
        for (int i = 0; i < 3; i++) {
            // 2 hits over theta
            if (th == TableTheta2[i] && (phSec==-15 || phSec==15)) {
                for(int k = 0; k < 2; k++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    if (phSec == -15.0) {
                        htccPMT[1] = 1;
                    } else if (phSec == 15.0) {
                        htccPMT[1] = 2;
                    }              
                    htccPMT[2] = i + 1 + k;
                    htccPMTS.add(htccPMT);  
                }                    
            }
            // 4 hits
            if (th == TableTheta2[i] && phSec==0) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    htccPMTS.add(htccPMT);  
                }
                }
            }
            // 4 hits in between sectors
            if (th == TableTheta2[i] && phSec==-30) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + j; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - j;
                    htccPMT[2] = i + 1 + k;
                    htccPMTS.add(htccPMT);  
                }
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            // 3 hit combinations
            if (th == TableTheta3[i] && phSec==-5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta3[i] && phSec==5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==0)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta3[i] && phSec==-25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector - j; if(htccPMT[0]==0) htccPMT[0]=6;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta3[i] && phSec==25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + j; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - j;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            // 3 hit combinations
            if (th == TableTheta4[i] && phSec==-5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta4[i] && phSec==5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==0)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta4[i] && phSec==-25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector - j; if(htccPMT[0]==0) htccPMT[0]=6;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta4[i] && phSec==25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + j; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - j;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }        }
        return htccPMTS;
    }

    public void setHtccMask(byte htccMask) {
        this.htccMask = htccMask;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }
    
    public void setECALenergy(int layer, double energy) {
        if(layer==1 || layer==4 || layer==7) this.particle.setProperty("ECALe"+layer, energy);
        else System.out.println("ROAD: error in setting the ECAL energy for layer " + layer);        
    }

    public void setPaddle(int layer, byte paddle) {
        if(layer>0 && layer<= 3) this.ftofPaddles[layer-1] = paddle;
        else System.out.println("ROAD: error in setting the FTOF paddle number for layer " + layer);
    }

    public void setSector(byte sector) {
        this.sector = sector;
    }

    public void setStrip(int layer, byte strip) {
        if(layer>0 && layer<=3) this.ecalStrips[layer-1] = strip;
        else System.out.println("ROAD: error in setting the PCAL strip number for layer " + layer);
    }

    public void setWire(int layer, byte wire) {
        if(layer>0 && layer<= 36) this.dcWires[layer-1] = wire;
        else System.out.println("ROAD: error in setting the DC wire number for layer " + layer);
    }

    public byte getPaddle(int layer) {
        if(layer>0 && layer<= 3) return 0;
        else return ftofPaddles[layer-1];
    }

    public byte getStrip(int layer) {
        if(layer>0 && layer<= 3) return 0;
        else return ecalStrips[layer-1];
    }

    public byte getHtccMask() {
        return htccMask;
    }

    public byte getSector() {
        return sector;
    }

    public int getLayerHits(int layer) {
        int n=0;
        if(layer>0  && layer<=6) {
            for(int isl=0; isl<6; isl++) {
                if(this.dcWires[layer-1+isl*6]>0) n++;
            }
        }
        return n;
    }
    
    public int getSuperLayers() {
        int nSL=0;
        for(int isl=0; isl<6; isl++) {
            int nL=0;
            for(int il=0; il<6; il++) {
                if(this.dcWires[isl*6+il]>0) nL++;
            }
            if(nL>0) nSL++;
        }
        return nSL;
    }
    
    public Particle getParticle() {
        return particle;
    }

    public byte[] getRoad() {
        byte[] road = new byte[13];
        for(int isl = 0; isl < 6; isl++) {
            for(int il=0; il<6; il++) {
                if(this.dcWires[isl*6 + il] != 0) {
                   road[isl] = this.dcWires[il*6+il];
                   break;
                }
            }
        }
        road[6] = this.ftofPaddles[1];
        road[7] = this.ftofPaddles[2];
        road[8] = this.ecalStrips[0];
        road[9] = this.ecalStrips[1];
        road[10] = this.ecalStrips[2];
        road[11] = this.htccMask;
        road[12] = this.sector;
        return road;
    }

    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        
        str.append(String.format("%d\t",   this.particle.charge()));
        str.append(String.format("%.2f\t", this.particle.p()));
        str.append(String.format("%.2f\t", Math.toDegrees(this.particle.theta())));
        str.append(String.format("%.2f\t", Math.toDegrees(this.particle.phi())));
        for(int i=0; i<this.dcWires.length; i++) str.append(String.format("%d\t", this.dcWires[i]));
        str.append(String.format("%d\t",   this.ftofPaddles[1]));
        str.append(String.format("%.2f\t", this.particle.vz()));
        str.append(String.format("%d\t",   this.ftofPaddles[2]));
        for(int i=0; i<this.ecalStrips.length; i++) str.append(String.format("%d\t", this.ecalStrips[i]));
        str.append(String.format("%d\t",   this.htccMask));
        str.append(String.format("%d\t",   this.sector));
        for(int i=0; i<this.ecalStrips.length; i++) str.append(String.format("%.1f\t", this.particle.getProperty("ECALe"+(i*3+1))));
        return str.toString();
    }
}
