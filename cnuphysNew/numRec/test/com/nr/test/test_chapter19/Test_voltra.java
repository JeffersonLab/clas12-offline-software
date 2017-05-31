package com.nr.test.test_chapter19;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.inv.*;

public class Test_voltra {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20,M=2;
    double h=0.05,t0=0.0,sbeps;
    double[] t=new double[N],fac1= new double[N],fac2= new double[N];
    double[][] f=new double[M][N];
    boolean localflag, globalflag=false;

    

    // Test voltra
    System.out.println("Testing voltra");

    final Gfunc gfun = new Gfunc();
    final Kernel ker =new Kernel();
    
    Volterra voltra = new Volterra(){

      @Override
      public double g(int k, double t) {
        return gfun.funk(k, t);
      }

      @Override
      public double ak(int k, int l, double t, double s) {
        return  ker.funk(k, l, t, s);
      }
      
    };
    voltra.voltra(t0, h, t, f);
    
    // exact soln is f[1]=exp(-t), f[2]=2sin(t)
    for (i=0;i<N;i++) {
      fac1[i]=f[0][i]-exp(-t[i]);
      fac2[i]=f[1][i]-2.0*sin(t[i]);
    }

    sbeps = 2.0e-3;
    System.out.println(maxel(fac1));
    localflag = maxel(fac1) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** voltra: Imprecise result for function exp(-t)");
      
    }

    sbeps = 2.0e-3;
    System.out.println(maxel(fac2));
    localflag = maxel(fac2) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** voltra: Imprecise result for function 2.0*sin(t)");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Gfunc 
  {
    public double funk (final int k, final double t) {
      return (k == 0 ? cosh(t)+t*sin(t) :
        2.0*sin(t)+t*(SQR(sin(t))+exp(t)));
    }
  };

  class Kernel
  {
    public double funk (final int k, final int l, final double t, final double s) {
      return ((k == 0) ? (l == 0 ? -exp(t-s) : -cos(t-s)) :
        (l == 0 ? -exp(t+s) : -t*cos(s)));
    }
  };

}
