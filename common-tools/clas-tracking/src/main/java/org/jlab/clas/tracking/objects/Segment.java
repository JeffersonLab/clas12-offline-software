/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.objects;

/**
 *
 * @author ziegler
 */
public class Segment extends TObject{

    public void setLength(double _length) {
        this._length = _length;
    }

    public Segment(int id, double x, double y, double z, 
            double ux, double uy, double uz, double length) {
        super(id, x, y, z);
        _ux     = ux;
        _uy     = uy;
        _uz     = uz;
        _length = length;
    }
    
    @Override
    public boolean equals(Object o) { 
  
        if (o == this) { 
            return true; 
        } 

        if (!(o instanceof Segment)) { 
            return false; 
        } 
 
        Segment c = (Segment) o; 

        return Double.compare(_x, c._x) == 0
                && Double.compare(_y, c._y) == 0
                && Double.compare(_z, c._z) == 0
                && Double.compare(_ux, c._ux) == 0
                && Double.compare(_uy, c._uy) == 0
                && Double.compare(_uz, c._uz) == 0; 
    }
    
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
     * @return the _length
     */
    public double getLength() {
        return _length;
    }

    /**
     * @param _length the _length to set
     */
    private int _id;
    private double _x;
    private double _y;
    private double _z;
    private double _ux;
    private double _uy;
    private double _uz;
    private double _length;
}
