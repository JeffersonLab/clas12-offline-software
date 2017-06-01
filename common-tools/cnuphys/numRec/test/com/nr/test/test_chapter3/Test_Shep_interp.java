package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.cos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Shep_interp;
import com.nr.ran.Ran;

public class Test_Shep_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,NPTS=100,NDIM=2,N=10,M=10;
    double p=5.0,sbeps=0.05;
    double[][] pts = new double[NPTS][NDIM];
    double[] y = new double[NPTS],actual = new double[M],estim = new double[M],ppt = new double[2];
    boolean localflag, globalflag=false;

    

    // Test Shep_interp
    System.out.println("Testing Shep_interp");
    Ran myran = new Ran(17);
    double[][] pt = new double[M][2];
    for (i=0;i<M;i++) {
      pt[i][0]=(double)(N)*myran.doub();
      pt[i][1]=(double)(N)*myran.doub();
      actual[i]=cos(pt[i][0]/20.0)*cos(pt[i][1]/20.0);
    }
    for (i=0;i<N;i++) {
      for (j=0;j<N;j++) {
        k=N*i+j;
        pts[k][0]=(double)(j);
        pts[k][1]=(double)(i);
        y[k]=cos(pts[k][0]/20.0)*cos(pts[k][1]/20.0);
      }
    }
    Shep_interp shep =new Shep_interp(pts,y,p);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=shep.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Shepard: Inaccurate Shepard interpolation.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
