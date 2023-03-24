package org.jlab.rec.vtx;

public class VertexFinder {
	
    public VertexFinder() {
            // TODO Auto-generated constructor stub
    }

    public Vertex computeVertex(Particle p1, Particle p2) {
        if(Constants.DEBUGMODE)
            System.out.println("Looking for common vertex for particles \n"+
                            p1.toString()+"\n and "+p2.toString());
        Vertex vtx = null;
        int q = p1.getCharge()+p2.getCharge();

        if(q !=0) {
                return vtx;
        }

        DoubleSwim ds = new DoubleSwim(p1.getVx(),  p1.getVy(), p1.getVz(),
                                    p1.getPx(), p1.getPy(), p1.getPz(), p1.getCharge(),
                                    p2.getVx(), p2.getVy(), p2.getVz(),
                                    p2.getPx(), p2.getPy(), p2.getPz(), p2.getCharge());

        double[][] t = ds.getDoubleSwimVertexes();
        
        if(t==null) return vtx;
            
        double r = Math.sqrt((t[0][0]-t[1][0])*(t[0][0]-t[1][0])
                     +(t[0][1]-t[1][1])*(t[0][1]-t[1][1])
                     +(t[0][2]-t[1][2])*(t[0][2]-t[1][2])  );

        if(Constants.DEBUGMODE) 
                System.out.println("r = "+r);
        
        if(r>Constants.getDOCACUT()) {
            if(Constants.DEBUGMODE) 
                System.out.println("Failed Doca cut = "+Constants.getDOCACUT());
            return vtx;
        } 

        p1.setVx(t[0][0]);
        p1.setVy(t[0][1]);
        p1.setVz(t[0][2]);
        p1.setPx(t[0][3]);
        p1.setPy(t[0][4]);
        p1.setPz(t[0][5]);
        p2.setVx(t[1][0]);
        p2.setVy(t[1][1]);
        p2.setVz(t[1][2]);
        p2.setPx(t[1][3]);
        p2.setPy(t[1][4]);
        p2.setPz(t[1][5]);


        double x = 0.5*(p1.getVx()+p2.getVx());
        double y = 0.5*(p1.getVy()+p2.getVy());
        double z = 0.5*(p1.getVz()+p2.getVz());
        double px = p1.getPx()+p2.getPx();
        double py = p1.getPy()+p2.getPy();
        double pz = p1.getPz()+p2.getPz();

        Particle p0 = new Particle(0, x, y, z, px, py, pz, q);

        vtx = new Vertex(r, p0, p1, p2);

        if(Constants.DEBUGMODE)
            System.out.println("Common vertex for particles \n"+p0.toString()+" for \n"+
                            p1.toString()+"\n and "+p2.toString());
        return vtx;

    } 
} // end class
