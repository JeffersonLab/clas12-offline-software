package com.nr.test.test_chapter3;

import static com.nr.NRUtil.*;
import static com.nr.test.NRTestUtil.*;
import static java.lang.Math.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.interp.Curve_interp;

public class Test_Curve_interp {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps;
    int i,NDIM=3,NPTS=20;
    boolean close;
    double vec[]={2.0/sqrt(29.0),3.0/sqrt(29.0),4.0/sqrt(29.0)};
    double[] target = new double[NDIM];
    double[][] ptsin = new double[NPTS][NDIM];
    boolean localflag, globalflag=false;

    

    // Test Curve_interp
    System.out.println("Testing Curve_interp");

    // Test straight line
    for (i=0;i<NPTS;i++) {
      ptsin[i][0]=i*vec[0];
      ptsin[i][1]=i*vec[1];
      ptsin[i][2]=i*vec[2];
    }
    target[0]=2.5*vec[0];
    target[1]=2.5*vec[1];
    target[2]=2.5*vec[2];
//    System.out.printf(target[0] << " " << target[1] << " " << target[2] << endl;
    Curve_interp myCurve = new Curve_interp(ptsin);
    double[] f=myCurve.interp(2.5/(NPTS-1));  
//    System.out.printf(f[0]/target[0] << " " << f[1]/target[1] << " " << f[2]/target[2] << endl;
    sbeps=1.e-3;
    System.out.printf("     Discrepancy (straight line): %f\n", maxel(vecsub(f,target)));
    localflag = maxel(vecsub(f,target)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Curve_interp: Does not work for data on a 3D straight line.");
      
    }

    // Test closed circle
    double a[]={0.0,vec[2],-vec[1]};  // vec x (1,0,0)
    double norm=sqrt(SQR(vec[2])+SQR(vec[1]));  // normalize a[]
    for (i=0;i<NDIM;i++) a[i] /= norm;
    double b[]={vec[1]*a[2]-vec[2]*a[1],vec[2]*a[0]-vec[0]*a[2],vec[0]*a[1]-vec[1]*a[0]}; // perpendicular to a[] and vec[]
    double pi=acos(-1.0);
    double theta;
    for (i=0;i<NPTS;i++) {
      theta=i*2.0*pi/NPTS;
      ptsin[i][0]=a[0]*cos(theta)+b[0]*sin(theta);
      ptsin[i][1]=a[1]*cos(theta)+b[1]*sin(theta);
      ptsin[i][2]=a[2]*cos(theta)+b[2]*sin(theta);
    }
    theta=2.5*2.0*pi/NPTS;
    target[0]=a[0]*cos(theta)+b[0]*sin(theta);
    target[1]=a[1]*cos(theta)+b[1]*sin(theta);
    target[2]=a[2]*cos(theta)+b[2]*sin(theta);
//    System.out.printf(target[0] << " " << target[1] << " " << target[2] << endl;
    close=true;
    Curve_interp myCurve2 = new Curve_interp(ptsin,close);
    f=myCurve2.interp(2.5/NPTS);
//    System.out.printf(f[0]/target[0] << " " << f[1]/target[1] << " " << f[2]/target[2] << endl;
    sbeps=1.e-4;
    System.out.printf("     Discrepancy (circle): %f\n", maxel(vecsub(f,target)));
    localflag = maxel(vecsub(f,target)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Curve_interp: Does not work for data on a 3D circle.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
