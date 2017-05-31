package com.nr.test.test_chapter10;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.RealValueFun;
import com.nr.min.Powell;
import com.nr.sf.Bessjy;

public class Test_Powell implements RealValueFun {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20;
    double theta,phi,f0,f,d0,d,pi=acos(-1.0);
    double sbeps1=5.0e-8,sbeps2=1.0e-15;
    double[] p=new double[3];
    double[][] ximat = new double[3][3];
    boolean localflag=false, globalflag=false;

    

    // Test Powell
    System.out.println("Testing Powell, interface1");

    Powell pow1 = new Powell(this);
    p[0]=p[1]=p[2]=0.0;
    p=pow1.minimize(p);
    f0=pow1.fret;
    d0=sqrt(SQR(p[0])+SQR(p[1])+SQR(p[2]));

    for (i=0;i<N;i++) {
      theta=pi*i/N;
      phi=pi*i/N;
      p[0]=sin(phi)*cos(theta);
      p[1]=sin(phi)*sin(theta);
      p[2]=cos(phi);
      p=pow1.minimize(p);
      f=pow1.fret;
      d=sqrt(SQR(p[0])+SQR(p[1])+SQR(p[2]));
//      System.out.printf(abs(d-d0) << " " << abs(f-f0));

      localflag = localflag || abs(d-d0) > sbeps1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Powell, interface1: First minimum of radial function reported at different radius for different starting points");
        
      }

      localflag = localflag || abs(f-f0) > sbeps2;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Powell, interface1: Reported function value at first minimum is different for different starting points");
        
      }
    }

    System.out.println("Testing Powell, interface2");
    Powell pow2 = new Powell(this);
    for (i=0;i<N;i++) {
      p[0]=0.1;
      p[1]=0.1;
      p[2]=-0.1;
      theta=pi*i/N;
      phi=pi*i/N;
      ximat[0][0]=sin(theta)*cos(phi);
      ximat[1][0]=sin(theta)*sin(phi);
      ximat[2][0]=cos(theta);
      ximat[0][1]=cos(theta)*cos(phi);
      ximat[1][1]=cos(theta)*sin(phi);
      ximat[2][1]=-sin(theta);
      ximat[0][2]=-sin(theta);
      ximat[1][2]=cos(theta);
      ximat[2][2]=0.0;
      p=pow2.minimize(p,ximat);
      f=pow2.fret;
      d=sqrt(SQR(p[0])+SQR(p[1])+SQR(p[2]));
//      System.out.printf(abs(d-d0) << " " << abs(f-f0));

      localflag = localflag || abs(d-d0) > sbeps1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Powell, interface2: First minimum of radial function reported at different radius for different xi[]");
        
      }

      localflag = localflag || abs(f-f0) > sbeps2;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Powell, interface2: Reported function value at first minimum is different for different xi[i]");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  public double funk(double[] x) {
    return Bessj0_Powell(x);
  }
  
  double Bessj0_Powell(double[] x) {
    Bessjy b = new Bessjy();
    return(b.j0(sqrt(SQR(x[0])+SQR(x[1])+SQR(x[2]))));
  }
}
