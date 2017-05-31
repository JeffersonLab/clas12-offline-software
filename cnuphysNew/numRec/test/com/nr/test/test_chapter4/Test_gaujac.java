package com.nr.test.test_chapter4;

import static com.nr.fi.GaussianWeights.gaujac;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.sf.Bessjy;

public class Test_gaujac {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=8,M=20;
    double alfa,beta,val,sbeps=1.e-11;
    double[] y= new double[N],yy= new double[N],x= new double[M],w= new double[M];
    boolean localflag, globalflag=false;

    // Test gaujac
    System.out.println("Testing gaujac");
    Bessj0_gaujac bj0 = new Bessj0_gaujac();
    Bessj1_gaujac bj1 = new Bessj1_gaujac();
    for (i=0;i<N;i++) {
      alfa=(double)(i)+1.0;
      beta=(double)(N)-i;
      gaujac(x,w,alfa,beta);
      val=0.0;
      for (j=0;j<M;j++) val += w[j]*bj1.funk(x[j]);
      y[i]=val;
      gaujac(x,w,alfa-1.0,beta);
      val=0.0;
      for (j=0;j<M;j++) val += w[j]*bj0.funk(x[j]);
      yy[i]= -alfa*val;
      gaujac(x,w,alfa,beta-1.0);
      val=0.0;
      for (j=0;j<M;j++) val += w[j]*bj0.funk(x[j]);
      yy[i] += beta*val;
    }
    System.out.printf("gaujac: Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** gaujac: Failure to achieve accurate integral");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Bessj0_gaujac implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b = new Bessjy();
      return(b.j0(x));
    }
  };

  class Bessj1_gaujac implements UniVarRealValueFun{
    public double funk (double x) {
      Bessjy b = new Bessjy();
      return(b.j1(x));
    }
  }
}
