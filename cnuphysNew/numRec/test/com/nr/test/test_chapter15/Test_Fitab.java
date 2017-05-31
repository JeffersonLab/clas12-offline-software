package com.nr.test.test_chapter15;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.model.Fitab;
import com.nr.ran.Normaldev;
import com.nr.ran.Ran;
public class Test_Fitab {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=100,M=10;
    double pi=acos(-1.0),sumx2,sa=0,sb=0,sbeps;
    double[] x= new double[N],y= new double[N],yy= new double[N],sig= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Fitab
    System.out.println("Testing Fitab");

    Ran myran =new Ran(17);
    sumx2=0;
    for (i=0;i<N;i++) {
      x[i]=10.0*myran.doub();
      y[i]=sqrt(2.0)+pi*x[i];
      sig[i]=1.0;
      sumx2 += SQR(x[i]);
    }

    Fitab fit1 = new Fitab(x,y,sig);    // Perfect fit, no noise
    sbeps=1.e-12;
    localflag = abs(fit1.a-sqrt(2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitab: Fitted constant term a has incorrect value");
      
    }

    localflag = abs(fit1.b-pi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitab: Fitted slope b has incorrect value");
      
    }

    localflag = fit1.chi2 > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitab: Chi^2 not zero for perfect linear data");
      
    }

    localflag = abs(fit1.q-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitab: Probability not 1.0 for perfect linear data");
      
    }

    localflag = abs(fit1.siga/fit1.sigb - sqrt(sumx2/N)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitab: Ratio of siga/sigb incorrect for special case");
      
    }

    // Test 2
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        sig[i]=0.1*(j+1); //0.1*(j+1)*sqrt(y[i]);
      }
      Fitab fit = new Fitab(x,y,sig);

      localflag = abs(fit.a-sqrt(2.0)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test2: Fitted constant term a has incorrect value");
        
      }

      localflag = abs(fit.b-pi) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test2: Fitted slope b has incorrect value");
        
      }

      localflag = fit.chi2 > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test2: Chi^2 not zero for perfect linear data");
        
      }

      localflag = abs(fit.q-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test2: Probability not 1.0 for perfect linear data");
        
      }

      if (j == 0) {
        sa=fit.siga;
        sb=fit.sigb;
      } else {
        localflag = (fit.siga/sa - (j+1)) > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Fitab,Test2: siga did not scale properly with data errors");
          
        }

        localflag = (fit.sigb/sb - (j+1)) > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Fitab,Test2: sigb did not scale properly with data errors");
          
        }
      }
    }

    // Test 3
    Normaldev ndev=new Normaldev(0.0,1.0,17);
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        yy[i] = y[i]+ndev.dev();
        sig[i]=1.0;
      }
      Fitab fit3 = new Fitab(x,yy,sig);

//      System.out.printf(fit3.a << " %f\n", fit3.b);
//      System.out.printf(fit3.siga << " %f\n", fit3.sigb);
//      System.out.printf(fit3.chi2 << " %f\n", fit3.q << endl);

      localflag = abs(fit3.a-sqrt(2.0)) > 3.0*fit3.siga;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test3: Fitted constant term a, or error siga, may be incorrect");
        
      }

      localflag = abs(fit3.b-pi) > 3.0*fit3.sigb;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test3: Fitted slope b, or error sigb, may be incorrect");
        
      }

      localflag = fit3.chi2 > 1.3*N;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test3: Chi^2 is unexpectedly high");
        
      }

      localflag = abs(fit3.q) < 0.1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitab,Test3: Probability q suggests a possibly bad fit");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
