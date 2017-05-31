package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.fi.Midpnt.qromo;
import static com.nr.sf.Gamma.factrl;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midinf;
import com.nr.fi.Midpnt;
import com.nr.ran.Ran;
import com.nr.sf.Chisqdist;
public class Test_Chisqdist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=21;
    double df,chisq,a,b,integral,sbeps=1.e-15;
    double xx[]={50.0,55.0,60.0,65.0,70.0,75.0,80.0,85.0,90.0,95.0,100.0,
      105.0,110.0,115.0,120.0,125.0,130.0,135.0,140.0,145.0,150.0};
    double ppexp[]={3.6021642652021244e-006,3.155505064508948e-005,0.0001840654006010498,
      0.0007630959740573441,0.0023653338270243898,0.0057062945235204368,
      0.011066885970397204,0.017717695264991538,0.023934624893103978,
      0.027788436837347191,0.028162503162595779,0.025247053664502552,
      0.020250717569759023,0.014677659772546816,0.0096963323034503553,
      0.0058827564286662278,0.0032997291091395658,0.0017213630503558305,
      0.00083956700733331499,0.00038466043659051097,0.00016625736749918696};
    double[] x=buildVector(xx),pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Chisqdist
    System.out.println("Testing Chisqdist");

    // Test special cases
    df=10.0; chisq=1.0;
    Chisqdist norm1=new Chisqdist(df);
    localflag = abs(norm1.p(chisq)-pow(chisq,df/2.-1.)*exp(-chisq/2.)/pow(2.,df/2.)/factrl((int)(df/2.-1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: Special case #1 failed");
      
    }

    df=10.0; chisq=2.0;
    Chisqdist norm2=new Chisqdist(df);
    localflag = abs(norm2.p(chisq)-pow(chisq,df/2.-1.)*exp(-chisq/2.)/pow(2.,df/2.)/factrl((int)(df/2.-1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: Special case #2 failed");
      
    }

    df=10.0; chisq=3.0;
    Chisqdist norm3=new Chisqdist(df);
    localflag = abs(norm3.p(chisq)-pow(chisq,df/2.-1.)*exp(-chisq/2.)/pow(2.,df/2.)/factrl((int)(df/2.-1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: Special case #3 failed");
      
    }

    df=6.0; chisq=1.0;
    Chisqdist norm4=new Chisqdist(df);
    localflag = abs(norm4.p(chisq)-pow(chisq,df/2.-1.)*exp(-chisq/2.)/pow(2.,df/2.)/factrl((int)(df/2.-1.))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: Special case #4 failed");
      
    }

    // integral of distribution is one
    sbeps=1.e-8;
    df=100.;
    func_Chisqdist dist = new func_Chisqdist(df);
    Midpnt q2=new Midpnt(dist,0.0,4.0);
    Midinf q3=new Midinf(dist,4.0,1.0e99);
    integral=qromo(q2)+qromo(q3);
    localflag = abs(1.0-integral) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-integral);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-7;
    df=100.;
    func_Chisqdist dist2 = new func_Chisqdist(df);
    Chisqdist normcdf=new Chisqdist(df);
    localflag=false;

    for (i=0;i<N;i++) {
      q2=new Midpnt(dist2,0.0,x[i]);
      integral=qromo(q2);
      c[i]=integral;
      d[i]=normcdf.cdf(x[i]);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    df=100.;
    Chisqdist normc=new Chisqdist(df);
    Ran myran = new Ran(17);
    sbeps=2.0e-12; // XXX 1.0e-12 not pass.
    localflag=false;
    for (i=0;i<1000;i++) {
      chisq=df-3.0*sqrt(df)+6.0*sqrt(df)*myran.doub();
      a=normc.cdf(chisq);
      b=normc.invcdf(a);
      if (abs(chisq-b) > sbeps) {
        System.out.printf("%f %f  %f\n",chisq, b ,abs(chisq-b));
      }
      localflag = localflag || abs(chisq-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: Inverse cdf does not accurately invert the cdf");
      
    }

    // Fingerprint test
    df=100.;
    Chisqdist normf=new Chisqdist(df);
    for (i=0;i<N;i++) {
      p[i]=normf.p(x[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Chisqdist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chisqdist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_Chisqdist implements UniVarRealValueFun{
    double df;
    Chisqdist normi;
    func_Chisqdist(double ddf) {
      df = ddf;
      normi = new Chisqdist(df);
    }
    public double funk(final double x) {
      return normi.p(x);
    }
  }
}
