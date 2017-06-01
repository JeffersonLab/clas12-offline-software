package com.nr.test.test_chapter4;

import static com.nr.fi.GaussianWeights.gaulag;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.sf.Bessjy;

public class Test_gaulag {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=8,M=20;
    double alf,val,sbeps=1.e-8;
    double[] y= new double[N],yy= new double[N],x= new double[M],w= new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test gaulag
    System.out.println("Testing gaulag");
    Bessj0_gaulag bj0 = new Bessj0_gaulag();
    Bess_sum bs = new Bess_sum();
    for (i=0;i<N;i++) {
      alf=0.5*i;
      gaulag(x,w,alf);
      val=0.0;
      for (j=0;j<M;j++) val += w[j]*bj0.funk(x[j]);
      y[i]=(alf+1.0)*val;
      gaulag(x,w,alf+1);
      val=0.0;
      for (j=0;j<M;j++) val += w[j]*bs.funk(x[j]);
      yy[i]=val;
    }
    System.out.printf("gaulag: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** gaulag: Failure to achieve accurate integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Bessj0_gaulag implements UniVarRealValueFun{
    public double funk(double x) {
      Bessjy b = new Bessjy();
      return(b.j0(x));
    }
  };

  class Bessj1_gaulag implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b= new Bessjy();
      return(b.j1(x));
    }
  };

  class Bess_sum implements UniVarRealValueFun{
    public double funk(double x) {
      Bessj0_gaulag b0 = new Bessj0_gaulag();
      Bessj1_gaulag b1 = new Bessj1_gaulag();
      return(b0.funk(x)+b1.funk(x));
    }
  }
}
