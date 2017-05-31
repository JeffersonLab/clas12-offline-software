package com.nr.test.test_chapter14;

import static com.nr.stat.Stattests.chsone;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Expondev;

public class Test_chsone {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,k,ibin,NBINS=10,NPTS=10000;
    doubleW chsq=new doubleW(0),prob = new  doubleW(0),df =new doubleW(0);
    double x;
    double[] bins=new double[NBINS],ebins=new double[NBINS];
    boolean localflag=false,globalflag=false;

    

    // Test chsone
    System.out.println("Testing chsone");

    Expondev edev=new Expondev(1.0,17);
    for (k=0;k<NPTS;k++) {
      x=edev.dev();
      ibin=(int)(x*NBINS/3.0);
      if (ibin < NBINS) ++bins[ibin];
    }
    for (i=0;i<NBINS;i++) ebins[i]=bins[i];
    chsone(bins,ebins,df,chsq,prob,0);
    localflag = localflag || (chsq.val != 0.) || (prob.val != 1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** chsone: Incorrect chsq or prob for perfectly matching distributions");
      
    }

    for (i=0;i<NBINS;i++)
      ebins[i]=3.0*NPTS/NBINS*exp(-3.0*(i+0.5)/NBINS);
    chsone(bins,ebins,df,chsq,prob,0);
//    System.out.printf(df << " %f\n", chsq << " %f\n", prob);
    localflag = (df.val != NBINS);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** chsone: Wrong number of degrees of freedom reported");
      
    }

    localflag = (prob.val < 0.20);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** chstwo: Unexpectedly low probability");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
