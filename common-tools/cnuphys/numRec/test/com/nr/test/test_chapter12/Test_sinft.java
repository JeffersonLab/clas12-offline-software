package com.nr.test.test_chapter12;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static java.lang.Math.*;
import static com.nr.fft.FFT.*;

public class Test_sinft {

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
    double[] data1= new double[N],data2= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test sinft
    System.out.println("Testing sinft");

    // Round-trip test for random numbers
    ranvec(data1);
    data1[0]=0.0;
    for (i=0;i<N;i++) data2[i] = (2.0/N)*data1[i];
    sinft(data2);
    sinft(data2);
//    System.out.printf(maxel(vecsub(data1,data2)));
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sinft: Round-trip test for random real values failed");
      
    }

    // Test delta-function in to sine-wave out, forward transform
    for (i=0;i<N;i++) data1[i]=0.0;
    data1[5]=1.0;
    sinft(data1);
    for (i=1;i<N;i++) {
      data2[i]=sin(pi*5*i/N);
    }
//    System.out.printf(maxel(vecsub(data1,data2)));
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sinft: Forward transform of a chosen delta function did not give expected result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
