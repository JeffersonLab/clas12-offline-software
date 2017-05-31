package com.nr.test.test_chapter22;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.lna.MParith.*;
import static java.lang.Math.*;

import com.nr.ran.*;


public class Test_MParith {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    // char buffer[30];
    int i,j,iv,ir,N=8,M=100;  // If N>6 we get inaccuracy errors below
    String s1,s2,s3,s4;
    double x1=0,x2=0,x3=0,x4,sbeps;
    char[] w =new char[N],u=new char[N],v=new char[N];
    boolean localflag, globalflag=false;

    

    // Test MParith
    System.out.println("Testing MParith");

    Ran myran=new Ran(17);

    // Test mpadd()
    System.out.println("  Test mpadd()");
    localflag=false;
    sbeps=1.e-15;
    char[] w2=new char[N+1];
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        u[i]=(char) (myran.int32p()%256);
        v[i]=(char) (myran.int32p()%256);
      }
      mpadd(w2,u,v);
      x1=x2=x3=0.0;
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(u[i]);
        x2=x2/256+(int)(v[i]);
      }
      for (i=N;i>0;i--) 
        x3=x3/256+(int)(w2[i]);
      x3 += 256*(int)(w2[0]);   // Note!! w2 is right-shifted by the algorithm
//      System.out.printf(x1 << "  %f\n", x2 << "  %f\n", x3 << "  %f\n", abs(1.0-(x1+x2)/x3));
      localflag=localflag || abs(1.0-(x1+x2)/x3) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision addition was inaccurate.");
      
    }

    // Test mpsub()
    System.out.println("  Test mpsub()");
    localflag=false;
    sbeps=1.e-14;
    for (j=0;j<M;j++) {
      u[0]=v[0]=0;
      for (i=1;i<N;i++) {
        u[i]=(char)(myran.int32p()%256);
        v[i]=(char)(myran.int32p()%256);
      }
      iv=mpsub(w,u,v);
      if (iv < 0) iv=mpsub(w,v,u); // If negative, reverse order of subtraction
      x1=x2=x3=0;
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(u[i]);
        x2=x2/256+(int)(v[i]);
        x3=x3/256+(int)(w[i]);
      }
//      System.out.printf(abs(x3-abs(x1-x2)));
      localflag=localflag || abs(x3-abs(x1-x2)) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision subtraction was inaccurate.");
      
    }

    // Test mpsad()
    System.out.println("  Test mpsad()");
    localflag=false;
    sbeps=1.e-15;
    for (j=0;j<=M;j++) {
      for (i=0;i<N;i++)
        u[i]=(char)(myran.int32p()%256);
      iv=myran.int32p()%256;
      mpsad(w2,u,iv);
      x1=x2=x3=0.0;
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(u[i]);
        x2=x2/256+(i==(N-1) ? iv : 0);
      }
      for (i=N;i>0;i--) {
        x3=x3/256+(int)(w2[i]);
      }
      x3 += 256*(int)(w2[0]);   // Note!! w2 is right-shifted by the algorithm
//      System.out.printf(abs(1.0-(x2+x1)/x3));
      localflag=localflag || abs(1.0-(x2+x1)/x3) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision short addition was inaccurate.");
      
    }

    // Test mpsmu()
    System.out.println("  Test mpsmu()");
    localflag=false;
    sbeps=1.e-15;
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++)
        u[i]=(char)(myran.int32p()%256);
      iv=myran.int32p()%256;
      mpsmu(w2,u,iv);
      x1=x2=x3=0.0;
      for (i=N-1;i>=0;i--)
        x1=x1/256+(int)(u[i]);
      x2=(int)(iv);
      for (i=N;i>0;i--)
        x3=x3/256+(int)(w2[i]);
      x3 += 256*(int)(w2[0]);   // Note!! w2 is right-shifted by the algorithm
//      System.out.printf(abs(1.0-x2*x1/x3));
      localflag=localflag || abs(1.0-x2*x1/x3) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision short multiplication was inaccurate.");
      
    }

    // Test mpsdv()
    System.out.println("  Test mpsdv()");
    localflag=false;
    sbeps=1.e-14;
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++)
        u[i]=(char)(myran.int32p()%256);
      iv=myran.int32p()%255 + 1; // Between 1 and 255
      ir=mpsdv(w,u,iv);
      x1=x2=x3=x4=0.0;
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(u[i]);
        x3=x3/256+(int)(w[i]);
        x4=x4/256+(i==(N-1) ? (int)(ir) : 0.0);
      }
      x2=(int)(iv);
//      System.out.printf(1.0-(x3*x2+x4)/x1 << "  %f\n", x4);
      localflag=localflag || abs(1.0-(x3*x2+x4)/x1) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision short division was inaccurate.");
      
    }

    // Test mpneg()
    System.out.println("  Test mpneg()");
    localflag=false;
    sbeps=1.e-15;
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        u[i]=(char)(myran.int32p()%256);
        v[i]=u[i];
      }
      mpneg(v);
      x1=x2=0.0;
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(u[i]);
        x2=x2/256+(int)(v[i]);
      }
//      System.out.printf(x1 << "  %f\n", x2 << " %f\n", abs(1.0-(x1+x2)/256.0));
      localflag=localflag || abs(1.0-(x1+x2)/256.0) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision twos-complement negation is inaccurate.");
      
    }

    // Test mpmov()
    System.out.println("  Test mpmov()");
    localflag=false;
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++)
        v[i]=(char)(myran.int32p()%256);
      mpmov(u,v);
      for (i=0;i<N;i++)
        localflag = localflag || (u[i] != v[i]); 
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision move operation did not work.");
      
    }

    // Test mplsh()
    System.out.println("  Test mplsh()");
    localflag=false;
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) {
        u[i]=(char)(myran.int32p()%256);
        v[i]=u[i];
      }
      mplsh(v);
      for (i=0;i<N-1;i++)
        localflag = localflag || (v[i] != u[i+1]);
      localflag = localflag || (v[N-1] != 0);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision left-shift operation did not work.");
      
    }

    // Test mpmul()
    System.out.println("  Test mpmul()");
    localflag=false;
    sbeps=1.e-15;
    for (j=0;j<M;j++) {
      u[0]=v[0]=0;
      for (i=1;i<N;i++) {
        u[i]=(char)(myran.int32p()%256);
        v[i]=(char)(myran.int32p()%256);
      }
      char[] w3=new char[N+N-1];
      mpmul(w3,u,v);
      x1=x2=x3=0.0;
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(u[i]);
        x2=x2/256+(int)(v[i]);
      }
      for (i=2*N-2;i>0;i--)
        x3=x3/256+(int)(w3[i]);
      x3 += 256*(int)(w3[0]); // w3[] was effectively right shifted to include all digits of result
//      System.out.printf(x1 << " %f\n", x2 << " %f\n", x3 << " %f\n", abs(1.0-x1*x2/x3));
      localflag = localflag || abs(1.0-x1*x2/x3) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision long multiplication in inaccurate.");
      
    }

    // Test mpinv()
    System.out.println("  Test mpinv()");
    localflag=false;
    sbeps=1.e-14;
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) 
        v[i]=(char)(myran.int32p()%256);
      v[0]=(char)(v[0] | 0x1);  // Make sure v[0] is not 0  
      mpinv(u,v);
      x1=x2=0;
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(v[i]);
        x2=x2/256+(int)(u[i]);
      }
//      System.out.printf(x1 << "  %f\n", x2 << "  %f\n", abs(x1*x2-1.0));
      localflag = localflag || abs(x1*x2-1.0) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision inverse in inaccurate.");
    }

    // Test mpdiv()
    System.out.println("  Test mpdiv()");
    localflag=false;
    sbeps=1.e-14;
    char[] v2=new char[N-2];
    char[] q=new char[3],r=new char[N-2];
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++)
        u[i]=(char)(myran.int32p()%256);
      for (i=0;i<N-2;i++)
        v2[i]=(char)(myran.int32p()%256);
      mpdiv(q,r,u,v2);
      x1=x2=x3=x4=0.0;
      for (i=N-1;i>=0;i--)
        x1=x1/256+(int)(u[i]);
      for (i=N-3;i>=0;i--)
        x2=x2/256+(int)(v2[i]);
      for (i=2;i>=0;i--)
        x3=x3/256+(int)(q[i]);
      for (i=N-3;i>=0;i--)
        x4=x4/256+(int)(r[i]);
//      System.out.printf(x1 << " %f\n", x2 << " %f\n", x3 << " %f\n", x4 << " %f\n", abs(1.0-x2*x3/x1-(x4/x1/256/256)));
      localflag=localflag || abs(1.0-x2*x3/x1-(x4/x1/256/256)) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision long division is inaccurate.");      
    }

    // Test mpsqrt()
    System.out.println("  Test mpsqrt()");
    localflag=false;
    sbeps=1.e-14;
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++)
        v[i]=(char)(myran.int32p()%256);
      mpsqrt(w,u,v);
      for (i=N-1;i>=0;i--) {
        x1=x1/256+(int)(v[i]);
        x2=x2/256+(int)(w[i]);
        x3=x3/256+(int)(u[i]);
      }
//      System.out.printf(x1 << " %f\n", x2 << " %f\n", x3 << " %f\n", abs(1.0-x2*x2/x1) << " %f\n", abs(1.0-x2*x3));
      localflag = localflag || abs(1.0-x2*x2/x1) > sbeps || abs(1.0-x2*x3) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Multiple precision square root is inaccurate.");
      
    }

    // Test mp2dfr()
    System.out.println("  Test mp2dfr()");
    x1=0.0;
    for (i=0;i<N;i++)
      v[i]=(char)((myran.int32p()&0xFFFFFFFFL)%256);
    for (i=N-1;i>=0;i--)
      x1=x1/256+(int)(v[i]);
    s1=mp2dfr(v);
    s1=s1.substring(0, 17); //.erase(17,s1.length());
    String s6 = String.format("%20.18f",x1);
    s6=s6.substring(0, 17);
    // System.out.println(s1);
    // System.out.println(s6);
    localflag = (!s1.equals(s6));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: Method mp2dfr() did not produce the correct string");
      
    }

    // Test mppi()
    System.out.println("  Test mppi()");
    String pi1="3.1415926535897932384626433832795028";
    String pi2="841971693993751058209749445923078164";
    String pi3=pi1+pi2;
    s1=mppi(30);
//    System.out.printf(s1 << " %f\n", pi3);
    localflag = (!s1.equals(pi3));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MParith: method mppi() gives inaccurate value for PI");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");   
  }

}
