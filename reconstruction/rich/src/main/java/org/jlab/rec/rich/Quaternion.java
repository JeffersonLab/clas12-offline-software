package org.jlab.rec.rich;

import org.jlab.detector.geant4.v2.SVT.Matrix;

import eu.mihosoft.vrl.v3d.Vector3d;

//Class created by gangel
//for the JLAB package 
//
// The quaternion here is defined as: 
//
public class Quaternion {
	//Definition of the components for the Quaternion
	public double x;
	public double y;
	public double z;
	public double w;

	public Quaternion() {
	}

	public Quaternion(double angle, Vector3d rotationAxis) {
		x = rotationAxis.x * Math.sin(angle / 2);
		y = rotationAxis.y * Math.sin(angle / 2);
		z = rotationAxis.z * Math.sin(angle / 2);
		w = Math.cos(angle / 2);
	}

	public Quaternion(double w, double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	/**
	 *  Set the rotation Quaternion
	 * @param angle the angle of rotation
	 * @param rotationAxis the rotation axis (normal two the vector plane)
	 */
	public void set(double angle, Vector3d rotationAxis) {
		x = rotationAxis.x * Math.sin(angle / 2);
		y = rotationAxis.y * Math.sin(angle / 2);
		z = rotationAxis.z * Math.sin(angle / 2);
		w = Math.cos(angle / 2);
	}

	public void set (double w, double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Set the Quaternion using another one 
	 * @param q
	 */
	public void set (Quaternion q) {
		this.x = q.x;
		this.y = q.y;
		this.z = q.z;
		this.w = q.w;
	}

	public double getSize() {
		return Math.sqrt(w * w + x * x + y * y + z * z);
	}

	public void normalize() {
		double sizeInv = 1 / getSize();
		x *= sizeInv;
		y *= sizeInv;
		z *= sizeInv;
		w *= sizeInv;
	}
	/** 
	 * Multiplication between two Quaternions
	 * @param qb the one that multiply
	 * @param result of the multiplication 
	 */
	public Quaternion multiply(Quaternion qb) {
		Quaternion qa = this;
                Quaternion qr = new Quaternion();
		qr.w = (qa.w * qb.w) - (qa.x * qb.x) - (qa.y * qb.y) - (qa.z * qb.z);
		qr.x = (qa.x * qb.w) + (qa.w * qb.x) + (qa.y * qb.z) - (qa.z * qb.y);
		qr.y = (qa.y * qb.w) + (qa.w * qb.y) + (qa.z * qb.x) - (qa.x * qb.z);
		qr.z = (qa.z * qb.w) + (qa.w * qb.z) + (qa.x * qb.y) - (qa.y * qb.x);
		return qr;
	}

	public void conjugate() {
		//w = w;
		x = -x;
		y = -y;
		z = -z;
	}


    //-----------------
    public Vector3d rotate(Vector3d point) {
    //-----------------
    /** 
     * public void rotate(Vector3d point, Vector3d rotatedPoint) {
     * Implementing the Quaternion Fomula for Rotation: P' = Q.P.Q*
     * @param point the vector 3d that needs to be rotate
     * @param rotatedPoint the rotated vector
     */

        Vector3d rotatedPoint = new Vector3d(0.,0.,0.);
        Quaternion quatTp1    = new Quaternion(0, point.x, point.y, point.z);
        Quaternion quatTp2    = new Quaternion();
        Quaternion quatTp3    = new Quaternion();
        quatTp2 = this.multiply(quatTp1);
        quatTp1.set(this);
        quatTp1.conjugate();
        quatTp3 = quatTp2.multiply(quatTp1);

        rotatedPoint.x = quatTp3.x;
        rotatedPoint.y = quatTp3.y;
        rotatedPoint.z = quatTp3.z;

        return rotatedPoint;
    }

	
	// To finish this code
	public Matrix toRotation(Quaternion q1)
	{
		Matrix Matrice = new Matrix(3,3);
		double heading =0;
		double attitude =0 ;
		double bank=0;
		
			double test = q1.x*q1.y + q1.z*q1.w;
			if (test > 0.499) { // singularity at north pole
				heading = 2 * Math.atan2(q1.x,q1.w);
				attitude = Math.PI/2;
				bank = 0;
				return null;
			}
			if (test < -0.499) { // singularity at south pole
				heading = -2 * Math.atan2(q1.x,q1.w);
				attitude = - Math.PI/2;
				bank = 0;
				return null;
			}
		    double sqx = q1.x*q1.x;
		    double sqy = q1.y*q1.y;
		    double sqz = q1.z*q1.z;
		    heading = Math.atan2(2*q1.y*q1.w-2*q1.x*q1.z , 1 - 2*sqy - 2*sqz);
			attitude = Math.asin(2*test);
			bank = Math.atan2(2*q1.x*q1.w-2*q1.y*q1.z , 1 - 2*sqx - 2*sqz);
		

		// to finis the implementations of this effect 
		return Matrice; 
	}

    //-----------------
    public double GetX() {
    //-----------------
    
        return x;
    }

    //-----------------
    public double GetY() {
    //-----------------
    
        return y;
    }

    //-----------------
       public double GetZ() {
    //-----------------
    
        return z;
    }

    //-----------------
       public double GetW() {
    //-----------------
     
        return w;
    }

    //-----------------
    public void show() {
    //-----------------
      
        System.out.format(" Quaternion axis %8.3f %8.3f %8.3f   angle  %8.3f (%8.3f) \n",x,y,z,w,w*57.3);         
    }

}
