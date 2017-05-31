package com.nr.test.test_chapter2;

import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.NRsparseCol;
import com.nr.ran.Ran;

public class Test_NRsparseCol {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps;
    int i,k,N=50,nnvals=20;
    boolean localflag, globalflag=false;

    

    // Test NRsparseCol
    System.out.println("Testing NRsparseCol");
    Ran myran = new Ran(17);
    double[] a =new double[N];
    
    // XXX java not support unsigned int.
    for (i=0;i<nnvals;i++) a[myran.int32p() % N] = (double)(i);
    nnvals=0;   // There may be fewer than nnvals non-zero elements
    for (i=0;i<N;i++)
      if (a[i] != 0.0) nnvals++;
    NRsparseCol as = new NRsparseCol(N,nnvals);
    k=0;
    for (i=0;i<a.length;i++)
      if (a[i] != 0.0) {
        as.row_ind[k]=i; 
        as.val[k++]=a[i];
      }
    double[] b = new double[N];
    for (i=0;i<as.nvals;i++) b[as.row_ind[i]]=as.val[i];
    sbeps = 5.e-14;
    localflag = maxel(vecsub(a,b)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** NRsparseCol: Reconstructed matrix is not correct");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
