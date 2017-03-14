/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.geant.Geant4Basic;

/**
 *
 * @author gavalian
 */
public class BSTGeant4Factory {
    public  BSTGeant4Factory(){
        
    }
    
    public Geant4Basic  createDetector(ConstantProvider cp){
        Geant4Basic   mVolume = new Geant4Basic("mother_bst","box",40,40,40);
        int nlayers = 8;
        for(int layer = 0; layer < nlayers; layer++){
            int region = layer/2;
            int nsectors = cp.getInteger("/geometry/bst/region/nsectors", region);
            for(int s = 0; s < nsectors; s++){
                Geant4Basic  bstLayer = this.createLayer(cp, s+1, layer+1);
                mVolume.getChildren().add(bstLayer);
            }
        }
        return mVolume;
    }
    
    public Geant4Basic createLayer(ConstantProvider cp, int sector, int layer){
        Geant4Basic  bstLayer = this.createLayerLocal(cp);
        bstLayer.setName("sector_"+sector+"_layer_"+layer);
        bstLayer.setId(sector,layer,0);
        int  region   = (layer-1)/2;
        int nsectors  = cp.getInteger("/geometry/bst/region/nsectors", region);
        double radius = 0.1*cp.getDouble("/geometry/bst/region/radius", region);
        double zstart = 0.1*cp.getDouble("/geometry/bst/region/zstart", region);
        double zrotation = (sector-1)*Math.toRadians(360.0/nsectors);
        double trX       = radius*Math.cos(zrotation);
        double trY       = radius*Math.sin(zrotation);
        
        bstLayer.setRotation("xyz", 0.0,0.0,zrotation);
        bstLayer.setPosition(trX, trY,zstart);
        return bstLayer;
    }
    
    public Geant4Basic createLayerLocal(ConstantProvider cp){
        double dx = 0.04;
        double dy = 4.0032;
        double dz = 33.5096;
        return new Geant4Basic("sector","box",0.5*dx,0.5*dy,0.5*dz);
    }
}
