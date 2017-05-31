package com.nr.test.test_chapter6;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cosh;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midinf;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Logisticdist;
import static com.nr.fi.Midpnt.qromo;

public class Test_Logisticdist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=15;
    double pi=acos(-1.0),pio4sqrt3,m,s,u,a,b,integral,sbeps=1.e-15;
    double xx[]={-3.0,-2.5,-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0};
    double ppexp[]={0.017059092664760158,0.030497647063658831,0.053500875984749827,
      0.090801643627527795,0.14574392339674236,0.21404650800280242,
      0.27627401928012629,0.30229989403903629,0.27627401928012629,
      0.21404650800280242,0.14574392339674236,0.090801643627527795,
      0.053500875984749827,0.030497647063658831,0.017059092664760158};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Logisticdist
    System.out.println("Testing Logisticdist");

    pio4sqrt3=pi/4.0/sqrt(3.0);

    // Test special cases
    m=0; s=1; u=0;
    Logisticdist norm1 = new Logisticdist(m,s);
    localflag = abs(norm1.p(u)-pio4sqrt3/s) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Special case #1 failed");
      
    }

    m=1; s=1; u=m;
    Logisticdist norm2 = new Logisticdist(m,s);
    localflag = abs(norm2.p(u)-pio4sqrt3) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Special case #2 failed");
      
    }

    m=1; s=1; u=0;
    Logisticdist norm3 = new Logisticdist(m,s);
//    System.out.printf(abs(norm3.p(u) - pio4sqrt3*SQR(1.0/cosh(2.0*pio4sqrt3))));
    localflag = abs(norm3.p(u)-pio4sqrt3*SQR(1.0/cosh(2.0*pio4sqrt3))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Special case #3 failed");
      
    }

    m=1; s=2; u=1;
    Logisticdist norm4 = new Logisticdist(m,s);
    localflag = abs(norm4.p(u)-pio4sqrt3/s) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Special case #4 failed");
      
    }

    m=1; s=2; u=0;
    Logisticdist norm5 = new Logisticdist(m,s);
    localflag = abs(norm5.p(u)-pio4sqrt3/s/SQR(cosh(pio4sqrt3))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=1.e-8;
    m=1.0;s=2.0;
    func_Logisticdist dist = new func_Logisticdist(m,s);
    Midinf q1 = new Midinf(dist,-1.0e99,-1.0);
    Midpnt q2 = new Midpnt(dist,-1.0,1.0);
    Midinf q3 = new Midinf(dist,1.0,1.0e99);
    integral=qromo(q1)+qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-7;
    m=0.5;s=1.5;
    func_Logisticdist dist2 = new func_Logisticdist(m,s);
    Logisticdist normcdf = new Logisticdist(m,s);
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
      fail("*** Logisticdist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    m=0.5;s=1.5;
    Logisticdist normc = new Logisticdist(m,s);
    Ran myran = new Ran(17);
    sbeps=5.0e-14;
    localflag=false;
    for (i=0;i<1000;i++) {
      u=m-3.0*s+6.0*s*myran.doub();
      a=normc.cdf(u);
      b=normc.invcdf(a);
//      if (abs(u-b) > sbeps) {
//        System.out.printf(setprecision(15) << u << " %f\n", b << " %f\n", abs(u-b));
//      }
      localflag = localflag || abs(u-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    m=0.5;s=1.5;
    Logisticdist normf = new Logisticdist(m,s);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Logisticdist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Fingerprint does not match expectations");
      
    }

    // Symmetry test
    localflag=false;
    for (i=0;i<N/2;i++)
      localflag = localflag || p[i] != p[N-1-i];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdist: Function does not have the right symmetry");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Logisticdist implements UniVarRealValueFun{
    double m,s;
    Logisticdist normi;
    func_Logisticdist(double mm, double ss) {
      m = mm;
      s = ss;
      normi = new Logisticdist(m,s); 
    }
    
    public double funk(final double x) {
      return normi.p(x);
    }
  };
}
