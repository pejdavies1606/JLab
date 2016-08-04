package SVTFactory;

import java.io.IOException;
import java.io.Writer;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

import SVTMisc.Utils;

/**
 * Processes fiducial survey data into alignment shifts. 
 * 
 * @author pdavies
 */
public class SVTAlignmentFactory
{
	private static String outputLine;
	private static String filenameSurveyIdeals;
	private static String filenameSurveyMeasured;
	
	private static String filenameShifts;
	private static Writer outputShifts;
	
	private static String filenameDistances = "measured_distances.dat";
	private static Writer outputDistances;
	
	private static double[][] dataNominal, dataSurveyIdeals, dataSurveyMeasured;
	
	
	
	public static void setDataFiles( String aInputSurveyIdeals, String aInputSurveyMeasured )
	{
		filenameSurveyIdeals = aInputSurveyIdeals;
		filenameSurveyMeasured = aInputSurveyMeasured;
	}
	
	
	
	public static void processSurveyData( ConstantProvider cp, String aOutputSurveyShifts ) throws NullPointerException
	{
		SVTGeant4Factory.getConstants( cp );
		
		filenameShifts = aOutputSurveyShifts;
		dataNominal = SVTGeant4Factory.getNominalFiducialData();
		try
		{
			dataSurveyIdeals = Utils.inputTaggedData( filenameSurveyIdeals, 3 ); // RSF X Y Z
			dataSurveyMeasured = Utils.inputTaggedData( filenameSurveyMeasured, 3 );
		}
		catch( IOException e ){ e.printStackTrace(); }
		
		if( dataNominal == null || dataSurveyIdeals == null || dataSurveyMeasured == null )
			throw new NullPointerException("no data");
		
		_fixMissingSurveyData();
		
		outputShifts = Utils.openOutputDataFile( filenameShifts );
		outputDistances = Utils.openOutputDataFile( filenameDistances );
		
		double fidXDist = 2*SVTGeant4Factory.FIDCUX;
		double fidZDist = SVTGeant4Factory.FIDCUZ + SVTGeant4Factory.FIDPKZ0 + SVTGeant4Factory.FIDPKZ1;
		double fidZDist0 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTGeant4Factory.FIDCUX + SVTGeant4Factory.FIDPKX, 2) );
		double fidZDist1 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTGeant4Factory.FIDCUX - SVTGeant4Factory.FIDPKX, 2) );
		
		//System.out.printf("fidXDist  %8.3f\n", fidXDist );
		//System.out.printf("fidZDist  %8.3f\n", fidZDist );
		//System.out.printf("fidZDist0 %8.3f\n", fidZDist0 );
		//System.out.printf("fidZDist1 %8.3f\n", fidZDist1 );
		
		for( int region = 0; region < SVTGeant4Factory.NREGIONS; region++ )
			for( int sector = 0; sector < SVTGeant4Factory.NSECTORS[region]; sector++ )
			{
				Point3D[] fidIPos3Ds = new Point3D[SVTGeant4Factory.NFIDUCIALS];
				Point3D[] fidMPos3Ds = new Point3D[SVTGeant4Factory.NFIDUCIALS];
				
				for( int fid = 0; fid < SVTGeant4Factory.NFIDUCIALS; fid++ )
				{
					int k = SVTGeant4Factory.convertRegionSectorFid2SurveyIndex( region, sector, fid );
					fidIPos3Ds[fid] = new Point3D( dataSurveyIdeals[k][0], dataSurveyIdeals[k][1], dataSurveyIdeals[k][2] );
					fidMPos3Ds[fid] = new Point3D( dataSurveyMeasured[k][0], dataSurveyMeasured[k][1], dataSurveyMeasured[k][2] );
				}
				
				//double dCu = fidIPos3Ds[0].distance(fidIPos3Ds[1]);
				//double dPk0 = fidIPos3Ds[2].distance(fidIPos3Ds[0]);
				//double dPk1 = fidIPos3Ds[2].distance(fidIPos3Ds[1]);
				double dCu = fidMPos3Ds[1].distance(fidMPos3Ds[0]);
				double dPk0 = fidMPos3Ds[2].distance(fidMPos3Ds[0]);
				double dPk1 = fidMPos3Ds[2].distance(fidMPos3Ds[1]);
				
				double ucMeasured = 0.030; // 30 um
				double DdCu = Utils.calcUncertaintyDistance( ucMeasured , fidMPos3Ds[1], fidMPos3Ds[0] );
				double DdPk0 = Utils.calcUncertaintyDistance( ucMeasured , fidMPos3Ds[2], fidMPos3Ds[0] );
				double DdPk1 = Utils.calcUncertaintyDistance( ucMeasured , fidMPos3Ds[2], fidMPos3Ds[1] );
				outputLine = String.format("R%dS%02d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, dCu, dCu - fidXDist, DdCu, dPk0, dPk0 - fidZDist0, DdPk0, dPk1, dPk1 - fidZDist1, DdPk1 );
				Utils.outputLine( outputDistances, outputLine );
				
				Triangle3D fidITri3D = new Triangle3D( fidIPos3Ds[0], fidIPos3Ds[1], fidIPos3Ds[2] );
				Triangle3D fidMTri3D = new Triangle3D( fidMPos3Ds[0], fidMPos3Ds[1], fidMPos3Ds[2] );
		
				// find shift for position
				Point3D fidICenPos3D = fidITri3D.center();
				Point3D fidMCenPos3D = fidMTri3D.center();
				Vector3D fidMIDiffVec3D = fidICenPos3D.vectorTo( fidMCenPos3D );
				
				// find shift for rotation
				Vector3D fidIVec3D = fidITri3D.normal().asUnit();
				Vector3D fidMVec3D = fidMTri3D.normal().asUnit();
				double[] eulerAxisAngle = Utils.convertVectorDiffToEulerAxisAngle( fidIVec3D, fidMVec3D );
		
				//System.out.printf("PlnI: p(% 8.3f % 8.3f % 8.3f) n(% 8.3f % 8.3f % 8.3f)\n", fidICenPos3D.x(), fidICenPos3D.y(), fidICenPos3D.z(), fidIVec3D.x(), fidIVec3D.y(), fidIVec3D.z() );
				//System.out.printf("PlnM: p(% 8.3f % 8.3f % 8.3f) n(% 8.3f % 8.3f % 8.3f)\n", fidMCenPos3D.x(), fidMCenPos3D.y(), fidMCenPos3D.z(), fidMVec3D.x(), fidMVec3D.y(), fidMVec3D.z() );
				//System.out.printf("Diff: p(% 8.3f % 8.3f % 8.3f) e(% 8.3f % 8.3f % 8.3f % 11.6f)\n", fidMIDiffVec3D.x(), fidMIDiffVec3D.y(), fidMIDiffVec3D.z(), eulerAxisAngle[0], eulerAxisAngle[1], eulerAxisAngle[2], Math.toDegrees(eulerAxisAngle[3]) );
		
				//outputLine = String.format("R%dS%02d % 11.6f % 11.6f % 11.6f % 11.6f % 11.6f % 11.6f % 11.6f\n", region+1, sector+1,
						//fidMIDiffVec3D.x(), fidMIDiffVec3D.y(), fidMIDiffVec3D.z(),
						//eulerAxisAngle[0], eulerAxisAngle[1], eulerAxisAngle[2], Math.toDegrees(eulerAxisAngle[3]) );
				
				outputLine = String.format("R%dS%02d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n", region+1, sector+1,
						fidMIDiffVec3D.x(), fidMIDiffVec3D.y(), fidMIDiffVec3D.z(),
						eulerAxisAngle[0], eulerAxisAngle[1], eulerAxisAngle[2], Math.toDegrees(eulerAxisAngle[3]) );
				
				//String outputLine = String.format("R%dS%02d % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 );
				Utils.outputLine( outputShifts, outputLine );
			}
		Utils.closeOutputDataFile( filenameShifts, outputShifts );
		Utils.closeOutputDataFile( filenameDistances, outputDistances );
	}
	
	
	
	public static double[][] getDataSurveyIdeals()
	{
		return dataSurveyIdeals;
	}
	
	
	
	public static double[][] getDataSurveyMeasured()
	{
		return dataSurveyMeasured;
	}
	
	
	
	private static void _fixMissingSurveyData()
	{
		// R171 (Region 1, Sector 7, Fid Cu1) is missing data, and was manually set to (0,0,0) in the data file
		// replace missing data with nominal, expected data
		int r = 0, s = 6, f = 0;
		int k = SVTGeant4Factory.convertRegionSectorFid2SurveyIndex( r, s, f );
		for( int i = 0; i < 3; i++ )
		{
			dataSurveyIdeals[k][i] = dataNominal[k][i];
			dataSurveyMeasured[k][i] = dataNominal[k][i];
		}
	}
}

