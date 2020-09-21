package org.jlab.detector.scalers;

import org.jlab.utils.groups.IndexedTable;

public class DaqScaler {

    protected static final String RAWBANKNAME="RAW::scaler";
    protected static final int CRATE=64;

    protected double clockFreq=1;   // Hz
    protected long fcup=-1;         // counts
    protected long clock=-1;        // counts
    protected long slm=-1;          // counts
    protected long gatedFcup=-1;    // counts
    protected long gatedClock=-1;   // counts
    protected long gatedSlm=-1;     // counts

    public long   getClock()       { return this.clock; }
    public long   getFcup()        { return this.fcup; }
    public long   getSlm()         { return this.slm; }
    public long   getGatedClock()  { return this.gatedClock; }
    public long   getGatedFcup()   { return this.gatedFcup; }
    public long   getGatedSlm()    { return this.gatedSlm; }
    public double getClockSeconds()   { return (double)this.clock / this.clockFreq; }
    public double getGatedClockSeconds() { return (double)this.gatedClock / this.clockFreq; }
       
    // not really "raw" anymore:
    protected double beamCharge=0;
    protected double beamChargeGated=0;
    protected double beamChargeSLM=0;
    protected double beamChargeGatedSLM=0;
    protected double livetime=0;
    protected void setBeamCharge(double q) { this.beamCharge=q; }
    protected void setBeamChargeGated(double q) { this.beamChargeGated=q; }
    protected void setBeamChargeSLM(double q) { this.beamChargeSLM=q; }
    protected void setBeamChargeGatedSLM(double q) { this.beamChargeGatedSLM=q; }
    protected void setLivetime(double l) { this.livetime=l; }
    public double getBeamCharge() { return beamCharge; }
    public double getBeamChargeGated() { return beamChargeGated; }
    public double getLivetime()   { return livetime; }
    public double getBeamChargeSLM() { return beamChargeSLM; }
    public double getBeamChargeGatedSLM() { return beamChargeGatedSLM; }

    protected final void calibrate(IndexedTable fcupTable,IndexedTable slmTable,double seconds) {
        if (this.clock > 0 && this.gatedClock>0) {
                
            final double fcup_slope  = fcupTable.getDoubleValue("slope",0,0,0);  // Hz/nA
            final double fcup_offset = fcupTable.getDoubleValue("offset",0,0,0); // Hz
            final double fcup_atten  = fcupTable.getDoubleValue("atten",0,0,0);  // attenuation
            final double slm_slope   = slmTable.getDoubleValue("slope",0,0,0);   // Hz/nA
            final double slm_offset  = slmTable.getDoubleValue("offset",0,0,0);  // Hz
            final double slm_atten   = slmTable.getDoubleValue("atten",0,0,0);   // attenuation

            double q,qg;

            q  = this.slm      - slm_offset * seconds;
            qg = this.gatedSlm - slm_offset * seconds;
            this.beamChargeSLM = q * slm_atten / slm_slope;
            this.beamChargeGatedSLM = qg * slm_atten / slm_slope;
            this.livetime = (double)this.gatedClock / this.clock;

            q  = this.fcup      - fcup_offset * seconds;
            qg = this.gatedFcup - fcup_offset * seconds;
            this.beamCharge = q * fcup_atten / fcup_slope;
            this.beamChargeGated = qg * fcup_atten / fcup_slope;
        }
    }
        
    protected final void calibrate(IndexedTable fcupTable,IndexedTable slmTable) {
        this.calibrate(fcupTable,slmTable,((double)this.gatedClock)/this.clockFreq);
    }
}
