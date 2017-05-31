package com.nr.sort;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Sorter {
    private Sorter() {
    }

    public static void sort(final double[] arr) {
	sort(arr, -1);
    }

    public static void sort(final double[] arr, final int m) {
	final int M = 7, NSTACK = 64;
	int i, ir, j, k, jstack = -1, l = 0, n = arr.length;
	double a;
	int[] istack = new int[NSTACK];
	if (m > 0)
	    n = min(m, n);
	ir = n - 1;
	for (;;) {
	    if (ir - l < M) {
		for (j = l + 1; j <= ir; j++) {
		    a = arr[j];
		    for (i = j - 1; i >= l; i--) {
			if (arr[i] <= a)
			    break;
			arr[i + 1] = arr[i];
		    }
		    arr[i + 1] = a;
		}
		if (jstack < 0)
		    break;
		ir = istack[jstack--];
		l = istack[jstack--];
	    } else {
		k = (l + ir) >> 1;
		swap(arr, k, l + 1);
		if (arr[l] > arr[ir]) {
		    swap(arr, l, ir);
		}
		if (arr[l + 1] > arr[ir]) {
		    swap(arr, l + 1, ir);
		}
		if (arr[l] > arr[l + 1]) {
		    swap(arr, l, l + 1);
		}
		i = l + 1;
		j = ir;
		a = arr[l + 1];
		for (;;) {
		    do
			i++;
		    while (arr[i] < a);
		    do
			j--;
		    while (arr[j] > a);
		    if (j < i)
			break;
		    swap(arr, i, j);
		}
		arr[l + 1] = arr[j];
		arr[j] = a;
		jstack += 2;
		if (jstack >= NSTACK)
		    throw new IllegalArgumentException(
			    "NSTACK too small in sort.");
		if (ir - i + 1 >= j - l) {
		    istack[jstack] = ir;
		    istack[jstack - 1] = i;
		    ir = j - 1;
		} else {
		    istack[jstack] = j - 1;
		    istack[jstack - 1] = l;
		    l = i;
		}
	    }
	}
    }

    public static void hpsort(final double[] ra) {
	int i, n = ra.length;
	for (i = n / 2 - 1; i >= 0; i--)
	    sift_down(ra, i, n - 1);
	for (i = n - 1; i > 0; i--) {
	    swap(ra, 0, i);
	    sift_down(ra, 0, i - 1);
	}
    }

    private static void sift_down(final double[] ra, final int l, final int r) {
	int j, jold;
	double a;
	a = ra[l];
	jold = l;
	j = 2 * l + 1;
	while (j <= r) {
	    if (j < r && ra[j] < ra[j + 1])
		j++;
	    if (a >= ra[j])
		break;
	    ra[jold] = ra[j];
	    jold = j;
	    j = 2 * j + 1;
	}
	ra[jold] = a;
    }

    public static void piksrt(double[] arr) {
	int i, j, n = arr.length;
	double a;
	for (j = 1; j < n; j++) {
	    a = arr[j];
	    i = j;
	    while (i > 0 && arr[i - 1] > a) {
		arr[i] = arr[i - 1];
		i--;
	    }
	    arr[i] = a;
	}
    }

    public static void piksr2(double[] arr, double[] brr) {
	int i, j, n = arr.length;
	double a;
	double b;
	for (j = 1; j < n; j++) {
	    a = arr[j];
	    b = brr[j];
	    i = j;
	    while (i > 0 && arr[i - 1] > a) {
		arr[i] = arr[i - 1];
		brr[i] = brr[i - 1];
		i--;
	    }
	    arr[i] = a;
	    brr[i] = b;
	}
    }

    public static double select(final int k, final double[] arr) {
	int i, ir, j, l, mid, n = arr.length;
	double a;
	l = 0;
	ir = n - 1;
	for (;;) {
	    if (ir <= l + 1) {
		if (ir == l + 1 && arr[ir] < arr[l]) {
		    swap(arr, l, ir);
		}
		return arr[k];
	    } else {
		mid = (l + ir) >> 1;
		swap(arr, mid, l + 1);
		if (arr[l] > arr[ir]) {
		    swap(arr, l, ir);
		}
		if (arr[l + 1] > arr[ir]) {
		    swap(arr, l + 1, ir);
		}
		if (arr[l] > arr[l + 1]) {
		    swap(arr, l, l + 1);
		}
		i = l + 1;
		j = ir;
		a = arr[l + 1];
		for (;;) {
		    do
			i++;
		    while (arr[i] < a);
		    do
			j--;
		    while (arr[j] > a);
		    if (j < i)
			break;
		    swap(arr, i, j);
		}
		arr[l + 1] = arr[j];
		arr[j] = a;
		if (j >= k)
		    ir = j - 1;
		if (j <= k)
		    l = i;
	    }
	}
    }

    public static void shell(final double[] a) {
	shell(a, -1);
    }

    public static void shell(final double[] a, final int m) {
	int i, j, inc, n = a.length;
	double v;
	if (m > 0)
	    n = Math.min(m, n);
	inc = 1;
	do {
	    inc *= 3;
	    inc++;
	} while (inc <= n);
	do {
	    inc /= 3;
	    for (i = inc; i < n; i++) {
		v = a[i];
		j = i;
		while (a[j - inc] > v) {
		    a[j] = a[j - inc];
		    j -= inc;
		    if (j < inc)
			break;
		}
		a[j] = v;
	    }
	} while (inc > 1);
    }

    public static void sort2(final double[] arr, final double[] brr) {
	final int M = 7, NSTACK = 64;
	int i, ir, j, k, jstack = -1, l = 0, n = arr.length;
	double a;
	double b;
	int[] istack = new int[NSTACK];
	ir = n - 1;
	for (;;) {
	    if (ir - l < M) {
		for (j = l + 1; j <= ir; j++) {
		    a = arr[j];
		    b = brr[j];
		    for (i = j - 1; i >= l; i--) {
			if (arr[i] <= a)
			    break;
			arr[i + 1] = arr[i];
			brr[i + 1] = brr[i];
		    }
		    arr[i + 1] = a;
		    brr[i + 1] = b;
		}
		if (jstack < 0)
		    break;
		ir = istack[jstack--];
		l = istack[jstack--];
	    } else {
		k = (l + ir) >> 1;
		swap(arr, k, l + 1);
		swap(brr, k, l + 1);
		if (arr[l] > arr[ir]) {
		    swap(arr, l, ir);
		    swap(brr, l, ir);
		}
		if (arr[l + 1] > arr[ir]) {
		    swap(arr, l + 1, ir);
		    swap(brr, l + 1, ir);
		}
		if (arr[l] > arr[l + 1]) {
		    swap(arr, l, l + 1);
		    swap(brr, l, l + 1);
		}
		i = l + 1;
		j = ir;
		a = arr[l + 1];
		b = brr[l + 1];
		for (;;) {
		    do
			i++;
		    while (arr[i] < a);
		    do
			j--;
		    while (arr[j] > a);
		    if (j < i)
			break;
		    swap(arr, i, j);
		    swap(brr, i, j);
		}
		arr[l + 1] = arr[j];
		arr[j] = a;
		brr[l + 1] = brr[j];
		brr[j] = b;
		jstack += 2;
		if (jstack >= NSTACK)
		    throw new IllegalArgumentException(
			    "NSTACK too small in sort2.");
		if (ir - i + 1 >= j - l) {
		    istack[jstack] = ir;
		    istack[jstack - 1] = i;
		    ir = j - 1;
		} else {
		    istack[jstack] = j - 1;
		    istack[jstack - 1] = l;
		    l = i;
		}
	    }
	}
    }

    /**
     * select Mth largest in place
     * 
     */
    public static double selip(final int k, final double[] arr) {
	final int M = 64;
	final double BIG = .99e99;
	int i, j, jl, jm, ju, kk, mm, nlo, nxtmm, n = arr.length;
	double ahi, alo, sum;
	int[] isel = new int[M + 2];
	double[] sel = new double[M + 2];
	if (k < 0 || k > n - 1)
	    throw new IllegalArgumentException("bad input to selip");
	kk = k;
	ahi = BIG;
	alo = -BIG;
	for (;;) {
	    mm = nlo = 0;
	    sum = 0.0;
	    nxtmm = M + 1;
	    for (i = 0; i < n; i++) {
		if (arr[i] >= alo && arr[i] <= ahi) {
		    mm++;
		    if (arr[i] == alo)
			nlo++;
		    if (mm <= M)
			sel[mm - 1] = arr[i];
		    else if (mm == nxtmm) {
			nxtmm = mm + mm / M;
			sel[(i + 2 + mm + kk) % M] = arr[i];
		    }
		    sum += arr[i];
		}
	    }
	    if (kk < nlo) {
		return alo;
	    } else if (mm < M + 1) {
		Sorter.shell(sel, mm);
		ahi = sel[kk];
		return ahi;
	    }
	    sel[M] = sum / mm;
	    Sorter.shell(sel, M + 1);
	    sel[M + 1] = ahi;
	    for (j = 0; j < M + 2; j++)
		isel[j] = 0;
	    for (i = 0; i < n; i++) {
		if (arr[i] >= alo && arr[i] <= ahi) {
		    jl = 0;
		    ju = M + 2;
		    while (ju - jl > 1) {
			jm = (ju + jl) / 2;
			if (arr[i] >= sel[jm - 1])
			    jl = jm;
			else
			    ju = jm;
		    }
		    isel[ju - 1]++;
		}
	    }
	    j = 0;
	    while (kk >= isel[j]) {
		alo = sel[j];
		kk -= isel[j++];
	    }
	    ahi = sel[j];
	}
    }
}
