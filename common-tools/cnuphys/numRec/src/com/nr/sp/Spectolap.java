package com.nr.sp;

public class Spectolap extends Spectreg {

    int first;
    double[] fullseg;

    public Spectolap(final int em) {
	super(em);
	first = 1;
	fullseg = new double[2 * em];
    }

    @Override
    public void adddataseg(final double[] data, final WindowFun wf) {
	int i;
	if (data.length != m)
	    throw new IllegalArgumentException("wrong size data segment");
	if (first != 0) {
	    for (i = 0; i < m; i++)
		fullseg[i + m] = data[i];
	    first = 0;
	} else {
	    for (i = 0; i < m; i++) {
		fullseg[i] = fullseg[i + m];
		fullseg[i + m] = data[i];
	    }
	    super.adddataseg(fullseg, wf);
	}
    }

    public void addlongdata(final double[] data, final WindowFun wf) {
	int i, k, noff, nt = data.length, nk = (nt - 1) / m;
	double del = nk > 1 ? (nt - m2) / (nk - 1.) : 0.;
	if (nt < m2)
	    throw new IllegalArgumentException("data length too short");
	for (k = 0; k < nk; k++) {
	    noff = (int) (k * del + 0.5);
	    for (i = 0; i < m2; i++)
		fullseg[i] = data[noff + i];
	    super.adddataseg(fullseg, wf);
	}
    }
}
