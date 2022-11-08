package org.jlab.rec.rich;

import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import org.jlab.geometry.prim.Line3d;   

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;


/**
 * @author mcontalb
 */
public class RICHUtil{


    //------------------------------
    public RICHUtil() {
    //------------------------------
    }


    //------------------------------
    public String toString(Vector3d vec, int qua) {
    //------------------------------
        if(qua==2)return String.format("%8.2f %8.2f %8.2f", vec.x, vec.y, vec.z);
        if(qua==3)return String.format("%8.3f %8.3f %8.3f", vec.x, vec.y, vec.z);
        if(qua==4)return String.format("%8.4f %8.4f %8.4f", vec.x, vec.y, vec.z);
        return String.format("%8.1f %8.1f %8.1f", vec.x, vec.y, vec.z);
 
    }


    //------------------------------
    public String toString(Vector3d vec) {
    //------------------------------
        return String.format("%8.3f %8.3f %8.3f", vec.x, vec.y, vec.z);
    }


    //------------------------------
    public String toString(Vector3D vec) {
    //------------------------------
        return String.format("%8.3f %8.3f %8.3f", vec.x(), vec.y(), vec.z());
    }


    //------------------------------
    public String toString(Point3D vec) {
    //------------------------------
        return String.format("%7.2f %7.2f %7.2f", vec.x(), vec.y(), vec.z());
    }


    //------------------------------
    public Vector3D toVector3D(Vector3d vin) {
    //------------------------------
        Vector3D vout = new Vector3D(vin.x, vin.y, vin.z); 
      return vout;
    }


    //------------------------------
    public Vector3D toVector3D(Point3D pin) {
    //------------------------------
        Vector3D vout = new Vector3D(pin.x(), pin.y(), pin.z()); 
      return vout;
    }


    //------------------------------
    public Vector3d toVector3d(Vertex ver) {return  new Vector3d(ver.pos.x, ver.pos.y, ver.pos.z); }
    //------------------------------


    //------------------------------
    public Vector3d toVector3d(Vector3D vin) {
    //------------------------------
        Vector3d vout = new Vector3d(vin.x(), vin.y(), vin.z()); 
      return vout;
    }


    //------------------------------
    public Vector3d toVector3d(Point3D pin) {
    //------------------------------
        Vector3d vout = new Vector3d(pin.x(), pin.y(), pin.z()); 
      return vout;
    }


    //------------------------------
    public Point3D toPoint3D(Vertex vin) {
    //------------------------------
       Point3D pout = new Point3D(vin.pos.x, vin.pos.y, vin.pos.z); 
       return pout;
    }


    //------------------------------
    public Point3D toPoint3D(Vector3D vin) {
    //------------------------------
       Point3D pout = new Point3D(vin.x(), vin.y(), vin.z()); 
       return pout;
    }


    //------------------------------
    public Point3D toPoint3D(Vector3d vin) {
    //------------------------------
       if(vin==null) return null;
       Point3D pout = new Point3D(vin.x, vin.y, vin.z); 
       return pout;
    }


    //------------------------------
    public Line3d toLine3d(Line3D lin) {
    //------------------------------
       Line3d lout = new Line3d(toVector3d(lin.origin()), toVector3d(lin.end()));
       return lout;
    }


    //------------------------------
    public Line3D toLine3D(Line3d lin) {
    //------------------------------
        Line3D lout = new Line3D(toPoint3D(lin.origin()), toPoint3D(lin.end()));
        return lout;
    }

}
