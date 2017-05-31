package com.nr.test.test_chapter19;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.inv.Fred2;

public class Test_Fred2 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20,M=100;
    double PI=acos(-1.0);
    double a=0.0,b=PI/2.0,sbeps,z;
    double xxexp[]={0.00539679,0.0282964,0.0689309,0.126357,0.199230,
      0.285843,0.384164,0.491890,0.606496,0.725294,0.845502,
      0.964301,1.07891,1.18663,1.28495,1.37157,1.44444,
      1.50187,1.54250,1.56540};
    double wwexp[]={0.013834,0.0318883,0.0492225,0.0654054,0.0800557,
      0.0928298,0.103428,0.111602,0.11716,0.119972,0.119972,
      0.11716,0.111602,0.103428,0.0928298,0.0800557,0.0654054,
      0.0492225,0.0318883,0.0138340};
    double[] fexp=new double[N],xexp=buildVector(xxexp),wexp=buildVector(wwexp),frac=new double[N],ff=new double[M],ffexp=new double[M];
    boolean localflag, globalflag=false;

    

    // Test Fred2
    System.out.println("Testing Fred2");

    final Gfunc gfun = new Gfunc();
    final Kernel ker = new Kernel();
    
    Fred2 fred = new Fred2(a,b,N){

      @Override
      public double g(double x) {
        return gfun.funk(x);
      }

      @Override
      public double ak(double x, double t) {
        return ker.funk(x, t);
      }
      
    };
    double[] x=fred.t;
    double[] f=fred.f;
    double[] weight=fred.w;
    for (i=0;i<N;i++) {
      fexp[i]=sqrt(x[i]);
//      System.out.printf(setprecision(6) << x[i] << " " <<weight[i] << " %f\n",  f[i] << " %f\n", fexp[i]);
    }
    
    sbeps = 5.0e-6;
    for (i=0;i<N;i++)
      frac[i]=(x[i]-xexp[i])/xexp[i];
    System.out.println(maxel(frac));
    localflag = maxel(frac) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fred2: Unexpected or imprecise abscissas");
      
    }

    sbeps = 5.0e-6;
    for (i=0;i<N;i++)
      frac[i]=(weight[i]-wexp[i])/wexp[i];
    System.out.println(maxel(frac));
    localflag = maxel(frac) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fred2: Unexpected or imprecise weights");
      
    }

    sbeps = 2.0e-6;
    System.out.println(maxel(vecsub(f,fexp)));
    localflag = maxel(vecsub(f,fexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fred2: Imprecise result from integral equation at Gaussian quadrature abscissas ");
      
    }

    // Test method fredin()
    for (i=0;i<M;i++) {
      z=(b-a)*i/(M-1);
      ff[i]=fred.fredin(z);
      ffexp[i]=sqrt(z);
    }
    sbeps = 2.0e-6;
    System.out.println(maxel(vecsub(ff,ffexp)));
    localflag = maxel(vecsub(ff,ffexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fred2 (fredin): Imprecise interpolation of Fredholm result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Gfunc {
    public double funk (double t) {
      return sqrt(t)-pow(PI/2.0,2.25)*pow(t,0.75)/2.25;
    }
  };

  class Kernel {
    public double funk (final double t, final double s) {
          return pow(t*s,0.75);
    }
  };
}
