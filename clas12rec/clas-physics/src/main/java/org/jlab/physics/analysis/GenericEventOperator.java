/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.analysis;

import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class GenericEventOperator extends EventOperator {
    
    private String operator = "";
    
    public GenericEventOperator(String name, String op, String... vars){
        setName(name);
        operator = op;
        setObservables(vars);
    }
    
    @Override
    public void operate(PhysicsEvent event){
        reset();
        if(getFilter().isValid(event)==false) return;
        Particle part = event.getParticle(operator);
        List<String> obs = observableList();
        for(String o : obs){
            double value = part.getProperty(o);
            observableMap().put(o, value);
        }
    }
}
