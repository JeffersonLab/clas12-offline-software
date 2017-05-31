package com.nr.test.test_chapter8;

import static com.nr.NRUtil.buildVector;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.Sorter;

public class Test_piksr2 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=100;
    double[] x= new double[N],y= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test piksr2
    System.out.println("Testing piksr2");
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      x[i]=myran.doub();
      y[i]=1.0-x[i];    // y is in opposite order of x
    }
    Sorter.piksr2(x,y);
    for (i=0;i<N-1;i++) localflag = localflag || (x[i] > x[i+1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** piksr2: First array is not correctly ordered");
      
    }

    for (i=0;i<N-1;i++) localflag = localflag || (y[i] < y[i+1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** piksr2: Second array did not get correspondingly rearranged");
      
    }

    double[] xx=buildVector(x);
    Sorter.piksr2(y,xx);
    for (i=0;i<N-1;i++) localflag = localflag || (x[i] == xx[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** piksr2: Sorting second array should have returned first to original order");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
