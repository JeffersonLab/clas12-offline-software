package com.nr.test.test_chapter17;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ode.Stochsim;
public class Test_Stochsim {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,NVAR=4,M=420;
    double t;
    double[] sinit=new double[NVAR],tsav=new double[M/10],smax=new double[NVAR];
    double[][] ssav=new double[M/10][NVAR];
    boolean localflag, globalflag=false;

    

    // Test Stochsim
    System.out.println("Testing Stochsim");

    sinit[0]=150.0;
    sinit[1]=10.0;
    sinit[2]=10.0;
    sinit[3]=0.0;
    Stochsim stoch = new Stochsim(sinit);

    for (k=0;k<NVAR;k++) smax[k]=-1;
    for (i=0;i<M;i++) {
      t=stoch.step();
      for (k=0;k<NVAR;k++)
        if (stoch.s[k] > smax[k]) smax[k]=stoch.s[k];
      if (i % 10 == 0) {
        j=i/10;
        tsav[j]=t;
//        System.out.printf(tsav[j];
        for (k=0;k<NVAR;k++) {
          ssav[j][k]=stoch.s[k];
//          System.out.println(" %f\n", ssav[j][k];
        }
//        System.out.printf(endl;
      }
    }

    // Test steady state
    localflag = stoch.s[0] != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected final number of species 0");
      
    }

    localflag = stoch.s[1] != 37;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected final number of species 1");
      
    }

    localflag = stoch.s[2] != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected final number of species 2");
      
    }

    localflag = stoch.s[3] != 133;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected final number of species 3");
      
    }

    // Test population maxima
    localflag = smax[0] != 149;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected maximum number of species 0");
      
    }

    localflag = smax[1] != 37;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected maximum number of species 1");
      
    }

    localflag = smax[2] != 29;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected maximum number of species 2");
      
    }

    localflag = smax[3] != 133;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Stochsim: Unexpected maximum number of species 3");
      
    }
    
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
