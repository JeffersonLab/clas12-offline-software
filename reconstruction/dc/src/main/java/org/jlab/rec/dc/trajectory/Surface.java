package org.jlab.rec.dc.trajectory;

import org.jlab.detector.base.DetectorType;

/**
 *
 * @author ziegler
 */
public class Surface {
    
    private DetectorType _DetectorType;
    public DetectorType getDetectorType() {
        return _DetectorType;
    }
    public void setDetectorType(DetectorType _DetectorType) {
        this._DetectorType = _DetectorType;
    }
    
    private int _DetectorLayer;
    public int getDetectorLayer() {
        return _DetectorLayer;
    }
    public void setDetectorLayer(int _DetectorLayer) {
        this._DetectorLayer = _DetectorLayer;
    }

    public Surface (DetectorType type, int layer, double d, double nx, double ny, double nz) {
        _DetectorType = type;
        _DetectorLayer = layer;
        _d = d;
        _nx = nx;
        _ny = ny;
        _nz = nz;
    }
    private double _d;
    private double _nx;
    private double _ny;
    private double _nz;

    public double get_d() {
        return _d;
    }

    public void set_d(double _d) {
        this._d = _d;
    }

    public double get_nx() {
        return _nx;
    }

    public void set_nx(double _nx) {
        this._nx = _nx;
    }

    public double get_ny() {
        return _ny;
    }

    public void set_ny(double _ny) {
        this._ny = _ny;
    }

    public double get_nz() {
        return _nz;
    }

    public void set_nz(double _nz) {
        this._nz = _nz;
    } 
}
