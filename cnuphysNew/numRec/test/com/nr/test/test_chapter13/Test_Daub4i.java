package com.nr.test.test_chapter13;

import static com.nr.sp.Wavelet.wt1;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sp.Daub4i;

public class Test_Daub4i {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,p,m,n,N=128;
    double sbeps=2.e-15;
    double[] a= new double[N],b= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Daub4i
    System.out.println("Testing Daub4i");

    Daub4i d4i=new Daub4i();
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) a[j]=0.0;
      a[i]=1.0/sqrt(N/2.0);

      wt1(a,-1,d4i);
      
//      if (i > 27 && i < 32) {
//        for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//        System.out.printf(endl);
//      }

      // Round-trip test
      wt1(a,1,d4i);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;

      for (j=0;j<N;j++) {
        if (j == i) 
          localflag = abs(a[j]-1.0/sqrt(N/2.0)) > sbeps;
        else 
          localflag = abs(a[j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Daub4i: Round-trip test did not return to original data");
          
        }
      }
    }

    // Translation test
    localflag=false;
    sbeps=1.e-15;
    for (i=3;i<7;i++) {
      p = (1 << i);
      for (j=p+2;j<2*p-3;j++) {
        for (k=0;k<N;k++) a[k]=b[k]=0.0;
        a[j]=1.0/sqrt(N/2.0);
        b[j+1]=1.0/sqrt(N/2.0);
        wt1(a,-1,d4i);
        wt1(b,-1,d4i);
        m=n=0;
        while(a[m] == 0.0 && m<N) {m++;}
        while(b[n] == 0.0 && n<N) {n++;}
        while(a[m] != 0 && m < N)
          localflag = localflag || abs(a[m++]-b[n++]) > sbeps;
      }
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** Daub4i: Wavelets do not follow expected translation pattern");
      
    }


    // Test a constant function
    for (i=0;i<N;i++) a[i]=1.0;
    wt1(a,1,d4i);
//    for (i=0;i<N;i++)
//      System.out.printf(setw(12) << a[i];
//    System.out.printf(endl;

    sbeps=1.e-14;
    localflag=false;
    for (i=4;i<N;i++) {
//      if (abs(a[i]) > sbeps) System.out.printf(i << " " << abs(a[i]));
      localflag=localflag || abs(a[i]) > sbeps;
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** Daub4i: Constant function should have no detail coefficients");
      
    }

    // Test a linear function
    for (i=0;i<N;i++) a[i]=i;
    wt1(a,1,d4i);
//    for (i=0;i<N;i++)
//      System.out.printf(setw(12) << a[i];
//    System.out.printf(endl;
    
    sbeps=1.e-12;
    localflag=false;
    for (i=4;i<N;i++) 
      localflag=localflag || abs(a[i]) > sbeps;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** Daub4i: Linear function should have no detail coefficients");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
