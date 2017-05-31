package com.nr.test.test_chapter4;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midpnt;

public class Test_midpnt {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int j,M=14;
    double a,b,s=0,expect,PI=acos(-1.0),sbeps=1.e-10;
    boolean localflag, globalflag=false;
    
    

    // Test midpnt
    System.out.println("Testing midpnt");

//    a=0.0;
//    b=1.0;
    a=0.0;
    b=PI;
    func_midpnt func_midpnt = new func_midpnt();
    Midpnt mpt = new Midpnt(func_midpnt,a,b);

    for (j=0;j<M;j++) {
      s=mpt.next();
//      System.out.printf(setw(6) << j << setw(24) << s << endl;
    }

    expect=fint_midpnt(b)-fint_midpnt(a);
//    System.out.printf(setw(9) << expect << endl;

    System.out.printf("midpnt: Maximum discrepancy = %f\n", abs(s-expect));
    localflag = abs(s-expect) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** midpnt: Failure to achieve expected accuracy in improper integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  class func_midpnt implements UniVarRealValueFun {
    public double funk(final double x) {
      return sin(x);
    }
  }
  
  //integral of test function
  double fint_midpnt(final double x)
  {
     return -cos(x);
  }
 
}
