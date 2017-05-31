package com.nr.test.test_chapter15;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealMultiValueFun;
import com.nr.model.Fitsvd;
import com.nr.ran.Normaldev;

public class Test_fpoly implements UniVarRealMultiValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=100;
    double stdev,TOL=1.e-12;
    double amp[]={1.0,0.1,0.01,0.001,0.0001,0.0,0.0,0.0,0.0,0.0};
    double[] x= new double[N],y= new double[N],sig= new double[N];
    boolean localflag, globalflag=false;

    

    // Test fpoly
    System.out.println("Testing fpoly");

    stdev=1.e-6;
    double[] f=fpoly(x[0]);
    int NP=f.length;
    Normaldev ndev = new Normaldev(0.0,stdev,17);

    for (i=0;i<N;i++) {   // Create a data set
      x[i]=0.1*(i+1);
      f=fpoly(x[i]);
      y[i]=0.0;
      for (j=0;j<NP;j++) y[i] += amp[j]*f[j];
      y[i] += ndev.dev();
      sig[i]=stdev;
    }

    Fitsvd myfit = new Fitsvd(x,y,sig,this,TOL);
    myfit.fit();

    for (j=0;j<NP;j++) {
//      System.out.printf(myfit.a[j] << " %f\n", amp[j] << " " ;
//      System.out.printf(sqrt(myfit.covar[j][j]));

      localflag = abs(myfit.a[j]-amp[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** fpoly: Fitted parameters not within estimated uncertainty");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public double[] funk(final double x) {
    return fpoly(x);
  }
  
  double[] fpoly(final double x) {
    final int fpoly_np=10;
    int j;
    double[] p=new double[fpoly_np];
    p[0]=1.0;
    for (j=1;j<fpoly_np;j++) p[j]=p[j-1]*x;
    return p;
  }
}
