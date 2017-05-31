package com.nr.test.test_chapter14;

import static com.nr.stat.Stattests.quadct;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.ran.Ran;

public class Test_quadct {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=8192;
    doubleW fa=new doubleW(0),fb = new doubleW(0);
    doubleW fc = new doubleW(0),fd = new doubleW(0);
    double x=0.5,y=0.5;
    double ffa=0.25/4.0,ffb=0.25*3.0/4.0,ffc=0.25*9.0/4.0,ffd=0.25*3.0/4.0;
    double[] xx=new double[N],yy=new double[N];
    boolean localflag=false,globalflag=false;

    

    // Test quadct
    System.out.println("Testing quadct");

    Ran myran=new Ran(17);
    j=0;
    for (i=0;i<N/4;i++) {
      xx[j]=myran.doub();   // Put a point in each x-y quadrant
      yy[j++]=myran.doub();
      xx[j]=-myran.doub();
      yy[j++]=myran.doub();
      xx[j]=-myran.doub();
      yy[j++]=-myran.doub();
      xx[j]=myran.doub();
      yy[j++]=-myran.doub();
    }   
    quadct(0.0,0.0,xx,yy,fa,fb,fc,fd);
//    System.out.printf(fa << " %f\n", fb << " %f\n", fc << " %f\n", fd);
    localflag = (fa.val != 0.25) || (fb.val != 0.25) || (fc.val != 0.25) || (fd.val != 0.25);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** quadct: Fractions do not agree know exact values");
      
    }

    quadct(x,y,xx,yy,fa,fb,fc,fd);    // Offset reference coordinates
//    System.out.printf(fa << " %f\n", fb << " %f\n", fc << " %f\n", fd);
//    System.out.printf(ffa << " %f\n", ffb << " %f\n", ffc << " %f\n", ffd);

    localflag = (abs(fa.val-ffa) > 0.02) || (abs(fb.val-ffb) > 0.02) || (abs(fc.val-ffc) > 0.02) || (abs(fd.val-ffd) > 0.02);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** quadct: Fractions do not agree adequately with uniform distribution");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
