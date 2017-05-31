package com.nr.test.test_chapter5;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.log;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.fe.Ratfn;

public class Test_pade {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=10,M=12;
    double val,fac,d1,d2,x,sbeps=1.e-6;
    double[] c= new double[M],y=new double[N],yy=new double[N],yyy=new double[N];
    boolean localflag, globalflag=false;

    

    // Test pade
    System.out.println("Testing pade");
    fac=1.0;
    for (i=0;i<M;i++) {
      c[i]=fac/(double)(i+1);
      fac = -fac;
    }
    double[] cc = buildVector(c);
    Ratfn r=Ratfn.pade(cc);
    for (i=0;i<N;i++) {
      x=0.2*(double)(i);
      y[i]=Func_pade(x);
      yy[i]=r.get(x);
      val=c[M-1];
      for (j=M-2;j>=0;j--) {
        val=val*x+c[j];
      }
      yyy[i]=val;
    }
    System.out.printf("pade: Maximum error of Pade approximant = %f\n", (d1=maxel(vecsub(y,yy))));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pade: Pade approximant is inaccurate");
      
    }
    System.out.printf("pade: Maximum discrepancy of power series = %f\n", (d2=maxel(vecsub(y,yyy))));
    localflag = d1 > d2;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pade: Pade approximant is less accurate than power series");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  double Func_pade(double x) {
    return(x==0.0 ? 1.0 : log(1.0+x)/x);
  }

}
