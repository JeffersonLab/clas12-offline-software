package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static com.nr.sf.Gamma.*;

public class Test_factrl {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=9;
    double sbeps=5.e-15;
    int x[]={0,1,2,4,8,16,32,64,128};
    double y[]={1.0,1.0,2.0,24.0,40320.0,2.092278988800000e13,
      2.631308369336935e35,1.268869321858842e89,
      3.856204823625803e+215};
    double[] z = new double[N],c = buildVector(N,1.0);
    boolean localflag, globalflag=false;

    

    // Test factrl
    System.out.println("Testing factrl");

    for (i=0;i<N;i++) z[i]=factrl(x[i])/y[i];
    System.out.printf("factrl: Maximum fractional discrepancy = %f\n", maxel(vecsub(z,c)));
    localflag = maxel(vecsub(z,c)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** factrl: Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
