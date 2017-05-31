package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import static com.nr.sf.Integrals.*;

public class Test_expint {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,m,N=22;
    double a,b,expect,sbeps=5.e-15;
    int n[]={0,0,0,0,0,1,1,1,1,1,2,2,2,2,3,3,4,4,5,5,10,10};
    double x[]={0.01,0.1,0.5,1.0,2.0,0.01,0.1,0.5,1.0,2.0, 
      0.0,0.5,1.0,2.0,0.0,1.0,0.0,1.0,0.0,1.0,0.0,1.0};
    double y[]={9.900498337491680e1,9.048374180359595,1.213061319425267,
      3.678794411714423e-1,6.766764161830635e-2,4.037929576538114,
      1.822923958419390,5.597735947761607e-1,2.193839343955203e-1,
      4.890051070806113e-2,1.0,0.32664386232455301773,
      0.14849550677592204,0.037534261820490452,0.5,
      0.10969196719776013,0.33333333333333333,0.086062491324560728,
      0.250,0.070454237461720398,0.11111111111111111,
      0.036393994031416401634};
    double[] yy=buildVector(y),zz = new double[N];
    boolean localflag, globalflag=false;

    

    // Test expint
    System.out.println("Testing expint");

    for (i=0;i<N;i++) {
      zz[i]=expint(n[i],x[i]);
//      System.out.printf(yy[i] << " %f\n", zz[i] << " %f\n", abs(yy[i]/zz[i]-1));
    }

    System.out.printf("expint: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** expint: Incorrect function values");
      
    }

    // Test special cases
    for (i=0;i<N;i++) {
      a=0.5*(i+1);
      b=expint(0,a);
      expect=exp(-a)/a;

      localflag = abs(b/expect-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** expint: Failure of special case n=0");
        
      }
    }

    for (i=0;i<N;i++) {
      m=i+2;
      b=expint(m,0.0);
      expect=1.0/(m-1);

      localflag = abs(b/expect-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** expint: Failure of special case x=0.0");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
