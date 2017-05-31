package com.nr.ran;

public class Hashfn1 {
    Ranhash hasher = new Ranhash();

    public Hashfn1(final int nn) {
    }

    public long fn(final int key) {
	return hasher.int64p(key);
    }

    public long fn(final long key) {
	return hasher.int64p(key);
    }
}
