package com.nr.model;

import static java.lang.Math.*;

import org.netlib.util.doubleW;

import com.nr.ran.Normaldev;

public class Proposal {
    Normaldev gau;
    double logstep;

    public Proposal(final int ranseed, final double lstep) {
	gau = new Normaldev(0., 1., ranseed);
	logstep = lstep;
    }

    public void get(final State s1, final State s2, final doubleW qratio) {
	double r = gau.doub();
	if (r < 0.9) {
	    s2.lam1 = s1.lam1 * exp(logstep * gau.dev());
	    s2.lam2 = s1.lam2 * exp(logstep * gau.dev());
	    s2.tc = s1.tc * exp(logstep * gau.dev());
	    s2.k1 = s1.k1;
	    s2.k2 = s1.k2;
	    qratio.val = (s2.lam1 / s1.lam1) * (s2.lam2 / s1.lam2)
		    * (s2.tc / s1.tc);
	} else {
	    r = gau.doub();
	    if (s1.k1 > 1) {
		if (r < 0.5)
		    s2.k1 = s1.k1;
		else if (r < 0.75)
		    s2.k1 = s1.k1 + 1;
		else
		    s2.k1 = s1.k1 - 1;
	    } else {
		if (r < 0.75)
		    s2.k1 = s1.k1;
		else
		    s2.k1 = s1.k1 + 1;
	    }
	    s2.lam1 = s2.k1 * s1.lam1 / s1.k1;
	    r = gau.doub();
	    if (s1.k2 > 1) {
		if (r < 0.5)
		    s2.k2 = s1.k2;
		else if (r < 0.75)
		    s2.k2 = s1.k2 + 1;
		else
		    s2.k2 = s1.k2 - 1;
	    } else {
		if (r < 0.75)
		    s2.k2 = s1.k2;
		else
		    s2.k2 = s1.k2 + 1;
	    }
	    s2.lam2 = s2.k2 * s1.lam2 / s1.k2;
	    s2.tc = s1.tc;
	    qratio.val = 1.;
	}
    }

    public static double mcmcstep(final int m, final State s, final Plog plog,
	    final Proposal propose) {
	State sprop = new State();
	double alph, ran;
	doubleW qratio = new doubleW(0);
	int accept = 0;
	plog.get(s);
	for (int i = 0; i < m; i++) {
	    propose.get(s, sprop, qratio);
	    alph = min(1., qratio.val * exp(plog.get(sprop) - s.plog));
	    ran = propose.gau.doub();
	    if (ran < alph) {
		State.copy(sprop, s);
		plog.get(s);
		accept++;
	    }
	}
	return accept / (double) (m);
    }
}
