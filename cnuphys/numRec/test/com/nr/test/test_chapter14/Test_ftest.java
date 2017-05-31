package com.nr.test.test_chapter14;

import static com.nr.stat.Moment.avevar;
import static com.nr.stat.Stattests.ftest;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Normaldev;

public class Test_ftest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,NVAL=11,NPTS=10000;
    doubleW ave1=new doubleW(0),var1 = new doubleW(0);
    doubleW f=new doubleW(0),prob = new doubleW(0);
    doubleW f1=new doubleW(0),f2 = new doubleW(0);
    doubleW prob1=new doubleW(0),prob2 = new doubleW(0);
    
    double factor,EPS=0.01,sbeps,sbeps1,sbeps2;
    double fingerprint[] = {1.0,0.618852,0.32215,0.139461,0.0498994,
      0.0147196,0.00357951,0.000718669,0.000119432,1.64815e-5,
      1.89557e-6};
    double[] data1=new double[NPTS],data2=new double[NPTS];
    boolean localflag=false,globalflag=false;

    

    // Test ftest
    System.out.println("Testing ftest");

    // Generate two gaussian distributions with different variances
    Normaldev ndev=new Normaldev(0.0,1.0,17);
    for (j=0;j<NPTS;j++) data1[j]=ndev.dev();
    avevar(data1,ave1,var1);
    for (j=0;j<NPTS;j++) data1[j] -= ave1.val;
    for (j=0;j<NPTS;j++) data2[j]=data1[j];
    ftest(data1,data2,f,prob);
//    System.out.printf(1.0-prob << " %f\n", 1.0-f);
    localflag = (f.val != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ftest: ftest on identical distributions does not return f=1.0");
      
    }

    sbeps=1.e-10;
    localflag = (1.0-prob.val > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ftest: ftest on identical distributions does not return prob=1.0");
      
    }

    for (j=0;j<NPTS;j++) data2[j]=(1.0+EPS)*data1[j];
    ftest(data1,data2,f1,prob1);
//    System.out.printf(f1 << " %f\n", prob1);
    ftest(data2,data1,f2,prob2);
//    System.out.printf(f2 << " %f\n", prob2);
    localflag = (f1.val != f2.val) || (prob1.val != prob2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ftest: ftest not symmetrical with respect to data arrays");
      
    }

    sbeps1=1.0e-14;
    sbeps2=1.e-6;
    for (i=0;i<NVAL;i++) {
      factor=sqrt(1.0+i*EPS);
      for (j=0;j<NPTS;j++) data2[j]=factor*data1[j];
      ftest(data1,data2,f,prob);
//      System.out.printf(f-1.0-i*EPS << " %f\n", prob);
      localflag = (f.val-1.0-i*EPS > sbeps1);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** ftest: Variance ratio f is incorrect");
        
      }

//      System.out.printf(abs(prob-fingerprint[i]));
      localflag = (abs(prob.val-fingerprint[i]) > sbeps2);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** ftest: Probabilities do not agree with previous fingerprint");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
