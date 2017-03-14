/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.base;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public abstract class EventOperation {
    
    public List<String>    operators  = new ArrayList<String>();
    public List<Particle>  particles  = new ArrayList<Particle>();
    public List<Double>    properties = new ArrayList<Double>();
    
    public EventOperation(){
        
    }
    
    public void init(String... oper){
        this.operators.clear();
        for(String item : oper){
            operators.add(item);
        }
    }
    
    abstract void  processEvent(PhysicsEvent event);
    
    public void initParticles(PhysicsEvent event){
        
    }
    
    public double getProperty(){
        return 0.0;
    }
                
}
