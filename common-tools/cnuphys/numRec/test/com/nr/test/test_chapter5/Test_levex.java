package com.nr.test.test_chapter5;

import static java.lang.Math.*;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fe.Levin;
import com.nr.sf.Bessel;
import static com.nr.fi.Trapzd.qromb;

public class Test_levex implements UniVarRealValueFun{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean localflag, globalflag=false;

    

    // Test levex
    System.out.println("Testing levex");

    localflag = main_levex() != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Levex : Program did not indicate successful completion");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  
  public double funk(final double x)
  {
    if (x == 0.0)
      return 0.0;
    else {
      Bessel bess = new Bessel();
      return x*bess.jnu(0.0,x)/(1.0+x*x);
    }
  }

  int main_levex(){
    int nterm=12;
    double beta=1.0,a=0.0,b=0.0,sum=0.0;
    Levin series = new Levin(100,0.0);
    //cout << setw(5) << "N" << setw(19) << "Sum (direct)" << setw(21)
    //  << "Sum (Levin)" << endl;
    for (int n=0; n<=nterm; n++) {
      b+=PI;
      double s=qromb(this,a,b,1.e-8);
      a=b;
      sum+=s;
      double omega=(beta+n)*s;
      double ans=series.next(sum,omega,beta);
      //cout << setw(5) << n << fixed << setprecision(14) << setw(21)
      //  << sum << setw(21) << ans << endl;
      System.out.printf("%f   %f\n", sum, ans);
    }
    return 0;
  }
}
