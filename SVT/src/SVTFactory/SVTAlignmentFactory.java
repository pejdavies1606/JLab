package SVTFactory;

import java.io.IOException;
import java.io.Writer;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;

import Alignment.AlignmentFactory;
import Misc.Util;

/**
 * <h1> Geometry for the SVT </h1>
 * 
 * Processes fiducial survey data into alignment shifts, and applies those shifts to a given point or volume.
 * 
 * @author pdavies
 * @version 0.1.0
 */
public class SVTAlignmentFactory
{
	private static String filenameSurveyIdeals;
	private static String filenameSurveyMeasured;
	
	//private static String filenameDistances = "measured_distances.dat";
	//private static Writer outputDistances;
	
	//private static String filenameIdealsFiducials = "survey_ideals_reformat2.dat";
	//private static Writer outputIdealsFiducials;
	
	//private static String filenameMeasuredFiducials = "survey_measured_reformat2.dat";
	//private static Writer outputMeasuredFiducials;
	
	private static double[][] dataSurveyIdeals, dataSurveyMeasured;
	
	
	public static void setup( ConstantProvider cp, String aInputSurveyIdeals, String aInputSurveyMeasured )
	{
		SVTConstants.load( cp );
		filenameSurveyIdeals = aInputSurveyIdeals;
		filenameSurveyMeasured = aInputSurveyMeasured;
		
		try
		{
			dataSurveyIdeals = Util.inputTaggedData( filenameSurveyIdeals, 3 ); // RSF (X Y Z)
			dataSurveyMeasured = Util.inputTaggedData( filenameSurveyMeasured, 3 ); // RSF (X Y Z)
		}
		catch( IOException e ){ e.printStackTrace(); }
		
		if( dataSurveyIdeals == null || dataSurveyMeasured == null )
			throw new IllegalArgumentException("no data");
	}

	
	
	
	public static void calcShifts( double[][] aDataNominal, double[][] aDataMeasured, String aOutputShifts )
	{
		String outputLine; Writer outputShifts;
		double[][] dataShifts = AlignmentFactory.calcShifts( SVTConstants.NTOTALSECTORS, aDataNominal, aDataMeasured );
		
		outputShifts = Util.openOutputDataFile( aOutputShifts );
		//outputDistances = Util.openOutputDataFile( filenameDistances );
		//outputIdealsFiducials = Util.openOutputDataFile( filenameIdealsFiducials );
		//outputMeasuredFiducials = Util.openOutputDataFile( filenameMeasuredFiducials );
		
		/*double fidXDist = 2*SVTConstants.FIDCUX;
		double fidZDist = SVTConstants.FIDCUZ + SVTConstants.FIDPKZ0 + SVTConstants.FIDPKZ1;
		double fidZDist0 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTConstants.FIDCUX + SVTConstants.FIDPKX, 2) );
		double fidZDist1 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTConstants.FIDCUX - SVTConstants.FIDPKX, 2) );
		
		System.out.printf("fidXDist  %8.3f\n", fidXDist );
		System.out.printf("fidZDist  %8.3f\n", fidZDist );
		System.out.printf("fidZDist0 %8.3f\n", fidZDist0 );
		System.out.printf("fidZDist1 %8.3f\n", fidZDist1 );*/
		
		for( int l = 0; l < SVTConstants.NTOTALSECTORS; l++ )
		{
			/*int nf = SVTConstants.NFIDUCIALS;
			Point3D[] fidNominalPos3Ds = new Point3D[nf];
			Point3D[] fidMeasuredPos3Ds = new Point3D[nf];
			
			for( int f = 0; f < nf; f++ )
			{
				fidNominalPos3Ds[f] = new Point3D( aDataNominal[l*nf+f][0], aDataNominal[l*nf+f][1], aDataNominal[l*nf+f][2] );
				fidMeasuredPos3Ds[f] = new Point3D( aDataMeasured[l*nf+f][0], aDataMeasured[l*nf+f][1], aDataMeasured[l*nf+f][2] );
			}
			
			double dCuNominal = fidNominalPos3Ds[1].distance(fidNominalPos3Ds[0]);
			double dPk0Nominal = fidNominalPos3Ds[2].distance(fidNominalPos3Ds[0]);
			double dPk1Nominal = fidNominalPos3Ds[2].distance(fidNominalPos3Ds[1]);
			
			double dCuMeasured = fidMeasuredPos3Ds[1].distance(fidMeasuredPos3Ds[0]);
			double dPk0Measured = fidMeasuredPos3Ds[2].distance(fidMeasuredPos3Ds[0]);
			double dPk1Measured = fidMeasuredPos3Ds[2].distance(fidMeasuredPos3Ds[1]);
			
			System.out.printf("NF  % 8.3f % 8.3f % 8.3f", dCuNominal, dPk0Nominal, dPk1Nominal );
			System.out.printf("    MF  % 8.3f % 8.3f % 8.3f", dCuMeasured, dPk0Measured, dPk1Measured );
			System.out.println();*/

			//double ucMeasured = 0.030; // 30 um
			//double DdCu = Util.calcUncertaintyDistance( ucMeasured , fidMeasuredPos3Ds[1], fidMeasuredPos3Ds[0] );
			//double DdPk0 = Util.calcUncertaintyDistance( ucMeasured , fidMeasuredPos3Ds[2], fidMeasuredPos3Ds[0] );
			//double DdPk1 = Util.calcUncertaintyDistance( ucMeasured , fidMeasuredPos3Ds[2], fidMeasuredPos3Ds[1] );
			//outputLine = String.format("R%dS%02d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, dCu, dCu - fidXDist, DdCu, dPk0, dPk0 - fidZDist0, DdPk0, dPk1, dPk1 - fidZDist1, DdPk1 );
			//Util.outputLine( outputDistances, outputLine );

			int[] rs = SVTConstants.convertSvtIndex2RegionSector( l );
			String fmt = "R%dS%02d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n";
			//String fmt = "R%dS%02d % 11.6f % 11.6f % 11.6f % 11.6f % 11.6f % 11.6f % 11.6f\n";
			outputLine = String.format(fmt, rs[0]+1, rs[1]+1,
					dataShifts[l][0], dataShifts[l][1], dataShifts[l][2], dataShifts[l][3], dataShifts[l][4], dataShifts[l][5], Math.toDegrees(dataShifts[l][6]) );
			//outputLine = String.format("R%dS%02d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 );
			Util.writeLine( outputShifts, outputLine );
			//System.out.print(outputLine);
		}
		Util.closeOutputDataFile( aOutputShifts, outputShifts );
		//Util.closeOutputDataFile( filenameDistances, outputDistances );
		//Util.closeOutputDataFile( filenameIdealsFiducials, outputIdealsFiducials );
		//Util.closeOutputDataFile( filenameMeasuredFiducials, outputMeasuredFiducials );
	}
	
	
	
	public static void calcDeltas( double[][] aDataNominal, double[][] aDataMeasured, String aOutputDeltas )
	{
		String outputLine; Writer outputDeltas;
		double[][] dataDeltasMeasuredFromNominal = AlignmentFactory.calcDeltas( SVTConstants.NTOTALSECTORS*SVTConstants.NFIDUCIALS, 3, aDataNominal, aDataMeasured );

		outputDeltas = Util.openOutputDataFile( aOutputDeltas );

		for( int k = 0; k < SVTConstants.NTOTALSECTORS*SVTConstants.NFIDUCIALS; k++ )
		{
			int[] rsf = SVTConstants.convertSurveyIndex2RegionSectorFiducial( k );
			outputLine = String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", rsf[0]+1, rsf[1]+1, rsf[2]+1,
					dataDeltasMeasuredFromNominal[k][0], dataDeltasMeasuredFromNominal[k][1], dataDeltasMeasuredFromNominal[k][2] );
			Util.writeLine( outputDeltas, outputLine );
			//System.out.print(outputLine);
		}
		Util.closeOutputDataFile( aOutputDeltas, outputDeltas );
	}
	
	
	/**
	 * Applies the given alignment shift to the given point.
	 * 
	 * @param aPos a volume in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 */
	public static void applyShift( Point3D aPos, double[] aShift, Point3D aNominalCenter )
	{
		AlignmentFactory.applyShift( aPos, aShift, aNominalCenter, 1.0, 1.0 );
	}
	
	
	/**
	 * Applies the given alignment shift to the given volume.
	 * 
	 * @param aVol a volume in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 */
	public static void applyShift( Geant4Basic aVol, double[] aShift, Point3D aNominalCenter )
	{
		AlignmentFactory.applyShift( aVol, aShift, aNominalCenter, 1.0, 1.0 );
	}
	
	
	/**
	 * Returns locations of shifted fiducial points.
	 * 
	 * @return double[][] an array of data in fiducial survey format.
	 */
	public double[][] getShiftedFiducialData()
	{
		double [][] data = new double[SVTConstants.NTOTALSECTORS*SVTConstants.NFIDUCIALS][];
		for( int region = 0; region < SVTConstants.NREGIONS; region++ )
			for( int sector = 0; sector < SVTConstants.NSECTORS[region]; sector++ )
			{
				Point3D fidPos3Ds[] = getShiftedFiducials( region, sector );
				for( int fid = 0; fid < SVTConstants.NFIDUCIALS; fid++ )
					data[SVTConstants.convertRegionSectorFid2SurveyIndex( region, sector, fid )] = new double[]{ fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() };
			}
		return data;
	}
	
	
	/**
	 * Returns locations of nominal fiducial points.
	 * 
	 * @return double[][] an array of data in fiducial survey format.
	 */
	public static double[][] getNominalFiducialData()
	{
		double [][] data = new double[SVTConstants.NTOTALSECTORS*SVTConstants.NFIDUCIALS][];
		for( int region = 0; region < SVTConstants.NREGIONS; region++ )
			for( int sector = 0; sector < SVTConstants.NSECTORS[region]; sector++ )
			{
				Point3D fidPos3Ds[] = getNominalFiducials( region, sector );
				for( int fid = 0; fid < SVTConstants.NFIDUCIALS; fid++ )
				{
					data[SVTConstants.convertRegionSectorFid2SurveyIndex( region, sector, fid )] 
							= new double[]{ fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() };
				}
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
		if( aRegion < 0 || aRegion > SVTConstants.NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > SVTConstants.NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		
		Point3D[] fidPos3Ds = getNominalFiducials( aRegion, aSector ); // lab frame
		Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
		
		for( int f = 0; f < SVTConstants.NFIDUCIALS; f++ )
			applyShift( fidPos3Ds[f], SVTConstants.getAlignmentShiftData()[SVTConstants.convertRegionSector2SvtIndex( aRegion, aSector )], fidTri3D.center() );
		
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
		if( aRegion < 0 || aRegion > SVTConstants.NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > SVTConstants.NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		
		Point3D[] fidPos3Ds = new Point3D[] { createFiducial(0), createFiducial(1), createFiducial(2) }; // relative to fiducial origin
		
		double fidOriginZ = SVTConstants.Z0ACTIVE[aRegion] - SVTConstants.DEADZNLEN - SVTConstants.FIDORIGINZ;
		double copperWideThk = 2.880;
		double radius = SVTConstants.SUPPORTRADIUS[aRegion] + copperWideThk;
		
		Transformation3D labFrame = SVTConstants.getLabFrame( aRegion, aSector, radius, fidOriginZ );
		
		for( int f = 0; f < SVTConstants.NFIDUCIALS; f++ )
			labFrame.apply( fidPos3Ds[f] );
		
		return fidPos3Ds;
	}
	
	/**
	 * Returns the fiducial center for a sector module before any alignment shifts been applied.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @return Point3D the mean average point of the 3 fiducial points (Cu+, Cu-, Pk)
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static Point3D getNominalFiducialCenter( int aRegion, int aSector ) throws IllegalArgumentException
	{
		Point3D[] fidPos3Ds = getNominalFiducials( aRegion, aSector );
		return new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] ).center();
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
		if( aFid < 0 || aFid > SVTConstants.NFIDUCIALS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		
		Point3D fidPos = new Point3D();
		
		switch( aFid )
		{
		case 0: // Cu +
			fidPos.set( SVTConstants.FIDCUX, 0.0, -SVTConstants.FIDCUZ );
			break;
		case 1: // Cu -
			fidPos.set( -SVTConstants.FIDCUX, 0.0, -SVTConstants.FIDCUZ );
			break;
		case 2: // Pk
			fidPos.set( -SVTConstants.FIDPKX, 0.0, SVTConstants.FIDPKZ0 + SVTConstants.FIDPKZ1 );
			break;
		}
		return fidPos;
	}
	
	
	
	public static double[][] getDataSurveyIdeals()
	{
		return dataSurveyIdeals;
	}
	
	
	
	public static double[][] getDataSurveyMeasured()
	{
		return dataSurveyMeasured;
	}
}

