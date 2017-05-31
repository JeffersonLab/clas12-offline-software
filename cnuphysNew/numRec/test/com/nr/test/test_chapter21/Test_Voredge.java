package com.nr.test.test_chapter21;

import static com.nr.cg.Point.dist;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Point;
import com.nr.cg.Voronoi;
import com.nr.ran.Ran;



public class Test_Voredge {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,NPTS=3;
    double sbeps=1.e-15;
    boolean localflag, globalflag=false;

    

    // Test Voredge
    System.out.println("Testing Voredge");

    Ran myran=new Ran(17);
    Point[] points=new Point[NPTS];
    for (i=0;i<NPTS;i++)
      points[i]=new Point(myran.doub(),myran.doub());

    Voronoi.Voredge v = new Voronoi.Voredge(points[0],points[1],2);

    localflag = dist(v.p[0],points[0]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Voredge: First endpoint of edge improperly initialized");
      
    }

    localflag = dist(v.p[1],points[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Voredge: Second endpoint of edge improperly initialized");
      
    }

    localflag = v.nearpt != 2;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Voredge: integer pointer to nearby point is improperly initialized");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
