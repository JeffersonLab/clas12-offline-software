package org.jlab.rec.cvt.trajectory;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class Ray {

    private Point3D _refPoint;
    private Vector3D _dirVec;

    private double _yxslope;
    private double _yzslope;
    private double _yxinterc;
    private double _yzinterc;
    private double _yxslopeErr;
    private double _yzslopeErr;
    private double _yxintercErr;
    private double _yzintercErr;

    public Ray(Point3D refPoint, Vector3D dirVec) {
        set_refPoint(refPoint);
        set_dirVec(dirVec);

    }

    public Point3D get_refPoint() {
        return _refPoint;
    }

    public void set_refPoint(Point3D _refPoint) {
        this._refPoint = _refPoint;
    }

    public Vector3D get_dirVec() {
        return _dirVec;
    }

    public void set_dirVec(Vector3D _dirVec) {
        this._dirVec = _dirVec;
    }

    public double get_yxslope() {
        return _yxslope;
    }

    public void set_yxslope(double _yxslope) {
        this._yxslope = _yxslope;
    }

    public double get_yzslope() {
        return _yzslope;
    }

    public void set_yzslope(double _yzslope) {
        this._yzslope = _yzslope;
    }

    public double get_yxinterc() {
        return _yxinterc;
    }

    public void set_yxinterc(double _yxinterc) {
        this._yxinterc = _yxinterc;
    }

    public double get_yzinterc() {
        return _yzinterc;
    }

    public void set_yzinterc(double _yzinterc) {
        this._yzinterc = _yzinterc;
    }

    public double get_yxslopeErr() {
        return _yxslopeErr;
    }

    public void set_yxslopeErr(double _yxslopeErr) {
        this._yxslopeErr = _yxslopeErr;
    }

    public double get_yzslopeErr() {
        return _yzslopeErr;
    }

    public void set_yzslopeErr(double _yzslopeErr) {
        this._yzslopeErr = _yzslopeErr;
    }

    public double get_yxintercErr() {
        return _yxintercErr;
    }

    public void set_yxintercErr(double _yxintercErr) {
        this._yxintercErr = _yxintercErr;
    }

    public double get_yzintercErr() {
        return _yzintercErr;
    }

    public void set_yzintercErr(double _yzintercErr) {
        this._yzintercErr = _yzintercErr;
    }

}
