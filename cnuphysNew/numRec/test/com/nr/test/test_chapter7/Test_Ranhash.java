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

import com.nr.ran.Ranhash;

public class Test_Ranhash {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,M=1024,N=100*M;   // M should divide evenly into 2^32
    doubleW dfW=new doubleW(0),chisqW=new doubleW(0),probW=new doubleW(0);
    double df,chisq,prob,average,sbeps=1.e-15;
    int fpr1[]={0x1fd00de3,
        0x71ba8e83,
        0xacc52358,
        0x7ed9f4d5,
        0x779d931b,
        0xd92b0544,
        0x63f3dbd3,
        0xed7fcc27,
        0xffc1d0da,
        0x9db21914};
    long fpr2[]={0x7b439d0c1fd00de3L,
        0xbea952a971ba8e83L,
        0x48eb9f74acc52358L,
        0x7451f8627ed9f4d5L,
        0x0beabdb3779d931bL,
        0xe2f08b10d92b0544L,
        0x8f56fd8d63f3dbd3L,
        0x33f9277eed7fcc27L,
        0x788581f6ffc1d0daL,
        0x075e47ec9db21914L};
    double fpr3[]={0.48150044961931843,0.74477116238424856,0.28484531973369492,
      0.45437576679874053,0.046550613703962448,0.88648289834342731,
      0.55992111875453021,0.20302054261037411,0.47078716545343657,
      0.028782363193109353};
    int[] fingerprint1 = buildVector(fpr1);
    long[] fingerprint2 = new long[10];System.arraycopy(fpr2, 0, fingerprint2, 0, 10);
    double[] fingerprint3 = buildVector(fpr3);
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Ranhash
    System.out.println("Testing Ranhash");
    average=N/(double)(M);
    double[] ebins = buildVector(M,average);

    // Check fingerprint of int32()
    localflag=false;
    Ranhash myran1 = new Ranhash();
    for (i=0;i<10;i++)
      localflag=localflag || (myran1.int32(i) != fingerprint1[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranhash: int32() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0.0;
    for (i=0;i<N;i++) bins[myran1.int32p(i+10000)%M] += 1.0;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);

    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranhash: int32() does not give distribution with correct variance");
      
    }

    // Check fingerprint of int64()
    localflag=false;
    Ranhash myran2 = new Ranhash();
    for (i=0;i<10;i++) 
      localflag=localflag || (myran2.int64(i) != fingerprint2[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranhash: int64() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(myran2.int64p(i)%M)] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranhash: int64() does not give distribution with correct variance");
      
    }

    // Check fingerprint of doub()
    Ranhash myran3 = new Ranhash();
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || abs(myran3.doub(i)-fingerprint3[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranhash: doub() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(floor(M*myran3.doub(i)))] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranhash: doub() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
