/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.oper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class PhysicsHistogramDescriptor {
    private String  histName = "";
    private int     numberOfBins = 100;
    private double  histMin      = 0.0;
    private double  histMax      = 1.0;
    private String  variable     = "a";
    private List<String>  cutList = new ArrayList<String>();
    
    public PhysicsHistogramDescriptor(String name, int bins, double min, double max
            ,String var, String cuts){
        this.histName = name;
        this.numberOfBins = bins;
        this.histMin = min;
        this.histMax = max;
        this.variable = var;
        this.setCuts(cuts);
    }
    
    public final void setCuts(String cuts){
        this.cutList.clear();
        String[] tokens = cuts.split(":");
        for(String item : tokens){
            this.cutList.add(item);
        }
    }
    
    public void addCut(String cut){
        this.cutList.add(cut);
    }
    
    public List<String>  getCuts() { return this.cutList;}
    public String getName(){ return this.histName;}
    public String getVariable(){ return this.variable;}
    public double getMin(){ return this.histMin;}
    public double getMax(){ return this.histMax;}
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("* %-12s * %-12s * (%5d, %12.5f, %12.5f) * NCUTS = %d", 
                this.histName,this.variable,this.numberOfBins,
                this.histMin,this.histMax,this.cutList.size()));
        for(String item : this.cutList){
            str.append(item);
            str.append("&");
        }
        str.append(" *\n");
        return str.toString();
    }
}
