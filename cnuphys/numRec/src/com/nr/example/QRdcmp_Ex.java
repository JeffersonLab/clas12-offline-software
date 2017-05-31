/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <hwh@gddsn.org.cn> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return. Huang Wen Hui
 * ----------------------------------------------------------------------------
 *
 */

package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.QRdcmp;

public class QRdcmp_Ex {

    public static void main(String[] args) {
	double[] a = new double[] { 1.0, 0.5, 1.0, 0.5, 2.2, -1.0, 1.0, -1.0,
		5.0 };
	double[][] aa = NRUtil.buildMatrix(3, 3, a);
	double[] b = new double[] { 2.0, 4.0, 1.0 };
	double[] x = new double[3];

	QRdcmp qr = new QRdcmp(aa);
	qr.solve(b, x);
	System.out.println(NRUtil.toString(x));
    }
}