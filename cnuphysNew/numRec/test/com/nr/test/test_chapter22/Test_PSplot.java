package com.nr.test.test_chapter22;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.lna.PSpage;
import com.nr.lna.PSplot;

public class Test_PSplot {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws IOException{
    double[] x1=new double[500],x2=new double[500],y1=new double[500],y2=new double[500],y3=new double[500],y4=new double[500];
    boolean globalflag=false;

    

    // Test PSplot
    System.out.println("Testing PSplot");
    
    for (int i=0;i<500;i++) {
      x1[i] = 5.*i/499.;
      y1[i] = exp(-0.5*x1[i]);
      y2[i] = exp(-0.5*SQR(x1[i]));
      y3[i] = exp(-0.5*sqrt(5.-x1[i]));
      x2[i] = cos(0.062957*i);
      y4[i] = sin(0.088141*i);
    }

    PSpage pg=new PSpage(new java.io.File("PSplot_output.ps"));
    PSplot plot1=new PSplot(pg,100.,500.,100.,500.);

    plot1.setlimits(0.,5.,0.,1.);
    plot1.frame();
    plot1.autoscales();
    plot1.xlabel("abscissa");
    plot1.ylabel("ordinate");
    plot1.lineplot(x1,y1);
    plot1.setdash("2 4");
    plot1.lineplot(x1,y2);
    plot1.setdash("6 2 4 2");
    plot1.lineplot(x1,y3);
    plot1.setdash("");

    plot1.pointsymbol(1.,exp(-0.5),72,16.);
    plot1.pointsymbol(2.,exp(-1.),108,12.);
    plot1.pointsymbol(2.,exp(-2.),115,12.);
    plot1.label("dingbat 72",1.1,exp(-0.5));
    plot1.label("dingbat 108",2.1,exp(-1.));
    plot1.label("dingbat 115",2.1,exp(-2.));
    
    PSplot plot2=new PSplot(pg,325.,475.,325.,475.);
    plot2.clear();
    plot2.setlimits(-1.2,1.2,-1.2,1.2);
    plot2.frame();
    plot2.scales(1.,0.5,1.,0.5);
    plot2.lineplot(x2,y4);

    pg.close();
    // pg.display();

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
