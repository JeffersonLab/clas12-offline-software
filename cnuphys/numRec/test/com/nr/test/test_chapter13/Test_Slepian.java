package com.nr.test.test_chapter13;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sp.Slepian;

public class Test_Slepian {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double freq1,freq2,flo=0,fhi,sum1,sum2,target,sbeps,pi=acos(-1.0);
    int i,j,k,M=128,N=2*M,K=64;
    double[] data=new double[N],spec=new double[M+1],freq=new double[M+1];
    boolean localflag, globalflag=false;

    

    // Test Slepian
    System.out.println("Testing Slepian");

    Slepian slep=new Slepian(M,3,1);
    for (i=0;i<K;i++) {
      // Generate a data set
      for (j=0;j<N;j++)
        data[j]=10.0*exp(-1.*SQR(j-M)/SQR(N/8))*
          (cos(2.0*pi*32*j/N)+cos(2.0*pi*64*j/N));
      slep.adddataseg(data);    // Note: text should clarify this usage
    }
    spec=slep.spectrum();
    freq=slep.frequencies();

    freq1=32.0/N;
    freq2=64.0/N;

//    System.out.printf(setprecision(6);
//    for (i=0;i<M+1;i++) 
//      System.out.printf(setw(20) << freq[i] << " " << setw(20) << spec[i]);

    j=0;
    for (i=0;i<M+1;i++) {
      if (spec[i] > 1.e-3 && j==0) {
        flo=freq[i];
        j=1;
      }
      if (spec[i] < 1.e-3 && j==1) {
        fhi=freq[i];
        j=0;

        sum1=0.0;
        sum2=0.0;
        for (k=0;k<M+1;k++) {
          if (freq[k] >= flo && freq[k] <= fhi) {
            sum1 += freq[k]*spec[k];
            sum2 += spec[k];
          }
        }

        target = (abs(sum1/sum2-freq1) < abs(sum1/sum2-freq2) ? freq1 : freq2);
//        System.out.printf(setprecision(15);
//        System.out.printf(sum1/sum2 << " " << target);

        sbeps=1.e-6;
        localflag=abs((sum1/sum2)/target-1.0) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Slepian: Incorrect identification of one of the frequencies");
          
        }
      }
    }

    // Test filltable() method
    // Table is dpss[][]

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
