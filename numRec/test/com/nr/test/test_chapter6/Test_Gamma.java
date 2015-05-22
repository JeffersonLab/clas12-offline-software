package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecadd;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Gamma;

public class Test_Gamma {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N;
    double sbeps;
    boolean localflag, globalflag=false;

    

    // Test Gamma
    System.out.println("Testing Gamma (gammp, gammq, gser, gcf, invgammp)");

    Gamma gam = new Gamma();

    // Test Gamma.gammp, Gamma.gammq (as well as gser, gcf)
    N=12;
    double a[]={0.5,0.5,0.5,1.0,1.0,1.0,3.0,3.0,3.0,10.0,10.0,10.0}; //,110.0,200.0
    double x[]={0.5,1.0,2.0,0.5,1.0,2.5,2.0,3.5,5.0,7.0,10.0,12.5}; //,100.0,220.0
    double y[]={6.826894921370857e-1,8.427007929497149e-1,9.544997361036406e-1,
      3.934693402873665e-1,6.321205588285578e-1,9.179150013761013e-1,
      3.233235838169363e-1,6.791528011378658e-1,8.753479805169189e-1,
      1.695040627613259e-1,5.420702855281473e-1,7.985688950544638e-1};
    double[] xx=buildVector(x),yy=buildVector(y),zz= new double[N],uu= new double[N],vv= new double[N],c=buildVector(N,1.);

    for (i=0;i<N;i++) {
      zz[i]=gam.gammp(a[i],x[i]);     // Test gammp
      uu[i]=gam.gammq(a[i],x[i]);     // Test gammq
      vv[i]=gam.invgammp(zz[i],a[i]);   // Test invgammp
    }

    System.out.printf("Gamma.gammp: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    sbeps=5.e-15;
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gamma.gammp: Incorrect function values");
      
    }

    System.out.printf("Gamma.gammq: Maximum discrepancy = %f\n", maxel(vecsub(vecadd(zz,uu),c)));
    sbeps=5.e-15;
    localflag = maxel(vecsub(vecadd(zz,uu),c)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gamma.gammq: gammp and gammq do not sum to 1.0");
      
    }

    System.out.printf("Gamma.invgammp: Maximum discrepancy = %f\n", maxel(vecsub(xx,vv)));
    sbeps=5.e-14;
    localflag = maxel(vecsub(xx,vv)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gamma.invgammp: Inverse does not return to original argument x[i]");
      
    }

    xx =new double[2];yy =new double[2];zz =new double[2];uu =new double[2];vv =new double[2];
    double[] aa = new double[2],cc=buildVector(2,1.0);
    aa[0]=110.0; aa[1]=200.0;
    xx[0]=100.0; xx[1]=210.0;
    yy[0]=1.705598979081085e-1; yy[1]=7.639696745011632e-1;
    zz[0]=gam.gammp(aa[0],xx[0]); zz[1]=gam.gammp(aa[1],xx[1]);
    uu[0]=gam.gammq(aa[0],xx[0]); uu[1]=gam.gammq(aa[1],xx[1]);
    vv[0]=gam.invgammp(zz[0],aa[0]); vv[1]=gam.invgammp(zz[1],aa[1]);

    System.out.printf("Gamma.gammp from gammpapprox: Maximum discrepancy = %f\n", maxel(vecsub(zz,yy)));
    sbeps=5.e-14;
    localflag = maxel(vecsub(zz,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gamma.gammp from gammpapprox: Incorrect function values");
      
    }

    System.out.printf("Gamma.gammq from gammpapprox: Maximum discrepancy = %f\n", maxel(vecsub(vecadd(zz,uu),cc)));
    sbeps=5.e-15;
    localflag = maxel(vecsub(vecadd(zz,uu),cc)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gamma.gammq from gammpapprox: gammp and gammq do not sum to 1.0");
      
    }

    System.out.printf("Gamma.invgammp: Maximum discrepancy = %f\n", maxel(vecsub(xx,vv)));
    sbeps=5.e-13;
    localflag = maxel(vecsub(xx,vv)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Gamma.invgammp with large a: Inverse does not return to original argument x[i]");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
