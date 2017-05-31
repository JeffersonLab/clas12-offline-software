package com.nr.test.test_chapter21;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Circle;
import com.nr.cg.Point;

public class Test_Circle {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    Point center=new Point(2);
    double radius,sbeps=1.e-16;
    boolean localflag, globalflag=false;

    

    // Test Circle
    System.out.println("Testing Circle");

    center.x[0]=1.5;
    center.x[1]=2.5;
    radius=3.5;

    Circle cir = new Circle(center,radius);

    localflag = Point.dist(center,cir.center) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Circle: Circle does not have correct center");
      
    }

    sbeps=1.0e-15;
    localflag = abs(radius-cir.radius) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Circle: Circle does not have correct radius");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
