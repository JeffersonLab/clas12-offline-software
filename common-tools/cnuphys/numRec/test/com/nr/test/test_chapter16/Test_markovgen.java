package com.nr.test.test_chapter16;

import static com.nr.NRUtil.buildMatrix;
import static com.nr.test.NRTestUtil.matsub;
import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.HMM;

public class Test_markovgen {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=10000,M=5;
    double sbeps,aatrans[]={
      0.0,0.7,0.1,0.0,0.2,
      0.2,0.4,0.0,0.2,0.2,
      0.0,1.0,0.0,0.0,0.0,
      0.0,0.3,0.0,0.7,0.0,
      0.1,0.1,0.0,0.0,0.8
    };
    int[] state=new int[N];
    double[][] atrans=buildMatrix(M,M,aatrans);
    boolean localflag, globalflag=false;

    

    // Test markovgen
    System.out.println("Testing markovgen");

    HMM.markovgen(atrans,state,0,17);

//    for (i=0;i<10;i++) {
//      for (j=0;j<10;j++)
//        System.out.printf(state[10*i+j] << " ";
//      System.out.printf(endl;
//    }
//    System.out.printf(endl;

    // Measure the transition probabilities in the result
    int[][] measure=new int[M][M];
    int[] ntimes=new int[M];
    for (i=1;i<N;i++) {
      ntimes[state[i-1]]++;
      measure[state[i-1]][state[i]]++;
    }

    double[][] prob=new double[M][M];

    for (i=0;i<M;i++) {
      for (j=0;j<M;j++) {
        prob[i][j]=(double)(measure[i][j])/ntimes[i];
//        System.out.printf(setw(6) << prob[i][j] << " ";
      }
//      System.out.printf(endl;
    }
//    System.out.printf(endl;

    sbeps=0.03;
//    System.out.printf( maxel(matsub(atrans,prob)));
    localflag = maxel(matsub(atrans,prob)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** markovgen: Transistion probabilities don't agree with specification");
      
    }

    for (i=0;i<M;i++) {
      for (j=0;j<M;j++) {
        if (atrans[i][j] == 0.0) 
          localflag = (prob[i][j] != 0.0);
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** markovgen: Markov chain made a disallowed transition");
          
        }
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
