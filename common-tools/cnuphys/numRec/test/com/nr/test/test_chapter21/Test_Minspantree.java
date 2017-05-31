package com.nr.test.test_chapter21;

import static com.nr.cg.Point.dist;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Minspantree;
import com.nr.cg.Point;
import com.nr.ran.Ran;


public class Test_Minspantree {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,min,N=1000;
    Point[] pvec=new Point[N],qvec=new Point[N];
    for(i=0;i<N;i++){
      pvec[i]= new Point(2);
      qvec[i]= new Point(2);
    }
    boolean localflag=false, globalflag=false;

    

    // Test Minspantree
    System.out.println("Testing Minspantree");

    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      pvec[i].x[0]=myran.doub();
      pvec[i].x[1]=myran.doub();
    }

    Minspantree minspan=new Minspantree(pvec);

      localflag = (minspan.nspan != N-1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Minspantree: Incorrect number of elements in tree");
      
    }

    // Check whether minspantree segments are in order of size
    for (i=0;i<minspan.nspan-1;i++) { 
      localflag = dist(pvec[minspan.minsega[i]],pvec[minspan.minsegb[i]])
        > dist(pvec[minspan.minsega[i+1]],pvec[minspan.minsegb[i+1]]); 
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Minspantree: Segments of tree are not in order of length");
        
      }
    }

    // Check whether every point is actually in the tree
    // (i.e. whether the combination of all minsega and minsegb
    // include all numbers from 1 to N
    int[] flags=new int[N];
    for (i=0;i<minspan.nspan;i++) {
      flags[minspan.minsega[i]]++;
      flags[minspan.minsegb[i]]++;
    }
    min=1;
    for (i=0;i<N;i++)
      if (flags[i] < min) min=flags[i];
    localflag = (min == 0);

    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Minspantree: One or more points do not appear in the tree");
      
    }

    // Put same points in a reversed order
    for (i=0;i<N;i++) {
      qvec[N-1-i].x[0]=pvec[i].x[0];
      qvec[N-1-i].x[1]=pvec[i].x[1];
    }
    
    Minspantree minspan2=new Minspantree(qvec);

    localflag = (minspan2.nspan != N-1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Minspantree: Incorrect number of elements in second tree");
      
    }

    // Check whether minspantree segments are in order of size
    for (i=0;i<minspan2.nspan-1;i++) { 
      localflag = dist(qvec[minspan2.minsega[i]],qvec[minspan2.minsegb[i]])
        > dist(qvec[minspan2.minsega[i+1]],qvec[minspan2.minsegb[i+1]]); 
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Minspantree: Segments of second tree are not in order of length");
        
      }
    }

    // Check whether every point is actually in the tree
    // (i.e. whether the combination of all minsega and minsegb
    // include all numbers from 1 to N
    int[] flags2=new int[N];
    for (i=0;i<minspan2.nspan;i++) {
      flags2[minspan2.minsega[i]]++;
      flags2[minspan2.minsegb[i]]++;
    }
    min=1;
    for (i=0;i<N;i++)
      if (flags2[i] < min) min=flags2[i];
    localflag = (min == 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Minspantree: One or more points do not appear in the second tree");
      
    }

    // Check whether the minsega and minsegb indices are related
    // by  i => N-i for the two trees (meaning that the two trees are
    // really the same). Note: minsega and minsegb also get swapped.
    for (i=0;i<minspan.nspan;i++) {
      localflag = (minspan.minsega[i] != N-1-minspan2.minsegb[i]);
      localflag = localflag || (minspan.minsegb[i] != N-1-minspan2.minsega[i]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Minspantree: Reversed list of points did not lead to same tree");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
