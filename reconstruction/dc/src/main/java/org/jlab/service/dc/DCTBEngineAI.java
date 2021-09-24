/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.dc;

/**
 *
 * @author ziegler
 */
public class DCTBEngineAI extends DCTBEngine {
    
    public DCTBEngineAI() {
        super("DCTBAI");
    }
    
    @Override
    public void initBankNames() {
        this.getBankNames().setHitsInputBank("HitBasedTrkg::AIHits");
        this.getBankNames().setClustersInputBank("HitBasedTrkg::AIClusters");
        this.getBankNames().setTracksInputBank("HitBasedTrkg::AITracks");
        this.getBankNames().setIdsBank("HitBasedTrkg::AIHitTrkId");
        this.getBankNames().setHitsBank("TimeBasedTrkg::AIHits");
        this.getBankNames().setClustersBank("TimeBasedTrkg::AIClusters");
        this.getBankNames().setSegmentsBank("TimeBasedTrkg::AISegments");
        this.getBankNames().setCrossesBank("TimeBasedTrkg::AICrosses");
        this.getBankNames().setTracksBank("TimeBasedTrkg::AITracks");
        this.getBankNames().setTrajBank("TimeBasedTrkg::AITrajectory");
        this.getBankNames().setCovmatBank("TimeBasedTrkg::AICovMat");
        this.getBankNames().setRecEventBank("RECHBAI::Event");
        this.getBankNames().setRecPartBank("RECHBAI::Particle");
        this.getBankNames().setRecTrackBank("RECHBAI::Track");
        
                
        super.registerOutputBank("TimeBasedTrkg::AIHits");
        super.registerOutputBank("TimeBasedTrkg::AIClusters");
        super.registerOutputBank("TimeBasedTrkg::AISegments");
        super.registerOutputBank("TimeBasedTrkg::AICrosses");
        super.registerOutputBank("TimeBasedTrkg::AITracks");
        super.registerOutputBank("TimeBasedTrkg::AICovMat");
        super.registerOutputBank("TimeBasedTrkg::AITrajectory");
    }
}
