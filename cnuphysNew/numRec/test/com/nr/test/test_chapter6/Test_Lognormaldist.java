package com.nr.test.test_chapter6;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static com.nr.fi.Midpnt.qromo;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midinf;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Lognormaldist;
public class Test_Lognormaldist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=21;
    double m,s,pi,oneoversqt2pi,u,a,b,integral,sbeps=1.e-15;
    double xx[]={0.0,0.2,0.4,0.6,0.8,1.0,1.2,1.4,1.6,1.8,2.0,2.2,2.4,2.6,2.8,3.0,3.2,3.4,3.6,3.8,4.0};
    double ppexp[]={0.0,0.49470470923655735,0.42576663180490482,
      0.35322936591651288,0.29597857104402842,0.25158881846199549,
      0.21671941633363451,0.18884694712205952,0.16619271634309699,
      0.14750357570255507,0.13188288209943969,0.11867678301708297,
      0.10739941429814887,0.097683352787360206,0.089246268876079712,
      0.081868064126928805,0.075374904208942842,0.069627868758855266,
      0.064514749064051538,0.059944029558968229,0.055840409331978126};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Lognormaldist
    System.out.println("Testing Lognormaldist");

    pi=acos(-1.0);
    oneoversqt2pi=1.0/sqrt(2.0*pi);

    // Test special cases
    m=0.0; s=1.0; u=exp(0.0);
    Lognormaldist norm1 = new Lognormaldist(m,s);
    localflag = abs(norm1.p(u)-oneoversqt2pi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: Special case #1 failed");
      
    }

    m=0.0; s=1.0; u=exp(1.0);
    Lognormaldist norm2 = new Lognormaldist(m,s);
//    System.out.printf(abs(norm2.p(u)-oneoversqt2pi*exp(-0.5)/u));
    localflag = abs(norm2.p(u)-oneoversqt2pi*exp(-0.5)/u) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: Special case #2 failed");
      
    }

    m=1.0; s=1.0; u=exp(0.0);
    Lognormaldist norm3 = new Lognormaldist(m,s);
//    System.out.printf(abs(norm3.p(u)-oneoversqt2pi*exp(-0.5)/u));
    localflag = abs(norm3.p(u)-oneoversqt2pi*exp(-0.5)/u) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: Special case #3 failed");
      
    }

    m=1.0; s=1.0; u=exp(m);
    Lognormaldist norm4 = new Lognormaldist(m,s);
    localflag = abs(norm4.p(u)-oneoversqt2pi/u) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: Special case #4 failed");
      
    }

    m=1.0; s=2.0; u=exp(0.0);
    Lognormaldist norm5 = new Lognormaldist(m,s);
    localflag = abs(norm5.p(u)-oneoversqt2pi*exp(-0.5/SQR(s))/s) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=5.e-8;
    m=0.5; s=1.5;
    func_Lognormaldist dist= new func_Lognormaldist(m,s);
    Midpnt q2 = new Midpnt(dist,0.0,10.0);
    Midinf q3 = new Midinf(dist,10.0,1.0e99);
    integral=qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=2.e-7;
    m=0.5; s=1.5;
    func_Lognormaldist dist2= new func_Lognormaldist(m,s);
    Lognormaldist normcdf = new Lognormaldist(m,s);
    localflag=false;
    for (i=0;i<N;i++) {
      q2 = new Midpnt(dist2,0.0,x[i]);
      integral=qromo(q2);
      c[i]=integral;
      d[i]=normcdf.cdf(x[i]);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    m=0.5; s=1.5;
    Lognormaldist normc = new Lognormaldist(m,s);
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
      fail("*** Lognormaldist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    m=0.5; s=1.5;
    Lognormaldist normf = new Lognormaldist(m,s);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Lognormaldist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Lognormaldist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Lognormaldist implements UniVarRealValueFun{
    double m,s;
    Lognormaldist normi;
    func_Lognormaldist(double mm, double ss) {
      m=mm; s=ss;
      normi = new Lognormaldist(m,s);
    }
    public double funk(final double x) {
      return normi.p(x);
    }
  }
}
