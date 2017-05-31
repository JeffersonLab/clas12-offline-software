package com.nr.test.test_chapter8;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.Indexx;

public class Test_Indexx {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=1000;
    double sbeps=1.e-16;
    double[] x= new double[N],z= new double[N],yy= new double[N],zz= new double[N],u = buildVector(N,1.0);
    int[] zi = new int[N];
    boolean localflag=false, globalflag=false;

    

    // Test Indexx
    System.out.println("Testing Indexx");
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) x[i]=myran.doub();  
    Indexx idx =new Indexx(x);      // Create the index
    double[] xx=buildVector(x);     // Copy of x
    idx.sort(xx);     // Test sort on x itself
    for (i=0;i<N-1;i++) localflag = localflag || (xx[i] > xx[i+1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Indexx: Array sorted according to index is incorrectly ordered");
      
    }

    // Test member function sort() on another array
    for (i=0;i<N;i++) yy[i] = 1.0-x[i]; // Invert the sorting order
    idx.sort(yy);   // yy sorted according to the sizes of x
    for (i=0;i<N-1;i++) localflag = localflag || (yy[i+1] > yy[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Indexx: Independent array sorted according to index is incorrectly ordered");
      
    }

    // Test member function el()
    for (i=0;i<N;i++) z[i] = 1.0-x[i];
    for (i=0;i<N;i++) zz[i] = idx.el(z,i);
    localflag = localflag || (maxel(vecsub(vecsub(u,zz),xx)) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Indexx: Member function el() does not sort inverted array into reverse order");
      
    }

    // Test l-value version of member function el()
    for (i=0;i<N;i++) idx.setEl(zz,i, xx[i]);
    localflag = localflag || (maxel(vecsub(x,zz)) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Indexx: l-value version of member function el() does not sort inverted array into reverse order");
      
    }

    // Test member function rank()
    idx.rank(zi);
    idx.sort(zi);
    for (i=0;i<N;i++) localflag = localflag || (zi[i] != i);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Indexx: Sorting the rank table into element-size order does not give increasing integers");
    }

    // Test use of empty constructor and member function index() with pointers
    double a[][]={{3.0,2.0,1.0,5.0,4.0},{3.0,1.0,5.0,2.0,4.0},{5.0,2.0,4.0,1.0,3.0}}; // matrix with unsorted rows
    int b[][]={{2,1,0,4,3},{1,3,0,4,2},{3,1,4,2,0}};    // Hand-generated index array for each row
    Indexx myhack = new Indexx();
    for (i=0;i<3;i++) {
      myhack.index(a[i],5);
      for (j=0;j<5;j++) localflag = localflag || (myhack.indx[j] != b[i][j]);
    } 
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Indexx: Member function index() did not work on array passed by pointer");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
