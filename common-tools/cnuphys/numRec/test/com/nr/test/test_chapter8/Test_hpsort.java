package com.nr.test.test_chapter8;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.Sorter;

public class Test_hpsort {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=100;
    double[] x = new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test hpsort
    System.out.println("Testing hpsort");
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) x[i]=myran.doub();
    Sorter.hpsort(x);
    for (i=0;i<N-1;i++) localflag = localflag || (x[i] > x[i+1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hpsort: Sorted values are not correctly ordered");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
