package com.nr.test.test_chapter7;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.VEGAS;

public class Test_vegas {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    doubleW tgral= new doubleW(0),sd= new doubleW(0),chi2a= new doubleW(0);
    double R1=3.0,R2=1,PI=acos(-1.0);
    double[] regn =new double[6]; 
    boolean localflag, globalflag=false;
    
    

    // Test vegas
    System.out.println("Testing vegas");

    regn[0]=0.0;
    regn[1]=-4.0;
    regn[2]=-1.0;
    regn[3]=4.0;
    regn[4]=4.0;
    regn[5]=1.0;

    // Test #1: Compute volume of hemitorus
    VEGAS vegas = new VEGAS() {
      public double fxn(final double[] x, final double wgt){
        return torusfunc1(x, wgt);
      }
    };
    vegas.vegas(regn,0,1000,100,-1,tgral,sd,chi2a);
//    System.out.printf(chi2a);
    localflag = chi2a.val > 1.2;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** vegas: chi-square for test #1 is too high");
      
    }

    vegas.vegas(regn,1,100000,1,-1,tgral,sd,chi2a);

//    System.out.printf(tgral << " %f\n", sd << " %f\n", chi2a);
//    System.out.printf(SQR(PI)*R1*SQR(R2));
    localflag = abs(tgral.val-SQR(PI)*R1*SQR(R2)) > 2.0*sd.val;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** vegas: Computation of hemitoroid volume is inaccurate");
      
    }

    // Test #2: Weight the integral by x
    vegas = new VEGAS() {
      public double fxn(final double[] x, final double wgt){
        return torusfunc2(x, wgt);
      }
    };
    vegas.vegas(regn,0,1000,100,-1,tgral,sd,chi2a);
//    System.out.printf(chi2a);
    localflag = chi2a.val > 1.2;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** vegas: chi-square for test #2 is too high");
      
    }

    vegas.vegas(regn,1,100000,1,-1,tgral,sd,chi2a);

//    System.out.printf(tgral << " %f\n", sd << " %f\n", chi2a);
//    System.out.printf(2.0*PI*SQR(R1)*SQR(R2)*(1+SQR(R2/R1)/4.0));
    localflag = abs(tgral.val-2.0*PI*SQR(R1)*SQR(R2)*(1+SQR(R2/R1)/4.0)) > 2.0*sd.val;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** vegas: Computation of hemitoroid weighted by x is inaccurate");
      
    }

    // Test #3: Weight the integral by z^2
    vegas = new VEGAS() {
      public double fxn(final double[] x, final double wgt){
        return torusfunc3(x, wgt);
      }
    };
    vegas.vegas(regn,0,1000,100,-1,tgral,sd,chi2a);
//    System.out.printf(chi2a);
    localflag = chi2a.val > 1.2;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** vegas: chi-square for test #3 is too high");
      
    }

    vegas.vegas(regn,1,100000,1,-1,tgral,sd,chi2a);

//    System.out.printf(tgral << " %f\n", sd << " %f\n", chi2a);
//    System.out.printf(SQR(PI)*R1*SQR(SQR(R2))/4.0);
    localflag = abs(tgral.val-SQR(PI)*R1*SQR(SQR(R2))/4.0) > 2.0*sd.val;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** vegas: Computation of hemitoroid weighted by z^2 is inaccurate");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  double torusfunc1(final double[] x, final double wgt) {
    double R1=3.0,R2=1.0;

    if (SQR(x[2])+SQR(sqrt(SQR(x[0])+SQR(x[1]))-R1) <= R2) return 1.0;
    else return 0.;
  }

  double torusfunc2(final double[] x, final double wgt) {
    double R1=3.0,R2=1.0;

    if (SQR(x[2])+SQR(sqrt(SQR(x[0])+SQR(x[1]))-R1) <= R2) return x[0];
    else return 0.;
  }

  double torusfunc3(final double[] x, final double wgt) {
    double R1=3.0,R2=1.0;

    if (SQR(x[2])+SQR(sqrt(SQR(x[0])+SQR(x[1]))-R1) <= R2) return SQR(x[2]);
    else return 0.;
  }
}
