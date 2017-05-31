package com.nr.test.test_chapter2;

import com.nr.la.Bandec;
import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_Bandec {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps;
    double[] r =new double[50],y =new double[50],rr =new double[50];
    boolean localflag, globalflag=false;

    

    // Test banmul and bandec
    System.out.println("Testing bandec and banmul");
    double[][] c = new double[50][17];
    ranmat(c);
    Bandec clu = new Bandec(c,9,7);
    ranvec(r);
    clu.solve(r,y);
    Bandec.banmul(c,9,7,y,rr);
    sbeps = 5.e15;
    localflag = maxel(vecsub(r,rr)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) { 
      fail("*** bandec,banmul: Inconsistent solution vector");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
