package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Bessjy;

public class Test_Bessjy {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=17;
    double sbeps;
    double x[]={-32.0,-16.0,-8.0,-4.0,-2.0,-1.0,-0.5,-0.1,0.0,
      0.1,0.5,1.0,2.0,4.0,8.0,16.0,32.0};
    double y1[]={1.380790097465560e-1,-1.748990739836292e-1,1.716508071375539e-1,
      -3.971498098638472e-1,2.238907791412356e-1,7.651976865579666e-1,
      9.384698072408130e-1,9.975015620660401e-1,1.0,9.975015620660401e-1,
      9.384698072408130e-1,7.651976865579666e-1,2.238907791412356e-1,
      -3.971498098638472e-1,1.716508071375539e-1,-1.748990739836292e-1,
      1.380790097465560e-1}; //,-5.767248077568736e-1 XXX remove it.
    double y2[]={2.658902847590528e-2,-9.039717566130416e-2,-2.346363468539147e-1,
      6.604332802354906e-2,-5.767248077568736e-001,-4.400505857449336e-1,
      -2.422684576748739e-1,-4.993752603624202e-2,0.0,4.993752603624202e-2,
      2.422684576748739e-1,4.400505857449336e-1,5.767248077568736e-001,
      -6.604332802354906e-2,2.346363468539147e-1,9.039717566130416e-2,
      -2.658902847590528e-2};
    double[] yy1=buildVector(y1),yy2=buildVector(y2),zz1=new double[N],zz2=new double[N];
    boolean localflag, globalflag=false;

    // Test Bessjy (j0)
    System.out.println("Testing Bessjy (j0)");
    Bessjy bess = new Bessjy();
    sbeps=1.e-15;
    for (i=0;i<N;i++) zz1[i]=bess.j0(x[i]);
    System.out.printf("Bessjy: Maximum discrepancy = %f\n", maxel(vecsub(zz1,yy1)));
    localflag = maxel(vecsub(zz1,yy1)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessjy (j0): Incorrect function values");
      
    }

    // Test Bessjy (j1)
    System.out.println("Testing Bessjy (j1)");
    sbeps=1.e-15;
    for (i=0;i<N;i++) zz2[i]=bess.j1(x[i]);
    System.out.printf("Bessjy: Maximum discrepancy = %f\n", maxel(vecsub(zz2,yy2)));
    localflag = maxel(vecsub(zz2,yy2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessjy (j1): Incorrect function values");
      
    }

    N=10;
    double x3[]={0.001,0.01,0.1,0.5,1.0,2.0,4.0,8.0,16.0,32.0};
    double y3[]={-4.471416611375924,-3.005455637083646,-1.534238651350367,
      -4.445187335067066e-1,8.825696421567700e-2,5.103756726497453e-1,
      -1.694073932506495e-2,2.235214893875663e-1,9.581099708071243e-2,
      -2.874248465443332e-2};
    double y4[]={-6.366221672311394e+002,-6.367859628206066e+001,-6.458951094702027,
      -1.471472392670243,-7.812128213002889e-1,-1.070324315409375e-1,
      3.979257105571000e-1,-1.580604617312476e-1,1.779751689394169e-1,
      -1.385448315327238e-1};
    double[] yy3=buildVector(y3),yy4=buildVector(y4),zz3=new double[N],zz4=new double[N];

    // Test Bessjy (y0)
    System.out.println("Testing Bessjy (y0)");
    sbeps=1.e-14;
    for (i=0;i<N;i++) zz3[i]=bess.y0(x3[i]);
    System.out.printf("Bessjy: Maximum discrepancy = %f\n", maxel(vecsub(zz3,yy3)));
    localflag = maxel(vecsub(zz3,yy3)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessjy (y0): Incorrect function values");
      
    }

    // Test Bessjy (y1)
    System.out.println("Testing Bessjy (y1)");
    sbeps=1.5e-13;
    for (i=0;i<N;i++) zz4[i]=bess.y1(x3[i]);
    System.out.printf("Bessjy: Maximum discrepancy = %f\n", maxel(vecsub(zz4,yy4)));
    localflag = maxel(vecsub(zz4,yy4)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessjy (y1): Incorrect function values");
      
    }

    N=16;
    int n5[]={2,2,2,2,5,5,5,5,10,10,10,10,50,50,50,50};
    double x5[]={0.01,0.1,1.0,10.0,0.01,0.1,1.0,10.0,0.01,0.1,1.0,10.0,0.01,0.1,1.0,10.0,};
    double y5[]={1.249989583365887e-5,1.248958658799919e-3,1.149034849319005e-1,
      2.546303136851206e-1,2.604155815991601e-14,2.603081790964445e-9,
      2.497577302112346e-4,-2.340615281867936e-1,2.691138339236341e-30,
      2.690532895434228e-20,2.630615123687453e-10,2.074861066333589e-1,
      2.920284285406856e-180,2.920142569099765e-130,2.906004948173220e-80,
      1.784513607871616e-30};
    double y6[]={-1.273271380077505e+4,-1.276447832426902e+2,-1.650682606816255,
      -5.868082442208572e-3,-2.444635204829711e12,-2.446148450230391e7,
      -2.604058666258123e2,1.354030476893622e-1,-1.182808190517663e28,
      -1.183133513204520e18,-1.216180142786892e8,-3.598141521834027e-1,
      -2.179992503765307e+177,-2.180102618471610e+127,-2.191142812605340e77,
      -3.641066501800778e27};
    double[] yy5=buildVector(y5),yy6=buildVector(y6),zz5=new double[N],zz6=new double[N];

    

    // Test Bessjy (jn)
    System.out.println("Testing Bessjy (jn)");

    sbeps=5.e-14;
    for (i=0;i<N;i++) zz5[i]=bess.jn(n5[i],x5[i]);
    for (i=0;i<N;i++) {
//      System.out.printf(fabs((zz5[i]-yy5[i])/yy5[i]));
      localflag = localflag || abs((zz5[i]-yy5[i])/yy5[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessjy (jn): Incorrect function values");
      
    }

    // Test Bessjy (yn)
    System.out.println("Testing Bessjy (yn)");
    sbeps=5.e-14;
    for (i=0;i<N;i++) zz6[i]=bess.yn(n5[i],x5[i]);
    for (i=0;i<N;i++) {
//      System.out.printf(abs((zz6[i]-yy6[i])/yy6[i]));
      localflag = localflag || abs((zz6[i]-yy6[i])/yy6[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessjy (yn): Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
