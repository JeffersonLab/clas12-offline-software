package com.nr.test.test_chapter6;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static com.nr.sf.Elliptic.sncndn;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_sncndn {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20;
    double sbeps=1.e-15;
    double u[]={0.1,0.2,0.5,1.0,2.0,0.1,0.2,0.5,1.0,2.0,0.1,0.2,0.5,1.0,2.0,4.0,-0.2,-0.5,-1.0,-2.0};
    double ksquare[]={0.0,0.0,0.0,0.0,0.0,0.5,0.5,0.5,0.5,0.5,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
    double y1[]={0.09983341664682815,0.1986693307950612,0.4794255386042030,
      0.8414709848078965,0.9092974268256817,0.09975068547462484,
      0.1980217429819704,0.4707504736556573,0.8030018248956439,
      0.9946623253580177,0.09966799462495582,0.1973753202249040,
      0.4621171572600098,0.7615941559557649,0.9640275800758169,
      0.9993292997390670,-0.1973753202249040,-0.4621171572600098,
      -0.7615941559557649,-0.9640275800758169};
    double y2[]={0.9950041652780258,0.9800665778412416,0.8775825618903727,
      0.5403023058681397,-0.4161468365471424,0.9950124626090582,
      0.9801976276784098,0.8822663948904403,0.5959765676721407,
      -0.1031836155277618,0.9950207489532265,0.9803279976447253,
      0.8868188839700739,0.6480542736638854,0.2658022288340797,
      0.03661899347368653,0.9803279976447253,0.8868188839700739,
      0.6480542736638854,0.2658022288340797};
    double y3[]={1.000000000000000,1.000000000000000,1.000000000000000,
      1.000000000000000,1.000000000000000,0.9975093485144243,
      0.9901483195224800,0.9429724257773857,0.8231610016315963,
      0.7108610477840873,0.9950207489532265,0.9803279976447253,
      0.8868188839700739,0.6480542736638854,0.2658022288340797,
      0.03661899347368653,0.9803279976447253,0.8868188839700739,
      0.6480542736638854,0.2658022288340797};
    double[] yy1=buildVector(y1),yy2=buildVector(y2),yy3=buildVector(y3),zz1= new double[N],zz2= new double[N],zz3= new double[N];
    double[] one=buildVector(N,1.0),test1= new double[N],test2= new double[N];
    boolean localflag, globalflag=false;

    

    // Test sncndn
    System.out.println("Testing sncndn");

    for (i=0;i<N;i++) {
      doubleW sn = new doubleW(zz1[i]);
      doubleW cn = new doubleW(zz2[i]);
      doubleW dn = new doubleW(zz3[i]);
      sncndn(u[i],1.0-ksquare[i],sn, cn, dn); // zz1[i],zz2[i],zz3[i]
      zz1[i] = sn.val; zz2[i] = cn.val; zz3[i] = dn.val;
      
      test1[i]=SQR(zz1[i])+SQR(zz2[i]);
      test2[i]=ksquare[i]*SQR(zz1[i])+ SQR(zz3[i]);
    }

    System.out.printf("test1: Maximum discrepancy = %f\n", maxel(vecsub(one,test1)));
    localflag = maxel(vecsub(one,test1)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sncndn: Test for relation between sn and cn failed");
      
    }

    System.out.printf("test2: Maximum discrepancy = %f\n", maxel(vecsub(one,test2)));
    localflag = maxel(vecsub(one,test2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sncndn: Test for relation between sn and dn failed");
      
    }

    System.out.printf("sncndn (sn): Maximum discrepancy = %f\n", maxel(vecsub(zz1,yy1)));
    localflag = maxel(vecsub(zz1,yy1)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sncndn: Incorrect function values for sn");
      
    }

    System.out.printf("sncndn (cn): Maximum discrepancy = %f\n", maxel(vecsub(zz2,yy2)));
    localflag = maxel(vecsub(zz2,yy2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sncndn: Incorrect function values for cn");
      
    }

    System.out.printf("sncndn (dn): Maximum discrepancy = %f\n", maxel(vecsub(zz3,yy3)));
    localflag = maxel(vecsub(zz3,yy3)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sncndn: Incorrect function values for dn");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
