package com.nr.test.test_chapter12;

import static com.nr.NRUtil.*;
import static com.nr.fft.FFT.rlft3;
import static com.nr.test.NRTestUtil.*;
import static java.lang.Math.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;
import com.nr.ran.Ran;

public class Test_rlft3 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,N=1;
    int m[]={8,32,16};
    int[] nn=buildVector(m);
    double sbeps=2.e-14,pi=acos(-1.0),temp;
    boolean localflag=false, globalflag=false;

    

    // Test rlft3, interface 1 (2 Dimensions)
    System.out.println("Testing rlft3, interface1");
    Ran myran = new Ran(17);
    for (i=0;i<2;i++) N *= nn[i];
    double[][] data1=new double[nn[0]][nn[1]];
    double[][] data2=new double[nn[0]][nn[1]];
    double[] speq1=new double[2*nn[0]];
    double[] speq2=new double[2*nn[0]];
    // Round-trip test for random numbers
    for (i=0;i<nn[0];i++) {
      for (j=0;j<nn[1];j++) {
        data1[i][j] = myran.doub();
        data2[i][j] = (2.0/N)*data1[i][j];
      }
    }
    rlft3(data2,speq2,1);
    rlft3(data2,speq2,-1);
//    System.out.printf(maxel(matsub(data1,data2)));
    localflag = localflag || maxel(matsub(data1,data2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rlft3, interface1: Round-trip test for random real values failed");
      
    }


    // Test delta-function in to sine-wave out, forward transform
    for (i=0;i<nn[0];i++) 
      for (j=0;j<nn[1];j++) data1[i][j]=0.0;
    data1[5][7]=1.0;
    rlft3(data1,speq1,1);
    for (i=0;i<nn[0];i++) {
      for (j=0;j<nn[1]/2;j++) {
        Complex r1 = Complex.I.mul(2.0*pi*5.0*i/nn[0]);
        Complex r2 = Complex.I.mul(2.0*pi*7.0*j/nn[1]);
        Complex r = r1.exp().mul(r2.exp());          
        data2[i][2*j] = r.re();
        data2[i][2*j+1]= r.im();
        /*
        data2[i][2*j]= real(exp(2.0*pi*Complex(0.0,1.0)*5.0*double(i)/double(nn[0]))
          *exp(2.0*pi*Complex(0.0,1.0)*7.0*double(j)/double(nn[1])));
        data2[i][2*j+1]= imag(exp(2.0*pi*Complex(0.0,1.0)*5.0*double(i)/double(nn[0]))
          *exp(2.0*pi*Complex(0.0,1.0)*7.0*double(j)/double(nn[1])));
          */
      }
      Complex r1 = Complex.I.mul(2.0*pi*5.0*i/nn[0]);
      Complex r2 = Complex.I.mul(pi*7.0);
      Complex r = r1.exp().mul(r2.exp());
      speq2[2*i]=r.re();
      speq2[2*i+1]=r.im();
      /*
      speq2[2*i]=real(exp(2.0*pi*Complex(0.0,1.0)*5.0*double(i)/double(nn[0]))*exp(pi*Complex(0.0,1.0)*7.0));
      speq2[2*i+1]=imag(exp(2.0*pi*Complex(0.0,1.0)*5.0*double(i)/double(nn[0]))*exp(pi*Complex(0.0,1.0)*7.0));
      */
    }
//    System.out.printf(maxel(matsub(data1,data2)));
    localflag = localflag || maxel(matsub(data1,data2)) > sbeps;
//    System.out.printf(maxel(vecsub(speq1,speq2)));
    localflag = localflag || maxel(vecsub(speq1,speq2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rlft3: Forward transform of a chosen delta function did not give expected result");
      
    }


    // Test rlft3, interface 2 (3 Dimensions)
    System.out.println("Testing rlft3, interface2");
    N=1;
    for (i=0;i<3;i++) N *= nn[i];
    double[][][] data3=new double[nn[0]][nn[1]][nn[2]];
    double[][][] data4=new double[nn[0]][nn[1]][nn[2]];
    double[][] speq3=new double[nn[0]][2*nn[1]];
    double[][] speq4=new double[nn[0]][2*nn[1]];
    // Round-trip test for random numbers
    for (i=0;i<nn[0];i++) { 
      for (j=0;j<nn[1];j++) {
        for (k=0;k<nn[2];k++) {
          data3[i][j][k] = myran.doub();
          data4[i][j][k] = (2.0/N)*data3[i][j][k];
        }
      }
    }
    rlft3(data4,speq4,1);
    rlft3(data4,speq4,-1);
    double max=0.0;
    for (i=0;i<nn[0];i++) { 
      for (j=0;j<nn[1];j++) {
        for (k=0;k<nn[2];k++) {
          temp=abs(data3[i][j][k]-data4[i][j][k]);
          max = (temp > max ? temp : max);
        }
      }
    }
//    System.out.printf(max);
    localflag = localflag || max > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rlft3, interface2: Round-trip test for random real values failed");
      
    }


    // Test delta-function in to sine-wave out, forward transform
    for (i=0;i<nn[0];i++) 
      for (j=0;j<nn[1];j++)
        for (k=0;k<nn[2];k++) data3[i][j][k]=0.0;
    data3[5][7][9]=1.0;
    rlft3(data3,speq3,1);

    for (i=0;i<nn[0];i++) {
      for (j=0;j<nn[1];j++) {
        for (k=0;k<nn[2]/2;k++) {
          Complex r1 = Complex.I.mul(2.0*pi*5.0*i/nn[0]);
          Complex r2 = Complex.I.mul(2.0*pi*7.0*j/nn[1]);
          Complex r3 = Complex.I.mul(2.0*pi*9.0*k/nn[2]);
          Complex r = r1.exp().mul(r2.exp()).mul(r3.exp());
          data4[i][j][2*k] = r.re();
          data4[i][j][2*k+1] = r.im();
          /*
          data4[i][j][2*k]=real(exp(2.0*Complex(0.0,1.0)*pi*5.0*double(i)/double(nn[0]))
            *exp(2.0*Complex(0.0,1.0)*pi*7.0*double(j)/double(nn[1]))
            *exp(2.0*Complex(0.0,1.0)*pi*9.0*double(k)/double(nn[2])));
          data4[i][j][2*k+1]=imag(exp(2.0*Complex(0.0,1.0)*pi*5.0*double(i)/double(nn[0]))
            *exp(2.0*Complex(0.0,1.0)*pi*7.0*double(j)/double(nn[1]))
            *exp(2.0*Complex(0.0,1.0)*pi*9.0*double(k)/double(nn[2])));
            **/
        }
        Complex r1 = Complex.I.mul(2.0*pi*5.0*i/nn[0]);
        Complex r2 = Complex.I.mul(2.0*pi*7.0*j/nn[1]);
        Complex r3 = Complex.I.mul(pi*9.0);
        Complex r = r1.exp().mul(r2.exp()).mul(r3.exp());
        speq4[i][2*j]=r.re();
        speq4[i][2*j+1]=r.im();
        /*
        speq4[i][2*j]=real(exp(2.0*Complex(0.0,1.0)*pi*5.0*double(i)/double(nn[0]))
            *exp(2.0*Complex(0.0,1.0)*pi*7.0*double(j)/double(nn[1]))
            *exp(Complex(0.0,1.0)*pi*9.0));
        speq4[i][2*j+1]=imag(exp(2.0*Complex(0.0,1.0)*pi*5.0*double(i)/double(nn[0]))
            *exp(2.0*Complex(0.0,1.0)*pi*7.0*double(j)/double(nn[1]))
            *exp(Complex(0.0,1.0)*pi*9.0));
            */
      }
    }
    // test data3
    for (i=0;i<nn[0];i++) { 
      for (j=0;j<nn[1];j++) {
        for (k=0;k<nn[2];k++) {
          temp=abs(data3[i][j][k]-data4[i][j][k]);
          max = (temp > max ? temp : max);
        }
      }
    }
//    System.out.printf(max);
    localflag = localflag || max > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rlft3, interface2: Forward transform of a chosen delta function did not give expected result for data array");
      
    }
    // test speq3
//    System.out.printf(maxel(matsub(speq3,speq4)));
    localflag = localflag || maxel(matsub(speq3,speq4)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rlft3, interface2: Forward transform of a chosen delta function did not give expected result for speq array");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
