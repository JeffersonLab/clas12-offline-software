package com.nr.ran;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Gammadev extends Normaldev {
    double alph, oalph, bet;
    double a1, a2;

    public Gammadev(final double aalph, final double bbet, final long i) {
	super(0., 1., i);
	alph = aalph;
	oalph = aalph;
	bet = bbet;

	if (alph <= 0.)
	    throw new IllegalArgumentException("bad alph in Gammadev");
	if (alph < 1.)
	    alph += 1.;
	a1 = alph - 1. / 3.;
	a2 = 1. / sqrt(9. * a1);
    }

    @Override
    public double dev() {
	double u, v, x;
	do {
	    do {
		x = super.dev();
		v = 1. + a2 * x;
	    } while (v <= 0.);
	    v = v * v * v;
	    u = doub();
	} while (u > 1. - 0.331 * SQR(SQR(x))
		&& log(u) > 0.5 * SQR(x) + a1 * (1. - v + log(v)));
	if (alph == oalph)
	    return a1 * v / bet;
	else {
	    do
		u = doub();
	    while (u == 0.);
	    return pow(u, 1. / oalph) * a1 * v / bet;
	}
    }
}
