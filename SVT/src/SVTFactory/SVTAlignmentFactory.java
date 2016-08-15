package SVTFactory;

import java.io.IOException;
import java.io.Writer;

import org.jlab.geom.base.ConstantProvider;

import Alignment.AlignmentFactory;
import Misc.Util;

/**
 * Processes fiducial survey data into alignment shifts. 
 * 
 * @author pdavies
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
		SVTGeant4Factory.getConstants( cp );
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
		double[][] dataShifts = AlignmentFactory.calcShifts( SVTGeant4Factory.NTOTALSECTORS, aDataNominal, aDataMeasured );
		
		outputShifts = Util.openOutputDataFile( aOutputShifts );
		//outputDistances = Util.openOutputDataFile( filenameDistances );
		//outputIdealsFiducials = Util.openOutputDataFile( filenameIdealsFiducials );
		//outputMeasuredFiducials = Util.openOutputDataFile( filenameMeasuredFiducials );
		
		/*double fidXDist = 2*SVTGeant4Factory.FIDCUX;
		double fidZDist = SVTGeant4Factory.FIDCUZ + SVTGeant4Factory.FIDPKZ0 + SVTGeant4Factory.FIDPKZ1;
		double fidZDist0 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTGeant4Factory.FIDCUX + SVTGeant4Factory.FIDPKX, 2) );
		double fidZDist1 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTGeant4Factory.FIDCUX - SVTGeant4Factory.FIDPKX, 2) );
		
		System.out.printf("fidXDist  %8.3f\n", fidXDist );
		System.out.printf("fidZDist  %8.3f\n", fidZDist );
		System.out.printf("fidZDist0 %8.3f\n", fidZDist0 );
		System.out.printf("fidZDist1 %8.3f\n", fidZDist1 );*/
		
		for( int l = 0; l < SVTGeant4Factory.NTOTALSECTORS; l++ )
		{
			/*int nf = SVTGeant4Factory.NFIDUCIALS;
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

			int[] rs = SVTGeant4Factory.convertSvtIndex2RegionSector( l );
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
		double[][] dataDeltasMeasuredFromNominal = AlignmentFactory.calcDeltas( SVTGeant4Factory.NTOTALSECTORS*SVTGeant4Factory.NFIDUCIALS, 3, aDataNominal, aDataMeasured );

		outputDeltas = Util.openOutputDataFile( aOutputDeltas );

		for( int k = 0; k < SVTGeant4Factory.NTOTALSECTORS*SVTGeant4Factory.NFIDUCIALS; k++ )
		{
			int[] rsf = SVTGeant4Factory.convertSurveyIndex2RegionSectorFiducial( k );
			outputLine = String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", rsf[0]+1, rsf[1]+1, rsf[2]+1,
					dataDeltasMeasuredFromNominal[k][0], dataDeltasMeasuredFromNominal[k][1], dataDeltasMeasuredFromNominal[k][2] );
			Util.writeLine( outputDeltas, outputLine );
			//System.out.print(outputLine);
		}
		Util.closeOutputDataFile( aOutputDeltas, outputDeltas );
	}
	
	
	
	public static double[][] getDataSurveyIdeals()
	{
		return dataSurveyIdeals;
	}
	
	
	
	public static double[][] getDataSurveyMeasured()
	{
		return dataSurveyMeasured;
	}
	
	
	
	/*private static void _fixMissingSurveyData()
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
	}*/
}

