package com.nr.test.test_chapter21;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Point;

public class Test_Point {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean localflag, globalflag=false;

    

    // Test Point
    System.out.println("Testing Point");

    // Test constructor defaults for 1,2,3 dimensions
    Point x1= new Point(1);
    Point x2= new Point(2);
    Point x3= new Point(3);

    localflag = false;
    localflag = (x1.x[0] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Faulty default constructor for 1D");
      
    }

    localflag = false;
    localflag = (x2.x[0] != 0.0) || (x2.x[1] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Faulty default constructor for 2D");
      
    }

    localflag = false;
    localflag = (x3.x[0] != 0.0) || (x3.x[1] != 0.0) || (x3.x[2] != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Faulty default constructor for 3D");
      
    }

    Point x11=new Point(1,1.0);
    Point x21=new Point(1.0,1.0);
    Point x31=new Point(1.0,1.0,1.0);

    localflag = false;
    localflag = (x11.x[0] != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Faulty element assignments for 1D constructor");
      
    }

    localflag = false;
    localflag = (x21.x[0] != 1.0) || (x21.x[1] != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Faulty element assignments for 2D constructor");
      
    }

    localflag = false;
    localflag = (x31.x[0] != 1.0) || (x31.x[1] != 1.0) || (x31.x[2] != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Faulty element assignments for 3D constructor");
      
    }

    // Test assignment operator
    x1.copyAssign(x11);
    x2.copyAssign(x21);
    x3.copyAssign(x31);

    localflag = false;
    localflag = (x1.x[0] != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of assignment operator for 1D");
      
    }

    localflag = false;
    localflag = (x2.x[0] != 1.0) || (x2.x[1] != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of assignment operator for 2D");
      
    }

    localflag = false;
    localflag = (x3.x[0] != 1.0) || (x3.x[1] != 1.0) || (x3.x[2] != 1.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of assignment operator for 3D");
      
    }

    // Test boolean equality operator
    localflag = false;
    localflag = !(x11.equals(x1));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of boolean equality operator for 1D");
      
    }

    localflag = false;
    localflag = !(x21.equals(x2));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of boolean equality operator for 2D");
      
    }

    localflag = false;
    localflag = !(x31.equals(x3));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of boolean equality operator for 3D");
      
    } 

    // Test copy constructor
    Point y1=new Point(x1);
    Point y2=new Point(x2);
    Point y3=new Point(x3);

    localflag = false;
    localflag = !(y1.equals(x1));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of copy constructor for 1D");
      
    }

    localflag = false;
    localflag = !(y2.equals(x2));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of copy constructor for 2D");
      
    }

    localflag = false;
    localflag = !(y3.equals(x3));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Point: Failure of copy constructor for 3D");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
