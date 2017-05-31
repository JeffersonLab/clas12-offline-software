package com.nr.test.test_chapter16;

import static com.nr.NRUtil.buildMatrix;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.HMM;
import com.nr.ran.Ran;

public class Test_HMM {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=1000,M=5,K=5;
    double r,sum=0;
    double aatrans[]={      // Transition matrix
      0.0,0.7,0.1,0.0,0.2,
      0.2,0.4,0.0,0.2,0.2,
      0.0,1.0,0.0,0.0,0.0,
      0.0,0.3,0.0,0.7,0.0,
      0.1,0.1,0.0,0.0,0.8
    };
    double bb[]={         // Symbol probabilities for each state
      0.2,0.0,0.0,0.8,0.0,
      0.2,0.0,0.6,0.2,0.0,
      0.0,1.0,0.0,0.0,0.0,
      0.3,0.2,0.4,0.1,0.0,
      0.5,0.0,0.0,0.0,0.5
    };
    int[] state= new int[N],symbols= new int[N];
    double[][] atrans=buildMatrix(M,M,aatrans),b=buildMatrix(M,K,bb);
    boolean localflag, globalflag=false;

    

    // Test HMM
    System.out.println("Testing HMM");

    // Generate the Markov sequence of states
    HMM.markovgen(atrans,state,0,17);

    // Generate the sequence of symbols emitted
    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      r=myran.doub();
      sum=0.0;
      for (j=0;j<K;j++) {
        sum += b[state[i]][j];
        if (r < sum) {
          symbols[i]=j;
          break;
        }
      }
    }

    // Try to discover the model, given the symbols
    HMM hmm=new HMM(atrans,b,symbols);
    hmm.forwardbackward();

    // Inspect results
    int jmax=0,ncorrect=0;
    double test;
    for (i=0;i<N;i++) {
      test=0;
      for (j=0;j<M;j++) {
        if (hmm.pstate[i][j] > test) {
          test=hmm.pstate[i][j];
          jmax=j;
        }
      }
//      System.out.printf(state[i] << " %f\n", jmax);
      if (jmax == state[i]) ncorrect++;
    }
    System.out.printf("Fraction correct: %f\n", (double)(ncorrect)/N);
    localflag = (double)(ncorrect)/N < 0.75;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** HMM: Actual state was not the top probability more than 25% of the time");
      
    }

    // Inspect reconstructed transition matrix
    int jpen=0;
    ncorrect=0;
    for (i=0;i<N;i++) {
      test=0.0;
      for (j=0;j<M;j++) {
        if (hmm.pstate[i][j] > test) {
          test=hmm.pstate[i][j];
          jmax=j;
        }
      }

      // Find second largest
      test=0.0;
      for (j=0;j<M;j++) {
        if (j != jmax) {
          if (hmm.pstate[i][j] > test) {
            test=hmm.pstate[i][j];
            jpen=j;
          }
        }
      }
    
//      System.out.printf(state[i] << " %f\n", jmax);
      if (jmax == state[i] || jpen == state[i]) ncorrect++;
    }
    System.out.printf("Fraction correct: %f\n", (double)(ncorrect)/N);
    double beforeBW=(double)(ncorrect)/N;
    localflag = (double)(ncorrect)/N < 0.95;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** HMM: Actual state was not in top 2 probabilities more than 5% of the time");
      
    }
    
//    System.out.println("Log-likelihood: %f\n", hmm.loglikelihood());

    // Test Baum-Welch reestimation
    for (i=0;i<100;i++) {
      hmm.baumwelch();
      hmm.forwardbackward();
//      System.out.println("Log-likelihood: %f\n", hmm.loglikelihood());
    }

    // Inspect reconstructed transition matrix
    ncorrect=0;
    for (i=0;i<N;i++) {
      test=0.0;
      for (j=0;j<M;j++) {
        if (hmm.pstate[i][j] > test) {
          test=hmm.pstate[i][j];
          jmax=j;
        }
      }

      // Find second largest
      test=0.0;
      for (j=0;j<M;j++) {
        if (j != jmax) {
          if (hmm.pstate[i][j] > test) {
            test=hmm.pstate[i][j];
            jpen=j;
          }
        }
      }
    
//      System.out.printf(state[i] << " %f\n", jmax);
      if (jmax == state[i] || jpen == state[i]) ncorrect++;
    }
    System.out.printf("Fraction correct after Baum-Welch: %f\n", (double)(ncorrect)/N);
    double afterBW=(double)(ncorrect)/N;
    localflag = afterBW <= beforeBW;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** HMM: Baum-Welch reestimation did not improve model");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
