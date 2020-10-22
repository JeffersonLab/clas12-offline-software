/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.trackrep;

import java.util.List;

/**
 *
 * @author ziegler
 */
public class Trajectory {

    /**
     * @return the _X
     */
    public List<Double> getX() {
        return _X;
    }

    /**
     * @param _X the _X to set
     */
    public void setX(List<Double> _X) {
        this._X = _X;
    }

    /**
     * @return the _Y
     */
    public List<Double> getY() {
        return _Y;
    }

    /**
     * @param _Y the _Y to set
     */
    public void setY(List<Double> _Y) {
        this._Y = _Y;
    }

    /**
     * @return the _Z
     */
    public List<Double> getZ() {
        return _Z;
    }

    /**
     * @param _Z the _Z to set
     */
    public void setZ(List<Double> _Z) {
        this._Z = _Z;
    }
    
    private List<Double> _X;
    private List<Double> _Y;
    private List<Double> _Z;
    
    
}
