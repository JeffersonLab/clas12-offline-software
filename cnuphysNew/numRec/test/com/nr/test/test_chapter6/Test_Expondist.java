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
import com.nr.sf.Expondist;
import static com.nr.fi.Midpnt.qromo;

public class Test_Expondist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=21;
    double beta,u,a,b,integral,sbeps=1.e-15;
    double xx[]={0.0,0.2,0.4,0.6,0.8,1.0,1.2,1.4,1.6,1.8,2.0,2.2,2.4,2.6,2.8,3.0,3.2,3.4,3.6,3.8,4.0};
    double ppexp[]={1.5,1.1112273310225769,0.82321745414103953,
      0.60985448961089872,0.45179131786830307,0.33469524022264474,
      0.24794833233237984,0.18368464237947291,0.13607692993411871,
      0.10080826910962463,0.074680602551795913,0.055324751101859991,
      0.040985583670938852,0.030362867168706571,0.022493365230716576,
      0.016663494807363458,0.012344620573530035,0.0091451198482734569,
      0.0067748714139189989,0.0050189481862069124,0.0037181282649995377};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Expondist
    System.out.println("Testing Expondist");

    // Test special cases
    beta=1.0; u=0.0;
    Expondist norm1=new Expondist(beta);
    localflag = abs(norm1.p(u)-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondist: Special case #1 failed");
      
    }

    beta=1.0; u=1.0;
    Expondist norm2=new Expondist(beta);
    localflag = abs(norm2.p(u)-exp(-1.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondist: Special case #2 failed");
      
    }

    beta=2.0; u=0.0;
    Expondist norm3=new Expondist(beta);
    localflag = abs(norm3.p(u)-2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondist: Special case #3 failed");
      
    }

    beta=2.0; u=1.0;
    Expondist norm4=new Expondist(beta);
    localflag = abs(norm4.p(u)-2.0*exp(-2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondist: Special case #4 failed");
      
    }

    // integral of distribution is one
    sbeps=1.e-8;
    beta=1.5;
    func_Expondist dist =new func_Expondist(beta);
    Midpnt q2 = new Midpnt(dist,0.0,4.0);
    Midinf q3 = new Midinf(dist,4.0,1.0e99);
    integral=qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-7;
    beta=1.5;
    func_Expondist dist2 = new func_Expondist(beta);
    Expondist normcdf=new Expondist(beta);
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
      fail("*** Expondist: cdf does not agree with result of quadrature");
    }

    // inverse cdf agrees with cdf
    beta=1.5;
    Expondist normc=new Expondist(beta);
    Ran myran=new Ran(17);
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
      fail("*** Expondist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    beta=1.5;
    Expondist normf=new Expondist(beta);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Expondist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Expondist implements UniVarRealValueFun{
    double beta;
    Expondist normi;
    func_Expondist(double bbeta) {
      beta = bbeta;
      normi = new Expondist(beta);
    }
    
    public double funk(final double x) {
      return normi.p(x);
    }
  }
}
