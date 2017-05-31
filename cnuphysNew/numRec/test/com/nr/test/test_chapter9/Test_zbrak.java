package com.nr.test.test_chapter9;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.root.Roots.Zbrak;
import com.nr.sf.Bessjy;

public class Test_zbrak {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i;
    int nroot = 0;
    double[] xb1,xb2;
    double x1=1.0,x2=50.0;
    double y[]={2.404825557695773,5.520078110286311,8.653727912911013,
      1.179153443901428e1,1.493091770848779e1,1.807106396791092e1,
      2.121163662987926e1,2.435247153074930e1,2.749347913204025e1,
      3.063460646843198e1,3.377582021357357e1,3.691709835366405e1,
      4.005842576462825e1,4.319979171317673e1,4.634118837166182e1,
      4.948260989739782e1};
    

    boolean localflag=false, globalflag=false;

    

    // Test zbrak
    System.out.println("Testing zbrak");
    Func_zbrak fx = new Func_zbrak();
    Zbrak z = new Zbrak();  z.zbrak(fx,x1,x2,100);
    xb1= z.xb1;xb2=z.xb2;nroot = z.nroot;
    for (i=0;i<nroot;i++)
      localflag = localflag || (fx.funk(xb1[i])*fx.funk(xb2[i]) >= 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** zbrak: One of the returned intervals does not contain a root.");
      
    }

    for (i=0;i<nroot;i++)
      localflag=localflag || (y[i] < xb1[i]) || (y[i] > xb2[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** zbrak: A known root of bessj0 does not fall in the identified intervals.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Func_zbrak implements UniVarRealValueFun {
    Bessjy b = new Bessjy();
    
    public double funk (final double x) {
      return b.j0(x);
    }
  };
}
