package com.nr.test.test_chapter13;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.sp.Fourier.*;

public class Test_convlv {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,isign,N=16,M=9; // M=response function dimension (odd integer)
    double cmp,sbeps;
    double[] data= new double[N],respns= new double[N],resp=new double[M],ans= new double[N];
    boolean localflag, globalflag=false;

    

    // Test convlv
    System.out.println("Testing convlv");

    for (i=0;i<N;i++)
      if ((i >= N/2-N/8-1) && (i <= N/2+N/8-1))
        data[i]=1.0;
      else
        data[i]=0.0;
    for (i=0;i<M;i++) {
      if ((i > 1) && (i < 6))
        respns[i]=1.0;
      else
        respns[i]=0.0;
      resp[i]=respns[i];
    }
    isign=1;
    convlv(data,resp,isign,ans);
    // compare with a direct convolution
    localflag=false;
    sbeps=2.e-15;
    for (i=0;i<N;i++) {
      cmp=0.0;
      for (j=1;j<=M/2;j++) {
        cmp += data[(i-j+N) % N]*respns[j];
        cmp += data[(i+j) % N]*respns[M-j];
      }
      cmp += data[i]*respns[0];
//      System.out.printf(setw(3) << i << setw(16) << ans[i];
//      System.out.printf(setw(13) << cmp);
      localflag = localflag || (ans[i]-cmp) > sbeps;
    }

    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** convlv: Direct calculation did not agree with F.T. result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
