package com.nr.test.test_chapter2;

import static com.nr.la.GaussJordan.gaussj;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_gaussj {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps, diag=10.0;
    double[][] a = new double[50][50],b =new double[50][30];
    boolean localflag, globalflag=false;
    ranmat(a,diag);
    ranmat(b);
    double[][] aa = buildMatrix(a),bb = buildMatrix(b); // saved original values

//    System.out.println("Matrices a,b initialized with maxels " << maxel(a) << maxel(b) << endl;

    

    // Test gaussj
    System.out.println("Testing gaussj");

    gaussj(a,b);
    // test inverse matrix
    sbeps = 5.e-15;
    localflag = maxel(matsub(matmul(a,aa),ident(a.length,1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** gaussj: Test of  inverse failed");
      
    }

    // Test rhs's
    sbeps = 5.e-15;
    localflag = maxel(matsub(matmul(aa,b),bb)) > sbeps;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** gaussj: Test of RHS failed");
      
    }

    // restore original a
    a = buildMatrix(aa);

    // Test inverse with no rhs's
    gaussj(a);
    sbeps = 5.e-15;
    localflag = maxel(matsub(matmul(a,aa),ident(a.length,1.))) > sbeps;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** gaussj: test of inverse with no RHS failed");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
