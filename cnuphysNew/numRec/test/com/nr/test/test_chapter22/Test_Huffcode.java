package com.nr.test.test_chapter22;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.intW;

import com.nr.lna.Huffcode;
import com.nr.ran.Ran;

public class Test_Huffcode {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double r;
    int i,N=1024,NCHAR=6;
    intW nb = new intW(0);
    int[] nfreq=new int[NCHAR];
    boolean localflag=false, globalflag=false;

    

    // Test Huffcode
    System.out.println("Testing Huffcode");

    // Create a random message in vowellish with character frequencies
    // as given in the book
    // a= 0.12  e=0.42  i=0.09  o=0.30  u=0.07 f=0.0(EOF marker)
    Ran myran=new Ran(17);
    byte[]c = new byte[N+1];
    for (i=0;i<N;i++) {
      r=myran.doub();
      if (r < 0.12) c[i]=0;
      else if (r < 0.54) c[i]=1;
      else if (r < 0.63) c[i]=2;
      else if (r < 0.93) c[i]=3;
      else c[i]=4;
    }
    c[N]=5;

    // Do an actual count of each char in the message
    for (i=0;i<N+1;i++) nfreq[c[i]]++;

    // Test codeone
    Huffcode huff=new Huffcode(NCHAR,nfreq);
    nb.val=0;
    byte[] code = new byte[N];
    for (i=0;i<N+1;i++) huff.codeone(c[i],code,nb);
    System.out.printf("Number of bits used: %d\n", nb.val);
    System.out.printf("Compression ratio: %f\n", (double)(N*3)/nb.val); // Normally 3 bits each for 5 characters

    // Recover the text
    nb.val=0;
    byte[]d=new byte[N+1];
    for (i=0;i<N+1;i++) {
      d[i]=(byte)huff.decodeone(code,nb);
      localflag = localflag || (c[i] != d[i]);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Huffcode: Recovered message not the same as the original");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
