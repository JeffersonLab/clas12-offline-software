package com.nr.eig;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.Complex;

/**
 * Computes all eigenvalues and eigenvectors of a real nonsymmetric matrix by
 * reduction to Hes- senberg form followed by QR iteration. - PTC
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Unsymmeig {
    public int n;
    public double[][] a, zz;
    public Complex[] wri;
    public double[] scale;
    public int[] perm;
    public boolean yesvecs, hessen;

    public Unsymmeig(final double[][] aa) {
	this(aa, true, false);
    }

    /**
     * Computes all eigenvalues and (optionally) eigenvectors of a real
     * nonsymmetric matrix a[0..n-1][0..n-1] by reduction to Hessenberg form
     * followed by QR iteration. If yesvecs is input as true (the default), then
     * the eigenvectors are computed. Otherwise, only the eigenvalues are
     * computed. If hessen is input as false (the default), the matrix is first
     * reduced to Hessenberg form. Otherwise it is assumed that the matrix is
     * already in Hessen- berg from. On output, wri[0..n-1] contains the
     * eigenvalues of a sorted into descending order, while zz[0..n-1][0..n-1]
     * is a matrix whose columns contain the corresponding eigenvectors. For a
     * complex eigenvalue, only the eigenvector corresponding to the eigen-
     * value with a positive imaginary part is stored, with the real part in
     * zz[0..n-1][i] and the imaginary part in h.zz[0..n-1][i+1]. The
     * eigenvectors are not normalized.
     * 
     * @param aa
     * @param yesvec
     * @param hessenb
     */
    public Unsymmeig(final double[][] aa, final boolean yesvec,
	    final boolean hessenb) {
	n = aa.length;
	a = buildMatrix(aa);
	zz = new double[n][n];
	wri = new Complex[n];
	for (int i = 0; i < n; i++)
	    wri[i] = new Complex(0, 0);
	scale = buildVector(n, 1.0);
	perm = new int[n];
	yesvecs = yesvec;
	hessen = hessenb;

	balance();
	if (!hessen)
	    elmhes();
	if (yesvecs) {
	    for (int i = 0; i < n; i++)
		zz[i][i] = 1.0;
	    if (!hessen)
		eltran();
	    hqr2();
	    balbak();
	    sortvecs();
	} else {
	    hqr();
	    sort();
	}
    }

    /**
     * Given a matrix a[0..n-1][0..n-1], this routine replaces it by a balanced
     * matrix with identical eigenvalues. A symmetric matrix is already balanced
     * and is unaffected by this procedure.
     */
    private void balance() {
	final double RADIX = FLT_RADIX;
	boolean done = false;
	double sqrdx = RADIX * RADIX;
	while (!done) {
	    done = true;
	    for (int i = 0; i < n; i++) {
		double r = 0.0, c = 0.0;
		for (int j = 0; j < n; j++)
		    if (j != i) {
			c += abs(a[j][i]);
			r += abs(a[i][j]);
		    }
		if (c != 0.0 && r != 0.0) {
		    double g = r / RADIX;
		    double f = 1.0;
		    double s = c + r;
		    while (c < g) {
			f *= RADIX;
			c *= sqrdx;
		    }
		    g = r * RADIX;
		    while (c > g) {
			f /= RADIX;
			c /= sqrdx;
		    }
		    if ((c + r) / f < 0.95 * s) {
			done = false;
			g = 1.0 / f;
			scale[i] *= f;
			for (int j = 0; j < n; j++)
			    a[i][j] *= g;
			for (int j = 0; j < n; j++)
			    a[j][i] *= f;
		    }
		}
	    }
	}
    }

    /**
     * Forms the eigenvectors of a real nonsymmetric matrix by back transforming
     * those of the corre- sponding balanced matrix determined by balance.
     */
    private void balbak() {
	for (int i = 0; i < n; i++)
	    for (int j = 0; j < n; j++)
		zz[i][j] *= scale[i];
    }

    /**
     * Reduction to Hessenberg form by the elimination method. Replaces the real
     * nonsymmetric matrix a[0..n-1][0..n-1] by an upper Hessenberg matrix with
     * identical eigenvalues. Rec- ommended, but not required, is that this
     * routine be preceded by balance. On output, the Hessenberg matrix is in
     * elements a[i][j] with i <= j+1. Elements with i > j+1 are to be thought
     * of as zero, but are returned with random values.
     */
    private void elmhes() {
	for (int m = 1; m < n - 1; m++) {
	    double x = 0.0;
	    int i = m;
	    for (int j = m; j < n; j++) {
		if (abs(a[j][m - 1]) > abs(x)) {
		    x = a[j][m - 1];
		    i = j;
		}
	    }
	    perm[m] = i;
	    if (i != m) {
		for (int j = m - 1; j < n; j++) {
		    // SWAP(a[i][j],a[m][j]);
		    double swap = a[i][j];
		    a[i][j] = a[m][j];
		    a[m][j] = swap;
		}
		for (int j = 0; j < n; j++) {
		    // SWAP(a[j][i],a[j][m]);
		    double swap = a[j][i];
		    a[j][i] = a[j][m];
		    a[j][m] = swap;
		}
	    }
	    if (x != 0.0) {
		for (i = m + 1; i < n; i++) {
		    double y = a[i][m - 1];
		    if (y != 0.0) {
			y /= x;
			a[i][m - 1] = y;
			for (int j = m; j < n; j++)
			    a[i][j] -= y * a[m][j];
			for (int j = 0; j < n; j++)
			    a[j][m] += y * a[j][i];
		    }
		}
	    }
	}
    }

    /**
     * This routine accumulates the stabilized elementary similarity
     * transformations used in the reduction to upper Hessenberg form by elmhes.
     * The multipliers that were used in the reduction are obtained from the
     * lower triangle (below the subdiagonal) of a. The transformations are
     * permuted according to the permutations stored in perm by elmhes.
     */
    private void eltran() {
	for (int mp = n - 2; mp > 0; mp--) {
	    for (int k = mp + 1; k < n; k++)
		zz[k][mp] = a[k][mp - 1];
	    int i = perm[mp];
	    if (i != mp) {
		for (int j = mp; j < n; j++) {
		    zz[mp][j] = zz[i][j];
		    zz[i][j] = 0.0;
		}
		zz[i][mp] = 1.0;
	    }
	}
    }

    private void hqr() {
	int nn, m, l, k, j, its, i, mmin;
	double z, y, x, w, v, u, t, s, r = 0, q = 0, p = 0, anorm = 0.0;

	final double EPS = DBL_EPSILON;
	for (i = 0; i < n; i++)
	    for (j = max(i - 1, 0); j < n; j++)
		anorm += abs(a[i][j]);
	nn = n - 1;
	t = 0.0;
	while (nn >= 0) {
	    its = 0;
	    do {
		for (l = nn; l > 0; l--) {
		    s = abs(a[l - 1][l - 1]) + abs(a[l][l]);
		    if (s == 0.0)
			s = anorm;
		    if (abs(a[l][l - 1]) <= EPS * s) {
			a[l][l - 1] = 0.0;
			break;
		    }
		}
		x = a[nn][nn];
		if (l == nn) {
		    wri[nn--] = new Complex(x + t, 0);
		} else {
		    y = a[nn - 1][nn - 1];
		    w = a[nn][nn - 1] * a[nn - 1][nn];
		    if (l == nn - 1) {
			p = 0.5 * (y - x);
			q = p * p + w;
			z = sqrt(abs(q));
			x += t;
			if (q >= 0.0) {
			    z = p + SIGN(z, p);
			    wri[nn - 1] = new Complex(x + z, 0);
			    wri[nn] = new Complex(x + z, 0);
			    if (z != 0.0)
				wri[nn] = new Complex(x - w / z, 0);
			} else {
			    wri[nn] = new Complex(x + p, -z);
			    wri[nn - 1] = wri[nn].conj();
			}
			nn -= 2;
		    } else {
			if (its == 30)
			    throw new IllegalArgumentException(
				    "Too many iterations in hqr");
			if (its == 10 || its == 20) {
			    t += x;
			    for (i = 0; i < nn + 1; i++)
				a[i][i] -= x;
			    s = abs(a[nn][nn - 1]) + abs(a[nn - 1][nn - 2]);
			    y = x = 0.75 * s;
			    w = -0.4375 * s * s;
			}
			++its;
			for (m = nn - 2; m >= l; m--) {
			    z = a[m][m];
			    r = x - z;
			    s = y - z;
			    p = (r * s - w) / a[m + 1][m] + a[m][m + 1];
			    q = a[m + 1][m + 1] - z - r - s;
			    r = a[m + 2][m + 1];
			    s = abs(p) + abs(q) + abs(r);
			    p /= s;
			    q /= s;
			    r /= s;
			    if (m == l)
				break;
			    u = abs(a[m][m - 1]) * (abs(q) + abs(r));
			    v = abs(p)
				    * (abs(a[m - 1][m - 1]) + abs(z) + abs(a[m + 1][m + 1]));
			    if (u <= EPS * v)
				break;
			}
			for (i = m; i < nn - 1; i++) {
			    a[i + 2][i] = 0.0;
			    if (i != m)
				a[i + 2][i - 1] = 0.0;
			}
			for (k = m; k < nn; k++) {
			    if (k != m) {
				p = a[k][k - 1];
				q = a[k + 1][k - 1];
				r = 0.0;
				if (k + 1 != nn)
				    r = a[k + 2][k - 1];
				if ((x = abs(p) + abs(q) + abs(r)) != 0.0) {
				    p /= x;
				    q /= x;
				    r /= x;
				}
			    }
			    if ((s = SIGN(sqrt(p * p + q * q + r * r), p)) != 0.0) {
				if (k == m) {
				    if (l != m)
					a[k][k - 1] = -a[k][k - 1];
				} else
				    a[k][k - 1] = -s * x;
				p += s;
				x = p / s;
				y = q / s;
				z = r / s;
				q /= p;
				r /= p;
				for (j = k; j < nn + 1; j++) {
				    p = a[k][j] + q * a[k + 1][j];
				    if (k + 1 != nn) {
					p += r * a[k + 2][j];
					a[k + 2][j] -= p * z;
				    }
				    a[k + 1][j] -= p * y;
				    a[k][j] -= p * x;
				}
				mmin = nn < k + 3 ? nn : k + 3;
				for (i = l; i < mmin + 1; i++) {
				    p = x * a[i][k] + y * a[i][k + 1];
				    if (k + 1 != nn) {
					p += z * a[i][k + 2];
					a[i][k + 2] -= p * r;
				    }
				    a[i][k + 1] -= p * q;
				    a[i][k] -= p;
				}
			    }
			}
		    }
		}
	    } while (l + 1 < nn);
	}
    }

    private void hqr2() {
	int nn, m, l, k, j, its, i, mmin, na;
	double z = 0, y, x, w, v, u, t, s = 0, r = 0, q = 0, p = 0, anorm = 0.0, ra, sa, vr, vi;

	final double EPS = DBL_EPSILON;
	for (i = 0; i < n; i++)
	    for (j = max(i - 1, 0); j < n; j++)
		anorm += abs(a[i][j]);
	nn = n - 1;
	t = 0.0;
	while (nn >= 0) {
	    its = 0;
	    do {
		for (l = nn; l > 0; l--) {
		    s = abs(a[l - 1][l - 1]) + abs(a[l][l]);
		    if (s == 0.0)
			s = anorm;
		    if (abs(a[l][l - 1]) <= EPS * s) {
			a[l][l - 1] = 0.0;
			break;
		    }
		}
		x = a[nn][nn];
		if (l == nn) {
		    wri[nn] = new Complex(x + t, 0);
		    a[nn][nn] = x + t;
		    nn--;
		} else {
		    y = a[nn - 1][nn - 1];
		    w = a[nn][nn - 1] * a[nn - 1][nn];
		    if (l == nn - 1) {
			p = 0.5 * (y - x);
			q = p * p + w;
			z = sqrt(abs(q));
			x += t;
			a[nn][nn] = x;
			a[nn - 1][nn - 1] = y + t;
			if (q >= 0.0) {
			    z = p + SIGN(z, p);
			    wri[nn - 1] = new Complex(x + z, 0);
			    wri[nn] = new Complex(x + z, 0);
			    if (z != 0.0)
				wri[nn] = new Complex(x - w / z, 0);
			    x = a[nn][nn - 1];
			    s = abs(x) + abs(z);
			    p = x / s;
			    q = z / s;
			    r = sqrt(p * p + q * q);
			    p /= r;
			    q /= r;
			    for (j = nn - 1; j < n; j++) {
				z = a[nn - 1][j];
				a[nn - 1][j] = q * z + p * a[nn][j];
				a[nn][j] = q * a[nn][j] - p * z;
			    }
			    for (i = 0; i <= nn; i++) {
				z = a[i][nn - 1];
				a[i][nn - 1] = q * z + p * a[i][nn];
				a[i][nn] = q * a[i][nn] - p * z;
			    }
			    for (i = 0; i < n; i++) {
				z = zz[i][nn - 1];
				zz[i][nn - 1] = q * z + p * zz[i][nn];
				zz[i][nn] = q * zz[i][nn] - p * z;
			    }
			} else {
			    wri[nn] = new Complex(x + p, -z);
			    wri[nn - 1] = wri[nn].conj();
			}
			nn -= 2;
		    } else {
			if (its == 30)
			    throw new IllegalArgumentException(
				    "Too many iterations in hqr");
			if (its == 10 || its == 20) {
			    t += x;
			    for (i = 0; i < nn + 1; i++)
				a[i][i] -= x;
			    s = abs(a[nn][nn - 1]) + abs(a[nn - 1][nn - 2]);
			    y = x = 0.75 * s;
			    w = -0.4375 * s * s;
			}
			++its;
			for (m = nn - 2; m >= l; m--) {
			    z = a[m][m];
			    r = x - z;
			    s = y - z;
			    p = (r * s - w) / a[m + 1][m] + a[m][m + 1];
			    q = a[m + 1][m + 1] - z - r - s;
			    r = a[m + 2][m + 1];
			    s = abs(p) + abs(q) + abs(r);
			    p /= s;
			    q /= s;
			    r /= s;
			    if (m == l)
				break;
			    u = abs(a[m][m - 1]) * (abs(q) + abs(r));
			    v = abs(p)
				    * (abs(a[m - 1][m - 1]) + abs(z) + abs(a[m + 1][m + 1]));
			    if (u <= EPS * v)
				break;
			}
			for (i = m; i < nn - 1; i++) {
			    a[i + 2][i] = 0.0;
			    if (i != m)
				a[i + 2][i - 1] = 0.0;
			}
			for (k = m; k < nn; k++) {
			    if (k != m) {
				p = a[k][k - 1];
				q = a[k + 1][k - 1];
				r = 0.0;
				if (k + 1 != nn)
				    r = a[k + 2][k - 1];
				if ((x = abs(p) + abs(q) + abs(r)) != 0.0) {
				    p /= x;
				    q /= x;
				    r /= x;
				}
			    }
			    if ((s = SIGN(sqrt(p * p + q * q + r * r), p)) != 0.0) {
				if (k == m) {
				    if (l != m)
					a[k][k - 1] = -a[k][k - 1];
				} else
				    a[k][k - 1] = -s * x;
				p += s;
				x = p / s;
				y = q / s;
				z = r / s;
				q /= p;
				r /= p;
				for (j = k; j < n; j++) {
				    p = a[k][j] + q * a[k + 1][j];
				    if (k + 1 != nn) {
					p += r * a[k + 2][j];
					a[k + 2][j] -= p * z;
				    }
				    a[k + 1][j] -= p * y;
				    a[k][j] -= p * x;
				}
				mmin = nn < k + 3 ? nn : k + 3;
				for (i = 0; i < mmin + 1; i++) {
				    p = x * a[i][k] + y * a[i][k + 1];
				    if (k + 1 != nn) {
					p += z * a[i][k + 2];
					a[i][k + 2] -= p * r;
				    }
				    a[i][k + 1] -= p * q;
				    a[i][k] -= p;
				}
				for (i = 0; i < n; i++) {
				    p = x * zz[i][k] + y * zz[i][k + 1];
				    if (k + 1 != nn) {
					p += z * zz[i][k + 2];
					zz[i][k + 2] -= p * r;
				    }
				    zz[i][k + 1] -= p * q;
				    zz[i][k] -= p;
				}
			    }
			}
		    }
		}
	    } while (l + 1 < nn);
	}
	if (anorm != 0.0) {
	    for (nn = n - 1; nn >= 0; nn--) {
		p = wri[nn].re();
		q = wri[nn].im();
		na = nn - 1;
		if (q == 0.0) {
		    m = nn;
		    a[nn][nn] = 1.0;
		    for (i = nn - 1; i >= 0; i--) {
			w = a[i][i] - p;
			r = 0.0;
			for (j = m; j <= nn; j++)
			    r += a[i][j] * a[j][nn];
			if (wri[i].im() < 0.0) {
			    z = w;
			    s = r;
			} else {
			    m = i;

			    if (wri[i].im() == 0.0) {
				t = w;
				if (t == 0.0)
				    t = EPS * anorm;
				a[i][nn] = -r / t;
			    } else {
				x = a[i][i + 1];
				y = a[i + 1][i];
				q = SQR(wri[i].re() - p) + SQR(wri[i].im());
				t = (x * s - z * r) / q;
				a[i][nn] = t;
				if (abs(x) > abs(z))
				    a[i + 1][nn] = (-r - w * t) / x;
				else
				    a[i + 1][nn] = (-s - y * t) / z;
			    }
			    t = abs(a[i][nn]);
			    if (EPS * t * t > 1)
				for (j = i; j <= nn; j++)
				    a[j][nn] /= t;
			}
		    }
		} else if (q < 0.0) {
		    m = na;
		    if (abs(a[nn][na]) > abs(a[na][nn])) {
			a[na][na] = q / a[nn][na];
			a[na][nn] = -(a[nn][nn] - p) / a[nn][na];
		    } else {
			Complex temp = new Complex(0.0, -a[na][nn])
				.div(new Complex(a[na][na] - p, q));
			a[na][na] = temp.re();
			a[na][nn] = temp.im();
		    }
		    a[nn][na] = 0.0;
		    a[nn][nn] = 1.0;
		    for (i = nn - 2; i >= 0; i--) {
			w = a[i][i] - p;
			ra = sa = 0.0;
			for (j = m; j <= nn; j++) {
			    ra += a[i][j] * a[j][na];
			    sa += a[i][j] * a[j][nn];
			}
			if (wri[i].im() < 0.0) {
			    z = w;
			    r = ra;
			    s = sa;
			} else {
			    m = i;
			    if (wri[i].im() == 0.0) {
				Complex temp = new Complex(-ra, -sa)
					.div(new Complex(w, q));
				a[i][na] = temp.re();
				a[i][nn] = temp.im();
			    } else {
				x = a[i][i + 1];
				y = a[i + 1][i];
				vr = SQR(wri[i].re() - p) + SQR(wri[i].im())
					- q * q;
				vi = 2.0 * q * (wri[i].re() - p);
				if (vr == 0.0 && vi == 0.0)
				    vr = EPS
					    * anorm
					    * (abs(w) + abs(q) + abs(x)
						    + abs(y) + abs(z));
				Complex temp = new Complex(x * r - z * ra + q
					* sa, x * s - z * sa - q * ra)
					.div(new Complex(vr, vi));
				a[i][na] = temp.re();
				a[i][nn] = temp.im();
				if (abs(x) > abs(z) + abs(q)) {
				    a[i + 1][na] = (-ra - w * a[i][na] + q
					    * a[i][nn])
					    / x;
				    a[i + 1][nn] = (-sa - w * a[i][nn] - q
					    * a[i][na])
					    / x;
				} else {
				    temp = new Complex(-r - y * a[i][na], -s
					    - y * a[i][nn]).div(new Complex(z,
					    q));
				    a[i + 1][na] = temp.re();
				    a[i + 1][nn] = temp.im();
				}
			    }
			}
			t = max(abs(a[i][na]), abs(a[i][nn]));
			if (EPS * t * t > 1)
			    for (j = i; j <= nn; j++) {
				a[j][na] /= t;
				a[j][nn] /= t;
			    }
		    }
		}
	    }
	    for (j = n - 1; j >= 0; j--)
		for (i = 0; i < n; i++) {
		    z = 0.0;
		    for (k = 0; k <= j; k++)
			z += zz[i][k] * a[k][j];
		    zz[i][j] = z;
		}
	}
    }

    private void sort() {
	int i;
	for (int j = 1; j < n; j++) {
	    Complex x = wri[j];
	    for (i = j - 1; i >= 0; i--) {
		if (wri[i].re() >= x.re())
		    break;
		wri[i + 1] = wri[i];
	    }
	    wri[i + 1] = x;
	}
    }

    private void sortvecs() {
	int i;
	double[] temp = new double[n];
	for (int j = 1; j < n; j++) {
	    Complex x = wri[j];
	    for (int k = 0; k < n; k++)
		temp[k] = zz[k][j];
	    for (i = j - 1; i >= 0; i--) {
		if (wri[i].re() >= x.re())
		    break;
		wri[i + 1] = wri[i];
		for (int k = 0; k < n; k++)
		    zz[k][i + 1] = zz[k][i];
	    }
	    wri[i + 1] = x;
	    for (int k = 0; k < n; k++)
		zz[k][i + 1] = temp[k];
	}
    }
}
