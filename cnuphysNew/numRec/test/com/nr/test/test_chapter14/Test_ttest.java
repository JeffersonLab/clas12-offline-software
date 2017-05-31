package com.nr.test.test_chapter14;

import static com.nr.stat.Moment.avevar;
import static com.nr.stat.Stattests.ttest;
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

public class Test_ttest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j;
    int NPTS=10000,NSHFT=10;
    doubleW ave1=new doubleW(0),ave2 = new doubleW(0);
    doubleW var1 = new doubleW(0), var2 = new doubleW(0);
    double sd,EPS=0.005,sbeps;
    double fingerprint[]={0.076341,0.156226,0.287591,0.478354,0.722981,
      1.0,0.722981,0.478354,0.287591,0.156226,0.076341};
    double[] data1=new double[NPTS],data2=new double[NPTS];
    double[] t=new double[NSHFT+1],prob=new double[NSHFT+1],tt= new double[NSHFT+1];
    boolean localflag=false, globalflag=false;

    

    // Test ttest
    System.out.println("Testing ttest");

    // Generate gaussian distributed data
    Normaldev ndev=new Normaldev(0.0,1.0,17);
    // Special case: identical distributions
    for (i=0;i<NPTS;i++) data1[i]=ndev.dev();
    for (i=0;i<NPTS;i++) data2[i]=(NSHFT/2.0)*EPS+data1[i];
    avevar(data1,ave1,var1);
    avevar(data2,ave2,var2);
    sd=sqrt(var1.val/NPTS*2.0);
    for (i=0;i<NSHFT+1;i++) {
      doubleW tw = new doubleW(0);
      doubleW pw = new doubleW(0);
      ttest(data1,data2,tw,pw);t[i]=tw.val;prob[i]=pw.val;
      tt[i]=((-NSHFT/2.0)+i)*EPS/sd;
//      System.out.printf(i << "  %f\n", t[i] << " %f\n", tt[i] << "  %f\n", prob[i]);
      for (j=0;j<NPTS;j++) data1[j] += EPS;
    }
    sbeps= 1.e-13;
//    System.out.printf(maxel(vecsub(t,tt)));
    localflag = localflag || (maxel(vecsub(t,tt)) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ttest: Returned t statistic incorrect for special case of identical distributions");
      
    }

    for (i=0;i<=NSHFT/2;i++)
      localflag = localflag || abs(t[i]+t[NSHFT-i]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ttest: Returned t statistic has incorrect symmetry");
      
    }

    sbeps=1.e-6;
    for (i=0;i<NSHFT+1;i++)
      localflag = localflag || abs(fingerprint[i]-prob[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ttest: Return probabilities don't match fingerprint");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
