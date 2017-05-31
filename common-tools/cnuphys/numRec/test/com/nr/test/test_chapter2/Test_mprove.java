package com.nr.test.test_chapter2;

import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.LUdcmp;

public class Test_mprove {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps, diag=10.0;
    int i;
    double[][] a = new double[50][50];
    double[] r = new double[50],y = new double[50];
    boolean localflag, globalflag=false;
    ranmat(a,diag);

    

    // Test mprove
    System.out.println("Testing mprove");

    ranvec(r);
    LUdcmp alu = new LUdcmp(a);
    alu.solve(r,y);
    for (i=0;i<y.length;i++) y[i] += 0.005*(2.*ran.doub()-1.);
    localflag = maxel(vecsub(r,matmul(a,y))) < 0.001;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mprove: Noisy vector too close to solution");
      
    }

    alu.mprove(r,y);
    sbeps = 5.e-15;
    localflag = maxel(vecsub(r,matmul(a,y))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mprove: Attempt to recover accurate solution failed");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
