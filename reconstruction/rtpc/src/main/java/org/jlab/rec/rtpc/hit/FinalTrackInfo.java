/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

/**
 *
 * @author davidpayette
 */
public class FinalTrackInfo {
    
    private double _px;
    private double _py;
    private double _pz;
    private double _vz;
    private double _tl;
    private double _dEdx;
    
    public FinalTrackInfo(){}
    
    public FinalTrackInfo(double px, double py, double pz, double vz, double tl, double dEdx){
        _px = px;
        _py = py;
        _pz = pz;
        _vz = vz;
        _tl = tl;
        _dEdx = dEdx;
    }
    
    public void set_px(double px){
        _px = px;
    }
    public void set_py(double py){
        _py = py;
    }
    public void set_pz(double pz){
        _pz = pz;
    }
    public void set_tl(double tl){
        _tl = tl;
    }
    public void set_dEdx(double dEdx){
        _dEdx = dEdx;
    }
    public void set_vz(double vz){
        _vz = vz;
    }
    
    public double get_px(){
        return _px;
    }   
    public double get_py(){
        return _py;
    }
    public double get_pz(){
        return _pz;
    }
    public double get_tl(){
        return _tl;
    }
    public double get_dEdx(){
        return _dEdx;
    }
    public double get_vz(){
        return _vz;
    }
}
