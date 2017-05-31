package com.nr.lna;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Machar {
    public int ibeta, it, irnd, ngrd, machep, negep, iexp, minexp, maxexp;
    public double eps, epsneg, xmin, xmax;

    public Machar() {
	int i, itemp, iz, j, k, mx, nxres;
	double a, b, beta, betah, betain, one, t, temp, temp1, tempa, two, y, z, zero;
	one = (1);
	two = one + one;
	zero = one - one;
	a = one;
	do {
	    a += a;
	    temp = a + one;
	    temp1 = temp - a;
	} while (temp1 - one == zero);
	b = one;
	do {
	    b += b;
	    temp = a + b;
	    itemp = (int) (temp - a);
	} while (itemp == 0);
	ibeta = itemp;
	beta = (ibeta);
	it = 0;
	b = one;
	do {
	    ++it;
	    b *= beta;
	    temp = b + one;
	    temp1 = temp - b;
	} while (temp1 - one == zero);
	irnd = 0;
	betah = beta / two;
	temp = a + betah;
	if (temp - a != zero)
	    irnd = 1;
	tempa = a + beta;
	temp = tempa + betah;
	if (irnd == 0 && temp - tempa != zero)
	    irnd = 2;
	negep = it + 3;
	betain = one / beta;
	a = one;
	for (i = 1; i <= negep; i++)
	    a *= betain;
	b = a;
	for (;;) {
	    temp = one - a;
	    if (temp - one != zero)
		break;
	    a *= beta;
	    --negep;
	}
	negep = -negep;
	epsneg = a;
	machep = -it - 3;
	a = b;
	for (;;) {
	    temp = one + a;
	    if (temp - one != zero)
		break;
	    a *= beta;
	    ++machep;
	}
	eps = a;
	ngrd = 0;
	temp = one + eps;
	if (irnd == 0 && temp * one - one != zero)
	    ngrd = 1;
	i = 0;
	k = 1;
	z = betain;
	t = one + eps;
	nxres = 0;
	for (;;) {
	    y = z;
	    z = y * y;
	    a = z * one;
	    temp = z * t;
	    if (a + a == zero || abs(z) >= y)
		break;
	    temp1 = temp * betain;
	    if (temp1 * beta == z)
		break;
	    ++i;
	    k += k;
	}
	if (ibeta != 10) {
	    iexp = i + 1;
	    mx = k + k;
	} else {
	    iexp = 2;
	    iz = ibeta;
	    while (k >= iz) {
		iz *= ibeta;
		++iexp;
	    }
	    mx = iz + iz - 1;
	}
	for (;;) {
	    xmin = y;
	    y *= betain;
	    a = y * one;
	    temp = y * t;
	    if (a + a != zero && abs(y) < xmin) {
		++k;
		temp1 = temp * betain;
		if (temp1 * beta == y && temp != y) {
		    nxres = 3;
		    xmin = y;
		    break;
		}
	    } else
		break;
	}
	minexp = -k;
	if (mx <= k + k - 3 && ibeta != 10) {
	    mx += mx;
	    ++iexp;
	}
	maxexp = mx + minexp;
	irnd += nxres;
	if (irnd >= 2)
	    maxexp -= 2;
	i = maxexp + minexp;
	if (ibeta == 2 && i == 0)
	    --maxexp;
	if (i > 20)
	    --maxexp;
	if (a != y)
	    maxexp -= 2;
	xmax = one - epsneg;
	if (xmax * one != xmax)
	    xmax = one - beta * epsneg;
	xmax /= (xmin * beta * beta * beta);
	i = maxexp + minexp + 3;
	for (j = 1; j <= i; j++) {
	    if (ibeta == 2)
		xmax += xmax;
	    else
		xmax *= beta;
	}
    }

    public void report() {
	System.out
		.printf("quantity:  numeric_limits<double> says  (we calculate)\n");
	System.out.printf("radix:  %d  (%d)\n", FLT_RADIX, ibeta);
	System.out.printf("mantissa digits:  %d  (%d)\n", DBL_MANT_DIG, it);
	System.out
		.printf("round style:  %d  (%d) [our 5 == IEEE 1]\n", 1, irnd); // round_style=1
	System.out.printf("guard digits:  [not in numeric_limits]  (%d)\n",
		ngrd);
	System.out.printf("epsilon:  %g  (%g)\n", DBL_EPSILON, eps);
	System.out.printf("neg epsilon:  [not in numeric_limits]  (%g)\n",
		epsneg);
	System.out.printf("epsilon power:  [not in numeric_limits]  (%d)\n",
		machep);
	System.out.printf(
		"neg epsilon power:  [not in numeric_limits]  (%d)\n", negep);
	System.out.printf("exponent digits:  [not in numeric_limits]  (%d)\n",
		iexp);
	System.out.printf("min exponent:  %d  (%d)\n", Double.MIN_EXPONENT,
		minexp);
	System.out.printf("max exponent:  %d  (%d)\n", Double.MAX_EXPONENT,
		maxexp);
	System.out.printf("minimum:  %g  (%g)\n", Double.MIN_NORMAL, xmin);
	System.out.printf("maximum:  %g  (%g)\n", Double.MAX_VALUE, xmax);
    }

    public static void main(String[] args) {
	Machar machar = new Machar();
	machar.report();
    }
}
