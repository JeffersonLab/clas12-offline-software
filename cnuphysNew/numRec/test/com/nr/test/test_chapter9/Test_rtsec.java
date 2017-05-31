package com.nr.test.test_chapter9;

import static com.nr.NRUtil.buildVector;
import static com.nr.root.Roots.rtsec;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.root.Roots.Zbrak;
import com.nr.sf.Bessjy;

public class Test_rtsec {

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
    double sbeps=1.e-12,xacc=0.;
    double x1=1.0,x2=50.0;
    double y[]={2.404825557695773,5.520078110286311,8.653727912911013,
      1.179153443901428e1,1.493091770848779e1,1.807106396791092e1,
      2.121163662987926e1,2.435247153074930e1,2.749347913204025e1,
      3.063460646843198e1,3.377582021357357e1,3.691709835366405e1,
      4.005842576462825e1,4.319979171317673e1,4.634118837166182e1,
      4.948260989739782e1};
    double[] yy=buildVector(y);
    boolean localflag, globalflag=false;

    

    // Test rtsec
    System.out.println("Testing rtsec");
    Func_rtsec fx = new Func_rtsec();
    Zbrak z = new Zbrak();  z.zbrak(fx,x1,x2,100);
    xb1= z.xb1;xb2=z.xb2;nroot = z.nroot;
    double[] root = new double[nroot];
    for (i=0;i<nroot;i++) {
      xacc=0.5*sbeps*(abs(xb1[i])+abs(xb2[i]));
      root[i]=rtsec(fx,xb1[i],xb2[i],xacc);
    }
    System.out.printf("rtsec: Maximum discrepancy = %f\n", maxel(vecsub(root,yy)));
    localflag = maxel(vecsub(root,yy)) > xacc;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rtsec: Incorrect roots");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Func_rtsec implements UniVarRealValueFun {
    Bessjy b = new Bessjy();
    public double funk (final double x) {
      return b.j0(x);
    }
  }
}
