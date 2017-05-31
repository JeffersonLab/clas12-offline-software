package com.nr.sp;

public class Slepwindow implements WindowFun {
    private int k;
    private double[][] dps;

    public Slepwindow(final int kkt, final double[][] dpss) {
	k = kkt;
	dps = dpss;
    }

    @Override
    public double window(final int j, final int n) {
	return dps[k][j];
    }

}
