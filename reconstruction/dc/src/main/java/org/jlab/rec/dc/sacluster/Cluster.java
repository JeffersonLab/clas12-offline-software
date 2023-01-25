/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.dc.sacluster;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author ziegler
 */
public class Cluster extends ArrayList<Hit> {

    static int MINCLSIZE = 3;

    public Cluster() {
    }
    
    public Cluster(Link link) {
        _sector = link.getHitlets()[0].getHits()[0].getSector();
        _superlayer = (link.getHitlets()[0].getLayer()-1)/6 +1;
        double deltaW = link.getHitlets()[0].getWire() - link.getHitlets()[1].getWire();
        double deltaL = link.getHitlets()[0].getLayer()- link.getHitlets()[1].getLayer();
        _dir = deltaW/deltaL;
        double aw = 0;
        for(HitLet hl : link) {
            this.add(hl.getHits()[0]);
            if(hl.isDoublet)
                this.add(hl.getHits()[1]);
            aw+=hl.getWire();
        }
        _aveWire = aw/link.size();
        this.sort(Comparator.comparing(Hit::getLayer));
        
        int size = this.size(); 
        for(int i = 0; i<size-1; i++) {
            if(this.get(i).getLayer()==this.get(i+1).getLayer() && 
                    this.get(i).getWire()==this.get(i+1).getWire())
                this.remove(i+1);
            size--;
        }
    }
    
    
    /**
     * @return the _sector
     */
    public int getSector() {
        return _sector;
    }

    /**
     * @param _sector the _sector to set
     */
    public void setSector(int _sector) {
        this._sector = _sector;
    }

    /**
     * @return the _superlayer
     */
    public int getSuperlayer() {
        return _superlayer;
    }

    /**
     * @param _superlayer the _superlayer to set
     */
    public void setSuperlayer(int _superlayer) {
        this._superlayer = _superlayer;
    }

    /**
     * @return the _aveWire
     */
    public double getAveWire() {
        return _aveWire;
    }

    /**
     * @param _aveWire the _aveWire to set
     */
    public void setAveWire(double _aveWire) {
        this._aveWire = _aveWire;
    }

    /**
     * @return the _dir
     */
    public double getDir() {
        return _dir;
    }

    /**
     * @param _dir the _dir to set
     */
    public void setDir(double _dir) {
        this._dir = _dir;
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
    
    
    
    private int _sector;
    private int _superlayer;
    private double _aveWire;
    private double _dir;
    private int _id;
    
    
    @Override
    public String toString() {
        String str = String.format("Cluster id=%d sector=%d superlayer=%d", 
                                    this.getId(), this.getSector(), this.getSuperlayer());
        str = str + "\n";
        for(Hit h: this) str = str + h.toString() + "\n";
        return str;
    }
}
