package com.nr.test.test_chapter4;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.RealValueFun;
import com.nr.fi.DErule;

public class Test_derule {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,M=10;
    double PI=acos(-1.0); // ,INFTY=1.0e99;
    double a,b,s=0,sold,expect,sbeps;
    boolean localflag, globalflag=false;
    
    // Test DErule
    System.out.println("Testing DErule");

    a=0.0;
    b=1.0;
    func_DErule func_DErule = new func_DErule();
    DErule derule = new DErule(func_DErule,a,b);
    expect=2.0-PI*PI/6.0;

    sold=derule.next();
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      s=derule.next();
      if (abs(s-sold) < sbeps) break;
      else sold=s;
    }

    System.out.printf("DErule,case 1: Maximum discrepancy = %f\n", abs(s-expect));
    localflag = abs(s-expect) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** DErule,case 2: Failure to achieve expected accuracy in improper integral");
      
    }

    a=0.0;
    b=1.0;
    
    func_DErule2 func_DErule2 = new func_DErule2();
    DErule derule2 = new DErule(func_DErule2,a,b,4.5);
    expect=PI;

    sold=derule2.next();
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      s=derule2.next();
      if (abs(s-sold) < sbeps) break;
      else sold=s;
    }

    System.out.printf("DErule,case 2: Maximum discrepancy = %f\n", abs(s-expect));
    localflag = abs(s-expect) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** DErule,case 2: Failure to achieve expected accuracy in improper integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  //Test functions
  class func_DErule implements RealValueFun {
    public double funk(double[] x) {//final double x, final double delta)
      return log(x[0])*log(1.0-x[0]); 
    }
  }
  
  class func_DErule2 implements RealValueFun {
    public double funk(final double[] xx) { //final double x, final double delta)
      double x = xx[0], delta = xx[1]; 
      if (x < 0.1) return (1.0/sqrt(delta)/sqrt(1.0-x));
      else if (x > 0.9) return (1.0/sqrt(x)/sqrt(delta));
      else return (1.0/sqrt(x)/sqrt(1.0-x));
    }
  }


}
