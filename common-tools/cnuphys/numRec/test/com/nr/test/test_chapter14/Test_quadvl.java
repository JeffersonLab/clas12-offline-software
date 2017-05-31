package com.nr.test.test_chapter14;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Ran;
import com.nr.stat.Quadvl;

public class Test_quadvl {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,NTRIES=100;
    doubleW faW=new doubleW(0),fbW = new doubleW(0);
    doubleW fcW = new doubleW(0),fdW = new doubleW(0);
    double x,y,fa,fb,fc,fd,sbeps;
    boolean localflag,globalflag=false;

    

    // Test quadvl
    System.out.println("Testing quadvl");

    Ran myran = new Ran(16);
    for (i=0;i<NTRIES;i++) {
      x=2.0*myran.doub()-1.0;
      y=2.0*myran.doub()-1.0;
      Quadvl quadvl = new Quadvl();
      quadvl.quadvl(x,y,faW,fbW,fcW,fdW);
      fa=faW.val;fb=fbW.val;fc=fcW.val;fd=fdW.val;

      sbeps=1.e-15;
      localflag = abs(fa+fb+fc+fd-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** quadvl: Sum of reported fractions is not 1.0");
        
      }

      localflag = abs(fa-0.25*(1.0-x)*(1.0-y)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** quadvl: Quadrant 1 reporting wrong fraction");
        
      }

      localflag = abs(fb-0.25*(x+1.0)*(1.0-y)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** quadvl: Quadrant 2 reporting wrong fraction");
        
      }

      localflag = abs(fc-0.25*(x+1.0)*(y+1.0)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** quadvl: Sum of reported fractions is not 1.0");
        
      }

      localflag = abs(fd-0.25*(1.0-x)*(y+1.0)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** quadvl: Sum of reported fractions is not 1.0");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
