/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.fastmc;

import org.jlab.clas.physics.Particle;

/**
 *
 * @author gavalian
 */
public interface IParticleResolution {
	void apply(Particle p, double torus_scale, double solenoid_scale);
}
