package com.nr.ran;

import java.util.Vector;

public abstract class Mhash<K, V> extends Hashtable<K> {
    private Vector<V> els;
    /**
     * Links to next sister element under a single key.
     */
    private int[] nextsis;

    private int nextget;

    public Mhash(final int nh, final int nm) {
	super(nh, nm);
	nextget = -1;
	els = new Vector<V>(nm);

	// init Vector space.
	for (int i = 0; i < nm; i++)
	    els.add(null);
	nextsis = new int[nm];
	for (int j = 0; j < nm; j++) {
	    nextsis[j] = -2;
	} // Initialize to "empty".
    }

    /**
     * Store an element el under key. Return index in 0..nmax-1, giving the
     * storage location utilized.
     * 
     * @param key
     * @param el
     * @return
     */
    public int store(final K key, final V el) {
	int j, k;
	j = iset(key);
	if (nextsis[j] == -2) {
	    els.set(j, el);
	    nextsis[j] = -1;
	    return j;
	} else {
	    while (nextsis[j] != -1) {
		j = nextsis[j];
	    }
	    k = ireserve();
	    els.set(k, el);
	    nextsis[j] = k;
	    nextsis[k] = -1;
	    return k;
	}
    }

    /**
     * Erase an element el previously stored under key. Return 1 for success, or
     * 0 if no matching element is found. Note: The == operation must be defined
     * for the type elT.
     * 
     * @param key
     * @param el
     * @return
     */
    public int erase(final K key, final V el) {
	int j = -1, kp = -1, kpp = -1;
	int k = iget(key);
	while (k >= 0) {
	    if (j < 0 && el.equals(els.get(k)))
		j = k;
	    kpp = kp;
	    kp = k;
	    k = nextsis[k];
	}
	if (j < 0)
	    return 0;
	if (kpp < 0) {
	    ierase(key);
	    nextsis[j] = -2;
	} else {
	    if (j != kp)
		els.set(j, els.get(kp));
	    nextsis[kpp] = -1;
	    irelinquish(kp);
	    nextsis[kp] = -2;
	}
	return 1;
    }

    /**
     * Return the number of elements stored under key, 0 if none.
     * 
     * @param key
     * @return
     */
    public int count(final K key) {
	int next, n = 1;
	if ((next = iget(key)) < 0)
	    return 0;
	while ((next = nextsis[next]) >= 0) {
	    n++;
	}
	return n;
    }

    /**
     * Initialize nextget so that it points to the first element stored under
     * key. Return 1 for success, or 0 if no such element.
     * 
     * @param key
     * @return
     */
    public int getinit(final K key) {
	nextget = iget(key);
	return ((nextget < 0) ? 0 : 1);
    }

    /**
     * If nextget points validly, copy its element into el, update nextget to
     * the next element with the same key, and return 1. Otherwise, do not
     * modify el, and return 0.
     * 
     * @param el
     * @param on
     * @return
     */
    public int getnext(final V[] el, final int on) {
	if (nextget < 0) {
	    return 0;
	}
	el[on] = els.get(nextget);
	nextget = nextsis[nextget];
	return 1;
    }
}
