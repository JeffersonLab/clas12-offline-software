/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.prim;

/**
 *
 * @author gavalian
 */
public class Region3D {
    private final Point3D  regionOrigin    = new Point3D();
    private final Vector3D regionDimension = new Vector3D();
    
    public Region3D(){
        
    }
    
    public Region3D(double x, double y, double z, double xw, double yw, double zw){
        this.regionOrigin.set(x, y, z);
        this.regionDimension.setXYZ(xw, yw, zw);
    }
    
    public void addPoint(double x, double y, double z){
        
        if(x<this.regionOrigin.x()){
            this.regionOrigin.setX(x);
            this.regionDimension.setX(Math.abs(x-this.regionOrigin.x()) + this.regionDimension.x());
        }
        if(x>(this.regionOrigin.x()+this.regionDimension.x())){
            this.regionDimension.setX(this.regionDimension.x()+Math.abs(x-(this.regionOrigin.x()+this.regionDimension.x())));
        }
        if(y<this.regionOrigin.y()){
            this.regionOrigin.setY(y);
            this.regionDimension.setY(Math.abs(y-this.regionOrigin.y()) + this.regionDimension.y());
        }
        if(y>(this.regionOrigin.y()+this.regionDimension.y())){
            this.regionDimension.setY(this.regionDimension.y()+Math.abs(y-(this.regionOrigin.y()+this.regionDimension.y())));
        }
        if(z<this.regionOrigin.z()){
            this.regionOrigin.setZ(z);
            this.regionDimension.setY(Math.abs(z-this.regionOrigin.z()) + this.regionDimension.z());
        }
        if(z>(this.regionOrigin.z()+this.regionDimension.z())){
            this.regionDimension.setZ(this.regionDimension.z()+Math.abs(z-(this.regionOrigin.z()+this.regionDimension.z())));
        }
    }
    
    public void addPoint(Point3D point){
        this.addPoint(point.x(),point.y(),point.z());
    }
    
    public Point3D getOrigin(){
        return this.regionOrigin;
    }
    
    public Vector3D getDimension(){
        return this.regionDimension;
    }
    
    public void show(){
        this.regionOrigin.show();
        this.regionDimension.show();
    }
}
