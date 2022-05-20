package org.jlab.clas.tracking.trackrep;
import org.jlab.clas.tracking.kalmanfilter.Units;
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
    private double _cosphi0;
    private double _sinphi0;
    private double _omega;
    private double _z0;
    private double _tanL;
    private int    _turningSign;
    private double _R;
        
    private double _xd; 
    private double _yd;
    private double _xc;
    private double _yc;
    private double _xb;
    private double _yb;
    private double _x;
    private double _y;
    private double _z;
    private double _px;
    private double _py;
    private double _pz;
    
    private Units units = Units.CM; //default
    
    public final static double LIGHTVEL = 0.0000299792458;       // velocity of light - conversion factor from radius in cm to momentum in GeV/c 
    
    public Helix() {
        
    }
    
    public Helix(double d0, double phi0, double omega, double z0, double tanL,
            int turningSign, double B, double xb, double yb, Units unit) {
        _d0          = d0;
        _phi0        = phi0;
        _cosphi0     = Math.cos(phi0);
        _sinphi0     = Math.sin(phi0);
        _omega       = omega;
        _z0          = z0;
        _tanL        = tanL;
        _turningSign = turningSign;
        _B           = B;
        _xb          = xb;
        _yb          = yb;
        units        = unit;
        this.update();
    }
    
    public Helix(double x0, double y0, double z0, double px0, double py0, double pz0,
            int q, double B, double xb, double yb, Units unit) {
        _turningSign = q;
        _B           = B;
        units        = unit;
        double pt    = Math.sqrt(px0*px0 + py0*py0);
        _R           = pt/(B*this.getLightVelocity());
        _cosphi0     = px0/pt;
        _sinphi0     = py0/pt;
        _phi0        = Math.atan2(py0, px0);
        _tanL        = pz0/pt;
        _z0          = z0;
        _omega       = (double) -_turningSign/_R;
        double S = Math.sin(_phi0);
        double C = Math.cos(_phi0);
        if(Math.abs(S)>=Math.abs(C)) {
            _d0 = -(x0-xb)/S;
        } else {
            _d0 = (y0-yb)/C;
        }
        _xb = xb;
        _yb = yb;
        this.update();
    }
    
    
    public Units getUnits() {
        return this.units;
    }

    public final double getLightVelocity() {
        return LIGHTVEL*units.value();
    }    
        
    public void reset(double d0, double phi0, double omega, double z0, double tanL, double B){
        _d0          = d0;
        _phi0        = phi0;
        _cosphi0     = Math.cos(phi0);
        _sinphi0     = Math.sin(phi0);
        _omega       = omega;
        _z0          = z0;
        _tanL        = tanL;
        _B           = B;
        this.update();
    }
    
    public final void update() {
        setR(1./Math.abs(getOmega()));
        _xd = -getD0()*getSinphi0()+_xb;
        _yd =  getD0()*getCosphi0()+_yb;
        _xc = -(_turningSign*_R + _d0)*getSinphi0()+_xb;
        _yc =  (_turningSign*_R + _d0)*getCosphi0()+_yb;
        _x  = getX(0);
        _y  = getY(0);
        _z  = getZ(0);
        _px = getPx(getB(), 0);
        _py = getPy(getB(), 0);
        _pz = getPz(getB()); 
    }
    
    public double getB() {
        return _B;
    }

    public double getD0() {
        return _d0;
    }

    public double getPhi0() {
        return _phi0;
    }

    public double getCosphi0() {
        return _cosphi0;
    }

    public double getSinphi0() {
        return _sinphi0;
    }

    public double getOmega() {
        return _omega;
    }

    public double getZ0() {
        return _z0;
    }

    public double getTanL() {
        return _tanL;
    }

    public int getTurningSign() {
        return _turningSign;
    }

    public double getR() {
        return _R;
    }

    public void setR(double _R) {
        this._R = _R;
    }


    public double getXc() {
        return _xc;
    }

    public double getYc() {
        return _yc;
    }

    public double getXb() {
        return _xb;
    }

    public double getYb() {
        return _yb;
    }

    public double getPhi(double l) {
        return getPhi0() + getOmega()*l;
    }
    
    public double getPt(double B) {
        return getLightVelocity() * getR() * B;
    }
    
    public double getX(double l){
        return getXc() + getTurningSign()*getR()*Math.sin(getPhi(l));
    }
    
    public double getY(double l){
        return getYc() - getTurningSign()*getR()*Math.cos(getPhi(l));
    }
    
    public double getZ(double l){
        return getZ0() -l*getTanL();
    }
    
    public double getPx(double B, double l) {
        return getPt(B) * Math.cos(getPhi(l));
    }
    
    public double getPy(double B, double l) {
        return getPt(B) * Math.sin(getPhi(l));
    }
    
    public double getPz(double B) {
        return getPt(B)*getTanL();
    }

    public double getX() {
        return this.getX(0);
    }

    public double getY() {
        return this.getY(0);
    }

    public double getZ() {
        return this.getZ(0);
    }

    public double getPx() {
        return this.getPx(this.getB(), 0);
    }

    public double getPy() {
        return this.getPy(this.getB(), 0);
    }

    public double getPz() {
        return this.getPz(this.getB());
    }

    public double getLAtPlane(double X1, double Y1, double X2, double Y2, 
            double tolerance) {
        // Find the intersection of the helix circle with the module plane projection in XY which is a line
        // Plane representative line equation y = mx +d
        double X = 0;
        double Y = 0;
        if (X2 - X1 == 0) {
            X = X1;
            double y1 = getYc() + Math.sqrt(getR() * getR() - (X - getXc()) * (X - getXc()));
            double y2 = getYc() - Math.sqrt(getR() * getR() - (X - getXc()) * (X - getXc()));

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
            double x1 = getXc() + Math.sqrt(getR() * getR() - (Y - getYc()) * (Y - getYc()));
            double x2 = getXc() - Math.sqrt(getR() * getR() - (Y - getYc()) * (Y - getYc()));

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
                    (1 + m * m) * (getXc() * getXc() + (d - getYc()) * (d - getYc()) - getR()*getR());
            if (del < 0) {
                //System.err.println("Helix Plane Intersection error - Returning 0 ");
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
            double y1 = getYc() + Math.sqrt(getR() * getR() - (X - getXc()) * (X - getXc()));
            double y2 = getYc() - Math.sqrt(getR() * getR() - (X - getXc()) * (X - getXc()));

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
        
        return dphi/getOmega();
    }
    
    
    
    public Point3D getHelixPointAtPlane(double X1, double Y1, double X2, double Y2, 
            double tolerance) {
        double l = getLAtPlane(X1, Y1, X2, Y2, tolerance);
        return new Point3D(getX(l),getY(l),getZ(l));
    }
    
    public Vector3D getMomentumAtPlane(double X1, double Y1, double X2, double Y2, 
            double tolerance) {
        double l = getLAtPlane(X1, Y1, X2, Y2, tolerance);
        return new Vector3D(getPx(getB(),l),getPy(getB(),l),getPz(getB()));
    }
    
    public double getLAtR(double r) {
        
        double x;
        double y;
        double xp = 0;
        double xm = 0;
        double yp = 0;
        double ym = 0;
        if( Math.abs(getYc()) >1.e-09) {
        double a = 0.5 * (r * r - getR() * getR() + getXc() * getXc() + getYc() * getYc()) / getYc();
            double b = -getXc() / getYc();

            double delta = a * a * b * b - (1 + b * b) * (a * a - r * r);

            xp = (-a * b + Math.sqrt(delta)) / (1 + b * b);
            xm = (-a * b - Math.sqrt(delta)) / (1 + b * b);

            yp = a + b * xp;
            ym = a + b * xm;
        } else {
            xm = (getXc()*getXc()-getR()*getR()+r*r)/(2*getXc());
            xp = xm;
            ym = Math.sqrt(r*r-xm*xm); 
            yp = Math.sqrt(r*r-xp*xp);
        }
        //double Cp = new Vector3D(xp,yp,0).asUnit().dot(new Vector3D(Math.cos(getPhi0()), Math.sin(getPhi0()),0));
        //double Cm = new Vector3D(xm,ym,0).asUnit().dot(new Vector3D(Math.cos(getPhi0()), Math.sin(getPhi0()),0));
        double Np = Math.sqrt(xp*xp+yp*yp);
        double Nm = Math.sqrt(xm*xm+ym*ym);
        double Cp = (xp*getCosphi0()+yp*getSinphi0())/Np;
        double Cm = (xm*getCosphi0()+ym*getSinphi0())/Nm;
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
        
        return dphi/getOmega();
    }
    
    public Point3D getHelixPointAtR(double r) {
        double l = getLAtR( r);
        return new Point3D(getX(l),getY(l),getZ(l));
    }
    
    public Vector3D getMomentumAtR(double r) {
        double l = getLAtR( r);
        return new Vector3D(getPx(getB(),l),getPy(getB(),l),getPz(getB()));
    }
    
    public double getLAtZ(double z) {
        return (z - getZ0())/getTanL();
    }
    
    public Point3D getHelixPointAtZ(double z) {
        double l = getLAtZ( z);
        return new Point3D(getX(l),getY(l),z);
    }
    
    public Vector3D getMomentumAtZ(double z) {
        double l = getLAtZ( z);
        return new Vector3D(getPx(getB(),l),getPy(getB(),l),getPz(getB()));
    }
    
    @Override
    public String toString() {
        String s = String.format("    drho=%.4f phi0=%.4f radius=%.4f z0=%.4f tanL=%.4f B=%.4f\n", this._d0, this._phi0, this._R, this._z0, this._tanL, this._B);
        s       += String.format("    x0=%.4f y0=%.4f x=%.4f y=%.4f z=%.4f px=%.4f py=%.4f pz=%.4f", this._xb, this._yb, this._x, this._y, this._z, this._px, this._py, this._pz);
        return s;
    }
}
