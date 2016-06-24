/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import org.jlab.clas.reactions.DecayKinematics;

/**
 *
 * @author gavalian
 */
public class PhysicsKinematics {
    
    public static Particle getParticleUnBoosted(Particle frame, Particle part){
        Vector3 boost = frame.vector().boostVector();
        boost.negative();
        LorentzVector  vecL = new LorentzVector();
        vecL.copy(part.vector());
        vecL.boost(boost);
        Vector3 partBoosted = new Vector3();
        partBoosted.copy(vecL.vect());
        Vector3 partInFrame = DecayKinematics.vectorToFrame(frame.vector().vect(), partBoosted);
        Particle result = new Particle();
        result.setParticleWithMass(part.mass(), (byte) part.charge(), 
                partInFrame.x(),partInFrame.y(),partInFrame.z(),
                part.vertex().x(),part.vertex().y(),part.vertex().z());
        return result;
    }
    
    public static Particle getParticleUnBoosted(Particle frame, Particle part, Boolean isBoosted){
        Vector3 boost = frame.vector().boostVector();
        boost.negative();
        LorentzVector  vecL = new LorentzVector();
        vecL.copy(part.vector());
        if(isBoosted==true){
            vecL.boost(boost);
        }
        Vector3 partBoosted = new Vector3();
        partBoosted.copy(vecL.vect());
        Vector3 partInFrame = DecayKinematics.vectorToFrame(frame.vector().vect(), partBoosted);
        Particle result = new Particle();
        result.setParticleWithMass(part.mass(), (byte) part.charge(), 
                partInFrame.x(),partInFrame.y(),partInFrame.z(),
                part.vertex().x(),part.vertex().y(),part.vertex().z());
        return result;
    }
    
}
