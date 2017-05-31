package com.nr.test.test_chapter10;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.RealValueFun;
import com.nr.UniVarRealValueFun;
import com.nr.min.Amoeba;
import com.nr.sf.Bessjy;

public class Test_Amoeba {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20,M=2;
    double tol=1.e-12,del=1.0,sbeps=0.;
    double[] point=new double[M],min=new double[M];
    boolean localflag=false, globalflag=false;

    Func_Amoeba Func_Amoeba= new Func_Amoeba();
    Bessj1_Amoeba Bessj1_Amoeba = new Bessj1_Amoeba();

    // Test Amoeba
    System.out.println("Testing Amoeba, interface1");
    Amoeba amb1=new Amoeba(tol);
    for (i=0;i<N;i++) {
      point[0]=point[1]=(2*i);
      min=amb1.minimize(point,del,Func_Amoeba);
      sbeps=sqrt(tol);
      localflag = localflag || (abs(-Bessj1_Amoeba.funk(min[0])) > sbeps)
          || (abs(2*min[1]) > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amoeba, interface1: Gradient not zero at one of the identified minima");
      
    }

    System.out.println("Testing Amoeba, interface2");
    Amoeba amb2 = new Amoeba(tol);
    double[] dels=new double[M];
    dels[0]=del;
    dels[1]=0.5*del;
    for (i=0;i<N;i++) {
      point[0]=point[1]=(2*i);
      min=amb2.minimize(point,dels,Func_Amoeba);
      sbeps=sqrt(tol);
      localflag = localflag || (abs(-Bessj1_Amoeba.funk(min[0])) > sbeps)
          || (abs(2*min[1]) > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amoeba, interface2: Gradient not zero at one of the identified minima");
      
    }

    System.out.println("Testing Amoeba, interface3");
    Amoeba amb3 = new Amoeba(tol);
    double[][] pp = new double[3][2];
    for (i=0;i<N;i++) {
      pp[0][0]=(2*i);
      pp[0][1]=(2*i);
      pp[1][0]=pp[0][0]+del;
      pp[1][1]=pp[0][1];
      pp[2][0]=pp[0][0];
      pp[2][1]=pp[0][1]+0.5*del;
      min=amb3.minimize(pp,Func_Amoeba);
      localflag = localflag || (abs(-Bessj1_Amoeba.funk(min[0])) > sbeps)
          || (abs(2*min[1]) > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amoeba, interface3: Gradient not zero at one of the identified minima");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Func_Amoeba implements RealValueFun {
    public double funk(final double[] x) {
    Bessjy b = new Bessjy();
    return(b.j0(x[0])+pow(x[1],2.0));
    }
  }
  class Bessj1_Amoeba implements UniVarRealValueFun {
    public double funk(final double x) {
    Bessjy b = new Bessjy();
    return(b.j1(x));
  }
  }
}
