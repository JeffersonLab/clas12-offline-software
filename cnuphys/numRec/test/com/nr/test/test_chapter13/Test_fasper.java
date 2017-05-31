package com.nr.test.test_chapter13;

import static com.nr.sp.Fourier.*;
import static java.lang.Math.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import com.nr.ran.Normaldev;

public class Test_fasper {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,jmax1,jmax2,nout1,nout2,NP=90,NPR=11,TWONP=2*NP;
    double prob1,prob2,sbeps;; //,twopi=2.0*acos(-1.0);
    double[] x=new double[NP],y=new double[NP];
    double[] px1=new double[TWONP],px2=new double[TWONP],py1=new double[TWONP],py2=new double[TWONP];
    boolean localflag, globalflag=false;

    

    // Test fasper
    System.out.println("Testing fasper");

    j=0;
    Normaldev mynorm = new Normaldev(0.0,1.0,17);
    for (i=0;i<NP+10;i++) {
      if (i != 2 && i != 3 && i != 5 && i != 20 &&
        i != 37 && i != 50 && i != 66 && i != 67 &&
        i != 82 && i != 92) {
        x[j]=i+1;
        y[j]=0.75*cos(0.6*x[j])+mynorm.dev();
        j++;
      }
    }
    Fasper fasper = new Fasper(px1,py1);
    fasper.fasper(x,y,4.0,1.0);
    px1 = fasper.px; py1=fasper.py; 
    nout1 = fasper.nout;jmax1=fasper.jmax;prob1 = fasper.prob;
    
    Period period =new Period(px2,py2);
    period.period(x,y,4.0,1.0);
    px2 = period.px; py2=period.py;
    nout2 = period.nout;jmax2=period.jmax;prob2 = period.prob;

//    System.out.println("fasper results for test signal (cos(0.6x) + noise):");
//    System.out.println("nout, jmax, prob = ");
//    System.out.printf(setw(5) << nout1 << setw(5) << jmax1 << setw(15) << prob1;
//    System.out.printf(setw(5) << nout2 << setw(5) << jmax2 << setw(15) << prob2;
//    System.out.printf(endl;

    localflag = (nout1 != nout2);
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** fasper: fasper() generated a different output array size than period()");
      
    }

    localflag = (jmax1 != jmax2);
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** fasper: The spectrum maximum had a different channelf for fasper() and period()");
      
    }

    sbeps=1.e-5;
//    System.out.printf(abs(prob1/prob2-1.0));
    localflag = abs(prob1/prob2-1.0) > sbeps;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** fasper: fasper() and period() calculated different probabilities");
      
    }

    sbeps=1.e-15;
    localflag=false;
    for (i=max(0,(int)(jmax1-NPR/2));i<min(nout1,(int)(jmax1+NPR/2+1));i++) {
//      System.out.printf(abs(px1[i]/px2[i]-1.0));

      localflag = localflag || abs(px1[i]/px2[i]-1.0) > sbeps;
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** fasper: fasper() and period() generated different frequency lists");
      
    }

    sbeps=1.e-6;
    localflag=false;
    for (i=max(0,(int)(jmax1-NPR/2));i<min(nout1,(int)(jmax1+NPR/2+1));i++) {
//      System.out.printf(abs(py1[i]/py2[i]-1.0));

      localflag = localflag || abs(py1[i]/py2[i]-1.0) > sbeps;
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** fasper: fasper() and period() generated different spectral densities");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
