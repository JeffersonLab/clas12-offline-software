package com.nr.test.test_chapter6;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;
import static com.nr.NRUtil.*;
import static com.nr.sf.Integrals.*;

public class Test_cisi {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=16;
    double sbeps=1.0e-15;
    Complex zz1,zz2;
    double x[]={0.5,0.6,0.7,0.8,0.9,1.0,1.2,1.4,1.6,1.8,2.0,2.5,3.0,3.5,4.0,4.5};
    double u[]={-0.1777840788066129,-0.02227070695927976,0.1005147070088978,
      0.1982786159524672,0.2760678304677729,0.3374039229009681,
      0.4204591828942405,0.4620065850946773,0.4717325169318778,
      0.4568111294183369,0.4229808287748650,0.2858711963653835,
      0.1196297860080003,-0.03212854851248112,-0.1409816978869304,
      -0.1934911221017388};
    double v[]={0.4931074180430667,0.5881288096080801,0.6812222391166113,
      0.7720957854819966,0.8604707107452929,0.9460830703671830,
      1.108047199013719,1.256226732779218,1.389180485870438,
      1.505816780255579,1.605412976802695,1.778520173443827,
      1.848652527999468,1.833125398665997,1.758203138949053,
      1.654140414379244};
    double[] uu=buildVector(u),vv=buildVector(v),zreal = new double[N],zimag = new double[N];
    Complex[] zz = new Complex[N];
    boolean localflag=false, globalflag=false;

    

    // Test cisi
    System.out.println("Testing cisi");

    for (i=0;i<N;i++) {
      zz[i]=cisi(x[i]);
      zreal[i]=zz[i].re();
      zimag[i]=zz[i].im();
    }
    System.out.printf("cisi: Maximum discrepancy of cosine integral = %f\n", maxel(vecsub(zreal,uu)));
    localflag = maxel(vecsub(zreal,uu)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cisi: Incorrect function values for cosine integral");
      
    }

    System.out.printf("cisi: Maximum discrepancy of sine integral = %f\n", maxel(vecsub(zimag,vv)));
    localflag = maxel(vecsub(zimag,vv)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cisi: Incorrect function values for sine integral");
      
    }

    // Test symmetries
    for (i=0;i<N;i++) {
      zz1=cisi(x[i]);
      zz2=cisi(-x[i]);
      localflag = localflag || zz1.re() != zz2.re();
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** cisi: Incorrect symmetry for cosine integral");
        
      }

      localflag = localflag || zz1.im() != -zz2.im();
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** cisi: Incorrect symmetry for sine integral");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
