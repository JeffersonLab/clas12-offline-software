package com.nr.ran;

public class Primpolytest {
    int N, nfactors;
    long[] factors;
    int[] t, a, p;

    public Primpolytest() {
	N = 32;
	nfactors = 5;
	factors = new long[nfactors];
	t = new int[N * N];
	a = new int[N * N];
	p = new int[N * N];
	long[] factordata = { 3, 5, 17, 257, 65537 };
	for (int i = 0; i < nfactors; i++)
	    factors[i] = factordata[i];
    }

    public int ispident() {
	int i, j;
	for (i = 0; i < N; i++)
	    for (j = 0; j < N; j++) {
		if (i == j) {
		    if (p[i * N + j] != 1)
			return 0;
		} else {
		    if (p[i * N + j] != 0)
			return 0;
		}
	    }
	return 1;
    }

    public void mattimeseq(final int[] a, final int[] b) {
	int i, j, k, sum;
	int[] tmp = new int[N * N];
	for (i = 0; i < N; i++)
	    for (j = 0; j < N; j++) {
		sum = 0;
		for (k = 0; k < N; k++)
		    sum += a[i * N + k] * b[k * N + j];
		tmp[i * N + j] = sum & 1;
	    }
	for (k = 0; k < N * N; k++)
	    a[k] = tmp[k];
    }

    public void matpow(long n) {
	int k;
	for (k = 0; k < N * N; k++)
	    p[k] = 0;
	for (k = 0; k < N; k++)
	    p[k * N + k] = 1;
	while (true) {
	    if ((n & 1) != 0)
		mattimeseq(p, a);
	    n >>>= 1;
	    if (n == 0)
		break;
	    mattimeseq(a, a);
	}
    }

    public int test(final long n) {
	int i, k, j;
	long pow, tnm1, nn = n;
	tnm1 = ((long) 1 << N) - 1;
	if (n > (tnm1 >>> 1))
	    throw new IllegalArgumentException("not a polynomial of degree N");
	for (k = 0; k < N * N; k++)
	    t[k] = 0;
	for (i = 1; i < N; i++)
	    t[i * N + (i - 1)] = 1;
	j = 0;
	while (nn != 0) {
	    if ((nn & 1) != 0)
		t[j] = 1;
	    nn >>>= 1;
	    j++;
	}
	t[N - 1] = 1;
	for (k = 0; k < N * N; k++)
	    a[k] = t[k];
	matpow(tnm1);
	if (ispident() != 1)
	    return 0;
	for (i = 0; i < nfactors; i++) {
	    pow = tnm1 / factors[i];
	    for (k = 0; k < N * N; k++)
		a[k] = t[k];
	    matpow(pow);
	    if (ispident() == 1)
		return 0;
	}
	return 1;
    }
}
