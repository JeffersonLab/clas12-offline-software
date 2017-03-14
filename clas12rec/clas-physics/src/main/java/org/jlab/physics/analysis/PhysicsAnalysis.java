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
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class PhysicsAnalysis {
    
    private Map<String,ParticleDescriptor>  descriptors   = new LinkedHashMap<String,ParticleDescriptor>();
    private Map<String,Float>               observableMap = new LinkedHashMap<String,Float>();
    private String                          projectName   = "defaultProject";
    
    public PhysicsAnalysis(){
        
    }
    
    public void addObservable(String descriptor, String observale){
        if(descriptors.containsKey(descriptor)==true){
            descriptors.get(descriptor).addObservable(observale);
        }
    }
    
    public void addDescriptor(ParticleDescriptor desc){
        this.descriptors.put(desc.getName(), desc);
    }
    
    public void addDescriptor(String name, String operator){
        this.addDescriptor(new ParticleDescriptor(name,operator));
    }
    
    public void processEvent(PhysicsEvent event){
        this.observableMap.clear();
        for(Map.Entry<String,ParticleDescriptor>  entry : this.descriptors.entrySet()){
            Particle  particle = event.getParticle(entry.getValue().getOperator());
            for(String property : entry.getValue().observableList()){
                float pvalue = (float) particle.get(property);
                this.observableMap.put(entry.getKey()+":"+property, pvalue);
            }
        }
    }
    
    public Map<String,Float>  getObservables(){
        return this.observableMap;
    }
    
    public String toTupleString(){
        StringBuilder str = new StringBuilder();
        for(Map.Entry<String,Float> entry : this.observableMap.entrySet()){
            str.append(String.format(" %f ", entry.getValue()));            
        }
        return str.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(Map.Entry<String,ParticleDescriptor>  entry : this.descriptors.entrySet()){
            str.append(entry.getValue().toString());
            str.append("\n");
        }
        return str.toString();
    }
    
    public static class ParticleDescriptor {
        
        String descriptorName = "";
        String descriptorOperator = "";
        List<String>   observables = new ArrayList<String>();
        
        public ParticleDescriptor(String name, String operator){
            descriptorName = name;
            descriptorOperator = operator;
        }
        
        public String getName(){
            return descriptorName;
        }
        
        public String getOperator(){
            return this.descriptorOperator;
        }
        
        public void addObservable(String ob){
            this.observables.add(ob);
        }
        
        public List<String> observableList(){
            return this.observables;
        }
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append(String.format("%12s : %24s ", getName(),getOperator()));
            for(String obs : observables){
                str.append(String.format(" : %s ", obs));
            }
            return str.toString();
        }
    }
    
}
