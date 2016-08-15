/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.swim;

import org.jlab.clas.fastmc.Clas12FastMC;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class SwimEngine extends ReconstructionEngine {
    
    GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
    Clas12FastMC    fastMC = new Clas12FastMC(-1.0,1.0);
    
    public SwimEngine(){
        super("SWIMMER","gavalian","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        PhysicsEvent genEvent = fitter.getGeneratedEvent(event);
        int count = genEvent.count();
        for(int j = 0; j < 15; j++){

            for(int i = 0; i < count; i ++){
                Particle  particle = genEvent.getParticle(i);
                boolean status = fastMC.checkParticle(particle);
            }
        }
        //System.out.println(genEvent.toLundString());
        return true;
    }

    @Override
    public boolean init() {
        return true;
    }
    
}
