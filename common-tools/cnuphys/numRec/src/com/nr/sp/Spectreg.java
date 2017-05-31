package com.nr.sp;

import static com.nr.NRUtil.*;
import com.nr.fft.FFT;

public class Spectreg {
    int m, m2, nsum;
    double[] specsum, wksp;

    public Spectreg(final int em) {
	m = em;
	m2 = 2 * m;
	nsum = 0;
	specsum = new double[m + 1];
	wksp = new double[m2];

	if ((m & (m - 1)) != 0)
	    throw new IllegalArgumentException("m must be power of 2");
    }

    public void adddataseg(final double[] data, final WindowFun wf) {
	int i;
	double w, fac, sumw = 0.;
	if (data.length != m2)
	    throw new IllegalArgumentException("wrong size data segment");
	for (i = 0; i < m2; i++) {
	    w = wf.window(i, m2);
	    wksp[i] = w * data[i];
	    sumw += SQR(w);
	}
	fac = 2. / (sumw * m2);
	FFT.realft(wksp, 1);
	specsum[0] += 0.5 * fac * SQR(wksp[0]);
	for (i = 1; i < m; i++)
	    specsum[i] += fac * (SQR(wksp[2 * i]) + SQR(wksp[2 * i + 1]));
	specsum[m] += 0.5 * fac * SQR(wksp[1]);
	nsum++;
    }

    public double[] spectrum() {
	double[] spec = new double[m + 1];
	if (nsum == 0)
	    throw new IllegalArgumentException("no data yet");
	for (int i = 0; i <= m; i++)
	    spec[i] = specsum[i] / nsum;
	return spec;
    }

    public double[] frequencies() {
	double[] freq = new double[m + 1];
	for (int i = 0; i <= m; i++)
	    freq[i] = i * 0.5 / m;
	return freq;
    }
}
