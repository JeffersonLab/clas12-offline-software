package com.nr.test.test_chapter14;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Moment.moment;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.acos;
import static java.lang.Math.sin;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_moment {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int NPTS=10000,NBIN=101,NPNB=NPTS+NBIN;
    int n=0,k,nlim;
    double pi=acos(-1.0),sbeps;
    // Expected ave,adev,sdev,vrnce,skew,curt
    double expect[]={pi/2.0,pi/2.0-1.0,0.683667,0.467401,0.0,-0.806249};
    doubleW ave=new doubleW(0),adev = new doubleW(0);
    doubleW sdev=new doubleW(0),vrnce = new doubleW(0);
    doubleW skew=new doubleW(0),curt = new doubleW(0);
    double x;
    double[] temp=new double[NPNB],e=buildVector(expect);
    boolean localflag, globalflag=false;

    

    // Test moment
    System.out.println("Testing moment");

    // Create a sinusoidal distribution
    for (x=0.0;x<=pi;x+=pi/NBIN) {
      nlim=(int)(0.5+sin(x)*pi/2.0*NPTS/NBIN);
      for (k=0;k<nlim;k++) temp[n++]=x;
    }
    double[] data=new double[n];
    for (k=0;k<n;k++) data[k]=temp[k];
    moment(data,ave,adev,sdev,vrnce,skew,curt);
    double observe[]={ave.val,adev.val,sdev.val,vrnce.val,skew.val,curt.val};
    double[] obs=buildVector(observe);
//    for (int i=0;i<6;i++) 
//      System.out.printf(observe[i] << "  %f\n", expect[i] << "  %f\n", expect[i]-observe[i]);
//    System.out.printf(maxel(vecsub(e,o)));
    sbeps = 2.e-3;

    localflag = maxel(vecsub(e,obs)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** moment: Moments of sinusoidal distribution were out of spec");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
