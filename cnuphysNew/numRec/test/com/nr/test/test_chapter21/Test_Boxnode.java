package com.nr.test.test_chapter21;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Boxnode;
import com.nr.cg.Point;
import com.nr.ran.Ran;

public class Test_Boxnode {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,NBOX=7;
    double d1,d2,a,b,c,d,expect1,expect2,sbeps=1.e-15;
    boolean localflag, globalflag=false;

    

    // Test Boxnode
    System.out.println("Testing Boxnode");

    // Create a tree of Boxnodes: 0 -> 1,2 ; 1 -> 3,4 ; 2 -> 5,6
    // With 2 points in each box

    Boxnode boxes2[]=new Boxnode[NBOX];
    Point[] points2=new Point[2*NBOX];

    Ran myran=new Ran(17);
    for (i=0;i<NBOX;i++) {
      Point plo=new Point(myran.doub(),myran.doub());
      Point phi=new Point(myran.doub(),myran.doub());
      d1=phi.x[0]-plo.x[0];
      d2=phi.x[1]-plo.x[1];
      points2[2*i]=new Point(plo.x[0]+0.3*d1,plo.x[1]+0.3*d2);
      points2[2*i+1]=new Point(plo.x[0]+0.6*d1,plo.x[1]+0.6*d2);
      boxes2[i]=new Boxnode(plo,phi,(i-1)/2,2*i+1,2*i+2,2*i,2*i+1);
    }

//    for (i=0;i<NBOX;i++) {
//      System.out.printf(boxes2[i].mom << " ";
//      System.out.printf(boxes2[i].dau1 << " ";
//      System.out.printf(boxes2[i].dau2 << " ";
//      System.out.printf(boxes2[i].ptlo << " ";
//      System.out.printf(boxes2[i].pthi << " ";
//      System.out.printf(endl;
//    }

    // Track through some daughters
    a=boxes2[boxes2[boxes2[0].dau1].dau2].lo.x[0];
    b=boxes2[boxes2[boxes2[0].dau1].dau2].hi.x[0];
//    System.out.printf(a << " %f\n", a+0.3*(b-a) << " %f\n", a+0.6*(b-a) << " %f\n", b);
    c=points2[boxes2[4].ptlo].x[0];
    d=points2[boxes2[4].pthi].x[0];
//    System.out.printf(c << " %f\n", d);

    expect1=a+0.3*(b-a);
    expect2=a+0.6*(b-a);
    localflag = (abs(c-expect1) > sbeps) || (abs(d-expect2) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Boxnode: Coordinate 0 of daughter-daughter path failed");
      
    }

    a=boxes2[boxes2[boxes2[0].dau1].dau2].lo.x[1];
    b=boxes2[boxes2[boxes2[0].dau1].dau2].hi.x[1];
//    System.out.printf(a << " %f\n", a+0.3*(b-a) << " %f\n", a+0.6*(b-a) << " %f\n", b);
    c=points2[boxes2[4].ptlo].x[1];
    d=points2[boxes2[4].pthi].x[1];
//    System.out.printf(c << " %f\n", d);

    expect1=a+0.3*(b-a);
    expect2=a+0.6*(b-a);
    localflag = (abs(c-expect1) > sbeps) || (abs(d-expect2) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Boxnode: Coordinate 1 of daughter-daughter path failed");
      
    }

    // Track through mothers and daughters
    a=boxes2[boxes2[boxes2[boxes2[NBOX-1].mom].mom].dau1].lo.x[0];
    b=boxes2[boxes2[boxes2[boxes2[NBOX-1].mom].mom].dau1].hi.x[0];
//    System.out.printf(a << " %f\n", a+0.3*(b-a) << " %f\n", a+0.6*(b-a) << " %f\n", b);
    c=points2[boxes2[1].ptlo].x[0];
    d=points2[boxes2[1].pthi].x[0];
//    System.out.printf(c << " %f\n", d); 

    expect1=a+0.3*(b-a);
    expect2=a+0.6*(b-a);
    localflag = (abs(c-expect1) > sbeps) || (abs(d-expect2) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Boxnode: Coordinate 0 of mother-mother-daughter path failed");
      
    }

    a=boxes2[boxes2[boxes2[boxes2[NBOX-1].mom].mom].dau1].lo.x[1];
    b=boxes2[boxes2[boxes2[boxes2[NBOX-1].mom].mom].dau1].hi.x[1];
//    System.out.printf(a << " %f\n", a+0.3*(b-a) << " %f\n", a+0.6*(b-a) << " %f\n", b);
    c=points2[boxes2[1].ptlo].x[1];
    d=points2[boxes2[1].pthi].x[1];
//    System.out.printf(c << " %f\n", d);

    expect1=a+0.3*(b-a);
    expect2=a+0.6*(b-a);
    localflag = (abs(c-expect1) > sbeps) || (abs(d-expect2) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Boxnode: Coordinate 1 of mother-mother-daughter path failed");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
