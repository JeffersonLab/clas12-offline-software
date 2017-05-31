package com.nr.test.test_chapter15;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.model.Fitmed;
import com.nr.ran.Ran;

public class Test_Fitmed {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=100,M=10;
    double pi=acos(-1.0),sbeps;
    double[] x= new double[N],y= new double[N],yy= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Fitmed
    System.out.println("Testing Fitmed");

    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      x[i]=10.0*myran.doub();
      y[i]=sqrt(2.0)+pi*x[i];
    }

    Fitmed fit1 = new Fitmed(x,y);   // Perfect fit, no noise

//    System.out.printf(fit1.a << " %f\n", fit1.b << " %f\n", fit1.abdev);

    sbeps=1.e-12;
    localflag = abs(fit1.a-sqrt(2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmed: Fitted constant term a has incorrect value");
      
    }

    localflag = abs(fit1.b-pi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmed: Fitted slope b has incorrect value");
      
    }

    localflag = fit1.abdev > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmed: abdev not zero for perfect linear data");
      
    }

    // Test 2
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        x[i]=10.0*i/(N-1);
        yy[i]=sqrt(2.0)+pi*x[i]+0.1*j/M*(i%2-0.5);
      // All points have same absolute deviation of 0.05
      }

      Fitmed fit2=new Fitmed(x,yy);

//      System.out.printf(endl;
//      System.out.printf(fit2.a << " %f\n", fit2.b << " %f\n", fit2.abdev);
//      System.out.printf(sqrt(2.0)-0.5*0.1*j/M << " %f\n", pi+0.1*j/M/x[N-1] << " %f\n", 0.5*0.1*j/M);

      sbeps=1.e-4;
      localflag = abs(fit2.a-(sqrt(2.0)-0.5*0.1*j/M)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitmed,Test2: Fitted constant term a is incorrect");
        
      }

      sbeps=5.e-5;
      localflag = abs(fit2.b-(pi+0.1*j/M/x[N-1])) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitmed,Test2: Fitted slope b is incorrect");
        
      }

      localflag = fit2.abdev > 0.05;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitmed,Test2: Calculated abdev is too high");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
