package com.nr.test.test_chapter2;

import static com.nr.la.Vander.*;
import static com.nr.test.NRTestUtil.*;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
public class Test_vander {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps;
    int i,j,N=5;
    double[] x = new double[N],q = new double[N],w = new double[N],qq = new double[N];
    boolean localflag, globalflag=false;
    ranvec(x);
    ranvec(q);    

    // Test vander
    System.out.println("Testing vander");
    vander(x,w,q);
    // test solution w[]
    sbeps = 1.e-6;
    for(i=0;i<N;i++) {
      qq[i]=0.0;
      for(j=0;j<N;j++) {
        qq[i] += w[j];
        w[j] *= x[j];
      }
    }

    localflag = maxel(vecsub(qq,q)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** vander: Inconsistant solution vector.\n");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
