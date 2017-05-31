package com.nr.test.test_chapter21;

import static com.nr.cg.Point.dist;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.Box;
import com.nr.cg.Point;
import com.nr.cg.Sphcirc;
import com.nr.ran.Ran;


public class Test_Sphcirc {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean test;
    int i,j,N=1000;
    double radius1,radius2;
    Point p21=new Point(2),p22=new Point(2);
    Point p31=new Point(3),p32=new Point(3);
    boolean localflag, globalflag=false;

    

    // Test Sphcirc
    System.out.println("Testing Sphcirc");

    Ran myran=new Ran(17);

    // Test method operator== in 2D
    Sphcirc[] circ=new Sphcirc[N];
    
    for (i=0;i<N;i++) {
      circ[i] = new Sphcirc(2);
      circ[i].center.x[0]=myran.doub();
      circ[i].center.x[1]=myran.doub();
      circ[i].radius=myran.doub();
    }
    // Make every fifth circle identical
    for (i=5;i<N;i+=5)
      circ[i]=circ[0];
    Sphcirc ctest=new Sphcirc(circ[0].center,circ[0].radius);
    for (i=0;i<N;i++) {
      j=myran.int32p() % N;
      test = (circ[j].equals(ctest));
      localflag = (test != (j % 5 == 0)); 
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,operator==(),2D: Incorrect signal of whether two circles are identical");
        
      }
    }

    // Test method isinbox() in 2D
    Point lo=new Point(2),hi=new Point(2);
    double boxsize=0.8,sphsize=0.25;
    for (i=0;i<N;i++) {
      p21.x[0]=myran.doub();
      p21.x[1]=myran.doub();

      lo.x[0]=p21.x[0]-boxsize;
      hi.x[0]=p21.x[0]+boxsize;
      lo.x[1]=p21.x[1]-boxsize;
      hi.x[1]=p21.x[1]+boxsize;
      Box box1=new Box(lo,hi);

      p22.x[0]=myran.doub();
      p22.x[1]=myran.doub();
      Sphcirc sph1=new Sphcirc(p22,sphsize);

//      System.out.printf(sph1.isinbox(box1));
      test=(sph1.isinbox(box1) != 0);

      localflag = (test != (
        (abs(p21.x[0]-p22.x[0]) <= (boxsize-sphsize))
        && (abs(p21.x[1]-p22.x[1]) <= (boxsize-sphsize))));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,isinbox(),2D: Incorrect signal of whether the circle is inside the box");
        
      }
    }

    // Test method contains() in 2D
    for (i=0;i<N;i++) {
      p21.x[0]=myran.doub();
      p21.x[1]=myran.doub();
      radius1=myran.doub();
      Sphcirc sph1=new Sphcirc(p21,radius1);

      p22.x[0]=myran.doub();
      p22.x[1]=myran.doub();

//      System.out.printf(sph1.contains(p22));
      test=(sph1.contains(p22) != 0);
      localflag = (test != (dist(p21,p22) < radius1));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,contains(),2D: Incorrect signal of whether point is in circle");
        
      }
    }

    // Test method collides() in 2D
    for (i=0;i<N;i++) {
      p21.x[0]=myran.doub();
      p21.x[1]=myran.doub();
      radius1=0.5*myran.doub();
      Sphcirc sph1=new Sphcirc(p21,radius1);

      p22.x[0]=myran.doub();
      p22.x[1]=myran.doub();
      radius2=0.5*myran.doub();
      Sphcirc sph2=new Sphcirc(p22,radius2);

//      System.out.printf(sph1.collides(sph2));
      test=(sph1.collides(sph2) != 0);
      localflag = (test != (dist(p21,p22) < (radius1+radius2)));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,collides(),2D: Incorrect identification of colliding circles");
        
      }
    }

    // Test method operator== in 3D
    Sphcirc[] circ3=new Sphcirc[N];
    for (i=0;i<N;i++) {
      circ3[i] = new Sphcirc(3);
      circ3[i].center.x[0]=myran.doub();
      circ3[i].center.x[1]=myran.doub();
      circ3[i].center.x[2]=myran.doub();
      circ3[i].radius=myran.doub();
    }
    // Make every fifth circle identical
    for (i=5;i<N;i+=5)
      circ3[i]=circ3[0];
    Sphcirc ctest3=new Sphcirc(circ3[0].center,circ3[0].radius);
    for (i=0;i<N;i++) {
      j=myran.int32p() % N;
      test = (circ3[j].equals(ctest3));
      localflag = (test != (j % 5 == 0)); 
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,operator==(),3D: Incorrect signal of whether two circles are identical");
        
      }
    }

    // Test method isinbox() in 3D
    Point lo3=new Point(3),hi3=new Point(3);
    boxsize=0.8;sphsize=0.25;
    for (i=0;i<N;i++) {
      p31.x[0]=myran.doub();
      p31.x[1]=myran.doub();
      p31.x[2]=myran.doub();

      lo3.x[0]=p31.x[0]-boxsize;
      hi3.x[0]=p31.x[0]+boxsize;
      lo3.x[1]=p31.x[1]-boxsize;
      hi3.x[1]=p31.x[1]+boxsize;
      lo3.x[2]=p31.x[2]-boxsize;
      hi3.x[2]=p31.x[2]+boxsize;
      Box box1=new Box(lo3,hi3);

      p32.x[0]=myran.doub();
      p32.x[1]=myran.doub();
      p32.x[2]=myran.doub();
      Sphcirc sph1=new Sphcirc(p32,sphsize);

//      System.out.printf(sph1.isinbox(box1));
      test=(sph1.isinbox(box1) != 0);

      localflag = (test != (
        (abs(p31.x[0]-p32.x[0]) <= (boxsize-sphsize))
        && (abs(p31.x[1]-p32.x[1]) <= (boxsize-sphsize))
        && (abs(p31.x[2]-p32.x[2]) <= (boxsize-sphsize)))
      );
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,isinbox(),3D: Incorrect signal of whether the circle is inside the box");
        
      }
    }

    // Test method contains() in 3D
    for (i=0;i<N;i++) {
      p31.x[0]=myran.doub();
      p31.x[1]=myran.doub();
      p31.x[2]=myran.doub();
      radius1=myran.doub();
      Sphcirc sph1=new Sphcirc(p31,radius1);

      p32.x[0]=myran.doub();
      p32.x[1]=myran.doub();
      p32.x[2]=myran.doub();

//      System.out.printf(sph2.contains(p32));
      test=(sph1.contains(p32) != 0);
      localflag = (test != (dist(p31,p32) < radius1));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,contains(),3D: Incorrect signal of whether point is in circle");
        
      }
    }

    // Test method collides() in 3D
    for (i=0;i<N;i++) {
      p31.x[0]=myran.doub();
      p31.x[1]=myran.doub();
      p31.x[2]=myran.doub();
      radius1=0.5*myran.doub();
      Sphcirc sph1=new Sphcirc(p31,radius1);

      p32.x[0]=myran.doub();
      p32.x[1]=myran.doub();
      p32.x[2]=myran.doub();
      radius2=0.5*myran.doub();
      Sphcirc sph2=new Sphcirc(p32,radius2);

//      System.out.printf(sph3.collides(sph4));
      test=(sph1.collides(sph2) != 0);
      localflag = (test != (dist(p31,p32) < (radius1+radius2)));
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Sphcirc,collides(),3D: Incorrect identification of colliding circles");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
