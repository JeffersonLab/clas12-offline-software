package com.nr.test.test_chapter3;

import static com.nr.test.NRTestUtil.*;
import static java.lang.Math.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.BaryRat_interp;
import com.nr.ran.Ran;

public class Test_BaryRat_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  double func_BaryRat(double x)
  {
    return (1.0-x*(0.5-0.1*x))/(1.0+(x-2.0)*(x-2.0));
  }
  
  @Test
  public void test() {
    int i,j,N=20,M=6;
    double ymax,dy,sbeps;
    double[] x=new double[N],y=new double[N],xx=new double[N],yy=new double[N],zz=new double[N];
    boolean localflag, globalflag=false;

    

    // Test BaryRat_interp
    System.out.println("Testing BaryRat_interp");
    for (i=0;i<N;i++) {
      x[i]=2.0*i/(N-1);
      y[i]=func_BaryRat(x[i]);
    }
    ymax=maxel(y);
    Ran myran = new Ran(17);
    for (j=0;j<M;j++) {
      BaryRat_interp z = new BaryRat_interp(x,y,j);
      for (i=0;i<N;i++) {
        xx[i]=2.0*myran.doub();
        yy[i]=z.interp(xx[i]);      // interpolated values
        zz[i]=func_BaryRat(xx[i]);    // Actual values 
      }
      dy=maxel(vecsub(zz,yy));
      System.out.printf("     BaryRat_interp: Order: %d  Max. error:    %f\n", j, dy);
      sbeps=pow(10.0,-(j+4.0)/2)*ymax;
//      System.out.printf(sbeps << endl;
      localflag = dy > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** BaryRat_interp: Inaccurate interpolation of rational function.");
        
      }
    }
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
