package com.nr.test.test_chapter21;

import static com.nr.cg.Point.dist;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Box;
import com.nr.cg.Point;
import com.nr.cg.Qotree;
import com.nr.cg.Sphcirc;


public class Test_Qotree {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,nbox,nlist,N=100;
    double sbeps=1.e-15;
    boolean localflag, globalflag=false;

    

    // Test Qotree
    System.out.println("Testing Qotree");

    // Ran myran=new Ran(17);

    // Test Qotree in 2D
    Qotree qotree2 = new Qotree(2,N,N,5);

    // Test setouterbox in 2D
    Point lo2=new Point(0.0,0.0),hi2=new Point(1.0,1.0);
    qotree2.setouterbox(lo2,hi2);

    // Test qobox in 2D
    Box b2=new Box(2);
    b2=qotree2.qobox(1);
//    System.out.printf(dist(b2.lo,lo2));
    localflag = dist(b2.lo,lo2) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Outer box lower corner improperly set");
      
    }

//    System.out.printf(dist(b2.hi,hi2));
    localflag = dist(b2.hi,hi2) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Outer box upper corner improperly set");
      
    }

    Point half=new Point(hi2.x[0]/2.0,hi2.x[1]/2.0);

    b2=qotree2.qobox(2);
//    System.out.printf(dist(b2.hi,half));
    localflag = dist(b2.hi,half) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Box 2 upper corner should be in center of outer box");
      
    }

    b2=qotree2.qobox(5);
//    System.out.printf(dist(b2.lo,half));
    localflag = dist(b2.lo,half) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Box 5 lower corner should be in center of outer box");
      
    }

    // Test qowhichbox in 2D
    Sphcirc s2 = new Sphcirc(2);
    s2.center=new Point(5.5/16.0,10.5/16.0);
    int ans[]={1,4,15,239};
    for (i=0;i<4;i++) {
      s2.radius=0.25/pow(2.0,i);
      nbox=qotree2.qowhichbox(s2);
//      System.out.printf(s2.radius << " %f\n", nbox);

      localflag = (nbox != ans[i]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,2D: Method qowhichbox() identified wrong box for specified circle");
        
      }
    }

    // Test qostore in 2D
    s2.center=new Point(5.5/16.0,10.5/16.0);
    for (i=0;i<4;i++) {
      s2.radius=0.25/pow(2.0,i);
      nbox=qotree2.qostore(s2);
//      System.out.println("box: %f\n", nbox);

      localflag = (nbox != ans[i]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,2D: Method qostore() stored specified circle in wrong box");
        
      }
    }

    // Test qoerase in 2D
    s2.center=new Point(5.5/16.0,10.5/16.0);
    for (i=0;i<4;i++) {
      s2.radius=0.25/pow(2.0,i);
      nbox=qotree2.qoerase(s2);
//      System.out.printf(nbox);

      localflag = (nbox != ans[i]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,2D: Method qoerase() removed circle from wrong box");
        
      }
    }

    // Store 16 circles in the tree
    s2.center=new Point(5.5/16.0,10.5/16.0);
    for (i=0;i<4;i++) {
      s2.radius=0.25/pow(2.0,i);
      qotree2.qostore(s2);
    }
    s2.center=new Point(10.5/16.0,10.5/16.0);
    for (i=0;i<4;i++) {
      s2.radius=0.25/pow(2.0,i);
      qotree2.qostore(s2);
    }
    s2.center=new Point(10.5/16.0,5.5/16.0);
    for (i=0;i<4;i++) {
      s2.radius=0.25/pow(2.0,i);
      qotree2.qostore(s2);
    }
    s2.center=new Point(5.5/16.0,5.5/16.0);
    for (i=0;i<4;i++) {
      s2.radius=0.25/pow(2.0,i);
      qotree2.qostore(s2);
    }

    // Test qoget in 2D
    Sphcirc[]list2=new Sphcirc[N];
    nlist=qotree2.qoget(1,list2,N);
    System.out.println(nlist);
    for (i=0;i<nlist;i++)
      System.out.printf( "%f %f  %f\n", list2[i].center.x[0], list2[i].center.x[1] , list2[i].radius);

    localflag = (nlist != 4);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Method qoget() found the wrong number of circles in Box 1");
      
    }

    localflag=false;
    for (i=0;i<nlist;i++) {
      nbox=qotree2.qowhichbox(list2[i]);
      localflag = localflag || (nbox != 1);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Method qoget() returned a circle that is not in Box 1");
      
    }
    
    // Test qodump in 2D
    int[] klist2=new int[N];
    nlist=qotree2.qodump(klist2,list2,N);
//    System.out.printf(nlist);

    localflag = (nlist != 16);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Method qodump() did not find all 16 circles in the tree");
      
    } 
    
    // Test qocontainspt in 2D
    Point p2=new Point(5.5/16.0,10.5/16.0);
    nlist=qotree2.qocontainspt(p2,list2,N);
//    System.out.printf(nlist);

    localflag = (nlist != 4);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Method qodump() did not find all 4 circles with the given point");
      
    }

    for (i=0;i<nlist;i++) {
      localflag = dist(p2,list2[i].center) > list2[i].radius;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,2D: Method qodump() returned a circle that does not contain the given point");
        
      }
    }

    // Test qocollides in 2D
    s2.center=new Point(0.75,0.75);
    s2.radius=0.10;
    nlist=qotree2.qocollides(s2,list2,N);
//    System.out.printf(nlist);

    localflag = (nlist != 3);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,2D: Method qocollides() did not find 3 circles that collide with target");
      
    }

    for (i=0;i<nlist;i++) {
      localflag = (dist(list2[i].center,s2.center) > list2[i].radius+s2.radius);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,2D: Incorrect collision detection by method qocollides()");
        
      }   
    }

    // Test Qotree in 3D
    Qotree qotree3=new Qotree(3,N,N,5);

    // Test setouterbox in 3D
    Point lo3=new Point(0.0,0.0,0.0),hi3=new Point(1.0,1.0,1.0);
    qotree3.setouterbox(lo3,hi3);

    // Test qobox in 3D
    Box b3=new Box(3);
    b3=qotree3.qobox(1);
//    System.out.printf(dist(b3.lo,lo3));
    localflag = dist(b3.lo,lo3) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Outer box lower corner improperly set");
      
    }

//    System.out.printf(dist(b3.hi,hi3));
    localflag = dist(b3.hi,hi3) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Outer box upper corner improperly set");
      
    }

    Point half3=new Point(hi3.x[0]/2.0,hi3.x[1]/2.0,hi3.x[2]/2.0);

    b3=qotree3.qobox(2);
//    System.out.printf(dist(b3.hi,half3));
    localflag = dist(b3.hi,half3) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Box 2 upper corner should be in center of outer box");
      
    }

    b3=qotree3.qobox(9);
//    System.out.printf(dist(b3.lo,half3));
    localflag = dist(b3.lo,half3) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Box 9 lower corner should be in center of outer box");
      
    }

    // Test qowhichbox in 3D
    Sphcirc s3=new Sphcirc(3);
    int ans3[]={1,8,59,3771};
    s3.center=new Point(5.5/16.0,10.5/16.0,10.5/16.0);
    for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      nbox=qotree3.qowhichbox(s3);
//      System.out.printf(s3.radius << " %f\n", nbox);

      localflag = (nbox != ans3[i]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,3D: Method qowhichbox() identified wrong box for specified sphere");
        
      }
    }
    
    // Test qostore in 3D
    s3.center=new Point(5.5/16.0,10.5/16.0,10.5/16.0);
    for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      nbox=qotree3.qostore(s3);
//      System.out.println("box: %f\n", nbox);

      localflag = (nbox != ans3[i]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,3D: Method qostore() stored specified sphere in wrong box");
        
      }
    }

    // Test qoerase in 3D
    s3.center=new Point(5.5/16.0,10.5/16.0,10.5/16.0);
    for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      nbox=qotree3.qoerase(s3);
//      System.out.printf(nbox);

      localflag = (nbox != ans3[i]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,3D: Method qoerase() removed sphere from wrong box");
        
      }
    }

    // Store 32 circles in the tree
    s3.center=new Point(5.5/16.0,5.5/16.0,5.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }
    s3.center=new Point(5.5/16.0,5.5/16.0,10.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }
    s3.center=new Point(5.5/16.0,10.5/16.0,5.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }
    s3.center=new Point(5.5/16.0,10.5/16.0,10.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }
    s3.center=new Point(10.5/16.0,5.5/16.0,5.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }
    s3.center=new Point(10.5/16.0,5.5/16.0,10.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }
    s3.center=new Point(10.5/16.0,10.5/16.0,5.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }
    s3.center=new Point(10.5/16.0,10.5/16.0,10.5/16.0);
      for (i=0;i<4;i++) {
      s3.radius=0.25/pow(2.0,i);
      qotree3.qostore(s3);
    }

    // Test qoget in 3D
    Sphcirc[] list3=new Sphcirc[N];
    nlist=qotree3.qoget(1,list3,N);
//    System.out.printf(nlist);
//    for (i=0;i<nlist;i++)
//      System.out.printf(list3[i].center.x[0] << " %f\n", list3[i].center.x[1] << " %f\n", list3[i].center.x[2] << " %f\n", list3[i].radius);
//    System.out.printf(endl;

    localflag = (nlist != 8);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Method qoget() found the wrong number of spheres in Box 1");
      
    }

    localflag=false;
    for (i=0;i<nlist;i++) {
      nbox=qotree3.qowhichbox(list3[i]);
      localflag = localflag || (nbox != 1);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Method qoget() returned a sphere that is not in Box 1");
      
    }
    
    // Test qodump in 3D
    int[] klist3=new int[N];
    nlist=qotree3.qodump(klist3,list3,N);
//    System.out.printf(nlist);

    localflag = (nlist != 32);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Method qodump() did not find all 32 spheres in the tree");
      
    } 

    // Test qocontainspt in 3D
    Point p3=new Point(5.5/16.0,10.5/16.0,10.5/16.0);
    nlist=qotree3.qocontainspt(p3,list3,N);
//    System.out.printf(nlist);

    localflag = (nlist != 4);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Method qodump() did not find all 4 spheres with the given point");
      
    }

    for (i=0;i<nlist;i++) {
      localflag = dist(p3,list3[i].center) > list3[i].radius;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,3D: Method qodump() returned a sphere that does not contain the given point");
        
      }
    }

    // Test qocollides in 3D
    s3.center=new Point(0.75,0.75,0.75);
    s3.radius=0.10;
    nlist=qotree3.qocollides(s3,list3,N);
//    System.out.printf(nlist);

    localflag = (nlist != 3);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Qotree,3D: Method qocollides() did not find 3 spheres that collide with target");
      
    }

    for (i=0;i<nlist;i++) {
      localflag = (dist(list3[i].center,s3.center) > list3[i].radius+s3.radius);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Qotree,3D: Incorrect collision detection by method qocollides()");
        
      }   
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
