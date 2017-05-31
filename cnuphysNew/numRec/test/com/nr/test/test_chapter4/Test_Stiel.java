package com.nr.test.test_chapter4;

import static com.nr.fi.GaussianWeights.gaulag;
import static com.nr.fi.GaussianWeights.gauleg;
import static com.nr.sort.Sorter.sort2;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fi.Stiel;

public class Test_Stiel {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int N=10;
    double sbeps;
    double[] x1= new double[N],w1= new double[N],x2= new double[N],w2= new double[N];
    boolean localflag, globalflag=false;
    
    

    // Test Stiel
    System.out.println("Testing Stiel");

    MyStiel ss1 = new MyStiel(N,-1.0,1.0,3.7);
    ss1.get_weights(x1,w1); // Largest abscissas are first
    sort2(x1,w1);   

    gauleg(-1.0,1.0,x2,w2);

    sbeps=1.e-15;
//    System.out.printf(maxel(vecsub(x1,x2)) << endl;
    localflag = maxel(vecsub(x1,x2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stiel: Incorrect abscissas for Gauss-Legendre");
      
    }

    sbeps=1.e-14;
//    System.out.printf(maxel(vecsub(w1,w2)) << endl;
    localflag = maxel(vecsub(w1,w2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stiel: Incorrect weights for Gauss-Legendre");
      
    }

    MyStiel ss2 = new MyStiel(N,-5.0,5.0);
    ss2.get_weights(x1,w1);
    sort2(x1,w1);

    gaulag(x2,w2,1.0);

    sbeps=5.e-14;
//    System.out.printf(maxel(vecsub(x1,x2)) << endl;
    localflag = maxel(vecsub(x1,x2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stiel: Incorrect abscissas for Gauss-Laguerre");
      
    }

    sbeps=1.e-14;
//    System.out.printf(maxel(vecsub(w1,w2)) << endl;
    localflag = maxel(vecsub(w1,w2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stiel: Incorrect weights for Gauss-Laguerre");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class MyStiel extends Stiel {

    public MyStiel(int nn, double aaa, double bbb) {
      super(nn, aaa, bbb);
    }

    public MyStiel(final int nn, final double aaa, final double bbb,
        final double hmaxx) {
      super(nn, aaa, bbb, hmaxx);
    }

    @Override
    public double wt1(final double x, final double del) {
      return 1.0; // Gauss-Legendre
    }

    @Override
    public double wt2(final double x) {
      return x * exp(-x);
    }

    @Override
    public double fx(final double t) {
      return exp(t - exp(-t));
    }

    @Override
    public double fdxdt(final double t) {
      double s = exp(-t);
      return exp(t - s) * (1.0 + s);
    }
  }

}
