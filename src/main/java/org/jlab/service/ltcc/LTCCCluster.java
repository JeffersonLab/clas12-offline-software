/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jMath.Vector.threeVec;
import java.util.List;

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
        
    // cluster sector
    private int sector;
    
    // theta range of the cluster
    private int iThetaMin;      // minimum theta index [0-17]
    private int iThetaMax;      // maximum theta index [0-17]
    private double thetaMin;    // minimum theta
    private double thetaMax;    // maximum theta
    
    // phi range of the cluster
    private int iLTCCPhiMin;    // minimum phi index   [0-11]
    private int iLTCCPhiMax;    // maximum phi index   [0-11]
    private double phiMin;      // mininmum phi
    private double phiMax;      // maximum phi
    
    // cluster averages/totals
    private double nphe;        // total number of photo-electrons
    private int nHits;          // total number of hits
    private threeVec position;  // average cluster position * nphe
    private double time;        // average cluster time * nphe
    private double segment;     // average segment * nphe
    
    LTCCCluster() {
        this.sector = -1;
        this.nphe = 0;
        this.nHits = 0;
        this.iThetaMin = 99;
        this.iThetaMax = -1;
        this.thetaMin = 0;
        this.thetaMax = 0;
        this.iLTCCPhiMin = 99;
        this.iLTCCPhiMax = -1;
        this.phiMin = 0;
        this.phiMax = 0;
        this.position = new threeVec();
        this.time = 0;
        this.segment = 0;
    }
    
    LTCCCluster(LTCCHit center) {
        this();
        add(center);
    }
    
    public void add(LTCCHit hit) {
        this.nHits += 1;
        updateThetaPhi(hit);
        updateTotals(hit);
        // first hit is the cluster center
        if (this.sector < 0) {
            this.sector = hit.getSector();
        }
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
    public threeVec getPosition() {
        return this.position.mult(1/this.nphe);
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
        int dTheta = this.iThetaMax - this.iThetaMin;
        int dPhi = this.iLTCCPhiMax - this.iLTCCPhiMin;
        return (this.nphe >= GOOD_CLUSTER_NPHE_MIN)
                && (this.nphe <= GOOD_CLUSTER_NPHE_MAX)
                && (dTheta >= GOOD_CLUSTER_N_SEGMENT_MIN)
                && (dTheta <= GOOD_CLUSTER_N_SEGMENT_MAX)
                && (dPhi >= GOOD_CLUSTER_N_SIDE_MIN)
                && (dPhi <= GOOD_CLUSTER_N_SIDE_MAX)
                && (this.nHits >= GOOD_CLUSTER_NHIT_MIN)
                && (this.nHits <= GOOD_CLUSTER_NHIT_MAX);   
    }
    
    public void write(DataBank clusterBank, int index) {
        // calculate average position
        threeVec xyz = this.getPosition();
                // set the bank entries
        clusterBank.setShort("id", index, (short) index);
        clusterBank.setByte("status", index, (byte) (isGood() ? 1 : 0));
        clusterBank.setByte("sector", index, (byte) getSector());
        clusterBank.setShort("segment", index, (short) getSegment());
        clusterBank.setFloat("x", index, (float) xyz.x());
        clusterBank.setFloat("y", index, (float) xyz.y());
        clusterBank.setFloat("z", index, (float) xyz.z());
        clusterBank.setFloat("nphe", index, (float) getNphe());
        clusterBank.setFloat("time", index, (float) this.getTime());
        clusterBank.setShort("nHits", index, (short) getNHits());
        clusterBank.setFloat("minTheta", index, (float) Math.toDegrees(this.thetaMin));
        clusterBank.setFloat("maxTheta", index, (float) Math.toDegrees(this.thetaMax));
        clusterBank.setFloat("minPhi", index, (float) Math.toDegrees(this.phiMin));
        clusterBank.setFloat("maxPhi", index, (float) Math.toDegrees(this.phiMax));
    }
    public static void writeClusters(DataEvent event, List<LTCCCluster> clusters) {
        if (clusters.size() == 0) {
            return;
        }
        DataBank clusterBank = event.createBank("LTCC::clusters", clusters.size());
        for (int i = 0; i < clusters.size(); ++i) {
            clusters.get(i).write(clusterBank, i);
        }
        event.appendBank(clusterBank);
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
        this.position.addi(hit.getPosition().mult(hit.getNphe()));
        this.time += hit.getTime() * hit.getNphe();   
        this.segment += hit.getSegment() * hit.getNphe();
    }
}
