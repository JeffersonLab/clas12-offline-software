package com.nr.stat;

import org.netlib.util.doubleW;
import static java.lang.Math.*;

public class Moment {
    private Moment() {
    }

    public static void moment(final double[] data, final doubleW ave,
	    final doubleW adev, final doubleW sdev, final doubleW var,
	    final doubleW skew, final doubleW curt) {
	int j, n = data.length;
	double ep = 0.0, s, p;
	if (n <= 1)
	    throw new IllegalArgumentException("n must be at least 2 in moment");
	s = 0.0;
	for (j = 0; j < n; j++)
	    s += data[j];
	ave.val = s / n;
	adev.val = var.val = skew.val = curt.val = 0.0;
	for (j = 0; j < n; j++) {
	    // adev.val += abs(s=data[j]-ave.val);
	    s = data[j] - ave.val;
	    adev.val += abs(s);
	    ep += s;
	    var.val += (p = s * s);
	    skew.val += (p *= s);
	    curt.val += (p *= s);
	}
	adev.val /= n;
	var.val = (var.val - ep * ep / n) / (n - 1);
	sdev.val = sqrt(var.val);
	if (var.val != 0.0) {
	    skew.val /= (n * var.val * sdev.val);
	    curt.val = curt.val / (n * var.val * var.val) - 3.0;
	} else
	    throw new IllegalArgumentException(
		    "No skew/kurtosis when variance = 0 (in moment)");
    }

    public static void avevar(final double[] data, final doubleW ave,
	    final doubleW var) {
	double s, ep;
	int j, n = data.length;
	ave.val = 0.0;
	for (j = 0; j < n; j++)
	    ave.val += data[j];
	ave.val /= n;
	var.val = ep = 0.0;
	for (j = 0; j < n; j++) {
	    s = data[j] - ave.val;
	    ep += s;
	    var.val += s * s;
	}
	var.val = (var.val - ep * ep / n) / (n - 1);
    }
}
