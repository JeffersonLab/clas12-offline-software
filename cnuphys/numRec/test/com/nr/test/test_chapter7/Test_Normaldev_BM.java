package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.chsone;
import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Normaldev_BM;
import com.nr.sf.Normaldist;
public class Test_Normaldev_BM {

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
    double fpr[]={-0.040234259938936714,-0.20636252953342041,-0.39109108994808306,
      -0.2184933724157597,1.3879841011885372,-0.62802347682638449,
      -0.023082200554415865,-0.18525427215527071,-1.5890668800145005,
      -1.728054800100431};
    double[] fingerprint=buildVector(fpr);
    double[] x=new double[M],bins =new double[M],ebins =new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Normaldev_BM
    System.out.println("Testing Normaldev_BM");

    // Check fingerprint of doub()
    mu=0.0;
    sig=1.0;
    Normaldev_BM myran = new Normaldev_BM(mu,sig,17);
    localflag=false;
    for (i=0;i<10;i++)
//      System.out.printf(setw(25) << setprecision(20) << myran.dev());
      localflag=localflag || abs(myran.dev()-fingerprint[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldev_BM: dev() does not match fingerprint");
      
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
    System.out.printf("     chisq,dev(): %f  prob: %f\n",chisq.val, prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Normaldev_BM: dev() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
