/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.utils.benchmark;

/**
 *
 * @author gavalian
 */
public class BenchmarkTimer {
    
    private String timerName = "generic";
    
    private long   lastStartTime = 0;
    private long   totalTime = 0;
    private long   timeAtResume = 0;
    private int    numberOfCalls = 0;
    private Boolean isPaused = true;
    
    public BenchmarkTimer(){
        
    }
    
    public BenchmarkTimer(String name){
        timerName = name;
    }
    
    public String getName(){
        return timerName;
    }
    
    public void resume(){
        if(isPaused == true){
            timeAtResume = System.nanoTime();
            isPaused = false;
        }
    }
    
    public void pause(){
        if(isPaused==false){
            long timeAtPause = System.nanoTime();
            totalTime += (timeAtPause - timeAtResume);
            numberOfCalls++;
            isPaused = true;
        }
    }
    
    public void reset(){
        lastStartTime = 0;
        totalTime = 0;
        timeAtResume = 0;
        numberOfCalls = 0;
        isPaused = true;
    }
    
    public double getMiliseconds(){
        return totalTime/(1.0e6);
    }
    
    public double getSeconds(){
        return totalTime/(1.0e9);
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        double timePerCall = 0.0;
        if(numberOfCalls!=0) timePerCall = this.getMiliseconds()/numberOfCalls;
        str.append(String.format("TIMER (%-12s) : N Calls %12d,  Total Time  = %12.2f sec,  Unit Time = %12.3f msec",
                this.getName(),numberOfCalls,this.getSeconds(),timePerCall));
        return str.toString();
    }
}
