package com.nr.test.test_chapter6;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Erf;

public class Test_erfcc {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=13;
    double sbeps=2.e-7;
    double x[]={-4.0,-3.0,-2.0,-1.0,-0.5,-0.1,0.0,0.1,0.5,1.0,2.0,3.0,4.0};
    double[] yy= new double[N],zz= new double[N];
    boolean localflag, globalflag=false;

    

    // Test erfcc
    System.out.println("Testing erfcc");

    Erf e = new Erf();
    for (i=0;i<N;i++) {
      yy[i]=Erf.erfcc(x[i]);
      zz[i]=e.erfc(x[i]);
    }
    System.out.printf("erfcc: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** erfcc: Error exceeds single precision accuracy");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
