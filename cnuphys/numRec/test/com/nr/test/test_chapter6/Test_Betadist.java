package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Betadist;
import static com.nr.fi.Midpnt.qromo;

public class Test_Betadist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=19;
    double alpha,beta,u,a,b,integral,sbeps=2.e-15;
    double xx[]={0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95};
    double ppexp[]={0.055499225064202982,0.15278874536821926,0.27278247243349879,
      0.40743665431525133,0.55132889542179109,0.70016599098719778,
      0.85021518016199371,0.99801190557910335,1.1401716162949527,
      1.2732395447351601,1.3935430865827199,1.4970178583686551,
      1.5789710488722741,1.6337206456367952,1.6539866862653729,
      1.6297466172610049,1.5457673437898269,1.3750987083139725,
      1.0544852762198569};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p=new double[N],c=new double[N],d=new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Betadist
    System.out.println("Testing Betadist");

    // Test special cases
    alpha=1.0; beta=1.0; u=0.5;
    Betadist norm1 = new Betadist(alpha,beta);
    localflag = abs(norm1.p(u)-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Special case #1 failed");
      
    }

    alpha=1.0; beta=2.0; u=0.5;
    Betadist norm2=new Betadist(alpha,beta);
    localflag = abs(norm2.p(u)-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Special case #2 failed");
      
    }

    alpha=2.0; beta=1.0; u=0.5;
    Betadist norm3=new Betadist(alpha,beta);
    localflag = abs(norm3.p(u)-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Special case #3 failed");
      
    }

    alpha=2.0; beta=2.0; u=0.5;
    Betadist norm4=new Betadist(alpha,beta);
    localflag = abs(norm4.p(u)-3.0/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Special case #4 failed");
      
    }

    alpha=2.0; beta=2.0; u=1.0/3.0;
    Betadist norm5=new Betadist(alpha,beta);
    localflag = abs(norm5.p(u)-4.0/3.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=2.e-6;
    alpha=2.5; beta=1.5;
    func_Betadist dist = new func_Betadist(alpha,beta);
    Midpnt q2 = new Midpnt(dist,0.0,1.0);
    integral=qromo(q2);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=5.e-6;
    alpha=2.5; beta=1.5;
    func_Betadist dist2 = new func_Betadist(alpha,beta);
    Betadist normcdf =new Betadist(alpha,beta);
    localflag=false;
    for (i=0;i<N;i++) {
      Midpnt qq2 = new Midpnt(dist2,0.0,x[i]);
      integral=qromo(qq2);
      c[i]=integral;
      d[i]=normcdf.cdf(x[i]);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    alpha=2.5; beta=1.5;
    Betadist normc = new Betadist(alpha,beta);
    Ran myran = new Ran(17);
    sbeps=5.0e-14;
    localflag=false;
    for (i=0;i<1000;i++) {
      u=myran.doub();
      a=normc.cdf(u);
      b=normc.invcdf(a);
//      if (abs(u-b) > sbeps) {
//        System.out.printf(setprecision(15) << u << " %f\n", b << " %f\n", abs(u-b));
//      }
      localflag = localflag || abs(u-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    alpha=2.5; beta=1.5;
    Betadist normf = new Betadist(alpha,beta);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Betadist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Betadist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Betadist implements UniVarRealValueFun{
    double alpha,beta;
    Betadist normi;
    func_Betadist(double aalpha, double bbeta) {
      alpha = aalpha;
      beta = bbeta;
      normi = new Betadist(alpha,beta);
    }
    
    public double funk(final double x) {
      return normi.p(x);
    }
  }

}
