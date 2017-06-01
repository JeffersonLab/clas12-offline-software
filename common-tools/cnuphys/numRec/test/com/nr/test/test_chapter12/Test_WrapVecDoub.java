package com.nr.test.test_chapter12;

import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;
import com.nr.fft.WrapVecDoub;
public class Test_WrapVecDoub {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=16;
    boolean localflag=false, globalflag=false;

    // Test Wrapdouble[], interface1
    System.out.println("Testing WrapVecDoub[], interface1");

    WrapVecDoub data = new WrapVecDoub(2*N);
    for (i=0;i<N;i++) data.set(i,new Complex(2*i,2*i+1));
    for (i=0;i<N;i++)
      localflag = localflag || (data.real(i) != 2*i) || (data.imag(i) != 2*i+1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** WrapVecDoub[], interface1: Failure to write complex, and read with .real() and .imag()");
      
    }

    for (i=0;i<N;i++) {
      data.setReal(i,2.0*(N-1)-2.0*i+1.0);
      data.setImag(i,2.0*(N-1)-2.0*i);
    }
    for (i=0;i<N;i++)
      localflag = localflag || (!data.get(i).equals(new Complex(2.0*(N-1)-2.0*i+1.0,2.0*(N-1)-2.0*i)));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** WrapVecDoub[], interface1: Failure to write with .real() and .imag(), and read complex");
      
    }

    // test conversion operator
    localflag = localflag || maxel(data.get()) != 2.0*N-1.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** WrapVecDoub[], interface1: Failure to convert WrapVecDoub[] to double[]");
      
    }

    // test the periodic indexing
    for (i=0;i<N;i++)
      localflag = localflag || data.get(i).sub(data.get(i-N)).abs()!= 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** WrapVecDoub[], interface1: Failure of indexing to have period of N");
      
    }
    
    // Test WrapVecDoub[], interface2
    System.out.println("Testing WrapVecDoub[], interface2");
    double[] dat = new double[2*N];
    for (i=0;i<2*N;i++) dat[i]=i;
    WrapVecDoub data2 = new WrapVecDoub(dat);

    for (i=0;i<N;i++)
      localflag = localflag || (data2.real(i) != 2*i) || (data2.imag(i) != 2*i+1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** WrapVecDoub[], interface2: Incorrect values recovered with .real() and .imag() from initialized WrapVecDoub[]");
      
    }

    for (i=0;i<N;i++)
      localflag = localflag || !data2.get(i).equals(new Complex(2.0*i,2.0*i+1.0));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** WrapVecDoub[], interface: Incorrect values recovered with [] from initialized WrapVecDoub[]");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
