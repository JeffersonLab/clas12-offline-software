package com.nr.test.test_chapter21;

import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Convexhull;
import com.nr.cg.Point;
import com.nr.cg.Polygon;
import com.nr.ran.Ran;


public class Test_Convexhull {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,nwind,N=10000;
    Point p=new Point(2);
    Point[] pvec = new Point[N];
    for(i=0;i<N;i++)
      pvec[i]= new Point(2);
    boolean localflag, globalflag=false;

    

    // Test Convexhull
    System.out.println("Testing Convexhull");

    Ran myran = new Ran(17);
    for (i=0;i<N;i++) {
      pvec[i].x[0]=myran.doub();
      pvec[i].x[1]=myran.doub();
    }
    Convexhull cvx=new Convexhull(pvec);

    // Hull should be close to unit square
//    System.out.printf(cvx.nhull);
    double[] a=new double[4],dis=new double[cvx.nhull];
    for (i=0;i<cvx.nhull;i++) {
      p=cvx.pts[cvx.hullpts[i]];
      a[0]=p.x[0];
      a[1]=1.0-p.x[0];
      a[2]=p.x[1];
      a[3]=1.0-p.x[1];
      dis[i]=1.0-maxel(a);
    }
//    System.out.printf(maxel(dis));

    localflag = maxel(dis) > 0.02;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Convexhull: Convex hull unexpectedly far from unit square");
      
    }

    Point[] hull = new Point[cvx.nhull];
    for (i=0;i<cvx.nhull;i++) 
      hull[i]=cvx.pts[cvx.hullpts[i]];

    localflag=false;
    boolean onhull;
    for (i=0;i<N;i++) {
      // Test only points inside the hull.  Points on hull are indeterminate.
      onhull=false;
      for (j=0;j<cvx.nhull;j++) 
        if (cvx.hullpts[j] == i) onhull=true;
      if (!onhull) {
        nwind=Polygon.polywind(hull,cvx.pts[i]);
//        if (nwind == 0) System.out.printf(i << " %f\n", cvx.pts[i].x[0] << " %f\n", cvx.pts[i].x[1]);
        localflag = localflag || (nwind == 0);
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Convexhull: Winding number of zero for one of the original points");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
