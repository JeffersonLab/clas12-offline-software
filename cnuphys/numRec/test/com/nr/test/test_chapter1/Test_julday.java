package com.nr.test.test_chapter1;


import static com.nr.util.Calendar.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_julday {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,id,im,iy,nrjday,N=16;
    /*
    char *event[]={"End of millenium","One day later",
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
    // Corresponding Julian days from U.S. Naval Observatory
    int expect[]={1721423,1721424,2299160,2299161,2344180,
      2402341,2417319,2420625,2423621,2427581,2427641,
      2428262,2428660,2435681,2442935,2440000};
    boolean localflag, globalflag=false;


    // Test julday
    System.out.println("Testing julday");

    for (i=0;i<N;i++) {
      id=nday[i];
      im=nmonth[i];
      iy=nyear[i];
      nrjday=julday(im,id,iy);

//      System.out.printf(setw(4) << im << setw(4) << id;
//      System.out.printf(setw(7) << iy << setw(10) << nrjday;
//      System.out.println("  " << event[i] << endl;

      localflag = (nrjday != expect[i]);
      globalflag = globalflag || localflag; 
      if (localflag) {
        fail("*** julday: Result does not agree with values from U.S. Naval Observatory");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
