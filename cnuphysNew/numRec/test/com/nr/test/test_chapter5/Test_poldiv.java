package com.nr.test.test_chapter5;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Poly;

public class Test_poldiv {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps=2.e-15;
    double u[]={1.0,7.0,-10.0,10.0,-5.0,1.0}; // Coefficients of (x-1.0)^5+2.0*(x+1);
    double v[]={-3.0,9.0,-9.0,3.0}; // Coefficients of 3.0*(x-1)^3
    double q[]={1.0/3.0,-2.0/3.0,1.0/3.0,0.0,0.0,0.0}; // Quotient is (1/3)*(x-1.0)^2;
    double r[]={2.0,2.0,0.0,0.0,0.0,0.0}; // Remainter is 2.0*(x+1)
    double[] uu = buildVector(u),vv = buildVector(v),qq=buildVector(q),rr = buildVector(r),s = new double[6],t = new double[6];
    boolean localflag, globalflag=false;

    

    // Test poldiv
    System.out.println("Testing poldiv");
    Poly.poldiv(uu,vv,s,t);
    System.out.printf("poldiv: Maximum discrepancy in quotient = %f\n", maxel(vecsub(s,qq)));
    localflag = maxel(vecsub(s,qq)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** poldiv: Division returns incorrect quotient");
      
    }

    System.out.printf("poldiv: Maximum discrepancy in remainder = %f\n", maxel(vecsub(t,rr)));
    localflag = maxel(vecsub(t,rr)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** poldiv: Division returns incorrect remainder");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
