/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.pdg.PDGDatabase;

/**
 *
 * @author gavalian
 */
public class ParticleFinder {
    private final List<String>  particleOperations = new ArrayList<String>();
    
    public ParticleFinder(){
        
    }
    
    public void generate(PhysicsEvent event){
        particleOperations.clear();
        int nphotons = event.countByPid(22);
        if(nphotons<2) return;
        for(int i = 0; i < nphotons; i++){
            for(int k = 0; k < nphotons;k++){
                if(i!=k){
                    particleOperations.add(String.format("[22,%d]+[22,%d]", i,k));
                }
            }
        }
    }
    
    public Particle getPion(PhysicsEvent event){
        this.generate(event);
        
        double mass     = PDGDatabase.getParticleById(111).mass();
        double distance = 1.0;
        Particle pion = null;
        for(String selection : particleOperations){
            Particle candidate = event.getParticle(selection);
            if(Math.abs(candidate.vector().mass()-mass) < distance){
                distance = Math.abs(candidate.vector().mass()-mass);
                pion     = candidate;
            }
        }
        return pion;
    }
}
