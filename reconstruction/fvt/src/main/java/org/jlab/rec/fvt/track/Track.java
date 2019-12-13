/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.track;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.matrix.Matrix;

/**
 *
 * @author ziegler
 */
public class Track {

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
    
    public List<Track> getDCTracks(DataEvent event) {
        
        List<Track> trkList = new ArrayList<Track>();
        
        DataBank trkbank = event.getBank("TimeBasedTrkg::TBTracks");
        int trkrows = trkbank.rows();
        
        for (int i = 0; i < trkrows; i++) {
            Track trk = new Track();
            trk.setId(trkbank.getShort("id", i));
            trk.setSector(trkbank.getByte("sector", i));
            trk.setQ(trkbank.getByte("q", i));
            trk.setX(trkbank.getFloat("Vtx0_x", i));
            trk.setY(trkbank.getFloat("Vtx0_y", i));
            trk.setZ(trkbank.getFloat("Vtx0_z", i));
            trk.setPx(trkbank.getFloat("p0_x", i));
            trk.setPy(trkbank.getFloat("p0_y", i));
            trk.setPz(trkbank.getFloat("p0_z", i));
            
            trkList.add(trk);
        }
        return trkList;
    }
}
