package com.nr.test.test_chapter18;

import static com.nr.sf.Legendre.plgndr;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.bvp.Difeq;
import com.nr.bvp.Solvde;

public class Test_Solvde {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int M=100,NE=3,NB=1,NYJ=NE,NYK=M+1;
    int i,mpt=M+1,itmax=100;
    int n[]={2,2,2,5,5,11,8,10,12};
    int mm[]={2,2,2,2,2,4,7,9,11};
    double anorm,q1,fac1,fac2,deriv,conv=1.0e-15,slowc=1.0;
    double h=1.0/M,result,sbeps=1.e-4;
    double c2[]={0.1,1.0,4.0,1.0,16.0,-1.0,0.0,1.0,-1.0};
    double expect[]={6.01426631394,6.14094899057,6.54249527439,
      30.43614538636,36.9962674974,131.560080919,
      72.0000000000,110.130237996,155.888762517};
    int[] indexv=new int[NE];
    double[] x=new double[M+1],scalv=new double[NE];
    double[][] y=new double[NYJ][NYK];
    boolean localflag, globalflag=false;

    

    // Test Solvde
    System.out.println("Testing Solvde");

    for (i=0;i<9;i++) {
      if ((n[i]+mm[i] & 1) != 0) {
        indexv[0]=0;
        indexv[1]=1;
        indexv[2]=2;
      } else {
        indexv[0]=1;
        indexv[1]=0;
        indexv[2]=2;
      }
      anorm=1.0;
      if (mm[i] != 0) {
        q1=n[i];
        for (int k=1;k<=mm[i];k++) anorm = -0.5*anorm*(n[i]+k)*(q1--/k);
      }
      for (int k=0;k<M;k++) {
        x[k]=k*h;
        fac1=1.0-x[k]*x[k];
        fac2=exp((-mm[i]/2.0)*log(fac1));
        y[0][k]=plgndr(n[i],mm[i],x[k])*fac2;
        deriv = -((n[i]-mm[i]+1)*plgndr(n[i]+1,mm[i],x[k])-
          (n[i]+1)*x[k]*plgndr(n[i],mm[i],x[k]))/fac1;
        y[1][k]=mm[i]*x[k]*y[0][k]/fac1+deriv*fac2;
        y[2][k]=n[i]*(n[i]+1)-mm[i]*(mm[i]+1);
      }
      x[M]=1.0;
      y[0][M]=anorm;
      y[2][M]=n[i]*(n[i]+1)-mm[i]*(mm[i]+1);
      y[1][M]=(y[2][M]-c2[i])*y[0][M]/(2.0*(mm[i]+1.0));
      scalv[0]=abs(anorm);
      scalv[1]=(y[1][M] > scalv[0] ? y[1][M] : scalv[0]);
      scalv[2]=(y[2][M] > 1.0 ? y[2][M] : 1.0);
      Difeq difeq = new Difeq(mm[i],n[i],mpt,h,c2[i],anorm,x);
      new Solvde(itmax,conv,slowc,scalv,indexv,NB,y,difeq);
      result=y[2][0]+mm[i]*(mm[i]+1);
      System.out.printf("   m = %d  n = %d  c**2 = %f  lamda = %f\n", 
          mm[i], n[i], c2[i],result);

//      System.out.printf(abs(result/expect[i]-1.0));
      localflag = abs(result/expect[i]-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Solvde: Did not achieve expected accuracy in eigenvalue");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
