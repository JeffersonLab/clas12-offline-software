package com.nr.test.test_chapter5;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Eulsum;

public class Test_Eulsum {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=9,M=100;
    double x,term,val=0,eps=1.e-15,sbeps=5.e-15;
    double[] y=new double[N],yy=new double[N];
    boolean localflag, globalflag=false;

    

    // Test Eulsum
    System.out.println("Testing Eulsum");
    for (i=0;i<N;i++) {
      Eulsum sum = new Eulsum(M,eps);
      x=0.1*(i+1);
      term=1.0;
      while (!sum.cnvgd) {
        val=sum.next(term);
        term *= (-x);
      }
      y[i]=val;
      yy[i]=1.0/(1.0+x);
    }
    System.out.printf("Eulsum: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Eulsum: Inaccurate estimate of series value");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
