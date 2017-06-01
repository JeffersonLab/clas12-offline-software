package com.nr.test.test_chapter16;

import static com.nr.NRUtil.buildMatrix;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.Phylo_upgma;

public class Test_Phylo_upgma {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,hamming,mother,ndif,NSEQ=16,NCHAR=16;
    double sbeps=1.e-15;
    // char base[]={'C','G','T','A'};
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
    int[][] sequence=buildMatrix(NSEQ,NCHAR,ssequence);
    double[][] dist=new double[NSEQ][NSEQ];
    boolean localflag, globalflag=false;

    

    // Test Phylo_upgma
    System.out.println("Testing Phylo_upgma");

    // Printout sequences for testing
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
        dist[i][j]=(hamming);
//        System.out.printf(dist[i][j] << " ";
      }
//      System.out.printf(endl;
    }

    // Create the agglomerative phylogenetic tree
    Phylo_upgma tree=new Phylo_upgma(dist);

    // Inspect the tree
//    System.out.println("Root: %f\n", tree.root);
    localflag = tree.root != 30;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_upgma: Tree does not have expected number of nodes");
      
    }

    localflag = (tree.t[tree.root].nel != NSEQ);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_upgma: Root node does not contain all the elements");
      
    }

    localflag = (tree.t[tree.t[tree.root].ldau].nel != NSEQ/2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_upgma: Left side of tree does not report half the elements");
      
    }

    localflag = (tree.t[tree.t[tree.root].rdau].nel != NSEQ/2);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_upgma: Right side of tree does not report half the elements");
      
    }

    localflag = (tree.t[tree.root].modist != 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Phylo_upgma: Mother distance of root is not zero");
      
    }

    for (i=0;i<NSEQ;i++) {
//      System.out.printf(tree.t[i].modist << " %f\n", tree.t[i].ldau << " %f\n", tree.t[i].rdau);
      mother = tree.t[i].mo;
//      System.out.printf(tree.t[tree.t[mother].ldau].modist << " %f\n", tree.t[tree.t[mother].rdau].modist);
      
      localflag = (tree.t[tree.t[mother].ldau].modist != tree.t[tree.t[mother].rdau].modist);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_upgma: Left and right daughter of a leaf reported different mother distances");
        
      }

      localflag = (tree.t[i].modist != 0.5);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_upgma: For this tree, all mother distances should be 0.5");
        
      }

      ndif=0;
      for (j=0;j<NCHAR;j++) 
        if (sequence[tree.t[mother].ldau][j] != sequence[tree.t[mother].rdau][j]) ndif++;
//      System.out.printf(double(ndif)/2.0);

      localflag = abs(tree.t[tree.t[mother].ldau].modist - 0.5*ndif) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Phylo_upgma: Hamming distance between two daughters is not twice the mother distance");
        
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
      fail("*** Phylo_upgma: Leaves of the tree were not encountered in the expected order");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  class Check_tree {
    int i;
    boolean flag;
    Phylo_upgma tree;

    Check_tree(Phylo_upgma ttree) {
      tree=ttree;
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
