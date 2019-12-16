/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.track;

import java.util.List;
import org.jlab.geom.prim.Point3D; 
import org.jlab.rec.fvt.track.fit.StateVecs;

/**
 *
 * @author ziegler
 */
public class Track {

    public int status = 0;

    /**
     * @return the _traj
     */
    public List<Point3D> getTraj() {
        return _traj;
    }

    /**
     * @param _traj the _traj to set
     */
    public void setTraj(List<Point3D> _traj) {
        this._traj = _traj;
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
     * @return the sector
     */
    public int getSector() {
        return _sector;
    }

    /**
     * @param _sector the sector to set
     */
    public void setSector(int _sector) {
        this._sector = _sector;
    }
    /**
     * @return the _q
     */
    public int getQ() {
        return _q;
    }

    /**
     * @param _q the _q to set
     */
    public void setQ(int _q) {
        this._q = _q;
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
     * @return the _px
     */
    public double getPx() {
        return _px;
    }

    /**
     * @param _px the _px to set
     */
    public void setPx(double _px) {
        this._px = _px;
    }

    /**
     * @return the _py
     */
    public double getPy() {
        return _py;
    }

    /**
     * @param _py the _py to set
     */
    public void setPy(double _py) {
        this._py = _py;
    }

    /**
     * @return the _pz
     */
    public double getPz() {
        return _pz;
    }

    /**
     * @param _pz the _pz to set
     */
    public void setPz(double _pz) {
        this._pz = _pz;
    }
    private int _id;
    private int _sector;
    private int _q;
    private double _x;
    private double _y;
    private double _z;
    private double _px;
    private double _py;
    private double _pz;
    
    private List<Point3D> _traj;

    public double[] getLabPars(StateVecs.StateVec sv) {
        double x = sv.x;
        double y = sv.y;
        double z = sv.z;
        double p = 1./Math.abs(sv.Q);
        double tx = sv.tx;
        double ty = sv.ty;
        double pz = p/Math.sqrt(tx*tx+ty*ty+1);
        double px = pz*tx;
        double py = pz*ty;
        return new double[] {x,y,z,px,py,pz};
        
    }
    
   
}
