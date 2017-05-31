package com.nr.test.test_chapter21;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static com.nr.cg.Point.dist;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.KDtree;
import com.nr.cg.Point;
import com.nr.ran.Ran;



public class Test_KDtree {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    boolean debug=false;
    int i,N=1000,M=100;
    double sbeps;
    Point[] kd2=new Point[N];  // vector of 2D Points  
    Point[] kd3=new Point[N];  // vector of 3D Points
    boolean localflag=false, globalflag=false;

    

    // Test KDtree
    System.out.println("Testing KDtree");

    Ran myran=new Ran(17);

    // Test KDtree in 2D
    for (i=0;i<N;i++) {
      kd2[i] = new Point(2);
      kd2[i].x[0]=myran.doub();
      kd2[i].x[1]=myran.doub();
    }
    KDtree tree2=new KDtree(2,kd2);
    
    // Test locate(Point) method
    Point y=new Point(2);
    int nb;       // number of box containing y
    localflag=false;
    for (i=0;i<M;i++) {
      y.x[0]=myran.doub();
      y.x[1]=myran.doub();
      nb=tree2.locate(y);
//      System.out.printf(nb);
//      System.out.printf(tree2.boxes[nb].lo.x[0] << " %f\n", tree2.boxes[nb].lo.x[1]);
//      System.out.printf(tree2.boxes[nb].hi.x[0] << " %f\n", tree2.boxes[nb].hi.x[1]);
      localflag = localflag || (y.x[0] < tree2.boxes[nb].lo.x[0]);
      localflag = localflag || (y.x[0] > tree2.boxes[nb].hi.x[0]);
      localflag = localflag || (y.x[1] < tree2.boxes[nb].lo.x[1]);
      localflag = localflag || (y.x[1] > tree2.boxes[nb].hi.x[1]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<2>,locate(Point): The located box does not contain selected point");
        
      }
    }

    // Test locate(int) method
    int n0;
    localflag=false;
    for (i=0;i<M;i++) {
      n0=myran.int32p() % N;
      nb=tree2.locate(n0);
//      System.out.printf(nb);
//      System.out.printf(tree2.boxes[nb].lo.x[0] << " %f\n", tree2.boxes[nb].lo.x[1]);
//      System.out.printf(tree2.boxes[nb].hi.x[0] << " %f\n", tree2.boxes[nb].hi.x[1]);
      localflag = localflag || (kd2[n0].x[0] < tree2.boxes[nb].lo.x[0]);
      localflag = localflag || (kd2[n0].x[0] > tree2.boxes[nb].hi.x[0]);
      localflag = localflag || (kd2[n0].x[1] < tree2.boxes[nb].lo.x[1]);
      localflag = localflag || (kd2[n0].x[1] > tree2.boxes[nb].hi.x[1]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<2>,locate(int): The located box does not contain selected point");
        
      }
    }

    // Test disti() method
    int n1,n2;
    sbeps=1.e-15;
    localflag=false;
    for (i=0;i<M;i++) {
      n1=myran.int32p() % N;
      n2=myran.int32p() % N;
      double dis=tree2.disti(n1,n2);
//      System.out.printf(dis << " %f\n", sqrt(SQR(kd2[n1].x[0]-kd2[n2].x[0]) 
//        + SQR(kd2[n1].x[1]-kd2[n2].x[1])));
      if (n1 == n2) 
        localflag = localflag || dis < 1.e99;
      else
        localflag = localflag || (dis-sqrt(SQR(kd2[n1].x[0]-kd2[n2].x[0]) 
          + SQR(kd2[n1].x[1]-kd2[n2].x[1]))) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<2>,disti(): The disti() method gave an incorrect result");
        
      }
    }

    // Test nearest() method
    int j,n3;
    double dis2sq;
    localflag=false;
    for (i=0;i<M;i++) {
      y.x[0]=myran.doub();
      y.x[1]=myran.doub();
      n3=tree2.nearest(y);
      dis2sq=SQR(kd2[n3].x[0]-y.x[0])+SQR(kd2[n3].x[1]-y.x[1]);
      for (j=0;j<N;j++) {
        if (j != n3) localflag = localflag || 
          (SQR(kd2[j].x[0]-y.x[0])+ SQR(kd2[j].x[1]-y.x[1])) < dis2sq;
      }
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<2>,nearest(): Exhaustive search found closer point");
        
      }
    }

    // Test nnearest() method
    int n4,n,nfar,K=5;
    boolean test;
    double far;
    int[]nn=new int[K];
    double[] dn=new double[K];
    localflag=false;
    for (i=0;i<M;i++) {
      n4=myran.int32p()%N;
      tree2.nnearest(n4,nn,dn,K);   // Find K nearest Points
      // See if there is anything closer than the furthest
      // of these points, other than the other points in the list nn[]
      far=0.0;
      nfar=0;
      for (j=0;j<K;j++)
        if (dn[j] > far) {
          far=dn[j];
          nfar=nn[j];
        }
      test=false;
      for (j=0;j<N;j++) {
        for (n=0;n<K;n++)
          test = test || (nn[n] == j);
        if (!test)
          localflag = localflag || (tree2.disti(j,n4) < far);
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** KDtree<2>,nnearest(): Found a nearer point than the supposed n nearest");
          
        }
      }
    }

    // Test locatenear() method
    int nmax=N/10;
    int[] list=new int[nmax];
    double r=0.1;
    y.x[0]=0.5;
    y.x[1]=0.5;
    int nret=tree2.locatenear(y,r,list,nmax);
    // Check the result by brute force
    int[] list2=new int[nmax];
    int nret2=0;
    for (i=0;i<N;i++) {
      if ((dist(y,kd2[i]) < r) && (nret2 < nmax)) {
        list2[nret2]=i;
        nret2++;
      }
    }

//    System.out.printf(nret << " %f\n", nret2);
    localflag = (nret != nret2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KDtree<2>,locatenear(): Found different number of points closer than radius r");
      
    }

    // Make sure list[] and list2[] are the same
    int[] llist=buildVector(list),llist2=buildVector(list2);
    Arrays.sort(llist);
    Arrays.sort(llist2);
    localflag=false;
    for (i=0;i<nret;i++) {
//      System.out.printf(llist[i] << " %f\n", llist2[i]);
      localflag=localflag || (llist[i] != llist2[i]);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KDtree<2>,locatenear(): The list of nearby points is not correct");
      
    }

    // Repeat all the tests in 3D
    // Test KDtree in 3D
    for (i=0;i<N;i++) {
      kd3[i] = new Point(3);
      kd3[i].x[0]=myran.doub();
      kd3[i].x[1]=myran.doub();
      kd3[i].x[2]=myran.doub();
    }
    KDtree tree3 = new KDtree(3,kd3);

    // Test locate(Point) method in 3D
    Point yy=new Point(3);
    localflag=false;
    for (i=0;i<M;i++) {
      yy.x[0]=myran.doub();
      yy.x[1]=myran.doub();
      yy.x[2]=myran.doub();
      nb=tree3.locate(yy);
//      System.out.printf(nb);
//      System.out.printf(tree3.boxes[nb].lo.x[0] << " %f\n", tree3.boxes[nb].lo.x[1] 
//        << tree3.boxes[nb].lo.x[2]);
//      System.out.printf(tree3.boxes[nb].hi.x[0] << " %f\n", tree3.boxes[nb].hi.x[1] 
//        << tree3.boxes[nb].hi.x[2]);
      localflag = localflag || (yy.x[0] < tree3.boxes[nb].lo.x[0]);
      localflag = localflag || (yy.x[0] > tree3.boxes[nb].hi.x[0]);
      localflag = localflag || (yy.x[1] < tree3.boxes[nb].lo.x[1]);
      localflag = localflag || (yy.x[1] > tree3.boxes[nb].hi.x[1]);
      localflag = localflag || (yy.x[2] < tree3.boxes[nb].lo.x[2]);
      localflag = localflag || (yy.x[2] > tree3.boxes[nb].hi.x[2]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<3>,locate(Point): The located box does not contain selected point");
        
      }
    }

    // Test locate(int) method in 3D
    localflag=false;
    for (i=0;i<M;i++) {
      n0=myran.int32p() % N;
      nb=tree3.locate(n0);
//      System.out.printf(nb);
//      System.out.printf(tree3.boxes[nb].lo.x[0] << " %f\n", tree3.boxes[nb].lo.x[1] 
//        tree3.boxes[nb].lo.x[2]);
//      System.out.printf(tree3.boxes[nb].hi.x[0] << " %f\n", tree3.boxes[nb].hi.x[1] 
//        tree3.boxes[nb].hi.x[2]);
      localflag = localflag || (kd3[n0].x[0] < tree3.boxes[nb].lo.x[0]);
      localflag = localflag || (kd3[n0].x[0] > tree3.boxes[nb].hi.x[0]);
      localflag = localflag || (kd3[n0].x[1] < tree3.boxes[nb].lo.x[1]);
      localflag = localflag || (kd3[n0].x[1] > tree3.boxes[nb].hi.x[1]);
      localflag = localflag || (kd3[n0].x[2] < tree3.boxes[nb].lo.x[2]);
      localflag = localflag || (kd3[n0].x[2] > tree3.boxes[nb].hi.x[2]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<3>,locate(int): The located box does not contain selected point");
        
      }
    }

    // Test disti() method in 3D
    sbeps=1.e-15;
    localflag=false;
    for (i=0;i<M;i++) {
      n1=myran.int32p() % N;
      n2=myran.int32p() % N;
      double dis=tree3.disti(n1,n2);
//      System.out.printf(dis << " %f\n", sqrt(SQR(kd3[n1].x[0]-kd3[n2].x[0]) 
//        + SQR(kd3[n1].x[1]-kd3[n2].x[1])
//        + SQR(kd3[n1].x[2]-kd3[n2].x[2])));
      if (n1 == n2) 
        localflag = localflag || dis < 1.e99;
      else
        localflag = localflag || (dis-sqrt(SQR(kd3[n1].x[0]-kd3[n2].x[0]) 
          + SQR(kd3[n1].x[1]-kd3[n2].x[1])
          + SQR(kd3[n1].x[2]-kd3[n2].x[2]))) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<3>,disti(): The disti() method gave an incorrect result");
        
      }
    }

    // Test nearest() method in 3D
    double dis3sq;
    localflag=false;
    for (i=0;i<M;i++) {
      yy.x[0]=myran.doub();
      yy.x[1]=myran.doub();
      yy.x[2]=myran.doub();
      n3=tree3.nearest(yy);
      dis3sq=SQR(kd3[n3].x[0]-yy.x[0])+SQR(kd3[n3].x[1]-yy.x[1])
        +SQR(kd3[n3].x[2]-yy.x[2]);
      for (j=0;j<N;j++) {
        if (j != n3) localflag = localflag || 
          (SQR(kd3[j].x[0]-yy.x[0]) + SQR(kd3[j].x[1]-yy.x[1])
          + SQR(kd3[j].x[2]-yy.x[2])) < dis3sq;
      }
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** KDtree<3>,nearest(): Exhaustive search found closer point");
        
      }
    }

    // Test nnearest() method in 3D
    localflag=false;
    for (i=0;i<M;i++) {
      n4=myran.int32p() % N;
      tree3.nnearest(n4,nn,dn,K);   // Find K nearest Points
      // See if there is anything closer than the furthest
      // of these points, other than the other points in the list nn[]
      far=0.0;
      nfar=0;
      for (j=0;j<K;j++)
        if (dn[j] > far) {
          far=dn[j];
          nfar=nn[j];
        }
      test=false;
      for (j=0;j<N;j++) {
        for (n=0;n<K;n++)
          test = test || (nn[n] == j);
        if (!test)
          localflag = localflag || (tree3.disti(j,n4) < far);
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** KDtree<3>,nnearest(): Found a nearer point than the supposed n nearest");
          
        }
      }
    }

    // Test locatenear() method in 3D
    r=0.2;
    yy.x[0]=0.5;
    yy.x[1]=0.5;
    yy.x[2]=0.5;
    nret=tree3.locatenear(yy,r,list,nmax);
    // Check the result by brute force
    nret2=0;
    for (i=0;i<N;i++) {
      if ((dist(yy,kd3[i]) < r) && (nret2 < nmax)) {
        list2[nret2]=i;
        nret2++;
      }
    }

//    System.out.printf(nret << " %f\n", nret2);
    localflag = (nret != nret2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KDtree<3>,locatenear(): Found different number of points closer than radius r");
      
    }

    // Make sure list[] and list2[] are the same
    int[] llist3=buildVector(list),llist4=buildVector(list2);
    Arrays.sort(llist3);
    Arrays.sort(llist4);
    localflag=false;
    for (i=0;i<nret;i++) {
//      System.out.printf(llist3[i] << " %f\n", llist4[i]);
      localflag=localflag || (llist3[i] != llist4[i]);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** KDtree<3>,locatenear(): The list of nearby points is not correct");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
