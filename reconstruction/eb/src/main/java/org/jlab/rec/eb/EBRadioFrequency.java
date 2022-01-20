package org.jlab.rec.eb;

import java.util.ArrayList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 *
 * @author devita
 * @author baltzell
 */
public class EBRadioFrequency {

    private final EBCCDBConstants ccdb;
    private final int debugMode = 0;
    
    private final ArrayList<rfSignal> rfSignals = new ArrayList<>();
    private double rfTime = -100;
   
    public EBRadioFrequency(EBCCDBConstants ccdb) {
        this.ccdb=ccdb;
    }

    /**
     * New-style start time, based on one particle's vertex time and position to
     * determine the beam bunch and RF correction (the trigger particle), and
     * another particle's z-vertex's (to correct for systematic z-vertex difference
     * with the trigger particle).  All times/distances are in units of ns/cm.
     * @param trigger the trigger particle
     * @param type type of timing detector to use for trigger particle
     * @param layer layer of timing detector to use for trigger particle
     * @param vz the z-vertex of the non-trigger particle 
     * @return start time corrected for RF and z-vertex
     */
    public double  getStartTime(DetectorParticle trigger,final DetectorType type,final int layer,final double vz) {

        final double rfPeriod = this.ccdb.getDouble(EBCCDBEnum.RF_BUCKET_LENGTH);

        // The RF calibration is relative to target center:
        final double zTarget = this.ccdb.getDouble(EBCCDBEnum.TARGET_POSITION);

        // The trigger particles' vertex info:
        final double tVertex = trigger.getVertexTime(type,layer,trigger.getPid());
        final double dzTargetVertex = zTarget - trigger.vertex().z();

        // The RF time at the trigger particle's vertex:
        // (Note, this is shifted forward by a large integer number of RF periods,
        // to accommadate the modulus in the next step, as it was done in CLAS6.
        // Probably this should be "simplified" with Math.IEEERemainder.)
        final double tVertexRF = - tVertex - dzTargetVertex / PhysicsConstants.speedOfLight()
                + this.rfTime + this.ccdb.getDouble(EBCCDBEnum.RF_OFFSET)
                + (EBConstants.RF_LARGE_INTEGER+0.5)*rfPeriod;

        // The relative, RF-beam-bucket-centering correction, based only on the
        // trigger particle:
        final double dtRF = tVertexRF % rfPeriod - rfPeriod/2;

        // The RF-corrected event start time, based only on the trigger particle:
        final double startTime = tVertex + dtRF;

        // The RF-corrected event start time, now corrected for beam flight time
        // between the trigger and non-trigger particles' z-vertices:
        return startTime + (vz - trigger.vertex().z()) / PhysicsConstants.speedOfLight();
    }

    /**
     * "traditional" start time, based only on one particle
     * @param p the particle with which to determine the start time
     * @param type type of detector to use for timing info
     * @param layer layer of detector to use for timing info
     * @return RF/vz-corrected start time
     */
    public double  getStartTime(DetectorParticle p,final DetectorType type,final int layer) {
        return this.getStartTime(p,type,layer,p.vertex().z());
    }
    
    public double getTime(DataEvent event) {
        this.processEvent(event);
        return rfTime;
    }
    
    public void processEvent(DataEvent event) {
        final int rfId=ccdb.getInteger(EBCCDBEnum.RF_ID);
        // correct for CAEN TDC jitter
        double triggerPhase = 0;
        if(event.hasBank("RUN::config")) {
            DataBank bank = event.getBank("RUN::config");
            double period = ccdb.getDouble(EBCCDBEnum.RF_JITTER_PERIOD);
            int    phase  = ccdb.getInteger(EBCCDBEnum.RF_JITTER_PHASE);
            int    cycles = ccdb.getInteger(EBCCDBEnum.RF_JITTER_CYCLES);
            long   timeStamp = bank.getLong("timestamp", 0);
            if(cycles>0 && timeStamp!=-1) triggerPhase=period*((timeStamp+phase)%cycles);
        
        }
        // if RF::tdc exists (data), calculate rfTime from scratch
        if(event.hasBank("RF::tdc")) {
            DataBank bank = event.getBank("RF::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int      comp = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
                if(order == 2) {
                    int ind = this.hasSignal(comp);
                    if(ind>=0) {
                        this.rfSignals.get(ind).add(TDC);
                    }
                    else {
                        rfSignal newSignal = new rfSignal(comp, triggerPhase);
                        newSignal.add(TDC);
                        this.rfSignals.add(newSignal);
                    }
                }
            }
            if(debugMode>0) bank.show();
            // always rewrite RF bank
            if(event.hasBank("RUN::rf")) event.removeBank("RUN::rf");
            DataBank bankOut = event.createBank("RUN::rf",this.rfSignals.size());
            for(int i =0; i< this.rfSignals.size(); i++) {
                bankOut.setShort("id",   i, (short) this.rfSignals.get(i).getId());
                bankOut.setFloat("time", i, (float) this.rfSignals.get(i).getTime());
                if(debugMode>0) this.rfSignals.get(i).print();
            }
            event.appendBank(bankOut);
            int index = this.hasSignal(rfId);
            if(index>=0) rfTime=this.rfSignals.get(index).getTime();           
        }  
        else if(event.hasBank("RUN::rf")) {
            DataBank bank = event.getBank("RUN::rf");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                if(bank.getShort("id", i)==rfId) rfTime=bank.getFloat("time", i);
                }
            }
        
    }
    
    
    private int hasSignal(int id) {
        int index = -1;
        for(int i=0; i<rfSignals.size(); i++)  {
            if(rfSignals.get(i).getId()==id) index = i;
        }
        return index;
    }
    
    private class rfSignal {
        private int _id;
        private double _jitter;
        private ArrayList<Integer> _tdc = new ArrayList<>();
        private ArrayList<Double> _time = new ArrayList<>();

        public rfSignal(int id, double jitter) {
            _id=id;
            _jitter=jitter;
        }

        public boolean add(int tdc) {
            final double rfTdc2Time = ccdb.getDouble(EBCCDBEnum.RF_TDC2TIME);
            final int rfCycles = ccdb.getInteger(EBCCDBEnum.RF_CYCLES);
            final double rfOffset = ccdb.getDouble(EBCCDBEnum.RF_OFFSET);
            final double rfBucketLength = ccdb.getDouble(EBCCDBEnum.RF_BUCKET_LENGTH); 
            boolean skip = false;
            double time = (tdc*rfTdc2Time) % (rfCycles*rfBucketLength) - this.getJitter();
            // check if new TDC value compared to previous one is consistent with 80*2.004 ns interval
            if(this._tdc.size()>0) {               
                int deltaTDC = tdc - this._tdc.get(this._tdc.size()-1);
                if(deltaTDC>((double) (rfCycles+1)*rfBucketLength)/rfTdc2Time) { // allow for an extra 2.004 ns
                    skip = true;
                }
            }
            this._tdc.add(tdc);
            this._time.add(time);
            return skip;
        }
        
        public int getId() {
            return _id;
        }

        public void setId(int _id) {
            this._id = _id;
        }

        public double getJitter() {
            return _jitter;
        }

        public void setJitter(double _jitter) {
            this._jitter = _jitter;
        }

        public double getTime() {
            double time = 0;
            for(int i=0; i<this._time.size(); i++) {
                time += this._time.get(i);
            }
            time /= this._time.size();
            return time;
        }
        
        public double getRMS() {
            double time2 = 0;
            for(int i=0; i<this._time.size(); i++) {
                time2 += Math.pow(this._time.get(i),2.);
            }
            time2 /= this._time.size();
            double time = this.getTime();
            double rms = Math.sqrt(time2-time*time);
            return rms;
        }
        
        public void print(){
            System.out.println("RF signal with ID = " + this._id + " N. Hits = " + this._tdc.size() + " and Time = (" + this.getTime() + "+/-" + this.getRMS() + ") ns");
            for(int i=0; i<this._tdc.size(); i++) {
                int dtdc = 0;
                if(i<this._tdc.size()-1) dtdc = this._tdc.get(i+1)-this._tdc.get(i);
                System.out.println("\t" + i + "\t" + this._tdc.get(i) + "\t" + dtdc + "\t" + (this._tdc.get(i)%6840) + "\t"  + this._time.get(i));
            }
        }
    }
}
