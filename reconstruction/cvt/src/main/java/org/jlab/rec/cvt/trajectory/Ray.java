package org.jlab.rec.cvt.trajectory;

import org.jlab.geom.prim.Line3D;
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
    public double chi2;
    
    public Ray(Point3D refPoint, Vector3D dirVec) {
        if(dirVec.y()!=0) {
            double t = -refPoint.y()/dirVec.y();
            refPoint.translateXYZ(t*dirVec.x(), t*dirVec.y(), t*dirVec.z());
            dirVec = dirVec.multiply(1/dirVec.y());
            this._refPoint = refPoint;
            this._dirVec   = dirVec.asUnit();
            this._yxinterc = refPoint.x();
            this._yzinterc = refPoint.z();
            this._yxslope  = dirVec.x();
            this._yzslope  = dirVec.z();
        }
    }

    public Ray(double yxSlope, double yxInterc, double yzSlope, double yzInterc) {
        this(yxSlope, 0, yxInterc, 0, yzSlope, 0, yzInterc,0);       
    }
    
    public Ray(double yxSlope, double yxSlopeErr, double yxInterc, double yxIntercErr,
               double yzSlope, double yzSlopeErr, double yzInterc, double yzIntercErr) {
        
        this._yxslope     = yxSlope;
        this._yxslopeErr  = yxSlopeErr;
        this._yxinterc    = yxInterc;
        this._yxintercErr = yxIntercErr;
        this._yzslope     = yzSlope;
        this._yzslopeErr  = yzSlopeErr;
        this._yzinterc    = yzInterc;
        this._yzintercErr = yzIntercErr;

        this._refPoint = new Point3D(yxInterc, 0, yzInterc);
        this._dirVec   = new Vector3D(yxSlope, 1, yzSlope).asUnit();

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
    
    public double[][] getCovMat() {
        double[][] cov = new double[5][5];
        cov[0][0] = this.get_yxintercErr()*this.get_yxintercErr();
        cov[1][1] = this.get_yzintercErr()*this.get_yzintercErr();
        cov[2][2] = this.get_yxslopeErr()*this.get_yxslopeErr();
        cov[3][3] = this.get_yzslopeErr()*this.get_yzslopeErr();
        cov[4][4] = 1;
        return cov;
    }

    public Line3D toLine() {
        return new Line3D(this.get_refPoint(), this.get_dirVec());
    }
}
