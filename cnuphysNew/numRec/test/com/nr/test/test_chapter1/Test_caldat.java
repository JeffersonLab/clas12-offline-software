package com.nr.test.test_chapter1;

import static com.nr.util.Calendar.caldat;
import static com.nr.util.Calendar.julday;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.intW;

public class Test_caldat {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,id,im,iy,N=16;
    intW idd = new intW(0);
    intW imm = new intW(0);
    intW iyy = new intW(0);
    /*
    String[] event={"End of millenium","One day later",
      "Day before Gregorian calendar","Gregorian calendar adopted",
      "Benjamin Franklin born","Abraham Lincoln shot",
      "San Francisco earthquake","Sinking of the Lusitania",
      "Pancho Villa assassinated","Bonnie and Clyde subdued",
      "John Dillinger shot","Bruno Hauptman electrocuted",
      "Hindenburg disaster","Sinking of the Andrea Doria",
      "Teton dam collapse","Julian Day 2440000"};*/
    int nmonth[]={12,1,10,10,1,4,4,5,7,5,7,4,5,7,6,5};
    int nday[]={31,01,04,15,17,14,18,7,20,23,22,3,6,26,5,23};
    int nyear[]={-1,1,1582,1582,1706,1865,1906,1915,1923,1934,
      1934,1936,1937,1956,1976,1968}; 

    boolean localflag, globalflag=false;


    // Test caldat
    System.out.println("Testing caldat");

    // Check whether caldat properly undoes the operation of julday
    for (i=0;i<N;i++) {
      id=nday[i];
      im=nmonth[i];
      iy=nyear[i];
      j=julday(im,id,iy);
      caldat(j,imm,idd,iyy);
//      System.out.printf(event[i] << endl;
//      System.out.println("USNO: " << setw(4) << im << setw(4) << id << setw(6) << iy << endl;
//      System.out.println("NR:   " << setw(4) << imm << setw(4) << idd << setw(6) << iyy << endl;
//      System.out.printf(endl;

      localflag = (iyy.val != iy);
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** caldat: Round-trip test did not return to same year");
      }

      localflag = (imm.val != im);
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** caldat: Round-trip test did not return to same month");
      }

      localflag = (idd.val != id);
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** caldat: Round-trip test did not return to same day");
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
