package com.nr.ran;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Normaldev extends Ran {
    double mu, sig;

    public Normaldev(final double mmu, final double ssig, final long i) {
	super(i);
	mu = mmu;
	sig = ssig;
    }

    public double dev() {
	double u, v, x, y, q;
	do {
	    u = doub();
	    v = 1.7156 * (doub() - 0.5);
	    x = u - 0.449871;
	    y = abs(v) + 0.386595;
	    q = SQR(x) + y * (0.19600 * y - 0.25472 * x);
	} while (q > 0.27597 && (q > 0.27846 || SQR(v) > -4. * log(u) * SQR(u)));
	return mu + sig * v / u;
    }
}
