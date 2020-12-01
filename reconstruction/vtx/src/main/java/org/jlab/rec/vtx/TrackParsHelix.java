package org.jlab.rec.vtx;

import org.jlab.geom.prim.Point3D;

public class TrackParsHelix {

	public TrackParsHelix() {
		// TODO Auto-generated constructor stub
	}
	
	
	private double _dca ;	 	// distance of closest approach to the z-axis in the lab frame
	private double _phi_dca;	// azimuth at the DOCA
	private double _rho ;         	// track curvature = 1/R, where R is the radius of the circle 
	private double _z_0 ;	       	// intersection of the helix axis with the z-axis
	private double _tan_lambda ;	// tangent of the dip angle 
	private double _q;
	private double _p;

	private double _r;  //  radius of the circle 
	private double _cosphidca ;
	private double _sinphidca ;
	
	private double _x;  // x of the reference point
	private double _y;  // y of the reference point
	private double _z;  // z of the reference point

	private double _x0;  // 
	private double _y0;  // 
	private double _z0;  // 

	
	public double Bfield = 5.0;
	
	public double get_x() {
		return _x;
	}

	public double get_y() {
		return _y;
	}

	public double get_z() {
		return _z;
	}
	
	public double get_dca() {
		return _dca;
	}

	public void set_dca(double _dca) {
		this._dca = _dca;
	}

	public double get_phi_dca() {
		return _phi_dca;
	}

	public void set_phi_dca(double _phi_dca) {
		this._phi_dca = _phi_dca;
	}

	public double get_rho() {
		return _rho;
	}
	public double get_q() {
		return _q;
	}
	public double get_p() {
		return _p;
	}

	public void set_rho(double _rho) {
		this._rho = _rho;
	}

	public double get_z_0() {
		return _z_0;
	}

	public void set_z_0(double _z_0) {
		this._z_0 = _z_0;
	}

	public double get_tan_lambda() {
		return _tan_lambda;
	}

	public void set_tan_lambda(double _tan_lambda) {
		this._tan_lambda = _tan_lambda;
	}
	public void set_q(double _q) {
		this._q = _q;
	}
	public void set_p(double _p) {
		this._p = _p;
	}

	final double LIGHTVEL = 0.000299792458 ; 
	public void setHelixParams(double x, double y, double z,
			                   double px, double py, double pz, 
			                   double Q, double Bfield) {
		   
            double pt = Math.sqrt(px*px + py*py);			

            // 5-parameter helix representation
            this._dca = Math.atan2(-x, y);	 			// set distance of closest approach to the z-axis in the lab frame
            this._phi_dca = -Math.atan2(px, py);		// set azimuth at the DOCA
            this._rho = (LIGHTVEL * Bfield) / (Q*pt); 	// set track curvature = 1/R, where R is the radius of the circle 
            this._z_0 = z;	              				// set intersection of the helix axis with the z-axis
            this._tan_lambda = pz/pt;	       			// set tangent of the dip angle  
            this._q = Q;

            this._r = 1./this._rho;
            this._sinphidca = Math.sin(_phi_dca);
            this._cosphidca = Math.cos(_phi_dca);        

            this._x = x;  // x of the reference point 
            this._y = y;  // y of the reference point
            this._z = z;  // z of the reference point

            this._x0 = x - this._dca*Math.cos(this._phi_dca);
            this._y0 = y - this._dca*Math.sin(this._phi_dca);
            this._z0 = z - this._z_0;

            this._p = Math.sqrt(px*px+py*py+pz*pz);
        
	} // end setHelixParams()

	
	public void setHelixParams(double pt, double phi0, double d0, double z0, 
			                   double tandip, double Q, double Bfield) {
            this._rho = (LIGHTVEL * Bfield) / (Q* pt);
            this._z_0 = z0;	              				
            this._tan_lambda = tandip;
            this._dca = d0;
            this._phi_dca = phi0;
            this._q = Q;
            this._r = 1./this._rho;
            this._sinphidca = Math.sin(_phi_dca);
            this._cosphidca = Math.cos(_phi_dca);    
	}
	
	
	// calculate coordinates of the point of the helix curve from the parameter phi.
	// phi=0 corresponds to the ref. point given by the DC track
	public Point3D calcPoint(double phi) {
		
            double x = _x0 + _dca*_cosphidca + _r*(_cosphidca - Math.cos(_phi_dca+phi));
            double y = _y0 + _dca*_sinphidca + _r*(_sinphidca - Math.sin(_phi_dca+phi));
            double z = _z0 + _z_0            - _r*_tan_lambda*phi;

            return new Point3D(x,y,z);
		
	}


}
