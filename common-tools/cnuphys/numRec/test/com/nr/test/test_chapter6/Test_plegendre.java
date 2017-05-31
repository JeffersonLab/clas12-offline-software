package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Legendre;

public class Test_plegendre {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=15;
    double sbeps=1.0e-15;
    int l[]={0,1,1,2,2,2,3,3,3,3,5,10,15,20,25};
    int m[]={0,0,1,0,1,2,0,1,2,3,3,5,7,5,3};
    double x[]={0.5,-0.5,0.2,-0.4,0.6,0.2,-0.4,0.6,-0.8,0.7,-0.7,0.4,-0.4,0.1,0.5};
    double y[]={2.820947917738781e-001,-2.443012559514600e-001,-3.385137501286538e-001,
      -1.640036139313104e-001,-3.708232339422620e-001,3.708232339422620e-001,
      3.283951726793016e-001,-2.068353178330564e-001,-2.943318172127853e-001,
      -1.519582778329822e-001,-4.296502741348962e-001,2.127192227262643e-001,
      -2.877479076870877e-001, 2.957240266476461e-001,2.658533041450948e-001};
    double[] yy = buildVector(y),zz = new double[N];
    boolean localflag, globalflag=false;

    

    // Test plegendre
    System.out.println("Testing plegendre");

    for (i=0;i<N;i++) zz[i]=Legendre.plegendre(l[i],m[i],x[i]);
    System.out.printf("plegendre: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** plegendre: Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
