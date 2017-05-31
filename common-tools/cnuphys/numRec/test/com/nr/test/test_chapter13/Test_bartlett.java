package com.nr.test.test_chapter13;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sp.BartlettWin;
import com.nr.sp.Spectreg;

public class Test_bartlett {

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

    

    // Test bartlett
    System.out.println("Testing bartlett");
    BartlettWin bartlett = new BartlettWin();
    

    Spectreg sp =new Spectreg(M);

    for (i=0;i<K;i++) {
      // Generate a data set
      for (j=0;j<N;j++)
        data[j]=cos(2.0*pi*64*j/N);
      sp.adddataseg(data,bartlett);
    }
    spec=sp.spectrum();
    freq=sp.frequencies();
//    for (i=0;i<M+1;i++) 
//      System.out.printf(freq[i] << " " << spec[i]);

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
      fail("*** bartlett: Spectrum maximum is at wrong frequency");
      
    }

    localflag = false;
    for (i=1;i<5;i++) {
      localflag = localflag || (spec[maxi+2*i]/maxs > sbeps);
      localflag = localflag || (spec[maxi-2*i]/maxs > sbeps);
    }
    globalflag = globalflag || localflag; 
    if (localflag) {
      fail("*** bartlett: Even numbered channels from the maximum should be zero for Bartlett window");
      
    }

    // test for symmetry
    sbeps=1.e-12;
    localflag=false;
    for (i=0;i<5;i++) {
//      System.out.printf(abs(spec[maxi+2*i+1]/spec[maxi-2*i-1]-1.0));
      localflag = localflag || abs(spec[maxi+2*i+1]/spec[maxi-2*i-1]-1.0) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** bartlett: Sidelobes at odd numbered channels from maximum are not symmetrical");
        
      }
    }

    // Test for amplitude
    sbeps=1.e-2;
    localflag=false;
    for (i=1;i<5;i++) {
//      System.out.printf(spec[maxi+2*i+1]/maxs << " " << spec[maxi-2*i-1]/maxs);
//      System.out.printf(abs(spec[maxi+2*i+1]/spec[maxi+1]*SQR(SQR(2*i+1))-1.0));
      localflag = localflag || abs(spec[maxi+2*i+1]/spec[maxi+1]*SQR(SQR(2*i+1))-1.0) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** bartlett: Odd numbered channels from the maximum are not in correct proportion");
        
      }
    }
    
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
