package com.nr.test.test_chapter15;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.model.FGauss;
import com.nr.model.Fitmrq;
import com.nr.ran.Normaldev;

public class Test_Fitmrq {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=100,MA=6;
    double aa[]={5.0,2.0,3.0,2.0,5.0,3.0};
    double gguess[]={4.5,2.2,2.8,2.5,4.9,2.8};
    double SPREAD=0.01;
    double[] x= new double[N],y= new double[N],sig= new double[N];
    double[] a=buildVector(aa),guess=buildVector(gguess);
    boolean localflag, globalflag=false;

    

    // Test Fitmrq
    System.out.println("Testing Fitmrq");

    Normaldev ndev=new Normaldev(0.0,1.0,17);
    // First try a sum of two Gaussians
    for (i=0;i<N;i++) {
      x[i]=0.1*(i+1);
      y[i]=0.0;
      for (j=0;j<MA;j+=3)
        y[i] += a[j]*exp(-SQR((x[i]-a[j+1])/a[j+2]));
      y[i] *= (1.0+SPREAD*ndev.dev());
      sig[i]=SPREAD*y[i];
    }
    FGauss fgauss = new FGauss();
    Fitmrq myfit=new Fitmrq(x,y,sig,guess,fgauss);
    myfit.fit();

//    System.out.printf(setw(18) << "chi-squared:%f\n", setw(13) << myfit.chisq);
//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << myfit.a[i];
//    System.out.printf(endl;
//    System.out.println("Uncertainties:");
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << sqrt(myfit.covar[i][i]);
//    System.out.printf(endl;
//    System.out.println("Expected results:");
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << a[i];
//    System.out.printf(endl);

    for (j=0;j<MA;j++) {
      localflag = abs(myfit.a[j]-a[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitmrq: Fitted parameters not within estimated uncertainty");
        
      }
    }

    // Test the hold() method on parameters 1 and 4
    myfit.hold(1,2.0);
    myfit.hold(4,5.0);
    myfit.fit();

//    System.out.printf(setw(18) << "chi-squared:%f\n", setw(13) << myfit.chisq);
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << myfit.a[i];
//    System.out.printf(endl;
//    System.out.println("Uncertainties:");
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << sqrt(myfit.covar[i][i]);
//    System.out.printf(endl;
//    System.out.println("Expected results:");
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << a[i];
//    System.out.printf(endl);

    localflag = (myfit.a[1] != a[1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmrq: A held parameter does not have its assigned value");
      
    }

    localflag = (myfit.a[4] != a[4]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmrq: A held parameter does not have its assigned value");
      
    }

    localflag = (myfit.covar[1][1] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmrq: A held parameter does not have uncertainty=0.0");
      
    }

    localflag = (myfit.covar[4][4] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmrq: A held parameter does not have uncertainty=0.0");
      
    }

    for (j=0;j<MA;j++) {
      localflag = abs(myfit.a[j]-a[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitmrq: Fitted parameters (with 2 parameters held) not within estimated uncertainty");
        
      }
    }

    localflag=false;
    for (i=0;i<MA;i++) {
      for (j=0;j<MA;j++) {
        if (i==1 || i==4 || j==1 || j==4)
          localflag = localflag || myfit.covar[i][j] != 0.0;
        else
          localflag = localflag || myfit.covar[i][j] == 0.0;
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitmrq: Covariance matrix with 2 held parameters has incorrect pattern");
      
    }

    // Test the free() method
    myfit.free(1);
    myfit.fit();

//    System.out.printf(setw(18) << "chi-squared:%f\n", setw(13) << myfit.chisq);
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << myfit.a[i];
//    System.out.printf(endl;
//    System.out.println("Uncertainties:");
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << sqrt(myfit.covar[i][i]);
//    System.out.printf(endl;
//    System.out.println("Expected results:");
//    for (i=0;i<MA;i++) System.out.printf(setw(9) << a[i];
//    System.out.printf(endl);

    localflag = (myfit.a[1] == a[1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A freed parameter still has its assigned value");
      
    }

    localflag = (myfit.a[4] != a[4]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have its assigned value");
      
    }

    localflag = (myfit.covar[1][1] == 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A freed parameter still has uncertainty=0.0");
      
    }

    localflag = (myfit.covar[4][4] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: A held parameter does not have uncertainty=0.0");
      
    }

    for (j=0;j<MA;j++) {
      localflag = abs(myfit.a[j]-a[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitlin: Fitted parameter (with 1 parameters held) not within estimated uncertainty");
        
      }
    }

    localflag=false;
    for (i=0;i<MA;i++) {
      for (j=0;j<MA;j++) {
        if (i==4 || j==4)
          localflag = localflag || myfit.covar[i][j] != 0.0;
        else
          localflag = localflag || myfit.covar[i][j] == 0.0;
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitlin: Covariance matrix with 1 held parameters has incorrect pattern");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
