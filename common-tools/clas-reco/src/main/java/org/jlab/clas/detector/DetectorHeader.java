/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

/**
 *
 * @author devita
 */
public class DetectorHeader {

    private int           run = 0;
    private int         event = 0;
    private int       trigger = 0;
    private double     rfTime = 0.0;
    private double  startTime = 0.0;

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

    public int getTrigger() {
        return trigger;
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

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }
}
