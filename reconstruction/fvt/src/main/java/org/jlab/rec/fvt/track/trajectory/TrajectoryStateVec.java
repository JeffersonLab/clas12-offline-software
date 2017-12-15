/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.track.trajectory;
 
/**
 *
 * @author ziegler
 */
public class TrajectoryStateVec {
    
    private int _TrkId;
    private double _X;
    private double _Y;
    private double _Z;
    private double _tX;
    private double _tY;
    private double _tZ;
    private double _pathLen;
    private double _B;
    private double _dEdx;

    public int getTrkId() {
        return _TrkId;
    }

    public void setTrkId(int _TrkId) {
        this._TrkId = _TrkId;
    }

    public double getX() {
        return _X;
    }

    public void setX(double _X) {
        this._X = _X;
    }

    public double getY() {
        return _Y;
    }

    public void setY(double _Y) {
        this._Y = _Y;
    }

    public double getZ() {
        return _Z;
    }

    public void setZ(double _Z) {
        this._Z = _Z;
    }

    public double getpX() {
        return _tX;
    }

    public void setpX(double _tX) {
        this._tX = _tX;
    }

    public double getpY() {
        return _tY;
    }

    public void setpY(double _tY) {
        this._tY = _tY;
    }

    public double getpZ() {
        return _tZ;
    }

    public void setpZ(double _tZ) {
        this._tZ = _tZ;
    }

    public double getPathLen() {
        return _pathLen;
    }

    public void setPathLen(double _pathLen) {
        this._pathLen = _pathLen;
    }

    public double getiBdl() {
        return _B;
    }

    public void setiBdl(double _B) {
        this._B = _B;
    }

    public double getdEdx() {
        return _dEdx;
    }

    public void setdEdx(double _dEdx) {
        this._dEdx = _dEdx;
    }

    private int _DetId;

    public int getDetId() {
        return _DetId;
    }

    public void setDetId(int _DetId) {
        this._DetId = _DetId;
    }
    
    private String _Name;

    public String getDetName() {
        return _Name;
    }

    public void setDetName(String name) {
        this._Name = name;
    }
    
}
