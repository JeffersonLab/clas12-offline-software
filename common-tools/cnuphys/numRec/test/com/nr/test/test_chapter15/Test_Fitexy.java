package com.nr.test.test_chapter15;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.ran.*;
import com.nr.model.*;

public class Test_Fitexy {

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
    double[] x= new double[N],y= new double[N],yy= new double[N],sigx= new double[N],sigy= new double[N];
    boolean localflag, globalflag=false;

    

    // Test Fitexy
    System.out.println("Testing Fitexy");

    Ran myran=new Ran(17);
    sumx2=0;
    for (i=0;i<N;i++) {
      x[i]=10.0*myran.doub();
      y[i]=sqrt(2.0)+pi*x[i];
      sigx[i]=0.3;
      sigy[i]=1.0;
      sumx2 += SQR(x[i]);
    }

    Fitexy fit1 = new Fitexy(x,y,sigx,sigy);   // Perfect fit, no noise
    sbeps=5.e-3;
    localflag = abs(fit1.a-sqrt(2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitexy: Fitted constant term a has incorrect value");
      
    }

    localflag = abs(fit1.b-pi) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitexy: Fitted slope b has incorrect value");
      
    }

    localflag = fit1.chi2 > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitexy: Chi^2 not zero for perfect linear data");
      
    }

    localflag = abs(fit1.q-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitexy: Probability not 1.0 for perfect linear data");
      
    }
    
//    System.out.printf(fit1.siga/fit1.sigb << " %f\n",  sqrt(sumx2/N));
    localflag = abs(fit1.siga/fit1.sigb - sqrt(sumx2/N)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Fitexy: Ratio of siga/sigb incorrect for special case");
      
    }

    // Test 2
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        sigy[i]=0.1*(j+1);
      }
      Fitexy fit = new Fitexy(x,y,sigx,sigy);

      localflag = abs(fit.a-sqrt(2.0)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test2: Fitted constant term a has incorrect value");
        
      }

      localflag = abs(fit.b-pi) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test2: Fitted slope b has incorrect value");
        
      }

      localflag = fit.chi2 > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test2: Chi^2 not zero for perfect linear data");
        
      }

      localflag = abs(fit.q-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test2: Probability not 1.0 for perfect linear data");
        
      }

      if (j == 0) {
        sa=fit.siga;
        sb=fit.sigb;
      } else {
        localflag = (fit.siga/sa - (j+1)) > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Fitexy,Test2: siga did not scale properly with data errors");
          
        }

        // System.out.printf(fit.sigb/sb << " %f\n", j+1);
        localflag = (fit.sigb/sb - (j+1)) > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Fitexy,Test2: sigb did not scale properly with data errors");
          
        }
      }
    }

    // Test 3
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        sigx[i]=0.03*(j+1);
        sigy[i]=1.0;
      }
      Fitexy fit = new Fitexy(x,y,sigx,sigy);

      localflag = abs(fit.a-sqrt(2.0)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test3: Fitted constant term a has incorrect value");
        
      }

      localflag = abs(fit.b-pi) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test3: Fitted slope b has incorrect value");
        
      }

      localflag = fit.chi2 > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test3: Chi^2 not zero for perfect linear data");
        
      }

      localflag = abs(fit.q-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test3: Probability not 1.0 for perfect linear data");
        
      }

      if (j == 0) {
        sa=fit.siga;
        sb=fit.sigb;
      } else {
        localflag = (fit.siga/sa - (j+1)) > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Fitexy,Test3: siga did not scale properly with data errors");
          
        }

        // System.out.printf(fit.sigb/sb << " %f\n", j+1);
        localflag = (fit.sigb/sb - (j+1)) > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Fitexy,Test3: sigb did not scale properly with data errors");
          
        }
      }
    }

    // Test 4
    Normaldev ndev=new Normaldev(0.0,1.0,17);
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        yy[i] = y[i]+ndev.dev();
        sigy[i]=1.0;
      }
      Fitexy fit4=new Fitexy(x,yy,sigx,sigy);

//      System.out.printf(fit4.a << " %f\n", fit4.b);
//      System.out.printf(fit4.siga << " %f\n", fit4.sigb);
//      System.out.printf(fit4.chi2 << " %f\n", fit4.q << endl);

      localflag = abs(fit4.a-sqrt(2.0)) > 3.0*fit4.siga;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test4: Fitted constant term a, or error siga, may be incorrect");
        
      }

      localflag = abs(fit4.b-pi) > 3.0*fit4.sigb;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test4: Fitted slope b, or error sigb, may be incorrect");
        
      }

      localflag = fit4.chi2 > 1.3*N;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test4: Chi^2 is unexpectedly high");
        
      }

      localflag = abs(fit4.q) < 0.1;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Fitexy,Test4: Probability q suggests a possibly bad fit");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
