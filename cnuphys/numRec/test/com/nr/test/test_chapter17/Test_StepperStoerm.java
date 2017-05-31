package com.nr.test.test_chapter17;

import static com.nr.sf.Gamma.gammln;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ode.DerivativeInf;
import com.nr.ode.Odeint;
import com.nr.ode.Output;
import com.nr.ode.StepperStoerm;
import com.nr.sf.Bessel;

public class Test_StepperStoerm {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nvar=2;
    double atol=1.0e-6,rtol=atol,h1=0.01,hmin=0.0,x1,x2,sbeps;
    double[] y=new double[2*nvar],dydx=new double[2*nvar],yout = new double[nvar],yexp=new double[nvar];
    boolean localflag, globalflag=false;

    

    // Test StepperStoerm
    System.out.println("Testing StepperStoerm");

    Bessel bess =new Bessel();
    y[0]=1.0/pow(3.0,2.0/3.0)/exp(gammln(2.0/3.0));     // Ai(x)
    y[nvar]=-1.0/pow(3.0,1.0/3.0)/exp(gammln(1.0/3.0));
    dydx[0]=0.0;
    y[1]=1.0/pow(3.0,1.0/6.0)/exp(gammln(2.0/3.0));     // Bi(x)
    y[nvar+1]=pow(3.0,1.0/6.0)/exp(gammln(1.0/3.0));
    dydx[1]=0.0;
    
    Output out=new Output(20);
    x1=0.0;x2=1.0;
    rhs_StepperStoerm d = new rhs_StepperStoerm();
    StepperStoerm s1= new StepperStoerm();
    Odeint ode1 = new Odeint(y,x1,x2,atol,rtol,h1,hmin,out,d,s1);
    ode1.integrate();
    yexp[0]=bess.airy_ai(1.0);
    yexp[1]=bess.airy_bi(1.0);

    for (i=0;i<nvar;i++) {
      yout[i]=out.ysave[i][out.count-1];
      System.out.printf("%f  %f\n", yout[i],yexp[i]);
    }

    sbeps = 1.e-8;
    System.out.println(maxel(vecsub(yout,yexp)));
    localflag = maxel(vecsub(yout,yexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** StepperStoerm: Inaccurate integration #1");
      
    }

    y[0]=1.0/pow(3.0,2.0/3.0)/exp(gammln(2.0/3.0));     // Ai(x)
    y[nvar]=-1.0/pow(3.0,1.0/3.0)/exp(gammln(1.0/3.0));
    dydx[0]=0.0;
    y[1]=1.0/pow(3.0,1.0/6.0)/exp(gammln(2.0/3.0));     // Bi(x)
    y[nvar+1]=pow(3.0,1.0/6.0)/exp(gammln(1.0/3.0));
    dydx[1]=0.0;

    x1=0.0;x2=-1.0;
    StepperStoerm s2 = new StepperStoerm();
    Odeint ode2 = new Odeint(y,x1,x2,atol,rtol,h1,hmin,out,d,s2);
    ode2.integrate();
    yexp[0]=bess.airy_ai(-1.0);
    yexp[1]=bess.airy_bi(-1.0);

    for (i=0;i<nvar;i++) {
      yout[i]=out.ysave[i][out.count-1];
      System.out.printf("%f  %f\n", yout[i],yexp[i]);
    }

    sbeps = 1.e-8;
    System.out.println(maxel(vecsub(yout,yexp)));
    localflag = maxel(vecsub(yout,yexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** StepperStoerm: Inaccurate integration #2");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class rhs_StepperStoerm implements DerivativeInf{
    public void derivs(final double x,double[] y,double[] dydx) {
      dydx[0]=x*y[0];
      dydx[1]=x*y[1];
    }
    public void jacobian(final double x,double[] y,double[] dfdx,double[][] dfdy) {}
  }
}
