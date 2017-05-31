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
import com.nr.ran.Normaldev;
public class Test_Fitlin implements UniVarRealMultiValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,ma,N=100;
    double stdev,amp[]={1.0,0.1,1.0,0.3,0.1};
    double[] x=new double[N],y= new double[N],sig= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Fitlin
    System.out.println("Testing Fitlin");

    double[] f=Fitlin_funcs(x[0]);
    ma=f.length;
    stdev=0.02;
    Normaldev ndev=new Normaldev(0.0,stdev,17);

    for (i=0;i<N;i++) {   // Create a data set
      x[i]=0.1*(i+1);
      f=Fitlin_funcs(x[i]);
      y[i]=0.0;
      for (j=0;j<ma;j++) y[i] += amp[j]*f[j];
      y[i] += ndev.dev();
      sig[i]=stdev;
    }

    Fitlin myfit = new Fitlin(x,y,sig,this);
    myfit.fit();

    for (j=0;j<ma;j++) {
      localflag = abs(myfit.a[j]-amp[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitlin: Fitted parameters not within estimated uncertainty");
        
      }
    }

//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<ma;i++) {
//      System.out.printf(setw(8) << myfit.a[i];
//      System.out.printf(setw(13) << sqrt(myfit.covar[i][i]));
//    }
//    System.out.printf(scientific << setprecision(4);
//    for (i=0;i<ma;i++) {
//      for (j=0;j<ma;j++) System.out.printf(setw(15) << myfit.covar[i][j];
//      System.out.printf(endl;
//    }
//    System.out.printf(endl;

    // Now check results of restricting fit parameters 1 and 3
    myfit.hold(1,amp[1]);
    myfit.hold(3,amp[3]);
    myfit.fit();

    localflag = (myfit.a[1] != amp[1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have its assigned value");
      
    }

    localflag = (myfit.a[3] != amp[3]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have its assigned value");
      
    }

    localflag = (myfit.covar[1][1] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have uncertainty=0.0");
      
    }

    localflag = (myfit.covar[3][3] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have uncertainty=0.0");
      
    }

    for (j=0;j<ma;j++) {
      localflag = abs(myfit.a[j]-amp[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitlin: Fitted parameters (with 2 parameters held) not within estimated uncertainty");
        
      }
    }

    localflag=false;
    for (i=0;i<ma;i++) {
      for (j=0;j<ma;j++) {
        if (i==1 || i==3 || j==1 || j==3)
          localflag = localflag || myfit.covar[i][j] != 0.0;
        else
          localflag = localflag || myfit.covar[i][j] == 0.0;
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: Covariance matrix with 2 held parameters has incorrect pattern");
      
    }

//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<ma;i++) {
//      System.out.printf(setw(8) << myfit.a[i];
//      System.out.printf(setw(13) << sqrt(myfit.covar[i][i]));
//    }
//    System.out.printf(scientific << setprecision(4);
//    for (i=0;i<ma;i++) {
//      for (j=0;j<ma;j++) System.out.printf(setw(15) << myfit.covar[i][j];
//      System.out.printf(endl;
//    }
//    System.out.printf(endl;

    // Now free one of the fixed parameters
    myfit.free(1);
    myfit.fit();

    localflag = (myfit.a[1] == amp[1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A free parameter still has its assigned value");
      
    }

    localflag = (myfit.a[3] != amp[3]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have its assigned value");
      
    }

    localflag = (myfit.covar[1][1] == 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A freed parameter still has uncertainty=0.0");
      
    }

    localflag = (myfit.covar[3][3] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have uncertainty=0.0");
      
    }

    for (j=0;j<ma;j++) {
      localflag = abs(myfit.a[j]-amp[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitlin: Fitted parameter (with 1 parameters held) not within estimated uncertainty");
        
      }
    }

    localflag=false;
    for (i=0;i<ma;i++) {
      for (j=0;j<ma;j++) {
        if (i==3 || j==3)
          localflag = localflag || myfit.covar[i][j] != 0.0;
        else
          localflag = localflag || myfit.covar[i][j] == 0.0;
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: Covariance matrix with 1 held parameters has incorrect pattern");
      
    }

//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<ma;i++) {
//      System.out.printf(setw(8) << myfit.a[i];
//      System.out.printf(setw(13) << sqrt(myfit.covar[i][i]));
//    }
//    System.out.printf(scientific << setprecision(4);
//    for (i=0;i<ma;i++) {
//      for (j=0;j<ma;j++) System.out.printf(setw(15) << myfit.covar[i][j];
//      System.out.printf(endl;
//    }
//    System.out.printf(endl;


    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public double[] funk(final double x) {
    return Fitlin_funcs(x);
  }
  
  double[] Fitlin_funcs(final double x)
  {
    int i;
    double[] ans=new double[5];

    ans[0]=1.0;
    ans[1]=x;
    for (i=2;i<5;i++) ans[i]=sin((i-1)*x);
    return ans;
  }

}
