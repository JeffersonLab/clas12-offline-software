/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.dc.sacluster;

/**
 *
 * @author ziegler
 */
public class Hit implements Comparable<Hit>{

    public Hit() {    
    }
    
    public Hit(int id, int sector, int layer, int wire, int status) {
        _id = id;
        _sector = sector;
        _layer = layer;
        _wire = wire;
        
        this.setSecLayer(sector, layer);
        this.setSecSuperlayer(sector, layer);
        this.setSecRegion(sector, layer);
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
     * @return the _cid
     */
    public int getCid() {
        return _cid;
    }

    /**
     * @param _cid the _cid to set
     */
    public void setCid(int _cid) {
        this._cid = _cid;
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
     * @return the _layer
     */
    public int getLayer() {
        return _layer;
    }

    /**
     * @param _layer the _layer to set
     */
    public void setLayer(int _layer) {
        this._layer = _layer;
    }

    /**
     * @return the _wire
     */
    public int getWire() {
        return _wire;
    }

    /**
     * @param _wire the _wire to set
     */
    public void setWire(int _wire) {
        this._wire = _wire;
    }

    /**
     * @return the _status
     */
    public int getStatus() {
        return _status;
    }

    /**
     * @param _status the _status to set
     */
    public void setStatus(int _status) {
        this._status = _status;
    }
    private int _id;
    private int _cid; //id of the cluster containing the hit
    private int _sector;
    private int _layer;
    private int _wire;
    private int _status;
    private int _secLayer;
    private int _secSuperlayer;
    private int _secRegion;
    public boolean inDoublet;
    
    public void setSecLayer(int sector, int layer) {
        _secLayer = (sector-1)*36+layer; 
    }
    public void setSecSuperlayer(int sector, int layer) {
        _secSuperlayer = (sector-1)*6+((layer-1)/6 + 1);
    }
    public void setSecRegion(int sector, int layer) {
        _secRegion = (int)((((layer-1)/6 + 1) + 1) / 2)+(sector-1)*3;
    }
    /**
     * @return the _secLayer
     */
    public int getSecLayer() {
        return _secLayer;
    }

    /**
     * @return the _secSuperlayer
     */
    public int getSecSuperlayer() {
        return _secSuperlayer;
    }

    /**
     * @return the _secRegion
     */
    public int getSecRegion() {
        return _secRegion;
    }
    
    public int getSuperLayer() {
        return (_layer-1)/6 + 1;
    }
    public int getLocLayer() {
        return (_layer-1)%6 +1;
    }
    @Override
    public int compareTo(Hit o) {
        int CompSL = this.getSecLayer() < o.getSecLayer() ? 0 : 1;
        int CompWr = this.getWire() < o.getWire() ? 0 : 1;
        return ((CompSL == 0) ? CompWr : CompSL);
    }
    @Override
    public boolean equals(Object o) {
 
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Hit)) {
            return false;
        }
         
        Hit h = (Hit) o;
        
        return (this.getId()==h.getId());
    }

     @Override
    public String toString() {
        String str = String.format("Hit id=%d sector=%d layer=%d wire=%d", 
                                    this.getId(), this.getSector(), this.getLayer(), this.getWire());
        return str;
    }
}
