/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.kalmanfilter;

import org.jlab.clas.tracking.objects.Strip;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;

/**
 *
 * @author ziegler
 */
public class Surface implements Comparable<Surface> {
    
    public Type type;
    public Plane3D plane;
    public Point3D refPoint;
    public Point3D lineEndPoint1;
    public Point3D lineEndPoint2;
    public Point3D finitePlaneCorner1;
    public Point3D finitePlaneCorner2;
    public Cylindrical3D cylinder;
    public Arc3D arc;
    public Strip strip;
    private double error;
    private int layer;
    private int sector;
    
    public Surface(Plane3D plane3d, Point3D refrPoint, Point3D c1, Point3D c2) {
        type = Type.PLANEWITHPOINT;
        plane = plane3d;
        refPoint = refrPoint;
        finitePlaneCorner1 = c1;
        finitePlaneCorner2 = c2;
    }
    public Surface(Plane3D plane3d, Point3D endPoint1, Point3D endPoint2, Point3D c1, Point3D c2) {
        type = Type.PLANEWITHLINE;
        plane = plane3d;
        lineEndPoint1 = endPoint1;
        lineEndPoint2 = endPoint2;
        finitePlaneCorner1 = c1;
        finitePlaneCorner2 = c2;
    }
    public Surface(Plane3D plane3d, Strip strp, Point3D c1, Point3D c2) {
        type = Type.PLANEWITHSTRIP;
        plane = plane3d;
        strip = strp;
        finitePlaneCorner1 = c1;
        finitePlaneCorner2 = c2;
        lineEndPoint1 = new Point3D(strip.getX(), strip.getY(), strip.getZ());
        lineEndPoint2 = new Point3D(strip.getX()+strip.getLength()*strip.getUx(), 
                strip.getY()+strip.getLength()*strip.getUy(), 
                strip.getZ()+strip.getLength()*strip.getUz());
    }
    public Surface(Cylindrical3D cylinder3d, Strip strp) {
        type = Type.CYLINDERWITHSTRIP;
        cylinder = cylinder3d;
        strip = strp;
        lineEndPoint1 = new Point3D(strip.getX(), strip.getY(), strip.getZ());
        lineEndPoint2 = new Point3D(strip.getX()+strip.getLength()*strip.getUx(), 
                strip.getY()+strip.getLength()*strip.getUy(), 
                strip.getZ()+strip.getLength()*strip.getUz());
    }
    public Surface(Cylindrical3D cylinder3d, Point3D refrPoint) {
        type = Type.CYLINDERWITHPOINT;
        cylinder = cylinder3d;
        refPoint = refrPoint;
    }
    public Surface(Cylindrical3D cylinder3d, Point3D endPoint1, Point3D endPoint2) {
        type = Type.CYLINDERWITHLINE;
        cylinder = cylinder3d;
        lineEndPoint1 = endPoint1;
        lineEndPoint2 = endPoint2;
    }

    /**
     * @return the error
     */
    public double getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(double error) {
        this.error = error;
    }

    /**
     * @return the layer
     */
    public int getLayer() {
        return layer;
    }

    /**
     * @param layer the layer to set
     */
    public void setLayer(int layer) {
        this.layer = layer;
    }

    /**
     * @return the sector
     */
    public int getSector() {
        return sector;
    }

    /**
     * @param sector the sector to set
     */
    public void setSector(int sector) {
        this.sector = sector;
    }
    public Surface(Cylindrical3D cylinder3d, Arc3D refArc, 
            Point3D endPoint1, Point3D endPoint2) {
        type = Type.CYLINDERWITHARC;
        cylinder = cylinder3d;
        arc = refArc;
        if(endPoint1 == null) {
            lineEndPoint1 = arc.origin();
        }
        if(endPoint2 == null) {
            lineEndPoint2 = arc.end();
        }
    }

    @Override
    public int compareTo(Surface o) {
       if (this.layer > o.layer) {
            return 1;
        } else {
            return -1;
        }
    }

}
