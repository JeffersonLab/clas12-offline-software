package org.jlab.detector.geom.RICH;

import org.jlab.geom.prim.Vector3D;

public class RICHFrame {
    // class to store reference systems for rotations

    private Vector3D xref = new Vector3D(1.,0.,0.);   //         reference x axis 
    private Vector3D yref = new Vector3D(0.,1.,0.);   //         reference y axis 
    private Vector3D zref = new Vector3D(0.,0.,1.);   //         reference z axis 
    private Vector3D bref = new Vector3D(0.,0.,0.);   //         reference barycenter
          
    // ----------------
    public RICHFrame() {
    // ----------------
 
    }

    // ----------------
    public RICHFrame(Vector3D vx, Vector3D vy, Vector3D vz, Vector3D vb) {
    // ----------------

        xref = vx;
        yref = vy;
        zref = vz;
        bref = vb;

    }

    // ----------------
    public Vector3D xref() { return xref; }
    // ----------------

    // ----------------
    public Vector3D yref() { return yref; }
    // ----------------

    // ----------------
    public Vector3D zref() { return zref; }
    // ----------------

    // ----------------
    public Vector3D bref() { return bref; }
    // ----------------

    // ----------------
    public void set_xref(Vector3D vx) { xref=vx; }
    // ----------------

    // ----------------
    public void set_yref(Vector3D vy) { yref=vy; }
    // ----------------

    // ----------------
    public void set_zref(Vector3D vz) { zref=vz; }
    // ----------------

    // ----------------
    public void set_bref(Vector3D vb) { bref=vb; }
    // ----------------

    // ----------------
    public RICHFrame clone() { return new RICHFrame(xref, yref, zref, bref);}
    // ----------------

    // ----------------
    public RICHFrame rotate(Vector3D axis, double angle) {
    // ---------------- 

        Vector3D xrot = xref.clone();
        Vector3D yrot = yref.clone();
        Vector3D zrot = zref.clone();
        Vector3D brot = bref.clone();
        axis.rotate(xrot, angle);
        axis.rotate(yrot, angle);
        axis.rotate(zrot, angle);
        axis.rotate(brot, angle);
        return new RICHFrame(xrot, yrot, zrot, brot);

    }


    // ----------------
    public void show() {
    // ----------------
        System.out.format(" FRAME xref %s  yref %s  zref %s bary %s \n",
            xref.toStringBrief(2), yref.toStringBrief(2), zref.toStringBrief(2), bref.toStringBrief(2));
    }
            
}
