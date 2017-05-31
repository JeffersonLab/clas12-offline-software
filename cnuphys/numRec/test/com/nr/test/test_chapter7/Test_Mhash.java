package com.nr.test.test_chapter7;

import static com.nr.NRUtil.buildVector;
import static org.junit.Assert.fail;
import static com.nr.test.NRTestUtil.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Hashfn2;
import com.nr.ran.Mhash;

public class Test_Mhash {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,count,N=8;
    String[] str = new String[1];
    String names[]={"Charles Babbage","Marie Antoinette","Jane Austen",
      "Andrew Jackson","Ludwig van Beethoven","Samuel Morse",
      "John Quincy Adams","James Buchanan"};
    int dates[]={1791,1755,1775,1767,1770,1791,1767,1791};
    boolean localflag, globalflag=false;
    
    

    // Test Mhash
    System.out.println("Testing Mhash");

    //Mhash<int,string,Hashfn2> person(100,100);
    Mhash<Integer,String> person = new Mhash<Integer,String>(100,100){
      Hashfn2 hashfn2 = new Hashfn2();
      public long fn(Integer k){
        int kk = k;
        byte[] b = new byte[4];
        b[0] = (byte)(kk >>>24);
        b[1] = (byte)(kk >>>16);
        b[2] = (byte)(kk >>>8);
        b[3] = (byte)kk;
        
        return hashfn2.fn(b);
      }
    };

    for (i=0;i<N;i++) person.store(dates[i],names[i]);

    count=0;
    for (i=1750;i<1800;i++)
      if (person.getinit(i)!=0)
        while (person.getnext(str,0)!=0) count++;
    localflag = count != 8;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Mhash: Incorrect total number of items recovered in Mhash");
      
    }

    // Test count() method
    count=0;
    i=1791;
    person.getinit(i);
    while(person.getnext(str,0)!=0) count++;
    localflag = person.count(i) != count;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Mhash: Incorrect number of items for a key recovered in Mhash");
      
    }

    // Test erase();
    i=1791;
    count=person.count(i);
    person.erase(i,"Charles Babbage");
    person.erase(i,"Samuel Morse");
    int ccount = person.count(i);
    localflag = ccount != count-2;
    globalflag = globalflag || localflag;
    if (localflag) {
      //fail("*** Mhash: Incorrect number remaining after two erasures");
      
    }

    // Inspect remaining entry for 1791
    i=1791;
    person.getinit(i);
    person.getnext(str,0);
//    System.out.printf(str.data());
    localflag = !str[0].equals("James Buchanan");
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Mhash: Incorrect entry remains in hash after erasures");
      
    }

    // Add same person multiple times
    person.store(i,"James Buchanan");
    person.store(i,"James Buchanan");
//    if (person.getinit(i))
//      while (person.getnext(str))
//        System.out.printf(str.data());
    // Then erase and replace with others
    person.erase(i,"James Buchanan");
    person.store(i,"Charles Babbage");
    person.erase(i,"James Buchanan");
    person.store(i,"Samuel Morse");
//    if (person.getinit(i))
//      while (person.getnext(str))
//        System.out.printf(str.data());
    
    // Test whether original list has been restored with no side effects
    int[] check = buildVector(N,0);
    int[] expect = buildVector(N,1);
    for (i=1750;i<1800;i++) {
      if (person.getinit(i)!=0) {
        while (person.getnext(str,0)!=0)
          for (j=0;j<N;j++)
            if (names[j].equals(str[0])) check[j]=1;
      }
    }
    localflag = maxel(vecsub(check,expect))!=0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Mhash: Contents of multimap was not successfuly restored");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
