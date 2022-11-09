package org.jlab.rec.cvt.fit;

/**
 * Returns the calculated center and radius of a circle from three points.
 *
 */
public class CircleCalcPars {

    private double _xc;
    private double _yc;
    private double _r;

    // The constructor
    public CircleCalcPars(double xc, double yc, double r) {
        _xc = xc; // circle center x coordinate
        _yc = yc; // circle center y coordinate
        _r = r;  // circle radius

    }
    // The methods

    /**
     * Returns the x coordinate of the reference point
     * @return 
     */
    public double xcen() {
        return _xc;
    }

    /**
     * Returns the y coordinate of the reference point
     * @return 
     */
    public double ycen() {
        return _yc;
    }

    /**
     * Returns the radius of curvature
     */
    public double radius() {
        return _r;
    }

}
