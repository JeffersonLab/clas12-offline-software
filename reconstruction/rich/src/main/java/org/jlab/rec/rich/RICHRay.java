package org.jlab.rec.rich;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.geometry.prim.Line3d;

// ----------------
public class RICHRay extends Line3d {
// ----------------

    private int debugMode = 0;

    private double refind  = 0;
    private int type = 0;    // 1=reflection, 2=transmission

    public RICHRay(Vector3d origin, Vector3d end) {
        super(origin, end);
    }

    // ----------------
    public double get_refind() { return refind; }
    // ----------------

    // ----------------
    public int get_type() { return type; }
    // ----------------

    // ----------------
    public void set_refind(double refind) { this.refind = refind; }
    // ----------------
            
    // ----------------
    public void set_type(int type) { this.type = type; }
    // ----------------

    // ----------------
    public void showRay() { 
    // ----------------

        System.out.format(" Ray  %8.2f %8.2f %8.2f --> %8.2f %8.2f %8.2f   (L %7.2f)  opt %4d   n %8.4f\n", 
             this.origin.x, this.origin.y, this.origin.z, 
             this.end.x, this.end.y, this.end.z, 
             this.diff().magnitude(), this.get_type(), this.get_refind());

    }

    // ----------------
    public void dumpRay() { 
    // ----------------

        System.out.format(" %8.2f %8.2f %8.2f %8.2f %8.2f %8.2f %7.2f %4d %8.4f\n", 
             this.origin.x, this.origin.y, this.origin.z, 
             this.end.x, this.end.y, this.end.z, 
             this.diff().magnitude(), this.get_type(), this.get_refind());

    }

}
