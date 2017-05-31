package com.nr.test.test_chapter15;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealMultiValueFun;
import com.nr.model.Fitlin;
import com.nr.model.Fitsvd;
import com.nr.ran.Normaldev;

public class Test_Fitsvd implements UniVarRealMultiValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,ma,N=100;
    double stdev,TOL=1.e-12,sbeps,amp[]={1.0,0.1,1.0,0.3,0.1};
    double[] x= new double[N],y= new double[N],sig= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Fitsvd
    System.out.println("Testing Fitsvd");

    double[] f=Fitsvd_funcs(x[0]);
    ma=f.length;
    stdev=0.02;
    Normaldev ndev = new Normaldev(0.0,stdev,17);

    for (i=0;i<N;i++) {   // Create a data set
      x[i]=0.1*(i+1);
      f=Fitsvd_funcs(x[i]);
      y[i]=0.0;
      for (j=0;j<ma;j++) y[i] += amp[j]*f[j];
      y[i] += ndev.dev();
      sig[i]=stdev;
    }

    Fitsvd mysvdfit = new Fitsvd(x,y,sig,this,TOL);
    mysvdfit.fit();

    for (j=0;j<ma;j++) {
      localflag = abs(mysvdfit.a[j]-amp[j]) > 2.0*sqrt(mysvdfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitsvd: Fitted parameters not within estimated uncertainty");
        
      }
    }

    Fitlin mylinfit = new Fitlin(x,y,sig,this);
    mylinfit.fit();
    sbeps=TOL;
    for (j=0;j<ma;j++) {
//      System.out.printf(mysvdfit.a[j] << " %f\n", mylinfit.a[j] << " ";
//      System.out.printf(sqrt(mysvdfit.covar[j][j]) << " %f\n", sqrt(mylinfit.covar[j][j]));
      localflag = abs(mysvdfit.a[j]-mylinfit.a[j]) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitsvd: SVD fit does not agree with Fitlin on parameters");
        
      }

      localflag = abs(sqrt(mysvdfit.covar[j][j])-sqrt(mylinfit.covar[j][j])) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitsvd: SVD fit does not agree with Fitlin on uncertainties");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public double[] funk(final double x) {
    return Fitsvd_funcs(x);
  }
  
  double[] Fitsvd_funcs(final double x) {
    int i;
    double[] ans=new double[5];

    ans[0]=1.0;
    ans[1]=x;
    for (i=2;i<5;i++) ans[i]=sin((i-1)*x);
    return ans;
  }

}
