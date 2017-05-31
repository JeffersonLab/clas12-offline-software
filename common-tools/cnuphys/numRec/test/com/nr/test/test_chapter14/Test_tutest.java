package com.nr.test.test_chapter14;

import static com.nr.stat.Moment.avevar;
import static com.nr.stat.Stattests.tutest;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Normaldev;

public class Test_tutest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,NPTS=10000,NSHFT=10;
    doubleW ave1=new doubleW(0),ave2 = new doubleW(0);
    doubleW var1 = new doubleW(0), var2 = new doubleW(0);
    double EPS=0.01,sd,sbeps;
    double fingerprint[]={0.024980,0.072904,0.178591,0.369851,0.653879,
      1.0,0.653879,0.369851,0.178591,0.072904,0.024980};
    double[] data1=new double[NPTS],data2=new double[NPTS];
    double[] t=new double[NSHFT+1],prob=new double[NSHFT+1],tt= new double[NSHFT+1];
    boolean localflag=false,globalflag=false;

    

    // Test tutest
    System.out.println("Testing tutest");

    // Generate two gaussian distributions of different variance
    Normaldev ndev = new Normaldev(0.0,1.0,17);
    for (i=0;i<NPTS;i++) data1[i]=ndev.dev();
    avevar(data1,ave1,var1);
    for (i=0;i<NPTS;i++) data1[i] -= ave1.val;
    // Conclass data with exactly twice the standard deviation
    for (i=0;i<NPTS;i++) data2[i]=NSHFT/2.0*EPS+2.0*data1[i];
    avevar(data2,ave2,var2);
//    System.out.printf(ave1 << " %f\n", ave2 << " %f\n", var1 << " %f\n", var2);
    sd=sqrt((var1.val+var2.val)/NPTS);
    for (i=0;i<NSHFT+1;i++) {
      doubleW tw = new doubleW(0);
      doubleW pw = new doubleW(0);
      tutest(data1,data2,tw, pw); t[i]=tw.val;prob[i]=pw.val;
      for (j=0;j<NPTS;j++) data1[j] += EPS;
      tt[i]=(-NSHFT/2.0+i)*EPS/sd;
//      System.out.printf(t[i] << " %f\n", prob[i] << " %f\n", tt[i]);
    }
    sbeps= 1.e-13;
//    System.out.printf(maxel(vecsub(t,tt)));
    localflag = localflag || (maxel(vecsub(t,tt)) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** tutest: Returned t statistic incorrect for special case of scaled distributions");
      
    }

    for (i=0;i<=NSHFT/2;i++)
      localflag = localflag || abs(t[i]+t[NSHFT-i]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** tutest: Returned t statistic has incorrect symmetry");
      
    }

    sbeps=1.e-6;
    for (i=0;i<NSHFT+1;i++)
      localflag = localflag || abs(fingerprint[i]-prob[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** tutest: Return probabilities don't match fingerprint");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
