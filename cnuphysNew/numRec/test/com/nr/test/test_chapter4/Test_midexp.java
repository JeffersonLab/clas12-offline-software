package com.nr.test.test_chapter4;

import static com.nr.fi.Midpnt.qromo;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midexp;

public class Test_midexp implements UniVarRealValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    // int i,j,M=14;
    double INFTY=1.e99;
    double a,b,s,expect,eps=3.0e-10,sbeps=1.e-6;
    boolean localflag, globalflag=false;
    
    

    // Test midexp
    System.out.println("Testing midexp");

    a=1.0;
    b=INFTY;
    Midexp midexp = new Midexp(this,a,b);
    s=qromo(midexp,eps);

    expect=2.0/exp(1.0);

//    System.out.printf(s << " %f\n", expect << endl;

    System.out.printf("midexp: Maximum discrepancy = %f\n", abs(s-expect));
    localflag = abs(s-expect) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** midexp: Failure to achieve expected accuracy in improper integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  //Test function
  public double funk(final double x) {
    return x*exp(-x);
  }
}
