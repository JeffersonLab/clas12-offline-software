package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sf.Beta;

public class Test_betai {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=21,M=100;
    double c,d,u,r,uu,sbeps;
    double a[]={0.1,0.1,0.1,0.5,0.5,0.5,1.0,1.0,1.0,2.0,2.0,2.0,
      5.0,5.0,5.0,10.0,10.0,10.0,20.0,20.0,20.0};
    double b[]={0.1,2.0,5.0,0.1,2.0,5.0,0.1,2.0,5.0,0.1,2.0,5.0,
      0.1,2.0,5.0,0.1,2.0,5.0,0.1,2.0,5.0};
    double x[]={0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,0.0,
      0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9};
    double y[]={0.0,0.8658177758494670,0.9766004939306013,
      0.1073414121635943,0.8221921916437793,0.9898804402645663,
      8.755646344451919e-2,0.9099999999999999,0.9996800000000000,
      0.1341822241505331,1.0,0.0,
      2.646501824334656e-7,1.600000000000003e-3,9.880866000000001e-2,
      2.079166305336834e-6,5.859375000000002e-3,0.2792569872383998,
      1.527902462384040e-5,5.764607523034282e-2,0.9149251141213293};
    double[] yy=buildVector(y),zz = new double[N];
    boolean localflag, globalflag=false;

    

    // Test betai
    System.out.println("Testing betai, invbetai");

    // Beta bi = new Beta();
    for (i=0;i<N;i++) {
      zz[i]=Beta.betai(a[i],b[i],x[i]);
//      System.out.printf(setprecision(15) << zz[i] << " %f\n", yy[i]);
    }

    System.out.printf("betai: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    sbeps=1.e-14;
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** betai: Incorrect function values");
      
    }

    // Round trip test
    Ran myran = new Ran(17);
    sbeps=1.e-8;
    for (i=0;i<M;i++) {
      c=10.0*myran.doub();
      d=10.0*myran.doub();
      u=myran.doub();
//      System.out.printf(i << " %f\n", u;
      r=Beta.betai(c,d,u);
//      System.out.println(" %f\n", r;
      uu=Beta.invbetai(r,c,d);
//      System.out.println(" %f\n", uu);
    
      localflag = abs(uu-u) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** betai: Failure in round-trip test");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
