/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.pid;

import org.jlab.clas.detector.*;
import org.jlab.detector.base.DetectorType;


/**
 *
 * @author jnewton
 */
public interface ParticleTiming {
    void CoincidenceCheck(DetectorEvent event, DetectorParticle particle, DetectorType type, int layer);
}
