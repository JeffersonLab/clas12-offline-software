package com.nr.test.test_chapter5;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Levin;

public class Test_Levin {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,k,N=9,M=100;
    double x,partialsum,omega,beta=1.0,val=0,eps=1.e-14,sbeps=5.e-14;
    double[] y=new double[N],yy=new double[N];
    boolean localflag, globalflag=false;

    

    // Test Levin
    System.out.println("Testing Levin");
    for (i=0;i<N;i++) {
      Levin sum =new Levin(M,eps);
      x=0.1*(i+1);
      partialsum=0.0;
      k=0;
      omega=1.0;
      while (!sum.cnvgd) {
        partialsum += omega;
        omega *= x;
        val=sum.next(partialsum,(k+beta)*omega);
      }
      y[i]=val;
      yy[i]=1.0/(1.0-x);
    }
    System.out.printf("Levin: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Levin: Inaccurate estimate of series value");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
