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

import com.nr.ran.Ranbyte;
public class Test_Ranbyte {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,N=100000,M=256;
    doubleW dfW=new doubleW(0),chisqW=new doubleW(0),probW=new doubleW(0);
    double df,chisq,prob,average,sbeps=1.e-15;
    int fpr1[]={248,118,10,200,182,254,69,191,65,220};
    int fpr2[]={0xf8760ac8,0xb6fe45bf,1104942861,46198748,0xb2610f18,
        0xc2d27d31,0x9d7091d6,0xf192ea75,0xdf166388,0xa0538d44};
    double fpr3[]={0.97055117988838291,0.25726455752988264,0.6967935023738594,
      0.61499892709396031,0.87143537602999133,0.77732089918244707,
      0.91599456154011516,0.86250678060214292,0.55851281050105261,
      0.51802028753147611};
    int[] fingerprint1 = buildVector(fpr1);
    int[] fingerprint2 = buildVector(fpr2);
    double[] fingerprint3 = buildVector(fpr3);
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Ranbyte
    System.out.println("Testing Ranbyte");
    average=N/(double)(M);
    double[] ebins = buildVector(M,average);

    // Check fingerprint of int8()
    localflag=false;
    Ranbyte myran1 = new Ranbyte(17);
    for (i=0;i<10;i++)
  //System.out.printf(int(myran1.int8()));
      localflag=localflag || (myran1.int8() != fingerprint1[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranbyte: int8() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<N;i++) bins[myran1.int8()%M] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int8(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranbyte: int8() does not give distribution with correct variance");
      
    }

    // Check fingerprint of int32()
    localflag=false;
    Ranbyte myran2 = new Ranbyte(17);
    for (i=0;i<10;i++) 
      localflag=localflag || (myran2.int32() != fingerprint2[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranbyte: int32() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[myran2.int32p()%M] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranbyte: int32() does not give distribution with correct variance");
      
    }

    // Check fingerprint of doub()
    Ranbyte myran3 = new Ranbyte(17);
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || abs(myran3.doub()-fingerprint3[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranbyte: doub() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(floor(M*myran3.doub()))] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,doub(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranbyte: doub() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
