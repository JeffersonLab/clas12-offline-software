package com.nr.sort;

import static com.nr.NRUtil.*;

public class Indexx {
    private int n;

    public int[] indx;

    public Indexx(final double[] arr) {
	index(arr, arr.length);
    }

    public Indexx() {
    }

    public void sort(final double[] brr) {
	if (brr.length != n)
	    throw new IllegalArgumentException("bad size in Index sort");
	double[] tmp = buildVector(brr);
	for (int j = 0; j < n; j++)
	    brr[j] = tmp[indx[j]];
    }

    public void sort(final int[] brr) {
	if (brr.length != n)
	    throw new IllegalArgumentException("bad size in Index sort");
	int[] tmp = buildVector(brr);
	for (int j = 0; j < n; j++)
	    brr[j] = tmp[indx[j]];
    }

    public int el(final int[] brr, final int j) {
	return brr[indx[j]];
    }

    public double el(final double[] brr, final int j) {
	return brr[indx[j]];
    }

    public void setEl(final int[] brr, final int j, final int v) {
	brr[indx[j]] = v;
    }

    public void setEl(final double[] brr, final int j, final double v) {
	brr[indx[j]] = v;
    }

    public void rank(final int[] irank) {
	for (int j = 0; j < n; j++)
	    irank[indx[j]] = j;
    }

    public void index(final double[] arr, final int nn) {
	final int M = 7, NSTACK = 64;
	int i, indxt, ir, j, k, jstack = -1, l = 0;
	double a;
	int[] istack = new int[NSTACK];
	n = nn;
	indx = new int[n];
	ir = n - 1;
	for (j = 0; j < n; j++)
	    indx[j] = j;
	for (;;) {
	    if (ir - l < M) {
		for (j = l + 1; j <= ir; j++) {
		    indxt = indx[j];
		    a = arr[indxt];
		    for (i = j - 1; i >= l; i--) {
			if (arr[indx[i]] <= a)
			    break;
			indx[i + 1] = indx[i];
		    }
		    indx[i + 1] = indxt;
		}
		if (jstack < 0)
		    break;
		ir = istack[jstack--];
		l = istack[jstack--];
	    } else {
		k = (l + ir) >> 1;
		swap(indx, k, l + 1);
		if (arr[indx[l]] > arr[indx[ir]]) {
		    swap(indx, l, ir);
		}
		if (arr[indx[l + 1]] > arr[indx[ir]]) {
		    swap(indx, l + 1, ir);
		}
		if (arr[indx[l]] > arr[indx[l + 1]]) {
		    swap(indx, l, l + 1);
		}
		i = l + 1;
		j = ir;
		indxt = indx[l + 1];
		a = arr[indxt];
		for (;;) {
		    do
			i++;
		    while (arr[indx[i]] < a);
		    do
			j--;
		    while (arr[indx[j]] > a);
		    if (j < i)
			break;
		    swap(indx, i, j);
		}
		indx[l + 1] = indx[j];
		indx[j] = indxt;
		jstack += 2;
		if (jstack >= NSTACK)
		    throw new IllegalArgumentException(
			    "NSTACK too small in index.");
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
}
