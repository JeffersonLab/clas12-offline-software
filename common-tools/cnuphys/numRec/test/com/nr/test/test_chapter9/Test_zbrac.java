package com.nr.test.test_chapter9;

import static com.nr.root.Roots.zbrac;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.sf.Bessjy;

public class Test_zbrac {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean success;
    int i,j,N=16,M=50;
    doubleW x1=new doubleW(0),x2=new doubleW(0);
    double y[]={2.404825557695773,5.520078110286311,8.653727912911013,
      1.179153443901428e1,1.493091770848779e1,1.807106396791092e1,
      2.121163662987926e1,2.435247153074930e1,2.749347913204025e1,
      3.063460646843198e1,3.377582021357357e1,3.691709835366405e1,
      4.005842576462825e1,4.319979171317673e1,4.634118837166182e1,
      4.948260989739782e1};
    boolean localflag, globalflag=false;

    

    // Test zbrac
    System.out.println("Testing zbrac");
    Func_zbrac fx = new Func_zbrac();
    for (i=1;i<M;i++) {
      x1.val=(double)(i);
      x2.val=(double)(i+1);
      success=zbrac(fx,x1,x2);

      localflag = !success;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** zbrac: Failed to find a bracket with one of the starting intervals.");
        
      }

      localflag = localflag || (fx.funk(x1.val)*fx.funk(x2.val) >= 0.0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** zbrac: An identified bracketing interval does not actually bracket a root.");
        
      }

      for (j=0;j<N;j++)
        localflag=localflag || (y[j] > x1.val) && (y[j] < x2.val);
      localflag = !localflag;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** zbrac: No known root of bessj0 falls witning one of the identified brackets.");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  class Func_zbrac implements UniVarRealValueFun{
    Bessjy b =new Bessjy();
    public double funk (final double x) {
      return b.j0(x);
    }
  }
}
