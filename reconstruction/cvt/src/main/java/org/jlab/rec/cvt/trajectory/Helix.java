package org.jlab.rec.cvt.trajectory;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

//import Jama.Matrix;
import org.jlab.rec.cvt.Constants;

/**
 * A class describing a helix; the helix parameters are field of the track
 * object These parameters are private and accessed by getters: _dca; //
 * distance of closest approach to the z-axis in the lab frame _phi_at_dca; //
 * azimuth at the DOCA _curvature; // track curvature = 1/R, where R is the
 * radius of the circle _Z0; // intersect of the helix axis with the z-axis
 * _tandip; // tangent of the dip angle = pz/pt
 *
 * Has a method getPointAtRadius(double r) which returns the point position
 * along the helix at a given radial distance from the lab origin. This method
 * is used to estimate the intersection of the helix with a plane or a cylinder.
 *
 */
public class Helix {
    public double B = 5.0;
    private double _dca;          // distance of closest approach to the z-axis in the lab frame
    private double _phi_at_dca;   // azimuth at the DOCA
    private double _curvature;    // track curvature = 1/R, where R is the radius of the circle 
    private double _Z0;           // intersect of the helix axis with the z-axis
    private double _tandip;       // tangent of the dip angle
    private double[][] _covmatrix = new double[5][5];
    //error matrix (assuming that the circle fit and line fit parameters are uncorrelated)
    // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
    // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
    // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
    // | 0                              0                             0                    d_Z0*d_Z0                     |
    // | 0                              0                             0                       0        d_tandip*d_tandip |
    // 

    public Helix(double dca, double phi_at_doca, double curvature, double Z0, double tandip, double[][] covmatrix) {
        set_dca(dca);
        set_phi_at_dca(phi_at_doca);
        set_curvature(curvature);
        set_Z0(Z0);
        set_tandip(tandip);
        set_covmatrix(covmatrix);
    }
    
    public Helix(double dca, double phi_at_doca, double curvature, double Z0, double tandip ) {
        set_dca(dca);
        set_phi_at_dca(phi_at_doca);
        set_curvature(curvature);
        set_Z0(Z0);
        set_tandip(tandip);
    }

    public double get_dca() {
        return _dca;
    }

    public final void set_dca(double dca) {
        this._dca = dca;
    }

    public double get_phi_at_dca() {
        return _phi_at_dca;
    }

    public final void set_phi_at_dca(double phi_at_dca) {
        this._phi_at_dca = phi_at_dca;
    }

    public double get_curvature() {
        return _curvature;
    }

    public final void set_curvature(double curvature) {
        this._curvature = curvature;
    }

    public double get_Z0() {
        return _Z0;
    }

    public final void set_Z0(double Z0) {
        this._Z0 = Z0;
    }

    public double get_tandip() {
        return _tandip;
    }

    public final void set_tandip(double tandip) {
        this._tandip = tandip;
    }

    public double[][] get_covmatrix() {
        return _covmatrix;
    }
    
    public final void set_covmatrix(double[][] covmatrix) {
        this._covmatrix = covmatrix;
    }

    //tandip = pz/pt 
    // cos(theta)
    public double costheta() {
        double costh = (this.get_tandip() / Math.sqrt(1 + this.get_tandip() * this.get_tandip()));
        return costh;
    }

    private double invtandip() {
        if (this.get_tandip() == 0) {
            return 0;
        }
        return 1. / this.get_tandip();
    }
    // sin(theta) = pt/p

    public double sintheta() {
        double sinth = (this.invtandip() / Math.sqrt(1 + this.invtandip() * this.invtandip()));
        return sinth;
    }

    // radius of circle
    public double radius() {
        double C = Math.abs(_curvature);
        if (C == 0) {
            System.err.println("Helix Curvature should not be zero ");
            return 0;
        }
        return 1. / C;
    }

    //  (x,y) coordinates of the circle center
    public double xcen() {
        return (radius() - this.get_dca()) * Math.sin(this.get_phi_at_dca());
    }

    public double ycen() {
        return (-radius() + this.get_dca()) * Math.cos(this.get_phi_at_dca());
    }

    //  (x,y) coordinates of the dca
    public double xdca() {
        return -this.get_dca() * Math.sin(this.get_phi_at_dca());
    }

    public double ydca() {
        return this.get_dca() * Math.cos(this.get_phi_at_dca());
    }

    public Point3D getVertex() {
        return new Point3D(this.xdca(),this.ydca(),this.get_Z0());
    }
        
    public double getPt(double solenoidMag) { 
        double pt = Constants.LIGHTVEL * this.radius() * solenoidMag;
        if(solenoidMag<0.001)
            pt = 100;
        return pt;
    }
        
    public Vector3D getPXYZ(double solenoidMag) { 
        double pt = this.getPt(solenoidMag);
        double pz = pt*this.get_tandip();
        double px = pt*Math.cos(this.get_phi_at_dca());
        double py = pt*Math.sin(this.get_phi_at_dca());
        
        return new Vector3D(px,py,pz);
    }
        
    public double getArcLength_dca(Point3D refpoint) {
        //insure that the refpoint is on the helix
        if (refpoint == null) {
            return 0;
        }
        double refX = radius() * Math.cos(refpoint.toVector3D().phi());
        double refY = radius() * Math.sin(refpoint.toVector3D().phi());
        double arclen = ArcLength(xcen(), ycen(), radius(), xcen(), ycen(), refX, refY);
        return arclen;
    }

    // this method finds the arclength between 2 points in a circle
    // this private method is used to get the pathlength from a point on the helical track to the distance of closest approach
    private double ArcLength(double xcenter, double ycenter, double circrad, double x1, double y1, double x2, double y2) {
        //  Find the azimuth of the 2 points and dphi
        double x1toxc = x1 - xcenter;
        double y1toyc = y1 - ycenter;
        double x2toxc = x2 - xcenter;
        double y2toyc = y2 - ycenter;
        double phi1 = Math.atan2(y1toyc, x1toxc);
        double phi2 = Math.atan2(y2toyc, x2toxc);
        double dphi = phi2 - phi1;
        //  put dphi in (-pi, pi)
        if (dphi > Math.PI) {
            dphi -= 2. * Math.PI;
        }
        if (dphi < -Math.PI) {
            dphi += 2. * Math.PI;
        }
        //  calculate the arc length
        double arclen = -circrad * dphi;
        return arclen;
    }

    public int get_charge() {
        int charge = (int) Math.signum(_curvature);

        return charge;
    }

    public Point3D getPointAtRadius(double r) {
        // a method to return a point (as a vector) at a given radial position.	
        double d0 = _dca;
        double omega = _curvature;
        double charge = this.get_charge();
        double phi0 = _phi_at_dca;
        double tandip = _tandip;
        double z0 = _Z0;

        if (Math.abs(_curvature)<1E-5) { // R > 100 m, assume it's straight track
            double x = -d0 * Math.sin(phi0) + r*Math.cos(phi0);
            double y =  d0 * Math.cos(phi0) + r*Math.sin(phi0);
            double z =  z0 + r*tandip;
            return new Point3D(x, y, z);
        }


        double par = 1. - ((r * r - d0 * d0) * omega * omega) / (2. * (1. + d0 * Math.abs(omega)));
        double newPathLength = Math.abs(Math.acos(par) / omega);

        double alpha = -newPathLength * omega;

        double x = d0 * charge * Math.sin(phi0) + (charge / Math.abs(omega)) 
                * (Math.sin(phi0) - Math.cos(alpha) * Math.sin(phi0) - Math.sin(alpha) * Math.cos(phi0));
        double y = -d0 * charge * Math.cos(phi0) - (charge / Math.abs(omega)) 
                * (Math.cos(phi0) + Math.sin(alpha) * Math.sin(phi0) - Math.cos(alpha) * Math.cos(phi0));
        double z = z0 + newPathLength * tandip;

        return new Point3D(x, y, z);
    }

    public Vector3D getTrackDirectionAtRadius(double r) {
        // a method to return a point (as a vector) at a given radial position.
        double d0 = _dca;
        double omega = _curvature;
        double phi0 = _phi_at_dca;
        double tandip = _tandip;
        
        if (Math.abs(_curvature) <1E-5) { // R > 100 m, assume it's straight track
            double ux =  Math.cos(phi0);
            double uy =  Math.sin(phi0);
            double uz =  tandip;
            return new Vector3D(ux, uy, uz);
        }

        double par = 1. - ((r * r - d0 * d0) * omega * omega) / (2. * (1. + d0 * Math.abs(omega)));
        double newPathLength = Math.abs(Math.acos(par) / omega);

        double alpha = newPathLength * omega;

        double sintheta = Math.abs(sintheta());

        double ux = Math.cos(-alpha + phi0) * sintheta;
        double uy = Math.sin(-alpha + phi0) * sintheta;
        double uz = costheta();

        Vector3D trkDir = new Vector3D(ux, uy, uz);

        return trkDir.asUnit();
    }

    @Override
    public String toString() {
        String s = String.format("Helix: Z0=%.4f  R=%.4e  DCA=%.4f  Phi=%.4f  Tan=%.4f", this._Z0,this._curvature,this._dca,this._phi_at_dca,this._tandip);
        return s;
    }
    
    public static void main(String arg[]) {
        //  	Helix h = new Helix(0, 0, 1/5., 0, -999, null);

    }
}
