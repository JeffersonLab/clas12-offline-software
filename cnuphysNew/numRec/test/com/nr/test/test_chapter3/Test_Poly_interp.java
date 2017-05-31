package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Poly_interp;
import com.nr.ran.Ran;

public class Test_Poly_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps;
    int i,N=20;
    double[] x= new double[N],y= new double[N],xx= new double[N],yy= new double[N],zz= new double[N],dyy= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Poly_interp
    System.out.println("Testing Poly_interp");
    for (i=0;i<N;i++) {
      x[i]=(double)(i)/(N-1);
      y[i]=1.0+x[i]*(1.0+x[i]*(1.0+x[i]*(1.0+x[i])));
    }
    Ran myran = new Ran(17);
    Poly_interp z = new Poly_interp(x,y,3);
    for (i=0;i<N;i++) {
      xx[i]=myran.doub();
      yy[i]=z.interp(xx[i]);    // interpolated values
      dyy[i]=z.dy;        // Estimated errors
      zz[i]=1.0+xx[i]*(1.0+xx[i]*(1.0+xx[i]*(1.0+xx[i])));  // Actual Values
    }
    System.out.printf("     Poly_interp: Max. estimated error: %f\n", maxel(dyy));
    System.out.printf("     Poly_interp: Max. actual error:    %f\n", maxel(vecsub(zz,yy)));
    sbeps = 0.001*maxel(y);
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poly_interp: interpolation of 4th-order polynomial unsuccessful.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
