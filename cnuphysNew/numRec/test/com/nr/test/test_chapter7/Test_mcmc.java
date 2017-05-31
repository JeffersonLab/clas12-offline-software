package com.nr.test.test_chapter7;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.model.Plog;
import com.nr.model.Proposal;
import com.nr.model.State;
import com.nr.ran.Poissondev;

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
    int i,j,k,k1=1,k2=2,N=1000,M=100;
    double accept,lambda1=3.0,lambda2=2.0,tc=200.0;
    double[] times = new double[1000];
    boolean localflag, globalflag=false;
    
    

    // Test mcmc
    System.out.println("Testing mcmc");

    // Generate data. Break each time interval into 100 parts
    Poissondev pdev1 = new Poissondev(lambda1/M,17);
    Poissondev pdev2 = new Poissondev(lambda2/M,17);
    j=0;
    for (i=0;i<(int)(lambda1*tc/k1);i++) {
      for (k=0;k<k1;k++) {
        while (pdev1.dev() == 0) j++;
        j++;
      }
      times[i] = j*1.0/M;
    }
    for (i=(int)(lambda1*tc/k1);i<N;i++) {
      for (k=0;k<k2;k++) {
        while (pdev2.dev() == 0) j++;
        j++;
      }
      times[i] = j*1.0/M;
    }
    
    State s = new State(1.0,3.0,100.0,1,1);
    Plog plog = new Plog(times);
    Proposal propose = new Proposal(17,0.01);
    for (i=0;i<1000;i++) accept=Proposal.mcmcstep(1,s,plog,propose);
    for (i=0;i<10000;i++)
      accept=Proposal.mcmcstep(10,s,plog,propose);
//    System.out.printf(s.lam1 << " %f\n", s.lam2 << " %f\n", s.k1 << " %f\n", s.k2 << " %f\n", s.tc);

    localflag = abs(s.lam1-lambda1)/lambda1 > 0.1;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Inaccurate estimate for rate lambda1");
      
    }

    localflag = abs(s.lam2-lambda2)/lambda2 > 0.1;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Inaccurate estimate for rate lambda2");
      
    }

    localflag = (s.k1 != k1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: integer parameter k1 was not correctly determined");
      
    }

    localflag = (s.k2 != k2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: integer parameter k2 was not correctly determined");
      
    }

    localflag = abs(s.tc-tc)/tc > 0.1;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mcmc: Inaccurate estimate for critical time tc");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
