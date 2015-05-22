package com.nr.test.test_chapter10;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.min.Bracketmethod;
import com.nr.ran.Ran;
import com.nr.sf.Bessjy;

public class Test_Bracketmethod implements UniVarRealValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=100;
    double a,b,span=1.0;
    boolean localflag=false, globalflag=false;

    

    // Test Bracketmethod
    System.out.println("Testing Bracketmethod");

    Ran myran=new Ran(17);
    Bracketmethod br = new Bracketmethod();
    for (i=0;i<N;i++) {
      a=50.0*myran.doub();
      b=a+span;
      br.bracket(a,b,this);
//      System.out.printf(br.ax << " " << br.bx << " " << br.cx);
//      System.out.printf(br.fa << " " << br.fb << " " << br.fc << endl);
      localflag = localflag || (br.fa < br.fb) || (br.fc < br.fb);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bracketmethod: Faulty bracket center value not the smallest");
      
    }

    double sbeps=3.e-16;
    for (i=0;i<N;i++) {
      a=50.0*myran.doub();
      b=a+span;
      br.bracket(a,b,this);
      localflag = localflag || abs(br.fa - funk(br.ax))>sbeps 
        || abs(br.fb - funk(br.bx))>sbeps
        || abs(br.fc - funk(br.cx))>sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bracketmethod: Function values don't agree with coordinates");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public double funk(final double x) {
    Bessjy b = new Bessjy();
    return(b.j0(x));
  }

}
