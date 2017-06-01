package com.nr.test.test_chapter7;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Hash;
import com.nr.ran.Hashfn2;

public class Test_Hash {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=8;
    String names[]={"Charles Babbage","Marie Antoinette","Jane Austen",
      "Andrew Jackson","Ludwig van Beethoven","Samuel Morse",
      "John Quincy Adams","James Buchanan"};
    int dates[]={1791,1755,1775,1767,1770,1791,1767,1791};
    boolean localflag, globalflag=false;
    
    

    // Test Hash
    System.out.println("Testing Hash");

    Hash<String,Integer> year = new Hash<String,Integer>(100,100){
      Hashfn2 hashfn2 = new Hashfn2();
      public long fn(String k){
        return hashfn2.fn(k.getBytes());
      }
    };

    // Test operator[]
    for (i=0;i<N;i++) 
      year.set(names[i],dates[i]);
    localflag=false;
    for (i=0;i<N;i++)   // Read back in another order
      localflag = localflag || (year.get(names[(i+3)%N]) != dates[(i+3)%N]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Hash: Data retrieve from hash is incorrect");
      
    }

    // Test erase()
    for (i=0;i<N;i++) {
      localflag = year.erase(names[i]) != 1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Hash: Failure to erase a stored key");
        
      }
    }

    // Test count()
    for (i=0;i<N;i+=2)
      year.set(names[i],dates[i]);
    for (i=0;i<N;i++) {
      j=year.count(names[i]);
      localflag = (i%2==0 ? (j != 1) : (j != 0));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Hash: Incorrect identification of stored element count");
        
      }
    }

    // Test set() command;
    for (i=1;i<N;i+=2)          // Fill in the odd numbers
      year.set(names[i],dates[i]);
    for (i=0;i<N;i++) {
      localflag = year.count(names[i]) != 1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Hash: Failure of set() command");
        
      }
    }
    
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
