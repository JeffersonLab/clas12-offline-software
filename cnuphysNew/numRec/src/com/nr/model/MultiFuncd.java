package com.nr.model;

import org.netlib.util.doubleW;

public interface MultiFuncd {
    public void funk(final double x, final double[] a, final doubleW y,
	    final double[] dyda);
}
