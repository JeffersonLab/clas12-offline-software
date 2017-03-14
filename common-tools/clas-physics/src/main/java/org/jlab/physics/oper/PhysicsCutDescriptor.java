/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.oper;

import java.util.Map;

/**
 *
 * @author gavalian
 */
public class PhysicsCutDescriptor {
    
    private String operator = "";
    private String variable = "";
    private double variableMin =   0.0;
    private double variableMax = 100.0;
    private String cutName     = "default";
    
    public PhysicsCutDescriptor(String name, String variable, double min, double max){
        this.cutName = name;
        this.setVariable(variable);
        this.setMinMax(min, max);
    }
    
    public final void setName(String name){ this.cutName = name; }
    public String     getName() { return this.cutName;}
    
    public final void setVariable(String var){
        this.variable = var;
    }
    
    public final void setMinMax(double min, double max){
        this.variableMin = min;
        this.variableMax = max;
    }
    
    public double getMin(){ return this.variableMin; }    
    public double getMax(){ return this.variableMax;}
    
    public boolean isValid(Map<String,PhysicsParticleDescriptor> map){
        if(map.containsKey(this.variable)==false) return false;
        double value = map.get(this.variable).getValue();
        return (value>=this.variableMin&&value<=this.variableMax);
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("* %-12s * %-12s * (%12.5f, %12.5f) *\n", 
                this.cutName,this.variable,
                this.variableMin,this.variableMax));
        return str.toString();
    }
}
