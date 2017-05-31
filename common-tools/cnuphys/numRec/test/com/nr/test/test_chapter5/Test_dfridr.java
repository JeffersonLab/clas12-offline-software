package com.nr.test.test_chapter5;

import static com.nr.fe.Dfridr.dfridr;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.sf.Bessjy;

public class Test_dfridr {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,M=10;
    double x,h=0.2,sbeps=5.e-14;
    doubleW err = new doubleW(0);
    double[] y= new double[M],yy= new double[M];
    boolean localflag, globalflag=false;

    

    // Test dfridr
    System.out.println("Testing dfridr");

    // Test with function
    bj0_dfridr bj0_dfridr = new bj0_dfridr();
    bj1_dfridr bj1_dfridr = new bj1_dfridr();
    for (i=0;i<M;i++) {
      x=(double)(i+1);
      y[i]=dfridr(bj0_dfridr,x,h,err);
      yy[i]= -bj1_dfridr.funk(x);
    }
    System.out.printf("dfridr (function): Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** dfridr: Derivative returns incorrect values with function");
      
    }

    // Test with functor
    Bessj0_dfridr bj0 = new Bessj0_dfridr();
    Bessj1_dfridr bj1 = new Bessj1_dfridr();
    for (i=0;i<M;i++) {
      x=(double)(i+1);
      y[i]=dfridr(bj0,x,h,err);
      yy[i]= -bj1.funk(x);
    }
    System.out.printf("dfridr (functor): Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** dfridr: Derivative returns incorrect values with functor");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Bessj0_dfridr  implements UniVarRealValueFun{
    Bessjy b = new Bessjy();
    public double funk (final double x) {
      return(b.j0(x));
    }
  };

  class Bessj1_dfridr implements UniVarRealValueFun{
    Bessjy b = new Bessjy();
    public double funk (final double x) {
      return(b.j1(x));
    }
  };

  class bj0_dfridr implements UniVarRealValueFun{
    public double funk(final double x) {
    Bessjy b = new Bessjy();
    return(b.j0(x));
  }
  }
  class bj1_dfridr implements UniVarRealValueFun{
  public double funk(final double x) {
    Bessjy b = new Bessjy();
    return(b.j1(x));
  }
  }
}
