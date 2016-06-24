/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.oper;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class PhysicsParticleDescriptor {
    private String  particleString   = "[b]";
    private String  particleVariable = "mass";
    private double  descriptorValue  = 0.0;
    private String  descName         = "unknown";
    
    public PhysicsParticleDescriptor(String name,String particle, String variable){
        this.descName = name;
        this.setParticle(particle);
        this.setVariable(variable);
    }
    
    public final void setName(String name) { this.descName = name; }
    public String getName() { return this.descName;}
    
    public final void setParticle(String part){
        this.particleString = part;
    }
    
    public final void setVariable(String var){
        this.particleVariable = var;
    }
    
    public void   applyEvent(PhysicsEvent event){
        Particle part = event.getParticle(this.particleString);
        if(part!=null){
            this.descriptorValue = part.get(this.particleVariable);
        }
    }
    
    public String getParticle(){ return this.particleString; }
    public String getVariable(){ return this.particleVariable; }
    public double getValue()   { return this.descriptorValue; }
     
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("* %-12s * %-12s * %-45s *\n", 
                this.descName, this.particleVariable,this.particleString));
        return str.toString();
    }
}
