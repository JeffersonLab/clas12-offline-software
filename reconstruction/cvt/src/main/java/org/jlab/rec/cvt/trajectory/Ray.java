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
        
    public Point3D getRefPoint() {
        return _refPoint;
    }

    public void setRefPoint(Point3D _refPoint) {
        this._refPoint = _refPoint;
    }

    public Vector3D getDirVec() {
        return _dirVec;
    }

    public void setDirVec(Vector3D _dirVec) {
        this._dirVec = _dirVec;
    }

    public double getYXSlope() {
        return _yxslope;
    }

    public void setYXSlope(double _yxslope) {
        this._yxslope = _yxslope;
    }

    public double getYZSlope() {
        return _yzslope;
    }

    public void setYZSlope(double _yzslope) {
        this._yzslope = _yzslope;
    }

    public double getYXInterc() {
        return _yxinterc;
    }

    public void setYXInterc(double _yxinterc) {
        this._yxinterc = _yxinterc;
    }

    public double getYZInterc() {
        return _yzinterc;
    }

    public void setYZInterc(double _yzinterc) {
        this._yzinterc = _yzinterc;
    }

    public double getYXSlopeErr() {
        return _yxslopeErr;
    }

    public void setYXSlopeErr(double _yxslopeErr) {
        this._yxslopeErr = _yxslopeErr;
    }

    public double getYZSlopeErr() {
        return _yzslopeErr;
    }

    public void setYZSlopeErr(double _yzslopeErr) {
        this._yzslopeErr = _yzslopeErr;
    }

    public double getYXIntercErr() {
        return _yxintercErr;
    }

    public void setYXInterErr(double _yxintercErr) {
        this._yxintercErr = _yxintercErr;
    }

    public double getYZIntercErr() {
        return _yzintercErr;
    }

    public void setYZIntercErr(double _yzintercErr) {
        this._yzintercErr = _yzintercErr;
    }
    
    public double[][] getCovMat() {
        double[][] cov = new double[5][5];
        cov[0][0] = this.getYXIntercErr()*this.getYXIntercErr();
        cov[1][1] = this.getYZIntercErr()*this.getYZIntercErr();
        cov[2][2] = this.getYXSlopeErr()*this.getYXSlopeErr();
        cov[3][3] = this.getYZSlopeErr()*this.getYZSlopeErr();
        cov[4][4] = 1;
        return cov;
    }

    public Line3D toLine() {
        return new Line3D(this.getRefPoint(), this.getDirVec());
    }
}
