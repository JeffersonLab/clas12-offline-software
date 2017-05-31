package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Ranfib;
import static com.nr.stat.Stattests.chsone;

public class Test_Ranfib {

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
    // use long instead.
    long fpr1[]={1570093361L,531812823L,3036182742L,622162844L,2692057818L,3344863307L,
        185290093L,2977926809L,1052440751L,705435735L};
    double fpr2[]={0.40862881198581419,0.68328679209185939,0.22230878805278581,
      0.16642529919524651,0.42011530915609308,0.42282311281081153,
      0.35131858982938047,0.94397175873012928,0.95567043897935067,
      0.88926845303637059};
    long[] fingerprint1 = new long[10];System.arraycopy(fpr1, 0, fingerprint1, 0, 10);
    double[] fingerprint2 = buildVector(fpr2);
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Ranfib
    System.out.println("Testing Ranfib");
    average=N/(double)(M);
    double[] ebins = buildVector(M,average);

    // Check fingerprint of int32()
    localflag=false;
    Ranfib myran1 = new Ranfib(171);
    for (i=0;i<10;i++) {
      localflag=(myran1.int32() != fingerprint1[i]) || localflag;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranfib: int32() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[myran1.int32p()%M] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranfib: int32() does not give distribution with correct variance");
      
    }

    // Check fingerprint of doub()
    Ranfib myran2 = new Ranfib(17);
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || abs(myran2.doub()-fingerprint2[i])>sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranfib: doub() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(floor(M*myran2.doub()))] += 1;
    chsone(bins,ebins,dfW,chisqW,probW);df = dfW.val; chisq = chisqW.val; prob = probW.val;
    System.out.printf("     chisq,doub(): %f  prob: %f\n",chisq, prob);
    localflag = (prob < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ranfib: doub() does not give distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
