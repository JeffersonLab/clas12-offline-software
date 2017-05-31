package com.nr.test.test_chapter21;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import com.nr.ran.*;

import com.nr.cg.*;
import static com.nr.cg.Circle.*;


public class Test_circumcircle {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10;
    double sbeps=1.e-15;
    Point a =new Point(2),b =new Point(2),c =new Point(2);
    Circle cir=new Circle(a,0.0);  // placeholder
    double[] d=new double[3];
    boolean localflag, globalflag=false;

    

    // Test circumcircle
    System.out.println("Testing circumcircle");

    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      a.x[0]=myran.doub();
      a.x[1]=myran.doub();
      b.x[0]=myran.doub();
      b.x[1]=myran.doub();
      c.x[0]=myran.doub();
      c.x[1]=myran.doub();

      cir=circumcircle(a,b,c);

      // Test whether the points are on this circle
      // and whether radius is correct
      d[0]=Point.dist(a,cir.center)-cir.radius;
      d[1]=Point.dist(b,cir.center)-cir.radius;
      d[2]=Point.dist(c,cir.center)-cir.radius;

      localflag = maxel(d) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** circumcircle: At least one point is not on supposed circumcircle");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
