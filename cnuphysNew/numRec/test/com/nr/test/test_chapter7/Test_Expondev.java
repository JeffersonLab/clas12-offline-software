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

import com.nr.ran.Expondev;
import com.nr.sf.Expondist;
public class Test_Expondev {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nbin,N=100000,M=1000;
    doubleW df = new doubleW(0);
    doubleW chisq = new doubleW(0);
    doubleW prob = new doubleW(0);
    double range=10.0,binsize,beta=1.0,sbeps=1.e-15;
    double fpr[]={4.2243978049659718,0.90295803506245254,0.12403099453814931,
      0.079329948033191738,1.3165010419652894,2.4667405038323449,
      0.95549662657685031,0.28104150659487703,4.8108667409405195,
      0.82390385975005687};
    double[] fingerprint = buildVector(fpr);
    double[] x = new double[M],bins =new double[M],ebins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Expondev
    System.out.println("Testing Expondev");

    // Check fingerprint of doub()
    Expondev myran = new Expondev(beta,17);
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || abs(myran.dev()-fingerprint[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondev: dev() does not match fingerprint");
      
    }

    // Check statistics
    Expondist expect = new Expondist(beta);
    binsize=range/(M);
    for (i=0;i<M;i++) {
      x[i]=binsize*i;
      ebins[i]=N*binsize*expect.p(x[i]+0.5*binsize);
      bins[i]=0;
    }
    for (i=0;i<N;i++) {
      nbin=(int)(floor(myran.dev()/binsize));
      if (nbin < M) bins[nbin] += 1;
    }
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,dev(): %f  prob: %f\n", chisq.val,prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Expondev: dev() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
