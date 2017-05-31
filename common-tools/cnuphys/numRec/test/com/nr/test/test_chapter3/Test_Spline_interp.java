package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.acos;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Spline_interp;
import com.nr.ran.Ran;

public class Test_Spline_interp {

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
    double yp1,ypn,pi;
    double[] x= new double[N],y= new double[N],xx= new double[N],yy= new double[N],zz= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Spline_interp
    System.out.println("Testing Spline_interp");
    pi=acos(-1.0);
    for(i=0;i<N;i++) {
      x[i]=(i)*pi/(N-1);
      y[i]=sin(x[i]);
    }
    yp1=1.0;
    ypn=-1.0;
    Spline_interp z = new Spline_interp(x,y,yp1,ypn);
    Ran myran = new Ran(17);
    for(i=0;i<N;i++) {
      xx[i]=pi*myran.doub();
      yy[i]=z.interp(xx[i]);
      zz[i]=sin(xx[i]);
    }
    System.out.printf("     Spline_interp: Max. actual error:    %f\n", maxel(vecsub(zz,yy)));
    sbeps=1.e-5*maxel(y);
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Spline_interp: Inaccurate interpolation of sin() function.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
