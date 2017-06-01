package com.nr.test.test_chapter4;

import static com.nr.NRUtil.SQR;
import static com.nr.fi.GaussianWeights.gauher;
import static com.nr.fi.Trapzd.qsimp;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.sf.Bessjy;

public class Test_gauher {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,N=8,M=20;
    double val,sbeps=1.e-14;
    double[] y= new double[N],yy= new double[N],x = new double[M],w = new double[M];
    boolean localflag, globalflag=false;
    
    // Test gauher
    System.out.println("Testing gauher");
    Bessjn_gauher bjn = new Bessjn_gauher();
    gauher(x,w);
    for (i=0;i<N;i++) {
      k=2*i;    // Evaluate with gauher
      val=0.0;
      for (j=0;j<M;j++) val += w[j]*bjn.get(k,x[j]);
      y[i]=val; // Evaluate with qsimp
      Func_gauher func = new Func_gauher(2*i);
      yy[i]=qsimp(func,-10.0,10.0);
    }
    System.out.printf("gauher: Maximum discrepancy = %g\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** gauher: Failure to achieve accurate integral");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  class  Bessjn_gauher {
    double get(int n,double x) {
      Bessjy b = new Bessjy();
      return(b.jn(n,x));
    }
  };

  class Func_gauher implements UniVarRealValueFun {
    int n;

    Func_gauher(int nn) {n = nn;}

    public double funk (double x) {
      Bessjn_gauher bjn = new Bessjn_gauher() ;
      return(exp(-SQR(x))*bjn.get(n,x));
    }
  };

}
