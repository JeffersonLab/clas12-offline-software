package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
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
import com.nr.ran.Ran;
import com.nr.sf.Normaldist;
import static com.nr.fi.Trapzd.qsimp;

public class Test_Normaldist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=15;
    double pi=acos(-1.0),invsq2pi,m,s,u,a,b,sbeps;
    double xx[]={-3.0,-2.5,-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0};
    double ppexp[]={0.017481259395806324,0.035993977675458706,0.066318092528499115,
      0.10934004978399577,0.1613138163460956,0.2129653370149015,
      0.25158881846199549,0.26596152026762182,0.25158881846199549,
      0.2129653370149015,0.1613138163460956,0.10934004978399577,
      0.066318092528499115,0.035993977675458706,0.017481259395806324};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Normaldist
    System.out.println("Testing Normaldist");

    invsq2pi=1.0/sqrt(2.0*pi);

    // Test special cases
    sbeps=1.e-15;
    m=0; s=1; u=0;
    Normaldist norm1 = new Normaldist(m,s);
    localflag = localflag || abs(norm1.p(u) - invsq2pi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Special case #1 failed");
      
    }

    m=1; s=1; u=m;
    Normaldist norm2 = new Normaldist(m,s);
    localflag = localflag || abs(norm2.p(u) - invsq2pi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Special case #2 failed");
      
    }

    m=1; s=1; u=0;
    Normaldist norm3 = new Normaldist(m,s);
    localflag = localflag || abs(norm3.p(u) - invsq2pi*exp(-0.5)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Special case #3 failed");
      
    }

    m=1; s=2; u=1;
    Normaldist norm4 = new Normaldist(m,s);
    localflag = localflag || abs(norm4.p(u) - invsq2pi/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Special case #4 failed");
      
    }

    m=1; s=2; u=0;
    Normaldist norm5 = new Normaldist(m,s);
    localflag = localflag || abs(norm5.p(u) - invsq2pi/2.0*exp(-1.0/8.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=1.e-10;
    m=1.0;s=2.0;
    func_Normaldist dist = new func_Normaldist(m,s);
    localflag = abs(1.0-qsimp(dist,-20.0,20.0)) > sbeps;
    //  System.out.printf(setprecision(15) << 1.0-qsimp(dist,-20.0,20.0));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-10;
    m=0.5;s=1.5;
    func_Normaldist dist2 =new func_Normaldist(m,s);
    Normaldist normcdf = new Normaldist(m,s);
    for (i=0;i<N;i++) {
      c[i]=qsimp(dist2,-20.0,x[i]);
      d[i]=normcdf.cdf(x[i]);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: cdf does not agree with result of quadrature");
      
    }
    
    // inverse cdf agrees with cdf
    m=0.5;s=1.5;
    Normaldist normc = new Normaldist(m,s);
    Ran myran = new Ran(17);
    sbeps=5.0e-14;
    for (i=0;i<1000;i++) {
      u=m-3.0*s+6.0*s*myran.doub();
      a=normc.cdf(u);
      b=normc.invcdf(a);
//      System.out.printf(setprecision(15) << u << " %f\n", b << " %f\n", abs(u-b));
      localflag = localflag || abs(u-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    m=0.5;s=1.5;
    Normaldist normf = new Normaldist(m,s);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Normaldist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = localflag || maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Fingerprint does not match expectations");
      
    }

    // Symmetry test
    for (i=0;i<N/2;i++)
      localflag = localflag || p[i] != p[N-1-i];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldist: Function does not have the right symmetry");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Normaldist implements UniVarRealValueFun{
    double m,s;
    Normaldist normi;
    func_Normaldist(double mm, double ss) {
      m=mm;s=ss;
      normi = new Normaldist(m,s);
    };
    public double funk(final double x) {
      return normi.p(x);
    }
  };

}
