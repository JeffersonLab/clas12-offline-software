package com.nr.sf;

import static java.lang.Math.*;

public class Gammadist extends Gamma {
    double alph, bet, fac;

    public Gammadist(double aalph) {
	this(aalph, 1.);
    }

    public Gammadist(double aalph, double bbet) {
	alph = aalph;
	bet = bbet;

	if (alph <= 0. || bet <= 0.)
	    throw new IllegalArgumentException("bad alph,bet in Gammadist");
	fac = alph * log(bet) - gammln(alph);
    }

    public double p(double x) {
	if (x <= 0.)
	    throw new IllegalArgumentException("bad x in Gammadist");
	return exp(-bet * x + (alph - 1.) * log(x) + fac);
    }

    public double cdf(double x) {
	if (x < 0.)
	    throw new IllegalArgumentException("bad x in Gammadist");
	return gammp(alph, bet * x);
    }

    public double invcdf(double p) {
	if (p < 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Gammadist");
	return invgammp(p, alph) / bet;
    }
}
