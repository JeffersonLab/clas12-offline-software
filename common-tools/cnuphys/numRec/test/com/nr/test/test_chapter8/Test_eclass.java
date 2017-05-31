package com.nr.test.test_chapter8;

import static com.nr.NRUtil.buildVector;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sort.EClass;

public class Test_eclass {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int N=15;
    int la[]={0,4,4,1,5,1,6,10,2,3,11};
    int lb[]={4,8,12,5,9,13,2,6,14,7,3};
    int i,j,nclass,ntotal;
    boolean[] nflag = new boolean[N];
    int[] nf = new int[N],lista = buildVector(la),listb=buildVector(lb);
    boolean localflag, globalflag=false;

    

    // Test eclass
    System.out.println("Testing eclass");
    EClass.eclass(nf,lista,listb);
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
      fail("*** eclass: Identified classes did not have uniform value mod-4");
      
    }

    ntotal=0;   // Total number of classes
    for (i=0;i<N;i++) 
      if (nflag[i]) ntotal++;
    localflag = (ntotal != 4);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** eclass: Number of identified classes is not correct");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
