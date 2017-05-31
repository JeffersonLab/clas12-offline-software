package com.nr.test.test_chapter5;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.log;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.fe.Ratfn;

public class Test_ratlsq implements UniVarRealValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=11,M=12;
    double fac,x,sbeps=2.e-13;
    doubleW dev = new doubleW(0);
    double[] c = new double[2*M],y=new double[N],yy=new double[N];
    boolean localflag, globalflag=false;

    

    // Test ratlsq
    System.out.println("Testing ratlsq");
    fac=1.0;
    for (i=0;i<2*M;i++) {     // Series coefficients
      c[i]=fac/(double)(i+1);
      fac = -fac;
    }
    Ratfn q=Ratfn.ratlsq(this,0.0,2.0,M,M,dev); // Diagonal works best for this function
    for (i=0;i<N;i++) {
      x=0.2*(double)(i);
      y[i]=funk(x);  // Function values
      yy[i]=q.get(x);     // Rational function
    }
    System.out.printf("ratlsq: Maximum error of ratlsq rational function approximation = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ratlsq: Ratlsq approximation is inaccurate");
      
    }

    System.out.printf("ratlsq: Reported maximum absolute deviation from ratlsq = %f\n", dev.val);
    localflag = maxel(vecsub(y,yy)) > dev.val;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ratlsq: Error exceeds reported maximum absolute deviation");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public double funk(double x) {
    return(x==0.0 ? 1.0 : log(1.0+x)/x);
  }

}
