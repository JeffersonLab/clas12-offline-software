package com.nr.test.test_chapter21;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import com.nr.ran.*;

import com.nr.cg.*;


public class Test_incircle {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=1000;
    double cx,cy,radius,pio2,result,d,sbeps=4.e-13;
    Point a=new Point(2),b=new Point(2),c=new Point(2),test=new Point(2);
    double[] theta=new double[3];
    boolean localflag, globalflag=false;

    

    // Test incircle
    System.out.println("Testing incircle");

    // Points on circle at center=(0.5,0.5), radius=0.4
    Ran myran=new Ran(17);
    pio2=acos(-1.0);
    cx=0.5;
    cy=0.5;
    radius=0.4;

    for (i=0;i<N;i++) {
      theta[0]=pio2*(-1.0+2.0*myran.doub());
      theta[1]=pio2*(-1.0+2.0*myran.doub());
      theta[2]=pio2*(-1.0+2.0*myran.doub());

      a.x[0]=cx+radius*cos(theta[0]);
      a.x[1]=cy+radius*sin(theta[0]);

      b.x[0]=cx+radius*cos(theta[1]);
      b.x[1]=cy+radius*sin(theta[1]);

      c.x[0]=cx+radius*cos(theta[2]);
      c.x[1]=cy+radius*sin(theta[2]);

      test.x[0]=myran.doub();
      test.x[1]=myran.doub();

      result=Triel.incircle(test,a,b,c);
//      System.out.printf(result << " ";
      // Verify result
      d=SQR(test.x[0]-cx)+SQR(test.x[1]-cy);
//      System.out.printf(radius*radius-d);

      localflag = abs(result-(radius*radius-d)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** incircle: Incorrect returned value");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
