package com.nr.test.test_chapter14;

import static com.nr.NRUtil.buildMatrix;
import static com.nr.stat.Stattests.kendl2;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;
public class Test_kendl2 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int N=3,M=3;
    doubleW tau1=new doubleW(0),z1 = new doubleW(0),prob1 = new doubleW(0);
    double sbeps=1.e-6;
    double adata[]={1.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,1.0};
    double bdata[]={0.0,0.0,1.0,0.0,1.0,0.0,1.0,0.0,0.0};
    double cdata[]={2.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,2.0};
    double ddata[]={0.0,1.0,0.0,1.0,0.0,1.0,0.0,1.0,0.0};
    double edata[]={1.0,0.0,1.0,0.0,1.0,0.0,1.0,0.0,1.0};
    double fdata[]={0.0,1.0,0.0,2.0,0.0,3.0,0.0,4.0,0.0};
    double gdata[]={1.0,0.0,2.0,0.0,3.0,0.0,4.0,0.0,5.0};
    double hdata[]={1.0,0.0,4.0,0.0,3.0,0.0,2.0,0.0,5.0};
    // Expected results for each test case
    double ae[]={1.0,1.566699,0.117185};
    double be[]={-1.0,-1.566699,0.117185};
    double ce[]={1.0,2.818009,0.00483224};
    double de[]={0.0,0.0,1.0};
    double ee[]={0.0,0.0,1.0};
    double fe[]={-0.100056,-0.402716,0.687157};
    double ge[]={-0.0448561,-0.233079,0.815700};
    double[][] a=buildMatrix(N,M,adata),b=buildMatrix(N,M,bdata),c=buildMatrix(N,M,cdata),
      d=buildMatrix(N,M,ddata),e=buildMatrix(N,M,edata),f=buildMatrix(N,M,fdata),
      g=buildMatrix(N,M,gdata),h=buildMatrix(N,M,hdata);
    boolean localflag=false,globalflag=false;

    

    // Test kendl2
    System.out.println("Testing kendl2");

    // Test 1
    kendl2(a,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ae[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Perfect correlation should give tau1=1.0");
      
    }

    localflag = localflag || abs(z1.val-ae[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Value of z1 should be sqrt(54/22)=1.566699 for this case");
      
    }

    localflag = localflag || abs(prob1.val-ae[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1, given the value of z1");
      
    }

    // Test 2
    kendl2(b,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-be[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Perfect anti-correlation should give tau1=-1.0");
      
    }

    localflag = localflag || abs(z1.val-be[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Value of z1 should be -sqrt(54/22)=-1.566699 for this case");
      
    }

    localflag = localflag || abs(prob1.val-be[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1, given the value of z1");
      
    }

    // Test 3 (= Test 1 with table entries doubled)
    kendl2(c,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ce[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Perfect correlation should give tau1=1.0");
      
    }

    localflag = localflag || abs(z1.val-ce[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Value of z1 should be sqrt(270/34)=2.818009 for this case");
      
    }

    localflag = localflag || abs(prob1.val-ce[2])/ce[2] > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1, given the value of z1");
      
    }

    // Test 4
    kendl2(d,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-de[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Concordant and discordant pairs should cancel in this case");
      
    }

    localflag = localflag || abs(z1.val-de[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: z should be zero because s is zero");
      
    }

    localflag = localflag || abs(prob1.val-de[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1.  Should be 1.0 for z=0");
      
    }

    // Test 5
    kendl2(e,tau1,z1,prob1);
//    System.out.printf(tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ee[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Concordant and discordant pairs should cancel in this case");
      
    }

    localflag = localflag || abs(z1.val-ee[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: z should be zero because s is zero");
      
    }

    localflag = localflag || abs(prob1.val-ee[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1.  Should be 1.0 for z=0");
      
    }

    // Test 6
    kendl2(f,tau1,z1,prob1);
//    System.out.printf(setprecision(10) << tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-fe[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Value of tau1 should be -3/sqrt(29*31)=-0.100056");
      
    }

    localflag = localflag || abs(z1.val-fe[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: z1 shoud be tau*sqrt(81/5)=-0.402716");
      
    }

    localflag = localflag || abs(prob1.val-fe[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1, given the value of z1");
      
    }

    // Test 7
    kendl2(g,tau1,z1,prob1);
//    System.out.printf(setprecision(10) << tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ge[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Value of tau1 should be -3/sqrt(63*71)=-0.044856");
      
    }

    localflag = localflag || abs(z1.val-ge[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: z1 shoud be tau*sqrt(1890/70)=-0.233079");
      
    }

    localflag = localflag || abs(prob1.val-ge[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1, given the value of z1");
      
    }

    // Test 8 (= Transpose of test 7)
    kendl2(h,tau1,z1,prob1);
//    System.out.printf(setprecision(10) << tau1 << " %f\n", z1 << " %f\n", prob1);
    localflag = localflag || abs(tau1.val-ge[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl2: Transpose of table should give same result");
      
    }

    localflag = localflag || abs(z1.val-ge[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Transpose of table should give same result");
      
    }

    localflag = localflag || abs(prob1.val-ge[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** kendl1: Unexpected value of prob1, given the value of z1");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
