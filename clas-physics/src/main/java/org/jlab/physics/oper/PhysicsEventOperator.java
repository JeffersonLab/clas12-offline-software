/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.oper;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class PhysicsEventOperator {
    
    private String operatorName = "unknown";
    private Double varMinimum = 0.0;
    private Double varMaximum = 0.0;
    private String particleSelector = "[b]";
    private String particleVariable = "mom";
    private Double currentValue     = 0.0;
    private ArrayList<String>  operatorCuts = new ArrayList<String>();
    
    public PhysicsEventOperator(String name, String variable, String particle,
            double min, double max){
        this.set(name, variable, particle);
        this.setLimits(min, max);
    }
    
    public final void set(String name, String variable, String particle){
        this.operatorName = name;
        this.particleVariable = variable;
        this.particleSelector = particle;
    }
    
    public final void setLimits(double min, double max){
        this.varMinimum = min;
        this.varMaximum = max;
    }
    
    public void apply(PhysicsEvent event){
        Particle part = event.getParticle(this.particleSelector);
        this.currentValue = part.get(this.particleVariable);
    }
    
    public boolean isValid(){
        return (this.currentValue>=this.varMinimum&&this.currentValue<this.varMaximum);
    }
    
    public double getMin(){return this.varMinimum;}
    public double getMax(){return this.varMaximum;}
    
    public String getName(){return this.operatorName;}
    
    public String stringValue(){
        StringBuilder str = new StringBuilder();
        str.append(this.operatorName);
        str.append(":");
        str.append(this.particleVariable);
        str.append(":");
        str.append(this.particleSelector);
        str.append(":");
        str.append(this.varMinimum.toString());
        str.append(":");
        str.append(this.varMaximum.toString());
        for(String cut : this.operatorCuts){
            str.append(":");
            str.append(cut);
        }
        return str.toString();
    }
    
    public List<String>  getCuts(){return this.operatorCuts;}
    public double        getValue(){ return this.currentValue;};
    
    @Override
    public String toString(){
        return this.stringValue();
    }
}
