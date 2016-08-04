package SVTFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

import SVTMisc.Matrix;
import SVTMisc.Utils;

/**
 * <h1> Geometry for the SVT </h1>
 * 
 * length unit: mm (3D Primitives), cm (Geant4Basic volume positions) <br>
 * angle unit: deg <br>
 * 
 * Conventions:
 * <ul>
 * <li> svt = four concentric regions / superlayers </li>
 * <li> region / superlayer = ring of a variable number of sectors </li>
 * <li> sector module = pair of sensor  modules and backing structure, connected and stabilised by copper and peek supports </li>
 * <li> sensor module = triplet of sensors </li>
 * <li> sensor = silicon with etched strips in active region </li>
 * <li> layer = plane of sensitive strips, spanning active regions of module </li>
 * <li> strip = sensitive line </li>
 * </ul>
 * 
 * @author pdavies
 * @version 1.0
 */

public final class SVTGeant4Factory
{
	private static String ccdbPath = "/geometry/cvt/svt/";
	private static boolean bLoadedConstants = false; // only load constants once
	
	private final Geant4Basic motherVol = new Geant4Basic("svt", "Box", 0 );
	private final HashMap< String, String > parameters = new LinkedHashMap<>(); // store core parameters from CCDB for GEMC
	private final HashMap< String, String > properties = new LinkedHashMap<>(); // author, email, date
	
	private boolean bShift = false; // switch to select whether alignment shifts are applied
	//private boolean bLoadedShiftData = false; // only load shift data once
	private double scaleT = 1.0, scaleR = 1.0;
	private String filenameShiftSurvey = null;
	private int NSHIFTDATARECLEN = 7;
	
	private int regionMin, regionMax, moduleMin, moduleMax, layerMin, layerMax;
	private int[] sectorMin, sectorMax;
	
	// SVT GEOMETRY PARAMETERS
	// fundamentals
	public static int NREGIONS; // number of regions
	public static int[] NSECTORS; // number of sectors in a region
	public static int NFIDUCIALS; // number of survey fiducials on a sector module
	public static int NMODULES; // number of modules in a sector
	public static int NSENSORS; // number of sensors in a module
	public static int NSTRIPS; // number of strips in a layer
	public static double STRIPOFFSETWID; // offset of first intermediate sensor strip from edge of active zone
	public static double READOUTPITCH; // distance between start of strips along front of hybrid sensor
	public static double STEREOANGLE; // total angle swept by sensor strips
	public static int[] STATUS; // whether a region is used in Reconstruction
	//
	// position and orientation of layers
	public static double PHI0;
	public static double SECTOR0;
	public static double[] REFRADIUS; // outer side of U (inner) module
	public static double LAYERPOSFAC; // location of strip layer within sensor volume
	public static double[] Z0ACTIVE; // Cu edge of hybrid sensor's active volume
	// fiducials
	public static double[] SUPPORTRADIUS; // from MechEng drawings, to inner side of wide copper part
	public static double FIDCUX;
	public static double FIDPKX;
	public static double FIDORIGINZ;
	public static double FIDCUZ;
	public static double FIDPKZ0;
	public static double FIDPKZ1;
	//
	// dimensions of sensors
	public static double ACTIVESENWID;
	public static double PHYSSENWID;
	public static double DEADZNWID;
	//
	public static double SILICONTHK;
	//
	public static double PHYSSENLEN;
	public static double ACTIVESENLEN;
	public static double DEADZNLEN;
	public static double MICROGAPLEN; // spacing between sensors
	//
	// dimensions of passive materials
	public static int NMATERIALS;
	public static double[][] MATERIALS;
	public static HashMap< String, double[] > MATERIALSBYNAME = new LinkedHashMap<>();
	//
	// calculated on load()
	public static int NLAYERS; // total number of layers in a sector
	public static double[][] LAYERRADIUS; // radius to strip planes
	public static double LAYERGAPTHK; // distance between pairs of layers within a sector
	public static double MODULELEN;    // || DZ |  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  || DZ ||
	public static double STRIPLENMAX;  //      ||  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  ||
	public static double MODULEWID; // || DZ | AZ | DZ || (along width)
	
	// data for alignment shifts
	private static double[][] SHIFTDATA = null;
	
	
	/**
	 * Constructs a new geometry factory and sets up the default configuration.
	 * 
	 * Please load the following tables from CCDB: svt, region, support, fiducial, alignment. 
	 * 
	 * @param cp a DatabaseConstantProvider that has loaded the necessary tables
	 */
	public SVTGeant4Factory( ConstantProvider cp )
	{
		getConstants( cp );
		
		sectorMin = new int[NREGIONS];
		sectorMax = new int[NREGIONS];
		
		// default behaviour
		setRange( 1, NREGIONS, new int[]{ 1, 1, 1, 1 }, NSECTORS, 1, NMODULES ); // all regions, sectors, and modules 
		//setAlignmentShift("ccdb");
	}
	
	
	/**
	 * Returns the path to the database directory that contains the core parameters and constants for the SVT.<br>
	 * To use with DatabaseConstantProvider:<ul>
	 * <li>Access tables with {@code getCcdbPath() +"table"}.</li>
	 * <li>Access constants with {@code getCcdbPath() +"table/constant"}.</li></ul>
	 * 
	 * @return String a path to a directory in CCDB of the format {@code "/geometry/detector/"}
	 */
	public static String getCcdbPath()
	{
		return ccdbPath;
	}
	
	
	/**
	 * Sets the path to the database directory that contains the core parameters and constants for the SVT.<br>
	 * 
	 * @param aPath a path to a directory in CCDB of the format {@code "/geometry/detector/"}
	 */
	public static void setCcdbPath( String aPath )
	{
		ccdbPath = aPath;
	}
	
	
	/**
	 * Returns the first region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the region index
	 */
	public int getRegionMin()
	{
		return regionMin;
	}

	
	/**
	 * Returns the last region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NREGIONS
	 * 
	 * @return int an upper bound for the region index
	 */
	public int getRegionMax()
	{
		return regionMax;
	}

	
	/**
	 * Returns the first sector in each region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the sector index in each region
	 */
	public int[] getSectorMin()
	{
		return sectorMin;
	}

	
	/**
	 * Returns the last sector in each region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NSECTORS[region]
	 * 
	 * @return int an upper bound for the sector index in each region
	 */
	public int[] getSectorMax()
	{
		return sectorMax;
	}

	
	/**
	 * Returns the first module in a sector to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the module index
	 */
	public int getModuleMin()
	{
		return moduleMin;
	}

	
	/**
	 * Returns the last module in a sector to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NMODULES
	 * 
	 * @return int an upper bound for the module index
	 */
	public int getModuleMax()
	{
		return moduleMax;
	}

	
	/**
	 * Returns the first layer to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the layer index
	 */
	public int getLayerMin()
	{
		return layerMin;
	}

	
	/**
	 * Returns the last layer to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NLAYERS
	 * 
	 * @return int an upper bound for the layer index
	 */
	public int getLayerMax()
	{
		return layerMax;
	}
	
	
	/**
	 * Converts RSF indices to linear index.
	 * Useful for writing data files.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aFiducial an index starting from 0
	 * @return int an index used for fiducial survey data
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static int convertRegionSectorFid2SurveyIndex( int aRegion, int aSector, int aFiducial ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		if( aFiducial < 0 || aFiducial > NREGIONS-1 ){ throw new IllegalArgumentException("fiducial out of bounds"); }
		return convertRegionSector2SvtIndex( aRegion, aSector )*NFIDUCIALS + aFiducial;
	}
	
	
	/**
	 * Converts RS indices to linear index.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @return int an index used for sector modules
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static int convertRegionSector2SvtIndex( int aRegion, int aSector ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		return Utils.subArraySum( NSECTORS, aRegion ) + aSector;
	}
	
	
	/**
	 * Converts linear index to Region, Sector, and Fiducial indices.
	 * Useful for reading data files.
	 * 
	 * @param aSurveyIndex an index used for fiducial survey data
	 * @return int[] an array containing RSF indices
	 * @throws IllegalArgumentException index out of bounds
	 */
	public static int[] convertSurveyIndex2RegionSectorFiducial( int aSurveyIndex ) throws IllegalArgumentException
	{
		if( aSurveyIndex < 0 || aSurveyIndex > NREGIONS-1 ){ throw new IllegalArgumentException("survey index out of bounds"); }
		
		int region = -1, sector = -1, fiducial = -1;
		for( int i = 0; i < NREGIONS; i++ )
		{
			int l0 = Utils.subArraySum( NSECTORS, i   )*NFIDUCIALS;
			int l1 = Utils.subArraySum( NSECTORS, i+1 )*NFIDUCIALS;
			if( i == NREGIONS-1 ){ l1 = l0 + NSECTORS[NREGIONS-1]*NFIDUCIALS; }
			if( l0 <= aSurveyIndex && aSurveyIndex <= l1-1 ){ region = i; break; }
		}
		//System.out.println("l="+l[0]+" "+l[1]+" "+l[2]+" "+l[3]+" "+(l[3] + NSECTORS[3]*NFIDUCIALS-1));
		
		sector = aSurveyIndex / NFIDUCIALS - Utils.subArraySum( NSECTORS, region );
		fiducial = aSurveyIndex % NFIDUCIALS;
		
		return new int[]{ region, sector, fiducial };
	}
	
	
	/**
	 * Converts Layer index to Region, Module indices.
	 * 
	 * @param aLayer an index starting from 0
	 * @return int[] an array containing RM indices
	 * @throws IllegalArgumentException index out of bounds
	 */
	public static int[] convertLayer2RegionModule( int aLayer ) throws IllegalArgumentException // l=[0:7], NMODULES = 2
	{
		if( aLayer < 0 || aLayer > NLAYERS-1 ){ throw new IllegalArgumentException("layer out of bounds"); }
		return new int[]{ aLayer/NMODULES, aLayer%NMODULES }; // r=[0:3], m=[0:1]
	}
	
	/**
	 * Converts Region, Module indices to Layer index.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aModule an index starting from 0
	 * @return int layer index starting from 0
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static int convertRegionModule2Layer( int aRegion, int aModule ) throws IllegalArgumentException // U/inner(m=0) V/outer(m=1) 
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aModule < 0 || aModule > NMODULES-1 ){ throw new IllegalArgumentException("module out of bounds"); }
		return aRegion*NMODULES + aModule; // zero-based indices
	}
	
	
	/** 
	 * Sets the range of indices to cycle over when generating the geometry in makeVolumes().
	 * Enter 0 to use the previous/default value.
	 * 
	 * @param aLayerMin an index starting from 1
	 * @param aLayerMax an index starting from 1
	 * @param aSectorMin an index starting from 1
	 * @param aSectorMax an index starting from 1
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public void setRange( int aLayerMin, int aLayerMax, int[] aSectorMin, int[] aSectorMax ) throws IllegalArgumentException
	{
		if( aLayerMin < 0 || aLayerMax > NLAYERS ){ throw new IllegalArgumentException("layer out of bounds"); }
		if( aSectorMin.length != NREGIONS || aSectorMax.length != NREGIONS ){ throw new IllegalArgumentException("invalid sector array"); }
		if( aLayerMin > aLayerMax ){ throw new IllegalArgumentException("invalid layer min/max"); }
		
		for( int i = 0; i < NREGIONS; i++ )
		{
			if( aSectorMin[i] < 0 || aSectorMax[i] > NSECTORS[i] )
				throw new IllegalArgumentException("sector out of bounds");
			if( aSectorMin[i] > aSectorMax[i] )
				throw new IllegalArgumentException("invalid sector min/max");
		}
		
		// 0 means use default / previous value
		if( aLayerMin != 0 )
		{
			layerMin = aLayerMin;
			regionMin = convertLayer2RegionModule( aLayerMin )[0]+1;
			moduleMin = convertLayer2RegionModule( aLayerMin )[1]+1;
		}
		if( aLayerMax != 0 )
		{
			layerMax = aLayerMax;
			regionMax = convertLayer2RegionModule( aLayerMax )[0]+1;
			moduleMax = convertLayer2RegionModule( aLayerMax )[1]+1;
		}
		for( int i = 0; i < NREGIONS; i++ )
		{
			if( aSectorMin[i] != 0 ){ sectorMin[i] = aSectorMin[i]; }
			if( aSectorMax[i] != 0 ){ sectorMax[i] = aSectorMax[i]; }
		}
	}
	
	
	/** 
	 * Sets the range of indices to cycle over when generating the geometry in makeVolumes().
	 * Enter 0 to use the previous/default value.
	 *  
	 * @param aRegionMin an index starting from 1
	 * @param aRegionMax an index starting from 1
	 * @param aSectorMin an index starting from 1
	 * @param aSectorMax an index starting from 1
	 * @param aModuleMin an index starting from 1
	 * @param aModuleMax an index starting from 1
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public void setRange( int aRegionMin, int aRegionMax, int[] aSectorMin, int[] aSectorMax, int aModuleMin, int aModuleMax ) throws IllegalArgumentException
	{
		if( aRegionMin < 0 || aRegionMax > NREGIONS ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSectorMin.length != NREGIONS || aSectorMax.length != NREGIONS ){ throw new IllegalArgumentException("invalid sector array"); }
		if( aRegionMin > aRegionMax ){ throw new IllegalArgumentException("invalid region min/max"); }
		
		for( int i = 0; i < NREGIONS; i++ )
		{
			if( aSectorMin[i] < 0 || aSectorMax[i] > NSECTORS[i] )
				throw new IllegalArgumentException("sector out of bounds");
			if( aSectorMin[i] > aSectorMax[i] )
				throw new IllegalArgumentException("invalid sector min/max");
		}
		if( aModuleMin < 0 || aModuleMax > NMODULES )
			throw new IllegalArgumentException("module out of bounds");
		
		// 0 means use default / previous value
		if( aRegionMin != 0 ){ regionMin = aRegionMin; }
		if( aRegionMax != 0 ){ regionMax = aRegionMax; }
		for( int i = 0; i < NREGIONS; i++ )
		{
			if( aSectorMin[i] != 0 ){ sectorMin[i] = aSectorMin[i]; }
			if( aSectorMax[i] != 0 ){ sectorMax[i] = aSectorMax[i]; }
		}
		if( aModuleMin != 0 ){ moduleMin = aModuleMin; }
		if( aModuleMax != 0 ){ moduleMax = aModuleMax; }
		if( aRegionMin != 0 || aModuleMin != 0 ){ layerMin = convertRegionModule2Layer( regionMin-1, moduleMin-1 )+1; }
		if( aRegionMax != 0 || aModuleMax != 0 ){ layerMax = convertRegionModule2Layer( regionMax-1, moduleMax-1 )+1; }
	}
	
	
	/**
	 * Sets the range of indices to cycle over when generating the geometry in makeVolumes().
	 * Enter 0 to use the previous/default value.
	 * 
	 * @param aRegion an index starting from 1
	 * @param aSectorMin an index starting from 1
	 * @param aSectorMax an index starting from 1
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public void setRange( int aRegion, int aSectorMin, int aSectorMax ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSectorMin < 0 || aSectorMin > NSECTORS[aRegion] ){ throw new IllegalArgumentException("sectorMin out of bounds"); }
		if( aSectorMax < 0 || aSectorMax > NSECTORS[aRegion] ){ throw new IllegalArgumentException("sectorMax out of bounds"); }
		if( aSectorMin > aSectorMax ){ throw new IllegalArgumentException("invalid sector min/max"); }
		
		// 0 means use default / previous value
		if( aRegion != 0 ){ regionMin = aRegion; regionMax = aRegion; }
		if( aSectorMin != 0 ){ sectorMin[aRegion-1] = aSectorMin; }
		if( aSectorMax != 0 ){ sectorMax[aRegion-1] = aSectorMax; }
		if( aRegion != 0 ){ layerMin = convertRegionModule2Layer( regionMin-1, moduleMin-1 )+1;
						    layerMax = convertRegionModule2Layer( regionMax-1, moduleMax-1 )+1; }
	}
	
	
	/**
	 * Returns a string to display the current range of indices.
	 * 
	 * @return String a line of text
	 */
	public String showRange()
	{
		String range = ""; 
		range = range +"layer ["+layerMin+":"+layerMax+"]";
		range = range +" region ["+regionMin+":"+regionMax+"]";
		range = range +" module ["+moduleMin+":"+moduleMax+"]";
		range = range +" sector ";
		for( int i = 0; i < NREGIONS; i++ )
			range = range +"["+sectorMin[i]+":"+sectorMax[i]+"]";
		return range;
	}
	
	
	/**
	 * Sets whether alignment shifts from CCDB should be applied to the geometry during generation.
	 * If set to true, also loads the alignment data.
	 * 
	 * @param b true/false
	 */
	public void setAlignmentShift( boolean b )
	{
		bShift = b;
	}
	
	
	/**
	 * Sets scale factors to amplify alignment shifts for visualisation purposes.
	 *  
	 * @param aScaleTranslation a scale factor for translation shifts
	 * @param aScaleRotation a scale factor for rotation shifts
	 */
	public void setAlignmentShiftScale( double aScaleTranslation, double aScaleRotation  )
	{
		scaleT = aScaleTranslation;
		scaleR = aScaleRotation;
	}
	
	
	/**
	 * Reads alignment data from the given file.
	 * 
	 * @param aFilename a filename
	 */
	public void setAlignmentShift( String aFilename )
	{
		filenameShiftSurvey = aFilename;			
		try{ SHIFTDATA = Utils.inputTaggedData( filenameShiftSurvey, NSHIFTDATARECLEN ); } // 3 translation(x,y,z), 4 rotation(x,y,z,a)
		catch( Exception e ){ e.printStackTrace(); System.exit(-1); } // trigger fatal error
		if( SHIFTDATA == null ){ System.err.println("stop: SHIFTDATA is null after reading file \""+filenameShiftSurvey+"\""); System.exit(-1); }
		bShift = true;
	}
	
	
	/**
	 * Reads alignment data from CCDB.
	 * 
	 * @param cp a DatabaseConstantProvider that has loaded the "alignment" table
	 */
	public void setAlignmentShift( ConstantProvider cp )
	{
		System.out.println("reading fiducial survey data from database");
		int nSectorsTotal = convertRegionSector2SvtIndex( NREGIONS-1, NSECTORS[NREGIONS-1]-1)+1;
		//System.out.println("n="+nSectorsTotal );
		
		SHIFTDATA = new double[nSectorsTotal][];
		
		for( int i = 0; i < nSectorsTotal; i++ )
		{
			double tx = cp.getDouble(getCcdbPath()+"alignment/tx", i );
			double ty = cp.getDouble(getCcdbPath()+"alignment/ty", i );
			double tz = cp.getDouble(getCcdbPath()+"alignment/tz", i );
			double rx = cp.getDouble(getCcdbPath()+"alignment/rx", i );
			double ry = cp.getDouble(getCcdbPath()+"alignment/ry", i );
			double rz = cp.getDouble(getCcdbPath()+"alignment/rz", i );
			double ra = cp.getDouble(getCcdbPath()+"alignment/ra", i );
			SHIFTDATA[i] = new double[]{ tx, ty, tz, rx, ry, rz, ra };
			//System.out.printf("%d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n", i+1, tx, ty, tz, rx, ry, rz, ra );
		}
		bShift = true;
	}
	
	
	/**
	 * Returns whether alignment shifts are applied.
	 * 
	 * @return boolean true/false
	 */
	public boolean isSetAlignmentShift()
	{
		return bShift;
	}
	
	
	/**
	 * Returns the alignment data.
	 * 
	 * @return double[][] an array of translations and axis-angle rotations of the form { tx, ty, tz, rx, ry, rz, ra }
	 */
	public double[][] getShiftData()
	{
		if( SHIFTDATA == null ){ System.err.println("stop: SHIFTDATA requested is null"); System.exit(-1); }
		return SHIFTDATA;
	}
	
	
	/**
	 * Returns the mother volume to export the geometry to, for example, a GDML file.
	 * 
	 * @return Geant4Basic the mother volume
	 */
	public Geant4Basic getMotherVolume()
	{
		return motherVol;
	}
	
	
	/**
	 * Appends a tag to the current volumes.
	 * Useful to avoid conflicts in a GDML file.
	 * 
	 * @param aTag something to add
	 */
	public void appendName( String aTag )
	{
		Utils.appendName( motherVol, aTag );
	}
	
	
	/**
	 * Returns a list of all the volumes in the gemcString() format.
	 * 
	 * @return String multiple lines of text
	 */
	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		_toString( motherVol, str );
		return str.toString();
	}
	
	
	/**
	 * Returns a value from the Properties HashMap.
	 * 
	 * @param aName name of a key
	 * @return String the value associated with the given key, or "none" if the key does not exist
	 */
	public String getProperty( String aName )
	{
		return properties.containsKey( aName ) ? properties.get( aName ) : "none";
	}
	
	
	/**
	 * Returns a value from the Parameters HashMap.
	 * 
	 * @param aName name of a key
	 * @return String the value associated with the given key, or "none" if the key does not exist.
	 */
	public String getParameter( String aName )
	{
		return parameters.containsKey( aName ) ? parameters.get( aName ) : "none";
	}
	
	
	/**
	 * Returns the "Parameters" HashMap. 
	 * Used by GEMC to interface with CCDB.
	 * 
	 * @return HashMap a HashMap containing named constants and core parameters.
	 */
	public HashMap<String, String> getParameters()
	{
		return parameters;
	}
	
	
	/**
	 * Reads all the necessary constants from CCDB into static variables.
	 * Please use a DatabaseConstantProvider to access CCDB and load the following tables:
	 * svt, region, support, fiducial, material.
	 *  
	 * @param cp a ConstantProvider
	 */
	public static void getConstants( ConstantProvider cp )
	{
		if( !bLoadedConstants )
		{
			// read constants from svt table
			NREGIONS = cp.getInteger( ccdbPath+"svt/nRegions", 0 );
			NMODULES = cp.getInteger( ccdbPath+"svt/nModules", 0 );
			NSENSORS = cp.getInteger( ccdbPath+"svt/nSensors", 0 );
			NSTRIPS = cp.getInteger( ccdbPath+"svt/nStrips", 0 );
			NFIDUCIALS = cp.getInteger( ccdbPath+"svt/nFiducials", 0 );
			
			READOUTPITCH = cp.getDouble( ccdbPath+"svt/readoutPitch", 0 );
			STEREOANGLE = Math.toRadians(cp.getDouble( ccdbPath+"svt/stereoAngle", 0 ));
			PHI0 = Math.toRadians(cp.getDouble( ccdbPath+"svt/phiStart", 0 ));
			SECTOR0 = Math.toRadians(cp.getDouble( ccdbPath+"svt/zRotationStart", 0 ));
			LAYERPOSFAC = cp.getDouble( ccdbPath+"svt/modulePosFac", 0 );
			
			SILICONTHK = cp.getDouble( ccdbPath+"svt/siliconThick", 0 );
			PHYSSENLEN = cp.getDouble( ccdbPath+"svt/physSenLen", 0 );
			PHYSSENWID = cp.getDouble( ccdbPath+"svt/physSenWid", 0 );
			ACTIVESENLEN = cp.getDouble( ccdbPath+"svt/activeSenLen", 0 );
			ACTIVESENWID = cp.getDouble( ccdbPath+"svt/activeSenWid", 0 );
			DEADZNLEN = cp.getDouble( ccdbPath+"svt/deadZnLen", 0 );
			DEADZNWID = cp.getDouble( ccdbPath+"svt/deadZnWid", 0 );
			MICROGAPLEN = cp.getDouble( ccdbPath+"svt/microGap", 0 );
			
			FIDCUX = cp.getDouble( ccdbPath+"fiducial/CuX", 0 );
			FIDPKX = cp.getDouble( ccdbPath+"fiducial/PkX", 0 );
			FIDORIGINZ = cp.getDouble( ccdbPath+"fiducial/OriginZ", 0 );
			FIDCUZ = cp.getDouble( ccdbPath+"fiducial/CuZ", 0 );
			FIDPKZ0 = cp.getDouble( ccdbPath+"fiducial/PkZ0", 0 );
			FIDPKZ1 = cp.getDouble( ccdbPath+"fiducial/PkZ1", 0 );
			
			// read constants from materials table
			NMATERIALS = 12;
			MATERIALS = new double[NMATERIALS][3];
			for( int m = 0; m < NMATERIALS; m++ )
			{
				MATERIALS[m] = new double[]{ cp.getDouble( ccdbPath+"material/wid", m ),
											 cp.getDouble( ccdbPath+"material/thk", m ),
											 cp.getDouble( ccdbPath+"material/len", m ) };
				switch( m )
				{
				case 0:
					MATERIALSBYNAME.put("rohacell", MATERIALS[m] );
					break;
				case 1:
					MATERIALSBYNAME.put("plastic", MATERIALS[m] );
					break;
				case 2:
					MATERIALSBYNAME.put("carbonFiber", MATERIALS[m] );
					break;
				case 3:
					MATERIALSBYNAME.put("busCable", MATERIALS[m] );
					break;
				case 4:
					MATERIALSBYNAME.put("epoxy", MATERIALS[m] );
					break;
				case 5:
					MATERIALSBYNAME.put("wirebond", MATERIALS[m] );
					break;
				case 6:
					MATERIALSBYNAME.put("pitchAdaptor", MATERIALS[m] );
					break;
				case 7:
					MATERIALSBYNAME.put("pcBoard", MATERIALS[m] );
					break;
				case 8:
					MATERIALSBYNAME.put("chip", MATERIALS[m] );
					break;
				case 9:
					MATERIALSBYNAME.put("rail", MATERIALS[m] );
					break;
				case 10:
					MATERIALSBYNAME.put("pad", MATERIALS[m] );
					break;
				}
			}
			
			// calculate derived constants
			NLAYERS = NMODULES*NREGIONS;
			MODULELEN = NSENSORS*(ACTIVESENLEN + 2*DEADZNLEN) + (NSENSORS - 1)*MICROGAPLEN;
			STRIPLENMAX = MODULELEN - 2*DEADZNLEN;
			MODULEWID = ACTIVESENWID + 2*DEADZNWID;
			STRIPOFFSETWID = cp.getDouble(ccdbPath+"svt/stripStart", 0 );
			LAYERGAPTHK = cp.getDouble(ccdbPath+"svt/layerGapThk", 0 ); // generated from fiducial analysis
			// do not use the current material values in CCDB, which are for the incorrect GEMC BST
			//double layerGapThk = MATERIALSBYNAME.get("rohacell")[1] + 2*(MATERIALSBYNAME.get("carbonFiber")[1] + MATERIALSBYNAME.get("busCable")[1] + MATERIALSBYNAME.get("epoxy")[1]); // construct from material thicknesses instead
			
			//System.out.println("LAYERGAPTHK="+LAYERGAPTHK);
			//System.out.println("layerGapThk="+layerGapThk);
			
			// read constants from region and support table
			NSECTORS = new int[NREGIONS];
			STATUS = new int[NREGIONS];
			LAYERRADIUS = new double[NREGIONS][NMODULES];
			REFRADIUS = new double[NREGIONS];
			Z0ACTIVE = new double[NREGIONS];
			SUPPORTRADIUS = new double[NREGIONS];
			
			// LAYERRADIUS and ZSTARTACTIVE are used primarily by the Reconstruction and getStrip()
			for( int region = 0; region < NREGIONS; region++ )
			{
				NSECTORS[region] = cp.getInteger(ccdbPath+"region/nSectors", region );
				STATUS[region] = cp.getInteger(ccdbPath+"region/status", region );
				Z0ACTIVE[region] = cp.getDouble(ccdbPath+"region/zStart", region ); // Cu edge of hybrid sensor's active volume
				REFRADIUS[region] = cp.getDouble(ccdbPath+"region/radius", region); // radius to outer side of U (inner) module
				
				SUPPORTRADIUS[region] = cp.getDouble(ccdbPath+"support/radius", region);
				
				// Consider Region 1 Sector 6 (not to scale)
				//
				// y (vertical)                                    .------------------^-------------
				// ^         			   						   | V (outer)		  |
				// |                                               | sensor layer	  | 0.32 silicon thickness
				// |                                               |				  |
				// |                                      .--------+--------------^---v------------- module radius 1
				// |                                      | carbon fibre etc	  |
				// |------^-----------------^-------------+-----------------------|----------------- fiducial layer
				// |      |   				|									  |
				// |	  |					|									  |
				// |      |   				| 2.50			rohacell			  | 3.236 layer gap
				// |	  |	2.88			|									  |
				// |      |         	    |									  |
				// |      |              +--v-------------+-----------------------|------------------ module radius 0
				// |  	  |				 |                | carbon fibre etc	  |
				// |      |				 |				  '---^----+--------------v-----^------------ radius CCDB
				// |------v-------^------'					  |	   | 				    |						radius MechEng
				// |              |                           |    | U (inner)			| 0.32 silicon thickness
				// |              |                           |    | sensor layer		|
				// |              |                           |    '--------------------v-----------
				// |			  | support					  | module 
				// |			  | radius					  | radius
				// |			  | 						  |
				// o==============v===========================v===================================-> z (beamline)
				
				for( int m = 0; m < NMODULES; m++ )
				{
					switch( m ) 
					{
					case 0: // U = lower / inner
						LAYERRADIUS[region][m] = REFRADIUS[region] - LAYERPOSFAC*SILICONTHK;
						break;
					case 1: // V = upper / outer
						LAYERRADIUS[region][m] = REFRADIUS[region] + LAYERGAPTHK + LAYERPOSFAC*SILICONTHK;
						break;
					}
					//System.out.println("LAYERRADIUS "+ LAYERRADIUS[region][m]);
				}
			}
			
			// check one constant from each table
			//if( NREGIONS == 0 || NSECTORS[0] == 0 || FIDCUX == 0 || MATERIALS[0][0] == 0 || SUPPORTRADIUS[0] == 0 )
				//throw new NullPointerException("please load the following tables from CCDB in "+ccdbPath+"\n svt\n region\n support\n fiducial\n material\n");
			
			bLoadedConstants = true;
		}
	}
	
	
	/**
	 * Populates the HashMaps with constants.
	 */
	public void putParameters()
	{
		properties.put("author", "P. Davies");
		properties.put("email", "pdavies@jlab.org");
		properties.put("date", "7/15/16");
		
		parameters.put("unit_length", "mm");
		parameters.put("unit_angle", "deg");
		parameters.put("nregions", Integer.toString(NREGIONS) );
		parameters.put("nmodules", Integer.toString(NMODULES) );
		parameters.put("nsensors", Integer.toString(NSENSORS) );
		parameters.put("nstrips", Integer.toString(NSTRIPS) );
		parameters.put("readout_pitch", Double.toString(READOUTPITCH) );
		parameters.put("silicon_thk", Double.toString(SILICONTHK) );
		// ...
		
		/*for( Map.Entry< String, String > entry : parameters.entrySet() )
		{
			System.out.println( entry.getKey() +" "+ entry.getValue() );
		}
		System.exit(0);*/
	}
	
	
	/**
	 * Returns a transformation from the local frame to the lab frame, for the given parameters of a sector module.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aRadius an index starting from 0
	 * @param aZ an index starting from 0
	 * @param aFlip an index starting from 0. adds a 180 deg rotation at the start of the sequence
	 * @return Transformation3D a sequence of transformations
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static Transformation3D getLabFrame( int aRegion, int aSector, double aRadius, double aZ, boolean aFlip ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		
		double phi = -2.0*Math.PI/NSECTORS[aRegion]*aSector + PHI0; // location around target
		Transformation3D labFrame = new Transformation3D();
		if( aFlip ) { labFrame.rotateZ( Math.toRadians(180) ); } // flip U layer
		labFrame.rotateZ( -Math.toRadians(SECTOR0) ).translateXYZ( aRadius, 0, aZ ).rotateZ( phi );
		return labFrame;
	}
	
	
	/**
	 * Returns a sensor strip.
	 * Used by the Reconstruction.
	 * 
	 * @param aLayer an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aStrip an index starting from 0
	 * @return Line3D a strip in the lab frame
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public Line3D getStrip( int aLayer, int aSector, int aStrip ) throws IllegalArgumentException
	{
		if( aLayer < 0 || aLayer > NLAYERS-1 ){ throw new IllegalArgumentException("layer out of bounds"); }
		int[] rm = convertLayer2RegionModule( aLayer );
		if( aSector < 0 || aSector > NSECTORS[rm[0]]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		if( aStrip < 0 || aStrip > NSTRIPS-1 ){ throw new IllegalArgumentException("strip out of bounds"); }
		return getStrip( rm[0], aSector, rm[1], aStrip );
	}


	/**
	 * Returns a sensor strip.
	 * Used by the Reconstruction.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aModule an index starting from 0
	 * @param aStrip an index starting from 0
	 * @return Line3D a strip in the lab frame
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public Line3D getStrip( int aRegion, int aSector, int aModule, int aStrip ) throws IllegalArgumentException // lab frame
	{
		//System.out.println("r "+ aRegion +" s "+ aSector +" m "+ aModule );
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		if( aModule < 0 || aModule > NMODULES-1 ){ throw new IllegalArgumentException("module out of bounds"); }
		
		Line3D stripLine = createStrip( aStrip );
		
		//System.out.printf("%d %d %d %d\n", aRegion, aSector, aModule, aStrip);
		
		double r = LAYERRADIUS[aRegion][aModule];
		double z = Z0ACTIVE[aRegion] + STRIPLENMAX/2;
		Transformation3D labFrame = getLabFrame( aRegion, aSector, r, z, (aModule == 0) ? true : false ); // flip U layer (m=0)
		labFrame.apply( stripLine );
		
		//if( bShift ) _applyShift( stripLine.origin(), SHIFTDATA[convertRegionSector2SvtIndex( aRegion, aSector )], scaleT, scaleR );
		//if( bShift ) _applyShift( stripLine.end(), SHIFTDATA[convertRegionSector2SvtIndex( aRegion, aSector )], scaleT, scaleR );
		
		// step 1: createStrip() in XZ plane
		// step 2: rotate 90 deg
		// step 3: shift along x axis by radius
		// step 4: rotate to correct sector about target
		//
		//								y
		//				  4 			^
		//			   **.^ 			|
		//			**_./   			|
		//		 **_./ 		    		|
		//		  /      				|
		//		 ' 						|
		//	   3 <--------------------- 2 <-._
		//	   *						*     '. 
		//	   *					    *       \
		//	   *				        *       '
		// x <-*-------------------***********--1--------------------
		//	   *						*   
		//	   *						*
		//								|
		//								|
		//								|
		//								|
		//								|
		//								|
		//								|
		//								|
		
		return stripLine;
	}
	
	
	/**
	 * Returns a sensor strip.
	 * 
	 * @param aStrip an index starting from 0
	 * @return Line3D a strip in the local frame
	 * @throws IllegalArgumentException index out of bounds
	 */
	public Line3D createStrip( int aStrip ) throws IllegalArgumentException // local frame
	{
		if( aStrip < 0 || aStrip > NSTRIPS-1 ){ throw new IllegalArgumentException("strip out of bounds"); }
		
		//	   + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
		//     |                                                                         | <- blank intermediate strips, not used
		//   0 |=========================================================================|
		//     |                                                                         |
		//   1 |=====================================------------------------------------|
		//     |                                                                         |
		//   2 |==========================------------------------_______________________|
		//     |                                                                         |
		//   3 |==================-------------------____________________________________|      x
		//   : :                                                                         :      ^
		// 255 |=======-------___________                                                |      |  y
		//     |                         ----------                                      |      | out
		//     + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +      +----> z
		
		// there are 513 strips in total starting on the hybrid side of the sensor module, but only 256 are sensitive / used in the readout
		// the readout pitch is the distance between sensitive strips along the hybrid side
		
		// STRIPWID = ACTIVESENWID/(2*NSTRIPS + 1);
		// MODULELEN = NSENSORS*(ACTIVESENLEN + 2*DEADZNLEN) + (NSENSORS - 1)*MICROGAPLEN;
		// STRIPLENMAX = MODULELEN - 2*DEADZNLEN;
		
		double w, a, q, x0, z0, x1, z1;
		//double r = 0;
		
		w = STRIPOFFSETWID + 0.5*READOUTPITCH + aStrip*READOUTPITCH; // first readout strip is offset from edge of active zone
		a = STEREOANGLE/NSTRIPS*aStrip;
		q = STRIPLENMAX*Math.tan(a); 
		
		x0 = ACTIVESENWID - w;
		z0 = 0;
		x1 = 0;
		z1 = 0;
		
		if( q < x0 )
		{
			x1 = x0 - q;
			z1 = STRIPLENMAX;
			//System.out.println("strip end");
			//r = q*Math.sin(a);
		}
		else
		{
			//x1 = 0;
			z1 = x0/Math.tan(a);
			//System.out.println("strip side");
			//r = x0*Math.sin(a);
		}
		
		/*System.out.println();
		System.out.printf("ACTIVESENWID    %8.3f\n", ACTIVESENWID );
		System.out.printf("ACTIVESENLEN    %8.3f\n", ACTIVESENLEN );
		System.out.printf("NSTRIPS         % d\n", NSTRIPS );
		System.out.printf("(2*NSTRIPS + 1) % d\n", (2*NSTRIPS + 1) );
		System.out.printf("NSENSORS          % d\n", NSENSORS );
		System.out.printf("DEADZNLEN       %8.3f\n", DEADZNLEN );
		System.out.printf("MICROGAPLEN     %8.3f\n", MICROGAPLEN );
		System.out.printf("MODULELEN       %8.3f\n", MODULELEN );
		System.out.printf("STRIPLENMAX     %8.3f\n", STRIPLENMAX );
		System.out.printf("STRIPWID        % 8.3f\n", STRIPWID );
		System.out.printf("STRIPWID*1.5    % 8.3f\n", STRIPWID*1.5 );
		System.out.printf("READOUTPITCH    % 8.3f\n", READOUTPITCH );
		System.out.println();
		System.out.printf("s   %3d\n", aStrip );
		System.out.printf("w  % 8.3f\n", w );
		System.out.printf("a  % 8.3f\n", Math.toDegrees(a) );
		System.out.printf("q  % 8.3f\n", q );
		System.out.printf("x0 % 8.3f\n", x0 );
		System.out.printf("z0 % 8.3f\n", z0 );
		System.out.printf("x1 % 8.3f\n", x1 );
		System.out.printf("z1 % 8.3f\n", z1 );*/
		
		/*System.out.println("STRIPWID "+ d );
		System.out.println("NSTRIPS "+ NSTRIPS );
		System.out.println("ACTIVESENWID = "+ ACTIVESENWID );
		System.out.println("calc wid     = "+ ((NSTRIPS-1)*p + 2*1.5*d) );
		System.out.println("READOUTPITCH = "+ p );
		System.out.println("STRIPWID*2   = "+ 2*d );
		System.out.println("STRIPWID*2*1.5 = "+ 2*1.5*d );
		System.out.println("calc W-(N-1)*p = "+ (ACTIVESENWID - (NSTRIPS-1)*p) );
		System.out.println("calc (N-1)*p   = "+ (NSTRIPS-1)*p );
		System.out.println("calc W-3*d     = "+ (ACTIVESENWID - 2*1.5*d) );*/
		
		Line3D stripLine = new Line3D( new Point3D( x0, 0.0, z0 ), new Point3D( x1, 0.0, z1 ) );
		Transformation3D transform = new Transformation3D();
		transform.translateXYZ( DEADZNWID - MODULEWID/2, 0, DEADZNLEN - MODULELEN/2 ); // align to centre of layer
		transform.apply( stripLine );
		
		return stripLine;
	}
	
	
	/**
	 * Applies an alignment shift to the first given point.
	 * 
	 * @param aPoint a point the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 */
	public void applyShift( Point3D aPoint, double[] aShift, Point3D aNominalCenter )
	{
		_applyShift( aPoint, aShift, aNominalCenter, scaleT, scaleR );
	}
	
	/**
	 * Returns locations of nominal fiducial points.
	 * 
	 * @return double[][] an array of data in fiducial survey format.
	 */
	public static double[][] getNominalFiducialData()
	{
		double [][] data = new double[SVTGeant4Factory.convertRegionSectorFid2SurveyIndex( NREGIONS-1, NSECTORS[NREGIONS-1]-1, NFIDUCIALS-1 )+1][];
		for( int region = 0; region < SVTGeant4Factory.NREGIONS; region++ )
			for( int sector = 0; sector < SVTGeant4Factory.NSECTORS[region]; sector++ )
			{
				Point3D fidPos3Ds[] = getNominalFiducials( region, sector );
				for( int fid = 0; fid < SVTGeant4Factory.NFIDUCIALS; fid++ )
					data[SVTGeant4Factory.convertRegionSectorFid2SurveyIndex( region, sector, fid )] = new double[]{ fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() };
			}
		return data;
	}
	
	
	/**
	 * Returns a set of fiducial points for a sector module after the alignment shifts been applied.
	 * These indices start from 0.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @return Point3D[] an array of fiducial points in the order Cu+, Cu-, Pk
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public Point3D[] getShiftedFiducials( int aRegion, int aSector ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		
		Point3D[] fidPos3Ds = getNominalFiducials( aRegion, aSector ); // lab frame
		Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
		
		for( int f = 0; f < NFIDUCIALS; f++ )
			_applyShift( fidPos3Ds[f], getShiftData()[convertRegionSector2SvtIndex( aRegion, aSector )], fidTri3D.center(), scaleT, scaleR );
		
		return fidPos3Ds;
	}
	
	
	/**
	 * Returns a set of fiducial points for a sector module before any alignment shifts been applied.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @return Point3D[] an array of fiducial points in the order Cu+, Cu-, Pk
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static Point3D[] getNominalFiducials( int aRegion, int aSector ) throws IllegalArgumentException // lab frame
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		
		Point3D[] fidPos3Ds = new Point3D[] { createFiducial(0), createFiducial(1), createFiducial(2) }; // relative to fiducial origin
		
		double fidOriginZ = Z0ACTIVE[aRegion] - DEADZNLEN - FIDORIGINZ;
		double copperWideThk = 2.880;
		double radius = SUPPORTRADIUS[aRegion] + copperWideThk;
		
		Transformation3D labFrame = getLabFrame( aRegion, aSector, radius, fidOriginZ, false );
		
		for( int f = 0; f < NFIDUCIALS; f++ )
			labFrame.apply( fidPos3Ds[f] );
		
		return fidPos3Ds;
	}
	
	
	/**
	 * Returns a fiducial point on a sector module in the local frame.
	 * 
	 * @param aFid an index for the desired point: 0, 1, 2
	 * @return Point3D one of 3 fiducial points: Cu+, Cu-, Pk
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static Point3D createFiducial( int aFid ) throws IllegalArgumentException // local frame
	{
		if( aFid < 0 || aFid > NFIDUCIALS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		
		Point3D fidPos = new Point3D();
		
		switch( aFid )
		{
		case 0: // Cu +
			fidPos.set( FIDCUX, 0.0, -FIDCUZ );
			break;
		case 1: // Cu -
			fidPos.set( -FIDCUX, 0.0, -FIDCUZ );
			break;
		case 2: // Pk
			fidPos.set( -FIDPKX, 0.0, FIDPKZ0 + FIDPKZ1 );
			break;
		}
		return fidPos;
	}


	/**
	 * Generates the geometry using the current range and stores it in the mother volume. 
	 */
	public void makeVolumes()
	{
		System.out.println("generating geometry with the following parameters");
		if( bShift )
		{
			
			System.out.println("  variation: survey shifted" );
			if( !(scaleT - 1.0 < 1.0E-3 && scaleR - 1.0 < 1.0E-3) ){ System.out.println("  scale(T,R): "+ scaleT + " " + scaleR ); }
		}
		else
		{
			System.out.println("  variation: nominal");
		}
		System.out.println( "  "+showRange() );
		
		for( int region = regionMin-1; region < regionMax; region++ ) // NREGIONS
		{
			Geant4Basic regionVol = createRegion( region );
			regionVol.setMother( motherVol );
			regionVol.setName(  regionVol.getName() + region );
			Utils.appendChildrenName(regionVol, "_r"+ region );
		}
	}
	
	
	/**
	 * Returns one region, containing the required number of sector modules.
	 * 
	 * @param aRegion an index starting from 0
	 * @return Geant4Basic a dummy volume positioned at the origin
	 * @throws IllegalArgumentException index out of bounds
	 */
	public Geant4Basic createRegion( int aRegion ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		
		Geant4Basic regionVol = new Geant4Basic("region", "box", 0 );
		//Geant4Basic regionVolDisplay = new Geant4Basic("region", "tube",  );
		
		// pin regionVol to fiducial origin along z?
		double zStartPhysical = Z0ACTIVE[aRegion] - DEADZNLEN; // Cu edge of hybrid sensor's physical volume
		//regionVol.setPosition( 0, 0, (zStartPhysical - FIDORIGINZ)*0.1 ); // central dowel hole
		
		// create faraday cage here?
		
		for( int sector = sectorMin[aRegion]-1; sector < sectorMax[aRegion]; sector++ ) // NSECTORS[region]
		{
			Geant4Basic sectorVol = createSector();
			sectorVol.setMother( regionVol );
			
			sectorVol.setName( sectorVol.getName() + sector ); // append name
			Utils.appendChildrenName( sectorVol, "_s"+ sector ); // append tag to end of name (material replacement search fails if it's prepended)
			
			double phi = -2.0*Math.PI/NSECTORS[aRegion]*sector + PHI0; // module rotation about target / origin
			double psi = phi - SECTOR0; // module rotation about centre of geometry
			
			Transformation3D rotatePhi = new Transformation3D();
			rotatePhi.rotateZ( phi );
			
			// mm
			double copperWideThk = 2.880;
			double fiducialRadius = SUPPORTRADIUS[aRegion] + copperWideThk;
			
			Point3D pos = new Point3D( fiducialRadius, 0.0, zStartPhysical - FIDORIGINZ ); // put sector in lab frame, not region frame
			rotatePhi.apply( pos );
			sectorVol.setPosition( pos.x()*0.1, pos.y()*0.1, pos.z()*0.1 );
			sectorVol.setRotation("xyz", 0.0, 0.0, -psi ); // change of sign for active/alibi -> passive/alias rotation
			
			//Geant4Basic sectorBall = new Geant4Basic("sectorBall"+sector, "Orb", 0.2 );
			//sectorBall.setMother( sectorVol );
						
			if( bShift )
			{
				//System.out.println("N "+sectorVol.gemcString() );
				Point3D[] fidPos3Ds = getNominalFiducials( aRegion, sector );
				Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
				
				double [] shift = getShiftData()[convertRegionSector2SvtIndex( aRegion, sector )].clone();
				Vector3D vec = new Vector3D( shift[3], shift[4], shift[5] ).asUnit();
				vec.scale(-100);
				//vec.show();
				Geant4Basic vecVol = Utils.createArrow("rotAxis"+sector, vec, 2.0, 1.0, true, true, false );
				vecVol.setPosition( fidTri3D.center().x()*0.1, fidTri3D.center().y()*0.1, fidTri3D.center().z()*0.1 );
				vecVol.setMother( regionVol );
				//System.out.println( vecVol.gemcString() );
				
				int n = 1;
				double d = shift[6]/n;
				for( int i = 1; i < n; i++ )
				{
					System.out.println( "vol "+ i );
					Geant4Basic stepVol = Utils.clone( sectorVol, regionVol );
					//stepVol.setMother( new Geant4Basic("dummy", "Box", 0 ) );
					stepVol.setMother(regionVol);
					Utils.appendName( stepVol, "_"+i );
					//Utils.shiftPosition( stepVol, 0, i, 0);
					shift[6] = i*d;
					_applyShift( stepVol, shift, fidTri3D.center(), scaleT, scaleR );
					//System.out.println("  "+stepVol.gemcString() );
					//for( int j = 0; j < stepVol.getChildren().size(); j++ )
						//System.out.println( stepVol.getChildren().get(j).gemcString() );
				}
				
				_applyShift( sectorVol, getShiftData()[convertRegionSector2SvtIndex( aRegion, sector )], fidTri3D.center(), scaleT, scaleR );
				//System.out.println("S "+sectorVol.gemcString() );
			}
		}
		
		return regionVol;
	}
	
	
	/**
	 * Returns one sector module, containing a pair of sensor modules and backing structure.
	 * 
	 * @return Geant4Basic a dummy volume positioned in the lab frame
	 */
	public Geant4Basic createSector()
	{
		Geant4Basic sectorVol = new Geant4Basic("sector", "Box", 0 );
		
		// position components relative to fiducial layer
		
		//create rohacell backing structure
		
		// for each side (U,V)
		// 		create sensor module
		// 		create passive materials (carbon fibre, bus cable, epoxy)
		
		double rohacellThickness =    2.500;
		double carbonFibreThickness = 0.190;
		double busCableThickness =    0.078;
		double epoxyThickness =       0.065;
		
		double passiveThickness = carbonFibreThickness + busCableThickness + epoxyThickness;
		
		//Geant4Basic rohacellVol = createRohacell( cp );
		//rohacellVol.setMother( sectorVol );
		// set position relative to pitch adaptor, calculated manually from a picture
		// double rohacellStart = pitchAdaptorRefZ - (1.70 +- 0.09)
		//rohacellVol.setPosition( 0.0, -rohacellThickness/2, 0.0 ); // where along z?
		
		List<Geant4Basic> moduleVols = new ArrayList<>();
		
		for( int module = moduleMin-1; module < moduleMax; module++ ) // NMODULES
		{
			moduleVols.add( createModule() );
			moduleVols.get(module).setMother( sectorVol );
			moduleVols.get(module).setName( moduleVols.get(module).getName() + module );
			Utils.appendChildrenName( moduleVols.get(module), "_m"+ module );
			
			double moduleRadius = 0.0;
			switch( module ) 
			{
			case 0: // U = lower / inner
				moduleRadius = 0.0 - rohacellThickness - passiveThickness - 0.5*SILICONTHK; // relative to fiducial layer
				break;
			case 1: // V = upper / outer
				moduleRadius = passiveThickness + 0.5*SILICONTHK;
				break;
			}
			moduleVols.get(module).setPosition( 0.0, moduleRadius*0.1, (FIDORIGINZ + MODULELEN/2)*0.1 );
		}
		
		return sectorVol;
	}
	
	
	/**
	 * Returns one rohacell copmonent.
	 * 
	 * @return Geant4Basic a volume positioned at the origin
	 */
	public Geant4Basic createRohacell()
	{	
		double rohacellWidth =          38.000;
		double rohacellThickness =       2.500;
		double rohacellLength =        353.070;
		
		Geant4Basic rohacellVol = new Geant4Basic( "rohacell", "Box", rohacellWidth*0.1, rohacellThickness*0.1, rohacellLength*0.1 );
		
		return rohacellVol;
	}
	
	
	/**
	 * Returns one sensor module, containing 3 physical sensors.
	 * 
	 * @return Geant4Basic a volume positioned at the origin
	 */
	public Geant4Basic createModule()
	{		
		Geant4Basic moduleVol = new Geant4Basic( "module", "Box", PHYSSENWID*0.1, SILICONTHK*0.1, MODULELEN*0.1 );
				
		List<Geant4Basic> sensorVols = new ArrayList<>();
		
		for( int sensor = 0; sensor < NSENSORS; sensor++ )
		{
			sensorVols.add( createSensorPhysical() ); // includes active and dead zones as children
			sensorVols.get(sensor).setMother( moduleVol );
			sensorVols.get(sensor).setName( sensorVols.get(sensor).getName() + sensor ); // add switch for hybrid, intermediate and far labels?
			Utils.appendChildrenName( sensorVols.get(sensor), "_sp"+ sensor );
			// module length = || DZ |  AL  | DZ |MG| DZ |  AL  | DZ |MG| DZ |  AL  | DZ ||
			sensorVols.get(sensor).setPosition( 0.0, 0.0, (DEADZNLEN + sensor*( ACTIVESENLEN + DEADZNLEN + MICROGAPLEN + DEADZNLEN) - MODULELEN/2.0 + ACTIVESENLEN/2.0)*0.1 );
		}
		
		return moduleVol;
	}
	
	
	/**
	 * Returns one physical sensor, containing active zone and dead zones.
	 * 
	 * @return Geant4Basic a volume positioned at the origin
	 */
	public Geant4Basic createSensorPhysical()
	{		
		Geant4Basic senPhysicalVol = new Geant4Basic( "sensorPhysical", "Box", PHYSSENWID*0.1, SILICONTHK*0.1, PHYSSENWID*0.1 );
		
		Geant4Basic senActiveVol = createSensorActive();
		senActiveVol.setMother( senPhysicalVol );
		
		// physical sensor length = || DZ |  AL  | DZ || // dead zones either side of active zone
		//
		// + - - - - - - - - + - +
		// |           l1        |
		// | - + - - - - - - + - +
		// | w2| active zone | w3|
		// | - + - - - - - - + - +
		// |           l0        |
		// + - + - - - - - - + - +
		//
		// length, width
		List<Geant4Basic> deadZnVols = new ArrayList<>();
		deadZnVols.add( createDeadZone("l") );
		deadZnVols.add( createDeadZone("l") );
		deadZnVols.add( createDeadZone("w") );
		deadZnVols.add( createDeadZone("w") );
		
		for( int deadZn = 0; deadZn < deadZnVols.size(); deadZn++ )
		{
			deadZnVols.get(deadZn).setName( deadZnVols.get(deadZn).getName() + deadZn );
			deadZnVols.get(deadZn).setMother(senPhysicalVol);
			switch( deadZn )
			{
			case 0:
				deadZnVols.get(deadZn).setPosition( -(ACTIVESENWID + DEADZNWID)/2.0*0.1, 0.0, 0.0 );
				break;
			case 1:
				deadZnVols.get(deadZn).setPosition(  (ACTIVESENWID + DEADZNWID)/2.0*0.1, 0.0, 0.0 );
				break;
			case 2:
				deadZnVols.get(deadZn).setPosition( 0.0, 0.0, -(ACTIVESENLEN + DEADZNLEN)/2.0*0.1 );
				break;
			case 3:
				deadZnVols.get(deadZn).setPosition( 0.0, 0.0,  (ACTIVESENLEN + DEADZNLEN)/2.0*0.1 );
				break;
			}
		}
		
		return senPhysicalVol;
	}
	
	
	/**
	 * Returns one active zone of a sensor.
	 * 
	 * @return Geant4Basic a volume positioned at the origin
	 */
	public Geant4Basic createSensorActive( )
	{	
		return new Geant4Basic( "sensorActive", "Box", ACTIVESENWID*0.1, SILICONTHK*0.1, ACTIVESENLEN*0.1 );
	}
	
	
	/**
	 * Returns one dead zone of a sensor.
	 * 
	 * @param aType one of two types of dead zone, extending along the length "l" or width "w" of the sensor
	 * @return Geant4Basic a volume positioned relative to a physical sensor
	 * @throws IllegalArgumentException unknown type
	 */
	public Geant4Basic createDeadZone( String aType ) throws IllegalArgumentException
	{
		Geant4Basic deadZnVol = null;
		
		switch( aType )
		{
		case "l":
			deadZnVol = new Geant4Basic( "deadZone", "Box", DEADZNWID*0.1, SILICONTHK*0.1, (ACTIVESENLEN + 2*DEADZNLEN)*0.1 );
			break;
		case "w":
			deadZnVol = new Geant4Basic( "deadZone", "Box", ACTIVESENWID*0.1, SILICONTHK*0.1, DEADZNLEN*0.1 );
			break;
		default:
			throw new IllegalArgumentException("unknown dead zone type: "+ aType );
		}
		return deadZnVol;
	}
	
	// ===================================================================================================================	
	
	/**
	 * Appends a list of gemcStrings of the given volume and it's children to the given StringBuilder
	 * 
	 * @param aVol a volume
	 * @param aStr a StringBuilder
	 */
	private void _toString( Geant4Basic aVol, StringBuilder aStr )
	{
		aStr.append( aVol.gemcString() );
		aStr.append(System.getProperty("line.separator"));
		for( Geant4Basic childVol : aVol.getChildren() )
			_toString( childVol, aStr ); // recursive
	}
	
	
	/**
	 * Applies the given shift to the position and rotation of the given volume. 
	 * 
	 * @param aVol a volume in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 * @param aScaleT a scale factor for the translation shift
	 * @param aScaleR a scale factor for the rotation shift
	 * @throws IllegalArgumentException incorrect number of elements in shift array
	 */
	private void _applyShift( Geant4Basic aVol, double[] aShift, Point3D aNominalCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
	{
		if( aShift.length != NSHIFTDATARECLEN ){ throw new IllegalArgumentException("shift array must have "+NSHIFTDATARECLEN+" elements"); }
		
		double rx = aShift[3];
		double ry = aShift[4];
		double rz = aShift[5];
		double ra = aShift[6];
		
		//System.out.println( aVol.gemcString() );
		
		ra  = aScaleR*Math.toRadians(ra);
		
		Point3D pos = new Point3D( aVol.getPosition()[0]*10, aVol.getPosition()[1]*10, aVol.getPosition()[2]*10 ); // cm -> mm
		_applyShift( pos, aShift, aNominalCenter, aScaleT, aScaleR );
		aVol.setPosition( pos.x()*0.1, pos.y()*0.1, pos.z()*0.1 );
		
		double[] rot = aVol.getRotation();
		Matrix rotMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -rot[0], -rot[1], -rot[2] ); // Geant = passive/alias, Java = active/alibi
		/*System.out.println("geant matrix "+ Math.toDegrees(-rot[0]) +" "+ Math.toDegrees(-rot[1]) +" "+ Math.toDegrees(-rot[2]) );
		rotMatrix.show();
		
		Vector3D[] vr = new Vector3D[3];
		for( int i = 0; i < vr.length; i++ )
		{
			switch( i )
			{
			case 0:
				vr[i] = new Vector3D( 1, 0, 0 ); break;
			case 1:
				vr[i] = new Vector3D( 0, 1, 0 ); break;
			case 2:
				vr[i] = new Vector3D( 0, 0, 1 ); break;
			}
			System.out.println("vector "+i+" matrix rotated"); Matrix.matMul( rotMatrix, new Matrix(3, 1, Utils.toDoubleArray( vr[i] ) ) ).show();
		}*/
		
		
		// this does not work
		Vector3D vrs = new Vector3D( rx, ry, rz ).asUnit();
		Matrix shiftMatrix = Matrix.convertRotationAxisAngleToMatrix( new double[]{ vrs.x(), vrs.y(), vrs.z(), ra } );
		rotMatrix = Matrix.matMul( shiftMatrix, rotMatrix );
		rot = Matrix.convertRotationToEulerInXYZ_ExZYX( rotMatrix );
		aVol.setRotation("xyz", -rot[0], -rot[1], -rot[2] );
		
		/*rotMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -rot[0], -rot[1], -rot[2] ); // Geant = passive/alias, Java = active/alibi
		System.out.println("geant matrix "+ Math.toDegrees(-rot[0]) +" "+ Math.toDegrees(-rot[1]) +" "+ Math.toDegrees(-rot[2]) );
		rotMatrix.show();
		
		for( int i = 0; i < vr.length; i++ )
		{
			switch( i )
			{
			case 0:
				vr[i] = new Vector3D( 1, 0, 0 ); break;
			case 1:
				vr[i] = new Vector3D( 0, 1, 0 ); break;
			case 2:
				vr[i] = new Vector3D( 0, 0, 1 ); break;
			}
			System.out.println("vector "+i+" matrix rotated"); Matrix.matMul( rotMatrix, new Matrix(3, 1, Utils.toDoubleArray( vr[i] ) ) ).show();
		}*/
		
		/*Vector3D oldVec = new Vector3D( aVol.getPosition()[0], aVol.getPosition()[1], aVol.getPosition()[2] );
		//System.out.println(aVol.gemcString());
		//oldVec.show();
		Vector3D shiftVec = new Vector3D( tx, ty, tz );
		//shiftVec.show();
		shiftVec.scale( 0.1 ); // mm -> cm
		//shiftVec.show();
		Vector3D newVec = oldVec.add( shiftVec );
		//newVec.show();
		aVol.setPosition( newVec.x(), newVec.y(), newVec.z() );
		
		double[] oldRot = aVol.getRotation().clone();
		Matrix oldMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -oldRot[0], -oldRot[1], -oldRot[2] ); // Geant = passive/alias, Java = active/alibi
		Matrix shiftMatrix = Matrix.convertRotationAxisAngleToMatrix( new double[]{ rx, ry, rz, ra } );
		Matrix newMatrix = Matrix.matMul( shiftMatrix, oldMatrix );
		double[] newRot = Matrix.convertRotationToEulerInXYZ_ExZYX( newMatrix );
		aVol.setRotation("xyz", -newRot[0], -newRot[1], -newRot[2] );*/
		
		//System.out.printf("oldRot % 8.3f % 8.3f % 8.3f\n", -Math.toDegrees(oldRot[0]), -Math.toDegrees(oldRot[1]), -Math.toDegrees(oldRot[2]));
	}

	
	/**
	 * Applies the given shift to the given point.
	 * 
	 * @param aPoint a point the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 * @param aScaleT a scale factor for the translation shift
	 * @param aScaleR a scale factor for the rotation shift
	 * @ throws IllegalArgumentException incorrect number of elements in shift array
	 */
	private void _applyShift( Point3D aPoint, double[] aShift, Point3D aNominalCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
	{
		if( aShift.length != NSHIFTDATARECLEN ){ throw new IllegalArgumentException("shift array must have "+NSHIFTDATARECLEN+" elements"); }
		
		double tx = aShift[0]; // The Java language has references but you cannot dereference the memory addresses like you can in C++.
		double ty = aShift[1]; // The Java runtime does have pointers, but they're not accessible to the programmer. (no pointer arithmetic)
		double tz = aShift[2];
		double rx = aShift[3];
		double ry = aShift[4];
		double rz = aShift[5];
		double ra = aShift[6];
		
		//for( int i = 0; i < 3; i++ )
			//aShift[i] = aScaleT/10.0*aShift[i]; // does this also change tx, ty ,tz?
		
		tx *= aScaleT;
		ty *= aScaleT;
		tz *= aScaleT;
		ra  = aScaleR*Math.toRadians(ra);
		
		//System.out.println();
		//System.out.printf("PN: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
		System.out.printf("ST: % 8.3f % 8.3f % 8.3f\n", tx, ty, tz );
		//System.out.printf("SR: % 8.3f % 8.3f % 8.3f % 8.3f\n", rx, ry, rz, Math.toDegrees(ra) );
		//System.out.printf("SC: % 8.3f % 8.3f % 8.3f\n", aNominalCenter.x(), aNominalCenter.y(), aNominalCenter.z() );
				
		if( !(ra < 1E-3) )
		{
			Vector3D centerVec = aNominalCenter.toVector3D();
			
			centerVec.scale( -1 ); // reverse translation
			aPoint.set( aPoint, centerVec ); // move origin to center of rotation axis
			
			//System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
			
			Vector3D vecAxis = new Vector3D( rx, ry, rz ).asUnit();
			System.out.printf("SR: % 8.3f % 8.3f % 8.3f % 8.3f\n", vecAxis.x(), vecAxis.y(), vecAxis.z(), Math.toDegrees(ra) );
			
			// http://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
			PrintStream originalStream = System.out;
			PrintStream dummyStream    = new PrintStream( new OutputStream(){ public void write(int b) {} } );
			
			System.setOut(dummyStream); // suppress unwanted debug output from Vector3D.rotate()
			vecAxis.rotate( aPoint, ra );
			System.setOut(originalStream);
			
			//System.out.printf("PR: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
			
			centerVec.scale( -1 ); // reverse translation
			aPoint.set( aPoint, centerVec );
			
			//System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
		}
		
		Vector3D translationVec = new Vector3D( tx, ty, tz );
		aPoint.set( aPoint, translationVec );
		
		//System.out.printf("PS: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
	}
	
}
