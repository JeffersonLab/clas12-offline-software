package org.jlab.rec.rich;

import org.jlab.detector.geant4.v2.RICHGeant4Factory;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4Box;

import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.CSG;

import org.jlab.geom.prim.Sphere3D;
import org.jlab.geom.prim.Shape3D;

// ----------------
public class RICHComponent{
// ----------------
// class linking geometrical volumes to optical properties

    private int debugMode = 0;

    private int id = 0;
    private int layer = 0;
    private int voltype = 0;   // 1=Box, 2=Stl
    private int optical = 0;
    private int type = 0;  // 1=aerogel, 2=front mirror, 3=lateral mirror, 4=spherical mirror, 5=mapmts
    private G4Box BoxVol = null;
    private G4Stl StlVol = null;

    private float ref_index = 0;  
    private float ref_planarity = 0;  
    private float ref_radius = 0;  

    private Sphere3D tracking_sphere      = null;
    private Shape3D  tracking_surf       = null;

    // ----------------
    public RICHComponent(int ico, int idlay, int opti, G4Box Vol){
    // ----------------
        this.id = ico;
        this.layer = idlay;
        this.voltype = 1;
        this.optical = opti;
        this.BoxVol = Vol;
    }

    // ----------------
    public RICHComponent(int ico, int idlay, int opti, G4Stl Vol){
    // ----------------
        this.id = ico;
        this.layer = idlay;
        this.voltype = 2;
        this.optical = opti;
        this.StlVol = Vol;
    }

    // ----------------
    public int get_id() { return id; }
    // ----------------

    // ----------------
    public void set_id(int id) { this.id = id; }
    // ----------------
          
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
    public void set_type(int ityp) {
    // ----------------
        this.type = ityp;
    }
          
    // ----------------
    public boolean is_aerogel() {
    // ----------------
        if(this.type==1)return true;
        return false;
    }

    // ----------------
    public boolean is_front_mirror() {
    // ----------------
        if(this.type==2)return true;
        return false;
    }

    // ----------------
    public boolean is_lateral_mirror() {
    // ----------------
        if(this.type==3)return true;
        return false;
    }

    // ----------------
    public boolean is_planar_mirror() {
    // ----------------
        if(this.type==2 || this.type==3)return true;
        return false;
    }

    // ----------------
    public boolean is_spherical_mirror() {
    // ----------------
        if(this.type==4)return true;
        return false;
    }

    // ----------------
    public boolean is_mirror() {
    // ----------------
        if(this.type==2)return true;
        if(this.type==3)return true;
        if(this.type==4)return true;
        return false;
    }

    // ----------------
    public boolean is_mapmt() {
    // ----------------
        if(this.type==5)return true;
        return false;
    }

    // ----------------
    public float get_index() {
    // ----------------
        return ref_index;
    }

    // ----------------
    public void set_index(float ref_index) {
    // ----------------
        this.ref_index = ref_index;
    }
          
    // ----------------
    public float get_planarity() {
    // ----------------
        return ref_planarity;
    }

    // ----------------
    public void set_planarity(float planarity) {
    // ----------------
        this.ref_planarity = planarity;
        float D = 10;   // aerogel tile half dimension 
        if(ref_planarity>0) this.ref_radius = (D*D + planarity* planarity)/(2*planarity);
    }
          
    // ----------------
    public float get_radius() {
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
    public Shape3D get_TrackingSurf() { return tracking_surf; }
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
