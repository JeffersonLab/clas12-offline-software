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

import com.nr.Complex;
import com.nr.fft.FFT;

public class FFT_Ex {

    public static void main(String[] args) {
	Complex[] vc = new Complex[8];
	vc[0] = new Complex(1, 10.1);
	vc[1] = new Complex(11, 1.1);
	vc[2] = new Complex(12, 12.1);
	vc[3] = new Complex(13, 17.15);
	vc[4] = new Complex(1.3, 1.13);
	vc[5] = new Complex(1.4, 1.21);
	vc[6] = new Complex(1.7, 11.1);
	vc[7] = new Complex(2.0, 12.1);
	FFT.four1(vc, 1);
	System.out.println(vc[0]);
	System.out.println(vc[1]);
	System.out.println(vc[4]);
	System.out.println(vc[7]);
	System.out.println();

	double[] a = { 1, 10.1, 11, 1.1, 12, 12.1, 13, 17.15, 1.3, 1.13, 1.4,
		1.21, 1.7, 11.1, 2.0, 12.1 };
	FFT.four1(a, 8, 1);
	System.out.println(a[8 + 0]);
	System.out.println(a[8 + 1]);
	System.out.println(a[8 + 4]);
	System.out.println(a[8 + 7]);

    }

}