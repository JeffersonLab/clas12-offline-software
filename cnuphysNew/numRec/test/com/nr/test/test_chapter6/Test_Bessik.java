package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Bessik;

public class Test_Bessik {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=17;
    double sbeps=1.e-15;
    double x[]={-32.0,-16.0,-8.0,-4.0,-2.0,-1.0,-0.5,-0.1,0.0,
      0.1,0.5,1.0,2.0,4.0,8.0,16.0,32.0};
    double y1[]={5.590908381350871e12,8.934462279201048e5,4.275641157218047e2,
      1.130192195213633e1,2.279585302336067,1.266065877752008,
      1.063483370741324,1.002501562934095,1.000000000000000,
      1.002501562934095,1.063483370741324,1.266065877752008,
      2.279585302336067,1.130192195213633e1,4.275641157218047e2,
      8.934462279201048e5,5.590908381350871e12};
    double y2[]={-5.502845511211247e12,-8.650594358548392e5,-3.998731367825601e2,
      -9.759465153704449e0,-1.590636854637329e0,-5.651591039924850e-1,
      -2.578943053908963e-1,-5.006252604709272e-2,0.0,5.006252604709272e-2,
      2.578943053908963e-1,5.651591039924850e-1,1.590636854637329e0,
      9.759465153704449e0,3.998731367825601e2,8.650594358548392e5,
      5.502845511211247e12};
    double[] yy1=buildVector(y1),yy2=buildVector(y2),zz1=new double[N],zz2=new double[N];
    boolean localflag, globalflag=false;

    

    // Test Bessik (i0)
    System.out.println("Testing Bessik (i0)");
    localflag=false;
    Bessik bess = new Bessik();
    sbeps=1.e-15;
    for (i=0;i<N;i++) zz1[i]=bess.i0(x[i]);
    for (i=0;i<N;i++) {
//      System.out.printf(fabs((zz1[i]-yy1[i])/yy1[i]));
      localflag = localflag || abs((zz1[i]-yy1[i])/yy1[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessik (i0): Incorrect function values");
      
    }

    // Test Bessik (i1)
    System.out.println("Testing Bessik (i1)");
    localflag=false;
    sbeps=1.e-15;
    for (i=0;i<N;i++) zz2[i]=bess.i1(x[i]);
    for (i=0;i<N;i++) {
      if (yy2[i] != 0.0) {
//        System.out.printf(abs((zz2[i]-yy2[i])/yy2[i]));
        localflag = localflag || abs((zz2[i]-yy2[i])/yy2[i]) > sbeps;
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessik (i1): Incorrect function values");
      
    }

    N=10;
    double x3[]={0.001,0.01,0.1,0.5,1.0,2.0,4.0,8.0,16.0,32.0};
    double y3[]={7.023688800562382,4.721244730161095,2.427069024702017,
      9.244190712276657e-1,4.210244382407083e-1,1.138938727495334e-1,
      1.115967608585303e-2,1.464707052228154e-4,3.499411663936500e-8,
      2.795057518761980e-15};
    double y4[]={9.999962381560855e2,9.997389411829624e1,9.853844780870606,
      1.656441120003301,6.019072301972346e-1,1.398658818165225e-1,
      1.248349888726843e-2,1.553692118050012e-4,3.607157117528780e-8,
      2.838399271974671e-15};
    double[] yy3=buildVector(y3),yy4=buildVector(y4),zz3=new double[N],zz4=new double[N];

    // Test Bessik (k0)
    System.out.println("Testing Bessik (k0)");
    localflag=false;
    sbeps=5.e-15;
    for (i=0;i<N;i++) zz3[i]=bess.k0(x3[i]);
    for (i=0;i<N;i++) {
//      System.out.printf(abs((zz3[i]-yy3[i])/yy3[i]));
      localflag = localflag || abs((zz3[i]-yy3[i])/yy3[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessik (k0): Incorrect function values");
      
    }

    // Test Bessik (k1)
    System.out.println("Testing Bessik (k1)");
    localflag=false;
    sbeps=5.e-15;
    for (i=0;i<N;i++) zz4[i]=bess.k1(x3[i]);
    for (i=0;i<N;i++) {
//      System.out.printf(abs((zz4[i]-yy4[i])/yy4[i]));
      localflag = localflag || abs((zz4[i]-yy4[i])/yy4[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessik (k1): Incorrect function values");
      
    }

    N=16;
    int n5[]={2,2,2,2,5,5,5,5,10,10,10,10,20,20,20,20};
    double x5[]={0.01,0.1,1.0,10.0,0.01,0.1,1.0,10.0,0.01,0.1,1.0,10.0,0.01,0.1,1.0,10.0,};
    double y5[]={1.250010416699221e-5,1.251041992241759e-3,1.357476697670383e-1,
      2.281518967726005e3,2.604177517380490e-14,2.605251929893700e-9,
      2.714631559569721e-4,7.771882864032602e2,2.691150571711139e-30,
      2.691756142922153e-20,2.752948039836874e-10,2.189170616372337e1,
      3.919909016180170e-65,3.920371031419948e-45,3.966835985819000e-25,
      1.250799735644947e-4};
    double y6[]={1.999950006838941e4,1.995039646421141e2,1.624838898635177,
      2.150981700693276e-5,3.839976000100000e12,3.837600999583593e7,
      3.609605896012407e2,5.754184998531225e-5,1.857940439048064e28,
      1.857429584630401e18,1.807132899010295e8,1.614255300390669e-3,
      6.377698248601134e62,6.376867526661186e42,6.294369360424536e22,
      1.787442782077055e2};
    double[] yy5=buildVector(y5),yy6=buildVector(y6),zz5=new double[N],zz6=new double[N];

    

    // Test Bessik (in)
    System.out.println("Testing Bessik (in)");

    localflag=false;
    sbeps=5.e-14;
    for (i=0;i<N;i++) zz5[i]=bess.in(n5[i],x5[i]);
    for (i=0;i<N;i++) {
//      System.out.printf(abs((zz5[i]-yy5[i])/yy5[i]));
      localflag = localflag || abs((zz5[i]-yy5[i])/yy5[i]) > sbeps;
    }

    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessik (in): Incorrect function values");
      
    }

    // Test Bessik (kn)
    System.out.println("Testing Bessik (kn)");
    localflag=false;
    sbeps=5.e-15;
    for (i=0;i<N;i++) zz6[i]=bess.kn(n5[i],x5[i]);
    for (i=0;i<N;i++) {
//      System.out.printf(abs((zz6[i]-yy6[i])/yy6[i]));
      localflag = localflag || abs((zz6[i]-yy6[i])/yy6[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessik (kn): Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
