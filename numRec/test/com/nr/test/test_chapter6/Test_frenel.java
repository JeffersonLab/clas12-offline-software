package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.sf.Integrals.frenel;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;

public class Test_frenel {

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
    double xx[]={0.0,0.2,0.4,0.6,0.8,1.0,1.2,1.4,1.6,1.8,2.0,2.2,2.4,2.6,2.8,3.0};
    double cc[]={0.0,0.1999210575944531,0.3974807591723594,0.5810954469916523,
      0.7228441718963561,0.7798934003768228,0.7154377229230734,
      0.5430957835462564,0.3654616834404877,0.3336329272215571,
      0.4882534060753408,0.6362860449033195,0.5549614058564281,
      0.3889374961919690,0.4674916516989060,0.6057207892976856};
    double ss[]={0.0,0.004187609161656762,0.03335943266061318,0.1105402073593870,
      0.2493413930539178,0.4382591473903548,0.6234009185462497,
      0.7135250773634121,0.6388876835093809,0.4509387692675831,
      0.3434156783636982,0.4557046121246569,0.6196899649456836,
      0.5499893231527195,0.3915284435431718,0.4963129989673750};
    double[] x=buildVector(xx),c=buildVector(cc),s=buildVector(ss),zreal= new double[N],zimag= new double[N];
    Complex[] z= new Complex[N];
    boolean localflag, globalflag=false;

    

    // Test frenel
    System.out.println("Testing frenel");

    for (i=0;i<N;i++) {
      z[i]=frenel(x[i]);
      zreal[i]=z[i].re();
      zimag[i]=z[i].im();
    }
    System.out.printf("frenel: Maximum discrepancy in C(x) = %f\n", maxel(vecsub(zreal,c)));
    localflag = maxel(vecsub(zreal,c)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** frenel: Incorrect function values for C(x)");
      
    }

    System.out.printf("frenel: Maximum discrepancy in S(x) = %f\n", maxel(vecsub(zimag,s)));
    localflag = maxel(vecsub(zimag,s)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** frenel: Incorrect function values for S(x)");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
