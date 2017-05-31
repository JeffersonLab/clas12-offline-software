package com.nr.test.test_chapter16;

import static com.nr.NRUtil.buildMatrix;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.Phylagglom;
import com.nr.ci.Phylo_wpgma;
public class Test_newick {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws IOException {
    int i,j,k,l,m,n,hamming,NSEQ=16,NCHAR=16;
    char base[]={'C','G','T','A'};
    String data,expect="";
    int ssequence[]={
      3,1,1,1,1,1,1,1,0,1,1,1,1,1,2,2,
      3,1,1,1,1,1,1,1,0,1,1,1,1,1,2,3,
      3,0,1,1,1,1,1,1,0,1,1,1,1,1,3,1,
      3,3,1,1,1,1,1,1,0,1,1,1,1,1,3,1,
      3,1,1,1,2,1,1,1,2,1,1,3,1,1,1,1,
      3,1,1,1,0,1,1,1,2,1,1,3,1,1,1,1,
      3,1,1,1,1,1,1,1,2,2,1,0,1,1,1,1,
      3,1,1,1,1,1,1,1,2,0,1,0,1,1,1,1,
      2,1,1,0,1,1,3,1,1,1,1,1,3,1,1,1,
      2,1,1,0,1,1,3,1,1,1,1,1,2,1,1,1,
      2,1,1,0,1,1,0,1,1,1,0,1,1,1,1,1,
      2,1,1,0,1,1,0,1,1,1,3,1,1,1,1,1,
      2,1,3,2,1,1,1,0,1,1,1,1,1,1,1,1,
      2,1,0,2,1,1,1,0,1,1,1,1,1,1,1,1,
      2,1,1,2,1,3,1,3,1,1,1,1,1,1,1,1,
      2,1,1,2,1,0,1,3,1,1,1,1,1,1,1,1
    };
    char[][] str=new char[NSEQ][NCHAR+1];
    int[][] sequence=buildMatrix(NSEQ,NCHAR,ssequence);
    double[][] dist=new double[NSEQ][NSEQ];
    boolean localflag, globalflag=false;

    

    // Test newick
    System.out.println("Testing newick");

    // Calculate hamming distance for all sequence pairs
//    System.out.printf(setprecision(0);
    for (i=0;i<NSEQ;i++) {
      for (j=0;j<NSEQ;j++) {
        hamming=0;
        for (k=0;k<NCHAR;k++) {
          if (sequence[i][k] != sequence[j][k]) hamming++;
        }
        dist[i][j]=(double)(hamming);
        //        System.out.printf(dist[i][j] << " ";
      }
      //      System.out.printf(endl;
    }

    // Create the agglomerative phylogenetic tree
    Phylo_wpgma tree = new Phylo_wpgma(dist);

    // Transfer to file in "newick" format
    for (i=0;i<NSEQ;i++) {
      for (j=0;j<NCHAR;j++)
        str[i][j]=base[sequence[i][j]];
      str[i][NCHAR]='\0';
    }
    Phylagglom.newick(tree,str,"newick.txt");

    // Read the Newick file into a string
    data="";
    Scanner sc = new Scanner(new File("newick.txt"));
    while(sc.hasNext()) {
      data += sc.nextLine();
    }
    sc.close();
    //    System.out.printf(data);

    // Compare with the expected string
    m=0;
    for (i=0;i<2;i++) {
      if (i==0) expect += "(";
      else expect += ",";
      for (j=0;j<2;j++) {
        if (j==0) expect += "(";
        else expect += ",";
        for (k=0;k<2;k++) {
          if (k==0) expect += "(";
          else expect += ",";
          for (l=0;l<2;l++) {
            if (l==0) expect += "(";
            else expect += ",";
            for (n=0;n<NCHAR;n++) {
              expect += base[sequence[m][n]];
            }
            m++;
            expect += ":";
            expect += "0.500000";
          }
          expect += ")";
          expect += ":";
          expect += "1.500000";
        }
        expect += ")";
        expect += ":";
        expect += "2.500000";
      }
      expect += ")";
      expect += ":";
      expect += "3.500000";
    }
    expect += ");";
//    System.out.printf(expect);

    localflag = !data.equals(expect);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** newick: Newick output file did not have expected contents");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
