package com.nr.test.test_chapter4;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fi.Trapzd;
import com.nr.sf.Bessjy;

public class Test_trapzd {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=10,M=16;
    double x1,x2,val=0,sbeps=1.e-9;
    double[] y=new double[N],yy=new double[N];
    boolean localflag, globalflag=false;
    
    

    // Test trapzd
    System.out.println("Testing trapzd");
    Bessj0_trapzd bj0 = new Bessj0_trapzd();
    Bessj1_trapzd bj1 = new Bessj1_trapzd();
    for (i=0;i<N;i++) {
      x1=1.0*i;
      x2=x1+2.0;
      Trapzd trap = new Trapzd(bj1,x1,x2);
      for (j=0;j<M;j++) val=trap.next();
      y[i]=val;
      yy[i]=bj0.funk(x1)-bj0.funk(x2);
    }
    System.out.printf("trapzd: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** trapzd: Incorrect integral value");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Bessj0_trapzd implements UniVarRealValueFun {
    public double funk(double x) {
      Bessjy b = new Bessjy();
      return(b.j0(x));
    }
  };

  class Bessj1_trapzd implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b = new Bessjy();
      return(b.j1(x));
    }
  };

}
