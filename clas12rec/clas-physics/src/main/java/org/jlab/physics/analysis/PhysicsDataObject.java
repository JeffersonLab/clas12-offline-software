/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class PhysicsDataObject {
    
    List<DataObjectDescriptor>   descriptors = new ArrayList<DataObjectDescriptor>();
    
    public PhysicsDataObject(){
        
    }
    
    public void addDescriptor(DataObjectDescriptor desc){
        this.descriptors.add(desc);
    }
    
    public void addDescriptor(String observable, String... cutList){
        DataObjectDescriptor  desc = new DataObjectDescriptor(observable);
        for(String cut : cutList){
            desc.getDataCuts().add(cut);
        }
        addDescriptor(desc);
    }
    
    public class DataObjectDescriptor {
        
        String dataObservable   = "";
        List<String>   dataCuts = new ArrayList<String>();
                
        public DataObjectDescriptor(String obs){
            this.dataObservable = obs;
        }
        
        public String getDataObservable(){
            return this.dataObservable;
        }
        
        public List<String>  getDataCuts(){
            return dataCuts;
        }
        
    }
}
