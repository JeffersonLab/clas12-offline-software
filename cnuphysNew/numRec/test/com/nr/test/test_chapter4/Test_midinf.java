package com.nr.test.test_chapter4;

import static com.nr.fi.Midpnt.qromo;
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

public class Test_midinf {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    // int i,j,M=14;
    double INFTY=1.e20,PI=acos(-1.0);
    double a,b,s1,s2,expect,sbeps=1.e-6;
    boolean localflag, globalflag=false;
    
    

    // Test midinf
    System.out.println("Testing midinf");

    expect=sqrt(PI)/2.0/exp(1.0);

    a=-INFTY;
    b=-1.0;
    func_midinf1 func_midinf1 =new func_midinf1();
    Midinf midinf1 = new Midinf(func_midinf1,a,b);
    s1=qromo(midinf1);

    System.out.printf("midinf,case 1: Maximum discrepancy = %f\n", abs(s1-expect));
    localflag = abs(s1-expect) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** midinf,case 1: Failure to achieve expected accuracy in improper integral");
      
    }

    a=1.0;
    b=INFTY;
    func_midinf2 func_midinf2 = new func_midinf2();
    Midinf midinf2 = new Midinf(func_midinf2,a,b);
    s2=qromo(midinf2);

    System.out.printf("midinf,case 2: Maximum discrepancy = %f\n", abs(s2-expect));
    localflag = abs(s2-expect) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** midinf,case 2: Failure to achieve expected accuracy in improper integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  // Test function
  class func_midinf1 implements UniVarRealValueFun {
    public double funk(final double x) {
      return sqrt(-x - 1) * exp(x);
    }
  }

  class func_midinf2 implements UniVarRealValueFun {
    public double funk(final double x) {
      return sqrt(x - 1) * exp(-x);
    }
  }
}
