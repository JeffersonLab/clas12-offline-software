package com.nr.test.test_chapter13;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static java.lang.Math.*;
import static com.nr.sp.Wavelet.*;
import com.nr.sp.*;

public class Test_Daub4 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=32;
    double sum,sbeps;
    double[] a= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Daub4
    System.out.println("Testing Daub4");

    Daub4 db4=new Daub4();
    sbeps=2e-15;
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) a[j]=0.0;
      a[i]=1.0/sqrt(N/2.0);
      wt1(a,-1,db4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;

      sum=0;
      for (j=0;j<N;j++) sum += a[j];
//      System.out.printf(sum);
      if (i < 2) localflag = abs(sum-1.0) > sbeps;
      else localflag = abs(sum) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** Daub4: Sum of wavelet coefficients had unexpected value");
        
      }

      wt1(a,+1,db4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      for (j=0;j<N;j++) {
        if (j == i) localflag = abs(a[j]-1.0/sqrt(N/2.0)) > sbeps;
        else localflag = abs(a[j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Daub4: Round-trip test did not return to original data");
          
        }
      }
    }

    // Test a smooth function for lack of detail coefficients
    for (i=0;i<N;i++) a[i]=1.0;
    wt1(a,1,db4);
//    for (i=0;i<N;i++)
//      System.out.printf(setw(12) << a[i];
//    System.out.printf(endl;

    sbeps=1.e-15;
    localflag=false;
    for (i=2;i<N;i++) 
      localflag=localflag || abs(a[i]) > sbeps;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** Daub4: Constant function should have no detail coefficients");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
