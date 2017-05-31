package com.nr.test.test_chapter12;

import static com.nr.NRUtil.buildVector;
import static com.nr.fft.FFT.four1;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.ranvec;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
public class Test_four1 {

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
    double[] data1 = new double[2*N];
    boolean localflag=false, globalflag=false;

    

    // Test four1
    System.out.println("Testing four1");
    Ran myran = new Ran(17);

    // Round-trip test for reals
    for (i=0;i<N;i++) {
      data1[2*i]=myran.doub();
      data1[2*i+1]=0.0;
    }
    double[] data2=buildVector(data1);
    for (i=0;i<2*N;i++) data2[i] /= N;
    four1(data2,1);
    four1(data2,-1);
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** four1: Round-trip test for random real values failed");
      
    }

    // Round-trip test for imaginaries
    for (i=0;i<N;i++) {
      data1[2*i]=0.0;
      data1[2*i+1]=myran.doub();
    }
    for (i=0;i<2*N;i++) data2[i]=data1[i]/N;
    four1(data2,1);
    four1(data2,-1);
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** four1: Round-trip test for random imaginary values failed");
      
    }

    // Round-trip test for complex numbers
    ranvec(data1);
    for (i=0;i<2*N;i++) data2[i]=data1[i]/N;
    four1(data2,1);
    four1(data2,-1);
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** four1: Round-trip test for random complex values failed");
      
    }

    // Test delta-function in to sine-wave out, forward transform
    for (i=0;i<2*N;i++) data1[i]=0.0;
    data1[2*5]=1.0;
    four1(data1,1);
    for (i=0;i<N;i++) {
      data2[2*i]=cos(2.0*pi*5*i/N);
      data2[2*i+1]=sin(2.0*pi*5*i/N);
//      System.out.printf(setw(15) << data1[2*i] << " " << setw(15) << data1[2*i+1] << " " << setw(15) << data2[2*i] << " " << setw(15) << data2[2*i+1]);
    }
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** four1: Forward transform of a chosen delta function did not give expected result");
      
    }

    // Test delta-function in to sine-wave out, backward transform
    for (i=0;i<2*N;i++) data1[i]=0.0;
    data1[2*7]=1.0;
    four1(data1,-1);
    for (i=0;i<N;i++) {
      data2[2*i]=cos(2.0*pi*7*i/N);
      data2[2*i+1]=-sin(2.0*pi*7*i/N);
//      System.out.printf(setw(15) << data1[2*i] << " " << setw(15) << data1[2*i+1] << " " << setw(15) << data2[2*i] << " " << setw(15) << data2[2*i+1]);
    }
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** four1: Backward transform of a chosen delta function did not give expected result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
