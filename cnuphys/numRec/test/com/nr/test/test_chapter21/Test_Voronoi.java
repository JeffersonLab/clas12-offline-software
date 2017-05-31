package com.nr.test.test_chapter21;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Point;
import com.nr.cg.Triel;
import com.nr.cg.Voronoi;
import com.nr.ran.Ran;


public class Test_Voronoi {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,j,k,mm,N=1000,M=100;
    long key;
    double sbeps=1.e-15;
    Point[] pvec=new Point[N];
    for(i=0;i<N;i++)
      pvec[i]= new Point(2);
    boolean localflag, globalflag=false;

    

    // Test Voronoi
    System.out.println("Testing Voronoi");

    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      pvec[i].x[0]=myran.doub();
      pvec[i].x[1]=myran.doub();
    }
    Voronoi vor=new Voronoi(pvec);

    int m,nsite,jfirst;
    Voronoi.Voredge edge; // = new Voronoi.Voredge();
    Point end1=new Point(2),end2=new Point(2);
    Triel tt;
    double dotprodi,dotprodj;

    for (mm=0;mm<M;mm++) {
      // Choose a random segment
      m=myran.int32p() % vor.nseg;
      edge=vor.segs[m];
      end1=edge.p[0];
      end2=edge.p[1];
      nsite=edge.nearpt;

      // Search for triangles containing this site
      tt=vor.thelist[vor.trindx[nsite]];
      if (tt.p[0] == nsite) {
        i=tt.p[1]; j=tt.p[2];
      } else if (tt.p[1] == nsite) {
        i=tt.p[2]; j=tt.p[0];
      } else if (tt.p[2] == nsite) {
        i=tt.p[0]; j=tt.p[1];
      } else 
        throw new IllegalArgumentException("Triangle should contain nsite");
      jfirst=j;
      localflag=true;

      while (true) {
        // See if either triangle edge is perpendicular to the Voronoi edge
        dotprodi=(vor.pts[nsite].x[0]-vor.pts[i].x[0])*(edge.p[1].x[0]-edge.p[0].x[0])
          + (vor.pts[nsite].x[1]-vor.pts[i].x[1])*(edge.p[1].x[1]-edge.p[0].x[1]);
        dotprodj=(vor.pts[nsite].x[0]-vor.pts[j].x[0])*(edge.p[1].x[0]-edge.p[0].x[0])
          + (vor.pts[nsite].x[1]-vor.pts[j].x[1])*(edge.p[1].x[1]-edge.p[0].x[1]);
        localflag = localflag && (dotprodi > sbeps) && (dotprodj > sbeps);
        key=vor.hashfn.int64(i)-vor.hashfn.int64(nsite);
        Integer[] k_w = new Integer[1];
        vor.linehash.get(key,k_w,0);k=k_w[0];
        if (k == jfirst) break;
        j=i;
        i=k;
      }

      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Voronoi: Chosen Voronoi edge is not perpendicular to any local Delaunay edge");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
