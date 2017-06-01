package com.nr.min;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Given the vector nstate whose integer values are the number of states in each
 * stage (1 for the first and last stages), and given a function cost(j,k,i)
 * that returns the cost of moving between state j of stage i and state k of
 * stage i+1, this routine returns a vector of the same length as nstate
 * containing the state numbers of the lowest cost path. States number from 0,
 * and the first and last components of the returned vector will thus always be
 * 0.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public abstract class Dynpro {
    public Dynpro() {
    }

    public abstract double cost(int jj, int kk, int ii);

    public int[] dynpro(final int[] nstate) {
	final double BIG = 1.e99;
	final double EPS = DBL_EPSILON;
	int i, j, k, nstage = nstate.length - 1;
	double a, b;
	int[] answer = new int[nstage + 1];
	if (nstate[0] != 1 || nstate[nstage] != 1)
	    throw new IllegalArgumentException(
		    "One state allowed in first and last stages.");
	double[][] best = new double[nstage + 1][];
	best[0] = new double[nstate[0]];
	best[0][0] = 0.;
	for (i = 1; i <= nstage; i++) {
	    best[i] = new double[nstate[i]];
	    for (k = 0; k < nstate[i]; k++) {
		b = BIG;
		for (j = 0; j < nstate[i - 1]; j++) {
		    if ((a = best[i - 1][j] + cost(j, k, i - 1)) < b)
			b = a;
		}
		best[i][k] = b;
	    }
	}
	answer[nstage] = answer[0] = 0;
	for (i = nstage - 1; i > 0; i--) {
	    k = answer[i + 1];
	    b = best[i + 1][k];
	    for (j = 0; j < nstate[i]; j++) {
		double temp = best[i][j] + cost(j, k, i);
		if (abs(b - temp) <= EPS * abs(temp))
		    break;
	    }
	    answer[i] = j;
	}
	return answer;
    }
}
