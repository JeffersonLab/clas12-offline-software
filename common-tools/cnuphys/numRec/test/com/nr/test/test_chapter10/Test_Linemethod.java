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
import com.nr.min.Linemethod;
import com.nr.sf.Bessjy;

public class Test_Linemethod implements RealValueFun{

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
    double[] p=new double[3],xi=new double[3];
    boolean localflag=false, globalflag=false;

    

    // Test Linemethod
    System.out.println("Testing Linemethod");
    Linemethod line=new Linemethod(this);

    p[0]=p[1]=p[2]=0.0;
    xi[0]=1.0;
    xi[1]=xi[2]=0.0;
    line.p=p;
    line.xi=xi;
    f0=line.linmin();
    d0=sqrt(SQR(line.p[0])+SQR(line.p[1])+SQR(line.p[2]));

    for (i=0;i<N;i++) {
      p[0]=0.1;
      p[1]=0.1;
      p[2]=-0.1;
      theta=pi*i/N;
      phi=pi*i/N;
      xi[0]=sin(phi)*cos(theta);
      xi[1]=sin(phi)*sin(theta);
      xi[2]=cos(phi);
      line.p=p;
      line.xi=xi;
      f=line.linmin();
      d=sqrt(SQR(line.p[0])+SQR(line.p[1])+SQR(line.p[2]));
//      System.out.printf(abs(d-d0) << " " << abs(f-f0));

      localflag = localflag || abs(d-d0) > sbeps1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Linemethod: First minimum of radial function reported at different radius for different xi[]");
        
      }

      localflag = localflag || abs(f-f0) > sbeps2;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Linemethod: Reported function value at first minimum is different for different xi[i]");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public double funk(final double[] x) {
    Bessjy b = new Bessjy();
    return(b.j0(sqrt(SQR(x[0])+SQR(x[1])+SQR(x[2]))));
  }

}
