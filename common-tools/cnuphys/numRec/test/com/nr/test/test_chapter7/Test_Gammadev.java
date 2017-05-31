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

import com.nr.ran.Gammadev;
import com.nr.sf.Gammadist;

public class Test_Gammadev {

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
    double range=10.0,alpha,beta,binsize,sbeps=2.e-15;
    double fpr[]={4.9886631780655621,2.1169108315306531,0.80651179816628638,
      2.6460107411356208,4.0099509519546519,1.942804746662151,
      3.7042540063195508,2.3875743636427345,1.9483140166958288,
      2.4434025578383811};
    double[] fingerprint = buildVector(fpr);
    double[] x = new double[M],bins=new double[M],ebins=new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Gammadev
    System.out.println("Testing Gammadev");

    // Check fingerprint of doub()
    alpha=3.0;
    beta=1.0;
    Gammadev myran=new Gammadev(alpha,beta,17);
    localflag=false;
    for (i=0;i<10;i++)
//      System.out.printf(setw(25) << setprecision(20) << myran.dev());
      localflag=localflag || abs(myran.dev()-fingerprint[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadev: dev() does not match fingerprint");
      
    }

    // Check statistics
    Gammadist expect=new Gammadist(alpha,beta);
    binsize=range/M;
    for (i=0;i<M;i++) {
      x[i]=binsize*i;
      ebins[i]=N*binsize*expect.p(x[i]+0.5*binsize);
      bins[i]=0;
    }
    for (i=0;i<N;i++) {
      nbin=(int)(floor(myran.dev()/binsize));
      if ((nbin >= 0) && (nbin < M)) bins[nbin] += 1;
    }
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,dev(): %f  prob: %f\n",chisq.val, prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gammadev: dev() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
