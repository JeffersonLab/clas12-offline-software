package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Rational_interp;
import com.nr.ran.Ran;

public class Test_Rat_interp {

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

    

    // Test Rat_interp
    System.out.println("Testing Rat_interp");
    for (i=0;i<N;i++) {
      x[i]=(double)(i)/(N-1);
      y[i]=(1.0-x[i]*(0.5-0.1*x[i]))/(1.0+(x[i]-2.0)*(x[i]-2.0));
    }
    Ran myran = new Ran(17);
    Rational_interp z = new Rational_interp(x,y,3);
    for (i=0;i<N;i++) {
      xx[i]=myran.doub();
      yy[i]=z.interp(xx[i]);      // interpolated values
      dyy[i]=z.dy;          // Estimated error
      zz[i]=(1.0-xx[i]*(0.5-0.1*xx[i]))/(1.0+(xx[i]-2.0)*(xx[i]-2.0)); // Actual values
    }
    System.out.printf("     Rat_interp: Max. estimated error: %f\n", maxel(dyy));
    System.out.printf("     Rat_interp: Max. actual error:    %f\n", maxel(vecsub(zz,yy)));
    sbeps=0.0001*maxel(y);
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Rat_interp: Inaccurate interpolation of rational function.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
