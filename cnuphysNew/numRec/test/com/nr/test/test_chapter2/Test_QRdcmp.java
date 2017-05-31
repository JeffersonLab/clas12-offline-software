package com.nr.test.test_chapter2;

import static com.nr.NRUtil.*;
import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.QRdcmp;

public class Test_QRdcmp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps, diag=10.0;
    int i,j;
    double[][] a = new double[50][50];
    double[] r = new double[50], y = new double[50];
    boolean localflag, globalflag=false;
    ranmat(a,diag);

    

    // Test QRdcmp
    System.out.println("Testing QRdcmp");
    QRdcmp aqr = new QRdcmp(a);
    sbeps = 5.e-14;
    localflag = maxel(matsub(matmul(transpose(aqr.qt),aqr.r),a)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** QRdcmp: QR decomposition was unsuccessful");
      
    }

    localflag = maxel(matsub(matmul(transpose(aqr.qt),aqr.qt),ident(a.length,1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** QRdcmp: Matrix aqr.qt is not orthogonal");
      
    }

    ranvec(r);
    aqr.solve(r,y);
    localflag = maxel(vecsub(matmul(a,y),r)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** QRdcmp: Error in solve() method");
      
    }

    // test update
    double[][] aa = buildMatrix(a);
    double[] s = new double[aa.length],t = new double[aa.length];
    ranvec(s); ranvec(t);
    double[] u = new double[aa.length],v = buildVector(t);
    aqr.qtmult(s,u);
    aqr.update(u,v);
    for (i=0;i<aa.length;i++) for (j=0;j<aa.length;j++) a[i][j] = aa[i][j]+s[i]*t[j];
    localflag = maxel(matsub(matmul(transpose(aqr.qt),aqr.r),a)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** QRdcmp: Error in method qtmult() or update()");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
