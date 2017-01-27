/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
//import org.jlab.utils.groups.IndexedTable;
import org.jMath.Vector.threeVec;

import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author Sylvester Joosten
 *
 * LTCC hit info
 *
 */
public final class LTCCHit {
    // nphe requirements for a good hit
    static private final double NPHE_MIN_HIT = 0.001;
    static private final double NPHE_MAX_HIT = 10000.;

    // raw LTCC info
    private final int sector;     // CLAS12 sector (1-6)
    private final int side;       // side within the segment (left: 1, right: 2)
    private final int segment;    // LTCC segment (1-18)
    private final int adc;        // integrated ADC
    private final double rawTime;// hit time
    private final short pedestal; // pedestal (unused, already in ADC)

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
        int run = 11; // TODO how to get run number?
        //IndexedTable gain = ccdb.getConstants(run, "/calibration/ltcc/gain");
        //IndexedTable timing_offset = ccdb.getConstants(run, "/calibration/ltcc/timing_offset");
        DataBank ltccADC = event.getBank("LTCC::adc");
        
        List<LTCCHit> hits = new LinkedList<>();
        for (int i = 0; i < ltccADC.rows(); ++i) {
            LTCCHit hit = new LTCCHit(ltccADC, i);
            if (hit.getNphe() > NPHE_MIN_HIT && hit.getNphe() < NPHE_MAX_HIT) {
                hits.add(new LTCCHit(ltccADC, i));
            }
        }
        return hits;
    }
    
    // TODO: add timing calibration
    LTCCHit(DataBank ltccADC, int index/*, IndexedTable gain*/) {
       this.sector = ltccADC.getByte("sector", index);
       this.side = ltccADC.getByte("layer", index);
       this.segment = ltccADC.getShort("component", index);
       this.adc = ltccADC.getInt("ADC", index);
       this.rawTime = ltccADC.getFloat("time", index);
       this.pedestal = ltccADC.getShort("ped", index);
       this.nphe = calcNphe(/* gain */);
       this.time = calcTime(/* timing */);
       this.iLTCCPhi = calcLTCCPhiIndex();
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
    public threeVec getPosition() {
        double phi = Math.toRadians(PHI0[this.segment - 1] 
                * (this.side == 1 ? -1 : 1))
                + 2. * Math.PI * (this.sector - 1) / 6.;
        threeVec v = new threeVec();
        v.setPolar(RHO0[this.segment - 1], 
                Math.toRadians(THETA0[this.segment - 1]),
                phi);

        return v;
    }
    public int getSector() {
        return sector;
    }
    public int getSegment() {
        return segment;
    }
    public int getSide() {
        return side;
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
    
    
    private double calcNphe(/*IndexedTable gain*/) {
        // TODO: verify with Maurizio if this is indeed the format
        //gain.getDoubleValue("gain", sector, side, segment);
        return this.adc / 100; // hard-coded for now
    }
    
    private double calcTime(/* IndexedTable timing */) {
        return this.rawTime;
    }
    
    private int calcLTCCPhiIndex() {
        return 2 * (this.sector - 1) + (this.side - 1);
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
