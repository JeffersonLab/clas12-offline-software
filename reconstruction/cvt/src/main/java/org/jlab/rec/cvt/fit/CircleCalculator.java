package org.jlab.rec.cvt.fit;



/**
 * A simple algorithm to calculate the center and radius of a circle that 
 * passes through three points
 */
public class CircleCalculator {

	private CircleCalcPars _circleresult;
	
	// the constructor
	public CircleCalculator() {
	}
	// Find a circle that passes through 3 points
	// Has a boolean method to return the status
	// Status set to false if no circle can be found
	
	public boolean status(double[] P_0, double[] P_1, double[] P_2) {
		// sort points
		double[][] sortedPoints = SortPoints(P_0, P_1, P_2);
		double[] P0 = sortedPoints[0];
		double[] P1 = sortedPoints[1];
		double[] P2 = sortedPoints[2];
		
		if ( Math.abs(P1[0]-P0[0])<1.0e-18 || Math.abs(P2[0]-P1[0])<1.0e-18) return false;
		
        // Find the intersection of the lines joining the innermost to middle and middle to outermost point
        double ma   = (P1[1]-P0[1])/(P1[0]-P0[0]);
        double mb   = (P2[1]-P1[1])/(P2[0]-P1[0]);

        if (Math.abs(mb-ma)<1.0e-18) return false; 
        
        double xcen = 0.5*(ma*mb*(P0[1]-P2[1]) + mb*(P0[0]+P1[0]) -ma*(P1[0]+P2[0]))/(mb-ma);
        double ycen = (-1./mb)*(xcen - 0.5*(P1[0]+P2[0])) + 0.5*(P1[1]+P2[1]);

        double CircRad = Math.sqrt((P0[0]-xcen)*(P0[0]-xcen)+(P0[1]-ycen)*(P0[1]-ycen));
            
        _circleresult = new CircleCalcPars(xcen, ycen, CircRad);
       
		return true;
	}

	public CircleCalcPars getCalc() {
		return _circleresult;
	}
	
	private double[][] SortPoints(double[] p0, double[] p1, double[] p2) {
		
		double[][] newArray = new double[3][3];
		
		double[] midpoint         = null;
		double[] nearestToTarget  = null;
		double[] farthestToTarget = null;
		
		double difsq01 = Math.pow((p0[0]-p1[0]),2)+Math.pow((p0[1]-p1[1]), 2);
		double difsq02 = Math.pow((p0[0]-p2[0]),2)+Math.pow((p0[1]-p2[1]), 2);
		double difsq12 = Math.pow((p1[0]-p2[0]),2)+Math.pow((p1[1]-p2[1]), 2);
		
		if(difsq01>difsq02 && difsq01>difsq12) midpoint = p2;
		if(difsq02>difsq01 && difsq02>difsq12) midpoint = p1;
		if(difsq12>difsq01 && difsq12>difsq02) midpoint = p0;
		
		double D0 = Math.pow(p0[0],2)+Math.pow(p0[1], 2);
		double D1 = Math.pow(p1[0],2)+Math.pow(p1[1], 2);
		double D2 = Math.pow(p2[0],2)+Math.pow(p2[1], 2);
		
		if(D0<D1 && D0<D2) {
			nearestToTarget = p0;
			if (p1 == midpoint) {
				farthestToTarget = p2;
			} else {
				farthestToTarget = p1;
			}
		}
		if(D1<D0 && D1<D2) {
			nearestToTarget = p1;
			if (p0 == midpoint) {
				farthestToTarget = p2;
			} else {
				farthestToTarget = p0;
			}
		}
		if(D2<D1 && D2<D0) {
			nearestToTarget = p2;
			if (p1 == midpoint) {
				farthestToTarget = p0;
			} else {
				farthestToTarget = p1;
			}
		}
		
		// rearrange points
		
		newArray[0]=nearestToTarget;
		newArray[1]=midpoint;
		newArray[2]=farthestToTarget;
		
		return newArray;
	}

}