package com.nr.test.test_chapter13;

import static com.nr.sp.Wavelet.wt1;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sp.Daub4;

public class Test_wt0 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=128;
    double sum,sbeps=2.e-15;
    double[] a=new double[N];
    boolean localflag, globalflag=false;

    

    // Test wt1
    System.out.println("Testing wt1");

    Daub4 d4=new Daub4();
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) a[j]=0.0;
      a[i]=1.0/sqrt(N/2.0);
      wt1(a,-1,d4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      sum=0;
      for (j=0;j<N;j++) sum += a[j];
//      System.out.printf(sum);
      if (i < 2) localflag = abs(sum-1.0) > sbeps;
      else localflag = abs(sum) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** wt1: Sum of wavelet coefficients had unexpected value");
        
      }

      wt1(a,+1,d4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      for (j=0;j<N;j++) {
        if (j == i) localflag = abs(a[j]-1.0/sqrt(N/2.0)) > sbeps;
        else localflag = abs(a[j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** wt1: Round-trip test did not return to original data");
          
        }
      }
    }

    localflag = false;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** wt1: *********************");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
