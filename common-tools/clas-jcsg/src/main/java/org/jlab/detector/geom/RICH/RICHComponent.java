package org.jlab.detector.geom.RICH;

import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4Box;

import eu.mihosoft.vrl.v3d.CSG;

import org.jlab.geom.prim.Sphere3D;
import org.jlab.geom.prim.Shape3D;

/**
 * @author mcontalb
 */
// ----------------
public class RICHComponent{
// ----------------
// class linking geometrical volumes to optical properties

    private int debugMode = 0;

    private int id      = 0;
    private int layer   = 0;
    private int sector  = 0;
    private int voltype = 0;   // 1=Box, 2=Stl
    private int optical = 0;
    private int type = 0;  // 1=aerogel, 2=front mirror, 3=lateral mirror, 4=spherical mirror, 5=mapmts
    private G4Box BoxVol = null;
    private G4Stl StlVol = null;

    private double ref_index     = 0.0;  
    private double ref_planarity = 0.0;  
    private double ref_radius    = 0.0;  

    private Sphere3D tracking_sphere     = null;
    private Shape3D  tracking_surf       = null;
    private Shape3D  nominal_plane       = null;

    // ----------------
    public RICHComponent(int isec, int ico, int ilay, int opti, G4Box Vol){
    // ----------------
        this.id      = ico;
        this.layer   = ilay;
        this.sector  = isec;
        this.voltype = 1;
        this.optical = opti;
        this.BoxVol  = Vol;
    }

    // ----------------
    public RICHComponent(int isec, int ico, int ilay, int opti, G4Stl Vol){
    // ----------------
        this.id      = ico;
        this.layer   = ilay;
        this.sector  = isec;
        this.voltype = 2;
        this.optical = opti;
        this.StlVol  = Vol;
    }

    // ----------------
    public int get_id() { return id; }
    // ----------------

    // ----------------
    public void set_id(int id) { this.id = id; }
    // ----------------
          

    // ----------------
    public int get_sector() {
    // ----------------
        return sector;
    }


    // ----------------
    public void set_sector(int sector) {
    // ----------------
        this.sector = sector;
    }


    // ----------------
    public int get_layer() {
    // ----------------
        return layer;
    }


    // ----------------
    public void set_layer(int layer) {
    // ----------------
        this.layer = layer;
    }


    // ----------------
    public int get_voltype() {
    // ----------------
        return voltype;
    }

    // ----------------
    public void set_voltype(int voltype) {
    // ----------------
        this.voltype = voltype;
    }
          
    // ----------------
    public int get_optical() {
    // ----------------
        return this.optical;
    }

    // ----------------
    public void set_optical(int opti) {
    // ----------------
        this.optical = opti;
    }
          
    // ----------------
    public boolean is_optical() {
    // ----------------
        if(this.optical == 1) return true;
        return false;
    }
          
    // ----------------
    public int get_type() {
    // ----------------
        return this.type;
    }

    // ----------------
    public void set_type(int type) {
    // ----------------
        this.type =type;
    }
          
    // ----------------
    public boolean is_2cm_aerogel() {
    // ----------------

        if( type==RICHLayerType.AEROGEL_2CM_B1.type() || type==RICHLayerType.AEROGEL_2CM_B2.type() ) return true;
        return false;
    }


    // ----------------
    public boolean is_3cm_aerogel() {
    // ----------------

        if( type==RICHLayerType.AEROGEL_3CM_L1.type() || type==RICHLayerType.AEROGEL_3CM_L2.type() ) return true;
        return false;

    }


    // ----------------
    public boolean is_aerogel() {
    // ----------------

        if( is_2cm_aerogel() || is_3cm_aerogel() ) return true;
        return false;

    }

    // ----------------
    public boolean is_spherical_mirror() {
    // ----------------

        if( type==RICHLayerType.MIRROR_SPHERE.type()) return true;
        return false;

    }

    // ----------------
    public boolean is_planar_mirror() {
    // ----------------

        if( is_front_mirror() || is_lateral_mirror() ) return true;
        return false;
    }


    // ----------------
    public boolean is_front_mirror() {
    // ----------------

        if( type==RICHLayerType.MIRROR_FRONT_B1.type() || type==RICHLayerType.MIRROR_FRONT_B2.type() ) return true;
        return false;
    }

   // ----------------
    public boolean is_lateral_mirror() {
    // ----------------

        if( type==RICHLayerType.MIRROR_LEFT_L1.type()  || type==RICHLayerType.MIRROR_LEFT_L2.type()  ||
            type==RICHLayerType.MIRROR_RIGHT_R1.type() || type==RICHLayerType.MIRROR_RIGHT_R2.type() ||
            type==RICHLayerType.MIRROR_BOTTOM.type() ) return true;
        return false;
    }


    // ----------------
    public boolean is_mirror() {
    // ----------------

        if( is_planar_mirror() || is_spherical_mirror() ) return true;
        return false;
    }


    // ----------------
    public boolean is_mapmt() {
    // ----------------

        if( type==RICHLayerType.MAPMT.type() ) return true;
        return false;
    }


    // ----------------
    public double get_index() {
    // ----------------
        return ref_index;
    }


    // ----------------
    public void set_index(double ref_index) {
    // ----------------
        this.ref_index = ref_index;
    }
          

    // ----------------
    public double get_planarity() {
    // ----------------
        return ref_planarity;
    }


    // ----------------
    public void set_planarity(double planarity) {
    // ----------------
        this.ref_planarity = planarity;
        double D = 10;   // aerogel tile half dimension 
        if(ref_planarity>0) this.ref_radius = (D*D + planarity* planarity)/(2*planarity);
    }
          
    // ----------------
    public double get_radius() {
    // ----------------
        return ref_radius;
    }

    // ----------------
    public void set_TrackingSphere(Sphere3D sphere) { tracking_sphere = sphere; }
    // ----------------

    // ----------------
    public Sphere3D get_TrackingSphere() { return tracking_sphere; }
    // ----------------

    // ----------------
    public void set_TrackingSurf(Shape3D plane) { tracking_surf = plane; }
    // ----------------

    // ----------------
    public void set_NominalPlane(Shape3D plane) { nominal_plane = plane; }
    // ----------------

    // ----------------
    public Shape3D get_TrackingSurf() { return tracking_surf; }
    // ----------------

    // ----------------
    public Shape3D get_NominalPlane() { return nominal_plane; }
    // ----------------

    // ----------------
    public G4Box get_BoxVol() { return this.BoxVol; }
    // ----------------

    // ----------------
    public void set_BoxVol(G4Box Vol) { this.BoxVol = Vol; }
    // ----------------

    // ----------------
    public G4Stl get_StlVol() { return this.StlVol; }
    // ----------------

    // ----------------
    public void set_StlVol(G4Stl Vol) { this.StlVol = Vol; }
    // ----------------


    // ----------------
    public CSG get_CSGVol() {
    // ----------------
        if(this.voltype==1) return BoxVol.toCSG();
        if(this.voltype==2) return StlVol.toCSG();
        return null;
    }


    /*// ----------------
    public rotate(int sec) {
    // ----------------
        if(this.voltype==1) {
            BoxVol.rotate("xzy", 0., Math.toRadians(180. - (sec-1) * 60.0), 0.);
            
            BoxVol.translate(position.rotateZ(-Math.toRadians(180.0 - (sec - 1) * 60.0)));
        }
        if(this.voltype==2) return StlVol.toCSG();
        return null;
    }*/


    // ----------------
    public void showComponent() {
    // ----------------
        System.out.format("Component id %3d  Layer %3d VolType %3d  Optical %3d  Type %3d  Rindex %7.4f \n"
            , this.get_id()
            , this.get_layer()
            , this.get_voltype()
            , this.get_optical()
            , this.get_type()
            , this.get_index());
    }
            
}
