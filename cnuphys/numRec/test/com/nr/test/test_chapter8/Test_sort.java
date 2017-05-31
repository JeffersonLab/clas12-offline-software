package com.nr.test.test_chapter8;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.Sorter;

public class Test_sort {

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
    boolean localflag,globalflag=false;

    

    // Test sort
    System.out.println("Testing sort");
    localflag=false;
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) x[i]=myran.doub();
    Sorter.sort(x);
    for (i=0;i<N-1;i++) localflag = localflag || (x[i] > x[i+1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sort: Sorted values are not correctly ordered");
      
    }

    localflag=false;
    for (i=0;i<N;i++) y[i]=1.0-x[i];  // Order is reversed
    Sorter.sort(y,N/2);            // sort first half of array
    for (i=0;i<N/2-1;i++) localflag = localflag || (y[i] > y[i+1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sort: First half of array is not correctly ordered");
      
    }

    localflag=false;
    for (i=N/2;i<N-1;i++) localflag = localflag || (y[i+1] > y[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sort: Second half of array is not correctly ordered");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
