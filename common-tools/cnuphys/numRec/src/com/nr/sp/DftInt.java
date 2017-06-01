package com.nr.sp;

import org.netlib.util.doubleW;

import com.nr.fft.*;
import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;
import com.nr.interp.Poly_interp;

/**
 * Example program illustrating how to use the routine dftcor. The user supplies
 * an external function func that returns the quantity h(t). The routine then
 * returns S(a,b, cos(wt)*h(t)*dt as cosint and S(a,b, sin(wt)*h(t)*dt as
 * sinint.
 * 
 */
public class DftInt {
    final int M = 64, NDFT = 1024, MPOL = 6;
    double aold = -1.e30, bold = -1.e30, delta;
    int init = 0;
    double[] data = new double[NDFT];
    double[] endpts = new double[8];
    UniVarRealValueFun funcold;

    public void dftint(final UniVarRealValueFun func, final double a,
	    final double b, final double w, final doubleW cosint,
	    final doubleW sinint) {
	final double TWOPI = 6.283185307179586476;
	int j, nn;
	double c, cdft, en, s, sdft;
	doubleW corfac = new doubleW(0);
	doubleW corim = new doubleW(0);
	doubleW corre = new doubleW(0);
	double[] cpol = new double[MPOL];
	double[] spol = new double[MPOL];
	double[] xpol = new double[MPOL];
	if (init != 1 || a != aold || b != bold || func != funcold) {
	    init = 1;
	    aold = a;
	    bold = b;
	    funcold = func;
	    delta = (b - a) / M;
	    for (j = 0; j < M + 1; j++)
		data[j] = func.funk(a + j * delta);
	    for (j = M + 1; j < NDFT; j++)
		data[j] = 0.0;
	    for (j = 0; j < 4; j++) {
		endpts[j] = data[j];
		endpts[j + 4] = data[M - 3 + j];
	    }
	    FFT.realft(data, 1);
	    data[1] = 0.0;
	}
	en = w * delta * NDFT / TWOPI + 1.0;
	nn = min(max((int) (en - 0.5 * MPOL + 1.0), 1), NDFT / 2 - MPOL + 1);
	for (j = 0; j < MPOL; j++, nn++) {
	    cpol[j] = data[2 * nn - 2];
	    spol[j] = data[2 * nn - 1];
	    xpol[j] = nn;
	}
	cdft = new Poly_interp(xpol, cpol, MPOL).interp(en);
	sdft = new Poly_interp(xpol, spol, MPOL).interp(en);
	Fourier.dftcor(w, delta, a, b, endpts, corre, corim, corfac);
	cdft *= corfac.val;
	sdft *= corfac.val;
	cdft += corre.val;
	sdft += corim.val;
	c = delta * cos(w * a);
	s = delta * sin(w * a);
	cosint.val = c * cdft - s * sdft;
	sinint.val = s * cdft + c * sdft;
    }
}
