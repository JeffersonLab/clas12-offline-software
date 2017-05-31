package com.nr.sf;

import static java.lang.Math.*;
import static com.nr.sf.Gamma.*;

public class Betadist extends Beta {
    double alph, bet, fac;

    public Betadist(double aalph, double bbet) {
	alph = aalph;
	bet = bbet;
	if (alph <= 0. || bet <= 0.)
	    throw new IllegalArgumentException("bad alph,bet in Betadist");
	fac = gammln(alph + bet) - gammln(alph) - gammln(bet);
    }

    public double p(double x) {
	if (x <= 0. || x >= 1.)
	    throw new IllegalArgumentException("bad x in Betadist");
	return exp((alph - 1.) * log(x) + (bet - 1.) * log(1. - x) + fac);
    }

    public double cdf(double x) {
	if (x < 0. || x > 1.)
	    throw new IllegalArgumentException("bad x in Betadist");
	return betai(alph, bet, x);
    }

    public double invcdf(double p) {
	if (p < 0. || p > 1.)
	    throw new IllegalArgumentException("bad p in Betadist");
	return invbetai(p, alph, bet);
    }
}
