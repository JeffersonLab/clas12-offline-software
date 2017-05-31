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

import com.nr.ran.Ran;

public class Test_Ran {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1000000,M=1000;
    doubleW df = new doubleW(0);
    doubleW chisq = new doubleW(0);
    doubleW prob = new doubleW(0);
    
    double average,sbeps=1.e-15;
    
    // XXX use Hex for int and long
    int fpr1[]={0xbebb6128,0x5bfd7420,0x5e7120b7,0xec23959a,0x5edc440b,
        0xdca5f0e1,0x65a1ecc7,0xbc19f944,0xa8288b4f,0x62bc44e7};
    
    long fpr2[]={0x3bf1034bebb6128L,0x67c63fbd5bfd7420L,0xe223630c5e7120b7L,
        0xec79e71fec23959aL,0x44a057b65edc440bL,0x15b9738adca5f0e1L,
        0x6276870165a1ecc7L,0xc1477bb4bc19f944L,0x21583f8a8288b4fL,
        0x704faac462bc44e7L};
    
    double fpr3[]={0.014634144665917075,0.40536878941565196,0.88335246135688239,
      0.92373508958202277,0.26807163431554765,0.084861012842018871,
      0.38462108406168455,0.75499699747533799,0.0081408006737168533,
      0.43871562285015481};
    int[] fingerprint1 = buildVector(fpr1);
    long[] fingerprint2 = new long[10]; System.arraycopy(fpr2, 0, fingerprint2, 0, 10);
    double[] fingerprint3 = new double[10];System.arraycopy(fpr3, 0, fingerprint3, 0, 10);
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test Ran
    System.out.println("Testing Ran");
    average=(double)(N)/(M);
    double[] ebins = buildVector(M,average);

    // Check fingerprint of int32()
    Ran myran1 = new Ran(17);
    localflag=false;
    for (i=0;i<10;i++)
      localflag=localflag || (myran1.int32() != fingerprint1[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ran: int32() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<N;i++) bins[myran1.int32p() % M] += 1;
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,int32(): %f  prob: %f\n", chisq.val, prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ran: int32() does not give distribution with correct variance");
      
    }

    // Check fingerprint of int64()
    Ran myran2 = new Ran(17);
    localflag=false;
    for (i=0;i<10;i++) 
      localflag=localflag || (myran2.int64() != fingerprint2[i]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ran: int64() does not match fingerprint");
      
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(myran2.int64p()%M)] += 1;
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,int64(): %f  prob: %f\n",chisq.val, prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ran: int64() does not give distribution with correct variance");
      
    }

    // Check fingerprint of doub()
    Ran myran3 = new Ran(17);
    localflag=false;
    for (i=0;i<10;i++) {
      double r = myran3.doub();
      localflag=localflag || abs(r-fingerprint3[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ran: doub() does not match fingerprint");
    }

    // Check statistics
    for (i=0;i<M;i++) bins[i]=0;
    for (i=0;i<N;i++) bins[(int)(floor(M*myran3.doub()))] += 1;
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,doub(): %f\n  prob: %f\n", chisq.val, prob.val);
    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Ran: doub() does not give distribution with correct variance");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
