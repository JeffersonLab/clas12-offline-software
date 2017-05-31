package com.nr.test.test_chapter12;

import static com.nr.NRUtil.buildVector;
import static com.nr.fft.FFT.fourn;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.acos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;
import com.nr.ran.Ran;

public class Test_fourn {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1,n0,n1,n2;
    int m[]={8,32,16};
    int[] nn = buildVector(m);
    double sbeps=1.e-13,pi=acos(-1.0);
    boolean localflag=false, globalflag=false;

    

    // Test fourn
    System.out.println("Testing fourn");
    Ran myran=new Ran(17);
    for (i=0;i<nn.length;i++) N *= nn[i];
    N *= 2;
    double[] data1=new double[N],data2=new double[N];
    // Round-trip test for random numbers
    for (i=0;i<N;i++) data1[i] = myran.doub();
    for (i=0;i<N;i++) data2[i] = (2.0/N)*data1[i];
    fourn(data2,nn,1);
    fourn(data2,nn,-1);
//    System.out.printf(maxel(vecsub(data1,data2)));
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** fourn: Round-trip test for random complex values failed");
      
    }

    // Test delta-function in to sine-wave out, forward transform
    for (i=0;i<N;i++) data1[i]=0.0;
    data1[2*(5*nn[1]*nn[2]+7*nn[2]+9)]=1.0;
    fourn(data1,nn,1);
    for (i=0;i<N/2;i++) {
      n2=i%nn[2];
      n1=((i-n2)/nn[2])%nn[1];
      n0=(i-n2-nn[2]*n1)/nn[1]/nn[2];
      Complex r1 = Complex.I.mul(2.0*pi*5.0*n0/nn[0]);
      Complex r2 = Complex.I.mul(2.0*pi*7.0*n1/nn[1]);
      Complex r3 = Complex.I.mul(2.0*pi*9.0*n2/nn[2]);
      Complex r = r1.exp().mul(r2.exp()).mul(r3.exp());          
      data2[2*i]=r.re();
      data2[2*i+1]=r.im();
    }
//    System.out.printf(maxel(vecsub(data1,data2)));
    localflag = localflag || maxel(vecsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** fourn: Forward transform of a chosen delta function did not give expected result");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
