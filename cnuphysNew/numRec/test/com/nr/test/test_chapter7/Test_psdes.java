package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.chsone;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;
import org.netlib.util.intW;

import com.nr.ran.HashAll;
public class Test_psdes {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,M=1024,N=100*M;
    intW lword = new intW(0),rword = new intW(0);
    doubleW dfW=new doubleW(0),chisqW=new doubleW(0),probW=new doubleW(0);
    double df,chisq,prob,average;
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test psdes
    System.out.println("Testing psdes");

    average=N/(double)(M);
    double[] ebins = buildVector(M,average);

    for (i=0;i<N/2;i++) {
      lword.val=2*i;
      rword.val=2*i+1;
      HashAll.psdes(lword,rword);
      bins[(lword.val&0x7FFFFFFF)%M] += 1.0;
      bins[(rword.val&0x7FFFFFFF)%M] += 1.0;
    }
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);

    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** psdes: Test for randomness does not give a distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
