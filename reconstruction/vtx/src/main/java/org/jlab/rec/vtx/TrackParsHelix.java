package org.jlab.rec.vtx;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class TrackParsHelix {

    /**
     * @return the _id1
     */
    public int getId() {
        return _id;
    }

    /**
     * @param _id1 the _id1 to set
     */
    public void setId(int _id1) {
        this._id = _id1;
    }


	public TrackParsHelix(int i1) {
            _id = i1;
            
	}
	
	private int _id;               // id of the track
	private double _dca ;	 	// distance of closest approach to the z-axis in the lab frame
	private double _phi_dca;	// azimuth at the DOCA
	private double _rho ;         	// track curvature = 1/R, where R is the radius of the circle 
	private double _z_0 ;	       	// intersection of the helix axis with the z-axis
	private double _tan_lambda ;	// tangent of the dip angle 
	private double _q;
	private double _p;
        private double _pt;

	private double _r;  //  radius of the circle 
	private double _cosphidca ;
	private double _sinphidca ;
	
	private double _x;  // x of the reference point
	private double _y;  // y of the reference point
	private double _z;  // z of the reference point

	private double _x0;  // 
	private double _y0;  // 
	private double _z0;  // 

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
        public double get_pt() {
		return _pt;
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
        public void set_pt(double _pt) {
		this._pt = _pt;
	}

	final double LIGHTVEL =  0.0000299792458 ; 
	public void setHelixParams(double x0, double y0, double z0,
			                   double px0, double py0, double pz0, 
			                   double q, double Bf) { 
            
            double xb=0; double yb=0;  // no beam offset
            double pt = Math.sqrt(px0*px0 + py0*py0);
            
            this._tan_lambda = pz0/pt;
            this._z_0 = z0;
            double omega = -q*(Bf*LIGHTVEL)/pt;
            double phi0=Math.atan2(py0, px0);
            double S = Math.sin(Math.atan2(py0, px0));
            double C = Math.cos(Math.atan2(py0, px0));
            double signedDCA = 0;
            if(Math.abs(S)>=Math.abs(C)) {
                signedDCA = -(x0-xb)/S;
            } else {
                signedDCA = (y0-yb)/C;
            }
            
            this._dca = signedDCA;
            double xcen = (1. / omega- signedDCA) *S;
            double ycen = (-1. / omega + signedDCA) * C;
            double kappa = (double ) -q/pt;
            
            this._phi_dca = Math.atan2(ycen, xcen);
            if (kappa < 0) {
                this._phi_dca = Math.atan2(-ycen, -xcen);
            }
            //this._dca = signedDCA*(Math.cos(phi0)*Math.sin( this._phi_dca) -Math.sin(phi0)*Math.cos( this._phi_dca));

            this._q = q;

            this._rho = omega;
            this._r = 1./omega;
            this._sinphidca = Math.sin(_phi_dca);
            this._cosphidca = Math.cos(_phi_dca);        

            this._x = x0;  // x of the reference point 
            this._y = y0;  // y of the reference point
            this._z = z0;  // z of the reference point

            this._x0 = xb;
            this._y0 = yb;
            this._z0 = 0;
           
            this._pt = Math.sqrt(px0*px0+py0*py0);
            this._p = Math.sqrt(px0*px0+py0*py0+pz0*pz0);
        
	} // end setHelixParams()

	
	// calculate coordinates of the point of the helix curve from the parameter phi.
	// phi=0 corresponds to the ref. point given by the DC track
	public Point3D calcPoint(double phi) {
		
            double x = _x0 + _dca*_cosphidca + _r*(_cosphidca - Math.cos(_phi_dca+phi));
            double y = _y0 + _dca*_sinphidca + _r*(_sinphidca - Math.sin(_phi_dca+phi));
            double z = _z0 + _z_0            - _r*_tan_lambda*phi;

            return new Point3D(x,y,z);
		
	}
        public Vector3D calcDir(double phi) {
            double px = -this._pt * Math.sin(this._phi_dca + phi);
            double py = this._pt * Math.cos(this._phi_dca + phi);
            double pz = this._pt * this._tan_lambda;
            
            return new Vector3D(px,py,pz).asUnit();
        }


}
