package com.nr.test.test_chapter17;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ode.DerivativeInf;
import com.nr.ode.Odeint;
import com.nr.sf.Bessjy;

public class Test_rk4 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nvar=4;
    double x,h,sbeps;
    double[] y= new double[nvar],dydx= new double[nvar],yout= new double[nvar],yexp= new double[nvar];
    boolean localflag, globalflag=false;

    

    // Test rk4
    System.out.println("Testing rk4");

    x=1.0;
    h=0.1;

    Bessjy bess=new Bessjy();
    for (i=0;i<nvar;i++) {
      y[i]=bess.jn(i,x);
      yexp[i]=bess.jn(i,x+h);
    }

    rk4_derivs(x,y,dydx);
    RK4_derivs rk4_derivs = new RK4_derivs();
    Odeint.rk4(y,dydx,x,h,yout,rk4_derivs);

    for (i=0;i<nvar;i++)
      System.out.printf("%f  %f\n", yout[i], yexp[i]);

    sbeps = 1.e-6;
    System.out.println(maxel(vecsub(yout,yexp)));
    localflag = maxel(vecsub(yout,yexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rk4: Inaccurate Runge-Kutta step");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  void rk4_derivs(final double x,final double[] y,final double[] dydx) {
    dydx[0]= -y[1];
    dydx[1]=y[0]-(1.0/x)*y[1];
    dydx[2]=y[1]-(2.0/x)*y[2];
    dydx[3]=y[2]-(3.0/x)*y[3];
  }
  
  class RK4_derivs implements DerivativeInf {
    public void derivs(final double x, double[] y, double[] dydx){
      rk4_derivs(x,y,dydx);
    }
    public void jacobian(final double x, double[] y, double[] dfdx, double[][] dfdy){}
  }
  

}
