package com.nr.ran;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.doubleW;

import com.nr.RealValueFun;

public class Miser {
    private Miser() {
    }

    static final int RANSEED = 5331;
    static Ran ran = new Ran(RANSEED);
    static int iran = 0;

    public static void ranpt(final double[] pt, final double[] regn) {
	int j, n = pt.length;
	for (j = 0; j < n; j++)
	    pt[j] = regn[j] + (regn[n + j] - regn[j]) * ran.doub();
    }

    /**
     * Monte Carlo samples a user-supplied ndim-dimensional function func in a
     * rectangular volume specified by regn[0..2*ndim-1], a vector consisting of
     * ndim "lower-left" coordinates of the region followed by ndim
     * "upper-right" coordinates. The function is sampled a total of npts times,
     * at locations determined by the method of recursive stratified sampling.
     * The mean value of the function in the region is returned as ave; an
     * estimate of the statistical uncertainty of ave (square of standard
     * deviation) is returned as var. The input parameter dith should normally
     * be set to zero, but can be set to (e.g.) 0.1 if func's active region
     * falls on the boundary of a power-of-two subdivision of region.
     * 
     * @param func
     * @param regn
     * @param npts
     * @param dith
     * @param ave
     * @param var
     */
    public static void miser(final RealValueFun func, final double[] regn,
	    final int npts, final double dith, final doubleW ave,
	    final doubleW var) {
	final int MNPT = 15, MNBS = 60;
	final double PFAC = 0.1, TINY = 1.0e-30, BIG = 1.0e30;
	int j, jb, n, ndim, npre, nptl, nptr;
	double fracl, fval, rgl, rgm, rgr, s, sigl, siglb, sigr, sigrb;
	double sum, sumb, summ, summ2;
	doubleW avel = new doubleW(0);
	doubleW varl = new doubleW(0);

	ndim = regn.length / 2;
	double[] pt = new double[ndim];
	if (npts < MNBS) {
	    summ = summ2 = 0.0;
	    for (n = 0; n < npts; n++) {
		ranpt(pt, regn);
		fval = func.funk(pt);
		summ += fval;
		summ2 += fval * fval;
	    }
	    ave.val = summ / npts;
	    var.val = max(TINY, (summ2 - summ * summ / npts) / (npts * npts));
	} else {
	    double[] rmid = new double[ndim];
	    npre = max((int) (npts * PFAC), MNPT);
	    double[] fmaxl = new double[ndim];
	    double[] fmaxr = new double[ndim];
	    double[] fminl = new double[ndim];
	    double[] fminr = new double[ndim];
	    for (j = 0; j < ndim; j++) {
		iran = (iran * 2661 + 36979) % 175000;
		s = SIGN(dith, (double) (iran - 87500));
		rmid[j] = (0.5 + s) * regn[j] + (0.5 - s) * regn[ndim + j];
		fminl[j] = fminr[j] = BIG;
		fmaxl[j] = fmaxr[j] = (-BIG);
	    }
	    for (n = 0; n < npre; n++) {
		ranpt(pt, regn);
		fval = func.funk(pt);
		for (j = 0; j < ndim; j++) {
		    if (pt[j] <= rmid[j]) {
			fminl[j] = min(fminl[j], fval);
			fmaxl[j] = max(fmaxl[j], fval);
		    } else {
			fminr[j] = min(fminr[j], fval);
			fmaxr[j] = max(fmaxr[j], fval);
		    }
		}
	    }
	    sumb = BIG;
	    jb = -1;
	    siglb = sigrb = 1.0;
	    for (j = 0; j < ndim; j++) {
		if (fmaxl[j] > fminl[j] && fmaxr[j] > fminr[j]) {
		    sigl = max(TINY, pow(fmaxl[j] - fminl[j], 2.0 / 3.0));
		    sigr = max(TINY, pow(fmaxr[j] - fminr[j], 2.0 / 3.0));
		    sum = sigl + sigr;
		    if (sum <= sumb) {
			sumb = sum;
			jb = j;
			siglb = sigl;
			sigrb = sigr;
		    }
		}
	    }
	    if (jb == -1)
		jb = (ndim * iran) / 175000;
	    rgl = regn[jb];
	    rgm = rmid[jb];
	    rgr = regn[ndim + jb];
	    fracl = abs((rgm - rgl) / (rgr - rgl));
	    nptl = (int) (MNPT + (npts - npre - 2 * MNPT) * fracl * siglb
		    / (fracl * siglb + (1.0 - fracl) * sigrb));
	    nptr = npts - npre - nptl;
	    double[] regn_temp = new double[2 * ndim];
	    for (j = 0; j < ndim; j++) {
		regn_temp[j] = regn[j];
		regn_temp[ndim + j] = regn[ndim + j];
	    }
	    regn_temp[ndim + jb] = rmid[jb];
	    miser(func, regn_temp, nptl, dith, avel, varl);
	    regn_temp[jb] = rmid[jb];
	    regn_temp[ndim + jb] = regn[ndim + jb];
	    miser(func, regn_temp, nptr, dith, ave, var);
	    ave.val = fracl * avel.val + (1 - fracl) * ave.val;
	    var.val = fracl * fracl * varl.val + (1 - fracl) * (1 - fracl)
		    * var.val;
	}
    }
}
