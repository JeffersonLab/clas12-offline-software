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

import com.nr.sp.Hann;
import com.nr.sp.Spectolap;

public class Test_Spectolap {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,jj,M=128,N=2*M,K=64;
    double freq1,freq2,flo=0,fhi,sbeps;
    double sum1,sum2,target,pi=acos(-1.0);
    double[] data=new double[M],spec=new double[M+1],freq=new double[M+1];
    boolean localflag, globalflag=false;

    

    // Test Spectolap
    System.out.println("Testing Spectolap");

    Spectolap sp=new Spectolap(M);
    Hann hann=new Hann(N);
    for (i=0;i<K;i++) {
      // Generate a data set
      for (j=0;j<M;j++) {
        jj=M*(i%2)+j;
        data[j]=10.0*exp(-1.*SQR(jj-M)/SQR(N/8))*
          (cos(2.0*pi*32*jj/N)+cos(2.0*pi*64*jj/N));
      }
      sp.adddataseg(data,hann);
    }
    spec=sp.spectrum();
    freq=sp.frequencies();
//    for (i=0;i<M+1;i++) 
//      System.out.printf(setw(20) << freq[i] << " " << setw(20) << spec[i]);

    freq1=32.0/N;
    freq2=64.0/N;

    j=0;
    for (i=0;i<M+1;i++) {
      if (spec[i] > 1.e-3 && j == 0) {
        flo=freq[i];
        j=1;
      }
      if (spec[i] < 1.e-3 && j == 1) {
        fhi=freq[i-1];
        j=0;

        sum1=0.0;
        sum2=0.0;
        for (k=0;k<M+1;k++) {
          if (freq[k]>=flo && freq[k]<=fhi) {
            sum1 += freq[k]*spec[k];
            sum2 += spec[k];
          }
        }

        target = (abs(sum1/sum2-freq1) < abs(sum1/sum2-freq2) ? freq1 : freq2);

        sbeps=1.e-6;

        localflag = abs((sum1/sum2)/target-1.0) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Spectolap: Incorrect identification of one of the frequencies");
          
        }
      }
    }

    // Test addlongdata() method
    int ntot=2*K*M+77;
    double[] data2=new double[ntot];
    Spectolap sp2=new Spectolap(M);
    // Generate a data set
    for (j=0;j<ntot;j++) {
      jj=j % N;
      data2[j]=10.0*exp(-1.*SQR(jj-M)/SQR(N/8))*
        (cos(2.0*pi*32*jj/N)+cos(2.0*pi*64*jj/N));
    }
    sp2.addlongdata(data2,hann);
    spec=sp2.spectrum();
    freq=sp2.frequencies();
//    for (i=0;i<M+1;i++) 
//      System.out.printf(setw(20) << freq[i] << " " << setw(20) << spec[i]);

    j=0;
    for (i=0;i<M+1;i++) {
      if (spec[i] > 1.e-3 && j == 0) {
        flo=freq[i];
        j=1;
      }
      if (spec[i] < 1.e-3 && j == 1) {
        fhi=freq[i-1];
        j=0;

        sum1=0.0;
        sum2=0.0;
        for (k=0;k<M+1;k++) {
          if (freq[k]>=flo && freq[k]<=fhi) {
            sum1 += freq[k]*spec[k];
            sum2 += spec[k];
          }
        }

        target = (abs(sum1/sum2-freq1) < abs(sum1/sum2-freq2) ? freq1 : freq2);

        sbeps=1.e-6;

        localflag = abs((sum1/sum2)/target-1.0) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Spectolap: Incorrect identification of one of the frequencies");
          
        }
      }
    }
    
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
