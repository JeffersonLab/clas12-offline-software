This package is completely stand-alone. It creates Magentic Field objects 
from binary files.

To use it, you need to know the full path to the binary file.

If you are using clasJlib, the binary files are in the data folder
that begins at the top level.

For example, you have clasJlib at the top level in your home folder,
you could use:

String fname = "clasJlib/data/torus/v1.0/clas12_torus_fieldmap_binary.dat;
File file = new File(fname);
Torus torus = Torus.fromBinaryFile(file);

You then should be able to use the interface methods to access the torus field.

See the javadoc to see all available methods. Here are some:

//give x,y,z in cm, get the field in kG. The x,y, and z
//components are in the 0,1 and 2 indices of result
field(float x, float y, float z, float[] result)

//give phi in degress, and rho and z in cm, get the field in kG. The x,y, and z
//components are in the 0,1 and 2 indices of result. NOTE: Even though
//the input is cylindrical, the output is Cartesian.
fieldCylindrical(double phi, double rho, double z, float result[])

Similarly for the solenoid.

If you create a Torus and a Solenoid, is is up to you to combine them
as appropriate.

There is a source Test.java in the code distribution of the magfield that
can be used as an example.
