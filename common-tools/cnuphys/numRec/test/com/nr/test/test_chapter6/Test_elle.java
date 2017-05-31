package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.sf.Elliptic.*;

public class Test_elle {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=16,M=10;
    double sbeps;
    double elle1[]={0.0,0.0998334166468281523,0.198669330795061215,0.295520206661339575,
      0.389418342308650492,0.479425538604203000,0.564642473395035357,
      0.644217687237691054,0.717356090899522762,0.783326909627483388,
      0.841470984807896507,0.891207360061435340,0.932039085967226350,
      0.963558185417192965,0.985449729988460181,0.997494986604054431};
    double elle2[]={0.523598776000000000,0.522691528960261784,0.519952907231629841,
      0.515330345767519975,0.508729236913189576,0.500000000347883381,
      0.488913351004473020,0.475110551307264258,0.457989419291834486,
      0.436367226265627954};
    double elle3[]={0.0,0.0499165830243987898,0.0993306265758595741,0.147729048734116570,
      0.194575897058209209,0.239295761817418877,0.281248556340456227,
      0.319686389142797354,0.353669004307650793,0.381861529207714857,
      0.401819480553494865};
    double[] a= new double[N],b= new double[N],c=new double[M],d=new double[M];
    boolean localflag, globalflag=false;

    

    // Test elle
    System.out.println("Testing elle");

    // Test values vs. phi for k=1
    sbeps=5.e-15;
    for (i=0;i<N;i++) {
      a[i]=elle(0.1*i,1.0);
      b[i]=elle1[i];
    }
//    System.out.println("elle: Maximum discrepancy = %f\n", maxel(vecsub(a,b)));
    localflag = maxel(vecsub(a,b)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** elle: Incorrect function of phi for k=1");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg)
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=elle(0.523598776,0.2*i);
      d[i]=elle2[i];
    }
//    System.out.println("elle: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** elle: Incorrect function of k for phi=0.523598776 (30 deg)");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg)
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=elle(0.05*i,2.0);
      d[i]=elle3[i];
    }
//    System.out.println("elle: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** elle: Incorrect function of phi for k=2");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
