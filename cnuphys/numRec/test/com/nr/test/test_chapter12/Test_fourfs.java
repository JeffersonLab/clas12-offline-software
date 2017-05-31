package com.nr.test.test_chapter12;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static com.nr.NRUtil.swap;
import static com.nr.fft.FFT.fourfs;
import static com.nr.fft.FFT.fourn;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
public class Test_fourfs {

  
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws IOException{
    int NX=8,NY=32,NZ=4,NDAT=2*NX*NY*NZ;
    int i,j,k,l,ll,nwrite,n[]={NX,NY,NZ};
    long cc;
    double diff,smax,sum,sum1=0.0,sum2=0.0,tot,sbeps;
    java.nio.channels.FileChannel flswap;
    String[] fnames={"fourfs1.dat","fourfs2.dat","fourfs3.dat","fourfs4.dat"};
    java.nio.channels.FileChannel[] file = new java.nio.channels.FileChannel[4];
    int[] nn=buildVector(n);
    double[] data1=new double[NDAT],data2=new double[NDAT];
    boolean localflag=false, globalflag=false;

    

    // Test fourfs
    System.out.println("Testing fourfs");

    tot=NDAT/2;
    for (j=0;j<4;j++) {
      file[j]=new RandomAccessFile(new File(fnames[j]),"rw").getChannel();
    }

    Ran myran = new Ran(17);
    for (i=0;i<nn[2];i++)
      for (j=0;j<nn[1];j++)
        for (k=0;k<nn[0];k++) {
          l=k+j*nn[0]+i*nn[1]*nn[0];
          l=(l<<1);
          data2[l]=data1[l]=2*myran.doub()-1;
          l++;
          data2[l]=data1[l]=2*myran.doub()-1;
        }
    nwrite=NDAT >> 1;
    ByteBuffer bb =  ByteBuffer.allocate(nwrite*8);
    for(int m=0;m<nwrite;m++)bb.putDouble(data1[m]);bb.flip(); file[0].write(bb);bb.clear();
    //file[0].write((char *)&data1[0],nwrite*sizeof(double));
    cc=file[0].position()/8;
    if (cc != nwrite) throw new IOException("write error in xfourfs");
    for(int m=0;m<nwrite;m++)bb.putDouble(data1[nwrite+m]);bb.flip(); file[1].write(bb);bb.clear();
    //file[1].write((char *)&data1[nwrite],nwrite*sizeof(double));
    cc=file[1].position()/8;
    if (cc != nwrite) throw new IOException("write error in xfourfs");

    for(j=0;j<4;j++) file[j].position(0);
//    fail("**************** now doing fourfs *********");
    fourfs(file,nn,1);

    for (j=0;j<4;j++) file[j].position(0);
    file[2].read(bb);bb.flip(); for(int m=0;m<nwrite;m++)data1[m] = bb.getDouble();bb.clear();
    //(*file[2]).read((char *)&data1[0],nwrite*sizeof(double));
    cc=file[2].position()/8;
    if (cc != nwrite) throw new IOException("read error in xfourfs");
    file[3].read(bb);bb.flip(); for(int m=0;m<nwrite;m++)data1[nwrite+m] = bb.getDouble(); bb.clear();
    //(*file[3]).read((char *)&data1[nwrite],nwrite*sizeof(double));
    cc=file[3].position()/8;
    if (cc != nwrite) throw new IOException("read error in xfourfs");

//    fail("**************** now doing fourn *********");
    fourn(data2,nn,1);

    sum=smax=0.0;
    for (i=0;i<nn[2];i++)
      for (j=0;j<nn[1];j++)
        for (k=0;k<nn[0];k++) {
          l=k+j*nn[0]+i*nn[1]*nn[0];
          l=(l<<1);
          ll=i+j*nn[2]+k*nn[2]*nn[1];
          ll=(ll<<1);
          diff=sqrt(SQR(data2[ll]-data1[l])+SQR(data2[ll+1]-data1[l+1]));
          sum2 += SQR(data1[l])+SQR(data1[l+1]);
          sum += diff;
          if (diff > smax) smax=diff;
        }
    sum2=sqrt(sum2/tot);
    sum=sum/tot;
//    System.out.printf(scientific << setprecision(2);
//    System.out.println("(r.m.s.) value, (max,ave) discrepancy= ";
//    System.out.printf(setw(13) << sum2 << setw(13) << smax;
//    System.out.printf(setw(13) << sum << endl);
    
    sbeps=1.e-12;
    localflag = sum > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** fourfs: Forward transform did not agree with fourn()");
      
    }

    // now check the inverse transforms
    swap(nn,0,2);

    // This swap step is conceptually a reversal
    flswap=file[0]; file[0]=file[2]; file[2]=flswap;
    flswap=file[3]; file[3]=file[1]; file[1]=flswap;

    for (j=0;j<4;j++) file[j].position(0);
//    fail("**************** now doing fourfs *********");
    fourfs(file,nn,-1);

    for (j=0;j<4;j++) file[j].position(0);
    file[2].read(bb);bb.flip();for(int m=0;m<nwrite;m++)data1[m] = bb.getDouble();bb.clear();
    
    //(*file[2]).read((char *)&data1[0],nwrite*sizeof(double));
    cc=file[2].position()/8;
    if (cc != nwrite) throw new IOException("read error in xfourfs");
    file[3].read(bb);bb.flip(); for(int m=0;m<nwrite;m++)data1[nwrite+m] = bb.getDouble();bb.clear();
    //(*file[3]).read((char *)&data1[nwrite],nwrite*sizeof(double));
    cc=file[3].position()/8;
    if (cc != nwrite) throw new IOException("read error in xfourfs");
    swap(nn,0,2);

//    fail("**************** now doing fourn *********");
    fourn(data2,nn,-1);

    sum=smax=0.0;
    double[] data1p=buildVector(data1),data2p=buildVector(data2);
    for (j=0;j<NDAT;j+=2) {
      sum1 += SQR(data1p[j])+SQR(data1p[j+1]);
      diff=sqrt(SQR(data2p[j]-data1p[j])+SQR(data2p[j+1]-data1p[j+1]));
      sum += diff;
      if (diff > smax) smax=diff;
    }
    sum=sum/tot;
    sum1=sqrt(sum1/tot);

//    System.out.println("(r.m.s.) value, (max,ave) discrepancy= ";
//    System.out.printf(setw(13) << sum1 << setw(13) << smax;
//    System.out.printf(setw(13) << sum << endl);
//    System.out.println("ratio of r.m.s. values, expected ratio= ";
//    System.out.printf(setw(12) << sum1/sum2 << setw(13) << sqrt(tot));

    sbeps=1.e-12;
    localflag = sum > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** fourfs: Reverse transform did not agree with fourn()");
      
    }

//    System.out.printf(abs((sum1/sum2)/sqrt(tot)-1.0));
    sbeps=1.e-14;
    localflag = abs((sum1/sum2)/sqrt(tot)-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** fourfs: Incorrect normalization after forward and reverse transform");
      
    }

    for (j=0;j<4;j++) file[j].close();

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
