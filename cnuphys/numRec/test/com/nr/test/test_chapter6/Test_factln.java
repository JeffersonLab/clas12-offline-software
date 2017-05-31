package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static com.nr.sf.Gamma.*;
public class Test_factln {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=9;
    double sbeps=1.e-13;
    int x[]={0,1,2,4,8,16,32,64,128};
    double y[]={0.0,0.0,0.6931471805599453,3.178053830347946,1.060460290274525e1,
      3.067186010608067e1,8.155795945611504e1,2.051681994826412e2,
      4.964054784872176e2};
    double[] yy =buildVector(y),z = new double[N];
    boolean localflag, globalflag=false;

    

    // Test factln
    System.out.println("Testing factln");

    for (i=0;i<N;i++) z[i]=factln(x[i]);
    System.out.printf("factln: Maximum discrepancy = %f\n", maxel(vecsub(z,yy)));
    localflag = maxel(vecsub(z,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** factln: Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
