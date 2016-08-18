package SVTFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

import Misc.Util;

public class SVTVolumeFactory
{
	private final Geant4Basic motherVol = new Geant4Basic("svt", "Box", 0,0,0 );
	private final HashMap< String, String > parameters = new LinkedHashMap<>(); // store core parameters from CCDB for GEMC
	private final HashMap< String, String > properties = new LinkedHashMap<>(); // author, email, date
	
	private int regionMin, regionMax, moduleMin, moduleMax, layerMin, layerMax;
	private int[] sectorMin, sectorMax;
	
	private boolean bShift = false; // switch to select whether alignment shifts are applied
	private double scaleT = 1.0, scaleR = 1.0;
	
	public SVTVolumeFactory( ConstantProvider cp, boolean bApplyAlignmentShifts )
	{
		SVTConstants.load( cp );
		setApplyAlignmentShifts( bApplyAlignmentShifts );
		if( bShift == true ){ SVTConstants.loadAlignmentShifts( cp ); }
		
		sectorMin = new int[SVTConstants.NREGIONS];
		sectorMax = new int[SVTConstants.NREGIONS];
		
		// default behaviour
		setRange( 1, SVTConstants.NREGIONS, new int[]{ 1, 1, 1, 1 }, SVTConstants.NSECTORS, 1, SVTConstants.NMODULES ); // all regions, sectors, and modules
		
		Geant4Basic top = new Geant4Basic("none", "Box", 0,0,0 );
		motherVol.setMother( top );
	}
	
	
	/**
	 * Populates the HashMaps with constants.
	 */
	public void putParameters()
	{
		System.out.println("factory: populating HashMap with parameters");
		
		properties.put("unit_length", "mm");
		properties.put("unit_angle", "deg");
		properties.put("author", "P. Davies");
		properties.put("email", "pdavies@jlab.org");
		properties.put("date", "7/15/16");
		
		parameters.put("nregions", Integer.toString(SVTConstants.NREGIONS) );
		for( int r = 0; r < SVTConstants.NREGIONS; r++){ parameters.put("nsectors_r"+(r+1), Integer.toString(SVTConstants.NSECTORS[r]) ); }
		parameters.put("nmodules", Integer.toString(SVTConstants.NMODULES) );
		parameters.put("nsensors", Integer.toString(SVTConstants.NSENSORS) );
		//parameters.put("nstrips", Integer.toString(SVTConstants.NSTRIPS) );
		parameters.put("nlayers", Integer.toString(SVTConstants.NLAYERS) );
		//parameters.put("ntotalsectors", Integer.toString(SVTConstants.NTOTALSECTORS) );
		//parameters.put("readout_pitch", Double.toString(SVTConstants.READOUTPITCH) );
		//parameters.put("silicon_thk", Double.toString(SVTConstants.SILICONTHK) );
		// ...
		
		/*for( Map.Entry< String, String > entry : parameters.entrySet() )
		{
			System.out.println( entry.getKey() +" "+ entry.getValue() );
		}
		System.exit(0);*/
	}
	
	
	/**
	 * Generates the geometry using the current range and stores it in the mother volume. 
	 */
	public void makeVolumes()
	{
		System.out.println("factory: generating geometry with the following parameters");
		
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
			//System.out.println("r "+region);
			Geant4Basic regionVol = createRegion( region );
			//regionVol.setMother( motherVol );
			regionVol.setName( regionVol.getName() + (region+1) );
			Util.appendChildrenName(regionVol, "_r"+ (region+1) );
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
		if( aRegion < 0 || aRegion > SVTConstants.NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		
		Geant4Basic regionVol = new Geant4Basic("region", "Box", 0,0,0 );
		//Geant4Basic regionVolDisplay = new Geant4Basic("region", "tube",  );
		
		// pin regionVol to fiducial origin along z?
		double zStartPhysical = SVTConstants.Z0ACTIVE[aRegion] - SVTConstants.DEADZNLEN; // Cu edge of hybrid sensor's physical volume
		//regionVol.setPosition( 0, 0, (zStartPhysical - FIDORIGINZ)*0.1 ); // central dowel hole
		
		// create faraday cage here?
		
		for( int sector = sectorMin[aRegion]-1; sector < sectorMax[aRegion]; sector++ ) // NSECTORS[region]
		{
			//System.out.println(" s "+sector);
			Geant4Basic sectorVol = createSector();
			//sectorVol.setMother( regionVol );
			sectorVol.setMother( motherVol );
			
			sectorVol.setName( sectorVol.getName() + (sector+1) ); // append name
			Util.appendChildrenName( sectorVol, "_s"+ (sector+1) ); // append tag to end of name (material replacement search fails if it's prepended)
			
			double phi = -2.0*Math.PI/SVTConstants.NSECTORS[aRegion]*sector + SVTConstants.PHI0; // module rotation about target / origin
			double psi = phi - SVTConstants.SECTOR0; // module rotation about centre of geometry
			
			Transformation3D rotatePhi = new Transformation3D();
			rotatePhi.rotateZ( phi );
			
			// mm
			double copperWideThk = 2.880;
			double fiducialRadius = SVTConstants.SUPPORTRADIUS[aRegion] + copperWideThk;
			
			Point3D pos = new Point3D( fiducialRadius, 0.0, zStartPhysical - SVTConstants.FIDORIGINZ ); // put sector in lab frame, not region frame
			rotatePhi.apply( pos );
			sectorVol.setPosition( pos.x()*0.1, pos.y()*0.1, pos.z()*0.1 );
			sectorVol.setRotation("xyz", 0.0, 0.0, -psi ); // change of sign for active/alibi -> passive/alias rotation
			
			//Geant4Basic sectorBall = new Geant4Basic("sectorBall"+sector, "Orb", 0.2 );
			//sectorBall.setMother( sectorVol );
						
			if( bShift )
			{
				//System.out.println("N "+sectorVol.gemcString() );
				Point3D[] fidPos3Ds = SVTAlignmentFactory.getNominalFiducials( aRegion, sector );
				Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
				
				//System.out.println("rs "+ convertRegionSector2SvtIndex( aRegion, sector ));
				double[] shift = SVTConstants.getAlignmentShiftData()[SVTConstants.convertRegionSector2SvtIndex( aRegion, sector )].clone();
				
				Vector3D vec = new Vector3D( shift[3], shift[4], shift[5] ).asUnit();
				vec.scale( (shift[6] == 0) ? 0 : 10 ); // length in mm, but only if non-zero angle
				//vec.show();
				
				Geant4Basic vecVol = Util.createArrow("rotAxis"+sector, vec, 2.0, 1.0, true, true, false );
				vecVol.setPosition( fidTri3D.center().x()*0.1, fidTri3D.center().y()*0.1, fidTri3D.center().z()*0.1 );
				vecVol.setMother( regionVol );
				//System.out.println( vecVol.gemcString() );
				
				int n = 1;
				double d = shift[6]/n;
				for( int i = 1; i < n; i++ )
				{
					//System.out.println( "vol "+ i );
					Geant4Basic stepVol = Util.clone( sectorVol, regionVol );
					//stepVol.setMother( new Geant4Basic("dummy", "Box", 0 ) );
					stepVol.setMother(regionVol);
					Util.appendName( stepVol, "_"+i );
					//Utils.shiftPosition( stepVol, 0, i, 0);
					shift[6] = i*d;
					SVTAlignmentFactory.applyShift( stepVol, shift, fidTri3D.center(), scaleT, scaleR );
					//System.out.println("  "+stepVol.gemcString() );
					//for( int j = 0; j < stepVol.getChildren().size(); j++ )
						//System.out.println( stepVol.getChildren().get(j).gemcString() );
				}
				
				SVTAlignmentFactory.applyShift( sectorVol, SVTConstants.getAlignmentShiftData()[SVTConstants.convertRegionSector2SvtIndex( aRegion, sector )], fidTri3D.center(), scaleT, scaleR );
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
		Geant4Basic sectorVol = new Geant4Basic("sector", "Box", 0,0,0 );
		
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
			Util.appendChildrenName( moduleVols.get(module), "_m"+ (module+1) );
			
			double moduleRadius = 0.0;
			switch( module ) 
			{
			case 0: // U = lower / inner
				moduleRadius = 0.0 - rohacellThickness - passiveThickness - 0.5*SVTConstants.SILICONTHK; // relative to fiducial layer
				break;
			case 1: // V = upper / outer
				moduleRadius = passiveThickness + 0.5*SVTConstants.SILICONTHK;
				break;
			}
			moduleVols.get(module).setPosition( 0.0, moduleRadius*0.1, (SVTConstants.FIDORIGINZ + SVTConstants.MODULELEN/2)*0.1 );
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
		Geant4Basic moduleVol = new Geant4Basic( "module", "Box", SVTConstants.PHYSSENWID*0.1, SVTConstants.SILICONTHK*0.1, SVTConstants.MODULELEN*0.1 );
				
		List<Geant4Basic> sensorVols = new ArrayList<>();
		
		for( int sensor = 0; sensor < SVTConstants.NSENSORS; sensor++ )
		{
			sensorVols.add( createSensorPhysical() ); // includes active and dead zones as children
			sensorVols.get(sensor).setMother( moduleVol );
			sensorVols.get(sensor).setName( sensorVols.get(sensor).getName() + sensor ); // add switch for hybrid, intermediate and far labels?
			Util.appendChildrenName( sensorVols.get(sensor), "_sp"+ (sensor+1) );
			// module length = || DZ |  AL  | DZ |MG| DZ |  AL  | DZ |MG| DZ |  AL  | DZ ||
			sensorVols.get(sensor).setPosition( 0.0, 0.0, (SVTConstants.DEADZNLEN + sensor*( SVTConstants.ACTIVESENLEN + SVTConstants.DEADZNLEN + SVTConstants.MICROGAPLEN + SVTConstants.DEADZNLEN) - SVTConstants.MODULELEN/2.0 + SVTConstants.ACTIVESENLEN/2.0)*0.1 );
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
		Geant4Basic senPhysicalVol = new Geant4Basic( "sensorPhysical", "Box", SVTConstants.PHYSSENWID*0.1, SVTConstants.SILICONTHK*0.1, SVTConstants.PHYSSENWID*0.1 );
		
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
			deadZnVols.get(deadZn).setName( deadZnVols.get(deadZn).getName() + (deadZn+1) );
			deadZnVols.get(deadZn).setMother(senPhysicalVol);
			switch( deadZn )
			{
			case 0:
				deadZnVols.get(deadZn).setPosition( -(SVTConstants.ACTIVESENWID + SVTConstants.DEADZNWID)/2.0*0.1, 0.0, 0.0 );
				break;
			case 1:
				deadZnVols.get(deadZn).setPosition(  (SVTConstants.ACTIVESENWID + SVTConstants.DEADZNWID)/2.0*0.1, 0.0, 0.0 );
				break;
			case 2:
				deadZnVols.get(deadZn).setPosition( 0.0, 0.0, -(SVTConstants.ACTIVESENLEN + SVTConstants.DEADZNLEN)/2.0*0.1 );
				break;
			case 3:
				deadZnVols.get(deadZn).setPosition( 0.0, 0.0,  (SVTConstants.ACTIVESENLEN + SVTConstants.DEADZNLEN)/2.0*0.1 );
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
		return new Geant4Basic( "sensorActive", "Box", SVTConstants.ACTIVESENWID*0.1, SVTConstants.SILICONTHK*0.1, SVTConstants.ACTIVESENLEN*0.1 );
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
			deadZnVol = new Geant4Basic( "deadZone", "Box", SVTConstants.DEADZNWID*0.1, SVTConstants.SILICONTHK*0.1, (SVTConstants.ACTIVESENLEN + 2*SVTConstants.DEADZNLEN)*0.1 );
			break;
		case "w":
			deadZnVol = new Geant4Basic( "deadZone", "Box", SVTConstants.ACTIVESENWID*0.1, SVTConstants.SILICONTHK*0.1, SVTConstants.DEADZNLEN*0.1 );
			break;
		default:
			throw new IllegalArgumentException("unknown dead zone type: "+ aType );
		}
		return deadZnVol;
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
	 * Appends a tag to the current volumes.
	 * Useful to avoid conflicts in a GDML file.
	 * 
	 * @param aTag something to add
	 */
	public void appendName( String aTag )
	{
		Util.appendName( motherVol, aTag );
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
	 * Sets whether alignment shifts from CCDB should be applied to the geometry during generation.
	 * 
	 * @param boolean true/false
	 */
	public void setApplyAlignmentShifts( boolean b )
	{
		bShift = b;
	}
	
	
	/**
	 * Returns whether alignment shifts are applied.
	 * 
	 * @return boolean true/false
	 */
	public boolean isSetApplyAlignmentShifts()
	{
		return bShift;
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
		if( aLayerMin < 0 || aLayerMax > SVTConstants.NLAYERS ){ throw new IllegalArgumentException("layer out of bounds"); }
		if( aSectorMin.length != SVTConstants.NREGIONS || aSectorMax.length != SVTConstants.NREGIONS ){ throw new IllegalArgumentException("invalid sector array"); }
		if( aLayerMin > aLayerMax ){ throw new IllegalArgumentException("invalid layer min/max"); }
		
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
		{
			if( aSectorMin[i] < 0 || aSectorMax[i] > SVTConstants.NSECTORS[i] )
				throw new IllegalArgumentException("sector out of bounds");
			if( aSectorMin[i] > aSectorMax[i] )
				throw new IllegalArgumentException("invalid sector min/max");
		}
		
		// 0 means use default / previous value
		if( aLayerMin != 0 )
		{
			layerMin = aLayerMin;
			regionMin = SVTConstants.convertLayer2RegionModule( aLayerMin )[0]+1;
			moduleMin = SVTConstants.convertLayer2RegionModule( aLayerMin )[1]+1;
		}
		if( aLayerMax != 0 )
		{
			layerMax = aLayerMax;
			regionMax = SVTConstants.convertLayer2RegionModule( aLayerMax )[0]+1;
			moduleMax = SVTConstants.convertLayer2RegionModule( aLayerMax )[1]+1;
		}
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
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
		if( aRegionMin < 0 || aRegionMax > SVTConstants.NREGIONS ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSectorMin.length != SVTConstants.NREGIONS || aSectorMax.length != SVTConstants.NREGIONS ){ throw new IllegalArgumentException("invalid sector array"); }
		if( aRegionMin > aRegionMax ){ throw new IllegalArgumentException("invalid region min/max"); }
		
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
		{
			if( aSectorMin[i] < 0 || aSectorMax[i] > SVTConstants.NSECTORS[i] )
				throw new IllegalArgumentException("sector out of bounds");
			if( aSectorMin[i] > aSectorMax[i] )
				throw new IllegalArgumentException("invalid sector min/max");
		}
		if( aModuleMin < 0 || aModuleMax > SVTConstants.NMODULES )
			throw new IllegalArgumentException("module out of bounds");
		
		// 0 means use default / previous value
		if( aRegionMin != 0 ){ regionMin = aRegionMin; }
		if( aRegionMax != 0 ){ regionMax = aRegionMax; }
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
		{
			if( aSectorMin[i] != 0 ){ sectorMin[i] = aSectorMin[i]; }
			if( aSectorMax[i] != 0 ){ sectorMax[i] = aSectorMax[i]; }
		}
		if( aModuleMin != 0 ){ moduleMin = aModuleMin; }
		if( aModuleMax != 0 ){ moduleMax = aModuleMax; }
		if( aRegionMin != 0 || aModuleMin != 0 ){ layerMin = SVTConstants.convertRegionModule2Layer( regionMin-1, moduleMin-1 )+1; }
		if( aRegionMax != 0 || aModuleMax != 0 ){ layerMax = SVTConstants.convertRegionModule2Layer( regionMax-1, moduleMax-1 )+1; }
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
		if( aRegion < 0 || aRegion > SVTConstants.NREGIONS ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSectorMin < 0 || aSectorMin > SVTConstants.NSECTORS[aRegion] ){ throw new IllegalArgumentException("sectorMin out of bounds"); }
		if( aSectorMax < 0 || aSectorMax > SVTConstants.NSECTORS[aRegion] ){ throw new IllegalArgumentException("sectorMax out of bounds"); }
		if( aSectorMin > aSectorMax ){ throw new IllegalArgumentException("invalid sector min/max"); }
		
		// 0 means use default / previous value
		if( aRegion != 0 ){ regionMin = aRegion; regionMax = aRegion; }
		if( aSectorMin != 0 ){ sectorMin[aRegion-1] = aSectorMin; }
		if( aSectorMax != 0 ){ sectorMax[aRegion-1] = aSectorMax; }
		if( aRegion != 0 ){ layerMin = SVTConstants.convertRegionModule2Layer( regionMin-1, moduleMin-1 )+1;
						    layerMax = SVTConstants.convertRegionModule2Layer( regionMax-1, moduleMax-1 )+1; }
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
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
			range = range +"["+sectorMin[i]+":"+sectorMax[i]+"]";
		return range;
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
}
