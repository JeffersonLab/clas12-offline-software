package com.nr.test.test_chapter5;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Poly;

public class Test_Poly {

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
    double[] cc = buildVector(c),y= new double[M],yy= new double[M];
    boolean localflag, globalflag=false;

    

    // Test Poly
    System.out.println("Testing Poly");
    Poly p = new Poly(cc);
    for (i=0;i<M;i++) {
      x=-5.0+i;
      y[i]=p.get(x);
      yy[i]=pow(x-1.0,5);
    }
    System.out.printf("Poly: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poly: Polynomial returns incorrect values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
