package com.nr.fft;

import static java.lang.Math.*;

import java.io.IOException;

import org.netlib.util.intW;

import com.nr.Complex;

import java.nio.ByteBuffer;
import static com.nr.NRUtil.*;

/**
 * FFT routines.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class FFT {
    private FFT() {
    }

    /**
     * Replaces data[0..2*n-1] by its discrete Fourier transform, if isign is
     * input as 1; or replaces data[0..2*n-1] by n times its inverse discrete
     * Fourier transform, if isign is input as 1. data is a complex array of
     * length n stored as a real array of length 2*n. n must be an integer power
     * of 2.
     * 
     * @param data
     * @param n
     * @param isign
     */
    public static void four1(final double[] data, final int n, final int isign) {
	int nn, mmax, m, j, istep, i;
	double wtemp, wr, wpr, wpi, wi, theta, tempr, tempi;
	if (n < 2 || (n & (n - 1)) != 0)
	    throw new IllegalArgumentException("n must be power of 2 in four1");
	nn = n << 1;
	j = 1;
	for (i = 1; i < nn; i += 2) {
	    if (j > i) {
		swap(data, j - 1, i - 1);
		swap(data, j, i);
	    }
	    m = n;
	    while (m >= 2 && j > m) {
		j -= m;
		m >>= 1;
	    }
	    j += m;
	}
	mmax = 2;
	while (nn > mmax) {
	    istep = mmax << 1;
	    theta = isign * (6.28318530717959 / mmax);
	    wtemp = sin(0.5 * theta);
	    wpr = -2.0 * wtemp * wtemp;
	    wpi = sin(theta);
	    wr = 1.0;
	    wi = 0.0;
	    for (m = 1; m < mmax; m += 2) {
		for (i = m; i <= nn; i += istep) {
		    j = i + mmax;
		    tempr = wr * data[j - 1] - wi * data[j];
		    tempi = wr * data[j] + wi * data[j - 1];
		    data[j - 1] = data[i - 1] - tempr;
		    data[j] = data[i] - tempi;
		    data[i - 1] += tempr;
		    data[i] += tempi;
		}
		wr = (wtemp = wr) * wpr - wi * wpi + wr;
		wi = wi * wpr + wtemp * wpi + wi;
	    }
	    mmax = istep;
	}
    }

    public static void four1(final double[] data, final int isign) {
	four1(data, data.length / 2, isign);
    }

    public static void four1(final Complex[] data, final int isign) {
	int n = data.length * 2;
	double[] rd = new double[n];
	for (int i = 0; i < data.length; i++) {
	    rd[2 * i] = data[i].re();
	    rd[2 * i + 1] = data[i].im();
	}
	four1(rd, data.length, isign);
	for (int i = 0; i < data.length; i++) {
	    data[i] = new Complex(rd[2 * i], rd[2 * i + 1]);
	}
    }

    /**
     * Calculates the Fourier transform of a set of n real-valued data points.
     * Replaces these data (which are stored in array data[0..n-1]) by the
     * positive frequency half of their complex Fourier transform. The
     * real-valued first and last components of the complex transform are
     * returned as elements data[0] and data[1], respectively. n must be a power
     * of 2. This routine also calculates the inverse transform of a complex
     * data array if it is the transform of real data. (Result in this case must
     * be multiplied by 2/n.)
     * 
     * @param data
     * @param isign
     */
    public static void realft(final double[] data, final int isign) {
	int i, i1, i2, i3, i4, n = data.length;
	double c1 = 0.5, c2, h1r, h1i, h2r, h2i, wr, wi, wpr, wpi, wtemp;
	double theta = PI / (n >> 1);
	if (isign == 1) {
	    c2 = -0.5;
	    four1(data, 1);
	} else {
	    c2 = 0.5;
	    theta = -theta;
	}
	wtemp = sin(0.5 * theta);
	wpr = -2.0 * wtemp * wtemp;
	wpi = sin(theta);
	wr = 1.0 + wpr;
	wi = wpi;
	for (i = 1; i < (n >> 2); i++) {
	    i2 = 1 + (i1 = i + i);
	    i4 = 1 + (i3 = n - i1);
	    h1r = c1 * (data[i1] + data[i3]);
	    h1i = c1 * (data[i2] - data[i4]);
	    h2r = -c2 * (data[i2] + data[i4]);
	    h2i = c2 * (data[i1] - data[i3]);
	    data[i1] = h1r + wr * h2r - wi * h2i;
	    data[i2] = h1i + wr * h2i + wi * h2r;
	    data[i3] = h1r - wr * h2r + wi * h2i;
	    data[i4] = -h1i + wr * h2i + wi * h2r;
	    wr = (wtemp = wr) * wpr - wi * wpi + wr;
	    wi = wi * wpr + wtemp * wpi + wi;
	}
	if (isign == 1) {
	    data[0] = (h1r = data[0]) + data[1];
	    data[1] = h1r - data[1];
	} else {
	    data[0] = c1 * ((h1r = data[0]) + data[1]);
	    data[1] = c1 * (h1r - data[1]);
	    four1(data, -1);
	}
    }

    /**
     * Calculates the sine transform of a set of n real-valued data points
     * stored in array y[0..n-1]. The number n must be a power of 2. On exit, y
     * is replaced by its transform. This program, without changes, also
     * calculates the inverse sine transform, but in this case the output array
     * should be multiplied by 2/n.
     * 
     * @param y
     */
    public static void sinft(final double[] y) {
	int j, n = y.length;
	double sum, y1, y2, theta, wi = 0.0, wr = 1.0, wpi, wpr, wtemp;
	theta = PI / n;
	wtemp = sin(0.5 * theta);
	wpr = -2.0 * wtemp * wtemp;
	wpi = sin(theta);
	y[0] = 0.0;
	for (j = 1; j < (n >> 1) + 1; j++) {
	    wr = (wtemp = wr) * wpr - wi * wpi + wr;
	    wi = wi * wpr + wtemp * wpi + wi;
	    y1 = wi * (y[j] + y[n - j]);
	    y2 = 0.5 * (y[j] - y[n - j]);
	    y[j] = y1 + y2;
	    y[n - j] = y1 - y2;
	}
	realft(y, 1);
	y[0] *= 0.5;
	sum = y[1] = 0.0;
	for (j = 0; j < n - 1; j += 2) {
	    sum += y[j];
	    y[j] = y[j + 1];
	    y[j + 1] = sum;
	}
    }

    /**
     * Calculates the cosine transform of a set y[0..n] of real-valued data
     * points. The transformed data replace the original data in array y. n must
     * be a power of 2. This program, without changes, also calculates the
     * inverse cosine transform, but in this case the output array should be
     * multiplied by 2/n.
     * 
     * @param y
     */
    public static void cosft1(final double[] y) {
	int j, n = y.length - 1;
	double sum, y1, y2, theta, wi = 0.0, wpi, wpr, wr = 1.0, wtemp;
	double[] yy = new double[n];
	theta = PI / n;
	wtemp = sin(0.5 * theta);
	wpr = -2.0 * wtemp * wtemp;
	wpi = sin(theta);
	sum = 0.5 * (y[0] - y[n]);
	yy[0] = 0.5 * (y[0] + y[n]);
	for (j = 1; j < n / 2; j++) {
	    wr = (wtemp = wr) * wpr - wi * wpi + wr;
	    wi = wi * wpr + wtemp * wpi + wi;
	    y1 = 0.5 * (y[j] + y[n - j]);
	    y2 = (y[j] - y[n - j]);
	    yy[j] = y1 - wi * y2;
	    yy[n - j] = y1 + wi * y2;
	    sum += wr * y2;
	}
	yy[n / 2] = y[n / 2];
	realft(yy, 1);
	for (j = 0; j < n; j++)
	    y[j] = yy[j];
	y[n] = y[1];
	y[1] = sum;
	for (j = 3; j < n; j += 2) {
	    sum += y[j];
	    y[j] = sum;
	}
    }

    /**
     * Calculates the "staggered" cosine transform of a set y[0..n-1] of
     * real-valued data points. The transformed data replace the original data
     * in array y. n must be a power of 2. Set isign to C1 for a transform, and
     * to 1 for an inverse transform. For an inverse transform, the output array
     * should be multiplied by 2/n.
     * 
     * @param y
     * @param isign
     */
    public static void cosft2(final double[] y, final int isign) {
	int i, n = y.length;
	double sum, sum1, y1, y2, ytemp, theta, wi = 0.0, wi1, wpi, wpr, wr = 1.0, wr1, wtemp;
	theta = 0.5 * PI / n;
	wr1 = cos(theta);
	wi1 = sin(theta);
	wpr = -2.0 * wi1 * wi1;
	wpi = sin(2.0 * theta);
	if (isign == 1) {
	    for (i = 0; i < n / 2; i++) {
		y1 = 0.5 * (y[i] + y[n - 1 - i]);
		y2 = wi1 * (y[i] - y[n - 1 - i]);
		y[i] = y1 + y2;
		y[n - 1 - i] = y1 - y2;
		wr1 = (wtemp = wr1) * wpr - wi1 * wpi + wr1;
		wi1 = wi1 * wpr + wtemp * wpi + wi1;
	    }
	    realft(y, 1);
	    for (i = 2; i < n; i += 2) {
		wr = (wtemp = wr) * wpr - wi * wpi + wr;
		wi = wi * wpr + wtemp * wpi + wi;
		y1 = y[i] * wr - y[i + 1] * wi;
		y2 = y[i + 1] * wr + y[i] * wi;
		y[i] = y1;
		y[i + 1] = y2;
	    }
	    sum = 0.5 * y[1];
	    for (i = n - 1; i > 0; i -= 2) {
		sum1 = sum;
		sum += y[i];
		y[i] = sum1;
	    }
	} else if (isign == -1) {
	    ytemp = y[n - 1];
	    for (i = n - 1; i > 2; i -= 2)
		y[i] = y[i - 2] - y[i];
	    y[1] = 2.0 * ytemp;
	    for (i = 2; i < n; i += 2) {
		wr = (wtemp = wr) * wpr - wi * wpi + wr;
		wi = wi * wpr + wtemp * wpi + wi;
		y1 = y[i] * wr + y[i + 1] * wi;
		y2 = y[i + 1] * wr - y[i] * wi;
		y[i] = y1;
		y[i + 1] = y2;
	    }
	    realft(y, -1);
	    for (i = 0; i < n / 2; i++) {
		y1 = y[i] + y[n - 1 - i];
		y2 = (0.5 / wi1) * (y[i] - y[n - 1 - i]);
		y[i] = 0.5 * (y1 + y2);
		y[n - 1 - i] = 0.5 * (y1 - y2);
		wr1 = (wtemp = wr1) * wpr - wi1 * wpi + wr1;
		wi1 = wi1 * wpr + wtemp * wpi + wi1;
	    }
	}
    }

    /**
     * Replaces data by its ndim-dimensional discrete Fourier transform, if
     * isign is input as 1. nn[0..ndim-1] is an integer array containing the
     * lengths of each dimension (number of com- plex values), which must all be
     * powers of 2. data is a real array of length twice the product of these
     * lengths, in which the data are stored as in a multidimensional complex
     * array: real and imaginary parts of each element are in consecutive
     * locations, and the rightmost index of the array increases most rapidly as
     * one proceeds along data. For a two-dimensional array, this is equivalent
     * to storing the array by rows. If isign is input as 1, data is replaced by
     * its inverse transform times the product of the lengths of all dimensions.
     * 
     * @param data
     * @param nn
     * @param isign
     */
    public static void fourn(final double[] data, final int[] nn,
	    final int isign) {
	int idim, i1, i2, i3, i2rev, i3rev, ip1, ip2, ip3, ifp1, ifp2;
	int ibit, k1, k2, n, nprev, nrem, ntot = 1, ndim = nn.length;
	double tempi, tempr, theta, wi, wpi, wpr, wr, wtemp;
	for (idim = 0; idim < ndim; idim++)
	    ntot *= nn[idim];
	if (ntot < 2 || (ntot & (ntot - 1)) != 0)
	    throw new IllegalArgumentException("must have powers of 2 in fourn");
	nprev = 1;
	for (idim = ndim - 1; idim >= 0; idim--) {
	    n = nn[idim];
	    nrem = ntot / (n * nprev);
	    ip1 = nprev << 1;
	    ip2 = ip1 * n;
	    ip3 = ip2 * nrem;
	    i2rev = 0;
	    for (i2 = 0; i2 < ip2; i2 += ip1) {
		if (i2 < i2rev) {
		    for (i1 = i2; i1 < i2 + ip1 - 1; i1 += 2) {
			for (i3 = i1; i3 < ip3; i3 += ip2) {
			    i3rev = i2rev + i3 - i2;
			    swap(data, i3, i3rev);
			    swap(data, i3 + 1, i3rev + 1);
			}
		    }
		}
		ibit = ip2 >> 1;
		while (ibit >= ip1 && i2rev + 1 > ibit) {
		    i2rev -= ibit;
		    ibit >>= 1;
		}
		i2rev += ibit;
	    }
	    ifp1 = ip1;
	    while (ifp1 < ip2) {
		ifp2 = ifp1 << 1;
		theta = isign * 6.28318530717959 / (ifp2 / ip1);
		wtemp = sin(0.5 * theta);
		wpr = -2.0 * wtemp * wtemp;
		wpi = sin(theta);
		wr = 1.0;
		wi = 0.0;
		for (i3 = 0; i3 < ifp1; i3 += ip1) {
		    for (i1 = i3; i1 < i3 + ip1 - 1; i1 += 2) {
			for (i2 = i1; i2 < ip3; i2 += ifp2) {
			    k1 = i2;
			    k2 = k1 + ifp1;
			    tempr = wr * data[k2] - wi * data[k2 + 1];
			    tempi = wr * data[k2 + 1] + wi * data[k2];
			    data[k2] = data[k1] - tempr;
			    data[k2 + 1] = data[k1 + 1] - tempi;
			    data[k1] += tempr;
			    data[k1 + 1] += tempi;
			}
		    }
		    wr = (wtemp = wr) * wpr - wi * wpi + wr;
		    wi = wi * wpr + wtemp * wpi + wi;
		}
		ifp1 = ifp2;
	    }
	    nprev *= n;
	}
    }

    /**
     * Given a three-dimensional real array data[0..nn1-1][0..nn2-1][0..nn3-1]
     * (where nn1 D 1 for the case of a logically two-dimensional array), this
     * routine returns (for isign=1) the complex fast Fourier transform as two
     * complex arrays: On output, data contains the zero and positive frequency
     * values of the third frequency component, while speq[0..nn1-1][0..2*nn2-1]
     * con- tains the Nyquist critical frequency values of the third frequency
     * component. First (and sec- ond) frequency components are stored for zero,
     * positive, and negative frequencies, in standard wraparound order. See
     * text for description of how complex values are arranged. For isign=-1,
     * the inverse transform (times nn1*nn2*nn3/2 as a constant multiplicative
     * factor) is performed, with output data (viewed as a real array) deriving
     * from input data (viewed as complex) and speq. For inverse transforms on
     * data not generated first by a forward transform, make sure the complex
     * input data array satisfies property (12.6.2). The dimensions nn1, nn2,
     * nn3 must always be integer powers of 2.
     * 
     * @param data
     * @param speq
     * @param isign
     * @param nn1
     * @param nn2
     * @param nn3
     */
    public static void rlft3(final double[] data, final double[] speq,
	    final int isign, final int nn1, final int nn2, final int nn3) {
	int i1, i2, i3, j1, j2, j3, k1, k2, k3, k4;
	double theta, wi, wpi, wpr, wr, wtemp;
	double c1, c2, h1r, h1i, h2r, h2i;
	int[] nn = new int[3];
	double[][] spq = new double[nn1][2 * nn2];
	// for (i1=0;i1<nn1;i1++) spq[i1] = speq + 2*nn2*i1;
	// copy data from speq to spq;
	for (i1 = 0; i1 < nn1; i1++)
	    for (j2 = 0; j2 < 2 * nn2; j2++)
		spq[i1][j2] = speq[2 * nn2 * i1 + j2];
	c1 = 0.5;
	c2 = -0.5 * isign;
	theta = isign * (6.28318530717959 / nn3);
	wtemp = sin(0.5 * theta);
	wpr = -2.0 * wtemp * wtemp;
	wpi = sin(theta);
	nn[0] = nn1;
	nn[1] = nn2;
	nn[2] = nn3 >> 1;
	if (isign == 1) {
	    fourn(data, nn, isign);
	    k1 = 0;
	    for (i1 = 0; i1 < nn1; i1++)
		for (i2 = 0, j2 = 0; i2 < nn2; i2++, k1 += nn3) {
		    spq[i1][j2++] = data[k1];
		    spq[i1][j2++] = data[k1 + 1];
		}
	}
	for (i1 = 0; i1 < nn1; i1++) {
	    j1 = (i1 != 0 ? nn1 - i1 : 0);
	    wr = 1.0;
	    wi = 0.0;
	    for (i3 = 0; i3 <= (nn3 >> 1); i3 += 2) {
		k1 = i1 * nn2 * nn3;
		k3 = j1 * nn2 * nn3;
		for (i2 = 0; i2 < nn2; i2++, k1 += nn3) {
		    if (i3 == 0) {
			j2 = (i2 != 0 ? ((nn2 - i2) << 1) : 0);
			h1r = c1 * (data[k1] + spq[j1][j2]);
			h1i = c1 * (data[k1 + 1] - spq[j1][j2 + 1]);
			h2i = c2 * (data[k1] - spq[j1][j2]);
			h2r = -c2 * (data[k1 + 1] + spq[j1][j2 + 1]);
			data[k1] = h1r + h2r;
			data[k1 + 1] = h1i + h2i;
			spq[j1][j2] = h1r - h2r;
			spq[j1][j2 + 1] = h2i - h1i;
		    } else {
			j2 = (i2 != 0 ? nn2 - i2 : 0);
			j3 = nn3 - i3;
			k2 = k1 + i3;
			k4 = k3 + j2 * nn3 + j3;
			h1r = c1 * (data[k2] + data[k4]);
			h1i = c1 * (data[k2 + 1] - data[k4 + 1]);
			h2i = c2 * (data[k2] - data[k4]);
			h2r = -c2 * (data[k2 + 1] + data[k4 + 1]);
			data[k2] = h1r + wr * h2r - wi * h2i;
			data[k2 + 1] = h1i + wr * h2i + wi * h2r;
			data[k4] = h1r - wr * h2r + wi * h2i;
			data[k4 + 1] = -h1i + wr * h2i + wi * h2r;
		    }
		}
		wr = (wtemp = wr) * wpr - wi * wpi + wr;
		wi = wi * wpr + wtemp * wpi + wi;
	    }
	}
	if (isign == -1)
	    fourn(data, nn, isign);

	// copy spq back to speq;
	for (i1 = 0; i1 < nn1; i1++)
	    for (j2 = 0; j2 < 2 * nn2; j2++)
		speq[2 * nn2 * i1 + j2] = spq[i1][j2];

    }

    public static void rlft3(final double[][][] data, final double[][] speq,
	    final int isign) {
	int dim1 = data.length;
	int dim2 = data[0].length;
	int dim3 = data[0][0].length;

	if (speq.length != dim1 || speq[0].length != 2 * dim2)
	    throw new IllegalArgumentException("bad dims in rlft3");
	double[] d = new double[dim1 * dim2 * dim3];
	double[] s = new double[dim1 * 2 * dim2];
	for (int i = 0; i < dim1; i++)
	    for (int j = 0; j < dim2; j++)
		for (int k = 0; k < dim3; k++)
		    d[i * dim2 * dim3 + j * dim3 + k] = data[i][j][k];
	for (int i = 0; i < dim1; i++)
	    for (int j = 0; j < 2 * dim2; j++)
		s[i * 2 * dim2 + j] = speq[i][j];

	rlft3(d, s, isign, dim1, dim2, dim3);

	for (int i = 0; i < dim1; i++)
	    for (int j = 0; j < dim2; j++)
		for (int k = 0; k < dim3; k++)
		    data[i][j][k] = d[i * dim2 * dim3 + j * dim3 + k];
	for (int i = 0; i < dim1; i++)
	    for (int j = 0; j < 2 * dim2; j++)
		speq[i][j] = s[i * 2 * dim2 + j];
    }

    public static void rlft3(final double[][] data, final double[] speq,
	    final int isign) {
	if (speq.length != 2 * data.length)
	    throw new IllegalArgumentException("bad dims in rlft3");
	int nn = data.length;
	int mm = data[0].length;
	double[] d = new double[nn * mm];
	for (int i = 0; i < nn; i++)
	    for (int j = 0; j < mm; j++)
		d[i * mm + j] = data[i][j];
	rlft3(d, speq, isign, 1, nn, mm);
	for (int i = 0; i < nn; i++)
	    for (int j = 0; j < mm; j++)
		data[i][j] = d[i * mm + j];
    }

    private static void fourew(final java.nio.channels.FileChannel[] file,
	    final intW na, intW nb, intW nc, intW nd) throws IOException {
	int i;
	for (i = 0; i < 4; i++)
	    file[i].position(0);
	swap(file, 1, 3);
	swap(file, 0, 2);
	na.val = 2;
	nb.val = 3;
	nc.val = 0;
	nd.val = 1;
    }

    /**
     * 
     * @param file
     * @param nn
     * @param isign
     * @throws IOException
     */
    public static void fourfs(java.nio.channels.FileChannel[] file, int[] nn,
	    final int isign) throws IOException {
	final int KBF = 128;
	final int doubSize = 8;
	final int[] mate = { 1, 0, 3, 2 };
	long cc, cc0;
	int j, j12, jk, k, kk, n = 1, mm, kc = 0, kd, ks, kr, nr, ns, nv;
	final intW na = new intW(0);
	final intW nb = new intW(0);
	final intW nc = new intW(0);
	final intW nd = new intW(0);
	double tempr, tempi, wr, wi, wpr, wpi, wtemp, theta;
	double[] afa = new double[KBF], afb = new double[KBF], afc = new double[KBF];
	ByteBuffer bb = ByteBuffer.allocate(KBF * doubSize);
	int ndim = nn.length;
	for (j = 0; j < ndim; j++) {
	    n *= nn[j];
	    if (nn[j] <= 1)
		throw new IllegalArgumentException(
			"invalid double or wrong ndim in fourfs");
	}
	nv = 0;
	jk = nn[nv];
	mm = n;
	ns = n / KBF;
	nr = ns >> 1;
	kd = KBF >> 1;
	ks = n;
	fourew(file, na, nb, nc, nd);
	for (;;) {
	    theta = isign * PI / (n / mm);
	    wtemp = sin(0.5 * theta);
	    wpr = -2.0 * wtemp * wtemp;
	    wpi = sin(theta);
	    wr = 1.0;
	    wi = 0.0;
	    mm >>= 1;
	    for (j12 = 0; j12 < 2; j12++) {
		kr = 0;
		do {
		    cc0 = file[na.val].position() / doubSize;
		    // file[na].read((char *) &afa[0],KBF*doubSize);
		    file[na.val].read(bb);
		    bb.flip();
		    for (int qq = 0; qq < KBF; qq++)
			afa[qq] = bb.getDouble();
		    bb.clear();
		    cc = file[na.val].position() / doubSize;
		    if ((cc - cc0) != KBF)
			throw new IllegalArgumentException(
				"read error 1 in fourfs");
		    cc0 = file[nb.val].position() / doubSize;
		    // file[nb].read((char *) &afb[0],KBF*doubSize);
		    file[nb.val].read(bb);
		    bb.flip();
		    cc = file[nb.val].position() / doubSize;
		    for (int qq = 0; qq < KBF; qq++)
			afb[qq] = bb.getDouble();
		    bb.clear();

		    if ((cc - cc0) != KBF)
			throw new IllegalArgumentException(
				"read error 2 in fourfs");
		    for (j = 0; j < KBF; j += 2) {
			tempr = wr * afb[j] - wi * afb[j + 1];
			tempi = wi * afb[j] + wr * afb[j + 1];
			afb[j] = afa[j] - tempr;
			afa[j] += tempr;
			afb[j + 1] = afa[j + 1] - tempi;
			afa[j + 1] += tempi;
		    }
		    kc += kd;
		    if (kc == mm) {
			kc = 0;
			wr = (wtemp = wr) * wpr - wi * wpi + wr;
			wi = wi * wpr + wtemp * wpi + wi;
		    }
		    cc0 = file[nc.val].position() / doubSize;
		    // file[nc].write((char *) &afa[0],KBF*doubSize);
		    for (int qq = 0; qq < KBF; qq++)
			bb.putDouble(afa[qq]);
		    bb.flip();
		    file[nc.val].write(bb);
		    bb.clear();
		    cc = file[nc.val].position() / doubSize;
		    if ((cc - cc0) != KBF)
			throw new IllegalArgumentException(
				"write error 1 in fourfs");
		    cc0 = file[nd.val].position() / doubSize;
		    // file[nd].write((char *) &afb[0],KBF*doubSize);
		    for (int qq = 0; qq < KBF; qq++)
			bb.putDouble(afb[qq]);
		    bb.flip();
		    file[nd.val].write(bb);
		    bb.clear();

		    cc = file[nd.val].position() / doubSize;
		    if ((cc - cc0) != KBF)
			throw new IllegalArgumentException(
				"write error 2 in fourfs");
		} while (++kr < nr);
		if (j12 == 0 && ks != n && ks == KBF) {
		    na.val = mate[na.val];
		    nb.val = na.val;
		}
		if (nr == 0)
		    break;
	    }
	    fourew(file, na, nb, nc, nd);
	    jk >>= 1;
	    while (jk == 1) {
		mm = n;
		jk = nn[++nv];
	    }
	    ks >>= 1;
	    if (ks > KBF) {
		for (j12 = 0; j12 < 2; j12++) {
		    for (kr = 0; kr < ns; kr += ks / KBF) {
			for (k = 0; k < ks; k += KBF) {
			    cc0 = file[na.val].position() / doubSize;
			    // file[na].read((char *) &afa[0],KBF*doubSize);
			    file[na.val].read(bb);
			    bb.flip();
			    for (int qq = 0; qq < KBF; qq++)
				afa[qq] = bb.getDouble();
			    bb.clear();

			    cc = file[na.val].position() / doubSize;
			    if ((cc - cc0) != KBF)
				throw new IllegalArgumentException(
					"read error 3 in fourfs");
			    cc0 = file[nc.val].position() / doubSize;
			    // file[nc]).write((char *) &afa[0],KBF*doubSize);
			    for (int qq = 0; qq < KBF; qq++)
				bb.putDouble(afa[qq]);
			    bb.flip();
			    file[nc.val].write(bb);
			    bb.clear();
			    cc = file[nc.val].position() / doubSize;
			    if ((cc - cc0) != KBF)
				throw new IllegalArgumentException(
					"write error 3 in fourfs");
			}
			nc.val = mate[nc.val];
		    }
		    na.val = mate[na.val];
		}
		fourew(file, na, nb, nc, nd);
	    } else if (ks == KBF)
		nb.val = na.val;
	    else
		break;
	}
	j = 0;
	for (;;) {
	    theta = isign * PI / (n / mm);
	    wtemp = sin(0.5 * theta);
	    wpr = -2.0 * wtemp * wtemp;
	    wpi = sin(theta);
	    wr = 1.0;
	    wi = 0.0;
	    mm >>= 1;
	    ks = kd;
	    kd >>= 1;
	    for (j12 = 0; j12 < 2; j12++) {
		for (kr = 0; kr < ns; kr++) {
		    cc0 = file[na.val].position() / doubSize;
		    // file[na].read((char *) &afc[0],KBF*doubSize);
		    file[na.val].read(bb);
		    bb.flip();
		    for (int qq = 0; qq < KBF; qq++)
			afc[qq] = bb.getDouble();
		    bb.clear();

		    cc = file[na.val].position() / doubSize;
		    if ((cc - cc0) != KBF)
			throw new IllegalArgumentException(
				"read error 4 in fourfs");
		    kk = 0;
		    k = ks;
		    for (;;) {
			tempr = wr * afc[kk + ks] - wi * afc[kk + ks + 1];
			tempi = wi * afc[kk + ks] + wr * afc[kk + ks + 1];
			afa[j] = afc[kk] + tempr;
			afb[j] = afc[kk] - tempr;
			afa[++j] = afc[++kk] + tempi;
			afb[j++] = afc[kk++] - tempi;
			if (kk < k)
			    continue;
			kc += kd;
			if (kc == mm) {
			    kc = 0;
			    wr = (wtemp = wr) * wpr - wi * wpi + wr;
			    wi = wi * wpr + wtemp * wpi + wi;
			}
			kk += ks;
			if (kk > KBF - 1)
			    break;
			else
			    k = kk + ks;
		    }
		    if (j > KBF - 1) {
			cc0 = file[nc.val].position() / doubSize;
			// file[nc].write((char *) &afa[0],KBF*doubSize);
			for (int qq = 0; qq < KBF; qq++)
			    bb.putDouble(afa[qq]);
			bb.flip();
			file[nc.val].write(bb);
			bb.clear();

			cc = file[nc.val].position() / doubSize;
			if ((cc - cc0) != KBF)
			    throw new IllegalArgumentException(
				    "write error 4 in fourfs");
			cc0 = file[nd.val].position() / doubSize;
			// file[nd].write((char *) &afb[0],KBF*doubSize);
			for (int qq = 0; qq < KBF; qq++)
			    bb.putDouble(afb[qq]);
			bb.flip();
			file[nd.val].write(bb);
			bb.clear();

			cc = file[nd.val].position() / doubSize;
			if ((cc - cc0) != KBF)
			    throw new IllegalArgumentException(
				    "write error 5 in fourfs");
			j = 0;
		    }
		}
		na.val = mate[na.val];
	    }
	    fourew(file, na, nb, nc, nd);
	    jk >>= 1;
	    if (jk > 1)
		continue;
	    mm = n;
	    do {
		if (nv < ndim - 1)
		    jk = nn[++nv];
		else
		    return;
	    } while (jk == 1);
	}
    }
}
