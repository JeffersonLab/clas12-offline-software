/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.trackrep;
import org.jlab.clas.tracking.Constants;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author ziegler
 */
public class Helix {

    //Setting for Negative polarity of the solenoid.
    // turningSign = -charge*polarity = + charge for nominal configuration
    
    private double _B;
    private double _d0;
    private double _phi0;
    private double _omega;
    private double _z0;
    private double _tanL;
    private int _turningSign;
    
    private double tFlightLen = 0;
    private double _R;
    
    private double _xd; 
    private double _yd;
    private double _xc;
    private double _yc;
    private double _x;
    private double _y;
    private double _z;
    private double _px;
    private double _py;
    private double _pz;
    
    
    public Helix(double d0, double phi0, double omega, double z0, double tanL,
            int turningSign, double B) {
        _d0             = d0;
        _phi0           = phi0;
        _omega          = omega;
        _z0             = z0;
        _tanL           = tanL;
        _turningSign    = turningSign;
        _B              = B;
        
        this.Update();
    }
    
    public Helix(double x0, double y0, double z0, double px0, double py0, double pz0,
            int q, double B) {
        _turningSign = q;
        _B = B;
        double pt = Math.sqrt(px0*px0 + py0*py0);
        _R = pt/(B*Constants.LIGHTVEL);
        _phi0 = Math.atan2(py0, px0);
        _tanL = pz0/pt;
        _z0 = z0;
        _omega = (double) q/_R;
        
        double S = Math.sin(_phi0);
        double C = Math.cos(_phi0);
        
        if(Math.abs(S)>=Math.abs(C)) {
            _d0 = -x0/S;
        } else {
            _d0 = y0/C;
        }
        
        this.Update();
    }
    
    public double getPhi(double l) {
        return _phi0 + _omega*l;
    }
    
    public double getPt(double B) {
        return Constants.LIGHTVEL * _R * B;
    }
    public double getX(double l){
        return getXc() + _turningSign*_R*Math.sin(getPhi(l));
    }
    public double getY(double l){
        return getYc() - _turningSign*_R*Math.cos(getPhi(l));
    }
    public double getZ(double l){
        return _z0 + _turningSign*l*_tanL;
    }
    public double getPx(double B, double l) {
        return getPt(B) * Math.cos(getPhi(l));
    }
    public double getPy(double B, double l) {
        return getPt(B) * Math.sin(getPhi(l));
    }
    public double getPz(double B) {
        return getPt(B)*_tanL;
    }
    public void Reset(double d0, double phi0, double omega, double z0, double tanL,
            double B){
        _d0             = d0;
        _phi0           = phi0;
        _omega          = omega;
        _z0             = z0;
        _tanL           = tanL;
        _B              = B;
        
        this.Update();
    }
    public void Update() {
        _R  = 1./Math.abs(_omega);
        _xd = -_d0*Math.sin(_phi0);
        _yd =  _d0*Math.cos(_phi0);
        setXc(-(_turningSign*_R + _d0)*Math.sin(_phi0));
        setYc((_turningSign*_R + _d0)*Math.cos(_phi0));
        setX(getX(tFlightLen));
        setY(getY(tFlightLen));
        setZ(getZ(tFlightLen));
        setPx(getPx(_B, tFlightLen));
        setPy(getPy(_B, tFlightLen));
        setPz(getPz(_B)); 
    }
    
    public double getLAtPlane(double X1, double Y1, double X2, double Y2, 
            double tolerance) {
        // Find the intersection of the helix circle with the module plane projection in XY which is a line
        // Plane representative line equation y = mx +d
        double X = 0;
        double Y = 0;
        if (X2 - X1 == 0) {
            X = X1;
            double y1 = getYc() + Math.sqrt(_R * _R - (X - getXc()) * (X - getXc()));
            double y2 = getYc() - Math.sqrt(_R * _R - (X - getXc()) * (X - getXc()));

            if (Math.abs(y1 - Y1) < Math.abs(Y2 - Y1)+tolerance) {
                Y = y1;
            } else {
                if (Math.abs(y2 - Y2) < Math.abs(Y2 - Y1)+tolerance) {
                    Y = y2;
                }
            }
        }
        if (Y2 - Y1 == 0) {
            Y = Y1;
            double x1 = getXc() + Math.sqrt(_R * _R - (Y - getYc()) * (Y - getYc()));
            double x2 = getXc() - Math.sqrt(_R * _R - (Y - getYc()) * (Y - getYc()));

            if (Math.abs(x1 - X1) < Math.abs(X2 - X1)+tolerance) {
                X = x1;
            } else {
                if (Math.abs(x2 - X1) < Math.abs(X2 - X1)+tolerance) {
                    X = x2;
                }
            }
        }

        if (X2 - X1 != 0 && Y2 - Y1 != 0) {
            double m = (Y2 - Y1) / (X2 - X1);
            double d = Y1 - X1 * m;

            //double del = r*r*(1+m*m) - (yc-m*xc-d)*(yc-m*xc-d);
            double del = (getXc() + (-d + getYc()) * m) * (getXc() + (-d + getYc()) * m) - 
                    (1 + m * m) * (getXc() * getXc() + (d - getYc()) * (d - getYc()) - _R*_R);
            if (del < 0) {
                System.err.println("Helix Plane Intersection error - Returning 0 ");
                return 0;
            }
            double x1 = (getXc() + (-d + getYc()) * m + Math.sqrt(del)) / (1 + m * m);
            double x2 = (getXc() + (-d + getYc()) * m - Math.sqrt(del)) / (1 + m * m);

            if (Math.abs(x1 - X1) < Math.abs(X2 - X1)+tolerance) {
                X = x1;
            } else {
                if (Math.abs(x2 - X1) < Math.abs(X2 - X1)+tolerance) {
                    X = x2;
                }
            }
            double y1 = getYc() + Math.sqrt(_R * _R - (X - getXc()) * (X - getXc()));
            double y2 = getYc() - Math.sqrt(_R * _R - (X - getXc()) * (X - getXc()));

            if (Math.abs(y1 - Y1) < Math.abs(Y2 - Y1)+tolerance) {
                Y = y1;
            } else {
                if (Math.abs(y2 - Y1) < Math.abs(Y2 - Y1)+tolerance) {
                    Y = y2;
                }
            }
        }
        
        double phi1 = Math.atan2(_yd -getYc(), _xd -getXc());
        double phi2 = Math.atan2(Y -getYc(), X -getXc());
        double dphi = phi2 - phi1;
        //  put dphi in (-pi, pi)
        if (dphi > Math.PI) {
            dphi -= 2. * Math.PI;
        }
        if (dphi < -Math.PI) {
            dphi += 2. * Math.PI;
        }
        
        return dphi/_omega;
    }
    
    
    
    public Point3D getHelixPointAtPlane(double X1, double Y1, double X2, double Y2, 
            double tolerance) {
        double l = getLAtPlane(X1, Y1, X2, Y2, tolerance);
        return new Point3D(getX(l),getY(l),getZ(l));
    }
    public Vector3D getMomentumAtPlane(double X1, double Y1, double X2, double Y2, 
            double tolerance) {
        double l = getLAtPlane(X1, Y1, X2, Y2, tolerance);
        return new Vector3D(getPx(_B,l),getPy(_B,l),getPz(_B));
    }
    
    /**
     * 
     * @param r radius
     * @return Computes intersection of helix with circle centered at 0 and of radius R
     */
    public double getLAtR(double r) {
        
        double x;
        double y;
        
        double a = 0.5 * (r * r - _R * _R + getXc() * getXc() + getYc() * getYc()) / getYc();
        double b = -getXc() / getYc();

        double delta = a * a * b * b - (1 + b * b) * (a * a - r * r);

        double xp = (-a * b + Math.sqrt(delta)) / (1 + b * b);
        double xm = (-a * b - Math.sqrt(delta)) / (1 + b * b);

        double yp = a + b * xp;
        double ym = a + b * xm;
        
        double Cp = new Vector3D(xp,yp,0).asUnit().dot(new Vector3D(Math.cos(_phi0), Math.sin(_phi0),0));
        double Cm = new Vector3D(xm,ym,0).asUnit().dot(new Vector3D(Math.cos(_phi0), Math.sin(_phi0),0));
        
        if(Cp > Cm) {
            x = xp;
            y = yp;
        } else {
            x = xm;
            y = ym;
        }
       
        double phi1 = Math.atan2(_yd -getYc(), _xd -getXc());
        double phi2 = Math.atan2(y -getYc(), x -getXc());
        double dphi = phi2 - phi1;
        //  put dphi in (-pi, pi)
        if (dphi > Math.PI) {
            dphi -= 2. * Math.PI;
        }
        if (dphi < -Math.PI) {
            dphi += 2. * Math.PI;
        }
        
        return dphi/_omega;
    }
    public Point3D getHelixPointAtR(double r) {
        double l = getLAtR( r);
        return new Point3D(getX(l),getY(l),getZ(l));
    }
    public Vector3D getMomentumAtR(double r) {
        double l = getLAtR( r);
        return new Vector3D(getPx(_B,l),getPy(_B,l),getPz(_B));
    }
    public double getLAtZ(double z) {
        return (z - _z0)/_tanL;
    }
    public Point3D getHelixPointAtZ(double z) {
        double l = getLAtZ( z);
        return new Point3D(getX(l),getY(l),getZ(l));
    }
    public Vector3D getMomentumAtZ(double z) {
        double l = getLAtZ( z);
        return new Vector3D(getPx(_B,l),getPy(_B,l),getPz(_B));
    }
    /**
     * @return the _x
     */
    public double getX() {
        return _x;
    }

    /**
     * @param _x the _x to set
     */
    public void setX(double _x) {
        this._x = _x;
    }

    /**
     * @return the _y
     */
    public double getY() {
        return _y;
    }

    /**
     * @param _y the _y to set
     */
    public void setY(double _y) {
        this._y = _y;
    }

    /**
     * @return the _z
     */
    public double getZ() {
        return _z;
    }

    /**
     * @param _z the _z to set
     */
    public void setZ(double _z) {
        this._z = _z;
    }

    /**
     * @return the _px
     */
    public double getPx() {
        return _px;
    }

    /**
     * @param _px the _px to set
     */
    public void setPx(double _px) {
        this._px = _px;
    }

    /**
     * @return the _py
     */
    public double getPy() {
        return _py;
    }

    /**
     * @param _py the _py to set
     */
    public void setPy(double _py) {
        this._py = _py;
    }

    /**
     * @return the _pz
     */
    public double getPz() {
        return _pz;
    }

    /**
     * @param _pz the _pz to set
     */
    public void setPz(double _pz) {
        this._pz = _pz;
    }

    /**
     * @return the _xc
     */
    public double getXc() {
        return _xc;
    }

    /**
     * @param _xc the _xc to set
     */
    public void setXc(double _xc) {
        this._xc = _xc;
    }

    /**
     * @return the _yc
     */
    public double getYc() {
        return _yc;
    }

    /**
     * @param _yc the _yc to set
     */
    public void setYc(double _yc) {
        this._yc = _yc;
    }
    
}
