package com.nr.test.test_chapter1;

import static com.nr.util.Calendar.*;
import static java.lang.Math.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.netlib.util.*;

public class Test_flmoon {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,i1,i2,j1,n,nph;
    intW j2 = new intW(0);
    intW iy = new intW(0);
    intW im = new intW(0);
    intW id = new intW(0);
    doubleW frac = new doubleW(0);  
    // Data from U.S. Naval Observatory follows:
    int nday[]={21,19,20,18,18,16,16,15,13,13,11,11};
    int nhour[]={4,16,4,17,7,22,13,5,19,8,21,9};
    int nmin[]={40,27,44,41,34,27,55,13,37,53,15,03};
    boolean localflag, globalflag=false;


    // Test flmoon
    System.out.println("Testing flmoon");

    // Full moons for the year 2000
    im.val=1;
    id.val=1;
    iy.val=2000;
    nph=2;

    // Approximate number of full moons since january 1900
    n=(int)(12.37*(iy.val-1900+((im.val-0.5)/12.0)));
    j1=julday(im.val,id.val,iy.val);
    flmoon(n,nph,j2,frac);
    n += (int)((j1-j2.val)/29.53 + (j1 >= j2.val ? 0.5 : -0.5));
    for (i=0;i < 12;i++) {
      n++;
      flmoon(n,nph,j2,frac);
      frac.val=24.0*frac.val;
      if (frac.val < 0.0) {
        j2.val--;
        frac.val += 24.0;
      }
      if (frac.val > 12.0) {
        j2.val++;
        frac.val -= 12.0;
      } else
        frac.val += 12.0;
      i1=(int)(frac.val);     // hours
      i2=(int)(60.0*(frac.val-i1)); // minutes
      caldat(j2.val,im,id,iy);

      localflag = (im.val != i+1);
      globalflag = globalflag || localflag; 
      if (localflag) {
        System.out.println("*** flmoon: There should be a full moon in each month");
        
      }

      localflag = (id.val != nday[i]);
      globalflag = globalflag || localflag; 
      if (localflag) {
        System.out.println("*** flmoon: Day of full moon disagrees with U.S. Naval Observatory");
        
      }

//      System.out.printf(frac-(nhour[i]+nmin[i]/60.0) << endl;
      localflag = abs(frac.val-(nhour[i]+nmin[i]/60.0)) > 1.0;
      globalflag = globalflag || localflag; 
      if (localflag) {
        System.out.println("*** flmoon: Time of full moon disagrees with U.S. Naval Observatory by > 2 hrs");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");

  }

}
