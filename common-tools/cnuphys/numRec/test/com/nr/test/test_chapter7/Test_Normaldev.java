package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.chsone;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Normaldev;
import com.nr.sf.Normaldist;
import static java.lang.Math.*;

public class Test_Normaldev {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,nbin,N=100000,M=1000;
    doubleW df = new doubleW(0);
    doubleW chisq = new doubleW(0);
    doubleW prob = new doubleW(0);
    double range=10.0,xl,xu,mu,sig,binsize,sbeps=1.e-15;
    double fpr[]={1.1374125522420115,-0.35950397273931667,-1.6105670258529632,
      0.42167950591239073,0.42086816489557621,0.71359860938979924,
      0.56718836082663404,0.10148179520762551,0.47465403328436934,
      -0.4866388952851291};
    double[] fingerprint=buildVector(fpr);
    double[] x=new double[M],bins =new double[M],ebins =new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Normaldev
    System.out.println("Testing Normaldev");

    // Check fingerprint of doub()
    mu=0.0;
    sig=1.0;
    Normaldev myran = new Normaldev(mu,sig,17);
    localflag=false;
    for (i=0;i<10;i++)
//      System.out.printf(setw(25) << setprecision(20) << myran.dev());
      localflag=localflag || abs(myran.dev()-fingerprint[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldev: dev() does not match fingerprint");
      
    }

    // Check statistics
    Normaldist expect = new Normaldist(mu,sig);
    xl=mu-range/2.0;
    xu=mu+range/2.0;
    binsize=range/M;
    for (i=0;i<M;i++) {
      x[i]=xl+binsize*i;
      ebins[i]=N*binsize*expect.p(x[i]+0.5*binsize);
      bins[i]=0;
    }
    for (i=0;i<N;i++) {
      nbin=(int)(floor((0.5*range+myran.dev())/binsize));
      if ((nbin >= 0) && (nbin < M)) bins[nbin] += 1;
    }
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,dev(): %f  prob: %f\n", chisq.val, prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldev: dev() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
