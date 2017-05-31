package com.nr.test.test_chapter22;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.lna.Gray;
import com.nr.ran.Ran;

public class Test_Gray {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,m,p,N=1 << 20;
    boolean localflag=false, globalflag=false;

    

    // Test Gray
    System.out.println("Testing Gray");

    Ran myran = new Ran(17);
    Gray g=new Gray();
    for (i=0;i<N;i++) {
      j=myran.int32();
      k=g.gray(j);
      m=g.gray(j+1);
      p = m ^ k;    // p should be a power of 2 (i.e. 1 bit set)
//      System.out.printf(k << " %f\n", m << " %f\n", p);
      localflag = localflag || (p & (p-1)) != 0;  // test for a power of 2
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gray: Gray code gave a value that differed from argument in more than 1 bit position");
      
    }

    // Test invgray
    System.out.println("Testing invgray");
    for (i=0;i<N;i++) {
      j=myran.int32();
      k=g.gray(j);
      m=g.invgray(k);
//      System.out.printf(j << " %f\n", m);
      localflag = localflag || (m != j);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gray: Round trip test of gray() and invgray() did not lead back to origial integer");
      
    }

    // See if all values are being used
    int[] map=new int[8192];
    for (i=0;i<8192;i++) map[g.gray(i)]=1;
    localflag=false;
    for (i=0;i<8192;i++)
      localflag = localflag || map[i] != 1;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gray: Gray code had not used all binary bit patterns");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
