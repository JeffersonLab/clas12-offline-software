package com.nr.test.test_chapter10;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniValRealValueFunWithDiff;
import com.nr.min.Dbrent;
import com.nr.sf.Bessjy;

public class Test_Dbrent {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=50;
    double a,b,min,span=1.0,sbeps=1.0e-7;
    boolean localflag=false, globalflag=false;

    

    // Test Dbrent
    System.out.println("Testing Dbrent");

    Funcd_Dbrent funcd = new Funcd_Dbrent();
    Dbrent dbr = new Dbrent();
    for (i=0;i<N;i++) {
      a=i;
      b=a+span;
      dbr.bracket(a,b,funcd);
      min=dbr.minimize(funcd);    // Minimum of bessj0
      localflag = localflag || (abs(funcd.df(min)) > sbeps);
    }

    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Dbrent: Identified minimum is not a zero of the function derivative");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Funcd_Dbrent implements UniValRealValueFunWithDiff{
    Bessjy b = new Bessjy();
    public double funk (final double x) {
      return b.j0(x);
    }
    public double df(final double x) {
      return -b.j1(x);
    }
  };
}
