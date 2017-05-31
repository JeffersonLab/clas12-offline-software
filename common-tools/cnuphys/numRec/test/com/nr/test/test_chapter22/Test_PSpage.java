package com.nr.test.test_chapter22;

import static com.nr.NRUtil.buildVector;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.lna.PSpage;

public class Test_PSpage {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws IOException {
    boolean globalflag=false;

    

    // Test PSpage
    System.out.println("Testing PSpage");
    
    PSpage pg=new PSpage(new java.io.File("PSpage_output.ps"));
    pg.puttext("Hello",100,100,0.0);
//    pg.addtext(" World");
    pg.putctext("Hello World",200,100,0.0);
    pg.putrtext("Hello World",300,100,0.0);
    pg.lineseg(400,100,500,100);
    pg.pointsymbol(550,100,72,16.0);

    pg.setlinewidth(3.0);
    pg.setdash("4 4");
    pg.puttext("Hello",100,200,90.0);
//    pg.addtext(" World");
    pg.putctext("Hello World",200,200,90.0);
    pg.putrtext("Hello World",300,200,90.0);
    pg.lineseg(400,200,500,200);
    pg.pointsymbol(550,200,108,20.0);

    pg.setdash("");
    pg.puttext("Hello",100,300,45.0);
//    pg.addtext(" World");
    pg.putctext("Hello World",200,300,45.0);
    pg.putrtext("Hello World",300,300,45.0);
    pg.lineseg(400,300,500,300);
    pg.pointsymbol(550,300,115,20.0);

    pg.gsave();
    pg.setgray(0.7);
    pg.puttext("Hello",100,400,45.0);
//    pg.addtext(" World");
    pg.putctext("Hello World",200,400,45.0);
    pg.putrtext("Hello World",300,400,45.0);
    pg.lineseg(400,400,500,400);
    pg.pointsymbol(550,400,110,20.0);

    pg.grestore();
    pg.puttext("Hello",100,500,45.0);
//    pg.addtext(" World");
    pg.putctext("Hello World",200,500,45.0);
    pg.putrtext("Hello World",300,500,45.0);
    pg.lineseg(400,500,500,500);
    pg.pointsymbol(550,500,112,12.0);

    pg.puttext("Hello",100,600,45.0);
    pg.rawps("gsave 200.0 600.0 translate 90.0 rotate 0 0 mt ");
    pg.addtext(" World");
    pg.rawps("grestore\n");

    double xx[]={300.0,350.0,400.0,350.0,300.0};
    double yy[]={650.0,650.0,600.0,550.0,550.0};
    double[] x=buildVector(xx),y=buildVector(yy);
    pg.polyline(x,y,true,false,false);

    pg.setcolor(255,0,0);
    double xx2[]={450.0,500.0,550.0,500.0,450.0};
    double yy2[]={650.0,650.0,600.0,550.0,550.0};
    double[] x2=buildVector(xx2),y2=buildVector(yy2);
    pg.polyline(x2,y2,true,true,false);

    pg.close();
    // pg.display();

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
