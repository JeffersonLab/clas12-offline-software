package com.nr.test.test_chapter16;

import static com.nr.NRUtil.buildMatrix;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.Phylagglom;
import com.nr.ci.Phylo_nj;
public class Test_Phylo_nj {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws IOException {
    int i,j,k,hamming,mother,ndif,NSEQ=16,NCHAR=16;
    double sbeps=1.e-15;
    char base[]={'C','G','T','A'};
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

    

    // Test Phylo_nj
    System.out.println("Testing Phylo_nj");

    // Printout sequences used for the test
//    for (i=0;i<NSEQ;i++) {
//      for (j=0;j<NCHAR;j++)
//        System.out.printf(base[sequence[i][j]];
//      System.out.printf(endl;
//    }

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

    // Create the NJ phylogenetic tree
    Phylo_nj tree=new Phylo_nj(dist);

    // Transfer to file in "newick" format
    for (i=0;i<NSEQ;i++) {
      for (j=0;j<NCHAR;j++)
        str[i][j]=base[sequence[i][j]];
      str[i][NCHAR]='\0';
    }
    Phylagglom.newick(tree,str,"newick.txt");

//    System.out.println("parents");
//    for (i=0;i<30;i++) {
//      System.out.printf(i << " %f\n", tree.t[i].mo);
//    }

    // Inspect the tree
//    System.out.println("Root: %f\n", tree.root);
    localflag = tree.root != 30;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Tree does not have expected number of nodes");
      
    }

    localflag = (tree.t[tree.root].nel != NSEQ);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Root node does not contain all the elements");
      
    }

//    System.out.printf(tree.t[tree.t[tree.root].ldau].nel);
    localflag = (tree.t[tree.t[tree.root].ldau].nel != NSEQ-1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Left side of tree should have all but one leaf");
      
    }

    localflag = (tree.t[tree.t[tree.root].rdau].nel != 1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Right side of tree should have one leaf");
      
    }

    localflag = (tree.t[tree.root].modist != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Mother distance of root is not zero");
      
    }

    for (i=0;i<NSEQ;i++) {
      localflag = abs(tree.t[i].modist-0.5) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_nj: Mother distances should be 0.5 for all leaves");
        
      }
    }

    localflag = abs(tree.t[29].modist) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Mother distances of node 29 should be zero");
      
    }
      
    for (i=0;i<NSEQ-2;i++) {
      mother = tree.t[i].mo;
      ndif=0;
      for (j=0;j<NCHAR;j++) 
        if (sequence[tree.t[mother].ldau][j] != sequence[tree.t[mother].rdau][j]) ndif++;

      localflag = abs(tree.t[tree.t[mother].ldau].modist+tree.t[tree.t[mother].rdau].modist
        -(double)(ndif)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_nj: Distance of two daughters is not the sum of the mother distances");
        
      }
    }

//    System.out.println("Tree:");
//    print_tree(tree,tree.root);

//    System.out.println("Check expected order of leaves on tree:");
    Check_tree ct=new Check_tree(tree);
    ct.test(tree.root);

    localflag = ct.flag;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Leaves of the tree were not encountered in the expected order");
      
    }

    i=tree.comancestor(0,7);
//    System.out.println("Common ancestor of 0,7: %f\n", i);
    Phylo_nj rerootedtree=new Phylo_nj(dist,i);

//    System.out.println("Rerooted Tree:");
//    print_tree(rerootedtree,rerootedtree.root);

    //  System.out.println("parents");
//    for (i=0;i<30;i++) {
//      System.out.printf(i << " %f\n", rerootedtree.t[i].mo);
//    }

    // Inspect the tree
    localflag = (rerootedtree.t[rerootedtree.root].nel != NSEQ);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Root node does not contain all the elements");
      
    }

//    System.out.printf(rerootedtree.t[rerootedtree.t[rerootedtree.root].ldau].nel);
    localflag = (rerootedtree.t[rerootedtree.t[rerootedtree.root].ldau].nel != NSEQ/2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Left side of rerooted tree does not report half the elements");
      
    }

//    System.out.printf(rerootedtree.t[rerootedtree.t[rerootedtree.root].rdau].nel);
    localflag = (rerootedtree.t[rerootedtree.t[rerootedtree.root].rdau].nel != NSEQ/2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Right side of rerooted tree does not report half the elements");
      
    }

    localflag = (rerootedtree.t[rerootedtree.root].modist != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_nj: Mother distance of root is not zero");
      
    }

    // Tree is now symmetric
    for (i=0;i<NSEQ;i++) {
      mother = rerootedtree.t[i].mo;
      
      localflag = abs(rerootedtree.t[rerootedtree.t[mother].ldau].modist
        -rerootedtree.t[rerootedtree.t[mother].rdau].modist) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_nj: Left and right daughter of a node reported different mother distances");
        
      }

      localflag = abs(rerootedtree.t[i].modist-0.5) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_nj: Mother distance should be 0.5 for all leaves");
        
      }

      ndif=0;
      for (j=0;j<NCHAR;j++) 
        if (sequence[rerootedtree.t[mother].ldau][j] != sequence[rerootedtree.t[mother].rdau][j]) ndif++;
//      System.out.printf(double(ndif)/2.0);
      localflag = abs(rerootedtree.t[rerootedtree.t[mother].ldau].modist-0.5*ndif) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_nj: Distance of two daughters is not twice the mother distance");
        
      }
    }

    for (i=0;i<2*NSEQ-2;i++) {
      localflag = abs(rerootedtree.t[rerootedtree.t[rerootedtree.t[i].mo].ldau].modist-
        rerootedtree.t[rerootedtree.t[rerootedtree.t[i].mo].rdau].modist) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_nj: Left and right daughter of a node reported different mother distances");
        
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Check_tree {
    int i;
    boolean flag;
    Phylo_nj tree;

    Check_tree(Phylo_nj ttree) {
      tree = ttree;
      i=0;
      flag=false;
    }
    
    void test(int node) {
      if (tree.t[node].ldau != -1) {
        test(tree.t[node].ldau);
      } else { 
        flag = flag || (node != i++);
//        System.out.printf(node << " %f\n", flag);
        return;
      }

      if (tree.t[node].rdau != -1) {
        test(tree.t[node].rdau);
      } else {
        flag = flag || (node != i++);
//        System.out.printf(node << " %f\n", flag);
        return;
      }
    }
  }
}
