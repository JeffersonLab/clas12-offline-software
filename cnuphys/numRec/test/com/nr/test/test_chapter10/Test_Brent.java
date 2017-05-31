package com.nr.test.test_chapter10;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.min.Brent;
import com.nr.sf.Bessjy;

public class Test_Brent implements UniVarRealValueFun{

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

    

    // Test Brent
    System.out.println("Testing Brent");

    Brent br = new Brent();
    for (i=0;i<N;i++) {
      a=i;
      b=a+span;
      br.bracket(a,b,this);
      min=br.minimize(this);    // Minimum of bessj0
      localflag = localflag || (abs(Bessj1_Brent(min)) > sbeps);
    }

    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Brent: Identified minimum is not a zero of the function derivative");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  public double funk(final double x) {
    return  Bessj0_Brent(x);
  }
  double Bessj0_Brent(final double x) {
    Bessjy b = new Bessjy();
    return(b.j0(x));
  }

  double Bessj1_Brent(final double x) {
    Bessjy b = new Bessjy();
    return(b.j1(x));
  }
}
