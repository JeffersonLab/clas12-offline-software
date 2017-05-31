package com.nr.test.test_chapter2;

import static com.nr.la.Toepltz.*;
import static com.nr.test.NRTestUtil.*;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_toeplz {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps;
    int i,j,N=10;
    double[] r =new double[2*N-1],x = new double[N],y= new double[N],yy= new double[N];
    boolean localflag, globalflag=false;
    ranvec(r);
    ranvec(y);

    

    // Test toeplz
    System.out.println("Testing toeplz");
    toeplz(r,x,y);
    // test solution w[]
    sbeps = 5.e-14;
    for(i=0;i<N;i++) {
      yy[i]=0.0;
      for(j=0;j<N;j++) yy[i] += r[i+N-1-j]*x[j];
    }
    localflag = maxel(vecsub(yy,y)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** toeplz: Inconsistant solution vector.\n");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
