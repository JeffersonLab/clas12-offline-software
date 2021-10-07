package org.jlab.rec.rtpc.hit;

public class KalmanFilterTrackInfo {
    private double _px;
    private double _py;
    private double _pz;
    private double _vz;
    private double _theta;
    private double _phi;

    public KalmanFilterTrackInfo(double _px, double _py, double _pz, double _vz, double _theta, double _phi) {
        this._px = _px;
        this._py = _py;
        this._pz = _pz;
        this._vz = _vz;
        this._theta = _theta;
        this._phi = _phi;
    }

    public double get_px() {
        return _px;
    }

    public void set_px(double _px) {
        this._px = _px;
    }

    public double get_py() {
        return _py;
    }

    public void set_py(double _py) {
        this._py = _py;
    }

    public double get_pz() {
        return _pz;
    }

    public void set_pz(double _pz) {
        this._pz = _pz;
    }

    public double get_vz() {
        return _vz;
    }

    public void set_vz(double _vz) {
        this._vz = _vz;
    }

    public double get_theta() {
        return _theta;
    }

    public void set_theta(double _theta) {
        this._theta = _theta;
    }

    public double get_phi() {
        return _phi;
    }

    public void set_phi(double _phi) {
        this._phi = _phi;
    }

}
