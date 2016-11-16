/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author gavalian
 */
public class DetectorCollection<T> {
    private IndexedList<T>  collection = new IndexedList<T>(3);
    private String          collectionName = "undefined";
    
     public void setName(String name){
        this.collectionName = name;
    }
    
    public String getName(){
        return this.collectionName;
    }
    
    public void add(int sector, int layer, int comp, T value){
        collection.add(value, new int[]{sector,layer,comp});
                //.put(DetectorDescriptor.generateHashCode(sector, layer, comp), value);
    }
    
    public void add(DetectorDescriptor desc, T value){
        int[] index = new int[]{desc.getSector(), desc.getLayer(), 
                desc.getComponent()};
        collection.add(value, index); 
    }
    
    public void clear(){
        this.collection.clear();
    }
    
    public boolean hasEntry(int sector, int layer, int comp){
        return this.collection.hasItem(sector,layer,comp);
    }
    
    public T  get(int sector, int layer, int comp){
        return this.collection.getItem(sector,layer,comp);
                //.get(DetectorDescriptor.generateHashCode(sector, layer, comp));
    }
}
