package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static com.nr.sf.Integrals.*;

public class Test_ei {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10;
    double sbeps=1.e-15;
    double x[]={0.5e-292,0.5,1.0,2.0,4.0,8.0,16.0,32.0,64.0,128.0};
    double y[]={-6.724707786699197e+002,4.542199048631735e-1,1.895117816355937,
      4.954234356001890,1.963087447005622e1,4.403798995348382e2,
      5.955609986708373e5,2.550043566357786e12,9.899640925974459e25,
      3.061380614342898e+053};
    double[] zz = new double[N],cc =buildVector(N,1.);
    boolean localflag, globalflag=false;

    

    // Test ei
    System.out.println("Testing ei");

    for (i=0;i<N;i++) zz[i]=ei(x[i])/y[i];
    System.out.printf("ei: Maximum fractional discrepancy = %f\n", maxel(vecsub(zz,cc)));
    localflag = maxel(vecsub(zz,cc)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ei: Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
