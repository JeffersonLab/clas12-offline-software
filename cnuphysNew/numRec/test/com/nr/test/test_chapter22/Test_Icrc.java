package com.nr.test.test_chapter22;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.nr.ran.*;

import com.nr.lna.*;

public class Test_Icrc {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws Exception {
    int i,j;
    int result;
    String str="A quick brown fox jumped over the lazy dog.";
    
    byte[] bb = str.getBytes();
    int nn=bb.length;
    byte[] cc = new byte[bb.length+2];
    System.arraycopy(bb, 0, cc, 0, bb.length);
    boolean localflag=false, globalflag=false;

    

    // Test Icrc
    System.out.println("Testing Icrc");

    // Test generator 0 with fill=false
    Icrc crc00=new Icrc(0,false);
    result=crc00.crc(str.getBytes());
    System.out.println(result);
    cc[nn]=(byte)crc00.hibyte(result);
    cc[nn+1]=(byte)crc00.lobyte(result);
    result=crc00.crc(cc);
    System.out.println(result);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc00 gave a non-zero CRC");
      
    }

    // Test generator 1 with fill=false
    Icrc crc10=new Icrc(1,false);
    result=crc10.crc(str.getBytes());
//    System.out.printf(result);
    // Append result onto str and look for zero CRC
    cc[nn]=(byte)crc10.hibyte(result);
    cc[nn+1]=(byte)crc10.lobyte(result);
    
    result=crc10.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc10 gave a non-zero CRC");
      
    }

    // Test generator 2 with fill=false
    Icrc crc20=new Icrc(2,false);
    result=crc20.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc20.hibyte(result);
    cc[nn+1]=(byte)crc20.lobyte(result);
    
    result=crc20.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc20 gave a non-zero CRC");
      
    }

    // Test generator 3 with fill=false
    Icrc crc30=new Icrc(3,false);
    result=crc30.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc30.hibyte(result);
    cc[nn+1]=(byte)crc30.lobyte(result);
    
    result=crc30.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc30 gave a non-zero CRC");
      
    }

    // Test generator 4 with fill=false
    Icrc crc40=new Icrc(4,false);
    result=crc40.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc40.hibyte(result);
    cc[nn+1]=(byte)crc40.lobyte(result);
    
    result=crc40.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc40 gave a non-zero CRC");
      
    }

    // Test generator 5 with fill=false
    Icrc crc50=new Icrc(5,false);
    result=crc50.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc50.hibyte(result);
    cc[nn+1]=(byte)crc50.lobyte(result);
    
    result=crc50.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc50 gave a non-zero CRC");
      
    }

    // Test generator 6 with fill=false
    Icrc crc60=new Icrc(6,false);
    result=crc60.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc60.hibyte(result);
    cc[nn+1]=(byte)crc60.lobyte(result);
    
    result=crc60.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc60 gave a non-zero CRC");
      
    }

    // Test generator 7 with fill=false
    Icrc crc70=new Icrc(7,false);
    result=crc70.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc70.hibyte(result);
    cc[nn+1]=(byte)crc70.lobyte(result);
    
    result=crc70.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc70 gave a non-zero CRC");
      
    }

    // Test generator 0 with fill=true
    Icrc crc01=new Icrc(0,true);
    result=crc01.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc01.hibyte(result);
    cc[nn+1]=(byte)crc01.lobyte(result);
    
    result=crc01.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc01 gave a non-zero CRC");
      
    }

    // Test generator 1 with fill=true
    Icrc crc11=new Icrc(1,true);
    result=crc11.crc(str.getBytes());
//    System.out.printf(result);
    // Append result onto str and look for zero CRC
    cc[nn]=(byte)crc11.hibyte(result);
    cc[nn+1]=(byte)crc11.lobyte(result);
    
    result=crc11.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc11 gave a non-zero CRC");
      
    }

    // Test generator 2 with fill=true
    Icrc crc21=new Icrc(2,true);
    result=crc21.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc21.hibyte(result);
    cc[nn+1]=(byte)crc21.lobyte(result);
    
    result=crc21.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc21 gave a non-zero CRC");
      
    }

    // Test generator 3 with fill=true
    Icrc crc31=new Icrc(3,true);
    result=crc31.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc31.hibyte(result);
    cc[nn+1]=(byte)crc31.lobyte(result);
    
    result=crc31.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc31 gave a non-zero CRC");
      
    }

    // Test generator 4 with fill=true
    Icrc crc41=new Icrc(4,true);
    result=crc41.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc41.hibyte(result);
    cc[nn+1]=(byte)crc41.lobyte(result);
    
    result=crc41.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc41 gave a non-zero CRC");
      
    }

    // Test generator 5 with fill=true
    Icrc crc51=new Icrc(5,true);
    result=crc51.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc51.hibyte(result);
    cc[nn+1]=(byte)crc51.lobyte(result);
    
    result=crc51.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc51 gave a non-zero CRC");
      
    }

    // Test generator 6 with fill=true
    Icrc crc61=new Icrc(6,true);
    result=crc61.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc61.hibyte(result);
    cc[nn+1]=(byte)crc61.lobyte(result);
    
    result=crc61.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc61 gave a non-zero CRC");
      
    }

    // Test generator 7 with fill=true
    Icrc crc71=new Icrc(7,true);
    result=crc71.crc(str.getBytes());
//    System.out.printf(result);
    cc[nn]=(byte)crc71.hibyte(result);
    cc[nn+1]=(byte)crc71.lobyte(result);
    
    result=crc71.crc(cc);
//    System.out.printf(result << endl);
    localflag = (result != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Icrc: Case crc71 gave a non-zero CRC");
      
    }

    // introduce errors into str and look for CRC indications
    Ran myran = new Ran(17);

    result=crc01.crc(str.getBytes());
    cc[nn]=(byte)crc01.hibyte(result);
    cc[nn+1]=(byte)crc01.lobyte(result);
    for (i=1;i<256;i++) {
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc01.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc01");
        
      }
    }

    result=crc11.crc(str.getBytes());
    cc[nn]=(byte)crc11.hibyte(result);
    cc[nn+1]=(byte)crc11.lobyte(result);
    for (i=1;i<256;i++) {
      
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc11.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc11");
        
      }
    }

    result=crc21.crc(str.getBytes());
    cc[nn]=(byte)crc21.hibyte(result);
    cc[nn+1]=(byte)crc21.lobyte(result);
    for (i=1;i<256;i++) {
      
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc21.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc21");
        
      }
    }

    result=crc31.crc(str.getBytes());
    cc[nn]=(byte)crc31.hibyte(result);
    cc[nn+1]=(byte)crc31.lobyte(result);
    for (i=1;i<256;i++) {
      
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc31.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc31");
        
      }
    }

    result=crc41.crc(str.getBytes());
    cc[nn]=(byte)crc41.hibyte(result);
    cc[nn+1]=(byte)crc41.lobyte(result);
    for (i=1;i<256;i++) {
      
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc41.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc41");
        
      }
    }

    result=crc51.crc(str.getBytes());
    cc[nn]=(byte)crc51.hibyte(result);
    cc[nn+1]=(byte)crc51.lobyte(result);
    for (i=1;i<256;i++) {
      
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc51.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc51");
        
      }
    }

    result=crc61.crc(str.getBytes());
    cc[nn]=(byte)crc61.hibyte(result);
    cc[nn+1]=(byte)crc61.lobyte(result);
    for (i=1;i<256;i++) {
      
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc61.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc61");
        
      }
    }

    result=crc71.crc(str.getBytes());
    cc[nn]=(byte)crc71.hibyte(result);
    cc[nn+1]=(byte)crc71.lobyte(result);
    for (i=1;i<256;i++) {
      
      j=myran.int32p() % 43;
      cc[j]=(byte)(cc[j] ^ (i&0xFFFF));
      result=crc71.crc(cc);
//      System.out.printf(result);
//      if (result == 0) System.out.printf(result << "  %f\n", hex << i);
      localflag = (result == 0);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Icrc: A byte error was undetected by case crc71");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
