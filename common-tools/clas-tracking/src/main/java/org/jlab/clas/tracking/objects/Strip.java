/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.objects;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;
/**
 *
 * @author ziegler
 */
public class Strip extends TObject {

    /**
     * @return the _id
     */
    public int getId() {
        return _id;
    }

    /**
     * @param _id the _id to set
     */
    public void setId(int _id) {
        this._id = _id;
    }

    /**
     * @return the _x
     */
    public double getX() {
        return _x;
    }

    /**
     * @param _x the _x to set
     */
    public void setX(double _x) {
        this._x = _x;
    }

    /**
     * @return the _y
     */
    public double getY() {
        return _y;
    }

    /**
     * @param _y the _y to set
     */
    public void setY(double _y) {
        this._y = _y;
    }

    /**
     * @return the _z
     */
    public double getZ() {
        return _z;
    }

    /**
     * @param _z the _z to set
     */
    public void setZ(double _z) {
        this._z = _z;
    }

    /**
     * @return the _ux
     */
    public double getUx() {
        return _ux;
    }

    /**
     * @param _ux the _ux to set
     */
    public void setUx(double _ux) {
        this._ux = _ux;
    }

    /**
     * @return the _uy
     */
    public double getUy() {
        return _uy;
    }

    /**
     * @param _uy the _uy to set
     */
    public void setUy(double _uy) {
        this._uy = _uy;
    }

    /**
     * @return the _uz
     */
    public double getUz() {
        return _uz;
    }

    /**
     * @param _uz the _uz to set
     */
    public void setUz(double _uz) {
        this._uz = _uz;
    }

    /**
     * @return the _ex
     */
    public double getEx() {
        return _ex;
    }

    /**
     * @param _ex the _ex to set
     */
    public void setEx(double _ex) {
        this._ex = _ex;
    }

    /**
     * @return the _ey
     */
    public double getEy() {
        return _ey;
    }

    /**
     * @param _ey the _ey to set
     */
    public void setEy(double _ey) {
        this._ey = _ey;
    }

    /**
     * @return the _ez
     */
    public double getEz() {
        return _ez;
    }

    /**
     * @param _ez the _ez to set
     */
    public void setEz(double _ez) {
        this._ez = _ez;
    }

    /**
     * @return the _length
     */
    public double getLength() {
        return _length;
    }

    /**
     * @param _length the _length to set
     */
    public void setLength(double _length) {
        this._length = _length;
    }

    /**
     * @return the _c
     */
    public double getC() {
        return _c;
    }

    /**
     * @param _c the _c to set
     */
    public void setC(double _c) {
        this._c = _c;
    }

    /**
     * @return the _ce
     */
    public double getCe() {
        return _ce;
    }

    /**
     * @param _ce the _ce to set
     */
    public void setCe(double _ce) {
        this._ce = _ce;
    }

    /**
     * @return the _phi
     */
    public double getPhi() {
        return _phi;
    }

    /**
     * @param _phi the _phi to set
     */
    public void setPhi(double _phi) {
        this._phi = _phi;
    }

    /**
     * @return the _phie
     */
    public double getPhie() {
        return _phie;
    }

    /**
     * @param _phie the _phie to set
     */
    public void setPhie(double _phie) {
        this._phie = _phie;
    }
    
    public Strip(int id, double centroid, double x1, double y1, double z1, 
            double x2, double y2, double z2) {
        super(id, x1, y1, z1);
        Line3D l = new Line3D(x1,y1,z1,x2,y2,z2);
        Vector3D dir = l.direction().asUnit();
        _x      = x1;
        _y      = y1;
        _z      = z1;
        _ux     = dir.x();
        _uy     = dir.y();
        _uz     = dir.z();
        _length = l.length();
        _c = centroid;
        type = Type.XYZ;
    }
    public Strip(int id, double centroid, double x, double y, double z, 
            double ux, double uy, double uz, double length) {
        super(id, x, y, z);
        _x      = x;
        _y      = y;
        _z      = z;
        _ux     = ux;
        _uy     = uy;
        _uz     = uz;
        _length = length;
        _c = centroid;
        type = Type.XYZ;
    }
    
    public Strip(int id, double centroid, double x, double y, double phi) {
        super(id, x, y, -999);
        _phi    = phi;
        _c = centroid;
        type = Type.PHI;
    }
    
    public Strip(int id, double centroid, double z) {
        super(id, -999, -999, z);
        _c = centroid;
        _z = z;
        type = Type.Z;
    }
    @Override
    public boolean equals(Object o) { 
  
        if (o == this) { 
            return true; 
        } 

        if (!(o instanceof Strip)) { 
            return false; 
        } 
 
        Strip c = (Strip) o; 

        return Double.compare(getX(), c.getX()) == 0
                && Double.compare(getY(), c.getY()) == 0
                && Double.compare(getZ(), c.getZ()) == 0
                && Double.compare(getUx(), c.getUx()) == 0
                && Double.compare(getUy(), c.getUy()) == 0
                && Double.compare(getUz(), c.getUz()) == 0; 
    }
    
    
    private int _id;
    private double _x;
    private double _y;
    private double _z;
    private double _ux;
    private double _uy;
    private double _uz;
    private double _ex; //errors
    private double _ey;
    private double _ez;
    private double _length;
    private double _c; //centroid
    private double _ce; //centroid error
    private double _phi;
    private double _phie; //phi error
    public Type type;
    
    public enum Type {
        UDF(-1), XYZ(0), Z(1), PHI(2);
        private final int value;

        Type(int value) {
            this.value = value;
        }

        public byte value() {
            return (byte) this.value;
        }
    }
    
    
}
