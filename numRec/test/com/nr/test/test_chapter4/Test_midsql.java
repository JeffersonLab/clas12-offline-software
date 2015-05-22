package com.nr.test.test_chapter4;

import static com.nr.fi.Midpnt.qromo;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Midsql;

public class Test_midsql implements UniVarRealValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    // int i,j,M=14;
    double PI=acos(-1.0);
    double a,b,s,sbeps=1.e-10; // expect
    boolean localflag, globalflag=false;
    
    

    // Test Nidsql
    System.out.println("Testing Midsql");

    a=0.0;
    b=0.5;
    Midsql msql = new Midsql(this,a,b);
    s=qromo(msql);

//    System.out.printf(s << endl;

    System.out.printf("Midsql: Maximum discrepancy = %f\n", abs(s-PI/2.0));
    localflag = abs(s-PI/2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Midsql: Failure to achieve expected accuracy in improper integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  // Test function
  public double funk(final double x) {
    return 1.0 / sqrt(x) / sqrt(1.0 - x);
  }
}
