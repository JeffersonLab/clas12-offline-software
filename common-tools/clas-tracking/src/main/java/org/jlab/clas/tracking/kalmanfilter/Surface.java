package org.jlab.clas.tracking.kalmanfilter;

import org.jlab.clas.tracking.objects.Strip;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author ziegler
 */
public class Surface implements Comparable<Surface> {
    
    public Type type;
    public Plane3D plane;
    public Point3D refPoint;
    public Point3D lineEndPoint1;
    public Point3D lineEndPoint2;
    public Point3D finitePlaneCorner1;
    public Point3D finitePlaneCorner2;
    public Cylindrical3D cylinder;
    private Transformation3D toGlobal = new Transformation3D();
    private Transformation3D toLocal  = new Transformation3D();
    public Arc3D arc;
    public Strip strip;
    private double error;
    private int index;
    private int layer;
    private int sector;
    // this is for multiple scattering estimates in track 
    private double _l_over_X0;
    //this is for energy loss
    private double _Z_over_A_times_l;
    private double _thickness;
    // this is for swimming
    public double swimAccuracy;
    public boolean notUsedInFit = false;
    public boolean passive = false;
    public double hemisphere = 1;
    
    public Surface(Plane3D plane3d, Point3D refrPoint, Point3D c1, Point3D c2, double accuracy) {
        type = Type.PLANEWITHPOINT;
        plane = plane3d;
        refPoint = refrPoint;
        finitePlaneCorner1 = c1;
        finitePlaneCorner2 = c2;
        swimAccuracy = accuracy;
    }
    public Surface(Plane3D plane3d, Point3D endPoint1, Point3D endPoint2, Point3D c1, Point3D c2, double accuracy) {
        type = Type.PLANEWITHLINE;
        plane = plane3d;
        lineEndPoint1 = endPoint1;
        lineEndPoint2 = endPoint2;
        finitePlaneCorner1 = c1;
        finitePlaneCorner2 = c2;
        swimAccuracy = accuracy;
    }
    public Surface(Plane3D plane3d, Strip strp, Point3D c1, Point3D c2, double accuracy) {
        type = Type.PLANEWITHSTRIP;
        plane = plane3d;
        strip = strp;
        finitePlaneCorner1 = c1;
        finitePlaneCorner2 = c2;
        lineEndPoint1 = new Point3D(strip.getX(), strip.getY(), strip.getZ());
        lineEndPoint2 = new Point3D(strip.getX()+strip.getLength()*strip.getUx(), 
                strip.getY()+strip.getLength()*strip.getUy(), 
                strip.getZ()+strip.getLength()*strip.getUz());
        swimAccuracy = accuracy;
    }
    public Surface(Cylindrical3D cylinder3d, Strip strp, double accuracy) {
        type = Type.CYLINDERWITHSTRIP;
        cylinder = cylinder3d;
        strip = strp;
        lineEndPoint1 = new Point3D(strip.getX(), strip.getY(), strip.getZ());
        lineEndPoint2 = new Point3D(strip.getX()+strip.getLength()*strip.getUx(), 
                strip.getY()+strip.getLength()*strip.getUy(), 
                strip.getZ()+strip.getLength()*strip.getUz());
        swimAccuracy = accuracy;
    }
    public Surface(Cylindrical3D cylinder3d, Point3D refrPoint, double accuracy) {
        type = Type.CYLINDERWITHPOINT;
        cylinder = cylinder3d;
        refPoint = refrPoint;
        swimAccuracy = accuracy;
    }
    public Surface(Cylindrical3D cylinder3d, Point3D endPoint1, Point3D endPoint2, double accuracy) {
        type = Type.CYLINDERWITHLINE;
        cylinder = cylinder3d;
        lineEndPoint1 = endPoint1;
        lineEndPoint2 = endPoint2;
        swimAccuracy = accuracy;
    }

    public Surface(Cylindrical3D cylinder3d, Arc3D refArc, Point3D endPoint1, Point3D endPoint2, double accuracy) {
        type = Type.CYLINDERWITHARC;
        cylinder = cylinder3d;
        arc = refArc;
        if(endPoint1 == null) {
            lineEndPoint1 = arc.origin();
        }
        if(endPoint2 == null) {
            lineEndPoint2 = arc.end();
        }
        swimAccuracy = accuracy;
    }

    public Surface(Point3D endPoint1, Point3D endPoint2, double accuracy) {
        type = Type.LINE;
        lineEndPoint1 = endPoint1;
        lineEndPoint2 = endPoint2;
        swimAccuracy = accuracy;
    }

    @Override
    public String toString() {
        String s = "Surface: ";
        s = s + String.format("Type=%s Index=%d  Layer=%d  Sector=%d  Emisphere=%.1f X0=%.4f  Z/AL=%.4f  Error=%.4f Skip=%b Passive=%b",
                               this.type.name(), this.getIndex(),this.getLayer(),this.getSector(),this.hemisphere,this.getl_over_X0(),
                               this.getZ_over_A_times_l(),this.getError(),this.notUsedInFit, this.passive);
        if(type==Type.PLANEWITHSTRIP) {
            s = s + "\n\t" + this.plane.toString();
            s = s + "\n\t" + this.finitePlaneCorner1.toString();
            s = s + "\n\t" + this.finitePlaneCorner2.toString();
            s = s + "\n\t" + this.strip.toString();
        }
        else if(type==Type.CYLINDERWITHSTRIP) {
            s = s + "\n\t" + this.cylinder.toString();
            s = s + "\n\t" + this.strip.toString();
        }
        else if(type==Type.LINE) {
            s = s + "\n\t" + this.lineEndPoint1.toString();
            s = s + "\n\t" + this.lineEndPoint2.toString();
        }
        return s;
    }
    /**
     * @return the error
     */
    public double getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(double error) {
        this.error = error;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the layer
     */
    public int getLayer() {
        return layer;
    }

    /**
     * @param layer the layer to set
     */
    public void setLayer(int layer) {
        this.layer = layer;
    }

    /**
     * @return the sector
     */
    public int getSector() {
        return sector;
    }

    /**
     * @param sector the sector to set
     */
    public void setSector(int sector) {
        this.sector = sector;
    }

    /**
     * @return  _l_over_X0
     */
    public double getl_over_X0() {
        return _l_over_X0;
    }

    /**
     * @param l_over_X0 the l_over_X0 to set
     */
    public void setl_over_X0(double l_over_X0) {
        this._l_over_X0 = l_over_X0;
    }

    /**
     * @return the _Z_over_A_times_l
     */
    public double getZ_over_A_times_l() {
        return _Z_over_A_times_l;
    }

    /**
     * @param _Z_over_A_times_l the _Z_over_A_times_l to set
     */
    public void setZ_over_A_times_l(double _Z_over_A_times_l) {
        this._Z_over_A_times_l = _Z_over_A_times_l;
    }

    public double getThickness() {
        return _thickness;
    }

    public void setThickness(double _thickness) {
        this._thickness = _thickness;
    }

    public Transformation3D toGlobal() {
        return toGlobal;
    }

    public Transformation3D toLocal() {
        return toLocal;
    }

    public void setTransformation(Transformation3D transform) {
        this.toGlobal = transform;
        this.toLocal  = transform.inverse();
    }

    @Override
    public int compareTo(Surface o) {
       if (this.index > o.index) {
            return 1;
        } else {
            return -1;
        }
    }

}
