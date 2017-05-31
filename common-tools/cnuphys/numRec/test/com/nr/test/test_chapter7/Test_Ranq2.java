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

import com.nr.ran.Ranq2;

public class Test_Ranq2 {

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
    int fpr1[]={0xa905d3eb,
        0x538ab06e,
        0xf8b45cfe,
        0x0a52e3b1,
        0xdbf57011,
        0x51261693,
        0x6dc59258,
        0xf7ecf1de,
        0x23941c07,
        0x2eaad665};
    long fpr2[]={0xc8a35078a905d3ebL,
        0x7b3015c1538ab06eL,
        0x53c615b4f8b45cfeL,
        0x417c81e80a52e3b1L,
        0x07263f19dbf57011L,
        0x5facfeaa51261693L,
        0x70e37edb6dc59258L,
        0xeee64af1f7ecf1deL,
        0x8726783b23941c07L,
        0x3034c1fe2eaad665L};
    double fpr3[]={0.78374197908217202,0.48120246858483529,0.32724128406762659,
      0.25580608286902479,0.02792734509781325,0.37373344095353678,
      0.44097130639217702,0.93320148856576046,0.52793075031111458,
      0.18830501990911883};
    int[] fingerprint1=buildVector(fpr1);
    long[] fingerprint2 = new long[10];System.arraycopy(fpr2, 0, fingerprint2, 0, 10);
    double[] fingerprint3 = buildVector(fpr3);
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Ranq2
    System.out.println("Testing Ranq2");
    average=N/(double)(M);
    double[] ebins=buildVector(M,average);

    // Check fingerprint of int32()
    Ranq2 myran1 = new Ranq2(17);
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || (myran1.int32() != fingerprint1[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq2: int32() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<N;i++) bins[myran1.int32p()%M] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq2: int32() does not give distribution with correct variance");
      
    }

    // Check fingerprint of int64()
    Ranq2 myran2 = new Ranq2(17);
    localflag=false;
    for (i=0;i<10;i++) 
      localflag=localflag || (myran2.int64() != fingerprint2[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq2: int64() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(myran2.int64p()%M)] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq2: int64() does not give distribution with correct variance");
      
    }

    // Check fingerprint of doub()
    Ranq2 myran3 = new Ranq2(17);
    localflag=false;
    for (i=0;i<10;i++) 
      localflag=localflag || abs(myran3.doub()-fingerprint3[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq2: doub() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(floor(M*myran3.doub()))] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranq2: doub() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
