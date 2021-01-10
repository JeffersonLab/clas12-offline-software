/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.prim;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.Showable;

/**
 *
 * @author gavalian
 */
public class Sphere3D implements Transformable, Showable{

    private final Point3D sphereCenter = new Point3D(0.0,0.0,0.0);
    private double sphereRadius  = 1.0;
    
    public Sphere3D(){
        
    }
    
    public Sphere3D(double x, double y, double z, double r){
        this.sphereCenter.set(x, y, z);
        this.sphereRadius = r;
    }
    
    @Override
    public void translateXYZ(double dx, double dy, double dz) {
        this.sphereCenter.translateXYZ(dx, dy, dz);
    }
        
    @Override
    public void rotateX(double angle) {
        this.sphereCenter.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        this.sphereCenter.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        this.sphereCenter.rotateZ(angle);
    }

    @Override
    public void show() {
        this.sphereCenter.show();
    }

    
    public Point3D getCenter(){
        return sphereCenter;
    }

    public double getRadius(){
        return sphereRadius;
    }

    public Vector3D getNormal(double x, double y, double z){
        Point3D point = new Point3D(x, y, z);
        return point.vectorFrom(sphereCenter);
    }
    
    public int intersection(final Line3D line, List<Point3D> intersections) {
        intersections.clear();
        double vx = line.end().x() - line.origin().x();
        double vy = line.end().y() - line.origin().y();
        double vz = line.end().z() - line.origin().z();
        double px = line.origin().x();
        double py = line.origin().y();
        double pz = line.origin().z();
        double cx = this.sphereCenter.x();
        double cy = this.sphereCenter.y();
        double cz = this.sphereCenter.z();
        double A = vx*vx + vy*vy + vz*vz;
        double B = 2.0 * (px * vx + py * vy + pz * vz - vx * cx - vy * cy - vz * cz);
        double C = px * px - 2 * px * cx + cx * cx + py * py - 2 * py * cy + cy * cy +
                   pz * pz - 2 * pz * cz + cz * cz - sphereRadius * sphereRadius;
        double D = B * B - 4 * A * C;
        
        if(D < 0){
            return 0;
        }
        
        double t1 = ( -B - Math.sqrt ( D ) ) / ( 2.0 * A );
        intersections.add(new Point3D(
                line.origin().x() *(1.0-t1) + t1*line.end().x(),
                line.origin().y() *(1.0-t1) + t1*line.end().y(),
                line.origin().z() *(1.0-t1) + t1*line.end().z())
                );
        if(D==0) return 1;
        double t2 = ( -B + Math.sqrt( D ) ) / ( 2.0 * A );
        intersections.add(new Point3D(
                line.origin().x() *(1.0-t2) + t2*line.end().x(),
                line.origin().y() *(1.0-t2) + t2*line.end().y(),
                line.origin().z() *(1.0-t2) + t2*line.end().z())
                );
        return 2;
    }
    
    
    public static void main(String[] args){
        Sphere3D  sphere = new Sphere3D(0.,0.,0.,10);
        Line3D    line = new Line3D(-100.0,0.0,0.0,100.0,0.0,0.0);
        
        List<Point3D>  points = new ArrayList<Point3D>();
        
        for(int loop = 0; loop < 500; loop++){
            sphere.intersection(line, points);
        }
        
        for(Point3D p : points){
            p.show();
        }
    }
}
