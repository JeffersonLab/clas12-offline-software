package com.nr.test.test_chapter22;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.lna.Icrc.*;
public class Test_decchk {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,nbad,ntot,N=7;
    byte[] ch = new byte[1];
    boolean localflag=false, globalflag=false;

    

    // Test decchk
    System.out.println("Testing decchk");

    byte[] s1="8635741".getBytes();
    byte[] s2="0".getBytes();
    byte[] s3;

    // Test for functionality of the check digit
    ntot=nbad=0;
    for (i=0;i<N;i++) {
      for (j='0';j<='9';j++) {
        ntot++;
        s3=new String(s1).getBytes();;
        s3[i]=(byte)j;
        decchk(s3,s2,0);
        s3=(new String(s3)+new String(s2)).getBytes();
        if (!decchk(s3,ch,0)) nbad++;
      }
    }
    localflag = localflag || (nbad != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** decchk: Failure of generated check digit");
      
    }

    // Test for capture of single errors in string
    ntot=nbad=0;
    for (i=0;i<N;i++) {
      for (j='0';j<='9';j++) {
        for (k='0';k<='9';k++) {
          if (j != k) {
            ntot++;
            s3=new String(s1).getBytes();
            s3[i]=(byte)j;
            decchk(s3,s2,0);
            s3=(new String(s3)+new String(s2)).getBytes();
            s3[i]=(byte)k;
            if (!decchk(s3,ch,0)) nbad++;
          }
        }
      }
    }
//    System.out.println("Single digit errors");
//    System.out.println("  Total tries:%f\n", " %f\n", ntot);
//    System.out.println("  Bad tries:%f\n", " %f\n", nbad);
    localflag = localflag || (nbad != ntot);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** decchk: Failure to identify all Single Digit errors");
      
    }

    // Test for capture of adjacent transpositions
    ntot=nbad=0;
    for (i=0;i<N-1;i++){
      for (j='0';j<='9';j++) {
        for (k='0';k<='9';k++) {
          if (j != k) {
            ntot++;
            s3=new String(s1).getBytes();
            s3[i]=(byte)j;
            s3[i+1]=(byte)k;
            decchk(s3,s2,0);
            s3=(new String(s3)+new String(s2)).getBytes();
            s3[i]=(byte)k;
            s3[i+1]=(byte)j;
            if (!decchk(s3,ch,0)) nbad++;
          }
        }
      }
    }
//    System.out.println("Adjacent Transpositions");
//    System.out.println("  Total tries:%f\n", " %f\n", ntot);
//    System.out.println("  Bad tries:%f\n", " %f\n", nbad);
    localflag = localflag || (nbad != ntot);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** decchk: Failure to identify all Adjacent Transpositions");
      
    }

    // Test twin errors
    ntot=nbad=0;
    for (i=0;i<N-1;i++) {
      for (j='0';j<='9';j++) {
        for (k='0';k<='9';k++) {
          if (j != k) {
            ntot++;
            s3=new String(s1).getBytes();
            s3[i]=(byte)j;
            s3[i+1]=(byte)j;
            decchk(s3,s2,0);
            s3=(new String(s3)+new String(s2)).getBytes();
            s3[i]=(byte)k;
            s3[i+1]=(byte)k;
            if (!decchk(s3,ch,0)) nbad++;
          }
        }
      }
    }
//    System.out.println("Twin errors:");
//    System.out.println("  Total tries:%f\n", " %f\n", ntot);
//    System.out.println("  Bad tries:%f\n", " %f\n", nbad);
    localflag = localflag || (nbad != 516);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** decchk: Failure to identify 516 of the 540 possible Twin errors");
      
    }

    // Test jump twin errors
    ntot=nbad=0;
    for (i=0;i<N-2;i++) {
      for (j='0';j<='9';j++) {
        for (k='0';k<='9';k++) {
          if (j != k) {
            ntot++;
            s3=new String(s1).getBytes();
            s3[i]=(byte)j;
            s3[i+2]=(byte)j;
            decchk(s3,s2,0);
            s3=(new String(s3)+new String(s2)).getBytes();
            s3[i]=(byte)k;
            s3[i+2]=(byte)k;
            if (!decchk(s3,ch,0)) nbad++;
          }
        }
      }
    }
//    System.out.println("Jump twin errors:");
//    System.out.println("  Total tries:%f\n", " %f\n", ntot);
//    System.out.println("  Bad tries:%f\n", " %f\n", nbad);
    localflag = localflag || (nbad != 426);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** decchk: Failure to identify 426 of the 450 possible Jump Twin errors");
      
    }

    // test all jump transpositions of the form 86ikj41 -> 86jki41 where i != j
    ntot=nbad=0;
    for (i='0';i<='9';i++) {
      for (j='0';j<='9';j++) {
        for (k='0';k<='9';k++) {
          s3=new String(s1).getBytes();
          s3[3]=(byte)k;
          if (i != j) {
            ntot++;
            s3[2]=(byte)i;
            s3[4]=(byte)j;
            decchk(s3,s2,0);
            s3=(new String(s3)+new String(s2)).getBytes();
            s3[2]=(byte)j;    // Transpose digits
            s3[4]=(byte)i;      
            if (!decchk(s3,ch,0)) nbad++; // See if it works with transposition
          }
        }
      }
    }
//    System.out.println("Jump Transitions:");
//    System.out.println("  Total tries:%f\n", " %f\n", ntot);
//    System.out.println("  Bad tries:%f\n", " %f\n", nbad);
    localflag = localflag || (nbad != 848);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** decchk: Failure to identify 848 of the possible 900 Jump Transition errors");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
