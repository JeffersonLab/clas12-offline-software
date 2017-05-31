package com.nr.test.test_chapter5;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Ratfn;

public class Test_Ratfn {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,M=10;
    double x,sbeps=1.e-15;
    double b[]={-3.0,15.0,-30.0,30.0,-15.0,3.0}; // Coefficients of 3.0*(x-1.0)^5;
    double c[]={-2.0,6.0,-6.0,2.0}; // Coefficients of 2.0*(x-1.0)^3
    double d[]={1.5,-7.5,15.0,-15.0,7.5,-1.5,-3.0,3.0,-1.0};
    double[] bb = buildVector(b),cc = buildVector(c),dd = buildVector(d),y= new double[M],yy= new double[M];
    boolean localflag, globalflag=false;

    

    // Test Ratfn
    System.out.println("Testing Ratfn: constructor #1");
    Ratfn rp1 = new Ratfn(bb,cc);
    for (i=0;i<M;i++) {
      x=-4.5+i;
      y[i]=rp1.get(x);
      yy[i]=(3.0/2.0)*pow(x-1.0,2);
    }
    System.out.printf("Ratfn: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ratfn: Rational function returns incorrect values");
      
    }

    System.out.println("Testing Ratfn: constructor #2");
    Ratfn rp2= new Ratfn(dd,6,4);
    for (i=0;i<M;i++) {
      x=-4.5+i;
      y[i]=rp2.get(x);
      yy[i]=(3.0/2.0)*pow(x-1.0,2);
    }
    System.out.printf("Ratfn: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ratfn: Rational function returns incorrect values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
