package com.nr.sort;

import static com.nr.NRUtil.*;
import static java.lang.Math.min;

public class Heapselect {
    int m, n, srtd;
    double[] heap;

    public Heapselect(int mm) {
	m = mm;
	n = 0;
	srtd = 0;
	heap = buildVector(mm, 1.e99);
    }

    public void add(double val) {
	int j, k;
	if (n < m) {
	    heap[n++] = val;
	    if (n == m)
		Sorter.sort(heap);
	} else {
	    if (val > heap[0]) {
		heap[0] = val;
		for (j = 0;;) {
		    k = (j << 1) + 1;
		    if (k > m - 1)
			break;
		    if (k != (m - 1) && heap[k] > heap[k + 1])
			k++;
		    if (heap[j] <= heap[k])
			break;
		    swap(heap, k, j);
		    j = k;
		}
	    }
	    n++;
	}
	srtd = 0;
    }

    public double report(int k) {
	int mm = min(n, m);
	if (k > mm - 1)
	    throw new IllegalArgumentException("Heapselect k too big");
	if (k == m - 1)
	    return heap[0];
	if (srtd == 0) {
	    Sorter.sort(heap);
	    srtd = 1;
	}
	return heap[mm - 1 - k];
    }
}
