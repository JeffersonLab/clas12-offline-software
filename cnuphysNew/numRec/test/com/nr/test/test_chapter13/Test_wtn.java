package com.nr.test.test_chapter13;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sp.Daub4;
import static com.nr.sp.Wavelet.*;

public class Test_wtn {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,jj,NDIM=2,N=128,M=32,NTRY=10;
    double sum,sbeps=5.e-15;
    int[] nn=new int[NDIM];
    double[] a=new double[N*M];
    boolean localflag, globalflag=false;

    

    // Test wtn
    System.out.println("Testing wtn");

    Daub4 d4=new Daub4();
    Ran myran = new Ran(17);
    nn[0]=N;
    nn[1]=M;
    for (i=0;i<NTRY;i++) {
      for (j=0;j<M*N;j++) a[j]=0.0;
      jj=myran.int32p() % (M*N);
      a[jj]=1.0;
      wtn(a,nn,-1,d4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      sum=0;
      for (j=0;j<M*N;j++) sum += a[j];
//      System.out.printf(sum);
      localflag = abs(sum) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** wtn: Sum of wavelet coefficients had unexpected value");
        
      }

      wtn(a,nn,+1,d4);
//      for (j=0;j<N;j++) System.out.printf(setw(8) << a[j] << " ";
//      System.out.printf(endl;
      for (j=0;j<M*N;j++) {
        if (j == jj) localflag = abs(a[j]-1.0) > sbeps;
        else localflag = abs(a[j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** wtn: Round-trip test did not return to original data");
          
        }
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
