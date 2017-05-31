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

import com.nr.ran.Ranq1;

public class Test_Ranq1 {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,N=100000,M=1000;
    doubleW dfW=new doubleW(0),chisqW=new doubleW(0),probW=new doubleW(0);
    double df,chisq,prob,average,sbeps=1.e-15;
    int fpr1[]={0xc9be937b,1097965987,611133294,0xf285a70f,0x85e49a7b,
      259469246,0xc02f890c,0xc5e63800,0xba316dd9,0xbbbc53a2};
    long fpr2[]={0x6ea5b5b4c9be937bL,
        0xc4d54ebd4171a1a3L,
        0x539c413e246d276eL,
        0x48ece077f285a70fL,
        0xc3145ec285e49a7bL,
        0x6b0587ce0f772fbeL,
        0xac379603c02f890cL,
        0x649cbe7cc5e63800L,
        0x2f9ac8dba316dd9L,
        0x5e4a8f9dbbbc53a2L};
    double fpr3[]={0.43221603072901821,0.76887981529711746,0.32660300986428992,
      0.28486445358659884,0.76202957390877868,0.41805313854953563,
      0.67272317496843759,0.39301672502724549,0.011622223473926245,
      0.36832521057998352};
    int[] fingerprint1 = buildVector(fpr1);
    long[] fingerprint2 = new long[10];System.arraycopy(fpr2, 0, fingerprint2, 0, 10);
    double[] fingerprint3 = buildVector(fpr3);
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
      
    

    // Test Ranq1
    System.out.println("Testing Ranq1");
    average=N/(double)(M);
    double[] ebins = buildVector(M,average);

    // Check fingerprint of int32()
    Ranq1 myran1 = new Ranq1(17);
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || (myran1.int32() != fingerprint1[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq1: int32() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<N;i++) bins[myran1.int32p()%M] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq1: int32() does not give distribution with correct variance");
      
    }

    // Check fingerprint of int64()
    Ranq1 myran2 = new Ranq1(17);
    localflag=false;
    for (i=0;i<10;i++) 
      localflag=localflag || (myran2.int64() != fingerprint2[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq1: int64() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(myran2.int64p()%M)] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int64(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq1: int64() does not give distribution with correct variance");
      
    }

    // Check fingerprint of doub()
    Ranq1 myran3 = new Ranq1(17);
    localflag=false;
    for (i=0;i<10;i++) 
      localflag=localflag || abs(myran3.doub()-fingerprint3[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq1: doub() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(floor(M*myran3.doub()))] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,doub(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq1: doub() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
