/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.fw;

import java.util.List;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public interface IPhysicsCrossSection {
    
    double crossSection(Map<String,Double> values);
    double getObservable(String name, Map<String,Double> values);
    List<String> getObservableList();
    List<IPhysicsObservable> getObservables(Map<String,Double> values);
    
}
