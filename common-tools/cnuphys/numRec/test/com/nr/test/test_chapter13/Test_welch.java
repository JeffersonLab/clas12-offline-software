package com.nr.test.test_chapter13;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sp.Spectreg;
import com.nr.sp.WelchWin;

public class Test_welch {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double maxf=0,maxs,pi=acos(-1.0),sbeps;;
    int i,j,maxi=0,M=128,N=2*M,K=64;
    double[] data=new double[N],spec=new double[M+1],freq=new double[M+1];
    boolean localflag, globalflag=false;

    

    // Test welch
    System.out.println("Testing welch");
    WelchWin welch = new WelchWin();
    Spectreg sp = new Spectreg(M);

    for (i=0;i<K;i++) {
      // Generate a data set
      for (j=0;j<N;j++)
        data[j]=cos(2.0*pi*64*j/N);
      sp.adddataseg(data,welch);
    }
    spec=sp.spectrum();
    freq=sp.frequencies();
//    for (i=0;i<M+1;i++) 
//      System.out.printf(freq[i] << " %f\n", spec[i]);

    maxs=-1.0;
    for (i=0;i<M+1;i++) {
      if (spec[i] > maxs) {
        maxf=freq[i];
        maxs=spec[i];
        maxi=i;
      }
    }

    sbeps=1.e-15;
    localflag=abs(maxf-0.25) > sbeps;
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** welch: Spectrum maximum is at wrong frequency");
      
    }

    // Test symmetry
    sbeps=1.e-12;
    localflag = false;
    for (i=1;i<10;i++) {
//      System.out.printf(abs(spec[maxi+i]/spec[maxi-i]-1.0));
      localflag = localflag || abs(spec[maxi+i]/spec[maxi-i]-1.0) > sbeps;
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** welch: Channels adjacent to maximum should be symmetrical");
      
    }

    // Test amplitudes
    sbeps=1.e-2;
    localflag = false;
    for (i=2;i<6;i++) {
//      System.out.printf(abs(spec[maxi+i]/spec[maxi+1]*SQR(SQR(i))-1.0));
      localflag = localflag || abs(spec[maxi+i]/spec[maxi+1]*SQR(SQR(i))-1.0) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** welch: Sidelobe amplitudes are not in the correct proportion");
        
      }
    }

    Spectreg sp2 = new Spectreg(M);

    for (i=0;i<K;i++) {
      // Generate a data set offset by 1/2 channel in frequency
      for (j=0;j<N;j++)
        data[j]=cos(2.0*pi*64.5*j/N);
      sp2.adddataseg(data,welch);
    }
    spec=sp2.spectrum();
    freq=sp2.frequencies();
//    for (i=0;i<10;i++) 
//      System.out.printf(spec[maxi-i]/maxs << " %f\n", spec[maxi+i+1]/maxs);

    sbeps=5.e-3;
    localflag=false;
    for (i=2;i<10;i++) {
      localflag = localflag || abs(spec[maxi+i]/spec[maxi+1]) > sbeps;
//      System.out.printf(abs(spec[maxi+i]/spec[maxi+1]));
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** welch: Leakage should be only into channels directly adjacent to peak");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
