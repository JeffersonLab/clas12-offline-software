package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.chsone;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.HashAll;
public class Test_hashall {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,M=1024,N=100*M;
    doubleW df = new doubleW(0);
    doubleW chisq = new doubleW(0);
    doubleW prob = new doubleW(0);
    
    double average;
    int[] arr = new int[N];
    double[] bins = new double[M];
    boolean localflag, globalflag=false;
    
    

    // Test hashall
    System.out.println("Testing hashall");

    average=N/(double)(M);
    double[] ebins =buildVector(M,average);

    for (i=0;i<N;i++)
      arr[i]=i+1;
    HashAll.hashall(arr);
    for (i=0;i<N;i++)
      bins[(arr[i]&0x7FFFFFFF)%M] += 1.0; // XXX java do not have unsigned int.
    chsone(bins,ebins,df,chisq,prob);
    System.out.printf("     chisq,int32(): %f  prob: %f\n",chisq.val, prob.val);

    localflag = (prob.val < 0.05);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hashall: Test for randomness does not give a distribution with correct variance");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
