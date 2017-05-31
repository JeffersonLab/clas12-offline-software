package com.nr.test.test_chapter17;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ode.DerivativeInf;
import com.nr.ode.Odeint;
import com.nr.ode.Output;
import com.nr.ode.StepperDopr5;
import com.nr.sf.Bessjy;

public class Test_StepperDopr5 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nvar=4;
    final double atol=1.0e-6,rtol=atol,h1=0.01,hmin=0.0,x1=1.0,x2=2.0;
    double sbeps;
    //dydx= new double[nvar]
    double[] y= new double[nvar],yout= new double[nvar],yexp= new double[nvar];
    boolean localflag, globalflag=false;

    

    // Test StepperDopr5
    System.out.println("Testing StepperDopr5");

    Bessjy bess = new Bessjy();
    for (i=0;i<nvar;i++) {
      y[i]=bess.jn(i,x1);
      yexp[i]=bess.jn(i,x2);
    }
    Output out = new Output(20);
    rhs_StepperDopr5 d = new rhs_StepperDopr5();
    StepperDopr5 s = new StepperDopr5();
    Odeint ode = new Odeint(y,x1,x2,atol,rtol,h1,hmin,out,d,s);
    ode.integrate();

    for (i=0;i<nvar;i++) {
      yout[i]=out.ysave[i][out.count-1];
      System.out.printf("%f  %f\n", yout[i],yexp[i]);
    }

    sbeps = 1.e-6;
    System.out.println(maxel(vecsub(yout,yexp)));
    localflag = maxel(vecsub(yout,yexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** StepperDopr5: Inaccurate integration");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class rhs_StepperDopr5 implements DerivativeInf {
    public void derivs (final double x, final double[] y, final double[]dydx) {
      dydx[0]= -y[1];
      dydx[1]=y[0]-(1.0/x)*y[1];
      dydx[2]=y[1]-(2.0/x)*y[2];
      dydx[3]=y[2]-(3.0/x)*y[3];
    }
    public void jacobian(final double x, double[] y, double[] dfdx, double[][] dfdy){}

  }
}
