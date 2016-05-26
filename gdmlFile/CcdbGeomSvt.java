package volume_geometry;

import org.jlab.clasrec.utils.DatabaseConstantProvider;

public class CcdbGeomSvt
{
	// SVT GEOMETRY PARAMETERS
	//
	//------------------
	// length unit: mm
	//  angle unit: deg
	//------------------
	//
	// fundamentals
	public static int NREG; // number of regions
	public static int NLAYR; // number of layers in a superlayer / module
	public static int[] STATUS;
	public static int[] NSECT; // number of sectors in a given region
	public static int NTOTSECTLAYR; // total number of layers in a sector
	public static int NSEN; // number of sensors in a layer
	public static int NSTRIP; // number of strips in a layer (spread over NSEN sensors)
	public static double PITCH; // distance between start of strips along front of hybrid sensor
	public static double STEREOANGLE; // total angle swept by sensor strips
	//
	// position and orientation of layers
	public static double[][] MODULERADIUS;
	public static double MODULEPOSFAC ; // % wrt top of module
	public static double[] Z0;
	public static double PHI0;
	public static double LOCZAXISROTATION;
	//
	// size and dimensions of each layer
	public static double SILICONTHICK;
	public static double LAYRGAP;
	//
	public static double TOTSENLEN;
	public static double ACTIVESENLEN;
	public static double DEADZNLEN;
	public static double MICROGAP;
	//
	public static double ACTIVESENWID;
	public static double TOTSENWID;
	public static double DEADZNWID;
	//
	// calculated on Load()
	public static double MODULELEN; // total active sensor length including gaps inbetween?
	//
	// flags and CCDB address
	public static boolean bConstantsLoaded = false;
	public static final String GEOMPATH = "/geometry/cvt/svt/"; // new tables
	
	
	
	public static synchronized void load()
	{
		if( bConstantsLoaded ) return;
		
		DatabaseConstantProvider cp = new DatabaseConstantProvider( 10, "default");
		
		cp.loadTable(GEOMPATH+"svt");
		NREG = cp.getInteger(GEOMPATH+"svt/nRegions", 0 );
		NLAYR = cp.getInteger(GEOMPATH+"svt/nLayers", 0 );
		NSEN = cp.getInteger(GEOMPATH+"svt/nSensors", 0 );
		NSTRIP = cp.getInteger(GEOMPATH+"svt/nStrips", 0 );
		LAYRGAP = cp.getDouble(GEOMPATH+"svt/layerGap", 0 );
		PITCH = cp.getDouble(GEOMPATH+"svt/readoutPitch", 0 );
		SILICONTHICK = cp.getDouble(GEOMPATH+"svt/siliconThick", 0 );
		TOTSENLEN = cp.getDouble(GEOMPATH+"svt/physSenLen", 0 );
		TOTSENWID = cp.getDouble(GEOMPATH+"svt/physSenWid", 0 );
		ACTIVESENLEN = cp.getDouble(GEOMPATH+"svt/activeSenLen", 0 );
		ACTIVESENWID = cp.getDouble(GEOMPATH+"svt/activeSenWid", 0 );
		DEADZNLEN = cp.getDouble(GEOMPATH+"svt/deadZnLen", 0 );
		DEADZNWID = cp.getDouble(GEOMPATH+"svt/deadZnWid", 0 ); // currently unknown, thanks to an obfuscated diagram
		MICROGAP = cp.getDouble(GEOMPATH+"svt/microGap", 0 );
		STEREOANGLE = cp.getDouble(GEOMPATH+"svt/stereoAngle", 0 );
		PHI0 = cp.getDouble(GEOMPATH+"svt/phiStart", 0 );
		LOCZAXISROTATION = cp.getDouble(GEOMPATH+"svt/locZAxisRotation", 0 );
		MODULEPOSFAC = cp.getDouble(GEOMPATH+"svt/modulePosFac", 0 );
		//
		// TOTSENLEN = || DZ |  AL  | DZ ||
		// MODULELEN = || DZ |  AL  | DZ |MG| DZ |  AL  | DZ |MG| DZ |  AL  | DZ ||
		// 
		// previous definition: DEADZNLEN = TOTSENLEN - ACTIVESENLEN == physSenLen - activeSenLen == 2*deadZnLen 
		// current definition: DEADZNLEN = deadZnLen
		//
		MODULELEN = NSEN*(ACTIVESENLEN + 2*DEADZNLEN) + (NSEN - 1)*MICROGAP;
		
		cp.loadTable(GEOMPATH+"region");
		NSECT = new int[NREG];
		STATUS = new int[NREG];
		MODULERADIUS = new double[NREG][]; // the radius of an svt module w.r.t. the beam axis
		Z0 = new double[NREG]; // the z-position of an svt module in the lab-frame
			
		for( int r = 0; r < NREG; r++ )
		{
			NSECT[r] = cp.getInteger(GEOMPATH+"region/nSectors", r );
			STATUS[r] = cp.getInteger(GEOMPATH+"region/status", r );
			
			double zStart = cp.getDouble(GEOMPATH+"region/zStart", r );
			// zStart goes to the edge of first active sensor volume
			// Z0 goes to the edge of the physical sensor volume
			Z0[r] = zStart - DEADZNLEN; // MODULELEN includes DEADZNLEN
			
			double radius = cp.getDouble(GEOMPATH+"region/radius", r);
			MODULERADIUS[r] = new double[NLAYR];
			for( int l = 0; l < NLAYR; l++ )
			{
				switch( l ) // radius = distance to inner (V layer) backing structure
				{
				case 0: // U = lower / inner
					MODULERADIUS[r][l] = radius - MODULEPOSFAC*SILICONTHICK;
					break;
				case 1: // V = upper / outer
					MODULERADIUS[r][l] = radius + LAYRGAP + MODULEPOSFAC*SILICONTHICK;
					break;
				}
			}
		}
		
		cp.disconnect();
		bConstantsLoaded = true;
		System.out.println(" geometry constants loaded ? "+bConstantsLoaded);
	}
	
	public static synchronized void show()
	{
		if( bConstantsLoaded )
		{
			System.out.println();
			System.out.println("CcdbGeomSvt Parameters:");
			System.out.println("NREG="+ NREG );
			System.out.println("NLAYR="+ NLAYR );
			System.out.println("NSEN="+ NSEN );
			System.out.println("NSTRIP="+ NSTRIP );
			System.out.println("LAYRGAP="+ LAYRGAP );
			System.out.println("PITCH="+ PITCH );
			System.out.println("SILICONTHICK="+ SILICONTHICK );
			System.out.println("TOTSENLEN="+ TOTSENLEN );
			System.out.println("TOTSENWID="+ TOTSENWID );
			System.out.println("ACTIVESENLEN="+ ACTIVESENLEN );
			System.out.println("ACTIVESENWID="+ ACTIVESENWID );
			System.out.println("DEADZNLEN="+ DEADZNLEN );
			System.out.println("DEADZNWID="+ DEADZNWID );
			System.out.println("MICROGAP="+ MICROGAP );
			System.out.println("STEREOANGLE="+ STEREOANGLE );
			System.out.println("PHI0="+ PHI0 );
			System.out.println("LOCZAXISROTATION="+ LOCZAXISROTATION );
			System.out.println("MODULEPOSFAC="+ MODULEPOSFAC );
			System.out.printf("MODULELEN=%8.3f\n", MODULELEN );
			for( int r = 0; r < NREG; r++ )
				System.out.println("NSECT["+r+"]="+NSECT[r] );
			for( int r = 0; r < NREG; r++ )
				System.out.println("STATUS["+r+"]="+STATUS[r] );
			for( int r = 0; r < NREG; r++ )
				System.out.printf("Z0["+r+"]=%8.3e\n", Z0[r] );
			for( int r = 0; r < NREG; r++ )
			{
				System.out.print("MODULERADIUS["+r+"]=");
				for( int l = 0; l < NLAYR; l++ )
					System.out.printf("%8.3e ", MODULERADIUS[r][l] );
				System.out.println();
			}
			System.out.println();
		}
	}
}
