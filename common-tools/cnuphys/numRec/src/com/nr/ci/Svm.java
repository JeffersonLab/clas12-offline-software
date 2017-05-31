package com.nr.ci;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;
import com.nr.ran.Ran;
import com.nr.sort.Indexx;

/**
 * Support Vector Machines Copyright (C) Numerical Recipes Software 1986-2007
 * Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Svm {
    private Svmgenkernel gker;
    private int m, fnz, fub, niter;
    private double[] alph, alphold;
    private Ran ran;
    private boolean alphinit;
    private double dalph;

    public Svm(final Svmgenkernel inker) {
	gker = inker;
	m = gker.y.length;
	alph = new double[m];
	alphold = new double[m];
	ran = new Ran(21);
	alphinit = false;
    }

    public double relax(final double lambda, final double om) {
	int iter, j, jj, k, kk;
	double sum;
	double[] pinsum = new double[m];
	if (alphinit == false) {
	    for (j = 0; j < m; j++)
		alph[j] = 0.;
	    alphinit = true;
	}
	alphold = alph;
	Indexx x = new Indexx(alph);
	for (fnz = 0; fnz < m; fnz++)
	    if (alph[x.indx[fnz]] != 0.)
		break;
	for (j = fnz; j < m - 2; j++) {
	    k = j + (ran.int32p() % (m - j));
	    swap(x.indx, j, k);
	}
	for (jj = 0; jj < m; jj++) {
	    j = x.indx[jj];
	    sum = 0.;
	    for (kk = fnz; kk < m; kk++) {
		k = x.indx[kk];
		sum += (gker.ker[j][k] + 1.) * gker.y[k] * alph[k];
	    }
	    alph[j] = alph[j] - (om / (gker.ker[j][j] + 1.))
		    * (gker.y[j] * sum - 1.);
	    alph[j] = max(0., min(lambda, alph[j]));
	    if (jj < fnz && alph[j] != 0) {
		--fnz;
		// SWAP(x.indx[--fnz],x.indx[jj]);
		swap(x.indx, fnz, jj);
	    }
	}
	Indexx y = new Indexx(alph);
	for (fnz = 0; fnz < m; fnz++)
	    if (alph[y.indx[fnz]] != 0.)
		break;
	for (fub = fnz; fub < m; fub++)
	    if (alph[y.indx[fub]] == lambda)
		break;
	for (j = fnz; j < fub - 2; j++) {
	    k = j + (ran.int32p() % (fub - j));
	    swap(y.indx, j, k);
	}
	for (jj = fnz; jj < fub; jj++) {
	    j = y.indx[jj];
	    sum = 0.;
	    for (kk = fub; kk < m; kk++) {
		k = y.indx[kk];
		sum += (gker.ker[j][k] + 1.) * gker.y[k] * alph[k];
	    }
	    pinsum[jj] = sum;
	}
	niter = max((int) (0.5 * (m + 1.0) * (m - fnz + 1.0) / (SQR(fub - fnz
		+ 1.0))), 1);
	for (iter = 0; iter < niter; iter++) {
	    for (jj = fnz; jj < fub; jj++) {
		j = y.indx[jj];
		sum = pinsum[jj];
		for (kk = fnz; kk < fub; kk++) {
		    k = y.indx[kk];
		    sum += (gker.ker[j][k] + 1.) * gker.y[k] * alph[k];
		}
		alph[j] = alph[j] - (om / (gker.ker[j][j] + 1.))
			* (gker.y[j] * sum - 1.);
		alph[j] = max(0., min(lambda, alph[j]));
	    }
	}
	dalph = 0.;
	for (j = 0; j < m; j++)
	    dalph += SQR(alph[j] - alphold[j]);
	return sqrt(dalph);
    }

    public double predict(final int k) {
	double sum = 0.;
	for (int j = 0; j < m; j++)
	    sum += alph[j] * gker.y[j] * (gker.ker[j][k] + 1.0);
	return sum;
    }

    public double predict(final double[] x) {
	double sum = 0.;
	for (int j = 0; j < m; j++)
	    sum += alph[j] * gker.y[j] * (gker.kernel(j, x) + 1.0);
	return sum;
    }
}
