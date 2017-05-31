package com.nr.test.test_chapter4;

import static com.nr.NRUtil.SQR;
import static com.nr.fi.NRf3.quad3d;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.RealValueFun;

public class Test_quad3d {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double s,expect,sbeps,PI=acos(-1.0);
    boolean localflag, globalflag=false;
    
    

    // Test quad3d
    System.out.println("Testing quad3d");
    func func = new func();
    func2 func2 = new func2();
    y1 y1 = new y1();
    y2 y2 = new y2();
    z1 z1 = new z1();
    z2 z2 = new z2();
    
    s=quad3d(func,-1.0,1.0,y1,y2,z1,z2);
    expect=2.0*PI/3.0;

//    System.out.printf(s << " %f\n", expect << endl;

    sbeps=1.e-3;
//    System.out.printf(fabs(s-expect)/expect << endl;
    localflag = abs(s-expect)/expect > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** quad3d: integral of hemispherical volume was inaccurate");
      
    }

    s=quad3d(func2,-1.0,1.0,y1,y2,z1,z2);
    expect=2.0*PI*(2.0-5.0/exp(1.0));

//    System.out.printf(s << " %f\n", expect << endl;

    sbeps=1.e-3;
//    System.out.printf(abs(s-expect)/expect << endl;
    localflag = abs(s-expect)/expect > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** quad3d: Weighted integral over hemispherical volume was inaccurate");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  class y1 implements RealValueFun {
    public double funk(final double[] xx) {
      double x = xx[0];
      return -sqrt(1.0 - SQR(x));
    }
  }

  class y2 implements RealValueFun {
    public double funk(final double[] xx) {
      double x = xx[0];
      return sqrt(1.0 - SQR(x));
    }
  }

  class z1 implements RealValueFun {
    public double funk(final double[] x) {
      return 0.0;
    }
  }

  class z2 implements RealValueFun {
    public double funk(final double[] xx) {
      double x = xx[0], y = xx[1];

      return sqrt(1.0 - SQR(x) - SQR(y));
    }
  }

  class func implements RealValueFun {
    public double funk(final double[] x) {
      return 1.0;
    }
  }

  class func2 implements RealValueFun {
    public double funk(final double[] xx) {
      double x = xx[0], y = xx[1], z = xx[2];

      double r;

      r = sqrt(SQR(x) + SQR(y) + SQR(z));
      return exp(-r);
    }
  }

}
