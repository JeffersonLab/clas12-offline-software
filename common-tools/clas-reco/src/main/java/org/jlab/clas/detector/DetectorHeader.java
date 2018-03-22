package org.jlab.clas.detector;

/**
 *
 * @author devita
 */
public class DetectorHeader {

    private int           run = 0;
    private int         event = 0;
    private long      trigger = 0;
    private double     rfTime = 0.0;
    private double  startTime = -1000.0;
    private byte     helicity = -99;
    private float beamChargeGated = 0;
    private float    livetime = -1;
    private short eventCategory = 0;

    public DetectorHeader() {
    }

    public int getEvent() {
        return event;
    }

    public double getRfTime(){
        return rfTime;
    }

    public int getRun() {
        return run;
    }

    public double getStartTime(){
        return this.startTime;
    }

    public long getTrigger() {
        return trigger;
    }

    public byte getHelicity() {
	 return helicity;
    }

    public float getLivetime() {
        return livetime;
    }

    public float getBeamChargeGated() {
	    return beamChargeGated;
    }
    
    public short getEventCategory() {
        return eventCategory;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public void setRfTime(double rf){
        this.rfTime = rf;
    }

    public void setRun(int run) {
        this.run = run;
    }

   public void setStartTime(double starttime){
        this.startTime = starttime;
    }

    public void setTrigger(long trigger) {
        this.trigger = trigger;
    }

    public void setHelicity(byte helicity) {
        this.helicity = helicity;
    }

    public void setBeamChargeGated(float bcg) {
	 this.beamChargeGated = bcg;   
    }

    public void setLiveTime(float livetime) {
	 this.livetime = livetime;
    }

    public void setEventCategory(short evcat) {
        this.eventCategory = evcat;
    }
}
