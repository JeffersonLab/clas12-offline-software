package com.nr.test.test_chapter21;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Point;
import com.nr.cg.Polygon;

public class Test_ispolysimple {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,ntest,N1=14,N2=6,N3=8,N4=8;
    // Polygon 1, complex
    double x0[]={-1.0,-1.0,1.0,1.0,-0.75,-0.75,0.75,0.75,-0.5,-0.5,0.5,0.5,0.0,0.0};
    double x1[]={0.0,1.0,1.0,-1.0,-1.0,0.75,0.75,-0.75,-0.75,0.5,0.5,-0.5,-0.5,0.0};
    // Polygon 2, simple and convex
    double x2[]={-1.0,0.0,1.0,1.0,1.0,0.0};
    double x3[]={0.0,1.0,1.0,0.0,-1.0,-1.0};
    // Polygon 3, simple and concave
    double x4[]={-1.0,-0.5,0.0,0.5,1.0,0.5,0.0,-0.5};
    double x5[]={0.0,1.0,0.5,1.0,0.0,-1.0,-0.5,-1.0};
    // Polygon 4, complex
    double x6[]={-1.0,-0.5,0.0,0.5,1.0,0.5,0.0,-0.5};
    double x7[]={0.0,1.0,-0.1,1.0,0.0,-1.0,0.1,-1.0};
    Point[] poly1=new Point[N1],poly2=new Point[N2],poly3=new Point[N3],poly4=new Point[N4];
    for(i=0;i<N1;i++)
      poly1[i]= new Point(2);
    for(i=0;i<N2;i++)
      poly2[i]= new Point(2);
    for(i=0;i<N3;i++)
      poly3[i]= new Point(2);
    for(i=0;i<N4;i++)
      poly4[i]= new Point(2);
    

    boolean localflag, globalflag=false;

    

    // Test ispolysimple
    System.out.println("Testing ispolysimple (note: CW = Clockwise)");

    // Test a clockwise spiral (Polygon 1)
    for (i=0;i<N1;i++) {
      poly1[i].x[0]=x0[i];
      poly1[i].x[1]=x1[i];
    }
    ntest=Polygon.ispolysimple(poly1);
//    System.out.printf(ntest);
    localflag = (ntest != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CW): Incorrect identification of a complex polygon");
      
    }

    // Test a counterclockwise spiral (Polygon 1)
    for (i=0;i<N1;i++) {
      poly1[i].x[0]=x0[N1-1-i];
      poly1[i].x[1]=x1[N1-1-i];
    }
    ntest=Polygon.ispolysimple(poly1);
//    System.out.printf(ntest);
    localflag = (ntest != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CCW): Incorrect identification of a complex polygon");
      
    }

    // Test a clockwise, simple convex polygon (Polygon 2)
    for (i=0;i<N2;i++) {
      poly2[i].x[0]=x2[i];
      poly2[i].x[1]=x3[i];
    }
    ntest=Polygon.ispolysimple(poly2);
//    System.out.printf(ntest);
    localflag = (ntest != -1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CW): Incorrect identification of a simple convex polygon");
      
    }

    // Test a counterclockwise, simple convex polygon (Polygon 2)
    for (i=0;i<N2;i++) {
      poly2[i].x[0]=x2[N2-1-i];
      poly2[i].x[1]=x3[N2-1-i];
    }
    ntest=Polygon.ispolysimple(poly2);
//    System.out.printf(ntest);
    localflag = (ntest != 1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CCW): Incorrect identification of a simple convex polygon");
      
    }

    // Test a clockwise simple concave polygon (Polygon 3)
    for (i=0;i<N3;i++) {
      poly3[i].x[0]=x4[i];
      poly3[i].x[1]=x5[i];
    }
    ntest=Polygon.ispolysimple(poly3);
//    System.out.printf(ntest);
    localflag = (ntest != -2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CW): Incorrect identification of a simple concave polygon");
      
    }

    // Test a counterclockwise simple concave polygon (Polygon 3)
    for (i=0;i<N3;i++) {
      poly3[i].x[0]=x4[N3-1-i];
      poly3[i].x[1]=x5[N3-1-i];
    }
    ntest=Polygon.ispolysimple(poly3);
//    System.out.printf(ntest);
    localflag = (ntest != 2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CCW): Incorrect identification of a simple concave polygon");
      
    }

    // Test another clockwise complex polygon (Polygon 4)
    for (i=0;i<N4;i++) {
      poly4[i].x[0]=x6[i];
      poly4[i].x[1]=x7[i];
    }
    ntest=Polygon.ispolysimple(poly4);
//    System.out.printf(ntest);
    localflag = (ntest != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CW): Incorrect identification of a second complex polygon");
      
    }

    // Test another counterclockwise complex polygon (Polygon 4)
    for (i=0;i<N4;i++) {
      poly4[i].x[0]=x6[N4-1-i];
      poly4[i].x[1]=x7[N4-1-i];
    }
    ntest=Polygon.ispolysimple(poly4);
//    System.out.printf(ntest);
    localflag = (ntest != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ispolysimple (CCW): Incorrect identification of a second complex polygon");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
