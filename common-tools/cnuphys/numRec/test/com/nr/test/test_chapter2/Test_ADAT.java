package com.nr.test.test_chapter2;

import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;
import static java.lang.Math.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.ADAT;
import com.nr.la.NRsparseMat;
import com.nr.ran.Ran;

public class Test_ADAT {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps=1.e-12;
    int i,j,k,nvals,N=30,M=50;
    double[][] a =new double[N][M];
    boolean localflag, globalflag=false;

    

    // Test ADAT
    System.out.println("Testing ADAT (and NRsparseMat)");
    Ran myran = new Ran(17);
    nvals=N+M;    // Create a sparse matrix with random non-zero entries
    for (i=0;i<nvals;i++) a[myran.int32p() % N][myran.int32p() % M] = (double)(i);
    nvals=0;    // There may be fewer than nvals non-zero elements
    for (i=0;i<N;i++) 
      for(j=0;j<M;j++)
        if (a[i][j] != 0) nvals++;

    NRsparseMat as = new NRsparseMat(N,M,nvals);
    k=0;
    for (i=0;i<M;i++) {     // Columns
      as.col_ptr[i]=k;
      for(j=0;j<N;j++)    // Rows
        if (a[j][i] != 0.0) {       
          as.row_ind[k]=j; 
          as.val[k++]=a[j][i];
        }
    }
    as.col_ptr[M]=k;

    NRsparseMat bs=as.transpose();
    ADAT c = new ADAT(as,bs);
    double[] d = new double[M];    // Random diagonal matrix
    ranvec(d);
    c.updateD(d);   // Update ADAT object
    NRsparseMat cs=c.ref();  // cs represents N x N matrix

    // Next, compute the N x N matrix by brute force
    double[][] e = new double[M][N];
    for (i=0;i<M;i++)
      for (j=0;j<N;j++)
        e[i][j]=d[i]*a[j][i];
    double[][] f=matmul(a,e); // f is N x N

    // Find sparse representation of F
    nvals=0;    // Count non-zero elements
    for (i=0;i<N;i++) 
      for(j=0;j<N;j++)
        if (f[i][j] != 0.0) nvals++;
    NRsparseMat fs = new NRsparseMat(N,N,nvals);
    k=0;
    for (i=0;i<N;i++) {     // Columns
      fs.col_ptr[i]=k;
      for(j=0;j<N;j++)    // Rows
        if (f[j][i] != 0.0) {       
          fs.row_ind[k]=j; 
          fs.val[k++]=f[j][i];
        }
    }
    fs.col_ptr[N]=k;

    // Sparse representations cs and fs should be the same
    localflag = (cs.nrows != fs.nrows);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ADAT: Sparse matrix and brute force result have different number of rows.");
      
    }

    localflag = (cs.ncols != fs.ncols);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ADAT: Sparse matrix and brute force result have different number of columns.");
      
    }

    localflag = (cs.nvals != fs.nvals);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ADAT: Sparse matrix and brute force result have different number of nonzero elements.");
      
    }

    localflag=false;
    for (i=0;i<cs.ncols+1;i++) 
      localflag=localflag || (cs.col_ptr[i] != fs.col_ptr[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ADAT: Column pointers differ between sparse matrix and brute force result.");
      
    }

    localflag=false;
    for (i=0;i<fs.nvals;i++)
      localflag=localflag || (cs.row_ind[i] != fs.row_ind[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ADAT: Row indices differ between sparse matrix and brute force result.");
      
    }

    localflag=false;
    for (i=0;i<fs.nvals;i++)
      localflag=localflag || abs(cs.val[i] - fs.val[i]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ADAT: Matrix entries differ between sparse matrix and brute force result.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
