package com.nr.test.test_chapter14;

import static com.nr.stat.Stattests.kstwo;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Ran;

public class Test_kstwo {

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

    

    // Test kstwo
    System.out.println("Testing kstwo");

    Ran myran=new Ran(17);
    for (j=0;j<NPTS;j++) data1[j]=myran.doub();
    kstwo(data1,data1,d1,prob1);
//    System.out.printf(setw(17) << d1 << setw(17) << prob1);
    localflag = localflag || (d1.val != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ksone: Unexpected K-S statistic for matching distributions");
      
    }

    localflag = localflag || (prob1.val != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ksone: Unexpected probability for matching distributions");
      
    }

    for (j=0;j<NPTS;j++) data2[j]=2.0*data1[j];
    kstwo(data1,data2,d2,prob2);
//    System.out.printf(setw(17) << d2 << setw(17) << prob2);
    localflag = localflag || abs(d2.val-0.5) > 0.05;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ksone: Scaled distribution did not give expected K-S statistic");
      
    }

    kstwo(data2,data1,d1,prob1);
//    System.out.printf(setw(17) << d1 << setw(17) << prob1);
    localflag = localflag || (prob1.val != prob2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ksone: Swapped distributions did not give same K-S statistic");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
