package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.fi.Midpnt.qromo;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midinf;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Studenttdist;
public class Test_Studenttdist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=15;
    double pi=acos(-1.0),oneoverpi,m,s,n,u,a,b,integral,sbeps=1.e-15;
    double xx[]={-3.0,-2.5,-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0};
    double ppexp[]={0.0328216417673656,0.045360921162651474,0.063836610836904312,
      0.090793368604857677,0.12830005981991693,0.17443694974549953,
      0.21734122156158497,0.23570226039551601,0.21734122156158497,
      0.17443694974549953,0.12830005981991693,0.090793368604857677,
      0.063836610836904312,0.045360921162651474,0.0328216417673656};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Studenttdist
    System.out.println("Testing Studenttdist");

    oneoverpi=1.0/pi;

    // Test special cases
    n=1.0;m=0.0;s=1.0;u=0.0;
    Studenttdist norm1 = new Studenttdist(n,m,s);
//    System.out.printf(norm1.p(u) << " %f\n", oneoverpi << " %f\n", norm1.p(u)-oneoverpi);
    localflag = abs(norm1.p(u)-oneoverpi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Special case #1 failed");
      
    }

    n=1.0;m=1.0;s=1.0;u=m;
    Studenttdist norm2 = new Studenttdist(n,m,s);
    localflag = abs(norm2.p(u)-oneoverpi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Special case #2 failed");
      
    }

    n=1.0;m=1.0;s=1.0;u=0.0;
    Studenttdist norm3 = new Studenttdist(n,m,s);
    localflag = abs(norm3.p(u)-oneoverpi/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Special case #3 failed");
      
    }

    n=1.0;m=1.0;s=2.0;u=1.0;
    Studenttdist norm4 = new Studenttdist(n,m,s);
    localflag = abs(norm4.p(u)-oneoverpi/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Special case #4 failed");
      
    }

    n=1.0;m=1.0;s=2.0;u=0.0;
    Studenttdist norm5 = new Studenttdist(n,m,s);
    localflag = abs(norm5.p(u)-oneoverpi/2.0/(1+1.0/4.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=1.e-10;
    n=2.0;m=1.0;s=2.0;
    func_Studenttdist dist = new func_Studenttdist(n,m,s);
    Midinf q1 = new Midinf(dist,-1.0e99,-1.0);
    Midpnt q2 = new Midpnt(dist,-1.0,1.0);
    Midinf q3 = new Midinf(dist,1.0,1.0e99);
    integral=qromo(q1)+qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-8;
    n=2.0;m=0.5;s=1.5;
    func_Studenttdist dist2 = new func_Studenttdist(n,m,s);
    Studenttdist normcdf = new Studenttdist(n,m,s);
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
//      System.out.printf(c[i] << " %f\n", d[i] << " %f\n", c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    n=2.0;m=0.5;s=1.5;
    Studenttdist normc = new Studenttdist(n,m,s);
    Ran myran = new Ran(17);
    sbeps=1.0e-13;
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
      fail("*** Studenttdist: Inverse cdf does not accurately invert the cdf");
      
    }

    // Function aa() agrees with incomplete integral
    sbeps=1.e-7;
    n=2.0;m=0.0;s=1.0;
    func_Studenttdist dist3 = new func_Studenttdist(n,m,s);
    Studenttdist normaa = new Studenttdist(n,m,s);
    localflag=false;
    for (i=0;i<10;i++) {
      u = 0.5*i;
      Midpnt qq1 = new Midpnt(dist3,-u,u);
      c[i]=qromo(qq1);
      d[i]=normaa.aa(u);
//      System.out.printf(setprecision(6) << c[i] << " %f\n", d[i] << " %f\n", c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: aa() does not agree with result of direct quadrature");
      
    }

    // inverse invaa() agrees with aa()
    n=2.0;m=0.5;s=1.5;
    Studenttdist normaa2 = new Studenttdist(n,m,s);
    sbeps=1.0e-13;
    localflag=false;
    for (i=0;i<1000;i++) {
      u=m+3.0*s*myran.doub();
      a=normaa2.aa(u);
      b=normaa2.invaa(a);
//      System.out.printf(setprecision(15) << u << " %f\n", b << " %f\n", abs(u-b));
      localflag = localflag || abs(u-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Inverse invaa() does not accurately invert aa()");
      
    }

    // Fingerprint test
    n=2.0;m=0.5;s=1.5;
    Studenttdist normf = new Studenttdist(n,m,s);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Studenttdist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Fingerprint does not match expectations");
      
    }

    // Symmetry test
    localflag=false;
    for (i=0;i<N/2;i++)
      localflag = localflag || p[i] != p[N-1-i];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Studenttdist: Function does not have the right symmetry");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Studenttdist implements UniVarRealValueFun{
    double n,m,s;
    Studenttdist normi;
    func_Studenttdist(double nn, double mm, double ss) {
      n = nn;m = mm; s=ss;
      normi = new Studenttdist(nn,mm,ss);
    };
    public double funk(final double x) {
      return normi.p(x);
    }
  }
}
