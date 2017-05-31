package com.nr.test.test_chapter13;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sp.Spectreg;
import com.nr.sp.WindowFun;

public class Test_Spectreg {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double maxs,sbeps,pi=acos(-1.0);
    int i,j,M=128,N=2*M,K=64;
    int[] index=new int[2];
    double[] data=new double[N],spec=new double[M+1],freq=new double[M+1];
    boolean localflag, globalflag=false;

    

    // Test Spectreg
    System.out.println("Testing Spectreg");
    Win window = new Win();
    Spectreg sp=new Spectreg(M);
    for (i=0;i<K;i++) {
      // Generate a data set
      for (j=0;j<N;j++) {
        data[j]=10.0*exp(-1.*SQR(j-M)/SQR(N/8))*
          (cos(2.0*pi*32*j/N)+cos(2.0*pi*64*j/N));
//        System.out.printf(data[j]);
      }
      sp.adddataseg(data,window);    // Note: text should clarify this usage
    }
    spec=sp.spectrum();
    freq=sp.frequencies();
//    for (i=0;i<M+1;i++) 
//      System.out.printf(freq[i] << " %f\n", spec[i]);

    maxs=-1.0;
    j=0;
    for (i=0;i<M+1;i++) {
      if (spec[i] > maxs) {
        maxs=spec[i];
        index[j]=i;
      } else {
        j=1;
        maxs=spec[i];
      }
    }

//    System.out.printf(index[0] << " %f\n", index[1]);

    // Check center frequencies
    sbeps=1.e-12;
    for (i=0;i<2;i++) {
//      System.out.printf(abs(freq[index[i]]-0.125*(i+1)));
      localflag = abs(freq[index[i]]-0.125*(i+1)) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** Spectreg: Frequency component "+ i +" was incorrectly identified");
        
      }
    }

    // Check symmetry
    sbeps=1.e-8;
    for (i=0;i<2;i++) {
      for (j=0;j<5;j++) {
//        System.out.printf(setprecision(15) << abs(spec[index[i]+j]-spec[index[i]-j]));
        localflag = abs(spec[index[i]+j]-spec[index[i]-j]) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** Spectreg: Spectral peak " + i + " is not symmetric");
          
        }
      }
    }

    // Compare two peaks
    sbeps=1.e-8;
    for (i=0;i<5;i++) {
//      System.out.printf(setprecision(15) << abs(spec[index[0]+i]-spec[index[1]+i]));
      localflag = abs(spec[index[0]+i]-spec[index[1]+i]) > sbeps;
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** Spectreg: Two spectral peaks are not same size and shape");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public class Win implements WindowFun{
    
    public double window(final int j, final int n) {
      return 1.;
    }

  }

}
