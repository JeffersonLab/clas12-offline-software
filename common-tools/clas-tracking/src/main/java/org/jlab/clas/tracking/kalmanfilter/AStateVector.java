package org.jlab.clas.tracking.kalmanfilter;

/**
 *
 * @author devita
 */
public abstract class AStateVector {
           
    private final int k;

    // regular stateComponents
    public double x;
    public double y;
    public double z;
    public double px;
    public double py;
    public double pz;

    // reference point 
    public double x0;
    public double y0;
    public double z0;
    
    public double residual;

    public double[] stateComponents;
    public double[][] covarianceMatrix;


    public AStateVector(int k) {
        this.k = k;
    }

    public AStateVector(AStateVector s) {
        this(s.k);
        this.copy(s);
    }

    public AStateVector(int k, AStateVector s) {
        this(k);
        this.copy(s);
    }

    public AStateVector(int k, double xpivot, double ypivot, double zpivot, AStateVector s) {
        this(k);
        this.copy(s);
        this.setPivot(xpivot, ypivot, zpivot);
    }

        public final void copy(AStateVector s) {
            this.x0 = s.x0;
            this.y0 = s.y0;
            this.z0 = s.z0;
            this.x = s.x;
            this.y = s.y;
            this.z = s.z;
            this.px = s.px;
            this.py = s.py;
            this.pz = s.pz;
            this.residual = s.residual;
            this.copyComponents(s);
            this.copyCovMat(s.covarianceMatrix);
        }
        
        public void copyComponents(AStateVector s) {
            if(s.stateComponents==null) return;
            for(int i=0; i<stateComponents.length; i++)
                this.stateComponents[i] = s.stateComponents[i];
        }

        public void copyCovMat(double[][] c) {
            if(c==null) return;
            int rows = c.length;
            int cols = c[0].length;
            this.covarianceMatrix = new double[rows][cols];
            for (int ir = 0; ir < rows; ir++) {
                for (int ic = 0; ic < cols; ic++) {
                    this.covarianceMatrix[ir][ic] = c[ir][ic];
                }
            }
        }
        
        public abstract void updateCoordinates() ;

        public abstract void updateComponents() ;

        public void pivotTransform() {
            this.setPivot(this.x, this.y, this.z);
        }

        public final void setPivot(double xPivot, double yPivot, double zPivot) {
            x0 = xPivot;
            y0 = yPivot;
            z0 = zPivot;
            this.updateComponents();
        }

        public final int getSize() {
            if(this.stateComponents!=null)
                return this.stateComponents.length;
            else
                return 0;
        }
        
        public double getComponent(int i) {
            return this.stateComponents[i];
        }
}
