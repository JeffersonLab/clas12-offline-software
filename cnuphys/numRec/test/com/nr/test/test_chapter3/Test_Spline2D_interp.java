package com.nr.test.test_chapter3;

import static com.nr.NRUtil.buildVector;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Spline2D_interp;

public class Test_Spline2D_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps=5.e-15;
    int i,j;
    double xx1[]={1.0,2.0,3.0,4.0},xx2[]={1.0,2.0,3.0,4.0};
    double[] x1 = buildVector(xx1),x2 = buildVector(xx2);
    double[][] y =new double[4][4];
    boolean localflag, globalflag=false;

    

    // Test Spline2D_interp
    System.out.println("Testing Spline2D_interp");
    for(i=0;i<4;i++) 
      for(j=0;j<4;j++) 
        y[i][j]=xx1[i]+xx2[j];
    Spline2D_interp z = new Spline2D_interp(x1,x2,y);
    localflag = abs(z.interp(2.5,2.5)-5.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Spline2D_interp: .");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
