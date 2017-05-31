package com.nr.test.test_chapter12;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static java.lang.Math.*;
import static com.nr.fft.FFT.*;

public class Test_realft {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=256;
    double sbeps=1.e-14,pi=acos(-1.0);
    double[] data1=new double[N],data2=new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test realft
    System.out.println("Testing realft");

    // Round-trip test for random numbers
    ranvec(data1);
    data2=data1;
    for (i=0;i<N;i++) data2[i] *= (2.0/N);
    realft(data2,1);
    realft(data2,-1);
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** realft: Round-trip test for random real values failed");
      
    }

    // Test delta-function in to sine-wave out, forward transform
    for (i=0;i<N;i++) data1[i]=0.0;
    data1[5]=1.0;
    realft(data1,1);
    data2[0]=1.0;
    data2[1]=cos(pi*5);
    for (i=1;i<N/2;i++) {
      data2[2*i]=cos(2.0*pi*5*i/N);
      data2[2*i+1]=sin(2.0*pi*5*i/N);
//      System.out.printf(setw(15) << data1[2*i] << " " << setw(15) << data1[2*i+1] << " " << setw(15) << data2[2*i] << " " << setw(15) << data2[2*i+1]);
    }
//    System.out.printf(maxel(vecsub(data1,data2)));
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** realft: Forward transform of a chosen delta function did not give expected result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
