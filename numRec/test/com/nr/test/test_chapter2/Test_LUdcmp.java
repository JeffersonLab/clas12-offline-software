package com.nr.test.test_chapter2;

import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.LUdcmp;

public class Test_LUdcmp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps, diag=10.0;
    double[][] a = new double[50][50],b = new double[50][30];
    boolean localflag, globalflag=false;
    ranmat(a,diag);
    ranmat(b);

    

    // Test ludcmp
    System.out.println("Testing ludcmp");
    LUdcmp alu = new LUdcmp(a);
    double[][] x = new double[b.length][b[0].length];
    alu.solve(b,x);
    sbeps=5.e-15;
    double maxel = maxel(matsub(matmul(a,x),b));
    localflag = maxel > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ludcmp: test of solution vector failed: "+ maxel);
      
    }

    // Test inverse
    double[][] ainv; // = buildMatrix(a); not need it
    ainv = alu.inverse();
    sbeps = 5.e-15;
    localflag = maxel(matsub(matmul(ainv,a),ident(a.length,1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ludcmp: Test of inverse failed");
      
    }

    // Test determinant
    LUdcmp ainvlu = new LUdcmp(ainv);
    sbeps = 5.e-15;
    localflag = (alu.det()*ainvlu.det()-1.) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ludcmp: Test of determinant failed");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
