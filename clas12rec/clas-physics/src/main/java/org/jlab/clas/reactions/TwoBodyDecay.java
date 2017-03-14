/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reactions;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.physics.Vector3;


/**
 *
 * @author gavalian
 */
public class TwoBodyDecay implements IDecay {
    
    int decayParticleID1;
    int decayParticleID2;
    int parentParticleID;
    LorentzVector decayProd1;
    LorentzVector decayProd2;
    
    public TwoBodyDecay()
    {        
        this.setDecayParticle(111);
        this.setDecayProducts(22,22);
    }
    
    public TwoBodyDecay(int parentID, int childid1, int childid2)
    {
        decayProd1 = new LorentzVector();
        decayProd2 = new LorentzVector();
        this.setDecayParticle(parentID);
        this.setDecayProducts(childid1,childid2);
    }

    public TwoBodyDecay(String parent, String child1, String child2)
    {
        this.setDecayParticle(parent);
        this.setDecayProducts(child1, child2);
    }

    public void init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDecayParticle(int id) {
        this.parentParticleID = id;
    }

    public void setDecayParticle(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDecayProducts(int pid1, int pid2) {
        this.decayParticleID1 = pid1;
        this.decayParticleID2 = pid2;
    }

    public void setDecayProducts(String name1, String name2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDecayProducts(int pid1, int pid2, int pid3) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDecayProducts(String name1, String name2, String name3) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private double getCosThetaRandom(){
        return Math.random()*2.0-1.0;
    }
    
    private double getPhiRandom(){
        return Math.random()*2.0*Math.PI-Math.PI;
    }
    
    public void decayParticles(PhysicsEvent event) {
        double cosTheta = this.getCosThetaRandom();
        double phi      = this.getPhiRandom();

        PDGParticle p1 = PDGDatabase.getParticleById(decayParticleID1);
        PDGParticle p2 = PDGDatabase.getParticleById(decayParticleID2);
    
        Particle mother = event.getParticleByPid(parentParticleID, 0);
        LorentzVector  vector = new LorentzVector();
        vector.copy(mother.vector());
        
        LorentzVector[] vec = DecayKinematics.getDecayParticles(vector,
                p1.mass(), p2.mass(), Math.acos(cosTheta), phi);
        Vector3 vectBoost = vector.boostVector();
        vec[0].boost(vectBoost);
        vec[1].boost(vectBoost);
        decayProd1.copy(vec[0]);
        decayProd2.copy(vec[1]);
        
        int index = event.getParticleIndex(parentParticleID, 0);
        event.removeParticle(index);
        
        event.addParticle(new Particle(decayParticleID1,
                vec[0].px(),  vec[0].py(), vec[0].pz(),
                mother.vertex().x(),mother.vertex().y(),mother.vertex().z()
        ));
        
        event.addParticle(new Particle(decayParticleID2,
                vec[1].px(),  vec[1].py(), vec[1].pz(),
                mother.vertex().x(),mother.vertex().y(),mother.vertex().z()
        ));                
    }

}
