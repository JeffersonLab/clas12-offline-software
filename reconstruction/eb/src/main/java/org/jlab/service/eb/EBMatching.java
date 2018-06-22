package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.physics.Vector3;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.eb.EBCCDBConstants;
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

        List<DetectorParticle> parts=new ArrayList<DetectorParticle>();

        List<DetectorResponse> responsesECAL =
            eventBuilder.getUnmatchedResponses(null, DetectorType.ECAL, ecalLayer);

        Vector3 vertex = new Vector3(0,0,0);
        if (eventBuilder.getEvent().getParticles().size()>0) {
            vertex.copy(eventBuilder.getEvent().getParticle(0).vertex());
        }

        for (DetectorResponse r : responsesECAL)
            parts.add(DetectorParticle.createNeutral(r,vertex));
        
        // add other responses:
        this.addResponsesECAL(parts,otherEcalLayers);
        this.addResponsesFTOF(parts);

        return parts;
    }

    /**
     * find CD neutrals and append to particle list
     */
    public boolean addCentralNeutrals(DetectorEvent de) {
        
        List<DetectorResponse> respCND =
            eventBuilder.getUnmatchedResponses(null, DetectorType.CND, 0);

        if (respCND.size()>0) {

            Vector3 vertex = new Vector3(0,0,0);
            if (de.getParticles().size()>0) {
                vertex.copy(eventBuilder.getEvent().getParticle(0).vertex());
            }

            for (DetectorResponse r : respCND) {

                DetectorParticle neutral = DetectorParticle.createNeutral(r,vertex);

                List<DetectorResponse> respCTOF =
                    eventBuilder.getUnmatchedResponses(null, DetectorType.CTOF, 0);

                final int indx=neutral.getDetectorHit(respCTOF,DetectorType.CTOF,0,
                        eventBuilder.ccdb.getDouble(EBCCDBEnum.CTOF_DZ));

                if (indx >= 0) {
                    neutral.addResponse(respCTOF.get(indx),true);
                    respCTOF.get(indx).setAssociation(de.getParticles().size()-1);
                }
                de.addParticle(neutral);
            }
        }

        return respCND.size()>0;
    }

    /**
     *
     * Central Tracks and CTOF/CND were already matched before
     * event builder.  Copy in.
     *
     * @deprecated switched to standard EB matching
     *
     */
    
    public void processCentralParticles(DataEvent de,
                                     String ctrkBankName,
                                     String ctofBankName,
                                     String cndBankName,
                                     List<DetectorParticle> cvtParticles,
                                     List<DetectorResponse> ctofHits,
                                     List<DetectorResponse> cndHits) {

        Vector3 vertex = new Vector3(0,0,0);
        if (eventBuilder.getEvent().getParticles().size()>0) {
            vertex.copy(eventBuilder.getEvent().getParticle(0).vertex());
        }

        // Make a neutral particle for each CND hit without an
        // associated track.
        if (de.hasBank(cndBankName)==true) {
            DataBank cndBank = de.getBank(cndBankName);
            final int ncnd=cndBank.rows();
            int cnd_count = 0;
            for (int icnd=0; icnd<ncnd; icnd++) {
                final int trkid=cndBank.getInt("trkID",icnd);
                if (trkid==-1) {
                    // make neutral particle
                    DetectorParticle p = DetectorParticle.createNeutral(cndHits.get(icnd),vertex);
                    this.eventBuilder.getEvent().addParticle(p);
                    cnd_count = cnd_count + 1;
                }
            }
            this.eventBuilder.getPindexMap().put(3,cnd_count);
        }

        // Add CTOF responses to charged CVT particles:
        if (de.hasBank(ctrkBankName)==true) {
            
            // Load map from CVT trkID to CTOF hits:
            DataBank ctofBank = null;
            Map<Integer,ArrayList<Integer>> ctofMap = null;
            if (de.hasBank(ctofBankName)==true) {
                ctofMap = new HashMap<Integer,ArrayList<Integer>>();
                ctofBank = de.getBank(ctofBankName);
                final int nctof=ctofBank.rows();
                for (int ictof=0; ictof<nctof; ictof++) {
                    final int trkid=ctofBank.getInt("trkID",ictof);
                    if (trkid>=0) {
                        if (!ctofMap.containsKey(trkid))
                            ctofMap.put(trkid,new ArrayList<Integer>());
                        // insert it according to energy:
                        boolean found=false;
                        final float ee = ctofBank.getFloat("energy",ictof);
                        for (int ilist=0; ilist<ctofMap.get(trkid).size(); ilist++) {
                            final float ee2 = ctofBank.getFloat("energy",ctofMap.get(trkid).get(ilist));
                            if (ee>ee2) {
                                ctofMap.get(trkid).add(ilist,ictof);
                                found=true;
                                break;
                            }
                        }
                        // no lower energies, put it at the end:
                        if (found==false) ctofMap.get(trkid).add(ictof);
                    }
                }
            }

            // Associate CTOF hit with particles:
            final int pindex_offset = eventBuilder.getPindexMap().get(0); //After the FD charged particles
            DataBank ctrkBank = de.getBank(ctrkBankName);
            final int nctrk=ctrkBank.rows();
            for (int ictrk=0; ictrk<nctrk; ictrk++) {
                DetectorParticle cvtParticle = cvtParticles.get(ictrk);
                final int trkid=ctrkBank.getInt("ID",ictrk);
                if (ctofMap!=null && ctofMap.containsKey(trkid)) {
                    for(int i = 0 ; i < ctofMap.get(trkid).size() ; i++) {
                        final int ctofIndex = ctofMap.get(trkid).get(i);
                        cvtParticle.addResponse(ctofHits.get(ctofIndex), true);
                        ctofHits.get(ctofIndex).setPath(ctofBank.getFloat("pathLength",ctofIndex));
                        ctofHits.get(ctofIndex).setAssociation(ictrk + pindex_offset);
                    }
                }
            }
        }

    }

}
