package com.nr.test.test_chapter7;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Sobol;

public class Test_sobseq {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,count,N=10000;
    double sum,rsq,arg,volume,vol,R1=0.6,R2=0.3,a=R2*sqrt(2.0),PI=acos(-1.0);
    double[] x=new double[3],xlo=new double[3],xhi=new double[3],volerr=new double[5],sumerr=new double[5];
    boolean localflag, globalflag=false;
    
    

    // Test sobseq
    System.out.println("Testing sobseq");

    xlo[0]=0.0;
    xhi[0]=R1+R2;
    xlo[1]=-(R1+R2);
    xhi[1]=R1+R2;
    xlo[2]=-R2;
    xhi[2]=R2;
    vol=1.0;
    for (j=0;j<3;j++) 
      vol *= (xhi[j]-xlo[j]);


    for (k=3;k<7;k++) {
      N=(int)(pow(10.,k));
      Sobol.sobseq(-1,x);
      count=0;
      sum=0.0;
      for (i=0;i<N;i++) {
        Sobol.sobseq(3,x);
        for (j=0;j<3;j++)
          x[j]=xlo[j]+x[j]*(xhi[j]-xlo[j]);
        rsq=SQR(sqrt(SQR(x[0])+SQR(x[1]))-R1)+SQR(x[2]);    
        if (rsq < SQR(R2)) {
          count ++;
          sum += 1.0+cos(PI*rsq/SQR(a));
        }
      }
      sum = vol*sum/N;
      volume = vol*(count)/N;
      arg=PI*SQR(R2)/SQR(a);
      sumerr[k-3]=abs(sum-SQR(PI)*R1*SQR(R2)*(1.0+sin(arg)/arg));
      volerr[k-3]=abs(volume-SQR(PI)*R1*SQR(R2));
//      System.out.printf(N << " %f\n", volerr[k-3] << " %f\n", sumerr[k-3]);
    }

//    System.out.printf(volerr[3]/volerr[1]);
//    System.out.printf(sumerr[3]/sumerr[1]);

    localflag = volerr[3]/volerr[1] > 0.02;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sobseq: Error in volume calculation is not approaching 1/N");
      
    }

    localflag = sumerr[3]/sumerr[1] > 0.02;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sobseq: Error in integral calculation is not approaching 1/N");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
