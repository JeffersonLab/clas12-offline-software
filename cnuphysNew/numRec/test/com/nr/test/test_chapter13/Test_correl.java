package com.nr.test.test_chapter13;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static java.lang.Math.*;
import static com.nr.sp.Fourier.*;

public class Test_correl {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=64;
    double cmp,sbeps;
    double[] data1= new double[N],data2= new double[N],ans= new double[N];
    boolean localflag, globalflag=false;

    

    // Test correl
    System.out.println("Testing correl");
    
    for (i=0;i<N;i++) {
      if ((i > N/2-N/8-1) && (i < N/2+N/8-1))
        data1[i]=1.0;
      else
        data1[i]=0.0;
      data2[i]=data1[i];
    }
    correl(data1,data2,ans);

    // Calculate result directly
    localflag=false;
    sbeps = 2.e-14;
    for (i=0;i<=16;i++) {
      cmp=0.0;
      for (j=0;j<N;j++)
        cmp += data1[((i+j) % N)]*data2[j];
      localflag=localflag || abs(ans[i]-cmp) > sbeps;
//      System.out.printf(i << " " << ans[i] << " " << cmp);
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** correl: Direct calculation did not agree with F.T. result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
