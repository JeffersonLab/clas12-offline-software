package com.nr.ran;

import java.util.Vector;

public abstract class Hash<K, V> extends Hashtable<K> {
    Vector<V> els;

    public Hash(final int nh, final int nm) {
	super(nh, nm);
	els = new Vector<V>(nm);

	// init Vector space.
	for (int i = 0; i < nm; i++)
	    els.add(null);
    }

    public void set(final K key, final V el) {
	els.set(iset(key), el);
    }

    public int get(final K key, final V[] el, final int on) {
	int ll = iget(key);
	if (ll < 0)
	    return 0;
	el[on] = els.get(ll);
	return 1;
    }

    public V get(final K key) {
	int ll = iget(key);
	if (ll < 0)
	    return null;
	return els.get(ll);
    }

    public int count(final K key) {
	int ll = iget(key);
	return (ll < 0 ? 0 : 1);
    }

    public int erase(final K key) {
	return (ierase(key) < 0 ? 0 : 1);
    }
}
