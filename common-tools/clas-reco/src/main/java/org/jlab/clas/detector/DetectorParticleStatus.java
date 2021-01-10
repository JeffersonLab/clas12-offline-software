package org.jlab.clas.detector;

import org.jlab.detector.base.DetectorType;
/**
 * A particle status definition based on detector "topology".
 * Negative means it's the one used to determine start time.
 *
 * FIXME: This was moved from a previous classless incarnation
 * with only a single status variable.  Now should simplify and
 * just use counters on detector hits as the base variables instead.
 *
 * @author baltzell
 */
public class DetectorParticleStatus {

    public static final int REGION=1000;
    public static final int SCINTILLATOR=100;
    public static final int CALORIMETER=10;
    public static final int CHERENKOV=1;
    public static final int BAND=8;
    public static final int CENTRAL=4;
    public static final int FORWARD=2;
    public static final int TAGGER=1;

    private int status=0;
    private boolean isForward=false;
    private boolean isCentral=false;
    private boolean isTagger=false;
    private boolean isBand=false;
    private int nScintillator=0;
    private int nCalorimeter=0;
    private int nCherenkov=0;

    public DetectorParticleStatus() {};

    private void setValue(int status) {
        this.status=status;
        this.isForward = ( (int)(Math.abs(this.status)/REGION) & FORWARD ) > 0;
        this.isCentral = ( (int)(Math.abs(this.status)/REGION) & CENTRAL ) > 0;
        this.isTagger  = ( (int)(Math.abs(this.status)/REGION) & TAGGER  ) > 0;
        this.isBand    = ( (int)(Math.abs(this.status)/REGION) & BAND    ) > 0;
        this.nCherenkov    = Math.abs(this.status)%(10*CHERENKOV)/CHERENKOV;
        this.nCalorimeter  = Math.abs(this.status)%(10*CALORIMETER)/CALORIMETER;
        this.nScintillator = Math.abs(this.status)%(10*SCINTILLATOR)/SCINTILLATOR;
    }

    public int getValue()             { return this.status; }
    public boolean isForward()        { return this.isForward; }
    public boolean isCentral()        { return this.isCentral; }
    public boolean isTagger()         { return this.isTagger; }
    public boolean isBAND()           { return this.isBand; }
    public int getScintillatorCount() { return this.nScintillator; }
    public int getCalorimeterCount()  { return this.nCalorimeter; }
    public int getCherenkovCount()    { return this.nCherenkov; }

    public void setTriggerParticle(boolean isTriggerParticle) {
        if (isTriggerParticle) {
            this.setValue(-Math.abs(this.status));
        }
        else {
            this.setValue(Math.abs(this.status));
        }
    }

    public static DetectorParticleStatus create(DetectorParticle p,final double minNpheHtcc,final double minNpheLtcc) {

        int status = 0;
        final int trackType = p.getTrackDetectorID();

        // central:
        if (p.hasHit(DetectorType.BMT)  ||
            p.hasHit(DetectorType.BST)  ||
            p.hasHit(DetectorType.CVT)  ||
            p.hasHit(DetectorType.CTOF) ||
            p.hasHit(DetectorType.CND)  ||
            p.hasHit(DetectorType.RTPC)) {
                status += CENTRAL*REGION;
        }
        else if (DetectorType.getType(trackType)==DetectorType.CVT) {
            status += CENTRAL*REGION;
        }

        // forward:
        if (p.hasHit(DetectorType.DC)     ||
            p.hasHit(DetectorType.FMT)    ||
            p.hasHit(DetectorType.ECAL,1) ||
            p.hasHit(DetectorType.ECAL,4) ||
            p.hasHit(DetectorType.ECAL,7) ||
            p.hasHit(DetectorType.FTOF,1) ||
            p.hasHit(DetectorType.FTOF,2) ||
            p.hasHit(DetectorType.FTOF,3) ||
            p.hasHit(DetectorType.HTCC)   ||
            p.hasHit(DetectorType.LTCC)   ||
            p.hasHit(DetectorType.RICH)) {
            status += FORWARD*REGION;
        }
        else if (DetectorType.getType(trackType)==DetectorType.DC) {
            status += FORWARD*REGION;
        }
        else if (p.hasHit(DetectorType.BAND)) {
            status += BAND*REGION;
        }

        // tagger:
        // need to fix broken response classes inheritance
        /*
        if (p.hasHit(DetectorType.FT)   ||
            p.hasHit(DetectorType.FTCAL)  ||
            p.hasHit(DetectorType.FTHODO) ||
            p.hasHit(DetectorType.FTTRK)) {
            status += TAGGER*REGION;
        }
        */
        if (p.getHit(DetectorType.FTCAL)!=null) status += TAGGER*REGION;

        // scintillators:
        status += SCINTILLATOR*p.countResponses(DetectorType.FTOF);
        status += SCINTILLATOR*p.countResponses(DetectorType.CTOF);
        status += SCINTILLATOR*p.countResponses(DetectorType.FTHODO);
        status += SCINTILLATOR*p.countResponses(DetectorType.BAND);

        // calorimeters:
        status += CALORIMETER*p.countResponses(DetectorType.CND);
        status += CALORIMETER*p.countResponses(DetectorType.ECAL);
        status += CALORIMETER*p.countResponses(DetectorType.FTCAL);

        // cherenkovs:
        if (p.getEnergy(DetectorType.LTCC) > minNpheLtcc) {
            status += CHERENKOV;
        }
        if (p.getEnergy(DetectorType.HTCC) > minNpheHtcc) {
            status += CHERENKOV;
        }
        status += CHERENKOV*p.countResponses(DetectorType.RICH);

        if (p.isTriggerParticle()) {
            status = -status;
        }

        DetectorParticleStatus dps=new DetectorParticleStatus();
        dps.setValue(status);
        return dps;
    }
}
