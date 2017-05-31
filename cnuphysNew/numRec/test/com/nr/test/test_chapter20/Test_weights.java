package com.nr.test.test_chapter20;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.pde.Weights;
import com.nr.ran.Ran;
import com.nr.sf.Bessjy;

public class Test_weights {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=17,M=3;
    double scale,y,f,f1,f2,sbeps;
    double[] x=new double[N];
    double[][] c=new double[N][M]; // ,d1=new double[N][N],d2=new double[N][N];
    boolean localflag, globalflag=false;

    

    // Test weights
    System.out.println("Testing weights");

    // Choose some collocation points
    scale=3.0/N;
    for (i=0;i<=N/2;i++) {
      x[i]=10.0*atan((N/2-i)*scale);
      x[N-1-i]= -x[i];
    }
//    for (i=0;i<N;i++) System.out.printf(x[i] << "  ";
//    System.out.printf(endl;

    // Calculate some function values and derivatives
    Ran myran=new Ran(14);
    Bessjy bess=new Bessjy();
    for (i=0;i<N;i++) {
      y=x[0]*(2.0*myran.doub()-1.0);
      Weights.weights(y,x,c);
      // Calculate function
      f=f1=f2=0;
      for (j=0;j<N;j++) {
        f += c[j][0]*bess.jn(5,x[j]);
        f1 += c[j][1]*bess.jn(5,x[j]);
        f2 += c[j][2]*bess.jn(5,x[j]);
      }
//      System.out.printf(y);
//      System.out.printf(abs(bess.jn(5,y)-f) << " ";
//      System.out.printf(abs(0.5*(bess.jn(4,y)-bess.jn(6,y))-f1) << " ";
//      System.out.printf(abs(0.25*(bess.jn(3,y)-2.0*bess.jn(5,y)+bess.jn(7,y))-f2));

      sbeps=1.e-3;
      localflag = abs(bess.jn(5,y)-f) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** weights: Evaluation of function was inaccurate");
        
      }

      sbeps=1.e-3;
      localflag = abs(0.5*(bess.jn(4,y)-bess.jn(6,y))-f1) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** weights: Evaluation of first derivative was inaccurate");
        
      }

      sbeps=1.e-3;
      localflag = abs(0.25*(bess.jn(3,y)-2.0*bess.jn(5,y)+bess.jn(7,y))-f2) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** weights: Evaluation of second derivative was inaccurate");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
