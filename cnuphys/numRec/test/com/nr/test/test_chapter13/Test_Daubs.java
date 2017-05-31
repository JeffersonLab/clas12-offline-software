package com.nr.test.test_chapter13;

import static com.nr.sp.Wavelet.wt1;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sp.Daub4;
import com.nr.sp.Daubs;

public class Test_Daubs {

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
    double[] a= new double[N],b= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Daubs
    System.out.println("Testing Daubs");

    Daubs ds4 = new Daubs(4);
    sbeps=2e-15;
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) a[j]=0.0;
      a[i]=1.0/sqrt(N/2.0);
      wt1(a,-1,ds4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      sum=0;
      for (j=0;j<N;j++) sum += a[j];
//      System.out.printf(sum);
      if (i < 2) localflag = abs(sum-1.0) > sbeps;
      else localflag = abs(sum) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** Daubs(4): Sum of wavelet coefficients had unexpected value");
        
      }

      wt1(a,+1,ds4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      for (j=0;j<N;j++) {
        if (j == i) localflag = abs(a[j]-1.0/sqrt(N/2.0)) > sbeps;
        else localflag = abs(a[j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Daubs(4): Round-trip test did not return to original data");
          
        }
      }
    }

    Daubs ds12=new Daubs(12);
    sbeps=1.e-11;
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) a[j]=0.0;
      a[i]=1.0/sqrt(N/2.0);
      wt1(a,-1,ds12);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      sum=0;
      for (j=0;j<N;j++) sum += a[j];
//      System.out.printf(sum);
      if (i < 2) localflag = abs(sum-1.0) > sbeps;
      else localflag = abs(sum) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** Daubs(12): Sum of wavelet coefficients had unexpected value");
        
      }

      wt1(a,+1,ds12);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      for (j=0;j<N;j++) {
        if (j == i) localflag = abs(a[j]-1.0/sqrt(N/2.0)) > sbeps;
        else localflag = abs(a[j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Daubs(12): Round-trip test did not return to original data");
          
        }
      }
    }

    Daubs ds20=new Daubs(20);
    sbeps=1.e-11;
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) a[j]=0.0;
      a[i]=1.0/sqrt(N/2.0);
      wt1(a,-1,ds20);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      sum=0;
      for (j=0;j<N;j++) sum += a[j];
//      System.out.printf(sum);
      if (i < 2) localflag = abs(sum-1.0) > sbeps;
      else localflag = abs(sum) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** Daubs(20): Sum of wavelet coefficients had unexpected value");
        
      }

      wt1(a,+1,ds20);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      for (j=0;j<N;j++) {
        if (j == i) localflag = abs(a[j]-1.0/sqrt(N/2.0)) > sbeps;
        else localflag = abs(a[j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Daubs(20): Round-trip test did not return to original data");
          
        }
      }
    }

    // Compare Daubs to Daub4 for the case of 4 coefficients
    Daub4 db4 = new Daub4();
    sbeps=1.e-15;
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) b[j]=a[j]=0.0;
      b[i]=a[i]=1.0/sqrt(N/2.0);
      wt1(a,-1,ds4);
      wt1(b,-1,db4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " " << b[j]);
//      System.out.printf(endl;
      
      localflag = maxel(vecsub(a,b)) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** Daubs(4): Daubs does not agree with Daub4 for the case of 4 coefficients");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
