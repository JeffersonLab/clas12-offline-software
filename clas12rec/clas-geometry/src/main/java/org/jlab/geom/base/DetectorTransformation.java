/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.geom.base;

import java.util.TreeMap;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author gavalian
 */
public class DetectorTransformation {
    private final TreeMap<Integer,Transformation3D>  detectorTransforms =
            new TreeMap<Integer,Transformation3D>();
    
    public DetectorTransformation(){
        
    }
    
    private Integer getHashCode(int sector, int superlayer, int layer){
        return 10000*sector + 1000*superlayer + 10*layer;
    }
    
    public void add(int sector, int superlayer, int layer, Transformation3D trans){
        detectorTransforms.put(this.getHashCode(sector, superlayer, layer), trans);
    }
    
    public Transformation3D get(int sector, int superlayer, int layer){
        Integer hashCode = this.getHashCode(sector, superlayer, layer);
        if(detectorTransforms.containsKey(hashCode)==true){
            return detectorTransforms.get(hashCode);
        }
        return null;
    }
}
