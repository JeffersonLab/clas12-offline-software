package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Linear_interp;
import com.nr.ran.Ran;

public class Test_Linear_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps=5.e-15;
    int i,N=10;
    double[] x=new double[N],y=new double[N],xx=new double[N],yy=new double[N],zz=new double[N];
    boolean localflag, globalflag=false;

    

    // Test Linear_interp
    System.out.println("Testing Linear_interp");
    for (i=0;i<N;i++) {
      x[i]=(double)(i);
      y[i]=2.0*(double)(i);
    }
    Linear_interp z = new Linear_interp(x,y);
    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      xx[i]=(N-1)*myran.doub();
      yy[i]=z.interp(xx[i]);    // interpolated values
      zz[i]=2.0*xx[i];      // Correct values
    }
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Linear_interp: interpolation of linear function unsuccessful.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
