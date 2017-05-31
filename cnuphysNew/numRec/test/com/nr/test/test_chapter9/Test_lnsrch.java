package com.nr.test.test_chapter9;

import static com.nr.NRUtil.SQR;
import static com.nr.root.Roots.lnsrch;
import static com.nr.test.NRTestUtil.maxel;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.booleanW;
import org.netlib.util.doubleW;

import com.nr.RealMultiValueFun;
import com.nr.RealValueFun;

public class Test_lnsrch {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,NDIM=4,M=10; // NTRIAL=6, not used.
    doubleW fnew = new doubleW(0);
    double fold,stpmax=1.0,sbeps=1.e-14;
    double x0[]={0.5,-0.5,1.0,1.0,1.0,0.5,-1.0,-1.0,-1.0,-0.5};
    double x1[]={0.5,-0.5,1.0,1.0,0.5,1.0,-1.0,-1.0,-0.5,-1.0};
    double x2[]={0.5,-0.5,1.0,0.5,1.0,1.0,-1.0,-0.5,-1.0,-1.0};
    double x3[]={0.5,-0.5,0.9,1.0,1.0,1.0,-0.9,-1.0,-1.0,-1.0};
    double p0[]={1.0,-1.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,-1.0};
    double p1[]={1.0,-1.0,0.0,0.0,1.0,0.0,0.0,0.0,-1.0,0.0};
    double p2[]={1.0,-1.0,0.0,1.0,0.0,0.0,0.0,-1.0,0.0,0.0};
    double p3[]={1.0,-1.0,1.0,0.0,0.0,0.0,-1.0,0.0,0.0,0.0};
    double[] xold=new double[NDIM],gold=new double[NDIM],p=new double[NDIM],xnew=new double[NDIM];
    double[] dy = new double[(M)];
    boolean localflag, globalflag=false;

    

    // Test lnsrch
    System.out.println("Testing lnsrch");

    Func_lnsrch f = new Func_lnsrch();    // function
    Funcd_lnsrch g = new Funcd_lnsrch();   // gradiant
    for (i=0;i<M;i++) {
      for (j=0;j<NDIM;j++) {
        xold[0]=x0[i];
        xold[1]=x1[i];
        xold[2]=x2[i];
        xold[3]=x3[i];
        p[0]=p0[i];
        p[1]=p1[i];
        p[2]=p2[i];
        p[3]=p3[i];
      }
      fold=f.funk(xold);
      gold=g.funk(xold);
      booleanW w = new booleanW(false);
      lnsrch(xold,fold,gold,p,xnew,fnew,stpmax,w,f);localflag =w.val;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** lnsrch: Final x is too close to xold");
        
      }
      dy[i]=abs(fnew.val);  // Function should be 0.0 at minima
    }
    System.out.printf("lnsrch: Maximum discrepancy = %f\n", maxel(dy));
    localflag = maxel(dy) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** lnsrch: Inaccurate minima");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Func_lnsrch implements RealValueFun{
    public double funk(final double[] x) {
      return(SQR(x[0]-x[1])+SQR(x[1]-x[2])+SQR(x[2]-x[3])+SQR(x[3]-1)*SQR(x[3]+1));
    } // Minima of 0.0 at x0=x1=x2=x3= +- 1;
  };

  class Funcd_lnsrch implements RealMultiValueFun {
    public double[] funk(final double[] x) {
      double[] g = new double[4];
      g[0]= 2.0*(x[0]-x[1]);
      g[1]= -2.0*(x[0]-x[1])+2.0*(x[1]-x[2]);
      g[2]= -2.0*(x[1]-x[2])+2.0*(x[2]-x[3]);
      g[3]= -2.0*(x[2]-x[3])+2.0*(x[3]-1)*SQR(x[3]+1)+2.0*(x[3]+1)*SQR(x[3]-1);
      return(g);
    }
  };
}
