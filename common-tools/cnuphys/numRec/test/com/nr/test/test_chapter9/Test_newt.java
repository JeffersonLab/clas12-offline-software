package com.nr.test.test_chapter9;

import static com.nr.NRUtil.SQR;
import static com.nr.root.Roots.newt;
import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.booleanW;

import com.nr.RealMultiValueFun;
public class Test_newt {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,NDIM=4,M=8; // NTRIAL=6 not used
    double sbeps=1.e-8;
    double x0[]={0.5,0.4,0.6,0.5,-0.5,-0.4,-0.5,-0.4};
    double x1[]={0.6,0.6,0.4,0.4,-0.4,-0.5,-0.6,-0.6};
    double x2[]={0.4,0.5,0.5,0.6,-0.6,-0.6,-0.4,-0.5};
    double x3[]={0.0,1.4,0.0,1.6,0.0,1.3,0.0,1.7};
    double[] fvec=new double[NDIM],x=new double[NDIM],dy = new double[M];
    boolean localflag, globalflag=false;

    

    // Test newt
    System.out.println("Testing newt");
    Func_newt f = new Func_newt();
    for (i=0;i<M;i++) {
      for (j=0;j<NDIM;j++) {
        x[0]=x0[i];
        x[1]=x1[i];
        x[2]=x2[i];
        x[3]=x3[i];
      }
      booleanW w = new booleanW(false);
      newt(x,w,f);localflag = w.val;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** newt: Test case "+ i + " converged to local minimum");
        
      }
      fvec=f.funk(x);
      dy[i]=maxel(fvec);
    }
    System.out.printf("newt: Maximum discrepancy = %f\n", maxel(dy));
    localflag = maxel(dy) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** newt: Inaccurate roots");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Func_newt implements RealMultiValueFun {
    public double[] funk(final double[] x) {
      double[] fvec =new double[4];

      fvec[0] = -SQR(x[0])-SQR(x[1])-SQR(x[2])+x[3];
      fvec[1] = SQR(x[0])+SQR(x[1])+SQR(x[2])+SQR(x[3])-1.0;
      fvec[2] = x[0]-x[1];
      fvec[3] = x[1]-x[2];
      return fvec;
    }
  }
}
