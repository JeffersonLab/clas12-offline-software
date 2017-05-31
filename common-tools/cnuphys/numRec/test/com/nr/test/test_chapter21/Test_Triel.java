package com.nr.test.test_chapter21;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Point;
import com.nr.cg.Triel;
import com.nr.ran.Ran;


public class Test_Triel {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean test;
    int i,j,NPTS=15,NTRI=13,N=50;
    boolean localflag, globalflag=false;

    

    // Test Triel
    System.out.println("Testing Triel");

    // Conclass a tree of triangles
    // 0 -> 1,2,3 ; 1 -> 4,5,6 ; 2 -> 7,8,9 ; 3 -> 10,11,12

    Point []points=new Point[NPTS];
    for (i=0;i<NPTS;i++)
      points[i]=new Point((double)(i%2),(double)(i/2));

//    for (i=0;i<NPTS;i++)
//      System.out.printf(points[i].x[0] << " %f\n", points[i].x[1]);

    Triel[] triels=new Triel[NTRI];
    for(i=0;i<NTRI;i++)triels[i] = new Triel();
    for (i=0;i<NTRI;i++) {
      if (i%2 == 0)   // Note: the triangle must be in couterclockwise
        triels[i].setme(i,i+1,i+2,points);  // order in order for the
      else                  // method contains() to work
        triels[i].setme(i,i+2,i+1,points);  // properly
      for (j=0;j<3;j++)
        triels[i].d[j]=3*i+j+1;   // Indexes for daughter triangles
    }

//    Point<2> ptest;
//    for (i=0;i<NTRI;i++) {
//      ptest=points[triels[i].p[0]];
//      System.out.printf(ptest.x[0] << " %f\n", ptest.x[1] << "   ";
//      ptest=points[triels[i].p[1]];
//      System.out.printf(ptest.x[0] << " %f\n", ptest.x[1] << "   ";
//      ptest=points[triels[i].p[2]];
//      System.out.printf(ptest.x[0] << " %f\n", ptest.x[1] << "   ";
//      System.out.printf(endl;
//    }

    localflag=false;
    for (i=0;i<NTRI;i++)
      localflag = localflag || triels[i].stat != 1;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Triel: Status flags are improperly set");
      
    }

    // Track through some daughters
    localflag = triels[triels[0].d[2]].d[0] != 10;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Triel: Indexing of daughters is not working");
      
    }

//    Triel t=triels[triels[triels[0].d[2]].d[0]];
//    System.out.printf(t.pts[t.p[0]].x[0] << " ";
//    System.out.printf(t.pts[t.p[1]].x[0] << " ";
//    System.out.printf(t.pts[t.p[2]].x[0] << " ";
//    System.out.printf(endl;
//    System.out.printf(t.pts[t.p[0]].x[1] << " ";
//    System.out.printf(t.pts[t.p[1]].x[1] << " ";
//    System.out.printf(t.pts[t.p[2]].x[1] << " ";
//    System.out.printf(endl;


    // Test contains() method
    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      Point z=new Point(2.0*myran.doub(),6.0*myran.doub());
      // Points in the range (0-2.0,0-6.0).  Only the ones
      // with x coordinate between 0 and 1 are inside a triangle
      test=false;   // Test if point is inside a triangle
      for (j=0;j<NTRI;j++) 
        test = test || (triels[j].contains(z) > 0.0);
//      System.out.printf(z.x[0] << " %f\n", test);
      
      localflag = (test != (z.x[0] < 1.0));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Triel: A point that should be inside a tringle was identified as outside all of them");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
