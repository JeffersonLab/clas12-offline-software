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
    private double _theta;
    private double _phi;
    private int _numhits;
    private double _R;
    private double _A;
    private double _B;
    private double _chi2;
    private double _ADCsum;
    
    public FinalTrackInfo(){}
    
    public FinalTrackInfo(double px, double py, double pz, double vz, double theta, double phi, int numhits, double tl, double ADCsum, double dEdx, double R, double A, double B, double chi2){
        _px = px;
        _py = py;
        _pz = pz;
        _vz = vz;
        _theta = theta;
        _phi = phi;
        _numhits = numhits;
        _tl = tl;
        _dEdx = dEdx;
        _R = R;
        _A = A;
        _B = B;
        _chi2 = chi2;
        _ADCsum = ADCsum;
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
    public void set_theta(double theta){
        _theta = theta;
    }
    public void set_phi(double phi){
        _phi = phi;
    }
    public void set_numhits(int numhits){
        _numhits = numhits;
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
    public double get_theta(){
        return _theta;
    }
    public double get_phi(){
        return _phi;
    }
    public int get_numhits(){
        return _numhits;
    }
    public double get_dEdx(){
        return _dEdx;
    }
    public double get_vz(){
        return _vz;
    }
    public double get_R(){
        return _R;
    }
    public double get_A(){
        return _A;
    }
    public double get_B(){
        return _B;
    }
    public double get_chi2(){
        return _chi2;
    }
    public double get_ADCsum(){
        return _ADCsum;
    }
}
