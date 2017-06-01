package com.nr.test.test_chapter11;

import static com.nr.test.NRTestUtil.matmul;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.ranvec;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.eig.Symmeig;
import com.nr.ran.Ran;

public class Test_Symmeig {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=10;
    double sbeps=5.e-15;
    double[] vec=new double[N],res=new double[N];
    double[][] a = new double[N][N];
    boolean localflag=false, globalflag=false;

    

    // Test Symmeig
    System.out.println("Testing Symmeig, interface1");
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      a[i][i]=myran.doub();
      for (j=0;j<i;j++) {
        a[i][j]=myran.doub();
        a[j][i]=a[i][j];
      }
    }
    Symmeig sym = new Symmeig(a);
    
    // test eigenvector/eigenvalue pairs
    for (i=0;i<N;i++) {   // for each eigenvector
      for (j=0;j<N;j++) vec[j]=sym.z[j][i];
      res=matmul(a,vec);
      for (j=0;j<N;j++) vec[j] *= sym.d[i];
//      System.out.printf(maxel(vecsub(res,vec)));
      localflag = localflag || (maxel(vecsub(res,vec)) > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Symmeig: Matrix times eigenvector was not the same as lambda*eigenvector");
      
    }

    // test the sorting of the eigenvalues by eigsrt()
    for (i=1;i<N;i++) 
      localflag = localflag || (sym.d[i] > sym.d[i-1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Symmeig: Eigenvalues not sorted in high-to-low order");
      
    }

    // Test Symmeig
    System.out.println("Testing Symmeig, interface2");
    double[] d=new double[N],e=new double[N];
    double[][] b=new double[N][N];
    ranvec(d);
    ranvec(e);
    for (i=0;i<N;i++) b[i][i]=d[i];
    for (i=1;i<N;i++) b[i][i-1]=b[i-1][i]=e[i];
    Symmeig sym2=new Symmeig(d,e);
    
    // test eigenvector/eigenvalue pairs
    for (i=0;i<N;i++) {   // for each eigenvector
      for (j=0;j<N;j++) vec[j]=sym2.z[j][i];
      res=matmul(b,vec);
      for (j=0;j<N;j++) vec[j] *= sym2.d[i];
//      System.out.printf(maxel(vecsub(res,vec)));
      localflag = localflag || (maxel(vecsub(res,vec)) > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Symmeig: Matrix times eigenvector was not the same as lambda*eigenvector");
      
    }

    // test the sorting of the eigenvalues by eigsrt()
    for (i=1;i<N;i++) 
      localflag = localflag || (sym2.d[i] > sym2.d[i-1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Symmeig: Eigenvalues not sorted in high-to-low order");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
