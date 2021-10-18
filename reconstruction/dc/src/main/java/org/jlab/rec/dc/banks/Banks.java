package org.jlab.rec.dc.banks;

/**
 *
 * @author devita
 */
public class Banks {
    
    private final String tdcBank  = "DC::tdc";
    private final String docaBank = "DC::doca";
    
    private String inHitsBank      = null;
    private String inClustersBank  = null;
    private String inTracksBank    = null;
    private String inIdsBank       = null;
    
    private String hitsBank        = null;
    private String clustersBank    = null;
    private String segmentsBank    = null;
    private String crossesBank     = null;
    private String tracksBank      = null;
    private String idsBank         = null;
    private String trajBank        = null;
    private String covmatBank      = null;
    
    private final String aiBank    = "ai::tracks";
    
    private String recEventBank    = null;
    private String recPartBank     = null;
    private String recTrackBank    = null;


    public String getTdcBank() {
        return tdcBank;
    }

    public String getDocaBank() {
        return docaBank;
    }

    public String getAiBank() {
        return aiBank;
    }
    private String setInputBank(BankType type, String item) {
        String bank = type.getInputPrefix() + "::" + type.getInputSuffix() + item;
        return bank;
    }
    
    private String setOutputBank(BankType type, String item) {
        String bank = type.getOutputPrefix() + "::" + type.getOutputSuffix() + item;
        if(type == BankType.TB && item.equals("Trajectory"))
            bank = type.getOutputPrefix() + "::" + item;
        return bank;
    }
    
    private String setRecBank(BankType type, String item) {
        String bank = type.getRec()+ "::" + item;
        return bank;
    }
    
    public void setInputBanks(BankType type) {
        this.inHitsBank     = this.setInputBank(type, "Hits");
        this.inClustersBank = this.setInputBank(type, "Clusters");
        this.inTracksBank   = this.setInputBank(type, "Tracks");
        this.inIdsBank      = this.setInputBank(type, "HitTrkId");        
    }
    
    public void setOutputBanks(BankType type) {
        this.hitsBank     = this.setOutputBank(type, "Hits");
        this.clustersBank = this.setOutputBank(type, "Clusters");
        this.segmentsBank = this.setOutputBank(type, "Segments");
        this.crossesBank  = this.setOutputBank(type, "Crosses");
        this.tracksBank   = this.setOutputBank(type, "Tracks");
        this.idsBank      = this.setOutputBank(type, "HitTrkId");
        this.trajBank     = this.setOutputBank(type, "Trajectory");
        this.covmatBank   = this.setOutputBank(type, "CovMat");
    }
    
    public void setRecBanks(BankType type) {
        this.recEventBank = this.setRecBank(type, "Event");
        this.recPartBank  = this.setRecBank(type, "Particle");
        this.recTrackBank = this.setRecBank(type, "Track");
    }
    
    public String getHitsInputBank() {
        return inHitsBank;
    }

    public String getClustersInputBank() {
        return inClustersBank;
    }

    public String getTracksInputBank() {
        return inTracksBank;
    }
    
    public String getIdsInputBank() {
        return inIdsBank;
    }
    
    public String getHitsBank() {
        return hitsBank;
    }

    public String getClustersBank() {
        return clustersBank;
    }

    public String getSegmentsBank() {
        return segmentsBank;
    }

    public String getCrossesBank() {
        return crossesBank;
    }

    public String getTracksBank() {
        return tracksBank;
    }

    public String getIdsBank() {
        return idsBank;
    }

    public String getTrajBank() {
        return trajBank;
    }

    public String getCovmatBank() {
        return covmatBank;
    }
    
    public String getRecEventBank() {
        return recEventBank;
    }

    public String getRecPartBank() {
        return recPartBank;
    }

    public String getRecTrackBank() {
        return recTrackBank;
    }

    
    public enum BankType {
        
        UDF  ("UDF",  "",             "",   "",              "",   ""       ),
        CR   ("CR",   "",             "",   "HitBasedTrkg",  "",   ""       ),
        HB   ("HB",   "HitBasedTrkg", "",   "HitBasedTrkg",  "HB", ""       ),
        HBAI ("HBAI", "HitBasedTrkg", "",   "HitBasedTrkg",  "AI", ""       ),
        TB   ("TB",   "HitBasedTrkg", "HB", "TimeBasedTrkg", "TB", "RECHB"  ),
        TBAI ("TBAI", "HitBasedTrkg", "AI", "TimeBasedTrkg", "AI", "RECHBAI");
        
        private String name;
        private String inputBankPrefix;
        private String inputBankSuffix;
        private String outputBankPrefix;
        private String outputBankSuffix;
        private String recBankPrefix;
                
        BankType(){
            name = "HB";
            inputBankPrefix  = "HitBasedTrkg";
            inputBankSuffix  = "";
            outputBankPrefix = "HitBasedTrkg";
            outputBankSuffix = "HB";
            recBankPrefix    = "";
        }

        BankType(String typeName, 
                 String inputPrefix,  String inputSuffix,
                 String outputPrefix, String outputSuffix,
                 String recPrefix){
            name = typeName;
            inputBankPrefix  = inputPrefix;
            inputBankSuffix  = inputSuffix;
            outputBankPrefix = outputPrefix;
            outputBankSuffix = outputSuffix;
            recBankPrefix    = recPrefix;
        }
        
        public String getName() {
            return name;
        }
        
        public String getInputPrefix() {
            return inputBankPrefix;
        }
        
        public String getInputSuffix() {
            return inputBankSuffix;
        }
        
        public String getOutputPrefix() {
            return outputBankPrefix;
        }
        
        public String getOutputSuffix() {
            return outputBankSuffix;
        }
        
        public String getRec() {
            return recBankPrefix;
        }
        
        public static BankType getType(String name) {
            name = name.trim();
            for(BankType t: BankType.values())
                if (t.getName().equalsIgnoreCase(name)) 
                    return t;
            return UDF;
        }
    }
}
