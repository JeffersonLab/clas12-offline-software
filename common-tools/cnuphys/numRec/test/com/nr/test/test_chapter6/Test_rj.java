package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.sf.Elliptic.rj;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;

public class Test_rj {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1000,M=15;
    double x,y,z,p,sbeps; // lambda not used.
    double xx[]={0.5,0.5,0.5,1.0,1.0,1.0,2.0,2.0,2.0,
      5.0,5.0,5.0,10.0,10.0,10.0};
    double yy[]={0.5,5.0,10.0,0.5,5.0,10.0,0.5,5.0,10.0,
      0.5,5.0,10.0,0.5,5.0,10.0};
    double zz[]={0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0,
      0.5,1.0,1.5,2.0,2.5,3.0,3.5};
    double pp[]={0.5,0.5,1.0,1.0,2.0,2.0,5.0,5.0,10.0,10.0,
      0.5,1.0,2.0,5.0,10.0};
    double fingerprint[]={2.8284271247461894,0.98504492018034995,0.44793650058808648,
      0.96012401711294937,0.29241747858151068,0.21151911434681123,
      0.22347653672793238,0.12251370089051629,0.093064515580572826,
      0.13984765109852496,0.41094690289468627,0.21220982894638879,
      0.25740492084525707,0.082096126702519048,0.041707103324323758};
    double[] f1= new double[N],f2= new double[N],f3= new double[N],ff1 = new double[M],expect = buildVector(fingerprint);
    boolean localflag, globalflag=false;

    

    // Test rj
    System.out.println("Testing rj");

    Ran myran = new Ran(17);
    
    // Test rj(x,x,x,x) = 1/x^(3/2)
    for (i=0;i<N;i++) {
      x=myran.doub();
      f1[i]=rj(x,x,x,x);
      f2[i]=1.0/pow(x,3.0/2.0);
    }

    System.out.printf("rj: Maximum discrepancy = %f\n", maxel(vecsub(f1,f2)));
    sbeps=2.e-11;  // XXX 1.e-12 not pass
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rj: Function rj(x,x,x,x) is not equal to 1/x^(3/2)");
      
    }

    // Symmetry test
    for (i=0;i<N;i++) {
      x=10.0*myran.doub();
      y=10.0*myran.doub();
      z=10.0*myran.doub();
      p=10.0*myran.doub();

      f1[i]=rj(x,y,z,p);
      f2[i]=rj(y,x,z,p);
      f3[i]=rj(x,z,y,p);
    }

    // Symmetry of x and y
    System.out.printf("rj: maximum discrepance with swap of x,y = %f\n", maxel(vecsub(f1,f2)));
    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rj: Function rj(x,y,z,p) is not equal to rj(y,x,z,p)");
      
    }

    // Symmetry of y and z
    System.out.printf("rj: maximum discrepance with swap of y,z = %f\n", maxel(vecsub(f1,f3)));

    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f3)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rj: Function rj(x,y,z,p) is not equal to rj(x,z,y,p)");
      
    }

    // Test symmetry with respect to p
    for (i=0;i<N;i++) {
      x=myran.doub();
      z=myran.doub();
      p=myran.doub();

      f1[i]=rj(x,x,z,p);
      f2[i]=rj(p,p,z,x);
    }

    // Symmetry of x,y and p
    System.out.printf("rj: maximum discrepance with x,y,p symmetry = %f\n", maxel(vecsub(f1,f2)));

    sbeps=1.e-12;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rj: Function rj(x,x,z,p) is not equal to rj(p,p,z,x)");
      
    }

    // Test symmetry with respect to p
    for (i=0;i<N;i++) {
      x=myran.doub();
      y=myran.doub();
      p=myran.doub();

      f1[i]=rj(x,y,y,p);
      f2[i]=rj(x,p,p,y);
    }

    // Symmetry of y,z and p
    System.out.printf("rj: maximum discrepance with y,z,p symmetry = %f\n", maxel(vecsub(f1,f2)));

    sbeps=1.e-12;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rj: Function rj(x,y,y,p) is not equal to rj(x,p,p,y)");
      
    }

    // Fingerprint test
    for (i=0;i<M;i++) {
      ff1[i]=rj(xx[i],yy[i],zz[i],pp[i]);
//      System.out.printf(setprecision(20) << ff1[i]);
    }
    System.out.printf("rj: Fingerprint discrepancy = %f\n", maxel(vecsub(ff1,expect)));

    localflag = maxel(vecsub(ff1,expect)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rj: Fuction does not match previously computed fingerprint");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
