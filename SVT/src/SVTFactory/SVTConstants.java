package SVTFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Transformation3D;

import Misc.Util;

public class SVTConstants
{
	private static String ccdbPath = "/geometry/cvt/svt/";
	private static boolean bLoadedConstants = false; // only load constants once
	
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
	public static int NTOTALSECTORS; // total number of sectors for all regions
	public static int NTOTALFIDUCIALS; // total number of fiducials for all sectors and regions
	public static double[][] LAYERRADIUS; // radius to strip planes
	public static double LAYERGAPTHK; // distance between pairs of layers within a sector
	public static double MODULELEN;    // || DZ |  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  || DZ ||
	public static double STRIPLENMAX;  //      ||  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  ||
	public static double MODULEWID; // || DZ | AZ | DZ || (along width)
	
	// data for alignment shifts
	public static int NSHIFTDATARECLEN = 7;
	private static double[][] SHIFTDATA = null;
	private static String filenameShiftSurvey = null;
	
	
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
	 * Reads all the necessary constants from CCDB into static variables.
	 * Please use a DatabaseConstantProvider to access CCDB and load the following tables:
	 * svt, region, support, fiducial, material.
	 *  
	 * @param cp a ConstantProvider
	 */
	public static void load( ConstantProvider cp )
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
			
			SILICONTHK = cp.getDouble( ccdbPath+"svt/siliconThk", 0 );
			PHYSSENLEN = cp.getDouble( ccdbPath+"svt/physSenLen", 0 );
			PHYSSENWID = cp.getDouble( ccdbPath+"svt/physSenWid", 0 );
			ACTIVESENLEN = cp.getDouble( ccdbPath+"svt/activeSenLen", 0 );
			ACTIVESENWID = cp.getDouble( ccdbPath+"svt/activeSenWid", 0 );
			DEADZNLEN = cp.getDouble( ccdbPath+"svt/deadZnLen", 0 );
			DEADZNWID = cp.getDouble( ccdbPath+"svt/deadZnWid", 0 );
			MICROGAPLEN = cp.getDouble( ccdbPath+"svt/microGapLen", 0 );
			
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
			
			NTOTALSECTORS = convertRegionSector2SvtIndex( NREGIONS-1, NSECTORS[NREGIONS-1]-1 )+1;
			NTOTALFIDUCIALS = convertRegionSectorFid2SurveyIndex(NREGIONS-1, NSECTORS[NREGIONS-1]-1, NFIDUCIALS-1  )+1;
			
			// check one constant from each table
			//if( NREGIONS == 0 || NSECTORS[0] == 0 || FIDCUX == 0 || MATERIALS[0][0] == 0 || SUPPORTRADIUS[0] == 0 )
				//throw new NullPointerException("please load the following tables from CCDB in "+ccdbPath+"\n svt\n region\n support\n fiducial\n material\n");
			
			bLoadedConstants = true;
		}
	}
	
	
	/**
	 * Reads alignment data from CCDB.
	 * 
	 * @param cp a DatabaseConstantProvider that has loaded the "alignment" table
	 */
	public static void loadAlignmentShifts( ConstantProvider cp )
	{
		System.out.println("reading fiducial survey data from database");
		
		boolean verbose = false;
		
		SHIFTDATA = new double[NTOTALSECTORS][];
		
		for( int i = 0; i < NTOTALSECTORS; i++ )
		{
			double tx = cp.getDouble(getCcdbPath()+"alignment/tx", i );
			double ty = cp.getDouble(getCcdbPath()+"alignment/ty", i );
			double tz = cp.getDouble(getCcdbPath()+"alignment/tz", i );
			double rx = cp.getDouble(getCcdbPath()+"alignment/rx", i );
			double ry = cp.getDouble(getCcdbPath()+"alignment/ry", i );
			double rz = cp.getDouble(getCcdbPath()+"alignment/rz", i );
			double ra = cp.getDouble(getCcdbPath()+"alignment/ra", i );
			SHIFTDATA[i] = new double[]{ tx, ty, tz, rx, ry, rz, Math.toRadians(ra) };
			if( verbose ) System.out.printf("%2d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n", i+1, tx, ty, tz, rx, ry, rz, ra );
		}
	}
	
	
	/**
	 * Reads alignment data from the given file.
	 * 
	 * @param aFilename a filename
	 */
	public static void loadAlignmentShifts( String aFilename )
	{
		filenameShiftSurvey = aFilename;			
		try{ SHIFTDATA = Util.inputTaggedData( filenameShiftSurvey, NSHIFTDATARECLEN ); } // 3 translation(x,y,z), 4 rotation(x,y,z,a)
		catch( Exception e ){ e.printStackTrace(); System.exit(-1); } // trigger fatal error
		if( SHIFTDATA == null ){ System.err.println("stop: SHIFTDATA is null after reading file \""+filenameShiftSurvey+"\""); System.exit(-1); }
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
	 * Converts linear index to Region, Sector, and Fiducial indices.
	 * For use with data files.
	 * 
	 * @param aSurveyIndex an index used for fiducial survey data
	 * @return int[] an array containing RSF indices
	 * @throws IllegalArgumentException index out of bounds
	 */
	public static int[] convertSurveyIndex2RegionSectorFiducial( int aSurveyIndex ) throws IllegalArgumentException
	{
		if( aSurveyIndex < 0 || aSurveyIndex > NTOTALSECTORS*NFIDUCIALS-1 ){ throw new IllegalArgumentException("survey index out of bounds"); }
		int region = -1, sector = -1, fiducial = -1;
		for( int i = 0; i < NREGIONS; i++ )
		{
			int l0 = Util.subArraySum( NSECTORS, i   )*NFIDUCIALS;
			int l1 = Util.subArraySum( NSECTORS, i+1 )*NFIDUCIALS;
			if( i == NREGIONS-1 ){ l1 = l0 + NSECTORS[NREGIONS-1]*NFIDUCIALS; }
			if( l0 <= aSurveyIndex && aSurveyIndex <= l1-1 ){ region = i; break; }
		}		
		sector = aSurveyIndex / NFIDUCIALS - Util.subArraySum( NSECTORS, region );
		fiducial = aSurveyIndex % NFIDUCIALS;
		return new int[]{ region, sector, fiducial };
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
		return Util.subArraySum( NSECTORS, aRegion ) + aSector;
	}
	
	
	/**
	 * Converts linear index to Region and Sector indices.
	 * For use with data files.
	 * 
	 * @param aSvtIndex
	 * @return int[] an array containing RS indices
	 * @throws IllegalArgumentException index out of bounds
	 */
	public static int[] convertSvtIndex2RegionSector( int aSvtIndex )
	{
		if( aSvtIndex < 0 || aSvtIndex > NTOTALSECTORS-1 ){ throw new IllegalArgumentException("svt index out of bounds"); }
		
		int region = -1, sector = -1;
		for( int i = 0; i < NREGIONS; i++ )
		{
			int l0 = Util.subArraySum( NSECTORS, i   );
			int l1 = Util.subArraySum( NSECTORS, i+1 );
			if( i == NREGIONS-1 ){ l1 = l0 + NSECTORS[NREGIONS-1]; }
			if( l0 <= aSvtIndex && aSvtIndex <= l1-1 ){ region = i; break; }
		}
		sector = aSvtIndex - Util.subArraySum( NSECTORS, region );
		return new int[]{ region, sector };
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
	 * Returns a transformation from the local frame to the lab frame, for the given parameters of a sector module.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aRadius transverse distance from beamline
	 * @param aZ longitudinal distance along beamline
	 * @return Transformation3D a sequence of transformations
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static Transformation3D getLabFrame( int aRegion, int aSector, double aRadius, double aZ ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		
		double phi = -2.0*Math.PI/NSECTORS[aRegion]*aSector + PHI0; // location around target
		Transformation3D labFrame = new Transformation3D();
		labFrame.rotateZ( -SECTOR0 ).translateXYZ( aRadius, 0, aZ ).rotateZ( phi );
		return labFrame;
	}
	
	
	/**
	 * Returns a transformation for the strip frame to the local frame. 
	 * 
	 * @param aFlip whether the transformation should append a rotation of 180 deg about the z axis
	 * @return Transformation3D a sequence of transformations
	 */
	public static Transformation3D getStripFrame( boolean aFlip )
	{
		Transformation3D stripFrame = new Transformation3D();
		stripFrame.translateXYZ( -SVTConstants.ACTIVESENWID/2, 0, 0 ); // move to centre along x
		if( aFlip ) { stripFrame.rotateZ( Math.toRadians(180) ); } // flip for U layer
		return stripFrame;
	}
	
	
	/**
	 * Returns the alignment data.
	 * 
	 * @return double[][] an array of translations and axis-angle rotations of the form { tx, ty, tz, rx, ry, rz, ra }
	 */
	public static double[][] getAlignmentShiftData()
	{
		if( SHIFTDATA == null ){ System.err.println("stop: SHIFTDATA requested is null"); System.exit(-1); }
		return SHIFTDATA;
	}
}
