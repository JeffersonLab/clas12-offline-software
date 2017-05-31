package com.nr.test.test_chapter19;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.inv.Quad_matrix;
import com.nr.la.LUdcmp;

public class Test_Fredex {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean localflag, globalflag=false;

    

    // Test Fredex
    System.out.println("Testing Fredex");

    localflag = main_fredex() != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fredex : Program did not indicate successful completion");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  int main_fredex()
  {
    final int N=40;
    double[] g=new double[N];
    double[][] a=new double[N][N];
    new Quad_matrix(a);
    LUdcmp alu = new LUdcmp(a);
    for (int j=0;j<N;j++)
      g[j]=sin(j*PI/(N-1));
    alu.solve(g,g);
    for (int j=0;j<N;j++) {
      double x=j*PI/(N-1);
      System.out.printf("%d  %f  %f\n",(j+1), x, g[j]);
    }
    return 0;
  }
}
