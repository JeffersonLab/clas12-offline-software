package com.nr.test.test_chapter13;

import static com.nr.sp.Fourier.fixrts;
import static com.nr.sp.Fourier.memcof;
import static com.nr.sp.Fourier.predic;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.exp;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_predic {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,NPTS=500,NPOLES=10,NFUT=20;
    double sbeps;
    doubleW dum = new doubleW(0);
    double[] d=new double[NPOLES],future=new double[NFUT],data=new double[NPTS];
    boolean localflag, globalflag=false;

    

    // Test predic
    System.out.println("Testing predic");
    
    for (i=0;i<NPTS;i++) 
      data[i]=f(i+1,NPTS);
    memcof(data,dum,d);
    fixrts(d);
    predic(data,d,future);

    sbeps=1.e-5;
    for (i=0;i<NFUT;i++) {
//      System.out.printf(setw(6) << i << setw(13) << f(NPTS+i+1,NPTS);
//      System.out.printf(setw(13) << future[i] << " " << abs(future[i]-f(NPTS+i+1,NPTS)));

      localflag = abs(future[i]-f(NPTS+i+1,NPTS)) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** predic: Inaccurate extrapolation");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  double f(int n, int npts)
  {
    double pi=acos(-1.0);

    return exp(-1.0*n/npts)*sin(2.0*pi*n/50.0)
      +exp(-2.0*n/npts)*sin(2.2*pi*n/50.0);
  }

}
