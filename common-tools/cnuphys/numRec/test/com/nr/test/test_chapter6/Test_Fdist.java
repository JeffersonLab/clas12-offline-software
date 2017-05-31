package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.fi.Midpnt.qromo;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midinf;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Fdist;
public class Test_Fdist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20;
    double pi,nu1,nu2,u,a,b,integral,sbeps=1.e-15;
    double xx[]={0.2,0.4,0.6,0.8,1.0,1.2,1.4,1.6,1.8,2.0,2.2,2.4,2.6,2.8,3.0,3.2,3.4,3.6,3.8,4.0};
    double ppexp[]={0.65903939205294171,0.58692958722433863,0.48156597192304679,
      0.39054186195729862,0.31830988618379047,0.26197671666380806,
      0.21795679194590994,0.18326522386704397,0.15563320920200663,
      0.13338019498623788,0.11526607892303817,0.10037116939735392,
      0.088007419281388721,0.077654806187662545,0.068916111927723928,
      0.061484724623014866,0.055121565493375241,0.049638410886319047,
      0.044885720465175881,0.040743665431525217};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Fdist
    System.out.println("Testing Fdist");

    pi=acos(-1.0);

    // Test special cases
    nu1=1.0; nu2=1.0; u=1.0;
    Fdist norm1=new Fdist(nu1,nu2);
    localflag = abs(norm1.p(u)-0.5/pi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fdist: Special case #1 failed");
      
    }

    nu1=1.0; nu2=2.0; u=1.0;
    Fdist norm2=new Fdist(nu1,nu2);
    localflag = abs(norm2.p(u)-1.0/pow(3.0,1.5)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fdist: Special case #2 failed");
      
    }

    nu1=2.0; nu2=1.0; u=1.0;
    Fdist norm3=new Fdist(nu1,nu2);
    localflag = abs(norm3.p(u)-1.0/pow(3.0,1.5)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fdist: Special case #3 failed");
      
    }

    nu1=2.0; nu2=2.0; u=1.0;
    Fdist norm4=new Fdist(nu1,nu2);
    localflag = abs(norm4.p(u)-0.25) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fdist: Special case #4 failed");
      
    }

    nu1=2.0; nu2=2.0; u=2.0;
    Fdist norm5=new Fdist(nu1,nu2);
    localflag = abs(norm5.p(u)-1.0/9.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fdist: Special case #5 failed");
      
    }

    // integral of distribution is one
    sbeps=5.e-7;
    nu1=5.0; nu2=5.0;
    func_Fdist dist = new func_Fdist(nu1,nu2);
    Midpnt q2 = new Midpnt(dist,0.0,10.0);
    Midinf q3 = new Midinf(dist,10.0,1.0e99);
    integral=qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fdist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-6;
    nu1=5.0; nu2=5.0;
    func_Fdist dist2 = new func_Fdist(nu1,nu2);
    Fdist normcdf=new Fdist(nu1,nu2);
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
      fail("*** Fdist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    nu1=5.0; nu2=5.0;
    Fdist normc=new Fdist(nu1,nu2);
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
      fail("*** Fdist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    nu1=3.0; nu2=3.0;
    Fdist normf=new Fdist(nu1,nu2);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Fdist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fdist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Fdist implements UniVarRealValueFun{
    double nu1,nu2;
    Fdist normi;
    
    func_Fdist(double nnu1, double nnu2) {
      nu1 = nnu1;
      nu2 = nnu2;
      normi = new Fdist(nu1,nu2);
    }
    
    public double funk(final double x) {
      return normi.p(x);
    }
  }
}
