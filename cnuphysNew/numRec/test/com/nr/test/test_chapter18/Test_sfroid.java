package com.nr.test.test_chapter18;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.bvp.Difeq;
import com.nr.bvp.Solvde;
import static com.nr.sf.Legendre.*;

public class Test_sfroid {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean localflag,globalflag=false;    

    // Test sfroid
    System.out.println("Testing sfroid");

    localflag = main_sfroid() != 0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sfroid : Program did not indicate successful completion");
    }
    
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  int main_sfroid()
  {
    final int M=40,MM=4;
    final int NE=3,NB=1,NYJ=NE,NYK=M+1;
    int mm=3,n=5,mpt=M+1;
    int[] indexv=new int[NE];
    double[] x=new double[M+1],scalv=new double[NE];
    double[][] y=new double[NYJ][NYK];
    int itmax=100;
    double c2[]={16.0,20.0,-16.0,-20.0};
    double conv=1.0e-14,slowc=1.0,h=1.0/M;
    if ((n+mm & 1) != 0) {
      indexv[0]=0;
      indexv[1]=1;
      indexv[2]=2;
    } else {
      indexv[0]=1;
      indexv[1]=0;
      indexv[2]=2;
    }
    double anorm=1.0;
    if (mm != 0) {
      double q1=n;
      for (int i=1;i<=mm;i++) anorm = -0.5*anorm*(n+i)*(q1--/i);
    }
    for (int k=0;k<M;k++) {
      x[k]=k*h;
      double fac1=1.0-x[k]*x[k];
      double fac2=exp((-mm/2.0)*log(fac1));
      y[0][k]=plgndr(n,mm,x[k])*fac2;
      double deriv = -((n-mm+1)*plgndr(n+1,mm,x[k])-
        (n+1)*x[k]*plgndr(n,mm,x[k]))/fac1;
      y[1][k]=mm*x[k]*y[0][k]/fac1+deriv*fac2;
      y[2][k]=n*(n+1)-mm*(mm+1);
    }
    x[M]=1.0;
    y[0][M]=anorm;
    y[2][M]=n*(n+1)-mm*(mm+1);
    y[1][M]=y[2][M]*y[0][M]/(2.0*(mm+1.0));
    scalv[0]=abs(anorm);
    scalv[1]=(y[1][M] > scalv[0] ? y[1][M] : scalv[0]);
    scalv[2]=(y[2][M] > 1.0 ? y[2][M] : 1.0);
    for (int j=0;j<MM;j++) {
      Difeq difeq = new Difeq(mm,n,mpt,h,c2[j],anorm,x);
      new Solvde(itmax,conv,slowc,scalv,indexv,NB,y,difeq);
      System.out.printf("\n m = %d  n = %d  c**2 = %f lamda = %f\n",
          mm, n, c2[j],(y[2][0]+mm*(mm+1)));
    }
    return 0;
  }
}
