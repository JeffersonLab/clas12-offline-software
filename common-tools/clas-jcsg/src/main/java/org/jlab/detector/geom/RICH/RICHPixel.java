package org.jlab.detector.geom.RICH;

import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.volume.G4Box;

import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * @author quasar
 *
 */
// ----------------
public class RICHPixel {
// ----------------
// Define all the properties of a PMT/pixel

    private int nrows = 8;
    private int ncols = 8;
    private double PixelBig = 0.626;
    private double PixelSmall = 0.608;
    private double PixelDeadSpace_x = 0.028;
    private double PixelDeadSpace_y = 0.028;
    private double PhCsize_x=0;
    private double PhCsize_y=0;

    // Define vectors containing the position of Pixels

    private double[] PXDX = new double[8];
    private double[] PXDY = new double[8];

    private double[] PIXEL_X= new double[65];
    private double[] PIXEL_Y= new double[65];
    private double[] PIXEL_Z= new double[65];

    private List<G4Box> Pixel_list = new ArrayList<G4Box>();

    // ----------------
    public RICHPixel () {
    // ----------------
    //Calculate the total size of the photocathode

        this.PhCsize_x = 2*this.PixelBig + 6*this.PixelSmall;
        this.PhCsize_y = 2*this.PixelBig + 6*this.PixelSmall;

        this.PXDX[0] = this.PixelBig;
        this.PXDX[this.ncols-1] = this.PixelBig;

        for (int i = 1; i < this.ncols-1; i++)
        {
            this.PXDX[i] = this.PixelSmall;
        }

        this.PXDY[0] = this.PixelBig;
        this.PXDY[this.nrows-1] = this.PixelBig;
        for (int i = 1; i < this.nrows-1; i++) 
        {
            this.PXDY[i] = this.PixelSmall;
        }

    }


    /**
    * Constructor of the PMT position with respect the PMT corner 
    * @param corner  is the top right vertex of PMT (vertex 3)
    * @param downversor is the versor moving down the PMT
    * @param rightversor is the versor moving right to the PMT
    */
    // ----------------
    public RICHPixel (Vector3d corner, Vector3d downversor, Vector3d rightversor) {
    // ----------------
    
        this();
        int debugMode = 0;

        if(debugMode==1){
            System.out.format("Creating pixel grid \n");
            System.out.format("Corner %8.3f %8.3f %8.3f \n", corner.x, corner.y, corner.z);
            System.out.format("Down   %8.3f %8.3f %8.3f \n", downversor.x, downversor.y, downversor.z);
            System.out.format("Left   %8.3f %8.3f %8.3f \n", rightversor.x, rightversor.y, rightversor.z);
        }
        
        // We should use nr 8 here for defying the nr of pixels
        int nrpixel = this.ncols;       

        int pixelcounter = 1; // Check the nr of the pixel
        Vector3d P3 = new Vector3d(corner); //Copy the position of the vector into another allocation of memory
        P3.add(downversor.times((this.PXDY[0])/2)); //Define the position of the first pixel
        P3.add(rightversor.times((this.PXDX[0])/2)); //Define the position of the first pixel 
        // Pixel array start from 0 (so remember it is pixel 1 in array position 0)
        PIXEL_X[0]=P3.x; 
        PIXEL_Y[0]=P3.y;
        PIXEL_Z[0]=P3.z;
        G4Box PixelBox = new G4Box("Pixel" + 0, this.PXDX[0] / 2, this.PXDY[0] / 2, 0.001);
        PixelBox.translate(P3.x, P3.y, P3.z);
        Pixel_list.add(PixelBox);

        for (int ii=1; ii <= nrpixel; ii++) {          

            double OriginalX = PIXEL_X[(pixelcounter-1)] ;
            double OriginalY = PIXEL_Y[(pixelcounter-1)] ;
            double OriginalZ  = PIXEL_Z[(pixelcounter-1)]  ;

            for (int kk=1; kk < nrpixel; kk++) {                            

                //System.out.println("------ Gonna move down -----");

                // Moving to the right, check the kk+1 pay attention on that
                //I need to move half of the pixel where I start and half of the next
                P3.add(rightversor.times((this.PXDX[kk-1]+this.PXDX[kk])/2));
                
                PIXEL_X[pixelcounter]=P3.x;
                PIXEL_Y[pixelcounter]=P3.y;
                PIXEL_Z[pixelcounter]=P3.z;
                if(ii!=nrpixel) {

                    G4Box PixelBox1 = new G4Box("Pixel" + pixelcounter, this.PXDX[kk-1] / 2, this.PXDY[ii-1] / 2, 0.001);
                    PixelBox1.translate(P3.x, P3.y, P3.z);
                    //System.out.println("Pixel nr " + pixelcounter+ " " + this.PXDX[kk-1] + " "+ this.PXDY[ii-1]);
                    Pixel_list.add(PixelBox1);

                }else{

                    // the last pixel
                    G4Box PixelBox1 = new G4Box("Pixel" + pixelcounter, this.PXDX[0] / 2, this.PXDY[0] / 2, 0.001);
                    PixelBox1.translate(P3.x, P3.y, P3.z);
                    Pixel_list.add(PixelBox1);
                }

                //System.out.println("\n X:"+ Vertex3.x+ " Y: " + Vertex3.y + " Z: "+ Vertex3.z);
                pixelcounter ++;
                //set P3 back to the vertex 3 position
                //P3.set(Vertex3);
                //System.out.println(pixelcounter);

            }

            if(ii!= nrpixel) {

                // Moving down at every ii index
                P3.set(OriginalX, OriginalY, OriginalZ);
                P3.add(downversor.times((this.PXDY[ii-1]+this.PXDY[ii])/2));
                PIXEL_X[pixelcounter]=P3.x;
                PIXEL_Y[pixelcounter]=P3.y;
                PIXEL_Z[pixelcounter]=P3.z;
                G4Box PixelBox2 = new G4Box("Pixel" + pixelcounter, this.PXDX[0] / 2, this.PXDY[ii] / 2, 0.001);
                PixelBox2.translate(P3.x, P3.y, P3.z);

                Pixel_list.add(PixelBox2);
                //System.out.println("Pixel nr " + pixelcounter+ " " + this.PXDX[0] + " "+ this.PXDY[ii]);

                pixelcounter ++;

            }
                //else System.out.println("&&&&&& Attention PMT end");
       }

       //this.show();

    }

       /**
        * starts from 1 
        * @param i the nr of pixel on x
        * @return the position of the pmt
        */

       public double GetXPixel(int i)
       {
              return PXDX[i-1];
       }


       /**
        * starts from 1
        * @param i the nr of pixel on y
        * @return the position of the pmt
        */
       public double GetYPixel(int i)
       {
              return PXDY[i-1];
       }       


    // ----------------
    public Vector3d GetPixelCenter(Vector3d corner, int i) {
    // ----------------
    // PMT anodes run from 1 to 64
    
        Vector3d Center = new Vector3d(PIXEL_X[i-1],PIXEL_Y[i-1],PIXEL_Z[i-1]);
        return corner.plus(Center);
    }       


    // ----------------
    public Vector3d GetPixelCenter(int i) {
    // ----------------
    // PMT anodes run from 1 to 64
    
        Vector3d Center = new Vector3d(PIXEL_X[i-1],PIXEL_Y[i-1],PIXEL_Z[i-1]);
        return Center;
    }       


    // ----------------
    public G4Box GetPixelBox(int i) {
    // ----------------

        return Pixel_list.get(i-1);

    }
       
    // ----------------
    public void show_Pixels(Vector3d ori) {
    // ----------------

        for (int k=0; k<Pixel_list.size(); k++){
            Vector3d vp = ori.plus(new Vector3d( this.PIXEL_X[k], this.PIXEL_Y[k], this.PIXEL_Z[k]) );
            System.out.format("Pixel nr %3d %9.3f %9.3f %9.3f \n", k, vp.x, vp.y, vp.z);
        }

    }
       
}


