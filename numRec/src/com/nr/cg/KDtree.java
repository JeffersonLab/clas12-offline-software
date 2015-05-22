package com.nr.cg;

import static com.nr.cg.Box.*;
import static com.nr.cg.Point.*;
import static com.nr.NRUtil.*;

/**
 * KD Trees and Nearest-Neighbor Finding Copyright (C) Numerical Recipes
 * Software 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class KDtree {
    static final double BIG = 1.0e99;
    final int DIM;
    public int nboxes, npts;
    public Point[] ptss;
    public Boxnode[] boxes;
    public int[] ptindx, rptindx;

    public double[] coords;

    public int dim() {
	return DIM;
    }

    public KDtree(final int dim, final Point[] pts) {
	DIM = dim;
	for (Point p : pts)
	    if (p.dim() != DIM)
		throw new IllegalArgumentException("Need same dim!");
	ptss = pts;
	npts = pts.length;
	ptindx = new int[npts];
	rptindx = new int[npts];
	int ntmp, m, k, kk, j, nowtask, jbox, np, tmom, tdim, ptlo, pthi;
	// int[] hp;
	// double[] cp;
	int[] taskmom = new int[50], taskdim = new int[50];
	for (k = 0; k < npts; k++)
	    ptindx[k] = k;
	m = 1;
	for (ntmp = npts; ntmp != 0; ntmp >>= 1) {
	    m <<= 1;
	}
	nboxes = 2 * npts - (m >> 1);
	if (m < nboxes)
	    nboxes = m;
	nboxes--;
	boxes = new Boxnode[nboxes];
	coords = new double[DIM * npts];
	for (j = 0, kk = 0; j < DIM; j++, kk += npts) {
	    for (k = 0; k < npts; k++)
		coords[kk + k] = pts[k].x[j];
	}
	Point lo = new Point(DIM, new double[] { -BIG, -BIG, -BIG });
	Point hi = new Point(DIM, new double[] { BIG, BIG, BIG });
	boxes[0] = new Boxnode(lo, hi, 0, 0, 0, 0, npts - 1);
	jbox = 0;
	taskmom[1] = 0;
	taskdim[1] = 0;
	nowtask = 1;
	while (nowtask != 0) {
	    tmom = taskmom[nowtask];
	    tdim = taskdim[nowtask--];
	    ptlo = boxes[tmom].ptlo;
	    pthi = boxes[tmom].pthi;
	    // hp = &ptindx[ptlo];
	    // cp = &coords[tdim*npts];
	    np = pthi - ptlo + 1;
	    kk = (np - 1) / 2;
	    selecti(kk, ptindx, ptlo, np, coords, tdim * npts);

	    // XXX = change to copyAssign
	    hi.copyAssign(boxes[tmom].hi);
	    lo.copyAssign(boxes[tmom].lo);

	    hi.x[tdim] = lo.x[tdim] = coords[tdim * npts + ptindx[ptlo + kk]];
	    boxes[++jbox] = new Boxnode(boxes[tmom].lo, hi, tmom, 0, 0, ptlo,
		    ptlo + kk);
	    boxes[++jbox] = new Boxnode(lo, boxes[tmom].hi, tmom, 0, 0, ptlo
		    + kk + 1, pthi);
	    boxes[tmom].dau1 = jbox - 1;
	    boxes[tmom].dau2 = jbox;
	    if (kk > 1) {
		taskmom[++nowtask] = jbox - 1;
		taskdim[nowtask] = (tdim + 1) % DIM;
	    }
	    if (np - kk > 3) {
		taskmom[++nowtask] = jbox;
		taskdim[nowtask] = (tdim + 1) % DIM;
	    }
	}
	for (j = 0; j < npts; j++)
	    rptindx[ptindx[j]] = j;
    }

    public double disti(final int jpt, final int kpt) {
	if (jpt == kpt)
	    return BIG;
	else
	    return dist(ptss[jpt], ptss[kpt]);
    }

    public int locate(final Point pt) {
	if (pt.dim() != DIM)
	    throw new IllegalArgumentException("Need same dim!");
	int nb, d1, jdim;
	nb = jdim = 0;
	while (boxes[nb].dau1 != 0) {
	    d1 = boxes[nb].dau1;
	    if (pt.x[jdim] <= boxes[d1].hi.x[jdim])
		nb = d1;
	    else
		nb = boxes[nb].dau2;
	    jdim = ++jdim % DIM;
	}
	return nb;
    }

    public int locate(final int jpt) {
	int nb, d1, jh;
	jh = rptindx[jpt];
	nb = 0;
	while (boxes[nb].dau1 != 0) {
	    d1 = boxes[nb].dau1;
	    if (jh <= boxes[d1].pthi)
		nb = d1;
	    else
		nb = boxes[nb].dau2;
	}
	return nb;
    }

    public int nearest(final Point pt) {
	if (pt.dim() != DIM)
	    throw new IllegalArgumentException("Need same dim!");
	int i, k, nrst = 0, ntask;
	int[] task = new int[50];
	double dnrst = BIG, d;
	k = locate(pt);
	for (i = boxes[k].ptlo; i <= boxes[k].pthi; i++) {
	    d = dist(ptss[ptindx[i]], pt);
	    if (d < dnrst) {
		nrst = ptindx[i];
		dnrst = d;
	    }
	}
	task[1] = 0;
	ntask = 1;
	while (ntask != 0) {
	    k = task[ntask--];
	    if (dist(boxes[k], pt) < dnrst) {
		if (boxes[k].dau1 != 0) {
		    task[++ntask] = boxes[k].dau1;
		    task[++ntask] = boxes[k].dau2;
		} else {
		    for (i = boxes[k].ptlo; i <= boxes[k].pthi; i++) {
			d = dist(ptss[ptindx[i]], pt);
			if (d < dnrst) {
			    nrst = ptindx[i];
			    dnrst = d;
			}
		    }
		}
	    }
	}
	return nrst;
    }

    public void nnearest(final int jpt, final int[] nn, final double[] dn,
	    final int n) {
	int i, k, ntask, kp;
	int[] task = new int[50];
	double d;
	if (n > npts - 1)
	    throw new IllegalArgumentException("too many neighbors requested");
	for (i = 0; i < n; i++)
	    dn[i] = BIG;
	kp = boxes[locate(jpt)].mom;
	while (boxes[kp].pthi - boxes[kp].ptlo < n)
	    kp = boxes[kp].mom;
	for (i = boxes[kp].ptlo; i <= boxes[kp].pthi; i++) {
	    if (jpt == ptindx[i])
		continue;
	    d = disti(ptindx[i], jpt);
	    if (d < dn[0]) {
		dn[0] = d;
		nn[0] = ptindx[i];
		if (n > 1)
		    sift_down(dn, nn, n);
	    }
	}
	task[1] = 0;
	ntask = 1;
	while (ntask != 0) {
	    k = task[ntask--];
	    if (k == kp)
		continue;
	    if (dist(boxes[k], ptss[jpt]) < dn[0]) {
		if (boxes[k].dau1 != 0) {
		    task[++ntask] = boxes[k].dau1;
		    task[++ntask] = boxes[k].dau2;
		} else {
		    for (i = boxes[k].ptlo; i <= boxes[k].pthi; i++) {
			d = disti(ptindx[i], jpt);
			if (d < dn[0]) {
			    dn[0] = d;
			    nn[0] = ptindx[i];
			    if (n > 1)
				sift_down(dn, nn, n);
			}
		    }
		}
	    }
	}
	return;
    }

    public void sift_down(final double[] heap, final int[] ndx, final int nn) {
	int n = nn - 1;
	int j, jold, ia;
	double a;
	a = heap[0];
	ia = ndx[0];
	jold = 0;
	j = 1;
	while (j <= n) {
	    if (j < n && heap[j] < heap[j + 1])
		j++;
	    if (a >= heap[j])
		break;
	    heap[jold] = heap[j];
	    ndx[jold] = ndx[j];
	    jold = j;
	    j = 2 * j + 1;
	}
	heap[jold] = a;
	ndx[jold] = ia;
    }

    public int locatenear(final Point pt, final double r, final int[] list,
	    final int nmax) {
	if (pt.dim() != DIM)
	    throw new IllegalArgumentException("Need same dim!");
	int k, i, nb, nbold, nret, ntask, jdim, d1, d2;
	int[] task = new int[50];
	nb = jdim = nret = 0;
	if (r < 0.0)
	    throw new IllegalArgumentException("radius must be nonnegative");
	while (boxes[nb].dau1 != 0) {
	    nbold = nb;
	    d1 = boxes[nb].dau1;
	    d2 = boxes[nb].dau2;
	    if (pt.x[jdim] + r <= boxes[d1].hi.x[jdim])
		nb = d1;
	    else if (pt.x[jdim] - r >= boxes[d2].lo.x[jdim])
		nb = d2;
	    jdim = ++jdim % DIM;
	    if (nb == nbold)
		break;
	}
	task[1] = nb;
	ntask = 1;
	while (ntask != 0) {
	    k = task[ntask--];
	    if (dist(boxes[k], pt) > r)
		continue;
	    if (boxes[k].dau1 != 0) {
		task[++ntask] = boxes[k].dau1;
		task[++ntask] = boxes[k].dau2;
	    } else {
		for (i = boxes[k].ptlo; i <= boxes[k].pthi; i++) {
		    if (dist(ptss[ptindx[i]], pt) <= r && nret < nmax)
			list[nret++] = ptindx[i];
		    if (nret == nmax)
			return nmax;
		}
	    }
	}
	return nret;
    }

    public static int selecti(final int k, final int[] indx, final int off_i,
	    final int n, final double[] arr, final int off_a) {
	int i, ia, ir, j, l, mid;
	double a;

	l = 0;
	ir = n - 1;
	for (;;) {
	    if (ir <= l + 1) {
		if (ir == l + 1
			&& arr[off_a + indx[off_i + ir]] < arr[off_a
				+ indx[off_i + l]]) {
		    swap(indx, off_i + l, off_i + ir);
		}
		return indx[off_i + k];
	    } else {
		mid = (l + ir) >> 1;
		swap(indx, off_i + mid, off_i + l + 1);
		if (arr[off_a + indx[off_i + l]] > arr[off_a + indx[off_i + ir]]) {
		    swap(indx, off_i + l, off_i + ir);
		}
		if (arr[off_a + indx[off_i + l + 1]] > arr[off_a
			+ indx[off_i + ir]]) {
		    swap(indx, off_i + l + 1, off_i + ir);
		}
		if (arr[off_a + indx[off_i + l]] > arr[off_a
			+ indx[off_i + l + 1]]) {
		    swap(indx, off_i + l, off_i + l + 1);
		}
		i = l + 1;
		j = ir;
		ia = indx[off_i + l + 1];
		a = arr[off_a + ia];
		for (;;) {
		    do
			i++;
		    while (arr[off_a + indx[off_i + i]] < a);
		    do
			j--;
		    while (arr[off_a + indx[off_i + j]] > a);
		    if (j < i)
			break;
		    swap(indx, off_i + i, off_i + j);
		}
		indx[off_i + l + 1] = indx[off_i + j];
		indx[off_i + j] = ia;
		if (j >= k)
		    ir = j - 1;
		if (j <= k)
		    l = i;
	    }
	}
    }
}
