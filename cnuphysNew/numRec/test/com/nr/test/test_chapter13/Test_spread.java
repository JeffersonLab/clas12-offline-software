package com.nr.test.test_chapter13;

import static com.nr.sp.Fourier.spread;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_spread {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,N=100,M=8;
    double coef[]={1.0,2.0,3.0,1.5,0.5,0.3,1.5,0.5,0.2};
    double x,y,f,sum,sbeps;
    double[] yy=new double[N],xx=new double[N];
    boolean localflag, globalflag=false;

    

    // Test spread
    System.out.println("Testing spread");

    x=32.5;
    y=5.0;
    spread(y,yy,x,M);
//    for (i=0;i<N;i++)
//      System.out.printf(i << " %f\n", yy[i]);

    for (i=0;i<N;i++) xx[i]=i+1.0;

    for (j=1;j<M+1;j++) {

      f=0.0;
      for (i=0;i<j;i++)
        f += coef[i]*pow(x-37.5,i);

      y=f*5.0;

      sum=0.0;
      for (i=0;i<N;i++) {
        f=0.0;
        for (k=0;k<j;k++)
          f += coef[k]*pow(xx[i]-37.5,k);
        sum += f*yy[i];
      }

      sbeps=1.e-15;
//      System.out.printf(abs(y/sum-1.0));
      localflag = abs(y/sum-1.0) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** spread: Incorrect extirpolation at order "+ (j-1));
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
