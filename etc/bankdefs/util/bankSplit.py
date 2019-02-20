#!/usr/bin/env python

import json
import sys
import os

# print usage
if len(sys.argv)<2:
   print "usage: bankSplit.py coatjavahipobankfolder (e.g. coatjava/etc/bankdefs/hipo/)"
   sys.exit()

# hipo schema directory
hipodirectory = sys.argv[1] +  "/"
os.chdir(hipodirectory)

# single jsons directory
workdirectory   = "singles/"
singledirectory = "full/"
os.mkdir(workdirectory)	    
os.mkdir(workdirectory + singledirectory)	    

# create schema directory
def createdirandlinks(dirname, banklist):
    os.mkdir(workdirectory + dirname)
    os.chdir(workdirectory + dirname)
    for bank in banklist:
        os.symlink("../" + singledirectory + bank + ".json", bank + ".json")
    os.chdir("../../")
    print("Json file links created in " + workdirectory + dirname)

    
# for each json file in hipo schema folder
for filename in os.listdir("./"):
    if filename.endswith(".json"):
		
        #Read JSON data into the datastore variable
        f = open(filename)
        try:
            datastore = json.load(f)
        except:
            print 'Invalid JSON:  '+filename
            sys.exit(1)
        # loop over banks in the json file
	for bank in datastore:
	    bankname = bank['bank']
	    file = open(workdirectory + singledirectory + bankname + ".json", 'w')
	    file.write(json.dumps([bank], sort_keys=True, indent=4))
	    file.close
print("Single json files saved in " + workdirectory + singledirectory)

# create dst, calibration and monitoring directories
dst = ["RUN::config","RAW::scaler","REC::Event","REC::Particle","REC::Calorimeter","REC::Cherenkov","REC::CovMat","REC::ForwardTagger","REC::Scintillator","REC::Track","REC::Traj","RICH::tdc","MC::Event","MC::Header","MC::Lund","MC::Particle","MC::True"]
calibration = ["BAND::adc","BAND::tdc","BAND::hits","CND::adc","CND::hits","CND::tdc","CTOF::adc","CTOF::hits","CTOF::tdc","CVTRec::Tracks","ECAL::calib","ECAL::clusters","ECAL::peaks","FT::particles","FTCAL::adc","FTCAL::clusters","FTCAL::hits","FTHODO::adc","FTHODO::clusters","FTHODO::hits","FTOF::adc","FTOF::hits","FTOF::tdc","HTCC::adc","HTCC::rec","LTCC::adc","LTCC::clusters","MC::Event","MC::Header","MC::Lund","MC::Particle","MC::True","RAW::scaler","REC::Calorimeter","REC::Cherenkov","REC::CovMat","REC::Event","REC::ForwardTagger","REC::Particle","REC::Scintillator","REC::Track","REC::Traj","RF::adc","RF::tdc","RICH::tdc","RUN::config","RUN::rf","RUN::trigger","TimeBasedTrkg::TBCrosses","TimeBasedTrkg::TBHits","TimeBasedTrkg::TBSegments","TimeBasedTrkg::TBSegmentTrajectory","TimeBasedTrkg::TBTracks","TimeBasedTrkg::Trajectory"]
monitoring = ["BMTRec::Crosses","BMTRec::Hits","BMTRec::LayerEffs","BSTRec::Crosses","BSTRec::Hits","BSTRec::LayerEffs","BAND::adc","BAND::tdc","BAND::hits","CND::adc","CND::clusters","CND::hits","CND::tdc","CTOF::adc","CTOF::hits","CTOF::tdc","CVTRec::Tracks","CVTRec::Trajectory","ECAL::calib","ECAL::clusters","ECAL::hits","ECAL::peaks","FT::particles","FTCAL::adc","FTCAL::clusters","FTCAL::hits","FTHODO::adc","FTHODO::clusters","FTHODO::hits","FTOF::adc","FTOF::hits","FTOF::tdc","HitBasedTrkg::HBTracks","HTCC::adc","HTCC::rec","LTCC::adc","LTCC::clusters","MC::Event","MC::Header","MC::Lund","MC::Particle","MC::True","RAW::scaler","RAW::vtp","REC::Calorimeter","REC::Cherenkov","REC::CovMat","REC::Event","REC::ForwardTagger","REC::Particle","REC::Scintillator","REC::Track","REC::Traj","RECHB::Calorimeter","RECHB::Cherenkov","RECHB::Event","RECHB::ForwardTagger","RECHB::Particle","RECHB::Scintillator","RECHB::Track","RF::adc","RF::tdc","RICH::tdc","RUN::config","RUN::rf","RUN::trigger","TimeBasedTrkg::TBCrosses","TimeBasedTrkg::TBHits","TimeBasedTrkg::TBSegments","TimeBasedTrkg::TBSegmentTrajectory","TimeBasedTrkg::TBTracks","TimeBasedTrkg::Trajectory"]
createdirandlinks("dst/", dst)
createdirandlinks("calibration/", calibration)
createdirandlinks("monitoring/",  monitoring)
    
