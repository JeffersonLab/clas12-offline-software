package com.nr.test.test_chapter10;

import static com.nr.min.StringAlign.stringalign;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;

public class Test_stringalign {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,N1=100,N2=64;
    char protein[]={'A','C','T','G'};
    char[] string1,string2,string3,aout,bout,summary;
    boolean localflag, globalflag=false;

    

    // Test stringalign
    System.out.println("Testing stringalign");

    string1=new char[N1+1];
    string2=new char[N2+1];
    string3=new char[N2+1];
    string1[N1]='\0';
    string2[N2]='\0';
    string3[N2]='\0';

    Ran myran = new Ran(17);
    for (i=0;i<N1;i++)
      string1[i]=protein[3];
    for (i=0;i<N2;i++)
      string2[i]=protein[myran.int32p()%3];

//    System.out.printf(string1);
//    System.out.printf(string2 << endl);

    // Bury a needle in the haystack
    for (i=0;i<N2;i+=4) {
      string1[(int)(1.5*i)+3]=string2[i];
      string1[(int)(1.5*i)+4]=string2[i+1];
      string1[(int)(1.5*i)+5]=string2[i+2];
      string1[(int)(1.5*i)+6]=string2[i+3];
    }

//    System.out.printf(string1);
//    System.out.printf(string2 << endl);

    aout=new char[N1+N2+1];
    bout=new char[N1+N2+1];
    summary=new char[N1+N2+1];

    stringalign(string1,string2,0.2,0.1,0.0,aout,bout,summary);

//    System.out.printf(aout);
//    System.out.printf(bout);
//    System.out.printf(summary);

    localflag = !new String(string1,0,N1+1).equals(new String(aout,0,N1+1));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** stringalign: aout is not the same as string1");
      
    }

    j=0;
    k=3;
    for (i=0;i<16;i++) {
      string3[j++]=bout[k++];
      string3[j++]=bout[k++];
      string3[j++]=bout[k++];
      string3[j++]=bout[k++];
      k+=2;
    }

    localflag = !new String(string3).equals(new String(string2));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** stringalign: bout is not the expected 4 letter groups from string2");
      
    }

    localflag=false;
    for (i=1;i<N1-1;i++)
      localflag = localflag || summary[i] != ((i-1)%6 < 2 ? ' ' : '=');
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** stringalign: Summary string was not in groups of 4 equal signs");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
