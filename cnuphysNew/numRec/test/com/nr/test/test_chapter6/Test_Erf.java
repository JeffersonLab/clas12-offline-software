package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Erf;

public class Test_Erf {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=13;
    double sbeps;
    double x[]={-4.0,-3.0,-2.0,-1.0,-0.5,-0.1,0.0,0.1,0.5,1.0,2.0,3.0,4.0};
    double y1[]={-9.999999845827421e-1,-9.999779095030015e-1,-9.953222650189527e-1,
      -8.427007929497148e-1,-5.204998778130465e-1,-1.124629160182849e-1,0.0,
      1.124629160182849e-1,5.204998778130465e-1,8.427007929497148e-1,
      9.953222650189527e-1,9.999779095030015e-1,9.999999845827421e-1};
    double y2[]={1.999999984582742e+000,1.999977909503002e+000,1.995322265018953e+000,
      1.842700792949715e+000,1.520499877813047e+000,1.112462916018285e+000,1.0,
      8.875370839817152e-001,4.795001221869535e-001,1.572992070502852e-001,
      4.677734981047265e-003,2.209049699858543e-005,1.541725790028003e-008};
    double[] xx=buildVector(x),yy1=buildVector(y1),yy2=buildVector(y2),zz1= new double[N],zz2= new double[N],zz3= new double[N],zz4= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Erf
    System.out.println("Testing Erf");

    Erf e = new Erf();
    for (i=0;i<N;i++) {
      zz1[i]=e.erf(xx[i]);
      zz2[i]=e.erfc(xx[i]);
    }

    sbeps=5.e-15;
    System.out.printf("Erf, erf(): Maximum discrepancy = %f\n", maxel(vecsub(zz1,yy1)));
    localflag = maxel(vecsub(zz1,yy1)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Erf, erf(): Incorrect function values");
      
    }

    sbeps=5.e-15;
    System.out.printf("Erf, erfc(): Maximum discrepancy = %f\n", maxel(vecsub(zz2,yy2)));
    localflag = maxel(vecsub(zz2,yy2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Erf, erfc(): Incorrect function values");
      
    }

    for (i=0;i<N;i++) {
      zz3[i]=e.inverf(zz1[i]);
      zz4[i]=e.inverfc(zz2[i]);
    }

    sbeps=1.e-9;
    System.out.printf("Erf, inverf(): Maximum discrepancy = %f\n", maxel(vecsub(zz3,xx)));
    localflag = maxel(vecsub(zz3,xx)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Erf, inverf(): Inversion does not return to original arguments");
      
    }

    sbeps=1.e-9;
    System.out.printf("Erf, inverfc(): Maximum discrepancy = %f\n", maxel(vecsub(zz4,xx)));
    localflag = maxel(vecsub(zz4,xx)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Erf, inverfc(): Inversion does not return to original arguments");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
