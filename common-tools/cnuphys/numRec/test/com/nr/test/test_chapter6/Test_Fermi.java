package com.nr.test.test_chapter6;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static com.nr.sf.Gamma.gammln;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Fermi;

public class Test_Fermi {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=35;
    double x,y,u,theta,arg,err,maxerr,sbeps;
    double k[]={-0.5,-0.5,-0.5,-0.5,-0.5,0.0,0.0,0.0,0.0,0.0,
      0.5,0.5,0.5,0.5,0.5,1.0,1.0,1.0,1.0,1.0,
      1.5,1.5,1.5,1.5,1.5,2.0,2.0,2.0,2.0,2.0,
      2.5,2.5,2.5,2.5,2.5};
    double eta[]={-4.0,-2.0,0.0,2.0,4.0,-4.0,-2.0,0.0,2.0,4.0,
      -4.0,-2.0,0.0,2.0,4.0,-4.0,-2.0,0.0,2.0,4.0,
      -4.0,-2.0,0.0,2.0,4.0,-4.0,-2.0,0.0,2.0,4.0,
      -4.0,-2.0,0.0,2.0,4.0};
    double llerchphi[]={0.98723954437218395070,0.91377221699828150158,0.60489864342163037025,
      0.19817072295601367000,0.040035599911294837599,0.99095248754732942653,
      0.93787819412221284129,0.69314718055994530942,0.28784840479838595436,
      0.073594983080535873137,0.99358824548351926869,0.95539396843037045842,
      0.76514702462540794537,0.38214911885839316878,0.119263520628480860315,
      0.99545798433477938115,0.96805859921517155120,0.82246703342411321824,
      0.47555757258932435467,0.17631919052265099404,0.99678355180598445962,
      0.97717889296141227425,0.86719988901218413819,0.56372754572416136561,
      0.24287431294359210786,0.99772287456821212285,0.98372520603432318369,
      0.90154267736969571405,0.64369983563986970788,0.31621358700726185813,
      0.99838823665214391464,0.98841134290237239108,0.92755357777394803511,
      0.71384242344355014277,0.39321310731185353334};
    double[] zz= new double[N],lerchphi=buildVector(llerchphi),expect= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Fermi
    System.out.println("Testing Fermi");

    // Test cases with theta=0
    // Fermi(k,eta,0)=exp(eta)*LerchPhi(exp(eta),k+1,1)
    Fermi dirac = new Fermi();

    for (i=0;i<N;i++) {
      zz[i]=dirac.val(k[i],eta[i],0.0);
      expect[i]=exp(gammln(k[i]+1.0))*exp(eta[i])*lerchphi[i];
//      System.out.printf(setw(15) << expect[i] << setw(15) << zz[i];
//      System.out.printf(setw(15) << (zz[i]/expect[i]-1.0));
    }

    sbeps=1.e-13;
    System.out.printf("Fermi: Maximum discrepancy = %f\n", maxel(vecsub(zz,expect)));
    localflag = maxel(vecsub(zz,expect)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fermi: Incorrect function values");
      
    }

    // Limiting cases for large eta and k=1/2
    maxerr=0.0;
    for (i=0;i<N;i++) {
      arg=1000.0;
      theta=0.1*(i+2);
      zz[i]=dirac.val(0.5,arg,theta);
      u=1.0+arg*theta;
      y=sqrt(SQR(u)-1.0);
      x=log((y+sqrt(SQR(y)+4.0))/2.0);
      expect[i]=(y*u-x)/pow(2.0*theta,1.5);
      err=abs(zz[i]/expect[i]-1.0);
      if (err > maxerr) maxerr=err;
//      System.out.printf(setw(15) << theta << setw(15) << expect[i] << setw(15) << zz[i];
//      System.out.printf(setw(15) << abs(zz[i]/expect[i]-1.0));
    }
    sbeps=1.e-4;
//    System.out.printf(maxerr);
    localflag = maxerr > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fermi: Incorrect dependence on theta for k=1/2 and large eta");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
