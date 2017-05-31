package com.nr.test.test_chapter9;

import static com.nr.NRUtil.buildVector;
import static com.nr.root.Roots.zrhqr;
import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;

public class Test_zrhqr {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=5;
    double sbeps=5.e-14;
    double a[]={2.0,-2.0,7.0,1.0,-3.0,5.0};
    Complex b = new Complex();
    double[] aa= buildVector(a),dy = new double[N];
    Complex[] rts = new Complex[N];
    boolean localflag, globalflag=false;

    // Test zrhqr
    System.out.println("Testing zrhqr");
    // Roots of polynomial  
    zrhqr(aa,rts);
    for (i=0;i<N;i++) {
      b=new Complex(a[5]);
      for (j=0;j<5;j++) b=b.mul(rts[i]).add(new Complex(a[4-j]));
      dy[i]=b.abs();
//      System.out.printf(b << "  %f\n", dy[i]);
    }
    System.out.printf("zrhqr: Maximum discrepancy = %f\n", maxel(dy));
    localflag = maxel(dy) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** zrhqr: Incorrect roots");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
