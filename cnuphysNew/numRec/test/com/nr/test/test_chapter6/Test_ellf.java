package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.sf.Elliptic.*;

public class Test_ellf {

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
    double ellf1[]={0.0,0.100167084547480182,0.201346823567727507,0.304603974401704104,
      0.411114219868596574,0.522238103278440330,0.639622515807764521,
      0.765350458597682954,0.902176698545870355,1.05392311374108558,
      1.22619117088351707,1.42776351721775348,1.67369924955824305,
      1.99339831974637447,2.45799559037297901,3.34067754279831100};
    double ellf2[]={0.523598776000000000,0.524508805698164762,0.527290159585071990,
      0.532106526205558691,0.539268044529137392,0.549306144797899353,
      0.563134596139227132,0.582430260118548007,0.610720592279546591,
      0.657851366229664988};
    double ellf3[]={0.0,0.0500836684734863288,0.1006775690197592557,0.152335159818448609,
      0.205707351112816793,0.261624740315344007,0.321243014549783015,
      0.386340279820085692,0.460042170380593855,0.549140322320619453,
      0.677417538203930387};
    double[] a= new double[N],b= new double[N],c=new double[M],d=new double[M];
    boolean localflag, globalflag=false;

    

    // Test ellf
    System.out.println("Testing ellf");

    // Test values vs. phi for k=1
    sbeps=5.e-15;
    for (i=0;i<N;i++) {
      a[i]=ellf(0.1*i,1.0);
      b[i]=ellf1[i];
    }
//    System.out.println("ellf: Maximum discrepancy = %f\n", maxel(vecsub(a,b)));
    localflag = maxel(vecsub(a,b)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellf: Incorrect function of phi for k=1");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg)
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=ellf(0.523598776,0.2*i);
      d[i]=ellf2[i];
    }
//    System.out.println("ellf: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellf: Incorrect function of k for phi=0.523598776 (30 deg)");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg)
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=ellf(0.05*i,2.0);
      d[i]=ellf3[i];
    }
//    System.out.println("ellf: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellf: Incorrect function of phi for k=2");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
