/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.trackrep;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.objects.TObject;

/**
 *
 * @author ziegler
 */
public class Seed extends ArrayList<TObject> {

    public Seed(){
    }
    public Seed(int id) {
        _id = id;
    }
    public Seed(int id, List<TObject> tobjects) {
        _id      = id;
        this.addAll(tobjects);
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
     * @return the _qualFac
     */
    public double getQualFac() {
        return _qualFac;
    }

    /**
     * @param _qualFac the _qualFac to set
     */
    public void setQualFac(double _qualFac) {
        this._qualFac = _qualFac;
    }

    /**
     * @return the _overlapFlag
     */
    public int getOverlapFlag() {
        return _overlapFlag;
    }

    /**
     * @param _overlapFlag the _overlapFlag to set
     */
    public void setOverlapFlag(int _overlapFlag) {
        this._overlapFlag = _overlapFlag;
    }

    /**
     * @return the _tobjects
     */
    public List<TObject> getTObjects() {
        return this;
    }
    
    /**
     * @param to the _tobjects to set
     */
    public void setTObjects(List<TObject> to) {
        this.addAll(to);
    }

    /**
     * @return the _overlaps
     */
    public List<Seed> getOverlaps() {
        return _overlaps;
    }

    /**
     * @param _overlaps the _overlaps to set
     */
    public void setOverlaps(List<Seed> _overlaps) {
        this._overlaps = _overlaps;
    }
    
    private int _id;
    private double _qualFac;
    private int _overlapFlag = 0; //0: not an overlap; 1: an overlap with another seed
    private List<Seed> _overlaps;

    
}
