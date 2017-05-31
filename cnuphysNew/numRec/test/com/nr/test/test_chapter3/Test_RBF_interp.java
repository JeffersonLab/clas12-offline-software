package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.cos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.RBF_gauss;
import com.nr.interp.RBF_interp;
import com.nr.interp.RBF_inversemultiquadric;
import com.nr.interp.RBF_multiquadric;
import com.nr.interp.RBF_thinplate;
import com.nr.ran.Ran;

public class Test_RBF_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,NPTS=100,NDIM=2,N=10,M=10;
    double scale,sbeps=0.05;
    double[][] pts =new double[NPTS][NDIM];
    double[] y = new double[NPTS],actual = new double[M],estim = new double[M],ppt = new double[2];
    boolean localflag, globalflag=false;

    

    // Test RBF_interp
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

    System.out.println("Testing RBF_interp with multiquadric function");
    scale=3.0;
    RBF_multiquadric multiquadric = new RBF_multiquadric(scale);
    RBF_interp myRBFmqf = new RBF_interp(pts,y,multiquadric,false);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFmqf.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,multiquadric: Inaccurate multquadric interpolation with no normalization.");
      
    }

    System.out.println("Testing RBF_interp with thinplate function");
    scale=2.0;
    RBF_thinplate thinplate = new RBF_thinplate(scale);
    RBF_interp myRBFtpf = new RBF_interp(pts,y,thinplate,false);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFtpf.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,thinplate: Inaccurate thinplate interpolation with no normalization.");
      
    }

    System.out.println("Testing RBF_interp with gauss function");
    scale=5.0;
    RBF_gauss gauss = new RBF_gauss(scale);
    RBF_interp myRBFgf = new RBF_interp (pts,y,gauss,false);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFgf.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,gauss: Inaccurate gauss interpolation with no normalization.");
      
    }

    System.out.println("Testing RBF_interp with inversemultiquadric function");
    scale=3.0;
    RBF_inversemultiquadric inversemultiquadric = new RBF_inversemultiquadric(scale);
    RBF_interp myRBFimqf =new RBF_interp(pts,y,inversemultiquadric,false);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFimqf.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,inversemultiquadric: Inaccurate inversemultiquadric interpolation with no normalization.");
      
    }

    // Test same interpolators with normalization turned on
    scale=3.0;
    System.out.println("Testing RBF_interp with multiquadric function");
    RBF_interp myRBFmqt = new RBF_interp(pts,y,multiquadric,true);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFmqt.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,multiquadric: Inaccurate multiquadric interpolation with normalization.");
      
    }

    System.out.println("Testing RBF_interp with thinplate function");
    scale=2.0;
    RBF_interp myRBFtpt =new RBF_interp(pts,y,thinplate,true);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFtpt.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,thinplate: Inaccurate thinplate interpolation with normalization.");
      
    }

    System.out.println("Testing RBF_interp with gauss function");
    scale=5.0;
    RBF_interp myRBFgt = new RBF_interp(pts,y,gauss,true);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFgt.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,gauss: Inaccurate gauss interpolation with normalization.");
      
    }

    System.out.println("Testing RBF_interp with inversemultiquadric function");
    scale=2.0;
    RBF_interp myRBFimqt = new RBF_interp(pts,y,inversemultiquadric,true);
    for (i=0;i<M;i++) {
      ppt[0]=pt[i][0];
      ppt[1]=pt[i][1];
      estim[i]=myRBFimqt.interp(ppt);
    }
    System.out.printf("     Discrepancy: %f\n", maxel(vecsub(actual,estim)));
    localflag = maxel(vecsub(actual,estim)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** RBF_interp,inversemultiquadric: Inaccurate inversemultiquadric interpolation with normalization.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
