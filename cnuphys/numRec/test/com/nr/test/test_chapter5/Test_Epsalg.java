package com.nr.test.test_chapter5;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Epsalg;

public class Test_Epsalg {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,k,N=9,M=100;
    double x,partialsum,val=0,eps=1.e-15,sbeps=5.e-15;
    double[] y=new double[N],yy=new double[N];
    boolean localflag, globalflag=false;

    

    // Test Epsalg
    System.out.println("Testing Epsalg");
    for (i=0;i<N;i++) {
      Epsalg sum = new Epsalg(M,eps);
      x=0.1*(i+1);
      partialsum=0.0;
      k=0;
      while (!sum.cnvgd) {
        partialsum += pow(x,k++);
        val=sum.next(partialsum);
      }
      y[i]=val;
      yy[i]=1.0/(1.0-x);
    }
    System.out.printf("Epsalg: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Epsalg: Inaccurate estimate of series value");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
