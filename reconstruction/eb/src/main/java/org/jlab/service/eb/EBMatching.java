package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.rec.eb.EBCCDBEnum;

/*
 *
 * Matching Utilities, for Forward Neutrals and Central
 *
 * @author baltzell
 * @author jnewton
 */
public class EBMatching {

    private EventBuilder eventBuilder=null;

    public EBMatching(EventBuilder eb) {
        this.eventBuilder=eb;
    }

    /*
     * addResponsesFTOF
     *
     * Find and add all unmatched, matching FTOF responses to each particle.
     * NOTE:  This will be simplified once FTOF reports clusters.
     *
     */
    public void addResponsesFTOF(List <DetectorParticle> parts) {

        List<DetectorResponse> respFTOF1A = eventBuilder.getUnmatchedResponses(null, DetectorType.FTOF, 1);
        List<DetectorResponse> respFTOF1B = eventBuilder.getUnmatchedResponses(null, DetectorType.FTOF, 2);
        List<DetectorResponse>  respFTOF2 = eventBuilder.getUnmatchedResponses(null, DetectorType.FTOF, 3);

        for (int ii=0; ii<parts.size(); ii++) {
            
            DetectorParticle part=parts.get(ii);
            
            int pindex_offset = this.eventBuilder.getPindexMap().get(0)
                                + this.eventBuilder.getPindexMap().get(1);

            int index1A = part.getDetectorHit(respFTOF1A, DetectorType.FTOF, 1, 
                    eventBuilder.ccdb.getDouble(EBCCDBEnum.FTOF_MATCHING_1A));
            if (index1A >= 0) {
                part.addResponse(respFTOF1A.get(index1A), true);
                respFTOF1A.get(index1A).setAssociation(ii + pindex_offset);
            }
            int index1B = part.getDetectorHit(respFTOF1B, DetectorType.FTOF, 2,
                    eventBuilder.ccdb.getDouble(EBCCDBEnum.FTOF_MATCHING_1B));
            if (index1B >= 0) {
                part.addResponse(respFTOF1B.get(index1B), true);
                respFTOF1B.get(index1B).setAssociation(ii + pindex_offset);
            }
            // only try to match with FTOF2 if not matched with 1A/1B:
            if (index1A<0 && index1B<0) {
                int index2 = part.getDetectorHit(respFTOF2, DetectorType.FTOF, 3,
                        eventBuilder.ccdb.getDouble(EBCCDBEnum.FTOF_MATCHING_2));
                if (index2>=0) {
                    part.addResponse(respFTOF2.get(index2), true);
                    respFTOF2.get(index2).setAssociation(ii + pindex_offset);
                }
            }
        }
    }

    /*
     * addResponsesECAL
     *
     * Find and add unmatched, matching ECAL responses to each particle,
     * 
     * But only for the given ECAL layers.
     *
     */
    public void addResponsesECAL(List <DetectorParticle> parts, int[] layers) {

        for (int ii=0; ii<parts.size(); ii++) {

            DetectorParticle part=parts.get(ii);
            
            for (int layer : layers) {

                double matching;
                switch (layer) {
                    case (1):
                        matching=eventBuilder.ccdb.getDouble(EBCCDBEnum.PCAL_MATCHING);
                        break;
                    case (4):
                        matching=eventBuilder.ccdb.getDouble(EBCCDBEnum.ECIN_MATCHING);
                        break;
                    case (7):
                        matching=eventBuilder.ccdb.getDouble(EBCCDBEnum.ECOUT_MATCHING);
                        break;
                    default:
                        throw new RuntimeException("Invalid ECAL Layer:  "+layer);
                }

                List<DetectorResponse> respECAL = eventBuilder.getUnmatchedResponses(null, DetectorType.ECAL, layer);

                int index = part.getDetectorHit(respECAL, DetectorType.ECAL, layer, matching);
                if (index>=0) {
                    int pindex_offset = this.eventBuilder.getPindexMap().get(0)
                                      + this.eventBuilder.getPindexMap().get(1); //After FD/CD Charged Particles
                    part.addResponse(respECAL.get(index), true); 
                    respECAL.get(index).setAssociation(ii + pindex_offset);
                }
            }
        }
    }

    /*
     * findNeutrals
     *
     * Create neutral particles from unmatched responses from the given ECAL layer.
     *
     * Add unmatched, matching responses from FTOF and other ECAL layers. 
     *
     */
    public List<DetectorParticle> findNeutrals(int ecalLayer) {

        int[] otherEcalLayers;
        switch (ecalLayer) {
            case (1):
                otherEcalLayers=new int[]{4,7};
                break;
            case (4):
                otherEcalLayers=new int[]{1,7};
                break;
            case (7):
                otherEcalLayers=new int[]{1,4};
                break;
            default:
                throw new RuntimeException("Invalid ECAL Layer:  "+ecalLayer);
        }

        List<DetectorParticle> parts=new ArrayList<>();

        List<DetectorResponse> responsesECAL =
            eventBuilder.getUnmatchedResponses(null, DetectorType.ECAL, ecalLayer);

        Vector3 vertex = new Vector3(0,0,0);
        if (eventBuilder.getEvent().getParticles().size()>0) {
            vertex.copy(eventBuilder.getEvent().getParticle(0).vertex());
        }

        for (DetectorResponse r : responsesECAL) {
            int pindex_offset = this.eventBuilder.getPindexMap().get(0)
                              + this.eventBuilder.getPindexMap().get(1); //After FD/CD Charged Particles
            r.setAssociation(parts.size()+pindex_offset);
            parts.add(DetectorParticle.createNeutral(r,vertex));
        }
        
        // add other responses:
        this.addResponsesECAL(parts,otherEcalLayers);
        this.addResponsesFTOF(parts);

        return parts;
    }

    /**
     * find CD neutrals and append to particle list
     */
    public boolean addCentralNeutrals(DetectorEvent de) {

        int newneuts=0;
        
        Vector3 vertex = new Vector3(0,0,0);
        if (de.getParticles().size()>0) {
            vertex.copy(eventBuilder.getEvent().getParticle(0).vertex());
        }

        List<DetectorResponse> respsCND =
            eventBuilder.getUnmatchedResponses(null, DetectorType.CND, 0);

        // make a new neutral particle for each unmatched CND cluster:
        for (DetectorResponse respCND : respsCND) {

            // haven't appended the particle yet, but this will be its index:
            final int pindex = de.getParticles().size();

            // make neutral particle from CND:
            DetectorParticle neutral = DetectorParticle.createNeutral(respCND,vertex);
            respCND.setAssociation(pindex);

            // find and associate matching CTOF hits:
            List<DetectorResponse> respCTOF =
                    eventBuilder.getUnmatchedResponses(null, DetectorType.CTOF, 0);
            final int indx=neutral.getDetectorHit(respCTOF,DetectorType.CTOF,0,
                    eventBuilder.ccdb.getDouble(EBCCDBEnum.CTOF_DZ));
            if (indx >= 0) {
                neutral.addResponse(respCTOF.get(indx),true);
                respCTOF.get(indx).setAssociation(pindex);
                // FIXME:  stop mixing Vector3 and Vector3D
                final double dx = respCTOF.get(indx).getPosition().x()-vertex.x();
                final double dy = respCTOF.get(indx).getPosition().y()-vertex.y();
                final double dz = respCTOF.get(indx).getPosition().z()-vertex.z();
                respCTOF.get(indx).setPath(Math.sqrt(dx*dx+dy*dy+dz*dz));
            }

            de.addParticle(neutral);
            newneuts++;
        }

        // make a new neutral particle for each unmatched CTOF cluster:
        List<DetectorResponse> respsCTOF =
            eventBuilder.getUnmatchedResponses(null, DetectorType.CTOF, 0);
        for (DetectorResponse respCTOF : respsCTOF) {
            // make neutral particle from CTOF:
            DetectorParticle neutral = DetectorParticle.createNeutral(respCTOF,vertex);
            respCTOF.setAssociation(de.getParticles().size());
            de.addParticle(neutral);
            newneuts++;
        }

        return newneuts>0;
    }

}
