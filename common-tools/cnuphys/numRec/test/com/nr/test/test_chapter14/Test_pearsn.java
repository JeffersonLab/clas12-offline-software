package com.nr.test.test_chapter14;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.pearsn;
import static java.lang.Math.abs;
import static java.lang.Math.log;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_pearsn {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10;
    double doses[]={56.1,64.1,70.0,66.6,82.0,91.3,90.0,99.7,115.3,110.0};
    double spores[]={0.11,0.40,0.37,0.48,0.75,0.66,0.71,1.20,1.01,0.95};
    doubleW prob1=new doubleW(0),r1 = new doubleW(0),z1 = new doubleW(0);
    doubleW prob2=new doubleW(0),r2 = new doubleW(0),z2 = new doubleW(0);
    double sbeps=1.e-16;
    double expect[]={0.9069586,0.2926505e-3,1.510110};
    double[] dose=buildVector(doses),spore=buildVector(spores),dose2=new double[N];
    boolean localflag=false,globalflag=false;

    

    // Test pearsn
    System.out.println("Testing pearsn");

    pearsn(dose,dose,r1,prob1,z1);
//    System.out.printf(r1 << " %f\n", prob1 << " %f\n", z1);
    localflag = (r1.val != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Correlation of an array with itself is not reported as perfect");
      
    }

    localflag = (prob1.val > 1.e-16);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Probability for a perfect correlation was not zero");
      
    }

    for (i=0;i<N;i++) dose2[i]=200.0-dose[i];
    pearsn(dose,dose2,r2,prob2,z2);
//    System.out.printf(r2 << " %f\n", prob2 << " %f\n", z2);
    localflag = (r2.val != -1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Correlation of an array with its negative is not reported as perfect");
      
    }

    localflag = (prob2.val > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Probability for a perfect anticorrelation was not zero");
      
    }

    sbeps=1.e-6;
    pearsn(dose,spore,r1,prob1,z1);  // Data with known results
    localflag = (abs(r1.val - expect[0]) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Unexpected correlation coefficient for test data");
      
    }

    localflag = (abs(prob1.val - expect[1]) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Unexpected probability for test data");
      
    }

    localflag = (abs(z1.val - expect[2]) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Unexpected Fisher's z coefficient for test data");
      
    }

    pearsn(spore,dose,r2,prob2,z2);
//    System.out.printf(r2 << " %f\n", prob2 << " %f\n", z2);
    localflag = (r2.val != r1.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Correlation coefficient modified when arrays swapped");
      
    }

    localflag = (abs(prob2.val-prob1.val) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Probability modified when arrays swapped");
      
    }

    localflag = (abs(z2.val-z1.val) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Fisher's z modified when arrays swapped");
      
    }

    localflag = (abs(z2.val-0.5*log((1+r2.val)/(1-r2.val)))) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** pearsn: Fisher's z not compatible with correlation coefficient");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
