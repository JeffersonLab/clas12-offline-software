package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.sf.Elliptic.rc;
import static com.nr.sf.Elliptic.rf;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
public class Test_rc {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1000,M=15;
    double x,y,sbeps;
    double xx[]={0.5,0.5,0.5,1.0,1.0,1.0,2.0,2.0,2.0,
      5.0,5.0,5.0,10.0,10.0,10.0};
    double yy[]={0.5,5.0,10.0,0.5,5.0,10.0,0.5,5.0,10.0,
      0.5,5.0,10.0,0.5,5.0,10.0};
    double fingerprint[]={1.4142135623730949,0.58880582378346324,0.43646741450163296,
      1.246450480280461,0.55357435889704532,0.41634859079941822,
      1.0752916200648814,0.51157686594443497,0.39143618306709632,
      0.85722388169844077,0.44721359549995793,0.35124073655203636,
      0.70672482712931073,0.39416225082970491,0.31622776601683794};
    double[] f1= new double[N],f2= new double[N],ff1 = new double[M],expect = buildVector(fingerprint);
    boolean localflag, globalflag=false;

    

    // Test rc
    System.out.println("Testing rc");

    // Test values against those of rf(x,y,z);
    Ran myran = new Ran(17);
    
    for (i=0;i<N;i++) {
      x=10.0*myran.doub();
      y=10.0*myran.doub();

      f1[i]=rc(x,y);
      f2[i]=rf(x,y,y);
    }
    System.out.printf("rc: Maximum discrepancy = %f\n", maxel(vecsub(f1,f2)));

    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rc: Function rc(x,y) does not equal rf(x,y,y)");
      
    }

    // Fingerprint test
    for (i=0;i<M;i++)
      ff1[i]=rc(xx[i],yy[i]);
    System.out.printf("rc: Fingerprint discrepancy = %f\n", maxel(vecsub(ff1,expect)));

    localflag = maxel(vecsub(ff1,expect)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rc: Fuction does not match previously computed fingerprint");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
