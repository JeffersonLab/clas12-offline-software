package com.nr.test.test_chapter16;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildMatrix;
import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.Kmeans;
import com.nr.ran.Normaldev;

public class Test_Kmeans {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,flag=0,NDIM=2,NMEANS=4,NPT=1000;
    double sqrt2=sqrt(2.0),d0,d1,sbeps;
    double ffrac[]={0.25,0.25,0.25,0.25};
    double mmeans[]={0.0,0.0,0.75,0.0,-0.25,-0.25,0.33,0.66};
    double gguess[]={0.1,0.1,0.7,0.1,-0.2,-0.3,0.3,0.5};
    double ssigma[]={0.1,0.1,0.02,0.2,0.01,0.1,0.1,0.05};
    double vvec1[]={1.0,0.0,1.0,0.0,sqrt2,sqrt2,sqrt2,sqrt2};
    double vvec2[]={0.0,1.0,0.0,1.0,-sqrt2,sqrt2,-sqrt2,sqrt2};
    int[] count=new int[NMEANS],error=new int[NMEANS];
    double[] frac=buildVector(ffrac),offset=new double[NMEANS];
    double[][] guess=buildMatrix(NMEANS,NDIM,gguess);
    double[][] means=buildMatrix(NMEANS,NDIM,mmeans),sigma=buildMatrix(NMEANS,NDIM,ssigma);
    double[][] vec1=buildMatrix(NMEANS,NDIM,vvec1),vec2=buildMatrix(NMEANS,NDIM,vvec2),x=new double[NPT][NDIM];
    boolean localflag, globalflag=false;

    

    // Test Kmeans
    System.out.println("Testing Kmeans");

    Normaldev ndev = new Normaldev(0.0,1.0,17);

    // Generate four groups of data
    k=0;
    for (i=0;i<4;i++) {
      count[i]=(int)(NPT*frac[i]);
      for (j=0;j<count[i];j++) {
        d0=sigma[i][0]*ndev.dev();
        d1=sigma[i][1]*ndev.dev();
        x[k][0]=means[i][0]+d0*vec1[i][0]+d1*vec2[i][0];
        x[k][1]=means[i][1]+d0*vec1[i][1]+d1*vec2[i][1];
        k++;
      }
    }

    Kmeans kmean=new Kmeans(x,guess);  // *** put in some weird guesses

    for (i=0;i<100;i++) {
      flag=kmean.estep();
      if (flag == 0) break;
      kmean.mstep();
    }

    // check for convergence
//    System.out.println("  flag: %f\n", flag);
    localflag = flag > 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Kmeans: No solution with 100 iterations");
      
    }

    // Check for correct populations
//    for (i=0;i<NMEANS;i++) System.out.printf(kmean.count[i] << " ";
//    System.out.printf(endl;
    for (i=0;i<NMEANS;i++) {
      error[i]=kmean.count[i]-count[i];
      localflag = (error[i] > 0 ? error[i] : -error[i]) > 15;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Kmeans: Populations are not the correct approximate size");
        
      }
    }

    // Check group assignments
    k=0;
    for (i=0;i<NMEANS;i++) {
      error[i]=0;
      for (j=0;j<count[i];j++) {
        if (kmean.assign[k] != i) error[i]++;
        k++;
      }
//      System.out.printf(error[i] << " ";
    }
//    System.out.printf(endl;

    // Check for correct determination of means
    for (i=0;i<NMEANS;i++) offset[i]=sqrt(SQR(kmean.means[i][0]-means[i][0])
      +SQR(kmean.means[i][1]-means[i][1]));
//    System.out.printf(maxel(offset));
    sbeps=0.02;
    localflag = maxel(offset) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Kmeans: Means are incorrectly identified");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
