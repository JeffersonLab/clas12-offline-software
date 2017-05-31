package com.nr.test.test_chapter5;

import static com.nr.NRUtil.buildVector;
import static com.nr.fe.Chebyshev.ipcshft;
import static com.nr.fe.Chebyshev.pcshft;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_ipcshft {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    // int M=6;
    double a=1.0,b=3.0,sbeps=1.e-13;
    double d[]={-1,6.0,-10.0,10.0,-6.0,1.0};
    double[] dd = buildVector(d);
    boolean localflag, globalflag=false;

    

    // Test ipcshft
    System.out.println("Testing ipcshft");
    double[] g = buildVector(dd);
    pcshft(a,b,g);
    double[] h = buildVector(g);
    ipcshft(a,b,h);

    localflag = maxel(vecsub(g,dd)) < 1.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ipcshft: Polynomial was not shifted");
      
    }

    localflag = maxel(vecsub(h,g)) < 1.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ipcshft: Polynomial was not inverse shifted");
      
    }

    System.out.printf("ipcshft: Maximum discrepancy = %f\n", maxel(vecsub(h,dd)));
    localflag = maxel(vecsub(h,dd)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ipcshft: Inverse shift did not undo the shift properly");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
