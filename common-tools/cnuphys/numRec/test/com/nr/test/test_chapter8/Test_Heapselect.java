package com.nr.test.test_chapter8;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.Heapselect;
import com.nr.sort.Sorter;

public class Test_Heapselect {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=1000,M=100;
    double xm,val;
    double[] x = new double[M],y = new double[M];
    boolean localflag, globalflag=false;

    

    // Test Heapselect
    System.out.println("Testing Heapselect");
    Heapselect heap = new Heapselect(M);
    Ran myran = new Ran(17);
    // Find top 100 values in 1000 random numbers
    for (i=0;i<N;i++) heap.add(myran.doub());
    for (i=0;i<M;i++) x[i]=heap.report(M-1-i);
    xm=x[0];    // The Mth largest

    Ran myran2 = new Ran(17); // Repeat same sequence
    j=0;
    for (i=0;i<N;i++) {
      val=myran2.doub();
      if (val >= xm) {
        if (j < M) y[j]=val;
        j++;
      }
    }
    localflag = (j != M);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Heapselect: The Mth largest was not correctly identified");
      
    }

    Sorter.sort(y);
    localflag = (maxel(vecsub(x,y))!=0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Heapselect: The M largest elements are not correctly ordered");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
