package com.nr.test.test_chapter2;

import static com.nr.test.NRTestUtil.matsub;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.ranvec;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.NRsparseMat;
import com.nr.ran.Ran;

public class Test_NRsparseMat {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double val,sbeps;
    int i,j,k,N=50,M=30,nvals;
    boolean localflag, globalflag=false;

    

    // Test NRsparseMat
    System.out.println("Testing NRsparseMat");
    Ran myran = new Ran(17);
    double[][] a = new double[N][M];
    nvals=N+M;
    
    // XXX java not support unsigned int.
    for (i=0;i<nvals;i++) a[myran.int32p() % N][myran.int32p() % M] = (double)(i);
    nvals=0;    // There may be fewer than nvals non-zero elements
    for (i=0;i<N;i++) 
      for(j=0;j<M;j++)
        if (a[i][j] != 0.0) nvals++;
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

    double[][] b = new double[N][M];
    for (i=0;i<M;i++)
      for (j=as.col_ptr[i];j<as.col_ptr[i+1];j++)
        b[as.row_ind[j]][i]=as.val[j];
    sbeps = 5.e-14;
    localflag = maxel(matsub(a,b)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** NRsparseMat: Reconstructed matrix is incorrect");
      
    }

    // Test multiplication of sparse matrix by vector
    double[] c = new double[as.ncols];
    ranvec(c);
    double[] d=as.ax(c);    // Sparse multiplication by vector
    // Find same result by brute force
    double[] e = new double[as.nrows];
    for (i=0;i<as.nrows;i++) {
      val=0.0;
      for (j=0;j<as.ncols;j++) val += a[i][j]*c[j];
      e[i]=val;
    }

//    System.out.println("Discrepancy in A*x: " << maxel(vecsub(d,e)) << endl;
    localflag = (maxel(vecsub(d,e)) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** NRsparseMat: Multiplication of sparse matrix by vector failed.");
      
    }
    
    // Test multiplication of sparse transpose by vector
    double[] f = new double[as.nrows];
    ranvec(f);
    double[] g=as.atx(f); // Sparse transpose multiplied by vector
    // Find same result by brute force
    double[] h= new double[as.ncols];
    for (i=0;i<as.ncols;i++) {
      val=0.0;
      for (j=0;j<as.nrows;j++) val += a[j][i]*f[j];
      h[i]=val;
    }

//    System.out.println("Discrepancy in AT*x: " << maxel(vecsub(g,h)) << endl;
    localflag = (maxel(vecsub(g,h)) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** NRsparseMat: Multiplication of sparse transpose by vector failed.");
      
    }

    // Note: Member function transpose() is tested in routine test_ADAT

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
