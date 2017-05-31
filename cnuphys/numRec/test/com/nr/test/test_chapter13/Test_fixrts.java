package com.nr.test.test_chapter13;

import static com.nr.NRUtil.buildVector;
import static com.nr.root.Roots.zroots;
import static com.nr.sp.Fourier.fixrts;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;

public class Test_fixrts {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean polish=true;
    int i,ncount,NPOLES=6;
    double sbeps,dd[]={3.0,-15.0/4,5.0/2,-15.0/16,3.0/16,63.0/64};
    Complex z1=new Complex(),z2=new Complex();
    double[] d =buildVector(dd);
    Complex[] zcoef=new Complex[NPOLES+1],zeros=new Complex[NPOLES];
    boolean localflag, globalflag=false;

    

    // Test fixrts
    System.out.println("Testing fixrts");

    // finding roots of (z-1/2)^6=1.0
    // first write roots
    zcoef[NPOLES]=new Complex(1.0,0);
    for (i=0;i<NPOLES;i++)
      zcoef[i] = new Complex(-d[NPOLES-1-i],0.0);
    zroots(zcoef,zeros,polish);

    sbeps=1.e-13;
    ncount=0;   // Count roots outside unit circle
    for (i=0;i<NPOLES;i++) {
      z1=zeros[i].sub(new Complex(0.5,0));  // compute (z-1/2)^6=1.0
      z2=z1.mul(z1).mul(z1);
      z1=z2.mul(z2);
      //System.out.printf(setprecision(18) << abs(z1-Complex(1.0,0.0)));
      localflag = z1.sub(new Complex(1.0,0.0)).abs() > sbeps;
      //System.out.printf(abs(zeros[i]));
      if (zeros[i].abs() > 1.0) ncount++;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** fixrts: zroots incorrectly identified a root");
        
      }
    }
    System.out.printf("   Original roots outside unit circle: %d\n", ncount);

    // now fix roots to lie within unit circle
    fixrts(d);

    // check results
    zcoef[NPOLES]=new Complex(1.0,0);
    for (i=0;i<NPOLES;i++)
      zcoef[i] = new Complex(-d[NPOLES-1-i],0.0);
    zroots(zcoef,zeros,polish);

    ncount=0;
    for (i=0;i<NPOLES;i++)
      if (zeros[i].abs() > 1.0) ncount++;
    System.out.printf("   Fixed roots outside unit circle: %d\n", ncount);

    localflag = (ncount > 0);
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** fixrts: A fixed root remains outside unit circle");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
