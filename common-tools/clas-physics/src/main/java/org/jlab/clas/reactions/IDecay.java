/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reactions;

import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gagikgavalian
 */

public interface IDecay {    
    void init();
    void setDecayParticle(int id);
    void setDecayParticle(String name);
    void setDecayProducts(int pid1, int pid2);
    void setDecayProducts(String name1, String name2);
    void setDecayProducts(int pid1, int pid2, int pid3);
    void setDecayProducts(String name1, String name2, String name3);
    void decayParticles(PhysicsEvent event);
}
