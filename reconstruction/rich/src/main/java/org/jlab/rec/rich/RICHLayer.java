package org.jlab.rec.rich;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Face3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Sphere3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import eu.mihosoft.vrl.v3d.Vertex;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Polygon;

public class RICHLayer extends ArrayList<RICHComponent> {

    /**
     * A layer in the RICH consists of an array of components
     */

    private int id;         // layer id
    private String name;    // layer name

    private Vector3d vinside = null;    // layer name

    private Vector3d barycenter           = null;
    private Shape3D  global_plane         = null;
    private Shape3D  component_surf       = null;
    private Sphere3D tracking_sphere      = null;
    private Shape3D  nominal_plane        = null;

    private List<Integer> compo_list          = new ArrayList<Integer>();
    
    // constructor
    // ----------------
    public RICHLayer(int lid, String sname, Vector3d vec) {
    // ----------------
          this.id = lid;
          this.name = sname;
          this.vinside = vec;
    }

    // ----------------
    public int get_id() { return this.id; }
    // ----------------

    // ----------------
    public void set_id(int lid) { this.id = lid; }
    // ----------------

    // ----------------
    public String get_Name() { return this.name; }
    // ----------------

    // ----------------
    public void set_Name(String sname) { this.name = sname; }
    // ----------------

    // ----------------
    public Vector3d get_Vinside() { return this.vinside; }
    // ----------------

    // ----------------
    public void set_Vinside(Vector3d vec) { this.vinside = vec; }
    // ----------------

    // ----------------
    public int get_size() { return this.size(); }
    // ----------------

    // ----------------
    public void add_Component_Face(Face3D face) { component_surf.addFace(face); }
    // ----------------

    // ----------------
    public void set_Component_Surf(Shape3D plane, List<Integer> icompos) { component_surf = plane; compo_list = icompos;}
    // ----------------
  
    // ----------------
    public Shape3D get_Component_Surf() { return component_surf; }
    // ----------------
  
    // ----------------
    public List<Integer> get_Component_List() { return compo_list; }
    // ----------------
  
    // ----------------
    public int get_Component_Index(int ifa) { 
    // ----------------
        if(compo_list.size()>0) return compo_list.get(ifa); 
        return -1;
    }

    // ----------------
    public Face3D get_Component_Face(int icompo, int iface) { 
    // ----------------
        
        int found=-1 ;
        for(int ifa=0; ifa<component_surf.size(); ifa++){
            if(compo_list.get(ifa)==icompo){
                found++;
                if(found==iface)return component_surf.face(ifa); 
            }
        }
        return null;
    }

    // ----------------
    public void set_Global_Plane(Shape3D plane) { global_plane = plane; }
    // ----------------
  
    // ----------------
    public Shape3D get_Global_Plane() { return global_plane; }
    // ----------------

    // ----------------
    public void set_Tracking_Sphere(Sphere3D sphere) { tracking_sphere = sphere; }
    // ----------------
  
    // ----------------
    public Sphere3D get_Tracking_Sphere() { return tracking_sphere; }
    // ----------------
  
    // ----------------
    public void set_Nominal_Plane(Shape3D plane) { nominal_plane = plane; }
    // ----------------
  
    // ----------------
    public Shape3D get_Nominal_Plane() { return nominal_plane; }
    // ----------------
  
    // ----------------
    public void set_Barycenter(Vector3d bary) { barycenter = bary; }
    // ----------------
  
    // ----------------
    public Vector3d get_Barycenter() { return barycenter; }
    // ----------------

    //------------------------------
    public Vector3d generate_Barycenter(){
    //------------------------------

        int debugMode = 0;

        // ATT: the loop over CSG volumes does not resolve the doubel counting of vertxes
        Vector3d bary = new Vector3d(0., 0., 0.);
        double nb=0.0;
        if(debugMode>=1)System.out.format(" Generate bary for lay %d with %d compos \n", this.get_id(), this.get_size());
        for (int ico=0; ico<this.get_size(); ico++){

            RICHComponent compo = this.get(ico);

            for (Polygon pol: compo.get_CSGVol().getPolygons()){
                for (Vertex ver: pol.vertices){
                    if(debugMode>=1)System.out.format(" bary from vertex: %7.2f %7.2f %7.2f\n", ver.pos.x, ver.pos.y, ver.pos.z);
                    bary.add( toVector3d(ver));
                    nb++;
                }
            }
        }
        if(nb==0.0)nb=1.0;  //no rescale
        this.set_Barycenter(bary.dividedBy(nb));
        return bary.dividedBy(nb);

    }

    //------------------------------
    public Vector3d get_CompoCenter(int icompo, Vector3D vers){
    //------------------------------

        int debugMode = 0;

        List<Vector3d> pts = new ArrayList<Vector3d>();
        Vector3d bary = new Vector3d(0., 0., 0.);
        double np=0.0;
        if(debugMode>=1)System.out.format(" Get center for compo %d \n", icompo);

        for(int ifa=0; ifa<component_surf.size(); ifa++){
            if(compo_list.get(ifa)!=icompo)continue;

            Face3D f = component_surf.face(ifa);
            if(toTriangle3D(f).normal().dot(vers)<0)continue;
            for (int ipo=0; ipo<3; ipo++){

                Vector3d p = toVector3d(f.point(ipo));
                int found = 0;
                for(int i=0; i<pts.size(); i++){
                    if(p.distance(pts.get(i))<1.e-3)found=1;
                }

                if(found==0){
                    pts.add(p);
                    bary.add(p);
                    np += 1;
                    if(debugMode>=1)System.out.format(" --> New Vertex %7.3f %7.3f %7.3f --> %3.0f %7.3f %7.3f %7.3f \n",
                                 p.x, p.y, p.z, np, bary.x, bary.y, bary.z);
                }else{
                    if(debugMode>=1)System.out.format(" --> Old Vertex %7.3f %7.3f %7.3f \n",p.x, p.y, p.z);
                }

            }
        }

        if(np==0.0)np=1.0;  //no rescale
        return bary.dividedBy(np);
    }


    // ----------------
    public boolean is_aerogel() { return this.get(0).is_aerogel(); } 
    // ----------------

    // ----------------
    public boolean is_spherical_mirror() { return this.get(0).is_spherical_mirror(); } 
    // ----------------

    // ----------------
    public boolean is_planar_mirror() { return this.get(0).is_planar_mirror(); } 
    // ----------------

    // ----------------
    public boolean is_mirror() { return this.get(0).is_mirror(); } 
    // ----------------

    // ----------------
    public boolean is_mapmt() { return this.get(0).is_mapmt(); } 
    // ----------------

    //------------------------------
    public Vector3d toVector3d(Vertex ver) {return  new Vector3d(ver.pos.x, ver.pos.y, ver.pos.z); }
    //------------------------------

    //------------------------------
    public Vector3d toVector3d(Vector3D ver) {return  new Vector3d(ver.x(), ver.y(), ver.z()); }
    //------------------------------

    //------------------------------
    public Vector3d toVector3d(Point3D pos) {return  new Vector3d(pos.x(), pos.y(), pos.z()); }
    //------------------------------

    //------------------------------
    public Triangle3D toTriangle3D(Face3D face){
    //------------------------------

        return new Triangle3D(face.point(0), face.point(1), face.point(2));

    }

    // ----------------
    public void show_Layer() {
    // ----------------
            System.out.format("Layer id %3d  size %4d \n", this.id, this.get_size()); 
            this.nominal_plane.show();
            for(int j = 0; j< this.size(); j++) {
                System.out.format("  --> comp # %3d  id %3d  voltype %3d  optical %3d  mirror %3d  n %6.3f \n",
                      j, this.get(j).get_id(), this.get(j).get_voltype(), this.get(j).get_optical(), this.get(j).get_type(), this.get(j).get_index());
            }
        }

}
