package com.nr.test.test_chapter15;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.model.Plog;
import com.nr.model.Proposal;
import com.nr.model.State;
import com.nr.ran.Gammadev;

public class Test_mcmc {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,j,k1=1,k2=2,N=1000;
    double lambda1=3.0,lambda2=2.0,t,tc=200.0,accept;
    double llam1,llam2,kk1,kk2,ttc;
    double lam1ave,lam2ave,tcave;
    double[] times=new double[N];
    boolean localflag, globalflag=false;

    

    // Test mcmc
    System.out.println("Testing mcmc");

    Gammadev gdev1 = new Gammadev(k1,lambda1,17);
    Gammadev gdev2 = new Gammadev(k2,lambda2,17);
    t=0.0;
    for (i=0;i<N;i++) {
      if (t < tc) {
        t += gdev1.dev();
      } else {
        t += gdev2.dev();
      }
      times[i]=t;
    }

    State s=new State(1.0,3.0,100.0,1,1);
    Plog plog = new Plog(times);
    Proposal propose = new Proposal(10102,0.01);
    for (i=0;i<1000;i++) {    // Burn-in
//      if (i%10 == 0) 
//        System.out.printf(s.lam1 << " %f\n", s.lam2 << " %f\n", s.k1 << " %f\n", s.k2 << " %f\n", s.tc);
      accept=Proposal.mcmcstep(1,s,plog,propose);
    }
    
    llam1=llam2=kk1=kk2=ttc=0.0;
    j=0;
    for (i=0;i<10000;i++) {   // Production
      accept = Proposal.mcmcstep(10,s,plog,propose);
      if (i%10 == 0) {
        llam1 += s.lam1;
        llam2 += s.lam2;
        kk1 = s.k1;
        kk2 = s.k2;
        ttc += s.tc;
        j++;
      }
    }
    lam1ave=llam1/j;
    lam2ave=llam2/j;
    tcave=ttc/j;

//    System.out.printf(lam1ave << " %f\n", lam2ave << " %f\n", kk1 << " %f\n", kk2 << " %f\n", tcave);

    localflag = abs(lam1ave-lambda1)/lambda1 > 0.05;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Model parameter lambda1 was not correctly determined");
      
    }

    localflag = abs(lam2ave-lambda2)/lambda2 > 0.05;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Model parameter lambda2 was not correctly determined");
      
    }

    localflag = kk1 != k1;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Model parameter k1 was not correctly determined");
      
    }

    localflag = kk2 != k2;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Model parameter k2 was not correctly determined");
      
    }
    
    localflag = abs(tcave-tc)/tc > 0.01;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Critical time tc was not accurately determined");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
