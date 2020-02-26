package org.jlab.rec.rich;

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Line3D;

// ----------------
public class RICHRay {
// ----------------

    private int debugMode = 0;

    private Point3D Point0 = new Point3D(); // the origin point
    private Point3D Point1 = new Point3D(); // the end point

    private double refind  = 0;
    private int type = 0;       // 1=reflection, 2=transmission
    private boolean detected = false;   // ending into the MAPMT array

    public RICHRay(Point3D origin, Point3D end) {
        Point0 = origin;
        Point1 = end;
    }

    public RICHRay(Point3D origin, Vector3D direction) {
        Point0 = origin;
        Point1 = new Point3D(origin.x() + direction.x(), origin.y() + direction.y(), origin.z() + direction.z());
    }

    // ----------------
    public Point3D origin() { return Point0; }
    // ----------------

    // ----------------
    public Point3D end() { return Point1; }
    // ----------------

    // ----------------
    public Vector3D direction() { return Point1.toVector3D().sub(Point0.toVector3D()); }
    // ----------------

    // ----------------
    public Line3D asLine3D() { return new Line3D(Point0, Point1); }
    // ----------------

    // ----------------
    public double get_refind() { return refind; }
    // ----------------

    // ----------------
    public void set_refind(double refind) { this.refind = refind; }
    // ----------------
            
    // ----------------
    public int get_type() { return type; }
    // ----------------

    // ----------------
    public void set_type(int type) { this.type = type; }
    // ----------------

    // ----------------
    public boolean is_detected() { return this.detected;}
    // ----------------

    // ----------------
    public void set_detected() { this.detected = true; }
    // ----------------

    // ----------------
    public void showRay() { 
    // ----------------

        int dete=0;
        if(this.detected)dete=1;
        System.out.format(" Ray  %s --> %s   (L %7.2f)  opt %4d  dete %3d  n %8.4f\n", 
             this.origin().toStringBrief(3), this.end().toStringBrief(3),
             this.direction().mag(), this.get_type(), dete, this.get_refind());

    }

    // ----------------
    public void dumpRay() { 
    // ----------------

        int dete=0;
        if(this.detected)dete=1;
        System.out.format(" %s %s  %7.2f %6d %4d %8.4f\n", 
             this.origin().toStringBrief(3), this.end().toStringBrief(3),
             this.direction().mag(), this.get_type(), dete, this.get_refind());

    }

}
