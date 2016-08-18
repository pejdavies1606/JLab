package SVTFactory;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

import Alignment.AlignmentFactory;

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
 * @version 0.1.0
 */
public class SVTStripFactory
{
	private boolean bShift = false; // switch to select whether alignment shifts are applied
	private double scaleT = 1.0, scaleR = 1.0;
	
	/**
	 * Constructs a new geometry factory for sensor strips.
	 * Please run {@code SVTConstants.connect() } first.
	 * 
	 * @param cp a DatabaseConstantProvider that has loaded the necessary tables
	 * @param applyAlignmentShifts a switch to set whether the alignment shifts from CCDB will be applied
	 */
	public SVTStripFactory( ConstantProvider cp, boolean applyAlignmentShifts )
	{
		SVTConstants.load( cp );
		if( applyAlignmentShifts ){ bShift = true; SVTConstants.loadAlignmentShifts( cp ); }
	}
	
	/**
	 * Constructs a new geometry factory for sensor strips.
	 * Please run {@code SVTConstants.connect() } first.
	 * 
	 * @param cp a DatabaseConstantProvider that has loaded the necessary tables
	 * @param filenameAlignmentShifts a file containing the alignment shifts to be applied
	 */
	public SVTStripFactory( ConstantProvider cp, String filenameAlignmentShifts )
	{
		SVTConstants.load( cp );
		bShift = true;
		SVTConstants.loadAlignmentShifts( filenameAlignmentShifts );
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
		if( aLayer < 0 || aLayer > SVTConstants.NLAYERS-1 ){ throw new IllegalArgumentException("layer out of bounds"); }
		int[] rm = SVTConstants.convertLayer2RegionModule( aLayer );
		if( aSector < 0 || aSector > SVTConstants.NSECTORS[rm[0]]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		if( aStrip < 0 || aStrip > SVTConstants.NSTRIPS-1 ){ throw new IllegalArgumentException("strip out of bounds"); }
		return getNominalStrip( rm[0], aSector, rm[1], aStrip );
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
	public Line3D getNominalStrip( int aRegion, int aSector, int aModule, int aStrip ) throws IllegalArgumentException // lab frame
	{
		//System.out.println("r "+ aRegion +" s "+ aSector +" m "+ aModule );
		if( aRegion < 0 || aRegion > SVTConstants.NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > SVTConstants.NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		if( aModule < 0 || aModule > SVTConstants.NMODULES-1 ){ throw new IllegalArgumentException("module out of bounds"); }
		
		//System.out.printf("%d %d %d %d\n", aRegion, aSector, aModule, aStrip);
		
		Line3D stripLine = createNominalStrip( aStrip, aModule ); // strip end points are returned relative to the front edge along z, and the centre along x 	
		double r = SVTConstants.LAYERRADIUS[aRegion][aModule];
		double z = SVTConstants.Z0ACTIVE[aRegion];
		SVTConstants.getLabFrame( aRegion, aSector, r, z ).apply( stripLine ); // local frame -> lab frame
		
		return stripLine;
	}
	
	
	/**
	 * Returns a sensor strip before any alignment shifts been applied.
	 * 
	 * @param aStrip an index starting from 0
	 * @param aModule an index starting from 0
	 * @return Line3D a strip in the local frame
	 * @throws IllegalArgumentException index out of bounds
	 */
	public Line3D createNominalStrip( int aStrip, int aModule ) throws IllegalArgumentException // local frame
	{
		if( aStrip < 0 || aStrip > SVTConstants.NSTRIPS-1 ){ throw new IllegalArgumentException("strip out of bounds"); }
		if( aModule < 0 || aModule > SVTConstants.NMODULES-1 ){ throw new IllegalArgumentException("module out of bounds"); }
		
		//     ACTIVESENWID
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
		// (0,0)															  ACTIVESENLEN
		
		// there are 513 strips in total starting on the hybrid side of the sensor module, but only 256 are sensitive / used in the readout
		// the readout pitch is the distance between sensitive strips along the hybrid side
		
		// STRIPWID = ACTIVESENWID/(2*NSTRIPS + 1);
		// MODULELEN = NSENSORS*(ACTIVESENLEN + 2*DEADZNLEN) + (NSENSORS - 1)*MICROGAPLEN;
		// STRIPLENMAX = MODULELEN - 2*DEADZNLEN;
		
		double w, a, q, x0, z0, x1, z1;
		//double r = 0;
		
		// STRIPOFFSETWID = offset of first *intermediate* sensor strip from edge of active zone
		// 0.5*SVTConstants.READOUTPITCH = distance bewteen adjacent intermediate and readout strips
		w = SVTConstants.STRIPOFFSETWID + 0.5*SVTConstants.READOUTPITCH + aStrip*SVTConstants.READOUTPITCH;
		a = SVTConstants.STEREOANGLE/SVTConstants.NSTRIPS*aStrip;
		q = SVTConstants.STRIPLENMAX*Math.tan(a);
		
		x0 = SVTConstants.ACTIVESENWID - w;
		z0 = 0;
		x1 = 0;
		z1 = 0;
		
		if( q < x0 )
		{
			x1 = x0 - q;
			z1 = SVTConstants.STRIPLENMAX;
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
		SVTConstants.getStripFrame( aModule == 0 ).apply( stripLine ); // strip frame -> local frame
		
		return stripLine; // strip end points are returned relative to the front edge along z, and the centre along x
	}
	
	/**
	 * Returns a sensor strip after the alignment shifts been applied.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aStrip an index starting from 0
	 * @param aModule an index starting from 0
	 * @return Line3D a strip in the lab frame
	 */
	public Line3D getShiftedStrip( int aRegion, int aSector, int aStrip, int aModule )
	{
		Line3D stripLine = getNominalStrip( aRegion, aSector, aStrip, aModule );
		AlignmentFactory.applyShift( stripLine.origin(), SVTConstants.getAlignmentShiftData()[SVTConstants.convertRegionSector2SvtIndex( aRegion, aSector )], SVTAlignmentFactory.getNominalFiducialCenter( aRegion, aSector ), scaleT, scaleR );
		AlignmentFactory.applyShift( stripLine.end(),    SVTConstants.getAlignmentShiftData()[SVTConstants.convertRegionSector2SvtIndex( aRegion, aSector )], SVTAlignmentFactory.getNominalFiducialCenter( aRegion, aSector ), scaleT, scaleR );
		return stripLine;
	}
	
	/**
	 * Returns a sensor strip after the alignment shifts been applied.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aStrip an index starting from 0
	 * @param aModule an index starting from 0
	 * @return Line3D a strip in the local frame
	 */
	public Line3D createShiftedStrip( int aRegion, int aSector, int aStrip, int aModule )
	{
		Line3D stripLine = getShiftedStrip( aRegion, aSector, aStrip, aModule );
		
		double r = SVTConstants.LAYERRADIUS[aRegion][aModule];
		double z = SVTConstants.Z0ACTIVE[aRegion];
		SVTConstants.getLabFrame( aRegion, aSector, r, z ).inverse().apply( stripLine ); // lab frame -> local frame
		
		return stripLine;
	}
	
	/**
	 * Returns the corners of a sensor layer in the local frame.
	 *  
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aModule an index starting from 0
	 * @return Point3D[] array of corners in order ( origin, max width, max width and max length, max length )
	 */
	public Point3D[] getLayerCorners( int aRegion, int aSector, int aModule )
	{
		Point3D[] cornerPos3Ds = new Point3D[4];
		cornerPos3Ds[0] = new Point3D( 0, 0, 0 );
		cornerPos3Ds[1] = new Point3D( SVTConstants.ACTIVESENWID, 0, 0 );
		cornerPos3Ds[2] = new Point3D( SVTConstants.ACTIVESENWID, 0, SVTConstants.STRIPLENMAX );
		cornerPos3Ds[3] = new Point3D( 0, 0, SVTConstants.STRIPLENMAX );
		
		Transformation3D stripFrame = SVTConstants.getStripFrame( aModule == 0 );
		for( int i = 0; i < 4; i++ ){ stripFrame.apply( cornerPos3Ds[i] ); } // strip frame -> local frame
		
		return cornerPos3Ds;
	}
	
	
	/**
	 * Sets whether alignment shifts from CCDB should be applied to the geometry during generation.
	 * 
	 * @param b true/false
	 */
	public void setAlignmentShifts( boolean b )
	{
		bShift = b;
	}
	
	
	/**
	 * Returns whether alignment shifts are applied.
	 * 
	 * @return boolean true/false
	 */
	public boolean isSetAlignmentShifts()
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
}
