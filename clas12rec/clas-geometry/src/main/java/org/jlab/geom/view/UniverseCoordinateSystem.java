/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.view;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Region3D;

/**
 *
 * @author gavalian
 */
public class UniverseCoordinateSystem {
    
    public static int  PROJECTION_XY = 1;
    public static int  PROJECTION_XZ = 2;
    public static int  PROJECTION_YZ = 3;
    
    Region3D  universeWorld = new Region3D();
    Region3D  drawingCanvas = new Region3D();
    
    private double  rotationAngle = 0.0;
    private int     projectionMode = PROJECTION_XY;
    
    public UniverseCoordinateSystem(){
        
    }
    
    public UniverseCoordinateSystem(double x0, double y0, double x1, double y1){
        universeWorld.set(x0, y0, -10.0, x1, y1, 10);
    }
    
    public final void setCanvas(int w, int h){
        drawingCanvas.set(0.0, 0.0, 0.0, w, h, 0);
    }
    
    public final void setWorld(double x0, double y0, double x1, double y1){
        universeWorld.set(x0, y0, -10.0, x1-x0, y1-y0, 20);
    }
    
    public Region3D getWorld(){ return this.universeWorld;}
    public Region3D getCanvas(){ return this.drawingCanvas;}
    
    public double getPointX(double x){
        double fraction = this.universeWorld.getFractionX(x);
        return this.drawingCanvas.getCoordinateX(fraction);
    }
    
    public double getPointY(double y){
        double fraction = this.universeWorld.getFractionY(y);
        return this.drawingCanvas.getCoordinateY(fraction);
    }
    
    public double getPointZ(double z){
        double fraction = this.universeWorld.getFractionZ(z);
        return this.drawingCanvas.getCoordinateZ(fraction);
    }
    
    
    public double getCanvasX(Point3D point){
        
        return 0.0;
    }
    
     public double getCanvasY(Point3D point){
        return 0.0;
    }
}
