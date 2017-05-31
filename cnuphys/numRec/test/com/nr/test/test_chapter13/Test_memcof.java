package com.nr.test.test_chapter13;

import static com.nr.sp.Fourier.memcof;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_memcof {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=1000,M=10,NEXT=100;
    double sum;
    doubleW pm = new doubleW(0);
    double[] cof= new double[M],reg= new double[M];
    double[] data= new double[N],expect= new double[NEXT],predict= new double[NEXT];
    boolean localflag, globalflag=false;

    

    // Test memcof
    System.out.println("Testing memcof");

    // Noise-free data
    for (i=0;i<N;i++) 
      data[i]=func_memcof(N,i);

    memcof(data,pm,cof);

    localflag = abs(pm.val) > 1.e-15;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** memcof: RMS error should be zero for noise-free data");
      
    }

//    for (i=0;i<M;i++) {
//      System.out.println("a[%f\n", setw(2) << i << "] = ";
//      System.out.printf(setw(12) << cof[i]);
//    }
//    System.out.printf(endl << "a0 = %f\n", setw(12) << pm);

    // Use coefficients to extrapolate and compare to actual function
    for (i=N;i<N+NEXT;i++)
      expect[i-N]=func_memcof(N,i);

    for (i=0;i<M;i++) reg[i]=data[N-1-i];
    for (i=0;i<NEXT;i++) {
      sum=0.0;
      for (j=0;j<M;j++)
        sum += cof[j]*reg[j];
      predict[i]=sum;
      for (j=M-1;j>=1;j--)
        reg[j]=reg[j-1];
      reg[0]=sum;
    }

//    for (i=0;i<NEXT;i++)
//      System.out.printf(expect[i] << " %f\n", predict[i]);

//    System.out.printf(maxel(vecsub(expect,predict)));
    localflag = maxel(vecsub(expect,predict)) > 2.e-3;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** memcof: Extrapolation of noise-free data was not accurate");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  double func_memcof(int N,int i) {
    return sin((i-12.0)/20.0)*exp(-0.1*i/N)
        + 0.3*cos((i+11.0)/35.0)*exp(0.2*i/N);
  }

}
