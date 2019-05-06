package org.jlab.rec.rich;

import eu.mihosoft.vrl.v3d.Vector3d;

// ----------------
public class RICHIntersection{
// ----------------
// class to store information at component boundary

    private int ilayer = 0;
    private int icompo= 0;
    private int ipoly = 0;
    private int itype = 0;   // 1=entrance, 2=exit

    private Vector3d position = null;
    private Vector3d normal = null;

    private float nin = 0;  
    private float nout = 0;  


    // ----------------
    public RICHIntersection(int ilay, int ico, int ipo, int ityp, Vector3d vec, Vector3d vno){
    // ----------------
        this.ilayer = ilay;
        this.icompo = ico;
        this.ipoly  = ipo;
        this.itype  = ityp;

        this.position = vec;
        this.normal   = vno;

        // default values to be updated for aerogel
        this.nin = (float) RICHConstants.RICH_AIR_INDEX;
        this.nout = (float) RICHConstants.RICH_AIR_INDEX;
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
    public float get_nin() {
    // ----------------
        return nin;
    }

    // ----------------
    public void set_nin(float ninside) {
    // ----------------
        this.nin = ninside;
    }

    // ----------------
    public float get_nout() {
    // ----------------
        return nout;
    }

    // ----------------
    public void set_nout(float noutside) {
    // ----------------
        this.nout = noutside;
    }

    // ----------------
    public Vector3d get_pos() {
    // ----------------
        return position;
    }

    // ----------------
    public void set_pos(Vector3d vec) {
    // ----------------
        this.position = vec;
    }

    // ----------------
    public Vector3d get_normal() {
    // ----------------
        return normal;
    }

    // ----------------
    public void set_normal(Vector3d vno) {
    // ----------------
        this.normal= vno;
    }

    // ----------------
    public void showIntersection() {
    // ----------------
        System.out.format("   Intersection ilay %3d  ico %3d  ipo %3d  type %3d  rin %6.3f  rout %6.3f  pos %7.2f %7.2f %7.2f  normal %7.2f %7.2f %7.2f \n",  
            this.get_layer(), this.get_component(), this.get_polygon(), this.get_type(), this.get_nin(), this.get_nout(), 
            this.get_pos().x, this.get_pos().y, this.get_pos().z, 
            this.get_normal().x, this.get_normal().y, this.get_normal().z);
    }
            
}
