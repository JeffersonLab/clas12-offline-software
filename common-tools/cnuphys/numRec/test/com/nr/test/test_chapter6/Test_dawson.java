package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static com.nr.sf.Integrals.*;

public class Test_dawson {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=21;
    double sbeps=1.0e-7;
    double x[]={0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2,1.3,1.4,
      1.5,1.6,1.7,1.8,1.9,2.0};
    double y[]={0.0,0.09933599239785286,0.1947510333680280,0.2826316650213119,
      0.3599434819348881,0.4244363835020223,0.4747632036629779,
      0.5105040575592318,0.5321017070563654,0.5407243187262987,
      0.5380795069127684,0.5262066799705525,0.5072734964077396,
      0.4833975173848241,0.4565072375268973,0.4282490710853986,
      0.3999398943230814,0.3725593489740788,0.3467727691148722,
      0.3229743193228178,0.3013403889237920};
    double[] yy=buildVector(y),zz = new double[N];
    boolean localflag, globalflag=false;

    

    // Test dawson
    System.out.println("Testing dawson");
    for (i=0;i<N;i++) zz[i]=dawson(x[i]);
    System.out.printf("dawson: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** dawson: Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
