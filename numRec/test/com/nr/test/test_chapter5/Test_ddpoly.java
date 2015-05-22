package com.nr.test.test_chapter5;

import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.nr.fe.Poly;
public class Test_ddpoly {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,M=11;
    double x,sbeps=1.e-15;
    double c[]={-1.0,5.0,-10.0,10.0,-5.0,1.0}; // Coefficients of (x-1.0)^5;
    double[] cc = buildVector(c),pd = new double[6],p = new double[6],y = new double[M];
    boolean localflag, globalflag=false;

    

    // Test ddpoly
    System.out.println("Testing ddpoly");
    for (i=0;i<M;i++) {
      x=-5.0+i;
      Poly.ddpoly(cc,x,pd);
      p[0]=pow(x-1.0,5);
      p[1]=5.0*pow(x-1.0,4);
      p[2]=20.0*pow(x-1.0,3);
      p[3]=60.0*pow(x-1.0,2);
      p[4]=120.0*(x-1.0);
      p[5]=120.0;
      y[i]=maxel(vecsub(p,pd));
    }
    System.out.printf("ddpoly: Maximum discrepancy = %f\n", maxel(y));
    localflag = maxel(y) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ddpoly: Incorrect values for function or derivatives");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
