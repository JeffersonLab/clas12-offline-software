package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.sf.Elliptic.rf;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
public class Test_rf {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1000,M=15;
    double x,y,z,lambda,sbeps;
    double xx[]={0.5,0.5,0.5,1.0,1.0,1.0,2.0,2.0,2.0,
      5.0,5.0,5.0,10.0,10.0,10.0};
    double yy[]={0.5,5.0,10.0,0.5,5.0,10.0,0.5,5.0,10.0,
      0.5,5.0,10.0,0.5,5.0,10.0};
    double zz[]={0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0,
      0.5,1.0,1.5,2.0,2.5,3.0,3.5};
    double fingerprint[]={1.4142135623730949,0.78318403980915063,0.61671477142548603,
      0.96885765327245232,0.62910156611289225,0.52066873070659714,
      0.76120858506833289,0.532702194896431,0.59067521087321406,
      0.78318403980915063,0.52979548043855929,0.44484198345350334,
      0.56995656023418362,0.42344785821702513,0.36781365041076669};
    double[] f1= new double[N],f2= new double[N],f3= new double[N],ff1 = new double[M],expect = buildVector(fingerprint);
    boolean localflag, globalflag=false;

    

    // Test rf
    System.out.println("Testing rf");

    Ran myran = new Ran(17);
    
    for (i=0;i<N;i++) {
      x=10.0*myran.doub();
      y=10.0*myran.doub();
      z=10.0*myran.doub();

      f1[i]=rf(x,y,z);
      lambda=sqrt(x*y)+sqrt(x*z)+sqrt(y*z);
      f2[i]=2.0*rf(x+lambda,y+lambda,z+lambda);
      f3[i]=rf((x+lambda)/4.0,(y+lambda)/4.0,(z+lambda)/4.0);
//      System.out.printf(f1[i] << " %f\n", f3[i]);
    }
    System.out.printf("rf: Rule 1, maximum discrepancy = %f\n", maxel(vecsub(f1,f2)));
    System.out.printf("rf: Rule 2, maximum discrepancy = %f\n", maxel(vecsub(f1,f3)));

    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rf: Function rf(x,y,z) does not follow duplication theorem rule 1");
      
    }

    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f3)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rf: Function rf(x,y,z) does not follow duplication theorem rule 2");
      
    }

    // Test rf(x,x,x) = 1/sqrt(x)
    for (i=0;i<N;i++) {
      x=myran.doub();
      f1[i]=rf(x,x,x);
      f2[i]=1.0/sqrt(x);
    }

    System.out.printf("rf: Rule 3: maximum discrepancy = %f\n", maxel(vecsub(f1,f2)));
    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rf: Function rf(x,x,x) is not equal to 1/sqrt(x)");
      
    }

    // Symmetry test
    for (i=0;i<N;i++) {
      x=10.0*myran.doub();
      y=10.0*myran.doub();
      z=10.0*myran.doub();

      f1[i]=rf(x,y,z);
      f2[i]=rf(y,x,z);
      f3[i]=rf(x,z,y);
    }

    // Symmetry of x and y
    System.out.printf("rf: maximum discrepance with swap of x,y = %f\n", maxel(vecsub(f1,f2)));
    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rf: Function rf(x,y,z) is not equal to rf(y,x,z)");
      
    }

    // Symmetry of y and z
    System.out.printf("rf: maximum discrepance with swap of y,z = %f\n", maxel(vecsub(f1,f3)));

    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f3)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rf: Function rf(x,y,z) is not equal to rf(x,z,y)");
      
    }

    // Fingerprint test
    for (i=0;i<M;i++) {
      ff1[i]=rf(xx[i],yy[i],zz[i]);
//      System.out.printf(setprecision(20) << ff1[i]);
    }
    System.out.printf("rf: Fingerprint discrepancy = %f\n", maxel(vecsub(ff1,expect)));

    localflag = maxel(vecsub(ff1,expect)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rf: Fuction does not match previously computed fingerprint");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
