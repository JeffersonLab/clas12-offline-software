package com.nr.test.test_chapter14;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Moment.avevar;
import static com.nr.stat.Stattests.tptest;
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

public class Test_tptest {

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
    double cov,sd,EPS=0.01,sbeps;
    double fingerprint[]={0.012206481763442537,0.044962205835956524,
      0.13262719786074151,0.31606378510286803,0.61616056459021917,
      1.0,0.61616056459021917,0.31606378510286803,0.13262719786074151,
      0.044962205835956524,0.012206481763442537};
    double[] data1=new double[NPTS],data2=new double[NPTS];
    double[] t=new double[NSHFT+1],prob=new double[NSHFT+1],texpect= new double[NSHFT+1];
    double[] fp=buildVector(fingerprint);
    boolean localflag=false,globalflag=false;

    

    // Test tptest
    System.out.println("Testing tptest");

    Normaldev ndev = new Normaldev(0.0,1.0,17);
    // Generate identical data but with a shift
    for (j=0;j<NPTS;j++) data1[j]=ndev.dev();
    avevar(data1,ave1,var1);
    for (j=0;j<NPTS;j++) {
      data1[j] -= ave1.val;
      data2[j] = -data1[j];
      data1[j] -= (NSHFT/2.0)*EPS;
    }
    avevar(data1,ave1,var1);
    avevar(data2,ave2,var2);
    cov=-var1.val;
    sd=sqrt((2.0*var1.val-2.0*cov)/NPTS);
    for (i=0;i<NSHFT+1;i++) {
      doubleW tw = new doubleW(0);
      doubleW pw = new doubleW(0);
      tptest(data1,data2,tw,pw);t[i]=tw.val;prob[i]=pw.val;
      texpect[i]=(-NSHFT/2.0+i)*EPS/sd;
//      System.out.printf(setprecision(20) << t[i] << " %f\n", prob[i] << " %f\n", texpect[i]);
      for (j=0;j<NPTS;j++) data1[j] += EPS;
    }

    sbeps=1.e-13;
//    System.out.printf(maxel(vecsub(t,texpect)));
    localflag = maxel(vecsub(t,texpect)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** tptest: Incorrect returned values for Student's t");
      
    }

    sbeps=1.e-15;
    localflag=false;
    for (i=1;i<NSHFT/2+1;i++) {
//      System.out.printf(abs(prob[i]/prob[NSHFT-i]-1.0));
      localflag = abs(prob[i]/prob[NSHFT-i]-1.0) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** tptest: Probabilities unsymmetrical for symmetrical shifts");
      
    }

    //Fingerprint test
    sbeps=3.e-12;
    localflag = maxel(vecsub(prob,fp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** tptest: Probabilities do not match fingerprint");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
