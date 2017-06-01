package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static org.junit.Assert.fail;
import static com.nr.fi.Midpnt.qromo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midinf;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Cauchydist;

public class Test_Cauchydist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=15;
    double pi=acos(-1.0),oneoverpi,m,s,u,a,b,integral,sbeps;
    double xx[]={-3.0,-2.5,-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0};
    double ppexp[]={0.032928608915564553,0.042441318157838762,0.056172332855963063,
      0.076394372684109771,0.1061032953945969,0.14691225516174955,
      0.19098593171027439,0.21220659078919379,0.19098593171027439,
      0.14691225516174955,0.1061032953945969,0.076394372684109771,
      0.056172332855963063,0.042441318157838762,0.032928608915564553};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Cauchydist
    System.out.println("Testing Cauchydist");

    oneoverpi=1.0/pi;

    sbeps=1.e-15;
    // Test special cases
    m=0; s=1; u=0;
    Cauchydist norm1=new Cauchydist(m,s);
    localflag = abs(norm1.p(u)-oneoverpi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Special case #1 failed");
      
    }

    m=1; s=1; u=m;
    Cauchydist norm2=new Cauchydist(m,s);
    localflag = abs(norm2.p(u)-oneoverpi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Special case #2 failed");
      
    }

    m=1; s=1; u=0;
    Cauchydist norm3=new Cauchydist(m,s);
    localflag = abs(norm3.p(u)-oneoverpi/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Special case #3 failed");
      
    }

    m=1; s=2; u=1;
    Cauchydist norm4=new Cauchydist(m,s);
    localflag = abs(norm4.p(u)-oneoverpi/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Special case #4 failed");
      
    }

    m=1; s=2; u=0;
    Cauchydist norm5=new Cauchydist(m,s);
    localflag = abs(norm5.p(u)-oneoverpi/2.0/(1+1.0/4.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=1.e-8;
    m=1.0;s=2.0;
    func_Cauchydist dist=new func_Cauchydist(m,s);
    Midinf q1 = new Midinf(dist,-1.0e99,-1.0);
    Midpnt q2 = new Midpnt(dist,-1.0,1.0);
    Midinf q3 = new Midinf(dist,1.0,1.0e99);
    integral=qromo(q1)+qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-7;
    m=0.5;s=1.5;
    func_Cauchydist dist2=new func_Cauchydist(m,s);
    Cauchydist normcdf = new Cauchydist(m,s);
    localflag=false;
    for (i=0;i<N;i++) {
      if (x[i] < 0.0) {
        q1 = new Midinf(dist2,-1.e99,x[i]);
        integral=qromo(q1);
      } else {
        q1 = new Midinf(dist2,-1.e99,-1.0);
        q2 = new Midpnt(dist2,-1.0,x[i]);
        integral=qromo(q1)+qromo(q2);
      }
      c[i]=integral;
      d[i]=normcdf.cdf(x[i]);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    m=0.5;s=1.5;
    Cauchydist normc=new Cauchydist(m,s);
    Ran myran = new Ran(17);
    sbeps=1.0e-14;
    localflag=false;
    for (i=0;i<1000;i++) {
      u=m-3.0*s+6.0*s*myran.doub();
      a=normc.cdf(u);
      b=normc.invcdf(a);
//      System.out.printf(setprecision(15) << u << " %f\n", b << " %f\n", abs(u-b));
      localflag = localflag || abs(u-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    m=0.5;s=1.5;
    Cauchydist normf=new Cauchydist(m,s);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Cauchydist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Fingerprint does not match expectations");
      
    }

    // Symmetry test
    localflag=false;
    for (i=0;i<N/2;i++)
      localflag = localflag || p[i] != p[N-1-i];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Cauchydist: Function does not have the right symmetry");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Cauchydist implements UniVarRealValueFun {
    double m,s;
    Cauchydist normi;
    func_Cauchydist(double mm, double ss) {
      m = mm;
      s =ss;
      normi = new Cauchydist(m,s);
    }
    public double funk(final double x) {
      return normi.p(x);
    }
  }
}
