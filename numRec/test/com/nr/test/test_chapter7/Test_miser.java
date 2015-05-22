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

import com.nr.RealValueFun;
import com.nr.ran.Miser;

public class Test_miser {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i;
    doubleW ave= new doubleW(0),var= new doubleW(0);
    double tgral,vol,sd,PI=acos(-1.0),R1=3.0,R2=1.0;
    double[] regn = new double[6];
    boolean localflag, globalflag=false;
    
    

    // Test miser
    System.out.println("Testing miser");

    regn[0]=0.0;
    regn[1]=-4.0;
    regn[2]=-1.0;
    regn[3]=4.0;
    regn[4]=4.0;
    regn[5]=1.0;
    vol=1.0;
    for (i=0;i<3;i++) vol *= (regn[i+3]-regn[i]);

    torusfunc1  torusfunc1 = new torusfunc1();
    // Test #1: Compute volume of hemitorus
    Miser.miser(torusfunc1,regn,1000000,0.0,ave,var);
    tgral=ave.val*vol;
    sd=sqrt(var.val)*vol;

//    System.out.printf(tgral << "  %f\n", sd);
//    System.out.printf(SQR(PI)*R1*SQR(R2));
    localflag = abs(tgral-SQR(PI)*R1*SQR(R2)) > 2.0*sd;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** miser: Computation of hemitoroid volume is inaccurate");
      
    }

    torusfunc2  torusfunc2 = new torusfunc2();
    // Test #2: Weight the integral by x
    Miser.miser(torusfunc2,regn,1000000,0.0,ave,var);
    tgral=ave.val*vol;
    sd=sqrt(var.val)*vol;

//    System.out.printf(tgral << "  %f\n", sd);
//    System.out.printf(2.0*PI*SQR(R1)*SQR(R2)*(1+SQR(R2/R1)/4.0));
    localflag = abs(tgral-2.0*PI*SQR(R1)*SQR(R2)*(1+SQR(R2/R1)/4.0)) > 2.0*sd;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** miser: Computation of hemitoroid weighted by x is inaccurate");
      
    }

    torusfunc3  torusfunc3 = new torusfunc3();
    // Test #3: Weight the integral by z^2
    Miser.miser(torusfunc3,regn,1000000,0.0,ave,var);
    tgral=ave.val*vol;
    sd=sqrt(var.val)*vol;

//    System.out.printf(tgral << "  %f\n", sd);
//    System.out.printf(SQR(PI)*R1*SQR(SQR(R2))/4.0);
    localflag = abs(tgral-SQR(PI)*R1*SQR(SQR(R2))/4.0) > 2.0*sd;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** miser: Computation of hemitoroid weighted by z^2 is inaccurate");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  
  class torusfunc1 implements RealValueFun {
    public double funk(final double[] x) {
      double R1 = 3.0, R2 = 1.0;

      if (SQR(x[2]) + SQR(sqrt(SQR(x[0]) + SQR(x[1])) - R1) <= R2)
        return 1.0;
      else
        return 0.;
    }
  }

  class torusfunc2 implements RealValueFun {
    public double funk(final double[] x) {
      double R1 = 3.0, R2 = 1.0;

      if (SQR(x[2]) + SQR(sqrt(SQR(x[0]) + SQR(x[1])) - R1) <= R2)
        return x[0];
      else
        return 0.;
    }
  }

  class torusfunc3 implements RealValueFun {
    public double funk(final double[] x) {
      double R1 = 3.0, R2 = 1.0;

      if (SQR(x[2]) + SQR(sqrt(SQR(x[0]) + SQR(x[1])) - R1) <= R2)
        return SQR(x[2]);
      else
        return 0.;
    }
  }
}
