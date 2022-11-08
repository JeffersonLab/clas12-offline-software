package org.jlab.detector.geom.RICH;

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Point3D;


// ----------------
public class RICHIntersection{
// ----------------
// class to store information at component boundary

    private int isec   = 0;
    private int ilayer = 0;
    private int icompo = 0;
    private int ipoly  = 0;
    private int itype  = 0;   // 1=entrance, 2=exit

    private Point3D  position = null;
    private Vector3D normal = null;

    private double nin = 0;  
    private double nout = 0;  


    // ----------------
    public RICHIntersection(int isec, int ilay, int ico, int ipo, int ityp, Point3D vec, Vector3D vno){
    // ----------------
        this.isec   = isec;
        this.ilayer = ilay;
        this.icompo = ico;
        this.ipoly  = ipo;
        this.itype  = ityp;

        this.position = vec;
        this.normal   = vno;

        // default values to be updated for aerogel
        this.nin  =  RICHGeoConstants.RICH_AIR_INDEX;
        this.nout =  RICHGeoConstants.RICH_AIR_INDEX;
    }


    // ----------------
    public int get_sector() {
    // ----------------
        return isec;
    }


    // ----------------
    public int get_layer() {
    // ----------------
        return ilayer;
    }


    // ----------------
    public void set_layer(int ilay) {
    // ----------------
        this.ilayer = ilay;
    }


    // ----------------
    public int get_component() {
    // ----------------
        return icompo;
    }

    // ----------------
    public void set_component(int ico) {
    // ----------------
        this.icompo = ico;
    }

    // ----------------
    public int get_polygon() {
    // ----------------
        return ipoly;
    }

    // ----------------
    public void set_polygon(int ipo) {
    // ----------------
        this.ipoly = ipo;
    }

    // ----------------
    public int get_type() {
    // ----------------
        return itype;
    }

    // ----------------
    public void  set_type(int ityp) {
    // ----------------
        this.itype = ityp;
    }

    // ----------------
    public double get_nin() {
    // ----------------
        return nin;
    }

    // ----------------
    public void set_nin(double ninside) {
    // ----------------
        this.nin = ninside;
    }

    // ----------------
    public double get_nout() {
    // ----------------
        return nout;
    }

    // ----------------
    public void set_nout(double noutside) {
    // ----------------
        this.nout = noutside;
    }

    // ----------------
    public Point3D get_pos() {
    // ----------------
        return position;
    }

    // ----------------
    public void set_pos(Point3D vec) {
    // ----------------
        this.position = vec;
    }

    // ----------------
    public Vector3D get_normal() {
    // ----------------
        return normal;
    }

    // ----------------
    public void set_normal(Vector3D vno) {
    // ----------------
        this.normal= vno;
    }

    // ----------------
    public void showIntersection() {
    // ----------------
        System.out.format("Inter. ilay %3d  ico %3d  ipo %3d  type %3d  rin %6.3f  rout %6.3f  pos %s  normal %s \n",
            this.get_layer(), this.get_component(), this.get_polygon(), this.get_type(), this.get_nin(), this.get_nout(), 
            this.get_pos().toStringBrief(3), this.get_normal().toStringBrief(3));
    }
            
}
