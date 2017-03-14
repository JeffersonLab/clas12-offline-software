/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.base;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class ParameterSpace {    
    Map<String,DimensionSpace>  parameters = new LinkedHashMap<String,DimensionSpace>();
    public ParameterSpace(){
        
    }
    
    public ParameterSpace addParameter(String name, double min, double max){
        parameters.put(name, new DimensionSpace(name,min,max));
        return this;
    }
    
    public ParameterSpace setValue(String name, double value){
        parameters.get(name).setValue(value);
        return this;
    }
    
    public double getValue(String name){
        return this.parameters.get(name).getValue();
    }
}
