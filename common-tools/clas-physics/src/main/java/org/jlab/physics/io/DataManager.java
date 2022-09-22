/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.io;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.clas.physics.*;

/**
 *
 * @author gavalian
 */
public class DataManager {
    public DataManager(){
        
    }
    
    public static PhysicsEvent getPhysicsEvent(double beam, Bank eventBank){
        
        PhysicsEvent physEvent = new PhysicsEvent();
        physEvent.setBeamParticle(new Particle(11,0.0,0.0,beam));
        
        int nrows = eventBank.getRows();
        
        for(int i = 0; i < nrows; i++){
            int pid = eventBank.getInt("pid", i);
            Particle p = new Particle();
            if(pid!=0){
                p.initParticle(pid,
                        eventBank.getFloat("px",i), 
                        eventBank.getFloat("py",i), 
                        eventBank.getFloat("pz",i),
                        eventBank.getFloat("vx",i), 
                        eventBank.getFloat("vy",i), 
                        eventBank.getFloat("vz",i)
                );
            } else {
                p.initParticleWithPidMassSquare(pid, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            }

            physEvent.addParticle(p);
        }
        return physEvent;
    }
}
