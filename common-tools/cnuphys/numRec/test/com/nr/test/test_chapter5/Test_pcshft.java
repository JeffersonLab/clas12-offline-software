package com.nr.test.test_chapter5;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Chebyshev;

public class Test_pcshft {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=11,M=6;
    double x,xx,a=1.0,b=3.0,val,sbeps=1.e-13;
    double d[]={-1,6.0,-10.0,10.0,-6.0,1.0};
    double[] dd = buildVector(d),y=new double[N],yy=new double[N];
    boolean localflag, globalflag=false;

    

    // Test pcshft
    System.out.println("Testing pcshft");
    double[] g = buildVector(dd);
    Chebyshev.pcshft(a,b,g);
    for (i=0;i<N;i++) {
      x= a+0.1*(b-a)*i;
      xx=(x-0.5*(b+a))/(0.5*(b-a));
      val=g[M-1];
      for (j=M-2;j>=0;j--) val=val*x+g[j];
      y[i]=val;
      val=dd[M-1];
      for (j=M-2;j>=0;j--) val=val*xx+dd[j];
      yy[i]=val;
    }
    localflag = maxel(vecsub(g,dd)) < 1.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pcshft: Polynomial was not shifted");
      
    }

    System.out.printf("pcshft: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pcshft: Shifted polynomial does not return the same values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
