package com.nr.test.test_chapter13;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.sp.DftInt;

public class Test_dftint implements UniVarRealValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20;
    doubleW cosint = new doubleW(0);
    doubleW sinint = new doubleW(0);
    double a,b,w,sbeps;
    double prefa,prefb,cwa,swa,cwb,swb,ci,si;
    boolean localflag, globalflag=false;

    

    // Test dftint
    System.out.println("Testing dftint");

    a=0.0;
    b=2.0;
    sbeps=5.e-6;
    DftInt dftint = new DftInt();
    for (i=0;i<N;i++) {
      w=0.1*i;
      dftint.dftint(this,a,b,w,cosint,sinint);
//      System.out.printf(cosint << " " << sinint);
      prefa=exp(-a)/(1.0+SQR(w));
      prefb=exp(-b)/(1.0+SQR(w));
      cwa=cos(w*a);
      swa=sin(w*a);
      cwb=cos(w*b);
      swb=sin(w*b);
      ci=prefa*(cwa-w*swa)-prefb*(cwb-w*swb);
      si=prefa*(swa+w*cwa)-prefb*(swb+w*cwb);
//      System.out.printf(ci << " " << si);

      localflag = abs(cosint.val-ci) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** dftint: cosint does not agree with analytical result");
        
      }

      localflag = abs(sinint.val-si) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** dftint: sinint does not agree with analytical result");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  public double funk(double t) {
    return exp(-t);
  }

}
