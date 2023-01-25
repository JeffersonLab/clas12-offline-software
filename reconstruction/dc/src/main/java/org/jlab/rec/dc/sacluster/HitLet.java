/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.dc.sacluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ziegler
 */
public class HitLet {

    
    
    public HitLet() {
    }
    public HitLet(Hit hit0, Hit hit1) {
        _hits[0] = hit0;
        _hits[1] = hit1;
        _wire = 0.5*(_hits[0].getWire()+_hits[1].getWire());
        _seclayer = _hits[0].getSecLayer();
        _secSuperlayer  = _hits[0].getSecSuperlayer();
        _secRegion = _hits[0].getSecRegion();
        _layer = _hits[0].getLayer();
        hit0.inDoublet = true;
        hit1.inDoublet = true;
        this.isDoublet = true;
    }
    
    public HitLet(Hit hit0) {
        _hits[0] = hit0;
        _wire = _hits[0].getWire();
        _seclayer = _hits[0].getSecLayer();
    }
    int group;
    public int hasNeighbors;
    
    /**
     * @return the _hits
     */
    public Hit[] getHits() {
        return _hits;
    }

    /**
     * @param _hits the _hits to set
     */
    public void setHits(Hit[] _hits) {
        this._hits = _hits;
    }

    /**
     * @return the _wire
     */
    public double getWire() {
        return _wire;
    }

    /**
     * @param _wire the _wire to set
     */
    public void setWire(double _wire) {
        this._wire = _wire;
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
     * @return the _seclayer
     */
    public int getSecLayer() {
        return _seclayer;
    }

    /**
     * @param _seclayer the _seclayer to set
     */
    public void setSecLayer(int _seclayer) {
        this._seclayer = _seclayer;
    }

    /**
     * @return the _secSuperlayer
     */
    public int getSecSuperlayer() {
        return _secSuperlayer;
    }

    /**
     * @return the _connectedlinks
     */
    public List<Link> getConnectedlinks() {
        return _connectedlinks;
    }

    /**
     * @param _connectedlinks the _connectedlinks to set
     */
    public void setConnectedlinks(List<Link> _connectedlinks) {
        this._connectedlinks = _connectedlinks;
    }

    /**
     * @param _sedSuperlayer the _secSuperlayer to set
     */
    public void setSecSuperlayer(int _sedSuperlayer) {
        this._secSuperlayer = _sedSuperlayer;
    }

    /**
     * @return the _secRegion
     */
    public int getSecRegion() {
        return _secRegion;
    }

    /**
     * @param _secRegion the _secRegion to set
     */
    public void setSecRegion(int _secRegion) {
        this._secRegion = _secRegion;
    }
    
    public int getLocLayer() {
        return (_layer-1)%6 +1;
    }
    
    private Hit[] _hits = new Hit[2]; 
    private double _wire;
    private int _layer;
    private int _seclayer;
    private int _secSuperlayer;
    private int _secRegion;
    public boolean isDoublet = false;
    private List<Link> _connectedlinks;
    
    private static Map<Integer, List<Hit>> hitsmap = new HashMap<>();  //key1 sectorlayer; value1 hits in same layer
    private static void fillHitsMap(List<Hit> hits) {
        hitsmap.clear();
        for(Hit h : hits) { 
            int sl = h.getSecLayer(); 
            if(!hitsmap.containsKey(sl)) {
                hitsmap.put(sl, new ArrayList<Hit>());
                hitsmap.get(sl).add(h); 
            } else {
                hitsmap.get(sl).add(h); 
            }
        }
        
        for (Integer sl : hitsmap.keySet()) {
            hitsmap.get(sl).sort(Comparator.comparing(Hit::getWire));
        }
    }
    
    public static List<HitLet> makeListOfDoublets(List<Hit> hits) {
        fillHitsMap(hits);
        List<HitLet> hitdoublets = new ArrayList<>();
        
        for (Integer sl : hitsmap.keySet()) {
            List<Hit> sortedhits = hitsmap.get(sl);
            
            for(int i = 0; i < sortedhits.size(); i++) { 
                for(int j = i+1; j < sortedhits.size(); j++) { 
                    if((sortedhits.get(i).getWire()+1)==sortedhits.get(j).getWire()) {
                        HitLet hd = new HitLet(sortedhits.get(i), sortedhits.get(j)); 
                        hitdoublets.add(hd);
                    }
                }
            }
            for(int i = 0; i < sortedhits.size(); i++) {
                if(!sortedhits.get(i).inDoublet) {
                    HitLet hd = new HitLet(sortedhits.get(i), sortedhits.get(i)); 
                    hitdoublets.add(hd);
                }
            }
        }
        hitdoublets.sort(Comparator.comparing(HitLet::getWire));
        
        return hitdoublets;
    }

    boolean isLinkable(HitLet o) {
        if(this.getSecSuperlayer()==o.getSecSuperlayer() 
                && o.getLayer()>this.getLayer()
                && o.getLayer()-this.getLayer()<Link.nSkipLayers+1
                && Math.abs(o.getWire()-this.getWire())<o.getLayer()-this.getLayer()+1) 
            return true;
        
        return false;
    }

    
}
