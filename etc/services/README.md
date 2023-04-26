
* Real Data
  * Warning, these must be set appropriately for the data in question:
    * the CCDB variation
    * the run number for MLTD/MLTAI network
  * [data-ai.yaml](data-ai.yaml)
    * AI track finding
  * [data-cv.yaml](data-cv.yaml)
    * conventional track finding
  * [data-aicv.yaml](data-aicv.yaml)
    * both conventional and AI track finding in parallel with separate output banks
* Simulation
  * Main difference from "data" is field maps, plus CCDB variation is not set by default. 
  * [mc-ai.yaml](mc-ai.yaml)
  * [mc-cv.yaml](mc-cv.yaml)
  * [mc-aicv.yaml](mc-aicv.yaml)
* Validation
  * [dcalign.yaml](dcalign.yaml)
    * for alignment validation
    * contains examples of setting alignments on-the-fly, instead of CCDB
  * [denoise.yaml](denoise.yaml)
    * for denoising validation, leverages ADC/TDC "order" marking
    * includes both denoising and non-denoising in parallel with separate output banks  
* Miscellaneous
  * [kpp.yaml](kpp.yaml)
    * valid only for old KPP data
    * still used for CI tests
  * [eb.yaml](eb.yaml)
    * event builder only
  * [swaps.yaml](swaps.yaml)
    * example of how to run the swap service

