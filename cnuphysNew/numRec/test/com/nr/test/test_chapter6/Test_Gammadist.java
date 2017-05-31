package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midinf;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Gammadist;
import static com.nr.fi.Midpnt.qromo;

public class Test_Gammadist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20;
    double alpha,beta,u,a,b,integral,sbeps=1.e-15;
    double xx[]={0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,1.9,2.0};
    double ppexp[]={0.056421908930476883,0.13735630417827352,0.21719074405218716,
      0.28780958001849588,0.34619922631227434,0.39170019681318374,
      0.42484430595288569,0.4467592468238949,0.45883671466221931,
      0.46254098941130778,0.45929855649326967,0.45043668078371302,
      0.43715226875054808,0.42049960233268446,0.40138972612974905,
      0.38059682877263284,0.35876857953338254,0.33643843523417555,
      0.31403863132501508,0.29191303997784868};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Gammadist
    System.out.println("Testing Gammadist");

    // Test special cases
    alpha=1.0; beta=1.0; u=1.0;
    Gammadist norm1 = new Gammadist(alpha,beta);
    localflag = abs(norm1.p(u)-exp(-1.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Special case #1 failed");
      
    }

    alpha=1.0; beta=2.0; u=1.0;
    Gammadist norm2 = new Gammadist(alpha,beta);
//    System.out.printf(abs(norm2.p(u)-2.0*exp(-2.0));
    localflag = abs(norm2.p(u)-2.0*exp(-2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Special case #2 failed");
      
    }

    alpha=2.0; beta=1.0; u=1.0;
    Gammadist norm3 = new Gammadist(alpha,beta);
//    System.out.printf(abs(norm3.p(u)-x*exp(-1.0));
    localflag = abs(norm3.p(u)-u*exp(-1.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Special case #3 failed");
      
    }

    alpha=3.0; beta=1.0; u=1.0;
    Gammadist norm4 = new Gammadist(alpha,beta);
    localflag = abs(norm4.p(u)-u*u*exp(-1.0)/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Special case #4 failed");
      
    }

    alpha=2.0; beta=2.0; u=2.0;
    Gammadist norm5 = new Gammadist(alpha,beta);
    localflag = abs(norm5.p(u)-4.0*u*exp(-4.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=2.e-7;
    alpha=2.5; beta=1.5;
    func_Gammadist dist = new func_Gammadist(alpha,beta);
    Midpnt q2 = new Midpnt(dist,0.0,2.0);
    Midinf q3 = new Midinf(dist,2.0,1.0e99);
    integral=qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=5.e-7;
    alpha=2.5; beta=1.5;
    func_Gammadist dist2 = new func_Gammadist(alpha,beta);
    Gammadist normcdf = new Gammadist(alpha,beta);
    localflag=false;
    for (i=0;i<N;i++) {
      q2 =new Midpnt(dist2,0.0,x[i]);
      integral=qromo(q2);
      c[i]=integral;
      d[i]=normcdf.cdf(x[i]);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    alpha=2.5; beta=1.5;
    Gammadist normc = new Gammadist(alpha,beta);
    Ran myran = new Ran(17);
    sbeps=5.0e-14;
    localflag=false;
    for (i=0;i<1000;i++) {
      u=3.0*myran.doub();
      a=normc.cdf(u);
      b=normc.invcdf(a);
//      if (abs(u-b) > sbeps) {
//        System.out.printf(setprecision(15) << u << " %f\n", b << " %f\n", abs(u-b));
//      }
      localflag = localflag || abs(u-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    alpha=2.5; beta=1.5;
    Gammadist normf = new Gammadist(alpha,beta);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Gammadist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Gammadist implements UniVarRealValueFun{
    double alpha,beta;
    Gammadist normi;
    func_Gammadist(double aalpha, double bbeta) {
      alpha=aalpha;
      beta=bbeta;
      normi = new Gammadist(alpha,beta);
    }
    
    public double funk(final double x) {
      return normi.p(x);
    }
  };
}
