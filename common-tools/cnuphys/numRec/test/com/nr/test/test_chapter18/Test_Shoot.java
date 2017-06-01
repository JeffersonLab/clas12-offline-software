package com.nr.test.test_chapter18;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.booleanW;

import com.nr.bvp.Shoot;
import com.nr.ode.DerivativeInf;
import com.nr.root.Roots;

public class Test_Shoot {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    booleanW check=new booleanW(false);
    int i,j,nvar=3,N2=1;
    int n[]={2,2,2,5,5,11,8,10,12};
    int m[]={2,2,2,2,2,4,7,9,11};
    double result=0,dx=1.0e-8;
    double c2[]={0.1,1.0,4.0,1.0,16.0,-1.0,0.0,1.0,-1.0};
    double expect[]={6.01426631394,6.14094899057,6.54249527439,
      30.43614538636,36.9962674974,131.560080919,
      72.0000000000,110.130237996,155.888762517};
    double[] v=new double[N2];
    double gmma,q1,x1,x2,sbeps=1.e-10;
    boolean localflag, globalflag=false;

    

    // Test Shoot
    System.out.println("Testing Shoot");

    for (j=0;j<9;j++) {
      gmma=1.0;
      q1=n[j];
      for (i=1;i<=m[j];i++) gmma *= -0.5*(n[j]+i)*(q1--/i);
      v[0]=n[j]*(n[j]+1)-m[j]*(m[j]+1)+c2[j]/2.0;
      x1= -1.0+dx;
      x2=0.0;
      final Load load = new Load(n[j],m[j],gmma,c2[j],dx);
      Rhs d = new Rhs(m[j],c2[j]);
      final Score score = new Score(n[j],m[j]);
      Shoot shoot =new Shoot(nvar,x1,x2, d) {

        @Override
        public double[] load(double x, double[] v) {
          return load.load(x, v);
        }

        @Override
        public double[] score(double x, double[] v) {
         return score.score(x, v);
        }
        
      };
      
      //Shoot<Load,Rhs,Score> shoot(nvar,x1,x2,load,d,score);
      Roots.newt(v,check,shoot);
      if (check.val) {
        System.out.println("shoot failed; bad initial guess");
      } else {
//        System.out.println("    %f\n", "mu(m,n)");
        result=v[0]+m[j]*(m[j]+1);
        System.out.println( result);
      }

//      System.out.printf(abs(result/expect[j]-1.0));
      localflag = abs(result/expect[j]-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Shoot: Did not achieve expected accuracy in the eigenvalue");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Rhs implements DerivativeInf{
    int m;
    double c2;

    Rhs(final int mm, final double cc2) {
      m =mm;
      c2 =cc2;
    }
    public void derivs (final double x, final double[] y, final double[] dydx)
    {
      dydx[0]=y[1];
      dydx[1]=(2.0*x*(m+1.0)*y[1]-(y[2]-c2*x*x)*y[0])/(1.0-x*x);
      dydx[2]=0.0;
    }
    public void jacobian(final double x, final double[] y, final double[] dfdx, final double[][] dfdy) {
      // TODO Auto-generated method stub
      
    }
  }

  class Load {
    int n,m;
    double gmma,c2,dx;
    double[] y;
    
    Load(final int nn, final int mm, final double gmmaa, double cc2, final double dxx) {
      n=nn;
      m=mm;
      gmma=gmmaa;
      c2=cc2;
      dx=dxx;
      y =new double[3];
    }

    double[] load(final double x1, final double[] v)
    {
      double y1 = ((n-m & 1) != 0 ? -gmma : gmma);
      y[2]=v[0];
      y[1] = -(y[2]-c2)*y1/(2*(m+1));
      y[0]=y1+y[1]*dx;
      return y;
    }
  }

  class Score {
    int n,m;
    double[] f;

    Score(final int nn,final int mm) {
      n=nn;
      m=mm;
      f=new double[1];
    }
    
    double[] score(final double xf, final double[] y)
    {
      f[0]=((n-m & 1) != 0 ? y[0] : y[1]);
      return f;
    }
  }
}
