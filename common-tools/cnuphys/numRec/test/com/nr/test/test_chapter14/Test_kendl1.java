package com.nr.test.test_chapter14;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.kendl1;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_kendl1 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    doubleW tau1=new doubleW(0),z1 = new doubleW(0),prob1 = new doubleW(0);
    doubleW tau2=new doubleW(0),z2 = new doubleW(0),prob2 = new doubleW(0);
    double sbeps=1.e-6;
    double adata[]={0.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0};
    double bdata[]={9.0,8.0,7.0,6.0,5.0,4.0,3.0,2.0,1.0,0.0};
    double cdata[]={1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,0.0}; // Note 0.0 at end
    double edata[]={1.0,2.0,1.0,2.0,1.0,2.0,1.0,2.0,1.0,2.0};
    double fdata[]={2.0,1.0,2.0,1.0,2.0,1.0,2.0,1.0,2.0,1.0};
    double gdata[]={1.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0}; // Case with extra x
    double hdata[]={1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,9.0}; // and extra y
    // Expected results for each test case
    double ae[]={1.0,4.024922,5.69941e-5};
    double be[]={-1.0,-4.024922,5.69941e-5};
    double ce[]={0.6,2.414953,0.01573722};
    double de[]={-1.0,-4.024922,5.69941e-5};
    double ee[]={0.977273,3.933447,8.37364e-5};
    double[] a=buildVector(adata),b=buildVector(bdata);
    double[] c=buildVector(cdata),e=buildVector(edata);
    double[] f=buildVector(fdata),g=buildVector(gdata),h=buildVector(hdata);
    boolean localflag=false,globalflag=false;

    

    // Test kendl1
    System.out.println("Testing kendl1");

    kendl1(a,a,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ae[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Tau should be 1 for perfect correlation");
      
    }

    localflag = localflag || abs(z1.val-ae[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected number of standard deviations.  Should be 9/sqrt(5)");
      
    }

    localflag = localflag || abs(prob1.val-ae[2])/ae[2] > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob, given the value of z");
      
    }

    kendl1(a,b,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-be[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Tau should be -1 for perfect anticorrelation");
      
    }

    localflag = localflag || abs(z1.val-be[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected number of standard deviations.  Should be -9/sqrt(5)");
      
    }

    localflag = localflag || abs(prob1.val-be[2])/be[2] > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpect value of prob, given the value of z");
      
    }

    kendl1(b,a,tau2,z2,prob2);
//    System.out.printf(tau2 << " %f\n", z2 << " %f\n", prob2);
    localflag = localflag || (tau1.val != tau2.val) || (z1.val != z2.val) || (prob1.val != prob2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Results changed when two arrays were swapped (case 1)");
      
    }

    kendl1(a,c,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ce[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Tau should be (36-9)/45=0.6 for this special case");
      
    }

    localflag = localflag || abs(z1.val-ce[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected number of standard deviations.  Should be 9*tau/sqrt(5)=2.414953");
      
    }

    localflag = localflag || abs(prob1.val-ce[2])/ce[2] > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpect value of prob, given the value of z");
      
    }

    kendl1(c,a,tau2,z2,prob2);
//    System.out.printf(tau2 << " %f\n", z2 << " %f\n", prob2);
    localflag = localflag || (tau1.val != tau2.val) || (z1.val != z2.val) || (prob1.val != prob2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Results changed when two arrays were swapped (case 2)");
      
    }

    kendl1(e,f,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-de[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Tau should be -1 for perfect anticorrelation");
      
    }

    localflag = localflag || abs(z1.val-de[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected number of standard deviations.  Should be -9/sqrt(5)");
      
    }

    localflag = localflag || abs(prob1.val-de[2])/de[2] > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpect value of prob, given the value of z");
      
    }

    kendl1(f,e,tau2,z2,prob2);
//    System.out.printf(tau2 << " %f\n", z2 << " %f\n", prob2);
    localflag = localflag || (tau1.val != tau2.val) || (z1.val != z2.val) || (prob1.val != prob2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Results changed when two arrays were swapped (case 3)");
      
    }

    kendl1(g,h,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ee[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of tau. Should be (43/44)=0.977273");
      
    }

    localflag = localflag || abs(z1.val-ee[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected number of standard deviations.  Should be (43/44)*9/sqrt(5)");
      
    }

    localflag = localflag || abs(prob1.val-ee[2])/ee[2] > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob, given the value of z");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
