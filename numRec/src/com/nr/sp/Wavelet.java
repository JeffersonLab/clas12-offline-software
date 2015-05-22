package com.nr.sp;

public abstract class Wavelet {
    public abstract void filt(double[] a, final int n, final int isign);

    public void condition(final double[] a, final int n, final int isign) {
    }

    public static void wt1(final double[] a, final int isign, final Wavelet wlet) {
	int nn, n = a.length;
	if (n < 4)
	    return;
	if (isign >= 0) {
	    wlet.condition(a, n, 1);
	    for (nn = n; nn >= 4; nn >>= 1)
		wlet.filt(a, nn, isign);
	} else {
	    for (nn = 4; nn <= n; nn <<= 1)
		wlet.filt(a, nn, isign);
	    wlet.condition(a, n, -1);
	}
    }

    public static void wtn(final double[] a, final int[] nn, final int isign,
	    final Wavelet wlet) {
	int idim, i1, i2, i3, k, n, nnew, nprev = 1, nt, ntot = 1;
	int ndim = nn.length;
	for (idim = 0; idim < ndim; idim++)
	    ntot *= nn[idim];
	if ((ntot & (ntot - 1)) != 0)
	    throw new IllegalArgumentException(
		    "all lengths must be powers of 2 in wtn");
	for (idim = 0; idim < ndim; idim++) {
	    n = nn[idim];
	    double[] wksp = new double[n];
	    nnew = n * nprev;
	    if (n > 4) {
		for (i2 = 0; i2 < ntot; i2 += nnew) {
		    for (i1 = 0; i1 < nprev; i1++) {
			for (i3 = i1 + i2, k = 0; k < n; k++, i3 += nprev)
			    wksp[k] = a[i3];
			if (isign >= 0) {
			    wlet.condition(wksp, n, 1);
			    for (nt = n; nt >= 4; nt >>= 1)
				wlet.filt(wksp, nt, isign);
			} else {
			    for (nt = 4; nt <= n; nt <<= 1)
				wlet.filt(wksp, nt, isign);
			    wlet.condition(wksp, n, -1);
			}
			for (i3 = i1 + i2, k = 0; k < n; k++, i3 += nprev)
			    a[i3] = wksp[k];
		    }
		}
	    }
	    nprev = nnew;
	}
    }
}
