package com.nr.test.test_chapter4;

import static com.nr.fi.Midpnt.qromo;
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

public class Test_qromo implements UniVarRealValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    // int i,j,M=14;
    double a,b,s,expect,PI=acos(-1.0),sbeps=1.e-11;
    boolean localflag, globalflag=false;
    
    

    // Test qromo
    System.out.println("Testing qromo");

    a=0.0;
    b=PI;
    Midpnt mpt = new Midpnt(this,a,b);
    s=qromo(mpt);

    expect=fint_qromo(b)-fint_qromo(a);
//    System.out.printf(setw(9) << expect << endl;

    System.out.printf("qromo: Maximum discrepancy = %f\n", abs(s-expect));
    localflag = abs(s-expect) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** qromo: Failure to achieve expected accuracy in improper integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
//Test function
public double funk(final double x)
{
 return sin(x);
}

//integral of test function
double fint_qromo(final double x)
{
 return -cos(x);
}

}
