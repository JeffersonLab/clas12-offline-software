package com.nr.test.test_chapter13;


import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Normaldev;
import com.nr.sp.Fourier.Period;

public class Test_period {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,jmax,nout,max1,max2,NP=90,TWONP=2*NP;
    double prob,sbeps,twopi=2.0*acos(-1.0);
    double[] x=new double[NP],y=new double[NP],px=new double[TWONP],py=new double[TWONP];
    boolean localflag, globalflag=false;

    

    // Test period
    System.out.println("Testing period");

    j=0;
    Normaldev mynorm = new Normaldev(0.0,1.0,17);
    for (i=0;i<NP+10;i++) {
      if (i != 2 && i != 3 && i != 5 && i != 20 &&
        i != 37 && i != 50 && i != 66 && i != 67 &&
        i != 82 && i != 92) {
        x[j]=i+1.0;
        y[j]=0.75*cos(0.6*x[j])+mynorm.dev();
        j++;
      }
    }
    
    Period period = new Period(px,py);
    period.period(x,y,4.0,1.0);
    px = period.px;py=period.py; 
    nout = period.nout;jmax=period.jmax;prob = period.prob;
    

//    System.out.println("period results for test signal (cos(0.6x) + noise):");
//    System.out.println("nout,jmax,prob = " << setw(5) << nout;
//    System.out.printf(setw(5) << jmax << setw(15) << prob << endl);
//    for (i=MAX(0,int(jmax-NPR/2));i<MIN(nout,int(jmax+NPR/2+1));i++)
//      System.out.printf(i << setw(15) << twopi*px[i] << setw(15) << py[i]);

    // Find two channels with the highest density
    max1=0;
    max2=0;
    for (i=0;i<nout;i++) {
      if (py[i] > py[max1])
        max1=i;
      else if (py[i] > py[max2])
        max2=i;
    }
//    System.out.printf(jmax << " " << max1 << " " << max2);

    localflag = (max1 != jmax);
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** period: Highest spectral density is not in channel jmax");
      
    }

//    System.out.printf(twopi*px[max1] << " " << twopi*px[max2]);
    localflag = (twopi*px[max1]-0.6)*(twopi*px[max2]-0.6) > 0.0;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** period: Actual signal frequency is not between two highest channels");
      
    }

    localflag = (prob > 0.01);
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** period: High value of prob suggest signal is not sinusoidal");
      
    }

    // Fingerprint
    sbeps=1.e-5;
    double expectpx[]={0.523599,0.539465,0.555332,0.571199,0.587065,
      0.602932,0.618799,0.634665,0.650532};
    for (i=jmax-4;i<jmax+5;i++) {
      j=i-(jmax-4);
      localflag = abs(twopi*px[i]-expectpx[j]) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** period: Frequency values do not match fingerprint");
        
      }
    }

    sbeps=1.e-4;
    double expectpy[]={1.58704,0.045545,1.24702,5.57825,9.55312,
      8.84550,4.16774,0.80101,1.38988};
    for (i=jmax-4;i<jmax+5;i++) {
      j=i-(jmax-4);
      localflag = abs(py[i]-expectpy[j]) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** period: Spectral densities do not match fingerprint");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
