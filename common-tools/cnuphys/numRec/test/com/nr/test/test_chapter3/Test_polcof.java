package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.*;
import static com.nr.interp.PolCoef.*;
import static java.lang.Math.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_polcof {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double pi,sbeps;
    int i,j,N=10;
    double[] x= new double[N],y= new double[N],cof= new double[N],z= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Linear_interp
    System.out.println("Testing polcof");
    pi=acos(-1.0);
    for (i=0;i<N;i++) {
      x[i]=pi*(double)(i)/(N-1);
      y[i]=sin(x[i]);
    }
    polcof(x,y,cof);
    for (i=0;i<N;i++) {
      z[i]=0;
      for (j=0;j<N;j++) 
        z[i]=z[i]*x[i]+cof[N-1-j];
    }
    sbeps=1.e-15*maxel(y);
    System.out.printf("     polcof: Max. actual error:    %f\n", maxel(vecsub(z,y)));
    localflag = maxel(vecsub(z,y)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polcof: .");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
