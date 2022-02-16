package org.jlab.rec.fmt.track;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.fmt.Constants;

/**
 *
 * @author devita
 */
public class Trajectory {
    
    private Point3D  position = null;
    private Point3D  localPosition = null;
    private Vector3D direction = null;
    private int      layer = 0;
    private double   path  = 0;

    public Trajectory(int layer, Point3D p, Vector3D d, double path) {
        this.layer     = layer;
        this.position  = p;
        this.direction = d;
        this.path      = path;
    }

    public Trajectory(int layer, double x, double y, double z, double tx, double ty, double tz, double path) {
        this.layer         = layer;
        this.position      = new Point3D(x,y,z);
        this.localPosition = Constants.toLocal(layer, position);
        this.direction     = new Vector3D(tx,ty,tz);
        this.path          = path;
    }
    
    
    public Point3D getPosition() {
        return position;
    }

    public void setPosition(Point3D position) {
        this.position = position;
    }

    public Point3D getLocalPosition() {
        return this.localPosition;
    }

    public void setLocalPosition(Point3D p) {
        this.localPosition = p;
    }

    public Vector3D getDirection() {
        return direction;
    }

    public void setDirection(Vector3D direction) {
        this.direction = direction;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public double getPath() {
        return path;
    }

    public void setPath(double path) {
        this.path = path;
    }
    
    public String toStringBrief() {
        String str = "Trajectory :" + " Layer "     + this.layer 
                                    + " Position "  + this.position.toStringBrief(4)
                                    + " Direction " + this.direction.toStringBrief(4);
        return str;
    }
    
    @Override
    public String toString() {
        String str = this.toStringBrief()
                   + String.format(" LocX %.4f",this.getLocalPosition().x())
                   + String.format(" LocY %.4f",this.getLocalPosition().y());
        return str;
    }
    
}
