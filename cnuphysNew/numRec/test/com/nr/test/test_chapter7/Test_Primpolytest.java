package com.nr.test.test_chapter7;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Primpolytest;
import com.nr.ran.Ran;

public class Test_Primpolytest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nflag,N=11;
    long mask;
    int pp[]={2046052277,1186898897,221421833,55334070,1225518245,
      216563424,1532859853,1735381519,2049267032,1363072601,
      130420448};
    // double sbeps=5.e-15; not used
    boolean localflag=false, globalflag=false;
    
    

    // Test Primpolytest
    System.out.println("Testing Primpolytest");

    Primpolytest ptest = new Primpolytest();
    for (i=0;i<N;i++) {
      nflag=ptest.test(pp[i]);
      localflag = localflag || nflag==0;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Primpolytest: A known primitive polynomial was not identified as such.");
      
    }

    Ran myran = new Ran(17);
    mask = ((((long)1 << 32) - 1) >> 1);
    for (i=0;i<N;i++) {
      nflag=ptest.test((long)(myran.int32()) & mask);
      localflag = localflag || nflag!=0;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Primpolytest: A randomly generated 31 bit integer tested positive (unlikely).");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
