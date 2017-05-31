package com.nr.test.test_chapter2;

import static com.nr.test.NRTestUtil.matmul;
import static com.nr.test.NRTestUtil.matsub;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.ranmat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.la.SVD;

public class Test_SVD {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps, diag=10.0;
    int N=50,i; // M=30 not use it.
    double[][] a = new double[N][N],b = new double[N][N];
    boolean localflag, globalflag=false;
    ranmat(a,diag);
    ranmat(b);

    

    // Test SVD (not very deeply, however)
    System.out.println("Testing SVD");
    SVD svd = new SVD(a);
    double[][] x = new double[b.length][b[0].length];
    svd.solve(b,x);
    sbeps = 1.e-14;
    localflag = maxel(matsub(matmul(a,x),b)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** SVD: Inconsistent solution vector");
      
    }

//    System.out.println("svd: rank = " << svd.rank() << "  nullity = " << svd.nullity() << endl;
    localflag = (svd.rank() != 50 || svd.nullity() != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** SVD: Unexpected rank or nullity in original matrix");
      
    }

    for (i=0;i<N;i++) {       // Decrease the matrix rank
      a[3][i]=a[2][i]-a[1][i];
      a[4][i]=0.0;
    }
    SVD svd2 = new SVD(a);
//    System.out.println("svd: rank = " << svd2.rank() << "  nullity = " << svd2.nullity() << endl;
    localflag = (svd2.rank() != 48 || svd2.nullity() != 2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** SVD: Unexpected rank or nullity in modified matrix");
      
    }
      
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
