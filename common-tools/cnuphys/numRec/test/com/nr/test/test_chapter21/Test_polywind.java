package com.nr.test.test_chapter21;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Point;
import com.nr.cg.Polygon;


public class Test_polywind {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nwind,N=14;
    double x0[]={-1.0,-1.0,1.0,1.0,-0.75,-0.75,0.75,0.75,-0.5,-0.5,0.5,0.5,0.0,0.0};
    double x1[]={0.0,1.0,1.0,-1.0,-1.0,0.75,0.75,-0.75,-0.75,0.5,0.5,-0.5,-0.5,0.0};
    Point test=new Point(2);
    Point[] poly=new Point[N];
    for(i=0;i<N;i++)
      poly[i]= new Point(2);
    boolean localflag, globalflag=false;

    // Test polywind
    System.out.println("Testing polywind (note: CW = Clockwise)");

    // Test a clockwise spiral
    for (i=0;i<N;i++) {
      poly[i].x[0]=x0[i];
      poly[i].x[1]=x1[i];
    }

    test.x[0]=0.0;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != -3);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CW): Incorrect winding number near center of spiral");
      
    }

    test.x[0]=0.6;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != -2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CW): Incorrect winding number inside second wind of spiral");
      
    }

    test.x[0]=0.9;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != -1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CW): Incorrect winding number inside first wind of spiral");
      
    }

    test.x[0]=1.1;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CW): Incorrect winding number outside of spiral");
      
    }

    for (i=0;i<N;i++) {
      poly[i].x[0]=x0[N-1-i];
      poly[i].x[1]=x1[N-1-i];
    }

    test.x[0]=0.0;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != 3);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CCW): Incorrect winding number near center of spiral");
      
    }

    test.x[0]=0.6;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != 2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CCW): Incorrect winding number inside second wind of spiral");
      
    }

    test.x[0]=0.9;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != 1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CCW): Incorrect winding number inside first wind of spiral");
      
    }

    test.x[0]=1.1;
    test.x[1]=0.25;
    nwind=Polygon.polywind(poly,test);
    localflag = (nwind != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** polywind (CCW): Incorrect winding number outside a spiral");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
