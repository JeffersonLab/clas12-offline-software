/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.circlecenter;

/**
 *
 * @author marmstr
 */
public class CircleCenter { 
    
    public static double[][] circleCircomference(double[] centerrad, int nPoints) {
        //Cacluates evenly spaced points on circomference of circle
        
        double[][] centroid = new double[nPoints][2];
        
        for (int theta = 0; theta<nPoints; theta++) {
            centroid[theta][0] = centerrad[2]*Math.cos(theta)+centerrad[0];
            centroid[theta][1] = centerrad[2]*Math.sin(theta)+centerrad[1];
        }
        
        return centroid;
    }
  
    public static double[] getCentroidND(double[][] points) {
        //Find centroid for set of points with arbitrary dimension 
    
    int nPoints = points.length;
    int nDimensions = points[0].length;
    double[] centroid = new double[nDimensions];
    double[] sums = new double[nDimensions];

    for (int n = 0; n < nPoints; n++) {
	if (points[n].length != nDimensions)
            throw new IllegalArgumentException("Number of dimensions must be equal");
	for (int i = 0; i < nDimensions; i++) {
	    sums[i] += points[n][i];
	}
    }

    for (int i = 0; i < nDimensions; i++) {
	centroid[i] = sums[i] / nPoints;
    }
    
        return centroid;
    }
    
    public static double[] taubinNewton( double[][] points) {
        //Performs taubin geometric circle fit
        
		int nPoints = points.length;
		if (nPoints < 3)
			throw new IllegalArgumentException("Too few points");
		double[] centroid = getCentroidND(points);
		double Mxx = 0, Myy = 0, Mxy = 0, Mxz = 0, Myz = 0, Mzz = 0;
		for (int i = 0; i < nPoints; i++) {
			double Xi = points[i][0] - centroid[0];
			double Yi = points[i][1] - centroid[1];
			double Zi = Xi * Xi + Yi * Yi;
			Mxy += Xi * Yi;
			Mxx += Xi * Xi;
			Myy += Yi * Yi;
			Mxz += Xi * Zi;
			Myz += Yi * Zi;
			Mzz += Zi * Zi;

		}
		Mxx /= nPoints;
		Myy /= nPoints;
		Mxy /= nPoints;
		Mxz /= nPoints;
		Myz /= nPoints;
		Mzz /= nPoints;

		double Mz = Mxx + Myy;
		double Cov_xy = Mxx * Myy - Mxy * Mxy;
		double A3 = 4 * Mz;
		double A2 = -3 * Mz * Mz - Mzz;
		double A1 = Mzz * Mz + 4 * Cov_xy * Mz - Mxz * Mxz - Myz * Myz - Mz * Mz * Mz;
		double A0 = Mxz * Mxz * Myy + Myz * Myz * Mxx - Mzz * Cov_xy - 2 * Mxz * Myz * Mxy + Mz * Mz * Cov_xy;
		double A22 = A2 + A2;
		double A33 = A3 + A3 + A3;

		double xnew = 0;
		double ynew = 1e+20;
		double epsilon = 1e-12;
		double iterMax = 20;

		for (int iter = 0; iter < iterMax; iter++) {
			double yold = ynew;
			ynew = A0 + xnew * (A1 + xnew * (A2 + xnew * A3));
			if (Math.abs(ynew) > Math.abs(yold)) {
				//Newton-Taubin goes wrong direction: |ynew| > |yold|
				xnew = 0;
				break;
			}
			double Dy = A1 + xnew * (A22 + xnew * A33);
			double xold = xnew;
			xnew = xold - ynew / Dy;
			if (Math.abs((xnew - xold) / xnew) < epsilon) {
				break;
			}
			if (iter >= iterMax) {
				//Newton-Taubin doesn't converge
				xnew = 0;
			}
			if (xnew < 0.) {
				//Newton-Taubin negative root:
				xnew = 0;
			}
		}
		double[] centreRadius = new double[3];
		double det = xnew * xnew - xnew * Mz + Cov_xy;
		double x = (Mxz * (Myy - xnew) - Myz * Mxy) / (det * 2);
		double y = (Myz * (Mxx - xnew) - Mxz * Mxy) / (det * 2);
		centreRadius[0] = x + centroid[0];
		centreRadius[1] = y + centroid[1];
		centreRadius[2] = Math.sqrt(x * x + y * y + Mz);

		return centreRadius;
}
    
    public static double circChiSquared(double[][] points, double[] centerrad) {
        //Calculates chisquared for circle fitting to points 
        
        //Choose how much to split up circle circomference
        int nPoints = 3600;
        
        double[][] seed_circle = new double[nPoints][2];
        double chi = 0;
                
        seed_circle= circleCircomference(centerrad,nPoints);        
        
        for (int i = 0; i<points.length; i++) {
            double diff = 0; 
            for (int j = 0; j<nPoints ; j++) {                              
                if ( (diff==0) || (Math.pow(points[i][0]-seed_circle[j][0],2) + Math.pow(points[i][1]-seed_circle[j][1],2))<diff) {
                    diff = Math.pow(points[i][0]-seed_circle[j][0],2) + Math.pow(points[i][1]-seed_circle[j][1],2);
                }                             
            }   
        chi += diff;  
        }       
        return chi;
    }  
    
}
