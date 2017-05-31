package com.nr.test.test_chapter4;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Adapt;
import com.nr.sf.Bessjy;

public class Test_Adapt {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10;
    double x1,x2,sbeps=1.e-14;
    double[] y= new double[N],yy= new double[N];
    boolean localflag, globalflag=false;

    // Test Adapt
    System.out.println("Testing Adapt");
    Bessj0_Adapt bj0 = new Bessj0_Adapt();
    Bessj1_Adapt bj1 = new Bessj1_Adapt();
    Adapt adp = new Adapt(0.0);
    for (i=0;i<N;i++) {
      x1=1.0*i;
      x2=x1+2.0;
      y[i]=adp.integrate(bj1,x1,x2);
      yy[i]=bj0.funk(x1)-bj0.funk(x2);
    }
    System.out.printf("Adapt: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Adapt: Failure to achieve advertised accuracy in integral");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  class Bessj0_Adapt implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b = new Bessjy();
      return(b.j0(x));
    }
  };

  class Bessj1_Adapt implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b = new Bessjy();
      return(b.j1(x));
    }
  };

}
