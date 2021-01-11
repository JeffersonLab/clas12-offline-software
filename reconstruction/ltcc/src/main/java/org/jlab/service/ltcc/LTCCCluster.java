/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
//import org.jMath.Vector.threeVec;
import java.util.List;
import java.util.ArrayList;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author Sylvester Joosten
 */
public final class LTCCCluster {
    
     // cluster requirements to define a good cluster
    static private final double GOOD_CLUSTER_NPHE_MIN = 0.;
    static private final double GOOD_CLUSTER_NPHE_MAX = 10000;
    static private final int GOOD_CLUSTER_N_SEGMENT_MIN = 1;
    static private final int GOOD_CLUSTER_N_SEGMENT_MAX = 5;
    static private final int GOOD_CLUSTER_N_SIDE_MIN = 1;
    static private final int GOOD_CLUSTER_N_SIDE_MAX = 2;
    static private final int GOOD_CLUSTER_NHIT_MIN = 1;
    static private final int GOOD_CLUSTER_NHIT_MAX = 
            GOOD_CLUSTER_N_SEGMENT_MAX * GOOD_CLUSTER_N_SIDE_MAX;
    
    private enum Status {
        GOOD (0), 
        BAD (1), 
        READ_AND_GOOD (2),
        READ_AND_BAD (3);        
        public final int code;         
        Status(int b) {
            this.code = b;
        }
        private static Status read(int i) {
            switch (i) {
                case 0:
                case 2:
                    return READ_AND_GOOD;
                case 1:
                case 3:
                default:
                    return READ_AND_BAD;
            }
        }
        private boolean readOnly() {
            return (this == READ_AND_GOOD || this == READ_AND_BAD);
        }
        private boolean isGood() {
            return (this != BAD && this != READ_AND_BAD);
        }
    }
    
    // cluster sector
    private int sector = -1;
    
    // theta range of the cluster
    private int iThetaMin = 99;     // minimum theta index [0-17]
    private int iThetaMax = -1;     // maximum theta index [0-17]
    private double thetaMin = 0;    // minimum theta
    private double thetaMax = 0;    // maximum theta
    
    // phi range of the cluster
    private int iLTCCPhiMin = 99;   // minimum phi index   [0-11]
    private int iLTCCPhiMax = -1;   // maximum phi index   [0-11]
    private double phiMin = 0;      // mininmum phi
    private double phiMax = 0;      // maximum phi
    
    // cluster averages/totals
    private double nphe = 0;        // total number of photo-electrons
    private int nHits = 0;          // total number of hits
    private final Vector3D position;  // average cluster position * nphe
    private double time = 0;        // average cluster time * nphe
    private double segment = 0;     // average segment * nphe
    private Status status = Status.GOOD; // cluster status
    
    LTCCCluster() {
        position = new Vector3D();
    }
    
    LTCCCluster(LTCCHit center) {
        this();
        add(center);
    }
    
    // read a cluster from a file
    LTCCCluster(DataBank bank, int index) {
        // we cannot meaningfully load the theta and phi boundary indices
        this.sector = bank.getByte("sector", index);
        this.status = Status.read(bank.getByte("status", index));
        this.nphe = bank.getFloat("nphe", index);
        // multiply segment/position/time with nphe because the 
        // respective getter will re-average the value
        this.segment = bank.getShort("segment", index) * this.nphe;
        this.position = new Vector3D(
                bank.getFloat("x", index), 
                bank.getFloat("y", index), 
                bank.getFloat("z", index));
        this.position.multiply(this.nphe);
        this.time = bank.getFloat("time", index) * this.nphe;
        this.nHits = bank.getShort("nHits", index);
        this.thetaMin = bank.getFloat("minTheta", index);
        this.thetaMax = bank.getFloat("maxTheta", index);
        this.phiMin = bank.getFloat("minPhi", index);
        this.phiMax = bank.getFloat("maxPhi", index);   
    }

    static public List<LTCCCluster> loadClusters(DataEvent event) {
        return loadClusters(event, false);
    }
    static public List<LTCCCluster> loadClusters(DataEvent event, boolean requireGood) {
        DataBank bank = event.getBank("LTCC::clusters");
        
        List<LTCCCluster> clusters = new ArrayList<>();
        for (int i = 0; i < bank.rows(); ++i) {
            LTCCCluster cluster = new LTCCCluster(bank, i);
            if (requireGood && !cluster.isGood()) {
                continue;
            }
            clusters.add(new LTCCCluster(bank, i));
        }
        return clusters;
    }
    
    public void add(LTCCHit hit) {
        // don't update clusters read from a file
        if (status.readOnly()) {
            return;
        }
        this.nHits += 1;
        updateThetaPhi(hit);
        updateTotals(hit);
        // first hit is the cluster center
        if (this.sector < 0) {
            this.sector = hit.getSector();
        }
        updateStatus();
    }
    
    public void print() {
        System.out.println("Cluster info");
        System.out.printf("Sector: %d\n", getSector());
        System.out.printf("Segment: %d\n", getSegment());
        System.out.printf("Nhits: %f\n", getNHits());
        System.out.printf("Nphe: %f\n", getNphe());
        System.out.printf("Time: %f\n", getTime());
        System.out.printf("Theta: %f\n", getPosition().theta());
        System.out.printf("Phi: %f", getPosition().phi());
        System.out.println("------");
    }
    
    public int getSector() {
        return this.sector;
    }
    public int getSegment() {
        return (int) (this.segment / this.nphe);
    }
    public double getNphe() {
        return this.nphe;
    }
    public Vector3D getPosition() {
        return this.position.multiply(1/this.nphe);
    }
    public double getTime() {
        return this.time / this.nphe;
    }
    public double getNHits() {
        return this.nHits;
    }
    public double getThetaMin() {
        return this.thetaMin;
    }
    public double getThetaMax() {
        return this.thetaMax;
    }
    public double getPhiMin() {
        return this.phiMin;
    }
    public double getPhiMax() {
        return this.phiMax;
    }
    public boolean isGood() {
        return this.status.isGood();
    }
    public boolean readOnly() {
        return this.status.readOnly();
    }
    
    public void save(DataBank bank, int index) {
        // calculate average position
        Vector3D xyz = this.getPosition();
                // set the bank entries
        bank.setShort("id", index, (short) index);
        bank.setByte("status", index, (byte) this.status.code);
        bank.setByte("sector", index, (byte) getSector());
        bank.setShort("segment", index, (short) getSegment());
        bank.setFloat("x", index, (float) xyz.x());
        bank.setFloat("y", index, (float) xyz.y());
        bank.setFloat("z", index, (float) xyz.z());
        bank.setFloat("nphe", index, (float) getNphe());
        bank.setFloat("time", index, (float) this.getTime());
        bank.setShort("nHits", index, (short) getNHits());
        bank.setFloat("minTheta", index, (float) Math.toDegrees(this.thetaMin));
        bank.setFloat("maxTheta", index, (float) Math.toDegrees(this.thetaMax));
        bank.setFloat("minPhi", index, (float) Math.toDegrees(this.phiMin));
        bank.setFloat("maxPhi", index, (float) Math.toDegrees(this.phiMax));
    }
    
    public static void saveClusters(DataEvent event, List<LTCCCluster> clusters) {
        if (!clusters.isEmpty()) {
            DataBank clusterBank = event.createBank("LTCC::clusters", clusters.size());
            for (int i = 0; i < clusters.size(); ++i) {
                clusters.get(i).save(clusterBank, i);
            }
            event.appendBank(clusterBank);
        }
    }
 
    private void updateThetaPhi(LTCCHit hit) {
        if (hit.getThetaIndex() > this.iThetaMax) {
            this.iThetaMax = hit.getThetaIndex();
            this.thetaMax = hit.getPosition().theta();
        }
        if (hit.getThetaIndex() < this.iThetaMin) {
            this.iThetaMin = hit.getThetaIndex();
            this.thetaMin = hit.getPosition().theta();
        }
        if (hit.getLTCCPhiIndex() > this.iLTCCPhiMax) {
            this.iLTCCPhiMax = hit.getLTCCPhiIndex();
            this.phiMax = hit.getPosition().phi();
        }
        if (hit.getLTCCPhiIndex() < this.iLTCCPhiMin) {
            this.iLTCCPhiMin = hit.getLTCCPhiIndex();
            this.phiMin = hit.getPosition().phi();
        }
    }
    private void updateTotals(LTCCHit hit) {
        this.nphe += hit.getNphe();
        this.position.add(hit.getPosition().multiply(hit.getNphe()));
        this.time += hit.getTime() * hit.getNphe();   
        this.segment += hit.getSegment() * hit.getNphe();
    }
    private void updateStatus() {
        int dTheta = this.iThetaMax - this.iThetaMin + 1;
        int dPhi = this.iLTCCPhiMax - this.iLTCCPhiMin + 1;
        if ((this.nphe >= GOOD_CLUSTER_NPHE_MIN)
                && (this.nphe <= GOOD_CLUSTER_NPHE_MAX)
                && (dTheta >= GOOD_CLUSTER_N_SEGMENT_MIN)
                && (dTheta <= GOOD_CLUSTER_N_SEGMENT_MAX)
                && (dPhi >= GOOD_CLUSTER_N_SIDE_MIN)
                && (dPhi <= GOOD_CLUSTER_N_SIDE_MAX)
                && (this.nHits >= GOOD_CLUSTER_NHIT_MIN)
                && (this.nHits <= GOOD_CLUSTER_NHIT_MAX)) {
            this.status = Status.GOOD;
        } else {
            this.status = Status.BAD;
        }
    }
}
