/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;
//import org.jMath.Vector.threeVec;

import java.util.LinkedList;
import java.util.List;
import org.jlab.geom.prim.Vector3D;


/**
 *
 * @author Sylvester Joosten
 *
 * LTCC hit info
 *
 */
public final class LTCCHit {
    // nphe requirements for a good hit
    static private final double NPHE_MIN_HIT = 0;
    static private final double NPHE_MAX_HIT = 10000.;
    
    private enum Status {
        GOOD, BAD;
        private boolean isGood() {
            return (this == GOOD);
        }
    }

    // raw LTCC info
    private final int sector;     // CLAS12 sector (1-6)
    private final int side;       // side within the segment (left: 1, right: 2)
    private final int segment;    // LTCC segment (1-18)
    private final int adc;        // integrated ADC
    private final double rawTime;// hit time
    //private final short pedestal; // pedestal (unused, already in ADC)
    
    // hit status
    private final Status status;

    // calibrated quantities
    private final double nphe;       // number of photo-electrons
    private final double time;    // calibrated hit timing
    
    // internal phi index starting from [sector 1/left, ... , sector 6/right]
    // Note that this index starts below the usual phi origin - doing 
    // (12 + iPhi - 1) % 12 will yield the standard phi index
    // see getPhiIndex() and getLTCCPhiIndex()
    private final int iLTCCPhi;

    // load a list of all good hits
    public static List<LTCCHit> loadHits(DataEvent event, ConstantsManager ccdb) {
        int run = -1;
        if (event.hasBank("RUN::config")) {
            DataBank header = event.getBank("RUN::config");
            run = header.getInt("run", 0);
        }
        IndexedTable spe = null;
        IndexedTable timing_offset = null;
        IndexedTable stat = null;
        if (ccdb != null && run > 0) {
            spe  = ccdb.getConstants(run, "/calibration/ltcc/spe");
            stat = ccdb.getConstants(run, "/calibration/ltcc/status");
            //timing_offset = ccdb.getConstants(run, "/calibration/ltcc/timing_offset");
        }
        DataBank bank = event.getBank("LTCC::adc");
        
        List<LTCCHit> hits = new LinkedList<>();
        for (int i = 0; i < bank.rows(); ++i) {
            LTCCHit hit = new LTCCHit(bank, i, spe, timing_offset, stat);
            if (hit.isGood()) {
                hits.add(hit);
            }
        }
        return hits;
    }
    public static List <LTCCHit> loadHits(DataEvent event) {
        return loadHits(event, null);
    }
    
    LTCCHit(DataBank bank, 
            int index, 
            IndexedTable spe, 
            IndexedTable timing_offset,
            IndexedTable status_table) {
       this.sector = bank.getByte("sector", index);
       this.segment = bank.getShort("component", index);
       this.side = bank.getByte("order", index) + 1;
       this.adc = bank.getInt("ADC", index);
       this.rawTime = bank.getFloat("time", index);
       //this.pedestal = bank.getShort("ped", index);
       this.nphe = calcNphe(spe);
       this.time = calcTime(timing_offset);
       this.iLTCCPhi = calcLTCCPhiIndex();
       this.status = calcStatus(status_table);
    }        
    LTCCHit(DataBank bank, int index) {
        this(bank, index, null, null, null);
    }
   
    public int getThetaIndex() {
        return this.segment - 1;
    }
    //  LTCC phi index starting from [sector 1/left, ... , sector 6/right]
    public int getLTCCPhiIndex() {
        return this.iLTCCPhi;
    }
    // standard phi index, starting from [sector 1/right, ..., sector 1/left]
    public int getPhiIndex() {
        return getPhiIndex(this.iLTCCPhi);
    }
    static public int getPhiIndex(int ltccPhiIndex) {
        return (12 + ltccPhiIndex - 1) % 12;
    }
    public double getNphe() {
        return this.nphe;
    }
    public double getTime() {
        return this.time;
    }
    // get the eliptical mirror center of the eliptical mirror that belongs 
    // to the hit PMT. 
    // NOTE: due to the angle of the track in the magnetic field, this is
    //       typically NOT the mirror that was actually hit. See
    //       LTCCClusterCorrection.calcPosition() for more info.
    public Vector3D getPosition() {
        double phi = Math.toRadians(PHI0[this.segment - 1] 
                * (this.side == 1 ? -1 : 1))
                + 2. * Math.PI * (this.sector - 1) / 6.;
        Vector3D v = new Vector3D(RHO0[this.segment - 1]*
                Math.sin(Math.toRadians(THETA0[this.segment - 1]))*Math.cos(phi),
                RHO0[this.segment - 1]*
                Math.sin(Math.toRadians(THETA0[this.segment - 1]))*Math.sin(phi),
                RHO0[this.segment - 1]*
                Math.cos(Math.toRadians(THETA0[this.segment - 1]))
        );
        //v.setPolar(RHO0[this.segment - 1], 
        //        Math.toRadians(THETA0[this.segment - 1]),
        //        phi);

        return v;
    }
    public int getSector() {
        return this.sector;
    }
    public int getSegment() {
        return this.segment;
    }
    public int getSide() {
        return this.side;
    }
    public int getADC() {
        return this.adc;
    }
    public double getRawTime() {
        return this.rawTime;
    }
    public boolean isGood() {
        return this.status.isGood();
    }

    
    // is this hit a neighbor within a theta and timing window? 
    // (phi is dealt with by enforcing that both hits were in the same
    //  sector)
    public boolean isNeighbor(LTCCHit hit, int dThetaMax, double dTimeMax) {
        int dTheta = Math.abs(this.segment - hit.segment);
        double dTime = Math.abs(this.time - hit.time);
        return (dTheta <= dThetaMax && this.sector == hit.sector && dTime <= dTimeMax);
    }
    public boolean isNeighbor(LTCCHit hit, double dTimeMax) { 
        return isNeighbor(hit, 1, dTimeMax);
    }
    
    
    private double calcNphe(IndexedTable spe) {
        // sane default in case there is no ccdb
        double calibration = 1. / 200.; 
        // load value from CCDB
        if (spe != null) {
            calibration = spe.getDoubleValue("mean", sector, side, segment);
            if (calibration > 0) {
                calibration = 1 / calibration;
            }
        }
        return (this.adc > 0 ? this.adc * calibration : -1);
    }
    
    private double calcTime(IndexedTable timing) {
        if (timing != null) {
            return this.rawTime;
        } else {
            // fallback
            return this.rawTime;
        }
    }
    
    private int calcLTCCPhiIndex() {
      return 2 * (this.sector - 1) + (this.side - 1);
    }
    
    private Status calcStatus(IndexedTable status_table) {
        int ccdb_status = status_table.getIntValue("status", sector, side, segment);
        return 
            (this.adc >= 0 && this.nphe > NPHE_MIN_HIT && this.nphe < NPHE_MAX_HIT && ccdb_status==0) 
                ? Status.GOOD
                : Status.BAD;
    }

    // LTCC specs
    // polar coordinates (in lab frame) of the eliptical mirror centers 
    // for each of the segments for from target for sector 1/right
    // note that 1/left  has the same coordinates, but with -1. * phi
    private final double[] RHO0 = { // [cm]
        664.612, 661.408, 657.519, 654.148, 650.091, 645.757,
        641.874, 638.225, 635.421, 632.638, 631.206, 627.403,
        626.36, 623.957, 617.503, 621.91, 621.845, 623.926};
    private final double[] THETA0 = { // [degree]
        5.76294, 7.06809, 8.38733, 9.73387, 11.0776, 12.4349,
        13.8235, 15.2304, 16.6625, 18.1621, 19.7188, 21.3341,
        22.9771, 24.873, 26.7458, 28.586, 30.5954, 31.175};
    private final double[] PHI0 = { // [degree]
        3.72443, 6.45884, 8.4087, 9.7843, 10.8255, 11.6715,
        12.3397, 12.6632, 12.9966, 13.2521, 13.3003, 13.4214,
        11.7866, 12.0826, 12.3421, 12.3184, 12.2741, 12.7263};
}
