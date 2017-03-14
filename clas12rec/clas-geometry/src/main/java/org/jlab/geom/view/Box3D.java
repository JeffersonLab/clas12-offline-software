/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.view;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformable;

/**
 *
 * @author gavalian
 */
public class Box3D implements Transformable {
    private List<Point3D> points = new ArrayList<Point3D>();
    private List<Line3D>  lines  = new ArrayList<Line3D>();
    
    public Box3D(double xdim, double ydim, double zdim){
        points.add(new Point3D(-xdim/2.0,-ydim/2.0,-zdim/2.0));
        points.add(new Point3D(-xdim/2.0, ydim/2.0,-zdim/2.0));
        points.add(new Point3D( xdim/2.0, ydim/2.0,-zdim/2.0));
        points.add(new Point3D( xdim/2.0,-ydim/2.0,-zdim/2.0));
        
        points.add(new Point3D(-xdim/2.0,-ydim/2.0, zdim/2.0));
        points.add(new Point3D(-xdim/2.0, ydim/2.0, zdim/2.0));
        points.add(new Point3D( xdim/2.0, ydim/2.0, zdim/2.0));
        points.add(new Point3D( xdim/2.0,-ydim/2.0, zdim/2.0));
        
        lines.add(new Line3D(points.get(0),points.get(1)));
        lines.add(new Line3D(points.get(1),points.get(2)));
        lines.add(new Line3D(points.get(2),points.get(3)));
        lines.add(new Line3D(points.get(3),points.get(0)));

        lines.add(new Line3D(points.get(4),points.get(5)));
        lines.add(new Line3D(points.get(5),points.get(6)));
        lines.add(new Line3D(points.get(6),points.get(7)));
        lines.add(new Line3D(points.get(7),points.get(4)));
        
        lines.add(new Line3D(points.get(0),points.get(4)));
        lines.add(new Line3D(points.get(1),points.get(5)));
        lines.add(new Line3D(points.get(2),points.get(6)));
        lines.add(new Line3D(points.get(3),points.get(7)));
        
    }
    
    public void translateXYZ(double dx, double dy, double dz) {
        for(Point3D point : points){
            point.translateXYZ(dx, dy, dz);
        }
        for(Line3D line : lines){
            line.translateXYZ(dx, dy, dz);
        }
    }

    public void rotateX(double angle) {
        for(Point3D point : points){
            point.rotateX(angle);
        }
        for(Line3D line : lines){
            line.rotateX(angle);
        }
    }

    public void rotateY(double angle) {
        for(Point3D point : points){
            point.rotateY(angle);
        }
        for(Line3D line : lines){
            line.rotateY(angle);
        }
    }

    public void rotateZ(double angle) {
        for(Point3D point : points){
            point.rotateZ(angle);
        }
        for(Line3D line : lines){
            line.rotateZ(angle);
        }
    }
    
    public List<Point3D> getPoints(){
        return this.points;
    }
    
    public List<Line3D> getLines(){
        return this.lines;
    }
}
