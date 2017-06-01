package com.nr.test.test_chapter8;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.Sorter;

public class Test_select {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,N=100,M=20;
    double xsel;
    double[] x=new double[N];
    boolean localflag, globalflag=false;

    

    // Test select
    System.out.println("Testing select");
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) x[i]=myran.doub();
    for (i=0;i<M;i++) {
      k=myran.int32p() % N;
      xsel=Sorter.select(k,x);

      localflag = (x[k] != xsel);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** select: Selected value is not in array position k");
        
      }

      localflag = false;
      for (j=0;j<k;j++) localflag = localflag || (x[j] > x[k]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** select: Some array positions below k have values above a[k]");
        
      }

      localflag = false;
      for (j=k+1;j<N;j++) localflag = localflag || (x[j] <= x[k]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** select: Some array positions above k have values less or equal to a[k]");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
