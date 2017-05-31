package com.nr.test.test_chapter8;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sort.EClass;
import com.nr.sort.EquivalenceInf;

public class Test_eclazz {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,nclass,ntotal,N=35;
    boolean[] nflag = new boolean[N];
    int[] nf = new int[N];
    boolean localflag, globalflag=false;

    

    // Test eclazz
    System.out.println("Testing eclazz");
    Equiv equiv = new Equiv();
    EClass.eclazz(nf,equiv);
    for (i=0;i<N;i++) nflag[i]=false;
    localflag=false;
    for (i=0;i<N;i++) {
      nclass=nf[i];
      if (!nflag[nclass]) {
        nflag[nclass]=true;
        for (j=i+1;j<N;j++)
          if (nf[j] == nclass)
            localflag = localflag || (nf[j]%4 != nclass%4);
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** eclazz: Identified classes did not have uniform value mod-4");
      
    }

    ntotal=0;   // Total number of classes
    for (i=0;i<N;i++) 
      if (nflag[i]) ntotal++;
    localflag = (ntotal != 4);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** eclazz: Number of identified classes is not correct");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  class Equiv implements EquivalenceInf {
    // Define equivalence as uniform value mod-4
    public boolean equiv(final int i, final int j) {
      return (i % 4) == (j % 4);
    }
  }
}
