package com.nr.cg;

public class Boxnode extends Box {
    public int mom, dau1, dau2, ptlo, pthi;

    public Boxnode(int dim) {
	super(dim);
    }

    public Boxnode(final Point lo, final Point hi, final int mom,
	    final int dau1, final int dau2, final int ptlo, final int pthi) {
	super(lo, hi);
	this.mom = mom;
	this.dau1 = dau1;
	this.dau2 = dau2;
	this.ptlo = ptlo;
	this.pthi = pthi;
    }

    @Override
    public Boxnode clone() {
	return new Boxnode(this.lo.clone(), this.hi.clone(), mom, dau1, dau2,
		ptlo, pthi);
    }
}
