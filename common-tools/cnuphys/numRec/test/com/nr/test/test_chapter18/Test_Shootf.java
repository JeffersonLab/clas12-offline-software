package com.nr.test.test_chapter18;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.booleanW;

import com.nr.bvp.Shootf;
import com.nr.ode.DerivativeInf;
import com.nr.root.Roots;

public class Test_Shootf {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    booleanW check=new booleanW(false);
    int i,j,N1=2,N2=1,NTOT=N1+N2,nvar=NTOT,n2=N2;
    int n[]=  {2,2,2,5,5,11,8,10,12};
    int m[]=  {2,2,2,2,2,4,7,9,11};
    double c2[]={0.1,1.0,4.0,1.0,16.0,-1.0,0.0,1.0,-1.0};
    double expect[]={6.01426631394,6.14094899057,6.54249527439,
      30.43614538636,36.9962674974,131.560080919,
      72.0000000000,110.130237996,155.888762517};
    double gmma,q1,result=0,dx=1.0e-7,sbeps=1.e-10;
    double[] v=new double[NTOT];
    boolean localflag, globalflag=false;

    

    // Test Shootf
    System.out.println("Testing Shootf");

    for (j=0;j<9;j++) {
      gmma=1.0;
      q1=n[j];
      for (i=1;i<=m[j];i++) gmma *= -0.5*(n[j]+i)*(q1--/i);
      v[0]=n[j]*(n[j]+1)-m[j]*(m[j]+1)+c2[j]/2.0;
      v[2]=v[0];
      v[1]=gmma*(1.0-(v[2]-c2[j])*dx/(2*(m[j]+1)));
      double x1=-1.0+dx;
      double x2=1.0-dx;
      double xf=0.0;
      final Load1 load1 = new Load1(n[j],m[j],gmma,c2[j],dx);
      final Load2 load2 =new Load2(m[j],c2[j]);
      Rhs d = new Rhs(m[j],c2[j]);
      final Score score = new Score();
      Shootf shootf = new Shootf(nvar,n2,x1,x2,xf,d){

        @Override
        public double[] load1(double x, double[] v) {
          return load1.load(x,v);
        }

        @Override
        public double[] load2(double x, double[] v) {
          return load2.load(x, v);
        }

        @Override
        public double[] score(double x, double[] v) {
          return score.score(x,v);
        }
        
      };
      Roots.newt(v,check,shootf);
      if (check.val) {
        System.out.println("shootf failed; bad initial guess");
      } else {
//        System.out.println("    %f\n", "mu(m,n)");
        result=v[0]+m[j]*(m[j]+1);
        System.out.println( result);
      }

//      System.out.printf(abs(result/expect[j]-1.0));
      localflag = abs(result/expect[j]-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Shootf: Did not achieve expected accuracy in the eigenvalue");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Rhs implements DerivativeInf{
    int m;
    double c2;

    Rhs(int mm, double cc2) {
      m=mm;
      c2=cc2;
    }

    public void derivs (final double x, double[] y, double[] dydx)
    {
      dydx[0]=y[1];
      dydx[1]=(2.0*x*(m+1.0)*y[1]-(y[2]-c2*x*x)*y[0])/(1.0-x*x);
      dydx[2]=0.0;
    }

    public void jacobian(double x, double[] y, double[] dfdx, double[][] dfdy) {
      // TODO Auto-generated method stub
      
    }
  }
  
  class Load1 {
    int n,m;
    double gmma,c2,dx;
    double[] y;
    
    Load1(int nn, int mm, double gmmaa, double cc2, double dxx){
      n=nn;
      m=mm;
      gmma=gmmaa;
      c2=cc2;
      dx=dxx;
      y = new double[3];
    }
    
    public double[] load (final double xx1, double[] v1)
    {
      double y1 = ((n-m & 1) != 0 ? -gmma : gmma);
      y[2]=v1[0];
      y[1] = -(y[2]-c2)*y1/(2*(m+1));
      y[0]=y1+y[1]*dx;
      return y;
    }
  }

  class Load2 {
    int m;
    double c2;
    double[] y;

    Load2(int mm, double cc2) {
      m=mm;
      c2=cc2;
      y=new double[3];
    }
    
    double[] load (final double x2, double[] v2)
    {
      y[2]=v2[1];
      y[0]=v2[0];
      y[1]=(y[2]-c2)*y[0]/(2*(m+1));
      return y;
    }
  }

  class Score {
    double[] f;

    Score() {
      f=new double[3];
    }
    double[] score (final double xf, double[] y)
    {
      for (int i=0;i<3;i++) f[i]=y[i];
      return f;
    }
  }
}
