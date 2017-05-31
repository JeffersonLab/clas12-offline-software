package com.nr.test.test_chapter22;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.intW;

import com.nr.lna.Arithcode;
import com.nr.ran.Ran;

public class Test_Arithcode {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double r;
    int i,N=1024,NCHAR=5;
    intW lcd = new intW(0);
    int[] nfreq=new int[NCHAR];
    boolean localflag=false, globalflag=false;

    

    // Test Arithcode
    System.out.println("Testing Arithcode");

    // Create a random message in vowellish with character frequencies
    // as given in the book
    // a= 0.12  e=0.42  i=0.09  o=0.30  u=0.07 f=0.0(EOF marker)
    Ran myran=new Ran(17);
    byte[] c = new byte[N+1];
    for (i=0;i<N;i++) {
      r=myran.doub();
      if (r < 0.12) c[i]=0;
      else if (r < 0.54) c[i]=1;
      else if (r < 0.63) c[i]=2;
      else if (r < 0.93) c[i]=3;
      else c[i]=4;
    }

    // Do an actual count of each char in the message
    for (i=0;i<N;i++) nfreq[c[i]]++;
//    for (i=0;i<NCHAR;i++) System.out.printf(nfreq[i] << "  ";
//    System.out.printf(endl;

    // Test codeone
    Arithcode arith = new Arithcode (nfreq,NCHAR,256);
    arith.messageinit();
    lcd.val=0;
    byte[] code = new byte[N];
    for (i=0;i<N;i++) arith.codeone(c[i],code,lcd);
    arith.codeone(NCHAR,code,lcd);    // EOM character
//    System.out.println("Number of bytes used: %f\n", lcd);
//    System.out.println("Compression ratio: %f\n", double(N*3)/8/lcd); // Normally 3 bits each for 5 characters

    // Recover the text
    arith.messageinit();
    lcd.val=0;
    char[] d=new char[N];
    i=0;
    while ((d[i]=(char)arith.decodeone(code,lcd)) != NCHAR) {
      System.out.printf("%x  %x\n", (int)c[i] ,(int)d[i]);
      localflag = localflag || (c[i] != d[i]);
      i++;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Arithcode: Recovered message not the same as the original");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
