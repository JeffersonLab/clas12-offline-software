/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.clas.physics.EventFilter;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class EventOperator {
    
    private String operatorName = "EV";
    private final Map<String,Number> operatorObservables = new LinkedHashMap<String,Number>();
    private final EventFilter  eventFilter = new EventFilter();
    
    public EventOperator(){
        
    }
    
    public EventFilter getFilter(){return eventFilter;}
    
    public String getName(){ return operatorName; }
    public EventOperator setName(String name){operatorName = name;return this;}
    public EventOperator setFilter(String filter){eventFilter.setFilter(filter);return this;}
    
    public EventOperator setObservables(String... obsnames){
        operatorObservables.clear();
        for(String n : obsnames){
            operatorObservables.put(n, (Double) 0.0);
        }
        return this;
    }
    
    public void reset(){
        Set<String> keys = this.operatorObservables.keySet();
        for(String key : keys) this.operatorObservables.put(key, (Double) 0.0);
    }
    
    public void operate(PhysicsEvent event){
        
    }
    
    public Map<String,Number> observableMap(){
        return this.operatorObservables;
    }
    
    public List<String> observableList(){
        Set<String> oset = operatorObservables.keySet();
        List<String> names = new ArrayList<String>();
        for(String on : oset){
            names.add(on);
        }
        return names;
    }
    
    @Override
    public String toString(){
         StringBuilder str = new StringBuilder();
         List<String> names = observableList();
         str.append(String.format(" %12s :", getName()));
         for(String n : names){
             str.append(String.format("\t %s : ", n));
             str.append(this.operatorObservables.get(n));
             str.append("\n");
         }
         return str.toString();
    }
}
