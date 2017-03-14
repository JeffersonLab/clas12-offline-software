/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    
    public Set<Long>  getKeys(){
        return this.collection.getMap().keySet();
    }
    
    public T getObjectForKey(Long key){
        return this.collection.getMap().get(key);
    }
    /**
     * Returns a list of all values in the collection
     * @return List of collection values
     */
    public List<T> getList(){
        Collection<T> vc = this.collection.getMap().values();
        List<T>  list = new ArrayList<T>();
        for(T c : vc){
            list.add(c);
        }
        return list;
    }
    /**
     * Returns Set of sectors defined in the map
     * @return 
     */
    public Set<Integer> getSectors(){
        Set<Long>  list = this.collection.getMap().keySet();
        Set<Integer>  sectors = new HashSet<Integer>();
                
        for(Long item : list){
            int sect = IndexedList.IndexGenerator.getIndex(item, 0);
            sectors.add(sect);
        }
        return sectors;
    }
    
     /**
     * returns Set of layers for given sector.
     * @param sector
     * @return 
     */
    public Set<Integer> getLayers(int sector){
        Set<Long>  list = this.collection.getMap().keySet();
        Set<Integer>  layers = new HashSet<Integer>();
        for(Long item : list){
            int sect = IndexedList.IndexGenerator.getIndex(item, 0);
            if(sect==sector){
                int lay = IndexedList.IndexGenerator.getIndex(item, 1);
                layers.add(lay);
            }
        }
        return layers;
    }
    /**
     * returns component set for given sector and layer
     * @param sector
     * @param layer
     * @return 
     */
    public Set<Integer>  getComponents(int sector, int layer){
        Set<Long>  list = this.collection.getMap().keySet();
        Set<Integer>  components = new HashSet<Integer>();
        for(Long item : list){
            int sect = IndexedList.IndexGenerator.getIndex(item, 0);
            int lay = IndexedList.IndexGenerator.getIndex(item, 1);
            if(sect==sector&&lay==layer){
                int comp = IndexedList.IndexGenerator.getIndex(item, 2);
                components.add(comp);
            }
        }
        return components; 
    }
    
}
