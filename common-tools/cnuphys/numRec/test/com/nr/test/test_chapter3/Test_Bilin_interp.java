package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Bilin_interp;
import com.nr.ran.Ran;

public class Test_Bilin_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps,xx1,xx2;
    int i,j,N=20;
    double[] x1=new double[N],x2=new double[N],yy=new double[N],zz=new double[N];
    double[][] y = new double[N][N];
    boolean localflag, globalflag=false;

    

    // Test Bilin_interp
    System.out.println("Testing Bilin_interp");
    for(i=0;i<N;i++) {
      x1[i]=(double)(i)/(N-1);
      for(j=0;j<N;j++) {
        x2[j]=(double)(j)/(N-1);
        y[i][j]=x1[i]+x2[j];
      }
    }
    Bilin_interp z = new Bilin_interp(x1,x2,y);
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      xx1=myran.doub();
      xx2=myran.doub();
      yy[i]=z.interp(xx1,xx2);  // interpolated values
      zz[i]=xx1+xx2;        // Actual values
    }
    sbeps=1.e-15;
    System.out.printf("     Bilin_interp: Max. actual error:    %f\n", maxel(vecsub(zz,yy)));
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bilin_interp: Inaccurate interpolation of 2D linear function.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
