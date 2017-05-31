package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildMatrix;
import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Moment.avevar;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Multinormaldev;

public class Test_Multinormaldev {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,DIM=3,N=10000;
    double xave,xvar,yave,yvar,zave,zvar;
    doubleW xaveW=new doubleW(0),xvarW=new doubleW(0);
    doubleW yaveW=new doubleW(0),yvarW=new doubleW(0);
    doubleW zaveW=new doubleW(0),zvarW=new doubleW(0);
    double covxy=0.0,covxz=0.0,covyz=0.0,sbeps=0.05;
    double mmean[]={1.0,2.0,3.0};
    double ccovar[]={1.0,0.5,0.5,0.5,2.0,0.5,0.5,0.5,3.0};
    double[] mean = buildVector(mmean),a = new double[DIM],x= new double[N],y= new double[N],z= new double[N];
    double[][] covar = buildMatrix(DIM,DIM,ccovar);
    boolean localflag, globalflag=false;
    
    

    // Test Multinormaldev
    System.out.println("Testing Multinormaldev");

    Multinormaldev multi =new Multinormaldev(17L,mean,covar);

    for (i=0;i<N;i++) {
      a=multi.dev();
      x[i]=a[0];
      y[i]=a[1];
      z[i]=a[2];
    }

    avevar(x,xaveW,xvarW); xave = xaveW.val; xvar = xvarW.val;
    avevar(y,yaveW,yvarW); yave = yaveW.val; yvar = yvarW.val;
    avevar(z,zaveW,zvarW); zave = zaveW.val; zvar = zvarW.val;

    // Test the covariance matrix
    for (i=0;i<N;i++) {
      covxy += (x[i]-xave)*(y[i]-yave);
      covxz += (x[i]-xave)*(z[i]-zave);
      covyz += (y[i]-yave)*(z[i]-zave);
    }
    covxy /= N;
    covxz /= N;
    covyz /= N;

//    System.out.printf(xave << " %f\n", yave << " %f\n", zave);
//    System.out.printf(xvar << " %f\n", yvar << " %f\n", zvar);
//    System.out.printf(covxy << " %f\n", covxz << " %f\n", covyz);

    localflag = abs(1.0-xave/mean[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Mean value of x does not match target value");
      
    }

    localflag = abs(1.0-yave/mean[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Mean value of y does not match target value");
      
    }

    localflag = abs(1.0-zave/mean[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Mean value of z does not match target value");
      
    }

    localflag = abs(1.0-xvar/covar[0][0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Variance of x does not match target value");
      
    }

    localflag = abs(1.0-yvar/covar[1][1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Variance of y does not match target value");
      
    }

    localflag = abs(1.0-zvar/covar[2][2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Variance of z does not match target value");
      
    }

    localflag = abs(1.0-covxy/covar[0][1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Covariance <xy> does not match target value");
      
    }

    localflag = abs(1.0-covxz/covar[0][2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Covariance <xz> does not match target value");
      
    }

    localflag = abs(1.0-covyz/covar[1][2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Multinormaldev: Covariance <yz> does not match target value");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
