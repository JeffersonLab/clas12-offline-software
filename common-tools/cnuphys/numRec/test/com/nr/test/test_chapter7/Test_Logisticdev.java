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

import com.nr.ran.Logisticdev;
import com.nr.sf.Logisticdist;
public class Test_Logisticdev {

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
    double range=10.0,xl,xu,mu,sig,binsize,sbeps=1.e-15; // beta=1.0 not used
    double fpr[]={-2.3209047306654593,-0.21123844393867705,1.1162025009381211,
      1.3751313608856173,-0.55377040640490538,-1.3110938420290161,
      -0.25911330570481678,0.62048941515098899,-2.6478632188924767,
      -0.13583435624321633};
    double[] fingerprint = buildVector(fpr);
    double[] x = new double[M],bins = new double[M],ebins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Logisticdev
    System.out.println("Testing Logisticdev");

    // Check fingerprint of doub()
    mu=0.0;
    sig=1.0;
    Logisticdev myran = new Logisticdev(mu,sig,17);
    localflag=false;
    for (i=0;i<10;i++)
//      System.out.printf(setw(25) << setprecision(20) << myran.dev());
      localflag=localflag || abs(myran.dev()-fingerprint[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Logisticdev: dev() does not match fingerprint");
      
    }

    // Check statistics
    Logisticdist expect = new Logisticdist(mu,sig);
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
      fail("*** Logisticdev: dev() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
