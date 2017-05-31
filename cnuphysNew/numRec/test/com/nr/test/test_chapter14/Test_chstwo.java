package com.nr.test.test_chapter14;

import static com.nr.stat.Stattests.chstwo;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Expondev;
public class Test_chstwo {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int NBINS=10,NPTS=10000;
    int i,ibin;
    doubleW chsq=new doubleW(0),prob = new  doubleW(0),df =new doubleW(0);
    double x;
    double[] bins1=new double[NBINS],bins2=new double[NBINS];
    boolean localflag=false,globalflag=false;

    

    // Test chstwo
    System.out.println("Testing chstwo");

    Expondev edev = new Expondev(1.0,17);
    for (i=0;i<NPTS;i++) {
      x=edev.dev();
      ibin=(int)(x*NBINS/3.0);
      if (ibin < NBINS) ++bins1[ibin];
    }
    for (i=0;i<NBINS;i++) bins2[i]=bins1[i];
    chstwo(bins1,bins2,df,chsq,prob,0);
    localflag = localflag || (chsq.val != 0.) || (prob.val != 1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** chtwo: Incorrect chsq or prob for perfectly matching distributions");
      
    }

    for (i=0;i<NBINS;i++) bins2[i]=0;
    for (i=0;i<NPTS;i++) {
      x=edev.dev();
      ibin=(int)(x*NBINS/3.0);
      if (ibin < NBINS) ++bins2[ibin];
    }
    chstwo(bins1,bins2,df,chsq,prob,0);
//    System.out.printf(df << " %f\n", chsq << " %f\n", prob);
    localflag = (df.val != NBINS);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** chstwo: Wrong number of degrees of freedom reported");
      
    }

    localflag = (prob.val < 0.20);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** chsone: Unexpectedly low probability");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
