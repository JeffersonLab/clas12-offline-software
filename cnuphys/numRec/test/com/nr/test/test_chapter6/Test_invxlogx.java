package com.nr.test.test_chapter6;

import static com.nr.sf.Integrals.invxlogx;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;

public class Test_invxlogx {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=100000;
    double x,y1,y2,sbeps=1.0e-15;
    boolean localflag, globalflag=false;

    

    // Test invxlogx
    System.out.println("Testing invxlogx");

    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      y1 = -exp(-1.0)*myran.doub();
//      System.out.printf(setprecision(17) << y1;
      x=invxlogx(y1);
      y2=x*log(x);
//      System.out.println(" %f\n", x << " %f\n", y2);

      localflag = abs(y1-y2)>sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** invxlogx: Incorrect function value for y=y1");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
