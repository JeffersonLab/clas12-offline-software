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
import org.netlib.util.doubleW;
import org.netlib.util.intW;

import com.nr.RealValueFunWithDiff;
import com.nr.min.QuasiNewton;
import com.nr.sf.Bessjy;

public class Test_dfpmin {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20;
    intW iter = new intW(0);
    doubleW f0=new doubleW(0),f=new doubleW(0);
    double theta,phi,d0,d,pi=acos(-1.0);
    double sbeps1=5.0e-8,sbeps2=5.0e-16,gtol=5.0e-8;
    double[] p = new double[3];
    boolean localflag=false, globalflag=false;

    

    // Test dfpmin
    System.out.println("Testing dfpmin");

    Funcd_dfpmin fd = new Funcd_dfpmin();
    p[0]=p[1]=p[2]=0.5;
    QuasiNewton.dfpmin(p,gtol,iter,f0,fd);
    d0=sqrt(SQR(p[0])+SQR(p[1])+SQR(p[2]));

    for (i=0;i<N;i++) {
      theta=pi*i/N;
      phi=pi*i/N;
      p[0]=sin(phi)*cos(theta);
      p[1]=sin(phi)*sin(theta);
      p[2]=cos(phi);
      QuasiNewton.dfpmin(p,gtol,iter,f,fd);
      d=sqrt(SQR(p[0])+SQR(p[1])+SQR(p[2]));
//      System.out.println("     " << abs(d-d0) << " " << abs(f-f0));

      localflag = localflag || abs(d-d0) > sbeps1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** dfpmin: First minimum of radial function reported at different radius for different starting points");
        
      }

      localflag = localflag || abs(f.val-f0.val) > sbeps2;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** dfpmin: Reported function value at first minimum is different for different starting points");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Funcd_dfpmin implements RealValueFunWithDiff{
    Bessjy b = new Bessjy();
    double u;
    public double funk (double[] x) {
      return(b.j0(sqrt(SQR(x[0])+SQR(x[1])+SQR(x[2]))));
    }
    public void df(double[] x, double[] deriv) {
      u=sqrt(SQR(x[0])+SQR(x[1])+SQR(x[2]));
      deriv[0]= -x[0]*b.j1(u)/u;
      deriv[1]= -x[1]*b.j1(u)/u;
      deriv[2]= -x[2]*b.j1(u)/u;
    }
  }
}
