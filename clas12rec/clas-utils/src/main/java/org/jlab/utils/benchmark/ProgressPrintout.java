/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.benchmark;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavalian
 */
public class ProgressPrintout {
    
    private TreeMap<String,Object> items   = new TreeMap<String,Object>();
    private TreeMap<String,Object> itemMax = new TreeMap<String,Object>();
    
    private Long                   previousPrintoutTime = (long) 0;
    private Long                   startPrintoutTime    = (long) 0;
    
    private double   printoutIntervalSeconds = 10.0;
    private String   printoutLeadingString   = ">>>>> progress : ";
    private Integer  numberOfCalls           = 0;
    
    public  ProgressPrintout(){
        this.previousPrintoutTime = System.currentTimeMillis();
        this.startPrintoutTime = System.currentTimeMillis();
    }
    
    public  ProgressPrintout(String name){
        this.printoutLeadingString = name;
        this.previousPrintoutTime = System.currentTimeMillis();
        this.startPrintoutTime = System.currentTimeMillis();
    }
    
    public void setInterval(double interval){
        this.printoutIntervalSeconds = interval;
    }
    
    public String getUpdateString(){
        double totalElapsedTime = (this.previousPrintoutTime-this.startPrintoutTime)*1e-3;
        StringBuilder str = new StringBuilder();
        double averageTime = 1000.0*totalElapsedTime/this.numberOfCalls;
        str.append(String.format("%s (%12d) : ", this.printoutLeadingString,this.numberOfCalls));
        str.append(String.format(" time : %8.2f (sec) =>>> average time = %9.3f msec", totalElapsedTime,averageTime));
        Set<String> keys = this.items.keySet();
        for(String key : keys){
            str.append(this.getItemString(key));
        }
        return str.toString();
    }
    
    public void showStatus(){
        System.out.println("\n\n");
        System.out.println(this.getUpdateString());
        System.out.println("\n\n");        
    }
    
    public void updateStatus(){        
        this.numberOfCalls++;
        Long currentTime   = System.currentTimeMillis();
        Double elapsedTime = (currentTime - this.previousPrintoutTime)*1e-3;
        //System.out.println("elapsed = " + elapsedTime);
        if(elapsedTime>=this.printoutIntervalSeconds){
            this.previousPrintoutTime = System.currentTimeMillis();
            //System.out.println(" passed time ");
            System.out.println(this.getUpdateString());
        }
    }
    
    public void   setAsInteger(String name, Integer value){
        this.items.put(name, value);
    }
    
    public void   setAsDouble(String name, Double value){
        this.items.put(name, value);
    }
    
    public String getItemString(String itemname){
        StringBuilder str = new StringBuilder();
        if(this.items.get(itemname) instanceof Integer){
            str.append(String.format("  %s : %5d",itemname,(Integer)this.items.get(itemname)));
        }
        
        if(this.items.get(itemname) instanceof Double){
            str.append(String.format("  %s : %8.3f",itemname,(Double)this.items.get(itemname)));
        }        
        return str.toString();
    }
    
    public static void main(String[] args){
        ProgressPrintout  progress = new ProgressPrintout();
        int loop = 0;
        while(true){
            loop++;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProgressPrintout.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println("cycle " + loop);
            progress.updateStatus();
        }
    }
}
