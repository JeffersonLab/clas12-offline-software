package com.nr.test.test_chapter4;

import static com.nr.fi.GaussianWeights.gauleg;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.sf.Bessjy;

public class Test_gauleg {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=10,M=10;
    double x1,x2,val,sbeps=51.e-14;
    double[] y= new double[N],yy= new double[N],x= new double[M],w= new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test gauleg
    System.out.println("Testing gauleg");
    Bessj0_gauleg bj0 = new Bessj0_gauleg();
    Bessj1_gauleg bj1 = new Bessj1_gauleg();
    for (i=0;i<N;i++) {
      x1=1.0*i;
      x2=x1+2.0;
      gauleg(x1,x2,x,w);
      val=0.0;
      for (j=0;j<M;j++) val += w[j]*bj1.funk(x[j]);
      y[i]=val;
      yy[i]=bj0.funk(x1)-bj0.funk(x2);
    }
    System.out.printf("gauleg: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** gauleg: Failure to achieve accurate integral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Bessj0_gauleg implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b=new Bessjy();
      return(b.j0(x));
    }
  };

  class Bessj1_gauleg implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b =new Bessjy();
      return(b.j1(x));
    }
  };
}
