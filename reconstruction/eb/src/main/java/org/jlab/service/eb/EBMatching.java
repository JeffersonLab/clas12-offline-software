package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.rec.eb.EBCCDBEnum;
import org.jlab.clas.detector.DetectorResponseComparators;
import org.jlab.clas.detector.DetectorResponseFactory;
import org.jlab.clas.detector.matching.MatchCND;

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

                List<DetectorResponse> responses = eventBuilder.getSectorResponses(null, part.getTrack().getSector(), DetectorType.ECAL, layer);
                int index = part.getDetectorHit(responses, DetectorType.ECAL, layer, matching);
                if (index>=0) {
                    if (responses.get(index).getAssociation() >= 0) {
                        DetectorResponse copy = DetectorResponseFactory.create(responses.get(index));
                        copy.clearAssociations();
                        responses.add(copy);
                        index = responses.size()-1;
                    }
                    int pindex_offset = this.eventBuilder.getPindexMap().get(0)
                                      + this.eventBuilder.getPindexMap().get(1); //After FD/CD Charged Particles
                    part.addResponse(responses.get(index), true); 
                    responses.get(index).addAssociation(ii + pindex_offset);
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
                otherEcalLayers=new int[]{7};
                break;
            case (7):
                otherEcalLayers=new int[]{};
                break;
            default:
                throw new RuntimeException("Invalid ECAL Layer:  "+ecalLayer);
        }

        List<DetectorParticle> parts=new ArrayList<>();

        List<DetectorResponse> responsesECAL =
            eventBuilder.getUnmatchedResponses(null, DetectorType.ECAL, ecalLayer);

        Vector3 vertex = new Vector3(0,0,0);
        if (!eventBuilder.getEvent().getParticles().isEmpty()) {
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
	 * @param de
	 * @return whether any neutrals were added 
     */
    public boolean addCentralNeutrals(DetectorEvent de) {

        int newNeutrals = 0;
        
        Vector3 vertex = new Vector3(0,0,0);
        if (!de.getParticles().isEmpty()) {
            vertex.copy(eventBuilder.getEvent().getParticle(0).vertex());
        }

        //
        // This is the new case where CND only does intralayer clustering,
        // and EB does CND's interlayer matching:
        //
        MatchCND matcher = new MatchCND(
                eventBuilder.ccdb.getDouble(EBCCDBEnum.CND_DZ),
                eventBuilder.ccdb.getDouble(EBCCDBEnum.CND_DPHI),
                eventBuilder.ccdb.getDouble(EBCCDBEnum.CND_DT));

        List<DetectorResponse> cnds =
                eventBuilder.getUnmatchedResponses(null, DetectorType.CND, 0);

        List<DetectorResponse> ctofs =
                eventBuilder.getUnmatchedResponses(null, DetectorType.CTOF, 0);

        // Layer-based ordering, with energy-ordering within layers:
        cnds.sort(DetectorResponseComparators.layerEnergy);

        while (!cnds.isEmpty()) {
            // CND clusters are already sorted such that the first one will
            // always seed a new neutral:
            DetectorResponse seed = cnds.remove(0);
            final int pindex = de.getParticles().size();
            DetectorParticle neutral = DetectorParticle.createNeutral(seed,vertex);
            seed.setAssociation(pindex);
            newNeutrals++;

            // Associate any remaining CND clusters to this neutral:
            for (int i=0; i<cnds.size(); i++) {
                if (matcher.matches(neutral, cnds.get(i), true)) {
                    DetectorResponse hit = cnds.remove(i);
                    neutral.addResponse(hit, true);
                    hit.setAssociation(pindex);
                    i--;
                }
            }

            // Associate any remaining CTOF clusters to this neutral:
            final int indx=neutral.getDetectorHit(ctofs,DetectorType.CTOF,0,
                    eventBuilder.ccdb.getDouble(EBCCDBEnum.CTOF_DZ));
            if (indx >= 0) {
                neutral.addResponse(ctofs.get(indx),true);
                ctofs.get(indx).setAssociation(pindex);
                // FIXME:  stop mixing Vector3 and Vector3D
                final double dx = ctofs.get(indx).getPosition().x()-vertex.x();
                final double dy = ctofs.get(indx).getPosition().y()-vertex.y();
                final double dz = ctofs.get(indx).getPosition().z()-vertex.z();
                ctofs.get(indx).setPath(Math.sqrt(dx*dx+dy*dy+dz*dz));
                ctofs.remove(indx);
            }
	    
            de.addParticle(neutral);
        }
        
        // make a new neutral particle for each unmatched CTOF cluster:
        for (DetectorResponse respCTOF : ctofs) {
            DetectorParticle neutral = DetectorParticle.createNeutral(respCTOF,vertex);
            respCTOF.setAssociation(de.getParticles().size());
            de.addParticle(neutral);
            newNeutrals++;
        }

        return newNeutrals>0;
    }

}
