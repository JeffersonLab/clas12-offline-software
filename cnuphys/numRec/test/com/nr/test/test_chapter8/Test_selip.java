package com.nr.test.test_chapter8;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.Sorter;

public class Test_selip {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,count,N=100,M=20;
    double xsel;
    double[] x = new double[N];
    boolean localflag, globalflag=false;

    

    // Test selip
    System.out.println("Testing selip");
    Ran myran =new Ran(17);
    for (i=0;i<N;i++) x[i]=myran.doub();
    double[] xx=buildVector(x);   // Copy of array for later test
    for (i=0;i<M;i++) {
      k=myran.int32p() % N;
      xsel=Sorter.selip(k,x);

      // Test for position in array
      count=0;
      for (j=0;j<N;j++)
        if (x[j] > xsel) count++;
      localflag = (count != (N-k-1));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** selip: There are not k values less or equal to selected value");
        
      }

      // Test that array was not altered
      localflag = (maxel(vecsub(x,xx)) != 0.0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** selip: Array was altered by selip");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
