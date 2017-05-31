package com.nr.test.test_chapter14;

import static com.nr.stat.Stattests.ksone;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.ran.Ran;

public class Test_ksone {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int j,NPTS=10000;
    doubleW d1=new doubleW(0),prob1 = new doubleW(0);
    doubleW d2=new doubleW(0),prob2 = new doubleW(0);
    double[] data1=new double[NPTS],data2=new double[NPTS];
    boolean localflag=false,globalflag=false;

    

    // Test ksone
    System.out.println("Testing ksone");

    Ran myran =new Ran(17);
    func1_ksone func1_ksone = new func1_ksone();
    for (j=0;j<NPTS;j++) data1[j]=myran.doub();
    ksone(data1,func1_ksone,d1,prob1);
//    System.out.printf(setw(17) << d1 << setw(17) << prob1);
    localflag = localflag || (prob1.val < 0.2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ksone: Unexpectedly low probability for a uniform distribution");
      
    }

    for (j=0;j<NPTS;j++) data2[j]=2.0*data1[j];
    func2_ksone func2_ksone = new func2_ksone();
    ksone(data2,func2_ksone,d2,prob2);
//    System.out.printf(setw(17) << d2 << setw(17) << prob2);
    localflag = localflag || (prob1.val != prob2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ksone: Scaled distribution did not return same statistic");
      
    }

    ksone(data2,func1_ksone,d2,prob2);
//    System.out.printf(setw(17) << d2 << setw(17) << prob2);
    localflag = localflag || abs(d2.val-0.5) > 0.05;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ksone: Mismatched distribution did not give correct K-S statistic");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class func1_ksone implements UniVarRealValueFun {
  public double funk(final double x) {
    return (x > 1.0 ? 1.0 : x);
  }
  }
  class func2_ksone implements UniVarRealValueFun {
  public double funk(final double x) {
    return (x > 2.0 ? 1.0 : x/2.0);
  }
  }

}
