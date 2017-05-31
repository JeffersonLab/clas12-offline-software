package com.nr.test.test_chapter13;

import static com.nr.sp.Fourier.evlmem;
import static com.nr.sp.Fourier.memcof;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_evlmem {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1000,M=10,NFDT=10000;
    double f,flo=0,fhi,fdt,freq1,freq2,PI=acos(-1.0);
    double s,sum1,sum2,target,sbeps;
    doubleW pm = new doubleW(0);
    double[] cof=new double[M],data=new double[N];
    boolean localflag, globalflag=false;

    

    // Test evlmem
    System.out.println("Testing evlmem");

    for (i=0;i<N;i++) data[i]=func_evlmem(N,i);

    memcof(data,pm,cof);

    freq1=1.0/(2.0*PI)/20.0;
    freq2=1.0/(2.0*PI)/40.0;
    
//    System.out.println("             f*delta             power");
//    System.out.printf(fixed << setprecision(6);
//    for (fdt=0.0;fdt<=0.01;fdt+=0.5/NFDT) {
//      System.out.printf(setw(20) << fdt;
//      System.out.printf(scientific << setw(20) << evlmem(fdt,cof,pm));
//    }

    i=0;
    for (fdt=0.0;fdt<=0.5;fdt += 0.5/NFDT) {
      if (evlmem(fdt,cof,pm.val) > 1.e-4 && i==0) {
        i=1;
        flo=fdt;
      }
      if (evlmem(fdt,cof,pm.val) < 1.e-4 && i==1) {
        i=0;
        fhi=fdt;

        sum1=0.0;
        sum2=0.0;
        for (f=flo;f<fhi;f += 0.5/NFDT) {
          s=evlmem(f,cof,pm.val);
          sum1 += f*s;
          sum2 += s;
        }

        target = (abs(sum1/sum2-freq1) < abs(sum1/sum2-freq2) ? freq1 : freq2);

//        System.out.printf(flo << " " << fhi);
//        System.out.printf(sum1/sum2 << " " << target);

        sbeps=0.015;
        localflag = abs((sum1/sum2)/target-1.0) > sbeps;
        globalflag = globalflag || localflag; 
        if (localflag) {
          fail("*** evlmem: One of the frequencies was incorrectly determined");
          
        }
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  double func_evlmem(int N,int i) {
    return sin((i-12.0)/20.0)*exp(-0.2*i/N)
        + cos((i+11.0)/40.0)*exp(0.2*i/N);
  }

}
