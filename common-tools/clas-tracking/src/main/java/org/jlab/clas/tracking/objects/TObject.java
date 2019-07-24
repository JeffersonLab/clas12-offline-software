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

public class TObject {
    /**
     * TObject is a tracking object that can be a cross 
     * or a segment representing a strip or a wire
     */
    public TObject(){       
    }
    public TObject(int id, double x, double y, double z) {
        _id = id;
        _x  = x;
        _y  = y;
        _z  = z;
    }
    
    @Override
    public boolean equals(Object o) { 
  
        if (o == this) { 
            return true; 
        } 

        if (!(o instanceof TObject)) { 
            return false; 
        } 
 
        TObject c = (TObject) o; 

        return Double.compare(_x, c._x) == 0
                && Double.compare(_y, c._y) == 0
                && Double.compare(_z, c._z) == 0; 
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
    
    private int _id;
    private double _x;
    private double _y;
    private double _z;
}
