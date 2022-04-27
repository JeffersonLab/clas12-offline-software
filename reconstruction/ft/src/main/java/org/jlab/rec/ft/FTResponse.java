/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author devita
 */
public class FTResponse extends DetectorResponse {
      
    private DetectorType _type;      // FTCAL, FTHODO, FTTRK
    private int    _size;            // cluster multiplicity
    private int    _id;              // response ID (ordinal number of hit)
    private int    _trkDet;          // number of TRK detector (0,1)
    
    
    public FTResponse() {
    }
    
    public FTResponse(DetectorType type) {
        this._type = type;
    }
    
    public DetectorType getType() {
        return _type;
    }
    
    public void setType(DetectorType type) {
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
    
    public int getTrkDet() {
        return _trkDet;
    }
    
    public void setTrkDet(byte layer) { 
    // if the name of the module is passed it is a byte variable        
        if(layer == DetectorLayer.FTTRK_MODULE1){         
            this._trkDet = 0;
        }else if(layer == DetectorLayer.FTTRK_MODULE2){
            this._trkDet = 1;
        }
    }
   
    public void setTrkDet(int ndet) {
        this._trkDet = ndet;
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
