package com.nr.sp;

public class Daubs extends Wavelet {
    int ncof, ioff, joff;
    double[] cc, cr;
    final static double[] c4 = { 0.4829629131445341, 0.8365163037378079,
	    0.2241438680420134, -0.1294095225512604 };
    final static double[] c12 = { 0.111540743350, 0.494623890398,
	    0.751133908021, 0.315250351709, -0.226264693965, -0.129766867567,
	    0.097501605587, 0.027522865530, -0.031582039318, 0.000553842201,
	    0.004777257511, -0.001077301085 };
    final static double[] c20 = { 0.026670057901, 0.188176800078,
	    0.527201188932, 0.688459039454, 0.281172343661, -0.249846424327,
	    -0.195946274377, 0.127369340336, 0.093057364604, -0.071394147166,
	    -0.029457536822, 0.033212674059, 0.003606553567, -0.010733175483,
	    0.001395351747, 0.001992405295, -0.000685856695, -0.000116466855,
	    0.000093588670, -0.000013264203 };

    public Daubs(final int n) {
	ncof = n;
	cc = new double[n];
	cr = new double[n];
	int i;
	ioff = joff = -(n >> 1);
	// ioff = -2; joff = -n + 2;
	if (n == 4)
	    for (i = 0; i < n; i++)
		cc[i] = c4[i];
	else if (n == 12)
	    for (i = 0; i < n; i++)
		cc[i] = c12[i];
	else if (n == 20)
	    for (i = 0; i < n; i++)
		cc[i] = c20[i];
	else
	    throw new IllegalArgumentException("n not yet implemented in Daubs");
	double sig = -1.0;
	for (i = 0; i < n; i++) {
	    cr[n - 1 - i] = sig * cc[i];
	    sig = -sig;
	}
    }

    @Override
    public void filt(final double[] a, final int n, final int isign) {
	double ai, ai1;
	int i, ii, j, jf, jr, k, n1, ni, nj, nh, nmod;
	if (n < 4)
	    return;
	double[] wksp = new double[n];
	nmod = ncof * n;
	n1 = n - 1;
	nh = n >> 1;
	for (j = 0; j < n; j++)
	    wksp[j] = 0.0;
	if (isign >= 0) {
	    for (ii = 0, i = 0; i < n; i += 2, ii++) {
		ni = i + 1 + nmod + ioff;
		nj = i + 1 + nmod + joff;
		for (k = 0; k < ncof; k++) {
		    jf = n1 & (ni + k + 1);
		    jr = n1 & (nj + k + 1);
		    wksp[ii] += cc[k] * a[jf];
		    wksp[ii + nh] += cr[k] * a[jr];
		}
	    }
	} else {
	    for (ii = 0, i = 0; i < n; i += 2, ii++) {
		ai = a[ii];
		ai1 = a[ii + nh];
		ni = i + 1 + nmod + ioff;
		nj = i + 1 + nmod + joff;
		for (k = 0; k < ncof; k++) {
		    jf = n1 & (ni + k + 1);
		    jr = n1 & (nj + k + 1);
		    wksp[jf] += cc[k] * ai;
		    wksp[jr] += cr[k] * ai1;
		}
	    }
	}
	for (j = 0; j < n; j++)
	    a[j] = wksp[j];
    }
}
