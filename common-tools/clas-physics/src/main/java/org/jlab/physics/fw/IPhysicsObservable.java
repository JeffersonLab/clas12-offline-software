/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.fw;

import java.util.Map;
import org.jlab.physics.base.ParameterSpace;
import org.jlab.physics.base.PhaseSpace;

/**
 *
 * @author gavalian
 */
public interface IPhysicsObservable {
    /*
    Map<String,Double>  getVariables();
    double  getValue();
    String  getName();*/
    double getValue(PhaseSpace space, ParameterSpace params);
}
