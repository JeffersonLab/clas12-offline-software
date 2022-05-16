package org.jlab.clas.tracking.objects;

/**
 *
 * @author ziegler
 */
public class Cross extends TObject {

    public Cross() {
    }
    
    public Cross(int id, double x, double y, double z, double ux, double uy) {
        super(id, x, y, z);
        _ux = ux;
        _uy = uy;
    }
    public Cross(int id, double x, double y, double z) {
        super(id, x, y, z);
    }
    
    @Override
    public boolean equals(Object o) { 
  
        if (o == this) { 
            return true; 
        } 

        if (!(o instanceof Cross)) { 
            return false; 
        } 
 
        Cross c = (Cross) o; 

        return Double.compare(_x, c._x) == 0
                && Double.compare(_y, c._y) == 0
                && Double.compare(_z, c._z) == 0; 
    }
    /**
     * @return the _id
     */
    @Override
    public int getId() {
        return _id;
    }

    /**
     * @param _id the _id to set
     */
    @Override
    public void setId(int _id) {
        this._id = _id;
    }

    /**
     * @return the _x
     */
    @Override
    public double getX() {
        return _x;
    }

    /**
     * @param _x the _x to set
     */
    @Override
    public void setX(double _x) {
        this._x = _x;
    }

    /**
     * @return the _y
     */
    @Override
    public double getY() {
        return _y;
    }

    /**
     * @param _y the _y to set
     */
    @Override
    public void setY(double _y) {
        this._y = _y;
    }

    /**
     * @return the _z
     */
    @Override
    public double getZ() {
        return _z;
    }

    /**
     * @param _z the _z to set
     */
    @Override
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
    
    private int _id;
    private double _x;
    private double _y;
    private double _z;
    private double _ux;
    private double _uy;
    
    
}
