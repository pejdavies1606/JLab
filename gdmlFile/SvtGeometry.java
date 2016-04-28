package volume_geometry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;

import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

public class SvtGeometry {

	// ---------------
	// length units
	// ===============
	// CCDB: mm
	// Geant4Basic: cm
	// ---------------
	// angle units
	// =====
	// Geant4Basic: rad, but displayed as deg on Geant4Basic.toString()
	// GDML: rad (default=deg)
	
	// Geant4Basic
	double mTopSide;
	Geant4Basic mTop;
	
	// modules
	double mModuleLength, mModuleWidth, mModuleHeight;
	int mNReg;
	int[] mNSect;
	double[] mRegionZ;
	double mSectorAngleStart;
	double mZRotationStart;
	double[][] mModLyrPos;
	
	// nominal fiducials
	double mOrbRadius = 0.2; // cm
	final int NFIDS = 3; // number of fiducial survey data points on each sensor module
	
	// external files
	String mFilenameGdml = "survey_ideal_all";//"nominal_fid";
	String mFilenameSurveyIdeals = "survey_ideals.dat";
	//String mFilenameSurveyMeasured = "survey_measured.dat";
	//String mFilenameSurveyDeltas = "survey_deltas.dat";
	
	// survey data
	double[][] mSurveyIdeals, mSurveyMeasured, mSurveyDeltas;
	
	
	
	public void loadConstants()
	{
		CcdbGeomSvt.load();
		CcdbGeomSvt.show();
		
		mModuleLength = CcdbGeomSvt.MODULELEN;
		mModuleWidth = CcdbGeomSvt.ACTIVESENWID; // total sensor width, not pitch adapter width
		mModuleHeight = CcdbGeomSvt.SILICONTHICK;		
		mNReg = CcdbGeomSvt.NREG;
		mNSect = CcdbGeomSvt.NSECT;
		mRegionZ = CcdbGeomSvt.Z0;
		mSectorAngleStart = CcdbGeomSvt.PHI0*Math.PI/180.0; // deg -> rad
		mZRotationStart = Math.toRadians( CcdbGeomSvt.LOCZAXISROTATION );
		mModLyrPos = new double[CcdbGeomSvt.NLAYR][3]; // x y z position of pair of layers
	}
	
	
	
	public void loadSurveyData()
	{
		mSurveyIdeals = _inputSurveyData( mFilenameSurveyIdeals );
		//mSurveyMeasured = _inputSurveyData( mFilenameSurveyMeasured );
		//mSurveyDeltas = _inputSurveyData( mFilenameSurveyDeltas );
	}
	
	
	
	public void defineTop()
	{
		mTopSide = 100.0; // cm
		mTop = new Geant4Basic("top", "box", mTopSide, mTopSide, mTopSide );
		//System.out.println( top.toString() );
	}
	
	public void defineRefs()
	{
		Geant4Basic refX = new Geant4Basic("refX", "box", 1.0, 0.25, 0.25 );
		refX.setPosition( 0.5, 0.0, 0.0 );
		mTop.getChildren().add( refX );
		
		Geant4Basic refY = new Geant4Basic("refY", "box", 0.25, 1.0, 0.25 );
		refY.setPosition( 0.0, 0.5, 0.0 );
		mTop.getChildren().add( refY );
		
		Geant4Basic refZ = new Geant4Basic("refZ", "box", 0.25, 0.25, 10.0 );
		refZ.setPosition( 0.0, 0.0, 5.0 );
		mTop.getChildren().add( refZ );
		
		/*Geant4Basic refRotZ = new Geant4Basic("refRotZ", "box", 2.0, 0.25, 0.25 );
		refRotZ.setPosition( 0.0, 0.0, -layerWidth/2.0 );
		top.getChildren().add( refRotZ );*/
	}
	
	
	
	public void defineTarget()
	{
		Geant4Basic target = new Geant4Basic("target", "eltube", 1.5240, 1.5240, 3.0 ); // cm
		mTop.getChildren().add( target );
	}
	
	
	
	public void defineModules()
	{
		for( int r = 0; r < mNReg; r++ ) // mNReg
		{
			double[] sectorRadius = CcdbGeomSvt.MODULERADIUS[r];
			//System.out.println("dphi="+ Math.toDegrees( 2.0*Math.PI/nSect[r]) );
			
			for( int s = 0; s < mNSect[r]; s++ ) // mNSect[r]
			{
				double phi = 2.0*Math.PI/mNSect[r]*s + mSectorAngleStart; // module rotation about target / origin
				//System.out.println("phi="+ Math.toDegrees(phi) );
				double modRot = phi - mZRotationStart;
				_defineSectorModules( r, s, sectorRadius, phi, modRot );
				_defineSectorFiducialsNominal( r, s, modRot );
				_defineSectorFiducialsSurveyIdeals( r, s );
			}
		}
	}
	
		
	
	public void defineAll()
	{
		System.out.println("defining Geant4Basic geometry");
		defineTop();
		defineRefs();
		defineTarget();
		defineModules();
	}
	
	
	
	public void outputGdml()
	{
		IVolumeExporter gdml = VolumeExporterFactory.createVolumeExporter("gdml");
		System.out.println("constructed GdmlFile");
		//gdml.setVerbose( true );
		//gdml.setPositionLoc("local");
		//gdml.setRotationLoc("local");
		gdml.setAngleUnit("rad");
		gdml.addTopVolume( mTop, "mat_vacuum" ); // define all volumes with default material, to set transparency
		//gdml.addMaterialPreset("mat_fid", "mat_fid"); // secondary material to set transparency of fids separately
		//gdml.replaceMat( mTop, "fid","mat_fid"); // replaces all volumes whose name contains "fid" with alternative material "mat_fid"
		gdml.writeFile( mFilenameGdml );
	}
	
	
	
	private void _defineSectorModules( int r, int s, double[] sectorRadius, double phi, double modRot )
	{
		for( int l = 0; l < CcdbGeomSvt.NLAYR; l++ )
		{
			String sl = ""; // super layer label
			switch( l )
			{
			case 0: sl = "u"; break; // lower / inner
			case 1: sl = "v"; break; // upper / outer
			}
			
			Geant4Basic module = new Geant4Basic("mod_r"+(r+1)+"s"+(s+1)+sl, "box", mModuleWidth/10.0, mModuleHeight/10.0, mModuleLength/10.0 );
			mModLyrPos[l][0] = sectorRadius[l]*Math.cos( phi );
			mModLyrPos[l][1] = sectorRadius[l]*Math.sin( phi );
			mModLyrPos[l][2] = mRegionZ[r] + mModuleLength/2.0;
			
			// =====                            
			// -----
			module.setRotation("xyz", 0.0, 0.0, -modRot ); // why minus sign?!
			// -----                            ^
			// =====                            
			//System.out.println("modRot="+ Math.toDegrees( modRot ) );
			
			module.setPosition( mModLyrPos[l][0]/10.0, mModLyrPos[l][1]/10.0, mModLyrPos[l][2]/10.0 );
			
			mTop.getChildren().add( module );
			//System.out.println( module.toString() );
		}
	}
	
	
	
	private void _defineSectorFiducialsNominal( int r, int s, double modRot )
	{
		// calculate centre of module from pair of layers
		double[] modCenPos = new double[3];
		for( int i = 0; i < 3; i++)
			modCenPos[i] = (mModLyrPos[0][i] + mModLyrPos[1][i])/2; // arithmetic mean
		
		Geant4Basic[] fids = new Geant4Basic[NFIDS];
		
		// Cu = copper, upstream
		// Pk = PEEK, downstream
		// mm
		double fidCuX = 17.35; // from centre of module; fid1 +ve X, fid2 -ve X
		double fidCuZ = -61.43 + -3.75; // from Cu edge of module (not edge of superlayer backing structure)
		// 61.43 or 57.13 ?
		double fidPkX = 3.50; // from centre of module; fid3  
		double fidPkZ = -61.43 + 410.72; // from Cu edge of module
		double fidY = 1.25; // from centre of module (half thickness of rohacell)
		
		double[] fidPos = new double[3]; // x y z position
		
		for( int i = 0; i < NFIDS; i++ )
		{
			fids[i] = new Geant4Basic("fidN"+(i+1)+"_r"+(r+1)+"s"+(s+1), "orb", mOrbRadius );
			
			switch(i)
			{
			case 0: // Cu +ve X
				fidPos[0] = fidCuX;
				fidPos[1] = fidY;
				fidPos[2] = -mModuleLength/2.0 + fidCuZ;
				break;
			case 1: // Cu -ve X
				fidPos[0] = -fidCuX;
				fidPos[1] = fidY;
				fidPos[2] = -mModuleLength/2.0 + fidCuZ;
				break;
			case 2: // Pk
				fidPos[0] = -fidPkX;
				fidPos[1] = fidY;
				fidPos[2] = -mModuleLength/2.0 + fidPkZ;
				break;
			}
			
			Point3D fidPos3D = new Point3D();
			Transformation3D fidTranslateFidPos = new Transformation3D();
			Transformation3D fidTranslateModPos = new Transformation3D();
			Transformation3D fidRotateModRot = new Transformation3D();
			
			fidTranslateFidPos.translateXYZ( fidPos[0]/10.0, fidPos[1]/10.0, fidPos[2]/10.0 );
			fidTranslateModPos.translateXYZ( modCenPos[0]/10.0, modCenPos[1]/10.0, modCenPos[2]/10.0 );
			// =====
			// -----
			fidRotateModRot.rotateZ( modRot ); // no minus sign...
			// -----                   ^
			// =====
			//System.out.println("fidRot="+ Math.toDegrees(modRot) );
			
			fidTranslateFidPos.apply( fidPos3D );
			fidRotateModRot.apply( fidPos3D );
			fidTranslateModPos.apply( fidPos3D );
			fids[i].setPosition( fidPos3D.x(), fidPos3D.y(), fidPos3D.z() );
			
			mTop.getChildren().add( fids[i] );
			System.out.println( fids[i].toString() );
		}
	}
	
	
	
	private static double[][] _inputSurveyData( String aFilename )
	{
		//System.out.println("_inputSurveyData()");
		//System.out.println("aFilename=\""+ aFilename +"\"");
		
		double[][] dataResult = null;
		boolean bVerbose = false;

		try
		{
			File file = new File( aFilename );
			Scanner scanner = new Scanner( file );
			
			ArrayList<String> tagList = new ArrayList<String>();
			ArrayList<double[]> dataList = new ArrayList<double[]>();
			
			//System.out.println("dataList.size()="+ dataList.size() );
			
			int i = 0;
			while( scanner.hasNext() )
			{
				if( bVerbose ) System.out.print("i="+ i++ );
				String tag = scanner.next();
				if( bVerbose ) System.out.print(" tag=\""+ tag +"\"");
				tagList.add( tag );
				double[] data = new double[3];
				if( bVerbose ) System.out.print(" data=");
				for( int j = 0; j < 3; j++ )
				{
					data[j] = scanner.nextDouble();
					if( bVerbose ) System.out.print(" "+ data[j] );
				}
				dataList.add( data );
				if( bVerbose ) System.out.println();
				//System.out.println("dataList.size()="+ dataList.size() );
			}
			scanner.close(); // also closes file
			
			//int tagLen = tagList.size();
			int dataLen = dataList.size();
			//System.out.println("tagLen="+ tagLen +" dataLen="+ dataLen );
			
			dataResult = new double[dataLen][3]; // like an RGB image
			
			for( int k = 0; k < dataLen; k++ )
				dataResult[k] = dataList.get(k);
			
			System.out.println("read "+ dataLen +" lines from \""+ aFilename +"\"");
			
		}
		catch( FileNotFoundException e )
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return dataResult;
	}
	
	
	
	/*private int _subSum( int[] aArray, int aIndex )
	{
		// sums elements of aArray from 0 to aIndex-1
		if( aIndex > 0 )
		{
			int[] subArray = new int[aIndex];
			for( int i = 0; i < aIndex; i++ ){
				subArray[i] = aArray[i];
			}
			return IntStream.of( subArray ).sum();
		}
		return 0;
	}*/
	
	
	
	private void _defineSectorFiducialsSurveyIdeals( int r, int s )
	{
		//for( int k = 0; k < mSurveyIdeals.length; k++ )
		//{
			// kn = 198, rn = 4, sn = (10,14,18,24)
			//
			// k = 0:197 = 0:29, 30:71, 72:125, 126:197
			// r = 0:3
			// s = 0:9, 0:13, 0:17, 0:23
			//       +0    +10    +14    +18
			// rs = 0:9, 10:24, 25:38, 39:62
			//			
			/*if     (   0 <= k && k <=  29 )
				r = 0;
			else if(  30 <= k && k <=  71 )
				r = 1;
			else if(  72 <= k && k <= 125 )
				r = 2;
			else if( 126 <= k && k <= 197 )
				r = 3;
			s = (k/NFIDS) - _subSum( mNSect, r );
			System.out.println("k="+ k +" r="+ r +" s="+ s +" ss="+ _subSum( mNSect, r ) );*/
			
			int k = r*mNSect[r] + s;
			
			Geant4Basic[] fids = new Geant4Basic[NFIDS];
			
			for( int i = 0; i < NFIDS; i++ )
			{
				fids[i] = new Geant4Basic("fidI"+(i+1)+"_r"+(r+1)+"s"+(s+1), "orb", mOrbRadius );
				fids[i].setPosition( mSurveyIdeals[k][0]/10.0, mSurveyIdeals[k][1]/10.0, mSurveyIdeals[k][2]/10.0 ); // mm -> cm
				mTop.getChildren().add( fids[i] );
				System.out.println( fids[i].toString() );
			}
		//}
	}
	
	
	
}
