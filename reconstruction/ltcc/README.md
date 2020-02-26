# clasrec-ltcc

## Cloning the repository in netbeans
 * select team > Git > Clone...
 * enter the repository url: https://github.com/sly2j/clasrec-ltcc.git, click 'next'
 * select the master branch, click 'next'
 * choose a local directory for the project, click 'Finish'
 * if prompted, click "open project"
 * the project will appear in the side bar
 
## Building the plugin and installation in COATJAVA 4
 * obtain the newest version (>=4) of COATJAVA.
   * When working with COATJAVA in a terminal, ensure that the COATJAVA environment variable is set to the relevant directory.
 * click build to build the plugin
 * copy ```clasrec-ltcc-1.0-SNAPSHOT.jar``` from the target directory to ```$COATJAVA/lib/services```
 * copy the cluster bank definitions to ```etc/bankdefs/hipo/LTCC.json``` to ```$COATJAVA/etc/bankdefs/hipo```
 
## Testing the plugin
 * ensure the plugin is fully installed
 * (optional) convert evio file to hipo: 
    * ```$COATJAVA/bin/evio2hipo -o data.hipo input1.evio [input2.evio ...]```
    * see http://clasweb.jlab.org/clas12offline/docs/software/3.0/html/rec/inputfiles.html for more info
 * To run the clustering algorithm, run ```$COATJAVA/bin/recon-util -i data.hipo -o reco.hipo org.jlab.service.ltcc.LTCCEngine```
 * If you want to enable DEBUG output
    * set the ```LTCCEngine.DEBUG``` flag to ```true``` in netbeans (defined in ```org.jlab.service.ltcc.LTCCEngine.java```)
    * rebuild/re-install
    
## Diagnostic histograms using netbeans
### Generating/viewing the histograms
  * in netbeans, open the source file ```org.jlab.service.ltcc.LTCCViewer```
  * on line 100, modify ```String inputfile = "/path/to/your/reconstructed/file.hipo;"``` to point to the relevant file
  * select run > Run File
  * the output canvas will appear in a new JFrame 

### Modifying the histograms
  * The available histograms are defined in ```org.jlab.service.ltcc/LTCCHitHistos.java``` and ```org.jlab.service.ltcc.LTCCClusterHistos.java```
    * Each of the histograms are created, some options are set, and then they are added to the ```LTCCHistogrammer```
    * This add call also requires a 'getter', a function (typically a lambda) that takes an object of the type ```DataType``` and returns a ```double``` (for ```H1F```) or ```Pair<Double, Double>``` (for ```H2F```).
    * You can add/modify the histograms here
  * To control which histograms are drawn, modify the ```LTCCViewer.drawClusters()``` and ```LTCCViewer.drawHits()``` methods in ```org.jlab.service.ltcc.LTCCViewer.java```
