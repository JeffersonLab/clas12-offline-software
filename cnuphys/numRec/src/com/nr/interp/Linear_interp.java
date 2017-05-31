package com.nr.interp;

/**
 * Piecewise linear interpolation object.
 * 
 * Construct with x and y vectors, then call interp for interpolated values.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Linear_interp extends Base_interp {

    public Linear_interp(final double[] xv, final double[] yv) {
	super(xv, yv, 2);
    }

    @Override
    public double rawinterp(final int j, final double x) {
	if (xx[j] == xx[j + 1])
	    return yy[j];
	else
	    return yy[j] + ((x - xx[j]) / (xx[j + 1] - xx[j]))
		    * (yy[j + 1] - yy[j]);
    }

}
