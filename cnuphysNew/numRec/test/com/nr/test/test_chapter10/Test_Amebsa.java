package com.nr.test.test_chapter10;

import static com.nr.NRUtil.buildMatrix;
import static com.nr.NRUtil.buildVector;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.intW;

import com.nr.RealValueFun;
import com.nr.min.Amebsa;

public class Test_Amebsa {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean test;
    int i,j,NDIM=4,NMAX=200;
    intW iter = new intW(0);
    double temperature,sbeps,del=10.0,FTOL=1.0e-8;
    double ddels[]={5.0,2.0,6.0,3.0};
    double[] dels=buildVector(ddels);
    double pp[]={3.5,3.2,3.2,3.0,
      8.4,-0.5,0.7,0.5,
      0.7,5.6,0.1,0.0,
      -0.7,0.7,7.5,-0.2,
      0.2,0.2,-0.2,5.4};
    double[][] p=buildMatrix(NDIM+1,NDIM,pp);
    boolean localflag, globalflag=false;

    

    // Test Amebsa
    System.out.println("Testing Amebsa");

    // Test interface #1
    double[] point=buildVector(4,3.5);
    func_amebsa func_amebsa = new func_amebsa();
    Amebsa amb1 = new Amebsa(point,del,func_amebsa,FTOL);
    
    iter.val=1000;
    temperature=100.0;
    for (j=0;j<NMAX;j++) {
      test=amb1.anneal(iter,temperature);
      if (test) break;
      else {
        iter.val=1000;
        temperature *= 0.8;
      }
    }

//    System.out.printf(j);
//    System.out.printf(iter);
//    System.out.printf(abs(1.0-amb1.yb));
//    System.out.printf(amb1.pb[0] << " " << amb1.pb[1] << " " << amb1.pb[2] << " " << amb1.pb[3]);
//    System.out.printf(endl;

    sbeps=1.e-8;
    localflag = abs(1.0-amb1.yb) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amebsa, interface 1: Incorrect minimum function value");
      
    }

    sbeps=1.e-4;
    localflag = false;
    for (i=0;i<4;i++)
      localflag = localflag || abs(amb1.pb[i]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amebsa, interface 1: Did not converge to the global minumum at the origin");
      
    }

    // Test interface #2
    Amebsa amb2 = new Amebsa(point,dels,func_amebsa,FTOL);

    iter.val=1000;
    temperature=100.0;
    for (j=0;j<NMAX;j++) {
      test=amb2.anneal(iter,temperature);
      if (test) break;
      else {
        iter.val=1000;
        temperature *= 0.8;
      }
    }

//    System.out.printf(j);
//    System.out.printf(iter);
//    System.out.printf(amb2.yb);
//    System.out.printf(amb2.pb[0] << " " << amb2.pb[1] << " " << amb2.pb[2] << " " << amb2.pb[3]);
//    System.out.printf(endl;

    sbeps=1.e-8;
    localflag = abs(1.0-amb2.yb) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amebsa, interface 2: Incorrect minimum function value");
      
    }

    sbeps=1.e-4;
    localflag = false;
    for (i=0;i<4;i++)
      localflag = localflag || abs(amb2.pb[i]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amebsa, interface 2: Did not converge to the global minumum at the origin");
      
    }

    // Test interface #3

    Amebsa amb3 =new Amebsa(p,func_amebsa,FTOL);

    iter.val=1000;
    temperature=100.0;
    for (j=0;j<NMAX;j++) {
      test=amb3.anneal(iter,temperature);
      if (test) break;
      else {
        iter.val=1000;
        temperature *= 0.8;
      }
    }

//    System.out.printf(j);
//    System.out.printf(iter);
//    System.out.printf(amb3.yb);
//    System.out.printf(amb3.pb[0] << " " << amb3.pb[1] << " " << amb3.pb[2] << " " << amb3.pb[3]);
//    System.out.printf(endl;

    sbeps=1.e-8;
    localflag = abs(1.0-amb3.yb) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amebsa, interface 3: Incorrect minimum function value");
      
    }

    sbeps=1.e-4;
    localflag = false;
    for (i=0;i<4;i++)
      localflag = localflag || abs(amb3.pb[i]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amebsa, interface 3: Did not converge to the global minumum at the origin");
      
    }

    localflag = false;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Amebsa: **********************");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class func_amebsa implements RealValueFun {
    public double funk(double[] p){ 
    int j,N=4;
    double q,r,sumd=0.0,sumr=0.0,RAD=0.3,AUG=2.0;
    double wwid[]={1.0,3.0,10.0,30.0};
    double[] wid=buildVector(wwid);

    for (j=0;j<N;j++) {
      q=p[j]*wid[j];
      r=(q >= 0 ? (int)(q+0.5) : -(int)(0.5-q));
      sumr += q*q;
      sumd += (q-r)*(q-r);
    }
    return 1.0+sumr*(1.0+(sumd > RAD*RAD ? AUG : AUG*sumd/(RAD*RAD)));
  }
  }
}
