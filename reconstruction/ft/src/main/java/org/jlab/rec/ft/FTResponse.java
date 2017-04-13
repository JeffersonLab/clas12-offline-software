/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft;

import org.jlab.clas.detector.DetectorResponse;

/**
 *
 * @author devita
 */
public class FTResponse extends DetectorResponse {
    
    private String _type;            // FTCAL, FTHODO, FTTRK
    private int    _size;            // cluster multiplicity
    private int    _id;              // response ID


    
    public FTResponse() {
    }
    
    public FTResponse(String type) {
        this._type = type;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        this._type = type;
    }

    public int getSize() {
        return _size;
    }

    public void setSize(int size) {
        this._size = size;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }
        
    public void show() {
        System.out.println("ID = "+ this.getId()
                    + " Type = "  + this.getType()
                    + " E = "     + this.getEnergy()
                    + " X = "     + this.getPosition().x()
                    + " Y = "     + this.getPosition().y()
                    + " Z = "     + this.getPosition().z()
                    + " Theta = " + Math.toDegrees(this.getPosition().theta())
                    + " Phi = "   + Math.toDegrees(this.getPosition().phi())
                    + " Time = "  + this.getTime()
                    + " Size = "  + this.getSize()
                    );
    }
}
