package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.sf.Elliptic.rd;
import static com.nr.sf.Elliptic.rj;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;

public class Test_rd {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1000,M=15;
    double x,y,z,sbeps;
    double xx[]={0.5,0.5,0.5,1.0,1.0,1.0,2.0,2.0,2.0,
      5.0,5.0,5.0,10.0,10.0,10.0};
    double yy[]={0.5,5.0,10.0,0.5,5.0,10.0,0.5,5.0,10.0,
      0.5,5.0,10.0,0.5,5.0,10.0};
    double zz[]={0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0,
      0.5,1.0,1.5,2.0,2.5,3.0,3.5};
    double fingerprint[]={2.828427124746189,0.6761446800665275,0.3561311564429841,
      0.61767396750725501,0.25585876344175834,0.16707403783066954,
      0.28683033981461781,0.14094109198361518,0.57990472541731197,
      0.6761446800665275,0.24574380041928576,0.1516469228750387,
      0.22495805526027765,0.10980936208850103,0.076942538498807111};
    double[] f1= new double[N],f2= new double[N],ff1 = new double[M],expect = buildVector(fingerprint);
    boolean localflag, globalflag=false;

    

    // Test rd
    System.out.println("Testing rd");

    // Test values against those of rj(x,y,z);
    Ran myran = new Ran(17);
    
    for (i=0;i<N;i++) {
      x=10.0*myran.doub();
      y=10.0*myran.doub();
      z=10.0*myran.doub();

      f1[i]=rd(x,y,z);
      f2[i]=rj(x,y,z,z);
    }
    System.out.printf("rd: Maximum discrepancy = %f\n", maxel(vecsub(f1,f2)));

    sbeps=1.e-14;
    localflag = maxel(vecsub(f1,f2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rd: Function rd(x,y,z) does not equal rf(x,y,z,z)");
      
    }

    // Fingerprint test
    for (i=0;i<M;i++) {
      ff1[i]=rd(xx[i],yy[i],zz[i]);
//      System.out.printf(setprecision(20) << ff1[i]);
    }
    System.out.printf("rd: Fingerprint discrepancy = %f\n", maxel(vecsub(ff1,expect)));

    localflag = maxel(vecsub(ff1,expect)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** rd: Fuction does not match previously computed fingerprint");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
