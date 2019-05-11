package org.jlab.geom.detector.ec;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;


/**
 * An Electromagnetic Calorimeter (EC) {@link org.jlab.geom.base.Factory Factory}.
 * <p>
 * Factory: <b>{@link org.jlab.geom.detector.ec.ECFactory ECFactory}</b><br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ec.ECDetector ECDetector} → 
 * {@link org.jlab.geom.detector.ec.ECSector ECSector} → 
 * {@link org.jlab.geom.detector.ec.ECSuperlayer ECSuperlayer} → 
 * {@link org.jlab.geom.detector.ec.ECLayer ECLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * <p>
 * The outer surface of the scintillator paddles are coated with a reflective 
 * layer of plastic which is less than 0.25 mm thick. This coating is neglected
 * by this factory and the coating is included in the 
 * {@code ScintillatorPaddle}s produced.
 * 
 * @author jnhankins
 */
public class ECFactory implements Factory<ECDetector, ECSector, ECSuperlayer, ECLayer> {

    @Override
    public ECDetector createDetectorCLAS(final ConstantProvider cp) {
        ECDetector detector = createDetectorSector(cp);
        for (ECSector sector : detector.getAllSectors()) {
            for (ECSuperlayer superlayer : sector.getAllSuperlayers()) {
                for (ECLayer layer: superlayer.getAllLayers()) {
                    int sectorId = superlayer.getSectorId();
                    Transformation3D trans = layer.getTransformation();
                    trans.rotateZ(Math.toRadians(60*sectorId));
                    layer.setTransformation(trans);
                }
            }
        }
        return detector;
    }
    
    @Override
    public ECDetector createDetectorSector(ConstantProvider cp) {
        ECDetector detector = createDetectorTilted(cp);
        for (ECSector sector: detector.getAllSectors()) {
            for (ECSuperlayer superlayer: sector.getAllSuperlayers()) {
                for (ECLayer layer: superlayer.getAllLayers()) {
                    double thtilt;
                    if (superlayer.getSuperlayerId() == 0)
                        thtilt = Math.toRadians(cp.getDouble("/geometry/pcal/pcal/thtilt", 0));
                    else
                        thtilt = Math.toRadians(cp.getDouble("/geometry/ec/ec/thtilt", 0));
                    Transformation3D trans = layer.getTransformation();
                    trans.rotateY(thtilt);
                    layer.setTransformation(trans);
                }
            }
        }
        return detector;
    }
    
    @Override
    public ECDetector createDetectorTilted(ConstantProvider cp) {
        ECDetector detector = createDetectorLocal(cp);
        for (ECSector sector: detector.getAllSectors()) {
            for (ECSuperlayer superlayer: sector.getAllSuperlayers()) {
                Transformation3D trans = new Transformation3D();
                if (superlayer.getSuperlayerId() == 0) {
                    double dist2tgt = cp.getDouble("/geometry/pcal/pcal/dist2tgt", 0)*0.1;
                    double deltaX   = cp.getDouble("/geometry/pcal/alignment/deltaX", sector.getSectorId()); // displacements are already in cm
                    double deltaY   = cp.getDouble("/geometry/pcal/alignment/deltaY", sector.getSectorId()); 
                    double deltaZ   = cp.getDouble("/geometry/pcal/alignment/deltaZ", sector.getSectorId()); 
                    double rotX     = cp.getDouble("/geometry/pcal/alignment/rotX", sector.getSectorId());   // rotations are in degrees
                    double rotY     = cp.getDouble("/geometry/pcal/alignment/rotY", sector.getSectorId()); 
                    double rotZ     = cp.getDouble("/geometry/pcal/alignment/rotZ", sector.getSectorId()); 
                    trans.rotateX(Math.toRadians(rotX));
                    trans.rotateY(Math.toRadians(rotY));
                    trans.rotateZ(Math.toRadians(rotZ));
                    trans.translateXYZ(deltaX, deltaY, deltaZ + dist2tgt);
                } else if (superlayer.getSuperlayerId() == 1) {
                    double dist2tgt = cp.getDouble("/geometry/ec/ec/dist2tgt", 0)*0.1;
                    double deltaX   = cp.getDouble("/geometry/ec/alignment/deltaX", sector.getSectorId()); // displacements are already in cm
                    double deltaY   = cp.getDouble("/geometry/ec/alignment/deltaY", sector.getSectorId()); 
                    double deltaZ   = cp.getDouble("/geometry/ec/alignment/deltaZ", sector.getSectorId()); 
                    double rotX     = cp.getDouble("/geometry/ec/alignment/rotX", sector.getSectorId());   // rotations are in degrees
                    double rotY     = cp.getDouble("/geometry/ec/alignment/rotY", sector.getSectorId()); 
                    double rotZ     = cp.getDouble("/geometry/ec/alignment/rotZ", sector.getSectorId()); 
                    trans.rotateX(Math.toRadians(rotX));
                    trans.rotateY(Math.toRadians(rotY));
                    trans.rotateZ(Math.toRadians(rotZ));
                    trans.translateXYZ(deltaX, deltaY, deltaZ + dist2tgt);
                } else {
                    double dist2tgt = cp.getDouble("/geometry/ec/ec/dist2tgt", 0)*0.1;
                    double strip_thick = cp.getDouble("/geometry/ec/ec/strip_thick", 0)*0.1;
                    double lead_thick = cp.getDouble("/geometry/ec/ec/lead_thick", 0)*0.1;
                    double deltaX   = cp.getDouble("/geometry/ec/alignment/deltaX", sector.getSectorId()); // displacements are already in cm
                    double deltaY   = cp.getDouble("/geometry/ec/alignment/deltaY", sector.getSectorId()); 
                    double deltaZ   = cp.getDouble("/geometry/ec/alignment/deltaZ", sector.getSectorId()); 
                    double rotX     = cp.getDouble("/geometry/ec/alignment/rotX", sector.getSectorId());   // rotations are in degrees
                    double rotY     = cp.getDouble("/geometry/ec/alignment/rotY", sector.getSectorId()); 
                    double rotZ     = cp.getDouble("/geometry/ec/alignment/rotZ", sector.getSectorId()); 
                    double dz = (strip_thick + lead_thick) * 15;
                    trans.rotateX(Math.toRadians(rotX));
                    trans.rotateY(Math.toRadians(rotY));
                    trans.rotateZ(Math.toRadians(rotZ));
                    trans.translateXYZ(deltaX, deltaY, deltaZ + dist2tgt + dz);
                }
                superlayer.setTransformation(trans);
            }
        }
        return detector;
    }
            
    @Override
    public ECDetector createDetectorLocal(ConstantProvider cp) {
        ECDetector detector = new ECDetector();
        for (int sectorId=0; sectorId<6; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    @Override
    public ECSector createSector(ConstantProvider cp, int sectorId) {
        if (!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        ECSector sector = new ECSector(sectorId);
        for (int superlayerId=0; superlayerId<3; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }

    @Override
    public ECSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if (!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if (!(0<=superlayerId && superlayerId<3))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        ECSuperlayer superlayer = new ECSuperlayer(sectorId, superlayerId);
        /*
        int numLayers = (superlayerId == 0)? 15 : // PCAL
                        (superlayerId == 1)? 15 : // Inner EC
                        (superlayerId == 2)? 24 : // Outer EC
                        -1; // ??
        */
        int numLayers = (superlayerId == 0)? 3 : // PCAL
                        (superlayerId == 1)? 3 : // Inner EC
                        (superlayerId == 2)? 3 : // Outer EC
                        -1; // ??
        for (int layerId=0; layerId<numLayers; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }

    @Override
    public ECLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if (!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if (!(0<=superlayerId && superlayerId<3))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
//        if (!(0<=layerId && layerId<3))
//            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        if (superlayerId==0 && !(0<=layerId && layerId<15)) // PCAL
            throw new IllegalArgumentException("Error: invalid invalid superlayer="+superlayerId+" layer="+layerId);
        if (superlayerId==1 && !(0<=layerId && layerId<15)) // Inner EC
            throw new IllegalArgumentException("Error: invalid invalid superlayer="+superlayerId+" layer="+layerId);
        if (superlayerId==2 && !(0<=layerId && layerId<24)) // Outer EC
            throw new IllegalArgumentException("Error: invalid invalid superlayer="+superlayerId+" layer="+layerId);
        
//        System.out.println(sectorId+" "+superlayerId+" "+layerId);
        switch (superlayerId) {
            case 0: switch(layerId%3) {
                case 0: return createPCALU(cp, sectorId, layerId);
                case 1: return createPCALV(cp, sectorId, layerId);
                case 2: return createPCALW(cp, sectorId, layerId);
            }
            case 1: switch(layerId%3) {
                case 0: return createECU(cp, sectorId, layerId, layerId);
                case 1: return createECV(cp, sectorId, layerId, layerId);
                case 2: return createECW(cp, sectorId, layerId, layerId);
            }
            case 2: switch(layerId%3) {
                case 0: return createECU(cp, sectorId, layerId, layerId+15);
                case 1: return createECV(cp, sectorId, layerId, layerId+15);
                case 2: return createECW(cp, sectorId, layerId, layerId+15);
            }
        }
        throw new RuntimeException("not implemented: superlayer="+superlayerId+" layer="+layerId);
    }
    
    private static ECLayer createPCALU(ConstantProvider cp, int sectorId, int layerId) {
        final int numPaddlesPCalU = 68;
        final int numDoublePaddlesPCalU = 16;
        final double strip_thick = cp.getDouble("/geometry/pcal/pcal/strip_thick", 0)*0.1;
        final double steel_thick = cp.getDouble("/geometry/pcal/pcal/steel_thick", 0)*0.1;
        final double strip_width = cp.getDouble("/geometry/pcal/pcal/strip_width", 0)*0.1;
        final double max_length = cp.getDouble("/geometry/pcal/Uview/max_length", 0)*0.1;
        final double yhigh = cp.getDouble("/geometry/pcal/pcal/yhigh", 0)*0.1;
        final double dz = (strip_thick+steel_thick)*layerId;
        
        ECLayer layer = new ECLayer(sectorId, 0, layerId);
        
        // Create the u-view paddles such that paddleId 0 corresponds to the 
        // shortest paddle and the first readout channel
        for (int paddleId = 0; paddleId < numPaddlesPCalU; paddleId++) {
            double lengthShorter;
            double lengthLonger;
            double width;
            double yOff;
            int paddleIdInv = numPaddlesPCalU-paddleId-1;
            if (paddleIdInv < numDoublePaddlesPCalU) {
                lengthLonger = calcLengthPCALU(max_length, strip_width, 2*paddleIdInv);
                lengthShorter = calcLengthPCALU(max_length, strip_width, 2*paddleIdInv+2);
                width = strip_width*2;
                yOff = paddleIdInv*strip_width*2;
            } else {
                lengthLonger = calcLengthPCALU(max_length, strip_width, numDoublePaddlesPCalU+paddleIdInv);
                lengthShorter = calcLengthPCALU(max_length, strip_width, numDoublePaddlesPCalU+paddleIdInv+1);
                width = strip_width;
                yOff = (numDoublePaddlesPCalU+paddleIdInv)*strip_width;
            }

            // Create the points that define the paddle's trapazoidal shape
            Point3D p0 = new Point3D(-width, -lengthShorter*0.5, 0);           // - - -
            Point3D p1 = new Point3D( 0,     -lengthLonger*0.5,  0);           // + - -
            Point3D p2 = new Point3D( 0,     -lengthLonger*0.5,  strip_thick); // + - +
            Point3D p3 = new Point3D(-width, -lengthShorter*0.5, strip_thick); // - - +
            
            Point3D p4 = new Point3D(-width,  lengthShorter*0.5, 0);           // - + -
            Point3D p5 = new Point3D( 0,      lengthLonger*0.5,  0);           // + + -
            Point3D p6 = new Point3D( 0,      lengthLonger*0.5,  strip_thick); // + + +
            Point3D p7 = new Point3D(-width,  lengthShorter*0.5, strip_thick); // - + +
            
            // Create the paddle
            ScintillatorPaddle paddle = new ScintillatorPaddle(paddleId, p0, p1, p2, p3, p4, p5, p6, p7);

            // Translate the paddle upwards into position with its upstream face
            // flush with the xy-plane.
            paddle.translateXYZ(yhigh - yOff, 0, dz);
            
            // Add the paddle to the list
            layer.addComponent(paddle);
        }
        
        makePcalPlaneAndBoundary(cp, layer, dz);
        
        return layer;
    }
    
    private static ECLayer createPCALV(ConstantProvider cp, int sectorId, int layerId) {
        final int numPaddlesPCalVW = 62;
        final int numSinglePaddlesPCalVW = 47;
        final double strip_thick = cp.getDouble("/geometry/pcal/pcal/strip_thick", 0)*0.1;
        final double steel_thick = cp.getDouble("/geometry/pcal/pcal/steel_thick", 0)*0.1;
        final double strip_width = cp.getDouble("/geometry/pcal/pcal/strip_width", 0)*0.1;
        final double max_length = cp.getDouble("/geometry/pcal/Vview/max_length", 0)*0.1;
        final double yhigh = cp.getDouble("/geometry/pcal/pcal/yhigh", 0)*0.1;
        final double view_angle = Math.toRadians(cp.getDouble("/geometry/pcal/pcal/view_angle", 0));
        
        final double l0u = cp.getDouble("/geometry/pcal/Uview/max_length", 0)*0.1;
        final double height = l0u*0.5*Math.tan(view_angle);
        final double ylo = height - yhigh;
        final double dz = (strip_thick+steel_thick)*layerId;
        
        ECLayer layer = new ECLayer(sectorId, 0, layerId);
        
        // Create the v-view paddles such that paddleId 0 corresponds to the 
        // shortest paddle and the first readout channel
        for (int paddleId = 0; paddleId < numPaddlesPCalVW; paddleId++) {

            double lengthLonger;
            double width;
            double yOff;
            int paddleIdInv = numPaddlesPCalVW-paddleId-1;
            if (paddleIdInv < numSinglePaddlesPCalVW) {
                lengthLonger = calcLengthPCALVW(max_length, strip_width, paddleIdInv);
                width = strip_width;
                yOff = paddleIdInv*strip_width;
            } else {
                lengthLonger = calcLengthPCALVW(max_length, strip_width, 2*paddleIdInv-numSinglePaddlesPCalVW);
                width = strip_width*2;
                yOff = (2*paddleIdInv-numSinglePaddlesPCalVW)*strip_width;
            }

            // Create the points that define the paddle's trapazoidal shape
            // l - left, r - right, u - up, d - down, f - far, n - near
            double topAngle = Math.toRadians(90)-view_angle; 
            double botAngle = 2*view_angle-Math.toRadians(90);
            double top = lengthLonger - width*Math.tan(topAngle);
            double bot = width*Math.tan(botAngle);
            Point3D p0 = new Point3D(-width, bot,          0);           // - - -
            Point3D p1 = new Point3D( 0,     0,            0);           // + - -
            Point3D p2 = new Point3D( 0,     0,            strip_thick); // + - +
            Point3D p3 = new Point3D(-width, bot,          strip_thick); // - - +
            Point3D p4 = new Point3D(-width, top,          0);           // - + -
            Point3D p5 = new Point3D( 0,     lengthLonger, 0);           // + + -
            Point3D p6 = new Point3D( 0,     lengthLonger, strip_thick); // + + +
            Point3D p7 = new Point3D(-width, top,          strip_thick); // - + +

            // Create the paddle
            ScintillatorPaddle paddle = new ScintillatorPaddle(paddleId, p0, p1, p2, p3, p4, p5, p6, p7);

            // Rotate it so that its direction vector is parallel with the
            // x-axis in an intermediate coordinate system defined as:
            //    the z-axis is the same as the PCAL z-axis
            //    the x-axis is parallel with the v-axis
            //    the y-axis is points towards the rest of the triangle
            //    the origin is at the apex of the isosceles triangle
            paddle.rotateZ(Math.toRadians(-90));

            // Translate the paddle into position in the triangle in the
            // intermediate coordinate system.
            double gamma = 1/Math.tan(Math.toRadians(180)-2*view_angle);
            paddle.translateXYZ(yOff*gamma, yOff, dz);

            // Transform the coordinate system from from the intermediate 
            // coordinates to PCAL coordinates
            paddle.rotateZ(view_angle - Math.toRadians(90));
            paddle.translateXYZ(-ylo, 0, 0);

            // Add the paddle to the list
            layer.addComponent(paddle);
        }
        
        makePcalPlaneAndBoundary(cp, layer, dz);
        
        return layer;
    }
    private static ECLayer createPCALW(ConstantProvider cp, int sectorId, int layerId) {
        final int numPaddlesPCalVW = 62;
        final int numSinglePaddlesPCalVW = 47;
        final double strip_thick = cp.getDouble("/geometry/pcal/pcal/strip_thick", 0)*0.1;
        final double steel_thick = cp.getDouble("/geometry/pcal/pcal/steel_thick", 0)*0.1;
        final double strip_width = cp.getDouble("/geometry/pcal/pcal/strip_width", 0)*0.1;
        final double max_length = cp.getDouble("/geometry/pcal/Wview/max_length", 0)*0.1;
        final double yhigh = cp.getDouble("/geometry/pcal/pcal/yhigh", 0)*0.1;
        final double view_angle = Math.toRadians(cp.getDouble("/geometry/pcal/pcal/view_angle", 0));
        
        final double l0u = cp.getDouble("/geometry/pcal/Uview/max_length", 0)*0.1;
        final double height = l0u*0.5*Math.tan(view_angle);
        final double ylo = height - yhigh;
        
        final double dz = (strip_thick+steel_thick)*layerId;
        
        ECLayer layer = new ECLayer(sectorId, 0, layerId);
        
        // Create the w-view paddles such that paddleId 0 corresponds to the 
        // shortest paddle and the first readout channel
        for (int paddleId = 0; paddleId < numPaddlesPCalVW; paddleId++) {

            double lengthLonger;
            double width;
            double yOff;
            int paddleIdInv = numPaddlesPCalVW-paddleId-1;
            if (paddleIdInv < numSinglePaddlesPCalVW) {
                lengthLonger = calcLengthPCALVW(max_length, strip_width, paddleIdInv);
                width = strip_width;
                yOff = paddleIdInv*strip_width;
            } else {
                lengthLonger = calcLengthPCALVW(max_length, strip_width, 2*paddleIdInv-numSinglePaddlesPCalVW);
                width = strip_width*2;
                yOff = (2*paddleIdInv-numSinglePaddlesPCalVW)*strip_width;
            }

            // Create the points that define the paddle's trapazoidal shape
            // l - left, r - right, u - up, d - down, f - far, n - near
            double topAngle = Math.toRadians(90)-view_angle; 
            double botAngle = 2*view_angle-Math.toRadians(90);
            double top = lengthLonger - width*Math.tan(topAngle);
            double bot = width*Math.tan(botAngle);
            Point3D p1 = new Point3D( 0,    0,            0);           // - - -
            Point3D p2 = new Point3D(width, bot,          0);           // + - -
            Point3D p3 = new Point3D(width, bot,          strip_thick); // + - +
            Point3D p4 = new Point3D( 0,    0,            strip_thick); // - - +
            Point3D p5 = new Point3D( 0,    lengthLonger, 0);           // - + -
            Point3D p6 = new Point3D(width, top,          0);           // + + -
            Point3D p7 = new Point3D(width, top,          strip_thick); // + + +
            Point3D p8 = new Point3D( 0,    lengthLonger, strip_thick); // - + +

            // Create the paddle
            ScintillatorPaddle paddle = new ScintillatorPaddle(paddleId, p1, p2, p3, p4, p5, p6, p7, p8);

            // Rotate it so that its direction vector is antiparallel with 
            // the x-axis in an intermediate coordinate system defined as:
            //    the z-axis is the same as the PCAL z-axis
            //    the x-axis is parallel with the v-axis
            //    the y-axis is points towards the rest of the triangle
            //    the origin is at the apex of the isosceles triangle
            paddle.rotateZ(Math.toRadians(90));

            // Translate the paddle into position in the triangle in the
            // intermediate coordinate system.
            double gamma = -1/Math.tan(Math.toRadians(180)-2*view_angle);
            paddle.translateXYZ(yOff*gamma, yOff, dz);

            // Transform the coordinate system from from the intermediate 
            // coordinates to PCAL coordinates
            paddle.rotateZ(-view_angle - Math.toRadians(90));
            paddle.translateXYZ(-ylo, 0, 0);

            // Add the paddle to the list
            layer.addComponent(paddle);
        }
        
        makePcalPlaneAndBoundary(cp, layer, dz);
        
        return layer;
    }
    private static double calcLengthPCALU(double L1, double width, int paddleId) {
        // Equation given in "The Geometry of the CLAS12 Pre-shower Calorimeter"
        // https://clasweb.jlab.org/wiki/images/d/d0/Pcal_geometry_note.pdf
        return L1 - 2*paddleId*width*Math.tan(Math.toRadians(27.1));
    }
    private static double calcLengthPCALVW(double L1, double width, int paddleId) {
        // Equation given in "The Geometry of the CLAS12 Pre-shower Calorimeter"
        // https://clasweb.jlab.org/wiki/images/d/d0/Pcal_geometry_note.pdf
        return L1 - paddleId*width*(Math.tan(Math.toRadians(27.1))+Math.tan(Math.toRadians(35.8)));
    }
    private static void makePcalPlaneAndBoundary(ConstantProvider cp, ECLayer layer, double depth) {
        final double u_length = cp.getDouble("/geometry/pcal/Uview/max_length", 0)*0.1;
        final double yhigh = cp.getDouble("/geometry/pcal/pcal/yhigh", 0)*0.1;
        final double view_angle = Math.toRadians(cp.getDouble("/geometry/pcal/pcal/view_angle", 0));
        
        double bz = depth;
        double by = u_length/2;
        double bx = yhigh - by*Math.tan(view_angle);
        
        Point3D b0 = new Point3D(bx,      0, bz);
        Point3D b1 = new Point3D(yhigh,  by, bz);
        Point3D b2 = new Point3D(yhigh, -by, bz);
        layer.getBoundary().addFace(new Triangle3D(b0, b1, b2));
        
        layer.getPlane().set(0, 0, bz, 0, 0, 1);
    }
    
    private static ECLayer createECU(ConstantProvider cp, int sectorId, int layerId, int realLayer) {
        int superlayerId = realLayer < 15? 1 : 2;
        ECLayer layer = new ECLayer(sectorId, superlayerId, layerId);

        final double view_angle = Math.toRadians(cp.getDouble("/geometry/ec/ec/view_angle", 0));
        final double strip_thick = cp.getDouble("/geometry/ec/ec/strip_thick", 0)*0.1;
        final double lead_thick = cp.getDouble("/geometry/ec/ec/lead_thick", 0)*0.1;
        final double dist2cnt = cp.getDouble("/geometry/ec/ec/dist2cnt", 0)*0.1;
        final double a1 = cp.getDouble("/geometry/ec/ec/a1", 0)*0.1;
        final double a2 = cp.getDouble("/geometry/ec/ec/a2", 0)*0.1;
        final double a3 = cp.getDouble("/geometry/ec/ec/a3", 0)*0.1;
        final double a4 = cp.getDouble("/geometry/ec/uview/a4", 0)*0.1;
        final double a5 = cp.getDouble("/geometry/ec/uview/a5", 0)*0.1;
        final double a6 = cp.getDouble("/geometry/ec/uview/a6", 0)*0.1;
        final int nstrips = cp.getInteger("/geometry/ec/uview/nstrips", 0);
        
        double dz = (strip_thick + lead_thick) * layerId;
        double tant  = Math.tan(view_angle);
        double ycent = a1*realLayer;
        double dy    = a2 + a3*realLayer;
        double dx    = 2*dy/tant;
        double wu    = a5 + a6*realLayer; // width
        
        Point3D pA = new Point3D(  0, ycent-dy, 0);
        Point3D pB = new Point3D( dx, ycent+dy, 0);
        Point3D pC = new Point3D(-dx, ycent+dy, 0);
        
        for (int paddleId = 0; paddleId < nstrips; paddleId++) {
            double yl = -a2 - a4*realLayer + wu*(paddleId);
            double yh = -a2 - a4*realLayer + wu*(paddleId+1);
            double xl = (yl - ycent + dy)/tant;
            double xh = (yh - ycent + dy)/tant;
            
            // WORKARROUND
            xl = Math.max(xl, 0.00001);
            
            Point3D p0 = new Point3D( xl, yl, 0);
            Point3D p1 = new Point3D( xh, yh, 0);
            Point3D p2 = new Point3D( xh, yh, strip_thick);
            Point3D p3 = new Point3D( xl, yl, strip_thick);
            Point3D p4 = new Point3D(-xl, yl, 0);
            Point3D p5 = new Point3D(-xh, yh, 0);
            Point3D p6 = new Point3D(-xh, yh, strip_thick);
            Point3D p7 = new Point3D(-xl, yl, strip_thick);
            
            ScintillatorPaddle paddle = new ScintillatorPaddle(paddleId, p0, p1, p2, p3, p4, p5, p6, p7);
            
            paddle.translateXYZ(0, -dist2cnt, dz);
            paddle.rotateZ(Math.toRadians(-90));
            
            layer.addComponent(paddle);
        }
      
        layer.getPlane().set(0, 0, 0, 0, 0, 1);
        layer.getPlane().translateXYZ(0, 0, dz);
        layer.getPlane().rotateZ(Math.toRadians(-90));
        
        layer.getBoundary().addFace(new Triangle3D(pA, pC, pB));
        layer.getBoundary().translateXYZ(0, -dist2cnt, dz);
        layer.getBoundary().rotateZ(Math.toRadians(-90));

        return layer;
    }
    private static ECLayer createECV(ConstantProvider cp, int sectorId, int layerId, int realLayer) {
        int superlayerId = realLayer < 15? 1 : 2;
        ECLayer layer = new ECLayer(sectorId, superlayerId, layerId);

        final double view_angle = Math.toRadians(cp.getDouble("/geometry/ec/ec/view_angle", 0));
        final double strip_thick = cp.getDouble("/geometry/ec/ec/strip_thick", 0)*0.1;
        final double lead_thick = cp.getDouble("/geometry/ec/ec/lead_thick", 0)*0.1;
        final double dist2cnt = cp.getDouble("/geometry/ec/ec/dist2cnt", 0)*0.1;
        final double a1 = cp.getDouble("/geometry/ec/ec/a1", 0)*0.1;
        final double a2 = cp.getDouble("/geometry/ec/ec/a2", 0)*0.1;
        final double a3 = cp.getDouble("/geometry/ec/ec/a3", 0)*0.1;
        final double a5 = cp.getDouble("/geometry/ec/vview/a5", 0)*0.1;
        final double a6 = cp.getDouble("/geometry/ec/vview/a6", 0)*0.1;
        final int nstrips = cp.getInteger("/geometry/ec/vview/nstrips", 0);
        
        double dz = (strip_thick + lead_thick) * layerId;
        double tant  = Math.tan(view_angle);
        double dy    = a2 + a3*realLayer;
        double dx    = 2*dy/tant;
        double ycent = a1*realLayer;
        
        Point3D pA = new Point3D(  0, ycent-dy, 0);
        Point3D pB = new Point3D( dx, ycent+dy, 0);
        Point3D pC = new Point3D(-dx, ycent+dy, 0);
        Vector3D nR = pC.vectorTo(pA).cross(new Vector3D(0, 0, 1));
        Plane3D pR = new Plane3D(pA, nR);
        Plane3D pT = new Plane3D(0, ycent+dy, 0, 0, 1, 0);
        
        double wv    = (a5 + a6*(realLayer-1)); 
        double sqrt1tan2 = Math.sqrt(1+tant*tant);
        
        Line3D line = new Line3D();
        Point3D p0 = new Point3D();
        Point3D p1 = new Point3D();
        Point3D p2 = new Point3D();
        Point3D p3 = new Point3D();
        Point3D p4 = new Point3D();
        Point3D p5 = new Point3D();
        Point3D p6 = new Point3D();
        Point3D p7 = new Point3D();
        for (int paddleId = 0; paddleId < nstrips; paddleId++) {
            
            line.setOrigin(300, ycent - dy + wv*(36-paddleId)*sqrt1tan2 + tant*300, 0);
            line.setEnd(  -300, ycent - dy + wv*(36-paddleId)*sqrt1tan2 - tant*300, 0);
            pR.intersection(line, p0);
            pT.intersection(line, p4);
            
            line.setOrigin(300, ycent - dy + wv*(36-paddleId-1)*sqrt1tan2 + tant*300, 0);
            line.setEnd(  -300, ycent - dy + wv*(36-paddleId-1)*sqrt1tan2 - tant*300, 0);
            pR.intersection(line, p1);
            pT.intersection(line, p5);
            
            line.setOrigin(300, ycent - dy + wv*(36-paddleId-1)*sqrt1tan2 + tant*300, strip_thick);
            line.setEnd(  -300, ycent - dy + wv*(36-paddleId-1)*sqrt1tan2 - tant*300, strip_thick);
            pR.intersection(line, p2);
            pT.intersection(line, p6);
            
            line.setOrigin(300, ycent - dy + wv*(36-paddleId)*sqrt1tan2 + tant*300, strip_thick);
            line.setEnd(  -300, ycent - dy + wv*(36-paddleId)*sqrt1tan2 - tant*300, strip_thick);
            pR.intersection(line, p3);
            pT.intersection(line, p7);
            
            // WORKARROUND
            if (paddleId == 0) {
                p0.copy(p0.lerp(pC, 1.01));
                p4.copy(p4.lerp(pC, 1.01));
                p3.copy(p0);
                p7.copy(p4);
                p3.setZ(strip_thick);
                p7.setZ(strip_thick);
            }
            
            ScintillatorPaddle paddle = new ScintillatorPaddle(paddleId, p0, p1, p2, p3, p4, p5, p6, p7);
            
            paddle.translateXYZ(0, -dist2cnt, dz);
            paddle.rotateZ(Math.toRadians(-90));
            
            layer.addComponent(paddle);
        }
      
        layer.getPlane().set(0, 0, 0, 0, 0, 1);
        layer.getPlane().translateXYZ(0, 0, dz);
        layer.getPlane().rotateZ(Math.toRadians(-90));
        
        layer.getBoundary().addFace(new Triangle3D(pA, pC, pB));
        layer.getBoundary().translateXYZ(0, -dist2cnt, dz);
        layer.getBoundary().rotateZ(Math.toRadians(-90));

        return layer;
    }
    private static ECLayer createECW(ConstantProvider cp, int sectorId, int layerId, int realLayer) {
        int superlayerId = realLayer < 15? 1 : 2;
        ECLayer layer = new ECLayer(sectorId, superlayerId, layerId);

        final double view_angle = Math.toRadians(cp.getDouble("/geometry/ec/ec/view_angle", 0));
        final double strip_thick = cp.getDouble("/geometry/ec/ec/strip_thick", 0)*0.1;
        final double lead_thick = cp.getDouble("/geometry/ec/ec/lead_thick", 0)*0.1;
        final double dist2cnt = cp.getDouble("/geometry/ec/ec/dist2cnt", 0)*0.1;
        final double a1 = cp.getDouble("/geometry/ec/ec/a1", 0)*0.1;
        final double a2 = cp.getDouble("/geometry/ec/ec/a2", 0)*0.1;
        final double a3 = cp.getDouble("/geometry/ec/ec/a3", 0)*0.1;
        final double a5 = cp.getDouble("/geometry/ec/wview/a5", 0)*0.1;
        final double a6 = cp.getDouble("/geometry/ec/wview/a6", 0)*0.1;
        final int nstrips = cp.getInteger("/geometry/ec/wview/nstrips", 0);
        
        double dz = (strip_thick + lead_thick) * layerId;
        double tant  = Math.tan(view_angle);
        double dy    = a2 + a3*realLayer;
        double dx    = 2*dy/tant;
        double ycent = a1*realLayer;
        
        Point3D pA = new Point3D(  0, ycent-dy, 0);
        Point3D pB = new Point3D( dx, ycent+dy, 0);
        Point3D pC = new Point3D(-dx, ycent+dy, 0);
        Vector3D nL = pA.vectorTo(pB).cross(new Vector3D(0, 0, 1));
        Plane3D pL = new Plane3D(pA, nL);
        Plane3D pT = new Plane3D(0, ycent+dy, 0, 0, 1, 0);
        
        double ww    = (a5 + a6*(realLayer-1)); 
        double sqrt1tan2 = Math.sqrt(1+tant*tant);
        
        Line3D line = new Line3D();
        Point3D p0 = new Point3D();
        Point3D p1 = new Point3D();
        Point3D p2 = new Point3D();
        Point3D p3 = new Point3D();
        Point3D p4 = new Point3D();
        Point3D p5 = new Point3D();
        Point3D p6 = new Point3D();
        Point3D p7 = new Point3D();
        for (int paddleId = 0; paddleId < nstrips; paddleId++) {
            
            line.setOrigin(300, ycent - dy + ww*(36-paddleId)*sqrt1tan2 - tant*300, 0);
            line.setEnd(  -300, ycent - dy + ww*(36-paddleId)*sqrt1tan2 + tant*300, 0);
            pT.intersection(line, p0);
            pL.intersection(line, p4);
            
            line.setOrigin(300, ycent - dy + ww*(36-paddleId-1)*sqrt1tan2 - tant*300, 0);
            line.setEnd(  -300, ycent - dy + ww*(36-paddleId-1)*sqrt1tan2 + tant*300, 0);
            pT.intersection(line, p1);
            pL.intersection(line, p5);
            
            line.setOrigin(300, ycent - dy + ww*(36-paddleId-1)*sqrt1tan2 - tant*300, strip_thick);
            line.setEnd(  -300, ycent - dy + ww*(36-paddleId-1)*sqrt1tan2 + tant*300, strip_thick);
            pT.intersection(line, p2);
            pL.intersection(line, p6);
            
            line.setOrigin(300, ycent - dy + ww*(36-paddleId)*sqrt1tan2 - tant*300, strip_thick);
            line.setEnd(  -300, ycent - dy + ww*(36-paddleId)*sqrt1tan2 + tant*300, strip_thick);
            pT.intersection(line, p3);
            pL.intersection(line, p7);
            
            // WORKARROUND
            if (paddleId == 0) {
                p0.copy(p0.lerp(pB, 1.01));
                p4.copy(p4.lerp(pB, 1.01));
                p3.copy(p0);
                p7.copy(p4);
                p3.setZ(strip_thick);
                p7.setZ(strip_thick);
            }
            
            ScintillatorPaddle paddle = new ScintillatorPaddle(paddleId, p0, p1, p2, p3, p4, p5, p6, p7);
            
            paddle.translateXYZ(0, -dist2cnt, dz);
            paddle.rotateZ(Math.toRadians(-90));
            
            layer.addComponent(paddle);
        }
      
        layer.getPlane().set(0, 0, 0, 0, 0, 1);
        layer.getPlane().translateXYZ(0, 0, dz);
        layer.getPlane().rotateZ(Math.toRadians(-90));
        
        layer.getBoundary().addFace(new Triangle3D(pA, pC, pB));
        layer.getBoundary().translateXYZ(0, -dist2cnt, dz);
        layer.getBoundary().rotateZ(Math.toRadians(-90));
        
        return layer;
    }

    /**
     * Returns "EC Factory".
     * @return "EC Factory"
     */
    @Override
    public String getType() {
        return "EC Factory";
    }

    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        return getType();
    }    

    @Override
    public Transformation3D getTransformation(ConstantProvider cp, int sector, int superlayer, int layer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DetectorTransformation getDetectorTransform(ConstantProvider cp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
