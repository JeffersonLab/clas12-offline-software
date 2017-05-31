package com.nr.test.test_chapter16;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.*;
import static com.nr.test.NRTestUtil.*;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.Gaumixmod;
import com.nr.ran.Normaldev;

public class Test_Gaumixmod {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,NDIM=2,NMEANS=4,NPT=1000;
    double flag=0,sqrt2=sqrt(2.0),d0,d1,sbeps;
    double ffrac[]={0.25,0.25,0.25,0.25};
    double mmeans[]={0.0,0.0,0.75,0.0,-0.25,-0.25,0.33,0.66};
    double gguess[]={0.1,0.1,0.7,0.1,-0.2,-0.3,0.3,0.5};
    double ssigma[]={0.1,0.1,0.02,0.2,0.01,0.1,0.1,0.05};
    double vvec1[]={1.0,0.0,1.0,0.0,sqrt2,sqrt2,sqrt2,sqrt2};
    double vvec2[]={0.0,1.0,0.0,1.0,-sqrt2,sqrt2,-sqrt2,sqrt2};
    double[] frac=buildVector(ffrac),offset = new double[NMEANS];
    double[][] guess=buildMatrix(NMEANS,NDIM,gguess);
    double[][] means=buildMatrix(NMEANS,NDIM,mmeans),sigma=buildMatrix(NMEANS,NDIM,ssigma);
    double[][] vec1=buildMatrix(NMEANS,NDIM,vvec1),vec2=buildMatrix(NMEANS,NDIM,vvec2),x=new double[NPT][NDIM];
    boolean localflag, globalflag=false;

    

    // Test Gaumixmod
    System.out.println("Testing Gaumixmod");

    Normaldev ndev=new Normaldev(0.0,1.0,17);

    // Generate four groups of data
    k=0;
    for (i=0;i<4;i++) {
      for (j=0;j<(int)(NPT*frac[i]);j++) {
        d0=sigma[i][0]*ndev.dev();
        d1=sigma[i][1]*ndev.dev();
        x[k][0]=means[i][0]+d0*vec1[i][0]+d1*vec2[i][0];
        x[k][1]=means[i][1]+d0*vec1[i][1]+d1*vec2[i][1];
        k++;
      }
    }

    Gaumixmod gmix = new Gaumixmod(x,guess);

    for (i=0;i<100;i++) {
      flag=gmix.estep();
      if (flag < 1.e-6) break;
      gmix.mstep();
    }

    // check for convergence
    //    System.out.println("  flag: %f\n", flag);
    localflag = flag > 1.e-6;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gaumixmod: No solution with 100 iterations");
      
    }

    // Check for correct determination of population fractions
    //    System.out.printf(maxel(vecsub(gmix.frac,frac)));
    sbeps=0.005;
    localflag = maxel(vecsub(gmix.frac,frac)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gaumixmod: Population fractions not accurately determined");
      
    }

    // Check for correct determination of means
    for (i=0;i<NMEANS;i++) offset[i]=sqrt(SQR(gmix.means[i][0]-means[i][0])
      +SQR(gmix.means[i][1]-means[i][1]));
    //    System.out.printf(maxel(offset));
    localflag = maxel(offset) > 0.01;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gaumixmod: Means are incorrectly identified");
      
    }

    // Check for correct determination of covariance matrices
    for (i=0;i<NMEANS;i++) {
      System.out.printf("%f %f\n",gmix.sig[i][0][0], gmix.sig[i][0][1]);
      System.out.printf("%f %f\n\n",gmix.sig[i][1][0], gmix.sig[i][1][1]);
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
