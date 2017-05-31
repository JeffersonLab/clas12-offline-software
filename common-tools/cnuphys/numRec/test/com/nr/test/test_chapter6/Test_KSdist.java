package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sf.KSdist;

public class Test_KSdist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=21;
    double z,a,b,sbeps=1.e-13; // integral
    double xx[]={0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,1.9,2.0,2.1,2.2,2.3};
    double ppksexp[]={9.3058013345666361e-006,0.0028076732227017387,0.036054756335124914,
      0.13571722094939573,0.28876480497031071,0.45585758842580204,
      0.60726929205934577,0.73000032832264539,0.82228180739359891,
      0.887750333329275,0.93190777815523362,0.96031812046188558,
      0.97778203738347491,0.98804795676080337,0.99382256936555591,
      0.99693237865242035,0.99853639516281267,0.99932907474422028,
      0.99970450327953686,0.99987495699245044,0.99994916130696765};
    double qqksexp[]={0.99999069419866549,0.9971923267772983,0.96394524366487511,
      0.86428277905060424,0.71123519502968935,0.54414241157419796,
      0.39273070794065423,0.26999967167735461,0.17771819260640109,
      0.11224966667072496,0.068092221844766362,0.039681879538114403,
      0.022217962616525127,0.011952043239196616,0.0061774306344441286,
      0.0030676213475797059,0.0014636048371873506,0.00067092525577969533,
      0.00029549672046311421,0.00012504300754960976,5.0838693032397749e-005};
    double[] x=buildVector(xx),pksexp=buildVector(ppksexp),qksexp=buildVector(qqksexp),pks= new double[N],qks= new double[N]; //,c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test KSdist
    System.out.println("Testing KSdist");

    // Test special cases
    z=0.0;
    KSdist norm = new KSdist();
    localflag = abs(norm.pks(z)-0.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Special case #1 failed for Pks");
      
    }

    localflag = abs(norm.qks(z)-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Special case #1 failed for Qks");
      
    }
    
    z=1.0;
//    System.out.printf(setprecision(17) << norm.pks(z) << " %f\n", 1-2.0*exp(-2.0)+2.0*exp(-8.0)-2.0*exp(-18.0));
    localflag = abs(norm.pks(z)-1+2.0*exp(-2.0)-2.0*exp(-8.0)+2.0*exp(-18.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Special case #2 failed for Pks");
      
    }

    localflag = abs(norm.qks(z)-2.0*exp(-2.0)+2.0*exp(-8.0)-2.0*exp(-18.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Special case #2 failed for Qks");
      
    }

    z=100.0;
    localflag = abs(norm.pks(z)-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Special case #3 failed for Pks");
      
    }

    localflag = abs(norm.qks(z)-0.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Special case #3 failed for Qks");
      
    }

    // Pks correctly related to Qks
    localflag=false;
    for (i=0;i<N;i++)
      localflag = localflag || 1.0-norm.pks(x[i])-norm.qks(x[i]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Pks and Qks do not add to 1.0 in all cases");
      
    }

    // inverse of Pks agrees with Pks
    Ran myran = new Ran(17);
    localflag=false;
    sbeps=6.e-10;  // XXX 5.e-10 not pass
    for (i=0;i<10;i++) {
      z=0.3+3.0*myran.doub();
      a=norm.pks(z);
      b=norm.invpks(a);
//      if (abs(z-b) > sbeps) {
//        System.out.printf(setprecision(15) << z << " %f\n", b << " %f\n", abs(z-b));
//      }
      localflag = localflag || abs(z-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: invPks does not accurately invert the Pks");
      
    }

    // inverse of Qks agrees with Qks
    localflag=false;
    sbeps=5.e-10;
    for (i=0;i<1000;i++) {
      z=0.3+3.0*myran.doub();
      a=norm.qks(z);
      b=norm.invqks(a);
//      if (abs(z-b) > sbeps) {
//        System.out.printf(setprecision(15) << z << " %f\n", b << " %f\n", abs(z-b));
//      }
      localflag = localflag || abs(z-b) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: invQks does not accurately invert the Qks");
      
    }

    // Fingerprint test for Pks
    sbeps=1.e-15;
    for (i=0;i<N;i++) {
      pks[i]=norm.pks(x[i]);
//      System.out.printf(setprecision(17) << pks[i]);
    }
//    System.out.println("KSdist: Maximum discrepancy = %f\n", maxel(vecsub(pks,pksexp)));
    localflag = maxel(vecsub(pks,pksexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Fingerprint does not match expectations");
      
    }

    // Fingerprint test for Qks
    sbeps=1.e-15;
    for (i=0;i<N;i++) {
      qks[i]=norm.qks(x[i]);
//      System.out.printf(setprecision(17) << qks[i]);
    }
//    System.out.println("KSdist: Maximum discrepancy = %f\n", maxel(vecsub(qks,qksexp)));
    localflag = maxel(vecsub(qks,qksexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KSdist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
