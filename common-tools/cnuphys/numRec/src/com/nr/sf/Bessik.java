package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import org.netlib.util.intW;

/**
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Bessik {
    private static final double i0p[] = { 9.999999999999997e-1,
	    2.466405579426905e-1, 1.478980363444585e-2, 3.826993559940360e-4,
	    5.395676869878828e-6, 4.700912200921704e-8, 2.733894920915608e-10,
	    1.115830108455192e-12, 3.301093025084127e-15,
	    7.209167098020555e-18, 1.166898488777214e-20,
	    1.378948246502109e-23, 1.124884061857506e-26, 5.498556929587117e-30 };
    private static final double i0q[] = { 4.463598170691436e-1,
	    1.702205745042606e-3, 2.792125684538934e-6, 2.369902034785866e-9,
	    8.965900179621208e-13 };
    private static final double i0pp[] = { 1.192273748120670e-1,
	    1.947452015979746e-1, 7.629241821600588e-2, 8.474903580801549e-3,
	    2.023821945835647e-4 };
    private static final double i0qq[] = { 2.962898424533095e-1,
	    4.866115913196384e-1, 1.938352806477617e-1, 2.261671093400046e-2,
	    6.450448095075585e-4, 1.529835782400450e-6 };
    private static final double i1p[] = { 5.000000000000000e-1,
	    6.090824836578078e-2, 2.407288574545340e-3, 4.622311145544158e-5,
	    5.161743818147913e-7, 3.712362374847555e-9, 1.833983433811517e-11,
	    6.493125133990706e-14, 1.693074927497696e-16,
	    3.299609473102338e-19, 4.813071975603122e-22,
	    5.164275442089090e-25, 3.846870021788629e-28, 1.712948291408736e-31 };
    private static final double i1q[] = { 4.665973211630446e-1,
	    1.677754477613006e-3, 2.583049634689725e-6, 2.045930934253556e-9,
	    7.166133240195285e-13 };
    private static final double i1pp[] = { 1.286515211317124e-1,
	    1.930915272916783e-1, 6.965689298161343e-2, 7.345978783504595e-3,
	    1.963602129240502e-4 };
    private static final double i1qq[] = { 3.309385098860755e-1,
	    4.878218424097628e-1, 1.663088501568696e-1, 1.473541892809522e-2,
	    1.964131438571051e-4, -1.034524660214173e-6 };
    private static final double k0pi[] = { 1.0, 2.346487949187396e-1,
	    1.187082088663404e-2, 2.150707366040937e-4, 1.425433617130587e-6 };
    private static final double k0qi[] = { 9.847324170755358e-1,
	    1.518396076767770e-2, 8.362215678646257e-5 };
    private static final double k0p[] = { 1.159315156584126e-1,
	    2.770731240515333e-1, 2.066458134619875e-2, 4.574734709978264e-4,
	    3.454715527986737e-6 };
    private static final double k0q[] = { 9.836249671709183e-1,
	    1.627693622304549e-2, 9.809660603621949e-5 };
    private static final double k0pp[] = { 1.253314137315499,
	    1.475731032429900e1, 6.123767403223466e1, 1.121012633939949e2,
	    9.285288485892228e1, 3.198289277679660e1, 3.595376024148513,
	    6.160228690102976e-2 };
    private static final double k0qq[] = { 1.0, 1.189963006673403e1,
	    5.027773590829784e1, 9.496513373427093e1, 8.318077493230258e1,
	    3.181399777449301e1, 4.443672926432041, 1.408295601966600e-1 };
    private static final double k1pi[] = { 0.5, 5.598072040178741e-2,
	    1.818666382168295e-3, 2.397509908859959e-5, 1.239567816344855e-7 };
    private static final double k1qi[] = { 9.870202601341150e-1,
	    1.292092053534579e-2, 5.881933053917096e-5 };
    private static final double k1p[] = { -3.079657578292062e-1,
	    -8.109417631822442e-2, -3.477550948593604e-3,
	    -5.385594871975406e-5, -3.110372465429008e-7 };
    private static final double k1q[] = { 9.861813171751389e-1,
	    1.375094061153160e-2, 6.774221332947002e-5 };
    private static final double k1pp[] = { 1.253314137315502,
	    1.457171340220454e1, 6.063161173098803e1, 1.147386690867892e2,
	    1.040442011439181e2, 4.356596656837691e1, 7.265230396353690,
	    3.144418558991021e-1 };
    private static final double k1qq[] = { 1.0, 1.125154514806458e1,
	    4.427488496597630e1, 7.616113213117645e1, 5.863377227890893e1,
	    1.850303673841586e1, 1.857244676566022, 2.538540887654872e-2 };

    private double y, z, ax, term;

    public Bessik() {
    }

    public double i0(final double x) {
	if ((ax = abs(x)) < 15.0) {
	    y = x * x;
	    return poly(i0p, 13, y) / poly(i0q, 4, 225. - y);
	} else {
	    z = 1.0 - 15.0 / ax;
	    return exp(ax) * poly(i0pp, 4, z) / (poly(i0qq, 5, z) * sqrt(ax));
	}
    }

    public double i1(final double x) {
	if ((ax = abs(x)) < 15.0) {
	    y = x * x;
	    return x * poly(i1p, 13, y) / poly(i1q, 4, 225. - y);
	} else {
	    z = 1.0 - 15.0 / ax;
	    double ans = exp(ax) * poly(i1pp, 4, z)
		    / (poly(i1qq, 5, z) * sqrt(ax));
	    return x > 0.0 ? ans : -ans;
	}
    }

    public double k0(final double x) {
	if (x <= 1.0) {
	    z = x * x;
	    term = poly(k0pi, 4, z) * log(x) / poly(k0qi, 2, 1. - z);
	    return poly(k0p, 4, z) / poly(k0q, 2, 1. - z) - term;
	} else {
	    z = 1.0 / x;
	    return exp(-x) * poly(k0pp, 7, z) / (poly(k0qq, 7, z) * sqrt(x));
	}
    }

    public double k1(final double x) {
	if (x <= 1.0) {
	    z = x * x;
	    term = poly(k1pi, 4, z) * log(x) / poly(k1qi, 2, 1. - z);
	    return x * (poly(k1p, 4, z) / poly(k1q, 2, 1. - z) + term) + 1. / x;
	} else {
	    z = 1.0 / x;
	    return exp(-x) * poly(k1pp, 7, z) / (poly(k1qq, 7, z) * sqrt(x));
	}
    }

    public double poly(final double[] cof, final int n, final double x) {
	double ans = cof[n];
	for (int i = n - 1; i >= 0; i--)
	    ans = ans * x + cof[i];
	return ans;
    }

    public double kn(final int n, final double x) {
	int j;
	double bk, bkm, bkp, tox;
	if (n == 0)
	    return k0(x);
	if (n == 1)
	    return k1(x);
	tox = 2.0 / x;
	bkm = k0(x);
	bk = k1(x);
	for (j = 1; j < n; j++) {
	    bkp = bkm + j * tox * bk;
	    bkm = bk;
	    bk = bkp;
	}
	return bk;
    }

    public double in(final int n, final double x) {
	final double ACC = 200.0;
	final int IEXP = Double.MAX_EXPONENT / 2; // numeric_limits<double>::max_exponent/2;
	int j;
	intW k = new intW(0);
	double bi, bim, bip, dum, tox, ans;
	if (n == 0)
	    return i0(x);
	if (n == 1)
	    return i1(x);
	if (x * x <= 8.0 * Double.MIN_NORMAL)
	    return 0.0;
	else {
	    tox = 2.0 / abs(x);
	    bip = ans = 0.0;
	    bi = 1.0;
	    for (j = 2 * (n + (int) (sqrt(ACC * n))); j > 0; j--) {
		bim = bip + j * tox * bi;
		bip = bi;
		bi = bim;
		dum = frexp(bi, k);
		dum = dum + 0;
		if (k.val > IEXP) {
		    ans = ldexp(ans, -IEXP);
		    bi = ldexp(bi, -IEXP);
		    bip = ldexp(bip, -IEXP);
		}
		if (j == n)
		    ans = bip;
	    }
	    ans *= i0(x) / bi;
	    return (x < 0.0 && (n & 1) != 0) ? -ans : ans;
	}
    }
}
