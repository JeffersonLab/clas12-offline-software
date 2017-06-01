package com.nr.model;

public class State {
    public double lam1, lam2;
    public double tc;
    public int k1, k2;
    public double plog;

    public State(final double la1, final double la2, final double t,
	    final int kk1, final int kk2) {
	lam1 = la1;
	lam2 = la2;
	tc = t;
	k1 = kk1;
	k2 = kk2;
    }

    public State() {

    }

    public static void copy(final State from, final State to) {
	to.lam1 = from.lam1;
	to.lam2 = from.lam2;
	to.tc = from.tc;
	to.k1 = from.k1;
	to.k2 = from.k2;
	to.plog = from.plog;
    }
}
