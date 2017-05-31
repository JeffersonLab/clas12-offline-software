package com.nr.test.test_chapter2;

import static com.nr.NRUtil.*;
import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.Cholesky;

public class Test_Cholesky {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps, diag=10.0;
    double[][] a = new double[50][50];
    double[] r=new double[50],y=new double[50];
    boolean localflag, globalflag=false;
    ranmat(a,diag);
    ranvec(r);

    

    // Test cholesky
    System.out.println("Testing cholesky");
    double[][] aposdef = matmul(a,transpose(a));
    Cholesky ach  = new Cholesky(aposdef);
    ach.solve(r,y);
    sbeps = 5.e-15;
    localflag = maxel(vecsub(matmul(aposdef,y),r)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cholesky: Error in solve() method");
      
    }

    ranvec(y);
    double[] yy = new double[y.length];
    ach.elmult(y,r);
    ach.elsolve(r,yy);
    sbeps = 5.e-15;
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cholesky: Error in method elmult() or elsolve()");
      
    }

    double[][] ainv = buildMatrix(a);
    ach.inverse(ainv);
    sbeps = 5.e-15;
    localflag = maxel(matsub(matmul(ainv,aposdef),ident(aposdef.length,1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cholesky: Error in method inverse()");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
