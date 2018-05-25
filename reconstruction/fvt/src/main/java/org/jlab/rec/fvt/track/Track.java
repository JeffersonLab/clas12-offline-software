/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.track;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;

import org.jlab.rec.fvt.fmt.cluster.Cluster;
import org.jlab.rec.fvt.track.trajectory.TrajectoryStateVec;

/**
 *
 * @author ziegler
 */
public class Track {
    
    private int _Id;
    private int _Sector;
     
    public int get_Id() {
        return _Id;
    }

    public void set_Id(int id) {
        this._Id = id;
    }
    
    public int get_Sector() {
        return _Sector;
    }

    public void set_Sector(int sector) {
        this._Sector = sector;
    }
    
    private List<Cluster> _Clusters;

    public List<Cluster> get_Clusters() {
        return _Clusters;
    }

    public void set_Clusters(List<Cluster> _Clusters) {
        this._Clusters = _Clusters;
    }
    
    private double _x;
    private double _y;
    private double _z;
    private double _px;
    private double _py;
    private double _pz;
    
    public double getX() {
        return _x;
    }

    public void setX(double _x) {
        this._x = _x;
    }

    public double getY() {
        return _y;
    }

    public void setY(double _y) {
        this._y = _y;
    }

    public double getZ() {
        return _z;
    }

    public void setZ(double _z) {
        this._z = _z;
    }

    public double getPx() {
        return _px;
    }

    public void setPx(double _px) {
        this._px = _px;
    }

    public double getPy() {
        return _py;
    }

    public void setPy(double _py) {
        this._py = _py;
    }

    public double getPz() {
        return _pz;
    }

    public void setPz(double _pz) {
        this._pz = _pz;
    }
    
    private int _q;

    public int getQ() {
        return _q;
    }

    public void setQ(int _q) {
        this._q = _q;
    }
   
}
