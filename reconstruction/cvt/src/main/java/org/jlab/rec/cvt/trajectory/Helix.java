package org.jlab.rec.cvt.trajectory;

import java.util.ArrayList;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;

import Jama.Matrix;

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

    private double _dca;          // distance of closest approach to the z-axis in the lab frame
    private double _phi_at_dca;   // azimuth at the DOCA
    private double _curvature;    // track curvature = 1/R, where R is the radius of the circle 
    private double _Z0;           // intersect of the helix axis with the z-axis
    private double _tandip;       // tangent of the dip angle
    private double _dip;          // dip angle 
    private Vector3D _refpoint;     // Reference point needed to define dca
    private Matrix _covmatrix = new Matrix(5, 5);
    //error matrix (assuming that the circle fit and line fit parameters are uncorrelated)
    // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
    // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
    // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
    // | 0                              0                             0                    d_Z0*d_Z0                     |
    // | 0                              0                             0                       0        d_tandip*d_tandip |
    // 

    public Helix(double dca, double phi_at_doca, double curvature, double Z0, double tandip, Vector3D pivot, Matrix covmatrix) {
        set_dca(dca);
        set_phi_at_dca(phi_at_doca);
        set_curvature(curvature);
        set_Z0(Z0);
        set_tandip(tandip);
        set_covmatrix(covmatrix);
        set_pivot(pivot);
    }
    
    public void initialize_with_particle(double vx, double vy, double vz, double px, double py, double pz, double charge, double Bz) {
    	Vector3D piv=new Vector3D(vx,vy,vz);
    	this.set_pivot(piv);
    	this.set_Z0(0);
    	this.set_dca(0);
    	this.set_curvature((Constants.LIGHTVEL * Bz * charge)/Math.sqrt(px*px+py*py));
    	double theta=Math.acos(pz/Math.sqrt(px*px+py*py+pz*pz));
    	this.set_tandip(Math.tan(Math.PI/2.-theta));
    	if (this.get_curvature()< 0) {
            this.set_phi_at_dca(Math.atan2(-this.ycen() + vy, -this.xcen() + vx));
        }
    	else this.set_phi_at_dca(Math.atan2(this.ycen() - vy, this.xcen() - vx));
    }

    public void set_pivot(Vector3D pivot) {
    	_refpoint=pivot;
    }
    
    public Vector3D get_pivot() {
    	return _refpoint;
    }
    
    public double get_dca() {
        return _dca;
    }

    public void set_dca(double dca) {
        this._dca = dca;
    }

    public double get_phi_at_dca() {
        return _phi_at_dca;
    }

    public void set_phi_at_dca(double phi_at_dca) {
        this._phi_at_dca = phi_at_dca;
    }

    public double get_curvature() {
        return _curvature;
    }

    public void set_curvature(double curvature) {
        this._curvature = curvature;
    }

    public double get_Z0() {
        return _Z0;
    }

    public void set_Z0(double Z0) {
        this._Z0 = Z0;
    }

    public double get_tandip() {
        return _tandip;
    }

    public void set_tandip(double tandip) {
        this._tandip = tandip;
        this._dip=Math.atan(tandip);
    }
    
    public double get_dip() {
        return this._dip;
    }

    public Matrix get_covmatrix() {
        return _covmatrix;
    }

    public void set_covmatrix(Matrix covmatrix) {
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
        
        if (_curvature == 0) {
            //System.err.println("Helix Curvature should not be zero");
            return 0;
        }
        return 1. /_curvature;
    }

    //  (x,y) coordinates of the circle center
    public double xcen() {
        return (radius() + this.get_dca()) * Math.sin(this.get_phi_at_dca())+_refpoint.x();
    }

    public double ycen() {
        return (-radius() - this.get_dca()) * Math.cos(this.get_phi_at_dca())+_refpoint.y();
    }

    //  (x,y) coordinates of the dca in lab frame
    public double xdca() {
        return this.get_dca() * Math.sin(this.get_phi_at_dca())+ _refpoint.x();
    }

    public double ydca() {
        return (-this.get_dca() * Math.cos(this.get_phi_at_dca()))+ _refpoint.y();
    }
    
    public double PhiRotdca() {
    	double xd=this.xdca();
    	double yd=this.ydca();
    	double xc=this.xcen();
    	double yc=this.ycen();
    	
    	double phi=Math.atan2(yc-yd, xc-xd);
    	
    	return phi;
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
        int charge = 0;
        charge = (int) Math.signum(_curvature);

        return charge;
    }

   public Point3D getPointAtRadius(double r) {
        // a method to return a point (as a vector) at a given radial position.	
        if (_curvature == 0) {
            return null;
        }

        double d0 = _dca;
        double omega = _curvature;
        double charge = this.get_charge();
        double phi0 = _phi_at_dca;
        double tandip = _tandip;
        double z0 = _Z0;

        double par = 1. - ((r * r - d0 * d0) * omega * omega) / (2. * (1. + d0 * Math.abs(omega)));
        double newPathLength = Math.abs(Math.acos(par) / omega);

        double alpha = -newPathLength * omega;

        double x = d0 * charge * Math.sin(phi0) + (charge / Math.abs(omega)) * (Math.sin(phi0) - Math.cos(alpha) * Math.sin(phi0) - Math.sin(alpha) * Math.cos(phi0));
        double y = -d0 * charge * Math.cos(phi0) - (charge / Math.abs(omega)) * (Math.cos(phi0) + Math.sin(alpha) * Math.sin(phi0) - Math.cos(alpha) * Math.cos(phi0));
        double z = z0 + newPathLength * tandip;

        return new Point3D(x, y, z);
    }
    
    public double getCurvilinearAbsAtRadius(double r) {
        // a method to return a point (as a vector) at a given radial position.	
    	// Works only if ref point is 0,0
        if (_curvature == 0) {
            return Double.NaN;
        }
        
        if (r<Math.abs(this.get_dca())||r>(this.get_dca()+2*Math.abs(this.radius()))) {
        	return Double.NaN;
        }
        
        double d0 = this.get_dca();
        //double Xref=-Math.abs(d0)-Math.abs(this.radius());
        double Xref=-d0-Math.abs(this.radius());
        double radii = this.radius()*this.radius();
        
        /*omega=1/_curvature;
        double par = 1. - ((r * r - d0 * d0) * omega * omega) / (2. * (1. + d0 * omega));
        double s = Math.abs(Math.acos(par) / omega)/Math.cos(_dip);*/
        double x=(radii+Xref*Xref-r*r)/(2.*Xref);
        double phi=Math.PI-Math.acos(x/Math.abs(this.radius()));
        double s=Math.abs(this.radius())*phi/Math.cos(_dip);
        return s;
    }
    
    public Vector3D getHelixPoint(double s) {
    	//S is an absciss along the path of the particle, s1>s0 means the particle is going forward in time
    	double z0=this._Z0;
    	
    	
        double radius = 1./_curvature;
           
        double x=0;
        double y=0;
        double z=0;
        
        double xn=0;
        double yn=0;
        
    	      
        if (Math.abs(_curvature)>1.e-8) {
        	xn = Math.signum(radius)*this._dca+Math.abs(radius)*(1-Math.cos(s*Math.cos(_dip)/Math.abs(radius)));//x0-
        	yn = radius*Math.sin(s*Math.cos(_dip)/Math.abs(radius));
        }
              
        else { //Straight line
        	xn = Math.signum(radius)*this._dca;//x0-
        	yn = Math.signum(radius)*s*Math.cos(_dip);
        }
        double phirot=this.PhiRotdca();
       
        
       x=Math.cos(phirot)*xn-yn*Math.sin(phirot)+ _refpoint.x();
       y=Math.sin(phirot)*xn+yn*Math.cos(phirot)+ _refpoint.y();            
       z = z0 + s * Math.sin(_dip) + _refpoint.z();
        
    	return new Vector3D(x,y,z);
    }
    
    public Vector3D getHelixDir(double s) {
    	   	
        double radius = 1./_curvature;
      
        
        double xn=0;
        double yn=0;
      
        
        double x=0;
        double y=0;
        double z=0;
    	             
        if (Math.abs(_curvature)>1.e-8) {
        	xn = Math.cos(_dip)*Math.sin(s*Math.cos(_dip)/Math.abs(radius));
        	yn = Math.signum(radius)*Math.cos(_dip)*Math.cos(s*Math.cos(_dip)/Math.abs(radius));
        }
        
        else { //Straight line
        	xn = 0;
        	yn = Math.signum(radius)*Math.cos(_dip);
        }
        double phirot=this.PhiRotdca();
       
        x=Math.cos(phirot)*xn-yn*Math.sin(phirot);
        y=Math.sin(phirot)*xn+yn*Math.cos(phirot);  
        z = Math.sin(_dip);
        
    	return new Vector3D(x,y,z);
    }
    

    public Vector3D getTrackDirectionAtRadius(double r) {
        // a method to return a point (as a vector) at a given radial position.
        if (_curvature == 0) {
            return null;
        }

        double d0 = _dca;
        double omega = _curvature;
        double phi0 = _phi_at_dca;

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
    
    public double getCurvAbsForPCAToPoint(Vector3D BeamPoint) {
    	//Return Curvilinear Abscisse to Point of closest approach of the Helix to another point (Beam for instance)
    	double cs=0; 
    	
    	
    	double range=10; //mm... computing distance of 3 points to cylinder or plane
    	double csold=cs;
    	for (int iter=0;iter<5;iter++) {
    		Vector3D inter=this.getHelixPoint(cs);
    		Vector3D interinf=this.getHelixPoint(cs-range);
    		Vector3D intersup=this.getHelixPoint(cs+range);
    	
    		double[][] A=new double[3][3];
    		double[][] B=new double[3][1];
    	
    		B[0][0]=Math.sqrt((inter.x()-BeamPoint.x())*(inter.x()-BeamPoint.x())+(inter.y()-BeamPoint.y())*(inter.y()-BeamPoint.y())); B[0][0]=B[0][0]*B[0][0];
    		B[1][0]=Math.sqrt((interinf.x()-BeamPoint.x())*(interinf.x()-BeamPoint.x())+(interinf.y()-BeamPoint.y())*(interinf.y()-BeamPoint.y())); B[1][0]=B[1][0]*B[1][0];
    		B[2][0]=Math.sqrt((intersup.x()-BeamPoint.x())*(intersup.x()-BeamPoint.x())+(intersup.y()-BeamPoint.y())*(intersup.y()-BeamPoint.y())); B[2][0]=B[2][0]*B[2][0];
    	
    		A[0][0]=cs*cs;
    		A[0][1]=cs;
    		A[0][2]=1;
    	
    		A[1][0]=(cs-range)*(cs-range);
    		A[1][1]=cs-range;
    		A[1][2]=1;
    	
    		A[2][0]=(cs+range)*(cs+range);
    		A[2][1]=cs+range;
    		A[2][2]=1;
    		
    		
    		Matrix matA=new Matrix(A);
    		if (matA.det()>1.e-20) {
    			Matrix invA=matA.inverse();
    			Matrix matB=new Matrix(B);
    			Matrix result=invA.times(matB);
    		
    			cs=-result.get(1, 0)/2./result.get(0, 0);
    			range=Math.abs(cs-csold)/10.;
    		}
    	}
    	
    	
    	return cs;
    }
    
    public double getCurvAbsAtRadius(double radius, Vector3D center) {
    	//Return the CurvAbs for closest intersection with 
    	double cs=Double.NaN;
    	Vector3D[] inter=this.FindIntersectionTwoCircles(center.x(),center.y(),radius,this.xcen(),this.ycen(),Math.abs(this.radius()));
    	if (Double.isNaN(inter[0].x())) return cs;
    	Vector3D start=this.getHelixPoint(0);
    	double d0=Math.sqrt((inter[0].x()-start.x())*(inter[0].x()-start.x())+(inter[0].y()-start.y())*(inter[0].y()-start.y()));
    	double d1=Math.sqrt((inter[1].x()-start.x())*(inter[1].x()-start.x())+(inter[1].y()-start.y())*(inter[1].y()-start.y()));
    	Vector3D v1=new Vector3D(start.x()-this.xcen(),start.y()-this.ycen(),0);
    	if (d0<=d1) {
    		Vector3D v2=new Vector3D(inter[0].x()-this.xcen(),inter[0].y()-this.ycen(),0);
    		cs=Math.abs(this.radius()*v1.angle(v2))/Math.cos(_dip);
    	}
    	if (d1<d0) {
    		Vector3D v2=new Vector3D(inter[1].x()-this.xcen(),inter[1].y()-this.ycen(),0);
    		cs=Math.abs(this.radius()*v1.angle(v2))/Math.cos(_dip);
    	}
    	return cs;
    }


    public Vector3D[] FindIntersectionTwoCircles(double xa, double ya, double Ra, double xb, double yb, double Rb) {
    	double R2=(xa-xb)*(xa-xb)+(ya-yb)*(ya-yb);
    	  double xi=(xa+xb)/2.+(Ra*Ra-Rb*Rb)/2./R2*(xb-xa)+0.5*Math.sqrt(2*(Ra*Ra+Rb*Rb)/R2-(Ra*Ra-Rb*Rb)*(Ra*Ra-Rb*Rb)/R2/R2-1)*(yb-ya);
    	  double yi=(ya+yb)/2.+(Ra*Ra-Rb*Rb)/2./R2*(yb-ya)+0.5*Math.sqrt(2*(Ra*Ra+Rb*Rb)/R2-(Ra*Ra-Rb*Rb)*(Ra*Ra-Rb*Rb)/R2/R2-1)*(xa-xb);
    	  double xii=(xa+xb)/2.+(Ra*Ra-Rb*Rb)/2./R2*(xb-xa)-0.5*Math.sqrt(2*(Ra*Ra+Rb*Rb)/R2-(Ra*Ra-Rb*Rb)*(Ra*Ra-Rb*Rb)/R2/R2-1)*(yb-ya);
    	  double yii=(ya+yb)/2.+(Ra*Ra-Rb*Rb)/2./R2*(yb-ya)-0.5*Math.sqrt(2*(Ra*Ra+Rb*Rb)/R2-(Ra*Ra-Rb*Rb)*(Ra*Ra-Rb*Rb)/R2/R2-1)*(xa-xb);
    	  Vector3D[] inter=new Vector3D[2];
    	  inter[0]=new Vector3D(xi,yi,0);
    	  inter[1]=new Vector3D(xii,yii,0);
    	  return inter;
	}

	public static void main(String arg[]) {
        //  	Helix h = new Helix(0, 0, 1/5., 0, -999, null);

    }
}
