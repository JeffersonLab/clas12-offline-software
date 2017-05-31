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

import com.nr.ran.Cauchydev;
import com.nr.sf.Cauchydist;

public class Test_Cauchydev {

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
    double fpr[]={-5.4660758319305183,-0.30564072790788632,-0.44755371579220488,
      -1.9442849501644124,0.66387554413406102,-0.004358322069596801,
      0.50260252584448173,9.5625234809339883,0.33471895222248255,
      -0.18486043134925173};
    double[] fingerprint = buildVector(fpr);
    double[] x = new double[M],bins = new double[M],ebins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Cauchydev
    System.out.println("Testing Cauchydev");

    // Check fingerprint of doub()
    mu=0.0;
    sig=1.0;
    Cauchydev myran = new Cauchydev(mu,sig,17);
    localflag=false;
    for (i=0;i<10;i++) {
      //System.out.printf("%.20f    %.20f\n", myran.dev(), fingerprint[i]);
      localflag=localflag || abs(myran.dev()-fingerprint[i])>sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      // fail("*** Cauchydev: dev() does not match fingerprint");
      
    }

    // Check statistics
    Cauchydist expect = new Cauchydist(mu,sig);
    xl=mu-range/2.0;
    xu=mu+range/2.0;
    binsize=range/(M);
    for (i=0;i<M;i++) {
      x[i]=xl+binsize*i;
      ebins[i]=(N)*binsize*expect.p(x[i]+0.5*binsize);
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
      fail("*** Cauchydev: dev() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
