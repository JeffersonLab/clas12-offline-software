/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.vtx;

import org.jlab.geom.prim.Point3D;

/**
 *
 * @author veronique
 */
public class Particle {

    public Particle(int pid, double x, double y, double z, double px, double py, double pz, int q) {
        _pid = pid;
        _vx = x;
        _vy = y;
        _vz = z;
        _px = px;
        _py = py;
        _pz = pz;
        _charge = q;
        
    }
    public Particle(int index, int pid, double x, double y, double z, double px, double py, double pz, int q) {
        _index = index;
        _pid = pid;
        _vx = x;
        _vy = y;
        _vz = z;
        _px = px;
        _py = py;
        _pz = pz;
        _charge = q;
        
    }

    /**
     * @return the _index
     */
    public int getIndex() {
        return _index;
    }

    /**
     * @param _index the _index to set
     */
    public void setIndex(int _index) {
        this._index = _index;
    }
    /**
     * @return the _pid
     */
    public int getPid() {
        return _pid;
    }

    /**
     * @param _pid the _pid to set
     */
    public void setPid(int _pid) {
        this._pid = _pid;
    }

    /**
     * @return the _charge
     */
    public int getCharge() {
        return _charge;
    }

    /**
     * @param _charge the _charge to set
     */
    public void setCharge(int _charge) {
        this._charge = _charge;
    }

    /**
     * @return the _vx
     */
    public double getVx() {
        return _vx;
    }

    /**
     * @param _vx the _vx to set
     */
    public void setVx(double _vx) {
        this._vx = _vx;
    }

    /**
     * @return the _vy
     */
    public double getVy() {
        return _vy;
    }

    /**
     * @param _vy the _vy to set
     */
    public void setVy(double _vy) {
        this._vy = _vy;
    }

    /**
     * @return the _vz
     */
    public double getVz() {
        return _vz;
    }

    /**
     * @param _vz the _vz to set
     */
    public void setVz(double _vz) {
        this._vz = _vz;
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
    private int _index = -1; 
    private int _pid;
    private int _charge;
    private double _vx;
    private double _vy;
    private double _vz;
    private double _px;
    private double _py;
    private double _pz;
    
    
    @Override
    public String toString() {
        String o = "[("+_index+") pid = "+_pid+" q = "+_charge+" X = "+new Point3D(_vx, _vy, _vz).toString()
                                            +" P = "+new Point3D(_px, _py, _pz).toString();   
        return o;
    }
}
