package com.nr.ran;

public abstract class Hashtable<K> {
    int nhash, nmax, nn, ng;
    int[] htable, next, garbg;
    long[] thehash;

    public Hashtable(final int nh, final int nm) {
	nhash = nh;
	nmax = nm;
	nn = 0;
	ng = 0;
	htable = new int[nh];
	next = new int[nm];
	garbg = new int[nm];
	thehash = new long[nm];
	for (int j = 0; j < nh; j++) {
	    htable[j] = -1;
	}
    }

    public abstract long fn(K k);

    public int iget(final K key) {
	int j, k;
	long pp = fn(key);
	j = (int) (pp % nhash);
	for (k = htable[j]; k != -1; k = next[k]) {
	    if (thehash[k] == pp) {
		return k;
	    }
	}
	return -1;
    }

    public int iset(final K key) {
	int j, k, kprev = 0;
	long pp = fn(key);
	j = (int) (pp % nhash);
	if (htable[j] == -1) {
	    k = ng != 0 ? garbg[--ng] : nn++;
	    htable[j] = k;
	} else {
	    for (k = htable[j]; k != -1; k = next[k]) {
		if (thehash[k] == pp) {
		    return k;
		}
		kprev = k;
	    }
	    k = ng != 0 ? garbg[--ng] : nn++;
	    next[kprev] = k;
	}
	if (k >= nmax)
	    throw new IllegalArgumentException("storing too many values");
	thehash[k] = pp;
	next[k] = -1;
	return k;
    }

    public int ierase(final K key) {
	int j, k, kprev;
	long pp = fn(key);
	j = (int) (pp % nhash);
	if (htable[j] == -1)
	    return -1;
	kprev = -1;
	for (k = htable[j]; k != -1; k = next[k]) {
	    if (thehash[k] == pp) {
		if (kprev == -1)
		    htable[j] = next[k];
		else
		    next[kprev] = next[k];
		garbg[ng++] = k;
		return k;
	    }
	    kprev = k;
	}
	return -1;
    }

    public int ireserve() {
	int k = ng != 0 ? garbg[--ng] : nn++;
	if (k >= nmax)
	    throw new IllegalArgumentException("reserving too many values");
	next[k] = -2;
	return k;
    }

    public int irelinquish(final int k) {
	if (next[k] != -2) {
	    return -1;
	}
	garbg[ng++] = k;
	return k;
    }
}
