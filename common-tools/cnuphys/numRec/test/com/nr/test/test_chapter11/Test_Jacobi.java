package com.nr.test.test_chapter11;

import static com.nr.test.NRTestUtil.matmul;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.eig.Jacobi;
import com.nr.ran.Ran;

public class Test_Jacobi {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=10;
    double sbeps=3.e-15;
    double[] vec=new double[N],res=new double[N];
    double[][] a = new double[N][N];
    boolean localflag=false, globalflag=false;

    

    // Test Jacobi
    System.out.println("Testing Jacobi");

    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      a[i][i]=myran.doub();
      for (j=0;j<i;j++) {
        a[i][j]=myran.doub();
        a[j][i]=a[i][j];
      }
    }
    Jacobi jac = new Jacobi(a);
    
    // test eigenvector/eigenvalue pairs
    for (i=0;i<N;i++) {   // for each eigenvector
      for (j=0;j<N;j++) vec[j]=jac.v[j][i];
      res=matmul(a,vec);
      for (j=0;j<N;j++) vec[j] *= jac.d[i];

      localflag = localflag || (maxel(vecsub(res,vec)) > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Jacobi: Matrix times eigenvector was not the same as lambda*eigenvector");
      
    }

    localflag=false;
    // test the sorting of the eigenvalues by eigsrt()
    for (i=1;i<N;i++) 
      localflag = localflag || (jac.d[i] > jac.d[i-1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Jacobi: Eigenvalues not sorted in high-to-low order");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
