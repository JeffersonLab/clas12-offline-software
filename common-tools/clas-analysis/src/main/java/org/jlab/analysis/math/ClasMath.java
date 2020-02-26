package org.jlab.analysis.math;

/**
 * 
 * @author naharrison
 *
 * all angles in radians
 */
public class ClasMath {
	
	public static double getRelativePhi(double phi) {
		int sector = ClasMath.getSectorFromPhi(phi);
		if(sector == 1) {
			return ClasMath.getAnglePiConvention(phi);
		}
		else {
			double newPhi = ClasMath.getAngle2PiConvention(phi);
			return newPhi - ((double) sector - 1.0)*(2.0*Math.PI/6.0);
		}
	}
	
	
	public static int getSectorFromPhi(double phi) {
		double newPhi = ClasMath.getAngle2PiConvention(phi);
		int sector = (int) Math.ceil((newPhi + (2.0*Math.PI/12.0))/Math.toRadians(60.0));
		if(sector == 7) sector = 1;
		return sector;
	}


	/** 0 to 2pi convention */
	public static double getAngle2PiConvention(double angle) {
		double newAngle = 0.0;
		double twoPi = 2.0*Math.PI;

		if(angle >= 0.0 && angle < twoPi) newAngle = angle;
		else if(angle >= twoPi) {
			double nFullRotations = Math.floor(angle/twoPi);
			newAngle = angle - twoPi*nFullRotations;
		}
		else if(angle < 0.0) {
			double nFullRotations = Math.floor((-1.0*angle)/twoPi);
			newAngle = (nFullRotations + 1.0)*twoPi + angle;
		}
		
		return newAngle;
	}
	
	
	/** negative pi to pi convention */
	public static double getAnglePiConvention(double angle) {
		double tempAngle = ClasMath.getAngle2PiConvention(angle);
		double newAngle = 0.0;
		
		if(tempAngle <= Math.PI) newAngle = tempAngle;
		else newAngle = tempAngle - 2.0*Math.PI;
		
		return newAngle;
	}


  public static boolean isWithinXPercent(double X, double val, double standard) {
    if(standard >= 0 && val > (1.0 - (X/100.0))*standard && val < (1.0 + (X/100.0))*standard) return true;
    else if(standard < 0 && val < (1.0 - (X/100.0))*standard && val > (1.0 + (X/100.0))*standard) return true;
    return false;
  }

}
