package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.chsone;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Binomialdev;
import com.nr.sf.Binomialdist;

public class Test_Binomialdev {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nbin,N=100000,M=20;
    doubleW df = new doubleW(0);
    doubleW chisq = new doubleW(0);
    doubleW prob = new doubleW(0);
    
    double pp;
    int fpr[]={12,10,9,8,9,13,5,10,9,7};
    int[] fingerprint =buildVector(fpr);
    double[] bins = new double[M],ebins = new double[M]; //  x=new double[M]
    boolean localflag, globalflag=false;
    
    

    // Test Binomialdev
    System.out.println("Testing Binomialdev");

    // Check fingerprint of doub()
    pp=0.5;
    Binomialdev myran = new Binomialdev(M,pp,17);
    localflag=false;
    for (i=0;i<10;i++)
//      System.out.printf(setw(25) << setprecision(20) << myran.dev());
      localflag=localflag || (myran.dev() != fingerprint[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdev: dev() does not match fingerprint");
      
    }

    // Check statistics
    Binomialdist expect = new Binomialdist(M,pp);
    for (i=0;i<M;i++) {
      ebins[i]=(N)*expect.p(i);
      bins[i]=0;
    }
    for (i=0;i<N;i++) {
      nbin=myran.dev();
      if ((nbin >= 0) && (nbin < M)) bins[nbin] += 1;
    }
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,dev(): %f\n  prob: %f\n",chisq.val, prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdev: dev() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
