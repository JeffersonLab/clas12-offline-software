/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.Polygon;
import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Path3D;

/**
 *
 * @author gavalian
 */
public class DetectorShape3D {
    
    public int SECTOR = 0;
    public int LAYER  = 0;
    public int COMPONENT = 0;
    public Boolean isActive = false;
    
    public DetectorId  detectorType = DetectorId.UNDEFINED;
    //public Path3D     shapePolygon = new Path3D();
    public Polygon     shapePolygon = new Polygon();
    public Path3D      shapePath    = new Path3D();
    
    public DetectorShape3D(){
        
    }
    
    public DetectorShape3D(DetectorId type, int sector, int layer, int component){
        this.detectorType = type;
        this.SECTOR = sector;
        this.LAYER  = layer;
        this.COMPONENT = component;
    }
    
    public void addPathPoint(double x, double y){
        this.shapePath.addPoint(x,y,0.0);
    }
    
    public Path3D getPath(){ return this.shapePath;}
    
    public void addPoint(double x, double y){
        this.shapePolygon.addPoint((int) x, (int) y);
    }
    
    public void setPoints(double[] x, double[] y){
        shapePolygon.reset();
        for(int loop = 0; loop < x.length; loop++){
            this.addPoint(x[loop], y[loop]);
        }
    }
    
    public int  getMinX(){
        if(this.shapePolygon.npoints>0){
            int min = this.shapePolygon.xpoints[0];
            for(int xp : this.shapePolygon.xpoints)
            {
                if(xp<min) min = xp;                
            }
            return min;
        }
        return 0;
    }
    
    public int getMaxX(){
        if(this.shapePolygon.npoints>0){
            int max = this.shapePolygon.xpoints[0];
            for(int xp : this.shapePolygon.xpoints)
            {
                if(xp>max) max = xp;                
            }
            return max;
        }
        return 0;
    }
    
    public int  getMinY(){
        if(this.shapePolygon.npoints>0){
            int min = this.shapePolygon.ypoints[0];
            for(int yp : this.shapePolygon.ypoints)
            {
                if(yp<min) min = yp;                
            }
            return min;
        }
        return 0;
    }
    
    public int getMaxY(){
        if(this.shapePolygon.npoints>0){
            int max = this.shapePolygon.ypoints[0];
            for(int yp : this.shapePolygon.ypoints)
            {
                if(yp>max) max = yp;                
            }
            return max;
        }
        return 0;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("S/L/C[%5d %5d %5d ] NPOINTS = %5d\n", 
                this.SECTOR, this.LAYER,this.COMPONENT, this.shapePolygon.npoints));
        for(int loop = 0; loop < this.shapePolygon.npoints; loop++){
            str.append(String.format("\t POINT = %5d  (%5d %5d) \n",loop,
                    this.shapePolygon.xpoints[loop] ,this.shapePolygon.ypoints[loop] ));
        }
        return str.toString();
    }
}
