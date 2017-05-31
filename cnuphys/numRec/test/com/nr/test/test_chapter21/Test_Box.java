package com.nr.test.test_chapter21;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Box;
import com.nr.cg.Point;

public class Test_Box {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    Point zero2=new Point(0.0,0.0),one2=new Point(1.0,1.0);
    Point zero3=new Point(0.0,0.0,0.0),one3=new Point(1.0,1.0,1.0);
    boolean localflag, globalflag=false;

    

    // Test Box
    System.out.println("Testing Box");

    // Test constructor defaults for 1,2,3 dimensions
    Box b2 = new Box(2);
    Box b3 = new Box(3);

    localflag = false;
    localflag = !(b2.lo.equals(zero2)) || !(b2.hi.equals(zero2));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Faulty default constructor for 2D");
      
    }

    localflag = false;
    localflag = !(b3.lo.equals(zero3)) || !(b3.hi.equals(zero3));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Faulty default constructor for 3D");
      
    }

    // Test constructor with arguments
    Box c2=new Box(zero2,one2);
    Box c3=new Box(zero3,one3);

    localflag = false;
    localflag = !(c2.lo.equals(zero2)) || !(c2.hi.equals(one2));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Faulty assignment of lo or hi for 2D constructor");
      
    }

    localflag = false;
    localflag = !(c3.lo.equals(zero3)) || !(c3.hi.equals(one3));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Faulty assignment of lo or hi for 3D constructor");
      
    }

    // Test assignment operator
    b2= b2.copyAssign(c2);
    b3=b3.copyAssign(c3);

    localflag = false;
    localflag = !(b2.lo.equals(zero2)) || !(b2.hi.equals(one2));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Failure of assignment operator for 2D");
      
    }

    localflag = false;
    localflag = !(b3.lo.equals(zero3)) || !(b3.hi.equals(one3));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Failure of assignment operator for 3D");
      
    }

    // Test copy constructor

    Box d2 = new Box(c2);
    Box d3 = new Box(c3);

    localflag = false;
    localflag = !(d2.lo.equals(c2.lo)) || !(d2.hi.equals(c2.hi));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Failure of copy constructor for 2D");
      
    }

    localflag = false;
    localflag = !(d3.lo.equals(c3.lo)) || !(d3.hi.equals(c3.hi));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Box: Failure of copy constructor for 3D");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
