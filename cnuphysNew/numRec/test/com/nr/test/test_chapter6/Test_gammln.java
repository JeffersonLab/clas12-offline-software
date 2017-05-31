package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import com.nr.sf.Gamma;
public class Test_gammln {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10;
    double sbeps=5.e-15;
    double x[]={0.5,1.0,1.5,2.0,3.0,4.0,5.0,7.0,10.0,20.0};
    double y[]={0.5723649429247001,0.0,-0.1207822376352453,0.0,0.6931471805599454,
      1.791759469228055,3.178053830347945,6.579251212010101,12.80182748008147,
      39.33988418719950};
    double[] yy=buildVector(y),zz = new double[N];
    boolean localflag, globalflag=false;

    

    // Test gammln
    System.out.println("Testing gammln");
    for (i=0;i<N;i++) zz[i]=Gamma.gammln(x[i]);
    System.out.printf("gammln: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** gammln: Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
