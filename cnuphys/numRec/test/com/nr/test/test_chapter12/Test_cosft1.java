package com.nr.test.test_chapter12;

import static com.nr.fft.FFT.cosft1;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.ranvec;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_cosft1 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=256;
    double sbeps=1.e-13,pi=acos(-1.0);
    double[] data1 = new double[N+1],data2 = new double[N+1];
    boolean localflag=false, globalflag=false;

    

    // Test cosft1
    System.out.println("Testing cosft1");

    // Round-trip test for random numbers
    ranvec(data1);
    for (i=0;i<N+1;i++) data2[i] = (2.0/N)*data1[i];
    cosft1(data2);
    cosft1(data2);
//    System.out.printf(maxel(vecsub(data1,data2)));
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cosft1: Round-trip test for random real values failed");
      
    }

    // Test delta-function in to sine-wave out, forward transform
    for (i=0;i<N+1;i++) data1[i]=0.0;
    data1[5]=1.0;
    cosft1(data1);
    for (i=0;i<N+1;i++) data2[i]=cos(pi*5*i/N);
//    System.out.printf(maxel(vecsub(data1,data2)));
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cosft1: Forward transform of a chosen delta function did not give expected result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
