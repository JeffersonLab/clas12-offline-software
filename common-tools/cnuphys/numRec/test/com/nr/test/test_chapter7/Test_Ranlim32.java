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

import com.nr.ran.Ranlim32;

public class Test_Ranlim32 {

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
    int fpr1[]={1149019610,0xbd3972a4,869460487,0xb490bfc1,2051969833,
      1403042588,1716234809,0xd74d3f17,757991611,0x9c4032ed};
    double fpr2[]={0.26752697536721826,0.73915783409029245,0.20243704481981695,
      0.70533369504846632,0.47776145697571337,0.32667130883783102,
      0.399592055240646,0.84102243720553815,0.17648367467336357,
      0.61035459791310132};
    int[] fingerprint1 = buildVector(fpr1);
    double[] fingerprint2=buildVector(fpr2);
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Ranlim32
    System.out.println("Testing Ranlim32");
    average=N/(double)(M);
    double[] ebins = buildVector(M,average);

    // Check fingerprint of int32()
    localflag=false;
    Ranlim32 myran1 = new Ranlim32(17);
    for (i=0;i<10;i++){
      int r = myran1.int32();
      localflag=localflag || (r != fingerprint1[i]);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranlim32: int32() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[myran1.int32p()%M] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranlim32: int32() does not give distribution with correct variance");
      
    }

    // Check fingerprint of doub()
    Ranlim32 myran2 = new Ranlim32(17);
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || abs(myran2.doub()-fingerprint2[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranlim32: doub() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(floor(M*myran2.doub()))] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranlim32: doub() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");

  }

}
