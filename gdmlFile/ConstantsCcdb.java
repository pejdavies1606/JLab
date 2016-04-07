package volume_geometry;

import org.jlab.clasrec.utils.DatabaseConstantProvider;

public class ConstantsCcdb
{
	// GEOMETRY PARAMETERS
	//
	//----------------
	// length unit: mm
	//----------------
	//
	// fundamentals
	public static int NREG;
	public static int[] NSECT;
	public static final int NSLAYR = 2;
	public static int NLAYR;
	public static int NSTRIP;
	//
	// position and orientation
	public static double[][] MODULERADIUS;
	public static double[] Z0;
	public static double PHI0;
	//
	// size and dimensions
	public static double SILICONTHICK;
	public static double LAYRGAP;
	
	public static double ACTIVESENWIDTH;
	
	public static double ACTIVESENLEN;
	public static double TOTSENLEN;
	public static double DEADZNLEN;
	
	public static double MICROGAP = 0.112;
	public static double MODULELENGTH;
		
	public static boolean areConstantsLoaded = false;
	//public static boolean newGeometry = true;
	public static final double MODULEPOSFAC = 0.5; // % wrt top of  module
	
	
	
	public static synchronized void Load()
	{
		if (areConstantsLoaded) return;
		
		DatabaseConstantProvider cp = new DatabaseConstantProvider( 10, "default");
		
		cp.loadTable("/geometry/bst/bst");
		NREG = cp.getInteger("/geometry/bst/bst/nregions", 0 );
		SILICONTHICK = cp.getDouble("/geometry/bst/bst/siliconWidth", 0 ); // why is it called a width in CCDB?
		
		cp.loadTable("/geometry/bst/sector");
		NSTRIP = cp.getInteger("/geometry/bst/sector/nstrips", 0 );
		TOTSENLEN = cp.getDouble("/geometry/bst/sector/physSenLen", 0 );
		ACTIVESENWIDTH = cp.getDouble("/geometry/bst/sector/activeSenWid", 0 );
		ACTIVESENLEN = cp.getDouble("/geometry/bst/sector/activeSenLen", 0 );
		
		DEADZNLEN = TOTSENLEN - ACTIVESENLEN;
		//DEADZNLEN = cp.getDouble("/geometry/bst/sector/deadZnSenLen1", 0 );
		
		MODULELENGTH = 3*ACTIVESENLEN + 2*DEADZNLEN + 2*MICROGAP; // active area for 3 sensors including inbetween dead zones
		
		cp.loadTable("/geometry/bst/region");
		NSECT = new int[NREG];
		LAYRGAP = cp.getDouble("/geometry/bst/region/layergap", 0 );
		MODULERADIUS = new double[NREG][]; // the radius of a BST module w.r.t. the beam axis
		Z0 = new double[NREG]; // the z-position of a BST module in the lab-frame
		
	    // the values of the z0 position of the BST module local coordinate system
	    // in the lab frame coordinate system (from gemc geometry file), for each of the regions
			
		for( int r = 0; r < NREG; r++ )
		{
			NSECT[r] = cp.getInteger("/geometry/bst/region/nsectors", r );
			
			double zStart = cp.getDouble("/geometry/bst/region/zstart", r );
			Z0[r] = zStart + 0.5*DEADZNLEN;
			
			double radius = cp.getDouble("/geometry/bst/region/radius", r);
			MODULERADIUS[r] = new double[NSLAYR];
			for( int l = 0; l < NSLAYR; l++ )
			{
				MODULERADIUS[r][0] = radius - MODULEPOSFAC*SILICONTHICK;
				MODULERADIUS[r][1] = radius + LAYRGAP + MODULEPOSFAC*SILICONTHICK;
			}
		}
		
		cp.disconnect();
		areConstantsLoaded = true;
		System.out.println(" geometry constants loaded ? "+areConstantsLoaded);
		
		System.out.println();
		System.out.println("NREG="+ NREG );
		System.out.println("NSTRIP="+ NSTRIP );
		System.out.println("LAYRGAP="+ LAYRGAP );
		System.out.println("SILICONTHICK="+ SILICONTHICK );
		System.out.println("TOTSENLEN="+ TOTSENLEN );
		System.out.println("ACTIVESENWIDTH="+ ACTIVESENWIDTH );
		System.out.println("ACTIVESENLEN="+ ACTIVESENLEN );
		System.out.printf("DEADZNLEN=%8.3f\n", DEADZNLEN );
		System.out.printf("MODULELENGTH=%8.3f\n", MODULELENGTH );
		for( int r = 0; r < NREG; r++ )
			System.out.println("NSECT["+r+"]="+NSECT[r] );
		for( int r = 0; r < NREG; r++ )
			System.out.println("Z0["+r+"]="+Z0[r] );
		
	}
}
