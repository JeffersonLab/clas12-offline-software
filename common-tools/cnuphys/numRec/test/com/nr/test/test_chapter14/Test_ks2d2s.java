package com.nr.test.test_chapter14;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import static com.nr.stat.Stattests.*;
import org.netlib.util.*;

import com.nr.ran.*;
import com.nr.sort.*;

public class Test_ks2d2s {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,NTRIAL=100,NPT1=1000,NPT2=1000;
    doubleW dd=new doubleW(0),pprob = new doubleW(0);
    double rms,factor,u,ave,sbeps;
    double[] x1=new double[NPT1],y1=new double[NPT1],x2=new double[NPT2],y2=new double[NPT2];
    double[] d=new double[NTRIAL],prob=new double[NTRIAL],f=new double[NTRIAL],g=new double[NTRIAL];
    boolean localflag,globalflag=false;

    // Test ks2d2s
    System.out.println("Testing ks2d2s");

    Ran myran =new Ran(17);
    for (j=1;j<NPT1;j++)
      x1[j]=2.0*myran.doub()-1.0;
    for (j=1;j<NPT1;j++)
      y1[j]=2.0*myran.doub()-1.0;
    for (i=0;i<NTRIAL;i++) {
      for (j=1;j<NPT2;j++)
        x2[j]=2.0*myran.doub()-1.0;
      for (j=1;j<NPT2;j++)
        y2[j]=2.0*myran.doub()-1.0;
      ks2d2s(x1,y1,x2,y2,dd,pprob);
      d[i]=dd.val;
      prob[i]=pprob.val;
    }
    // Note: It was observed that qualitatively if the probabilities
    // are ordered,and then g[i]=p[i]*(2=p[i]), the result is approximately
    // distributed uniformly from 0.0 to 1.0
    Sorter.sort2(d,prob);
    rms=0.0;
    for (i=0;i<NTRIAL;i++) {
      f[i]=prob[i]*(2.0-prob[i]);
      g[i]=(double)(NTRIAL-i)/NTRIAL;
      rms += SQR(f[i]-g[i]);
//      System.out.printf(prob[i] << " %f\n", f[i] << " %f\n", g[i]);
//      System.out.printf(i << " %f\n", d[i] << " %f\n", prob[i]);
    }
    rms=sqrt(rms/NTRIAL);
    System.out.printf("rms: %f\n", rms);

    sbeps = 0.15;
    localflag = rms > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ks2d2s: Result deviated from a qualitative model");
    }

    // Distort the distribution
    factor=0.2;
    ave=0.0;
    for (i=0;i<NTRIAL;i++) {
      for (j=1;j<NPT2;j++) {
        u=myran.doub();
        u=u*((1-factor)+u*factor);
        x2[j]=2.0*u-1.0;
      }
      for (j=1;j<NPT2;j++) {
        u=myran.doub();
        u=u*((1-factor)+u*factor);
        y2[j]=2.0*myran.doub()-1.0;
      }
      ks2d2s(x1,y1,x2,y2,dd,pprob);
      ave += pprob.val;
    }
    ave /= NTRIAL;
    System.out.printf("ave: %f\n", ave);

    sbeps=0.15;
    localflag = ave > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ks2d2s: Distorted data still reported as a good fit");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
