package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Hashfn2;
import com.nr.ran.Hashtable;
import com.nr.ran.Ran;

public class Test_Hashtable {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,ia,ib,N=10;
    int atnum[]={1,2,3,4,5,6,7,8,9,10};
    int atmass[]={1,4,7,9,11,12,14,16,19,20};
    String[]atname ={"hydrogen","helium","lithium",
      "beryllium","boron","carbon","nitrogen","oxygen",
      "fluorine","neon"};
    boolean localflag, globalflag=false;
    
    

    // Test prog
    System.out.println("Testing Hashtable");

    atom[] chart=new atom[N];

    for (i=0;i<10;i++) {
      chart[i] = new atom();
      chart[i].atomicnumber=atnum[i];
      chart[i].atomicmass=atmass[i];
      chart[i].name=atname[i];
    }

    Hashtable<atom> hash = new Hashtable<atom>(N,2*N){
      Hashfn2 hashfn2 = new Hashfn2();
      public long fn(atom k) {
        
        return hashfn2.fn(k.toByte());
      }
    };
    
    // Test simple iset(), iget()
    i=hash.iset(chart[5]);
//    System.out.printf(i);
    localflag = (i != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: First iset() did not return index 0");
      
    }

    i=hash.iget(chart[5]);
//    System.out.printf(i);
    localflag = (i != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: iget() did not return correct index");
      
    }

    // Test simple ierase()
    i=hash.ierase(chart[5]);
    localflag = (i != 0);
//    System.out.printf(i);
    localflag = (i != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: ierase() did not return correct index");
      
    }

    i=hash.iget(chart[5]);
//    System.out.printf(i);
    localflag = (i != -1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: iget() from erased key should return -1");
      
    }

    // Test garbage collector
    i=hash.iset(chart[5]);    // Return hashes for 3 keys
//    System.out.printf(i);
    i=hash.iset(chart[2]);
//    System.out.printf(i);
    i=hash.iset(chart[7]);
//    System.out.printf(i);
    ia=hash.ierase(chart[2]); // erase two keys
//    System.out.printf(i);
    ib=hash.ierase(chart[7]);
//    System.out.printf(i);

    // Following two iset()'s should use indices just erased
    i=hash.iset(chart[1]);    // store two more keys
//    System.out.printf(i);
    localflag = (i != ib);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: iset() should use hash of last erased key");
      
    }

    i=hash.iset(chart[9]);
//    System.out.printf(i);
    localflag = (i != ia);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: iset() should use hash of penultimate erased key");
      
    }

    // Test multiple iset() on same keys. They should return same index.
    Ran myran = new Ran(17);
    int j;
    for (i=0;i<100;i++) {
      j=myran.int32p()%N;
      hash.iset(chart[j]);
    }

    // N keys should always use only the first N indices
    int[] expect =buildVector(N,1);
    int[] check = buildVector(N,0);
    for (i=0;i<N;i++) {
      j=hash.iget(chart[i]);
//      System.out.printf(j << " ";
      check[j]=1;
      hash.ierase(chart[i]);  // Clear away the hashes
    }
//    System.out.printf(endl;
    localflag = maxel(vecsub(check,expect)) != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: N stored keys should have used the first N indices");
      
    }

    // Test of ireserve()
    for (i=0;i<N;i++) {     // Reserve the first N indices
      j=hash.ireserve();
//      System.out.printf(j << " ";
    }
//    System.out.printf(endl;

    for (i=0;i<100;i++) {   // Now store N keys again
      j=myran.int32p()%N;
      hash.iset(chart[j]);
    }

    int[] expect2 = buildVector(2*N,1);
    for (i=0;i<N;i++) expect2[i]=0;
    int[] check2 = buildVector(2*N,0);
    for (i=0;i<N;i++) {
      j=hash.iget(chart[i]);
//      System.out.printf(j << " ";
      check2[j]=1;
      hash.ierase(chart[i]);
    }
//    System.out.printf(endl;
    localflag = maxel(vecsub(check2,expect2)) != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: N stored keys should have used the second N indices");
      
    }

    // Test of irelinquish()
    j=0;
    for (i=0;i<2*N;i++)
      j += hash.irelinquish(i);       // final value of j should be (N/2)(N-3)
    localflag = (j != (N/2)*(N-3));       // for N=10, j=5*7=35;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: Sum of return values of irelinquish() was not correct");
      
    }

    // Now run original test on fully relinquished table
    for (i=0;i<100;i++) {
      j=myran.int32p()%N;
      hash.iset(chart[j]);
    }

    for (i=0;i<2*N;i++) {
      check2[i]=0;
      if (i<N) expect2[i]=1;
      else expect2[i]=0;
    }
    for (i=0;i<N;i++) {
      j=hash.iget(chart[i]);
//      System.out.printf(j << " ";
      check2[j]=1;
      hash.ierase(chart[i]);
    }
//    System.out.printf(endl;
    localflag = maxel(vecsub(check2,expect2)) != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hashtable: N stored keys should have used the first N keys");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");

  }
  
  class atom {
    int atomicnumber;
    int atomicmass;
    String name;
    
    public byte[] toByte() {
      byte[] by = name.getBytes();
      byte[] buf = new byte[8+by.length];
      ByteBuffer bb = ByteBuffer.wrap(buf);
      bb.putInt(atomicnumber);
      bb.putInt(atomicmass);
      bb.put(by);
      return buf;
    }
  }
}
