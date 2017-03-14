/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.physics.base;

import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public interface IPhysicsCut {
    void init(String format);
    Boolean isValid(PhysicsEvent event);
}
