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
public class DCTBEngineConv extends DCTBEngine {
    public DCTBEngineConv() {
        super("DCTBCONV");
    }
    
    @Override
    public void initBankNames() {
        this.getBankNames().setHitsInputBank("HitBasedTrkg::HBHits");
        this.getBankNames().setClustersInputBank("HitBasedTrkg::HBClusters");
        this.getBankNames().setTracksInputBank("HitBasedTrkg::HBTracks");
        this.getBankNames().setIdsBank("HitBasedTrkg::HBHitTrkId");
        this.getBankNames().setHitsBank("TimeBasedTrkg::TBHits");
        this.getBankNames().setClustersBank("TimeBasedTrkg::TBClusters");
        this.getBankNames().setSegmentsBank("TimeBasedTrkg::TBSegments");
        this.getBankNames().setCrossesBank("TimeBasedTrkg::TBCrosses");
        this.getBankNames().setTracksBank("TimeBasedTrkg::TBTracks");
        this.getBankNames().setTrajBank("TimeBasedTrkg::Trajectory");
        this.getBankNames().setCovmatBank("TimeBasedTrkg::TBCovMat");
        this.getBankNames().setRecEventBank("RECHB::Event");
        this.getBankNames().setRecPartBank("RECHB::Particle");
        this.getBankNames().setRecTrackBank("RECHB::Track");
    }
}
