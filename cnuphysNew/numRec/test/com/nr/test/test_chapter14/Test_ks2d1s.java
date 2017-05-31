package com.nr.test.test_chapter14;

import static com.nr.NRUtil.SQR;
import static com.nr.stat.Stattests.ks2d1s;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Ran;
import com.nr.sort.*;
import com.nr.stat.Quadvl;

public class Test_ks2d1s {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,NTRIAL=100,NPT=1000;
    doubleW dd=new doubleW(0),pprob = new doubleW(0);
    double rms,u,factor,ave,sbeps;
    double[] x=new double[NPT],y=new double[NPT];
    double[] d=new double[NTRIAL],prob=new double[NTRIAL];
    double[] f=new double[NTRIAL],g=new double[NTRIAL];
    boolean localflag,globalflag=false;

    Quadvl quadvl = new Quadvl();

    // Test ks2d1s
    System.out.println("Testing ks2d1s");

    Ran myran=new Ran(17);
    for (i=0;i<NTRIAL;i++) {
      for (j=1;j<NPT;j++)
        x[j]=2.0*myran.doub()-1.0;
      for (j=1;j<NPT;j++)
        y[j]=2.0*myran.doub()-1.0;
      ks2d1s(x,y,quadvl,dd,pprob);
      d[i]=dd.val;
      prob[i]=pprob.val;
    }
    // Note: It was observed that qualitatively if the probabilities
    // are ordered,and then g[i]=p[i]*(2=p[i]), the result is approximately
    // distributed uniformly from 0.0 to 1.0
    Sorter.sort2(d,prob);
    rms=0.0;
    for (i=0;i<NTRIAL;i++) {
      f[i]=(double)(NTRIAL-i)/NTRIAL;
      g[i]=prob[i]*(2.0-prob[i]);
      rms += SQR(f[i]-g[i]);
//      System.out.printf(prob[i] << " %f\n", f[i] << " %f\n", g[i]);
//      System.out.printf(i << " %f\n", d[i] << " %f\n", prob[i]);
    }
    rms=sqrt(rms/NTRIAL);
    System.out.printf("rms: %f\n", rms);

    sbeps = 0.08;
    localflag = rms > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ks2d1s: Result deviated from a qualitative model");
      
    }

    // Distort the distribution
    factor=0.2;
    ave=0.0;
    for (i=0;i<NTRIAL;i++) {
      for (j=1;j<NPT;j++) {
        u=myran.doub();
        u=u*((1-factor)+u*factor);
        x[j]=2.0*u-1.0;
      }
      for (j=1;j<NPT;j++) {
        u=myran.doub();
        u=u*((1-factor)+u*factor);
        y[j]=2.0*myran.doub()-1.0;
      }
      ks2d1s(x,y,quadvl,dd,pprob);
      ave += pprob.val;
    }
    ave /= NTRIAL;
    System.out.printf("ave: %f\n", ave);

    sbeps=0.05;
    localflag = ave > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ks2d1s: Distorted data still reported as a good fit");
      
    }
   
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
