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
public class Test_fleg implements UniVarRealMultiValueFun{

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
    double amp[]={1.0,2.0,3.0,2.0,1.0,0.0,0.0,0.0,0.0,0.0};
    double[] x= new double[N],y= new double[N],sig= new double[N];
    boolean localflag, globalflag=false;

    

    // Test fleg
    System.out.println("Testing fleg");

    stdev=1.e-3;
    double[] f=fleg(x[0]);
    int NP=f.length;
    Normaldev ndev = new Normaldev(0.0,stdev,17);

    for (i=0;i<N;i++) {   // Create a data set
      x[i]=-1.0+0.02*(i+1);
      f=fleg(x[i]);
      y[i]=0.0;
      for (j=0;j<NP;j++) y[i] += amp[j]*f[j];
      y[i] += ndev.dev();
      sig[i]=stdev;
    }

    Fitsvd myfit=new Fitsvd(x,y,sig,this,TOL);
    myfit.fit();

    for (j=0;j<NP;j++) {
//      System.out.printf(myfit.a[j] << " %f\n", amp[j] << " " ;
//      System.out.printf(sqrt(myfit.covar[j][j]));

      localflag = abs(myfit.a[j]-amp[j]) > 2.0*sqrt(myfit.covar[j][j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** fleg: Fitted parameters not within estimated uncertainty");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public double[] funk(final double x) {
    return fleg(x);
  }
  
  double[] fleg(final double x) {
    final int fleg_nl = 10;

    int j;
    double twox,f2,f1,d;
    double[] pl =new double[fleg_nl];
    pl[0]=1.;
    pl[1]=x;
    if (fleg_nl > 2) {
      twox=2.*x;
      f2=x;
      d=1.;
      for (j=2;j<fleg_nl;j++) {
        f1=d++;
        f2+=twox;
        pl[j]=(f2*pl[j-1]-f1*pl[j-2])/d;
      }
    }
    return pl;
  }
}
