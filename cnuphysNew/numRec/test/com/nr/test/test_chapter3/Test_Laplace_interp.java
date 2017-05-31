package com.nr.test.test_chapter3;

import static com.nr.NRUtil.*;
import static com.nr.test.NRTestUtil.*;
import static java.lang.Math.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Laplace_interp;
import com.nr.ran.Ran;

public class Test_Laplace_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,p,q,N=100,M=100,NBAD=1000;
    double sbeps=0.01;
    double[][] actual = new double[N][M];
    boolean localflag, globalflag=false;

    

    // Test Laplace_interp
    System.out.println("Laplace_interp");
    Ran myran = new Ran(17);
    for (i=0;i<N;i++)
      for (j=0;j<M;j++)
        actual[i][j]=cos((double)(i)/20.0)*cos((double)(j)/20.0);
    double[][] mat = buildMatrix(actual);
    for (i=0;i<NBAD;i++) {  // insert "missing" data
      p=myran.int32p()%N;
      q=myran.int32p()%M;
      mat[p][q]=1.e99;
    }
    System.out.printf("     Initial discrepancy: %g\n", maxel(matsub(actual,mat)));
    Laplace_interp mylaplace = new Laplace_interp(mat);
    mylaplace.solve();
    System.out.printf("     Final discrepancy: %g\n", maxel(matsub(actual,mat)));
    localflag = maxel(matsub(actual,mat)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Laplace_interp: Inaccurate Laplace interpolation of missing matrix data.");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
