package com.nr.test.test_chapter17;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ode.DerivativeInf;
import com.nr.ode.Odeint;
import com.nr.ode.Output;
import com.nr.ode.StepperRoss;

public class Test_StepperRoss {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nvar=3;
    final double atol=1.0e-6,rtol=atol,h1=2.9e-4,hmin=0.0,x1=0.0,x2=50.0;
    double sbeps;
    //dydx= new double[nvar]
    double[] y= new double[nvar],yout= new double[nvar];
    boolean localflag, globalflag=false;

    

    // Test StepperRoss
    System.out.println("Testing StepperRoss");

    y[0]=1.0; y[1]=1.0; y[2]=0.0;
    Output out=new Output(20);
    rhs_StepperRoss d = new rhs_StepperRoss();
    StepperRoss s = new StepperRoss();
    Odeint ode = new Odeint(y,x1,x2,atol,rtol,h1,hmin,out,d,s);
    ode.integrate();

    for (i=0;i<nvar;i++) {
      yout[i]=out.ysave[i][out.count-1];
//      System.out.printf(setprecision(12) << yout[i]);
    }

    sbeps = 1.e-6;
    System.out.println(abs(yout[2]-yout[0]-yout[1]+2.0));
    localflag = abs(yout[2]-yout[0]-yout[1]+2.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** StepperRoss: Inaccurate relationship #1 among y[i] components");
      
    }

    System.out.println(abs(yout[1]-pow(yout[0]*exp(0.013*x2),2.5)));
    localflag = abs(yout[1]-pow(yout[0]*exp(0.013*x2),2.5)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** StepperRoss: Inaccurate relationship #2 among y[i] components");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class rhs_StepperRoss implements DerivativeInf{
    public void derivs(final double x,double[] y,double[] dydx) {
      dydx[0]= -0.013*y[0]-1000.0*y[0]*y[2];
      dydx[1]= -2500.0*y[1]*y[2];
      dydx[2]= -0.013*y[0]-1000.0*y[0]*y[2]-2500.0*y[1]*y[2];
    }

    public void jacobian(final double x,double[] y,double[] dfdx,double[][] dfdy) {
      int n=y.length;
      for (int i=0;i<n;i++) dfdx[i]=0.0;
      dfdy[0][0]= -0.013-1000.0*y[2];
      dfdy[0][1]= 0.0;
      dfdy[0][2]= -1000.0*y[0];
      dfdy[1][0]= 0.0;
      dfdy[1][1]= -2500.0*y[2];
      dfdy[1][2]= -2500.0*y[1];
      dfdy[2][0]= -0.013-1000.0*y[2];
      dfdy[2][1]= -2500.0*y[2];
      dfdy[2][2]= -1000.0*y[0]-2500.0*y[1];
    }
  }
}
