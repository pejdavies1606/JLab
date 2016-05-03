package volume_geometry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;

import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;

public class SvtGeometry {

	// ---------------
	// length units
	// ===============
	// CCDB: mm
	// Geant4Basic: cm
	// ---------------
	// angle units
	// =====
	// Geant4Basic: rad, but displayed as deg on Geant4Basic.gemcString()
	// GDML: rad (default=deg)
	
	// Geant4Basic
	private double mTopSide;
	private Geant4Basic mTop, mGroupMod;
	
	// modules
	private double mModuleLength, mModuleWidth, mModuleHeight;
	private int mNReg;
	private int[] mNSect;
	private double[] mRegionZ;
	private double mSectorAngleStart;
	private double mZRotationStart;
	private double[][] mModLyrPos;
	
	// nominal fiducials
	private double mFidOrbRadius = 0.1; // cm
	private final int NFIDS = 3; // number of fiducial survey data points on each sensor module
	
	// plane arrows
	private double mRefBallRadius = 0.15;
	private double mArrowLength = 1.0;
	private double mArrowDiameter = 0.1*mArrowLength;
	
	// external files
	private String mFilenameGdml;
	private String mFilenameFidSurveyIdeals = "survey_ideals.dat";
	private String mFilenameFidSurveyMeasured = "survey_measured.dat";
	//String mFilenameSurveyDeltas = "survey_deltas.dat";
	
	private String mFilenameNominal = "nominal.dat";
	private Writer mOutputNominal = null;
	private String mFilenameNominalDeltas = "nominal_deltas.dat";
	private Writer mOutputNominalDeltas = null;
	
	// fiducial survey data
	private double[][] mFidSurveyIdeals, mFidSurveyMeasured, mFidSurveyDeltas;
	private double[][] mFidNominalIdeals, mFidNominalDeltas; // nominalDeltas = nominal compared to survey ideal
	
	// component visibility
	private boolean mVisMod, mVisFidN, mVisFidI, mVisFidM, mVisFidNA, mVisFidIA, mVisFidMA;
	
	
	
	public SvtGeometry( String aName )
	{
		mFilenameGdml = aName;
		
		// default visibility
		mVisMod = true;
		mVisFidN = true;
		mVisFidI = true;
		mVisFidM = true;
		mVisFidNA = true;
		mVisFidIA = true;
		mVisFidMA = true;
		
		//System.out.println("setting up SVT Factory");
		
		// setup
		loadConstants();
		loadSurveyData();
		defineTop(); // mother volume
	}
	
	
	
	public void setVisibilityModules( boolean aVis )
	{
		mVisMod = aVis;
	}
	
	
	
	public void setVisibilityFiducialsNominal( boolean aVis )
	{
		mVisFidN = aVis;
	}
	
	
	
	public void setVisibilityFiducialsNominalPlanes( boolean aVis )
	{
		mVisFidNA = aVis;
	}
	
	
	
	public void setVisibilityFiducialsSurveyIdeals( boolean aVis )
	{
		mVisFidI = aVis;
	}
	
	
	
	public void setVisibilityFiducialsSurveyIdealsPlanes( boolean aVis )
	{
		mVisFidIA = aVis;
	}
	
	
	
	public void setVisibilityFiducialsSurveyMeasured( boolean aVis )
	{
		mVisFidM = aVis;
	}
	
	
	
	public void setVisibilityFiducialsSurveyMeasuredPlanes( boolean aVis )
	{
		mVisFidMA = aVis;
	}
	
	
	
	public int getNReg()
	{
		return mNReg;
	}
	
	
	
	public int[] getNSect()
	{
		return mNSect;
	}
	
	
	
	public Geant4Basic getMother()
	{
		return mTop;
	}
	
	
	
	public void loadConstants()
	{
		CcdbGeomSvt.load();
		CcdbGeomSvt.show();
		
		mModuleLength = CcdbGeomSvt.MODULELEN; // total sensor length, including dead zones and micro gaps
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
		mFidSurveyIdeals = _inputSurveyData( mFilenameFidSurveyIdeals );
		mFidSurveyMeasured = _inputSurveyData( mFilenameFidSurveyMeasured );
		//mSurveyDeltas = _inputSurveyData( mFilenameSurveyDeltas );
	}
	
	
	public void openOutputDataFiles()
	{
		try
		{
			mOutputNominal = new BufferedWriter( new FileWriter( mFilenameNominal ) );
			System.out.println("opened \""+ mFilenameNominal +"\"");
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			System.exit(-1);
		}
		try
		{
			mOutputNominalDeltas = new BufferedWriter( new FileWriter( mFilenameNominalDeltas ) );
			System.out.println("opened \""+ mFilenameNominalDeltas +"\"");
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	
	public void closeOutputDataFiles()
	{
		if( mOutputNominal != null )
			try {
				mOutputNominal.close();
				System.out.println("closed \""+ mFilenameNominal +"\"");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		if( mOutputNominalDeltas != null )
			try {
				mOutputNominalDeltas.close();
				System.out.println("closed \""+ mFilenameNominalDeltas +"\"");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
	}
	
	
	
	public void defineTop()
	{
		mTopSide = 100.0; // cm
		mTop = new Geant4Basic("top", "box", mTopSide, mTopSide, mTopSide );
		//mTop.setId( 0 );
		//System.out.println( mTop.gemcString() );
	}
	
	public void defineRefs()
	{
		Geant4Basic refOrigin = new Geant4Basic("refOrigin", "box", 0.0 );
		//refOrigin.setId( 0 );
		refOrigin.setMother( mTop );
		refOrigin.setPosition( 0.0, 10.0, 0.0 );
		//System.out.println( refOrigin.gemcString() );
		
		Geant4Basic refX = new Geant4Basic("refX", "box", 1.0, 0.25, 0.25 );
		refX.setPosition( 0.5, 0.0, 0.0 );
		//refX.setId( 1 );
		//refOrigin.getChildren().add( refX );
		refX.setMother( refOrigin );
		//mTop.getChildren().add( refX );
		//System.out.println( refX.gemcString() );
		
		Geant4Basic refY = new Geant4Basic("refY", "box", 0.25, 1.0, 0.25 );
		refY.setPosition( 0.0, 0.5, 0.0 );
		//refY.setId( 1 );
		refY.setMother( refOrigin );
		//mTop.getChildren().add( refY );
		//System.out.println( refY.gemcString() );
		
		Geant4Basic refZ = new Geant4Basic("refZ", "box", 0.25, 0.25, 1.0 );
		refZ.setPosition( 0.0, 0.0, 0.5 );
		//refZ.setId( 1 );
		refZ.setMother( refOrigin );
		//mTop.getChildren().add( refZ );
		//System.out.println( refZ.gemcString() );
		
		Geant4Basic refRotGroup = new Geant4Basic("refRot", "box", 0.0 );
		//refRotGroup.setId( 0 );
		refRotGroup.setMother( mTop );
		//System.out.println( refRotGroup.gemcString() );
		
		Geant4Basic refRotX = new Geant4Basic("refRotX", "box", 0.25, 2.0, 0.25 );
		refRotX.setPosition( -2.0, 0.0, 0.0 );
		refRotX.setRotation("xyz", Math.toRadians(30.0), 0.0, 0.0 );
		//refRotX.setId( 1 );
		refRotX.setMother( refRotGroup );
		//mTop.getChildren().add( refRotX );
		//System.out.println( refRotX.gemcString() );
		
		Geant4Basic refRotY = new Geant4Basic("refRotY", "box", 0.25, 0.25, 2.0 );
		refRotY.setPosition( 0.0, -2.0, 0.0 );
		refRotY.setRotation("xyz", 0.0, Math.toRadians(30.0), 0.0 );
		//refRotY.setId( 1 );
		refRotY.setMother( refRotGroup );
		//mTop.getChildren().add( refRotY );
		//System.out.println( refRotY.gemcString() );
		
		Geant4Basic refRotZ = new Geant4Basic("refRotZ", "box", 2.0, 0.25, 0.25 );
		refRotZ.setPosition( 0.0, 0.0, -2.0 );
		refRotZ.setRotation("xyz", 0.0, 0.0, Math.toRadians(30.0) );
		//refRotZ.setId( 1 );
		refRotZ.setMother( refRotGroup );
		//mTop.getChildren().add( refRotZ );
		//System.out.println( refRotZ.gemcString() );
	}
	
	
	
	public void defineTarget()
	{
		Geant4Basic target = new Geant4Basic("target", "eltube", 1.5240, 1.5240, 3.0 ); // cm
		//mTop.getChildren().add( target );
		target.setMother( mTop );
	}
	
	
	// ======================================================================================
	public void defineModules( int regionMin, int regionMax, int sectorMin, int[] sectorMax )
	{
		mFidNominalIdeals = new double[_subSum( mNSect, mNReg )*NFIDS][3];
		mFidNominalDeltas = new double[_subSum( mNSect, mNReg )*NFIDS][3];
		
		mGroupMod = new Geant4Basic("modules", "box", 0.0 );
		mGroupMod.setMother( mTop );
		
		for( int r = regionMin; r < regionMax; r++ ) // mNReg
		{
			double[] sectorRadius = CcdbGeomSvt.MODULERADIUS[r];
			//System.out.println("dphi="+ Math.toDegrees( 2.0*Math.PI/nSect[r]) );
			
			for( int s = sectorMin; s < sectorMax[r]; s++ ) // mNSect[r]
			{
				double phi = -2.0*Math.PI/mNSect[r]*s + mSectorAngleStart; // module rotation about target / origin
				//System.out.println("phi="+ Math.toDegrees(phi) );
				double modRot = phi - mZRotationStart; // module rotation about centre of mass
				// booleans: display component, display plane indicators
				_defineSectorModulesNominal( r, s, sectorRadius, phi, modRot, mVisMod );
				_defineSectorFiducialsNominal( r, s, modRot, mVisFidN, mVisFidNA );
				if( r == 0 && s == 6 )
				{
					System.out.println("fixing missing data: survey ideals");
					mFidSurveyIdeals = _correctMissingSurveyData( mFidSurveyIdeals );
					System.out.println("fixing missing data: survey measured");
					mFidSurveyMeasured = _correctMissingSurveyData( mFidSurveyMeasured );
				}
				_defineSectorFiducialsSurvey( "I", mFidSurveyIdeals, r, s, mVisFidI, mVisFidIA );
				_defineSectorFiducialsSurvey( "M", mFidSurveyMeasured, r, s, mVisFidM, mVisFidMA );
			}
		}
	}
	// ======================================================================================
	
	
	public void defineAll()
	{
		defineRefs();
		defineTarget();
		defineModules( 0, mNReg, 0, mNSect );
	}
	
	
	
	public void outputGdml()
	{
		IVolumeExporter gdml = VolumeExporterFactory.createVolumeExporter("gdml");
		System.out.println("constructed GdmlFile");
		//gdml.setVerbose( true ); // not useful for large numbers of volumes
		//gdml.setPositionLoc("local");
		//gdml.setRotationLoc("local");
		gdml.addTopVolume( mTop );
		gdml.addMaterialPreset("mat_fid", "mat_vacuum"); // secondary material to set transparency of fids separately
		gdml.replaceMat( mTop, "fid","mat_fid"); // replaces the materail of all logical volumes "vol_" whose name contains "fid" with alternative material "mat_fid", identical to "mat_vacuum"
		gdml.replaceMat( mTop, "vec","mat_fid");
		gdml.writeFile( mFilenameGdml );
	}
	
	
	//======================================================================================================================
	//======================================================================================================================
	
	
	private void _defineSectorModulesNominal( int r, int s, double[] sectorRadius, double phi, double modRot, boolean aDisplay )
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
			
			if( aDisplay ) module.setMother( mGroupMod );
			//System.out.println( module.gemcString() );
		}
	}
	
	
	
	private void _outputLine( Writer aWriter, String aLine )
	{		
		try
		{
			if( aWriter != null )
				aWriter.write( aLine );
		}
		catch( IOException e )
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	
	private void _definePlaneArrow( String aName, Plane3D aPlane, boolean aDisplay )
	{
		// put reference ball at base of normal vector, with arrow extending vertically in y-direction
		Geant4Basic refBall = new Geant4Basic( aName+"b", "orb", mRefBallRadius );
		refBall.setPosition( aPlane.point().x(), aPlane.point().y(), aPlane.point().z() );
		
		Geant4Basic arrow = new Geant4Basic( aName+"a", "box", mArrowDiameter, mArrowLength, mArrowDiameter );
		//Geant4Basic arrow = new Geant4Basic( aName+"a", "eltube", mArrowDiameter, mArrowDiameter, mArrowLength );
		
		double thetaZ = aPlane.normal().theta();
		double thetaY = Math.PI/2.0 - thetaZ;
		double phiZ = aPlane.normal().phi();
		double phiY = -(Math.PI/2.0 - phiZ);
		arrow.setRotation("xyz", thetaY, 0.0, -phiY ); // invert z-rotation bug
		//arrow.setRotation("xyz", -thetaZ, 0.0, 0.0 );
		
		double shiftX = -mArrowLength/2.0*Math.sin(phiY);
		double shiftY = mArrowLength/2.0*Math.cos(phiY);
		//double shiftX = 0.0;//-mArrowLength/2.0*Math.sin(phiZ);
		//double shiftY = 0.0;//mArrowLength/2.0*Math.cos(phiZ);
		double shiftZ = 0.0;
		
		/*System.out.println(aName);
		System.out.println("thetaZ="+ Math.toDegrees(thetaZ) );
		System.out.println("thetaY="+ Math.toDegrees(thetaY) );
		System.out.println("phiZ="+ Math.toDegrees(phiZ) );
		System.out.println("phiY="+ Math.toDegrees(phiY) );*/
		
		arrow.setPosition( aPlane.point().x() + shiftX, aPlane.point().y() + shiftY, aPlane.point().z() + shiftZ );
		
		if( aDisplay ) mTop.getChildren().add( refBall );
		if( aDisplay ) mTop.getChildren().add( arrow );
	}
	
	
	
	private void _defineSectorFiducialsNominal( int r, int s, double modRot, boolean aDisplayFids, boolean aDisplayArrows )
	{
		// calculate centre of module from pair of layers
		double[] modCenPos = new double[3];
		for( int i = 0; i < 3; i++)
			modCenPos[i] = (mModLyrPos[0][i] + mModLyrPos[1][i])/2; // arithmetic mean
		
		Geant4Basic[] fids = new Geant4Basic[NFIDS];
		Point3D[] fidPos3Ds = new Point3D[NFIDS];
		
		// from centre of module
		// Cu = copper, upstream
		// Pk = PEEK, downstream
		// mm
		//
		// these parameters account for the residual differences
		//double fidPkZFix = -0.016;
		//double[] fidCuZFix = new double[]{ -1.640, -1.560, -1.346, -1.669 };
		//
		double fidPkZFix = 0.0;
		double[] fidCuZFix = new double[]{ 0.0, 0.0, 0.0, 0.0 };
		//
		double fidOriginZ = -mModuleLength/2.0 + -62.13;
		double fidCuX = 17.35;
		double fidCuZ = fidOriginZ + -3.75 + fidCuZFix[r];
		double fidPkX = 3.50;
		double fidPkZ = fidOriginZ + 402.64 + 2.50 + fidPkZFix + fidCuZFix[r];
		double fidY = 1.25; // from centre of module (half thickness of rohacell)
		
		for( int f = 0; f < NFIDS; f++ )
		{
			fids[f] = new Geant4Basic("fidN"+(f+1)+"_r"+(r+1)+"s"+(s+1), "orb", mFidOrbRadius );
			Point3D fidPos3D = new Point3D();
			
			switch(f)
			{
			case 0: // Cu +ve X
				fidPos3D.set( fidCuX/10.0, fidY/10.0, fidCuZ/10.0 );
				break;
			case 1: // Cu -ve X
				fidPos3D.set( -fidCuX/10.0, fidY/10.0, fidCuZ/10.0 );
				break;
			case 2: // Pk
				fidPos3D.set( -fidPkX/10.0, fidY/10.0, fidPkZ/10.0 );
				break;
			}
			
			Transformation3D fidTranslateModPos = new Transformation3D();
			Transformation3D fidRotateModRot = new Transformation3D();
			
			fidTranslateModPos.translateXYZ( modCenPos[0]/10.0, modCenPos[1]/10.0, modCenPos[2]/10.0 );
			// =====
			// -----
			fidRotateModRot.rotateZ( modRot ); // no minus sign...
			// -----                ^
			// =====
			//System.out.println("fidRot="+ Math.toDegrees(modRot) );
			
			fidRotateModRot.apply( fidPos3D );
			fidTranslateModPos.apply( fidPos3D );
			fidPos3Ds[f] = new Point3D( fidPos3D ); // save Point3D for calculating fiducial plane later
			fids[f].setPosition( fidPos3D.x(), fidPos3D.y(), fidPos3D.z() ); // assign location to volume
			//System.out.println( fidPos3D.toString() );
			
			if( aDisplayFids ) mTop.getChildren().add( fids[f] );
			//System.out.println( fids[f].gemcString() );
			
			int k = (_subSum( mNSect, r ) + s)*NFIDS + f;
			mFidNominalIdeals[k][0] = fidPos3D.x();
			mFidNominalIdeals[k][1] = fidPos3D.y();
			mFidNominalIdeals[k][2] = fidPos3D.z();
			for( int i = 0; i < 3; i++ ){
				mFidNominalDeltas[k][i] = mFidNominalIdeals[k][i]*10.0 - mFidSurveyIdeals[k][i]; // cm -> mm
			}
			String outputLine;
			outputLine = String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", r+1, s+1, f+1, mFidNominalIdeals[k][0]*10.0, mFidNominalIdeals[k][1]*10.0, mFidNominalIdeals[k][2]*10.0 ); // cm -> mm
			_outputLine( mOutputNominal, outputLine );
			outputLine = String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", r+1, s+1, f+1, mFidNominalDeltas[k][0]*10.0, mFidNominalDeltas[k][1]*10.0, mFidNominalDeltas[k][2]*10.0 ); // cm -> mm
			_outputLine( mOutputNominalDeltas, outputLine );
		}
		
		// calculate fiducial plane
		Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
		Plane3D fidPln3D = fidTri3D.plane();
		//Point3D fidRef3D = fidPln3D.point(); // reference point for translation shift
		//Vector3D fidNml3D = fidPln3D.normal(); // normal vector for rotation shift
		
		_definePlaneArrow("vecN_r"+(r+1)+"s"+(s+1), fidPln3D, aDisplayArrows );
		
	}
	
	
	
	private double[][] _inputSurveyData( String aFilename )
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
	
	
	
	private double[][] _correctMissingSurveyData( double[][] aData )
	{
		// 3 fids per module: Cu1, Cu2, Pk3
		// 
		// R171 (Region 1, Sector 7, Fid Cu1) is missing data, and was set to (0,0,0) in the data file
		//
		// need to guess where it should be
		// take average RMS x,y,z and angular distribution of the other 9 Cu1 fids
		//
		int r = 0; // region 1
		int s = 6; // sector 7
		int f = 0; // fid Cu1
		int k = (_subSum( mNSect, r ) + s)*NFIDS + f;
		double[] oldData = aData[k].clone();
		for( int i = 0; i < 3; i++)
		{
			aData[k][i] = mFidNominalIdeals[k][i]*10.0; // replace missing data with nominal, expected data
		}
		double[] newData = aData[k].clone();
		
		System.out.println("replacing R"+(r+1)+"S"+(s+1)+"F"+(f+1)+" with nominal data: " + oldData[0] +" "+ oldData[1] +" "+ oldData[2] +" -> "+ newData[0] +" "+ newData[1] +" "+ newData[2] +" ");
		
		/*double[] subDataX = new double[mNSect[r]];
		double[] subDataY = new double[mNSect[r]];
		double[] subDataZ = new double[mNSect[r]];
		double[][] rms = new double[3];
		
		System.out.println("subData=");
		for( int s = 0; s < mNSect[r]; s++)
		{
			int k = (_subSum( mNSect, r ) + s)*NFIDS + f;
			subDataX[s] = aData[k][0];
			subDataY[s] = aData[k][1];
			subDataZ[s] = aData[k][2];
			System.out.println("s="+ s +" data="+ subDataX[s] +" "+ subDataY[s] +" "+ subDataZ[s] );
		}
		
		rms[0] = _rootMeanSquared( subDataX );
		rms[1] = _rootMeanSquared( subDataY );
		rms[2] = _rootMeanSquared( subDataZ );*/

		return aData;
	}
	
	
	
	private int _subSum( int[] aArray, int aIndex )
	{
		// sums elements of aArray from 0 to aIndex-1
		if( aIndex > 0 )
		{
			int[] subArray = new int[aIndex];
			for( int i = 0; i < aIndex; i++ )
				subArray[i] = aArray[i];
			return IntStream.of( subArray ).sum();
		}
		return 0;
	}
	
	
	
	private void _defineSectorFiducialsSurvey( String aLabel, double[][] aData, int r, int s, boolean aDisplayFids, boolean aDisplayArrows )
	{
		//for( int k = 0; k < aData.length; k++ )
		//{
			// kn = 198, rn = 4, sn = (10,14,18,24)
			//
			// k = 0:197 = 0:29, 30:71, 72:125, 126:197
			// r = 0:3
			// s = 0:9, 0:13, 0:17, 0:23
			//       +0    +10    +14    +18
			// rs = 0:9, 10:24, 25:38, 39:62
			//
			// 0 = 0, 30 = 
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
						
			Geant4Basic[] fids = new Geant4Basic[NFIDS];
			Point3D[] fidPos3Ds = new Point3D[NFIDS];
			
			for( int f = 0; f < NFIDS; f++ )
			{
				int k = (_subSum( mNSect, r ) + s)*NFIDS + f;
				//System.out.println("k="+ k +" : r="+ r +" s="+ s +" i="+ i + " : " + aData[k][0] + " " + aData[k][1] + " " + aData[k][2] );
				fids[f] = new Geant4Basic("fid"+ aLabel +(f+1)+"_r"+(r+1)+"s"+(s+1), "orb", mFidOrbRadius );
				fids[f].setPosition( aData[k][0]/10.0, aData[k][1]/10.0, aData[k][2]/10.0 ); // mm -> cm
				if( aDisplayFids ) mTop.getChildren().add( fids[f] );
				//System.out.println( fids[f].gemcString() );
				double[] fidPos = fids[f].getPosition();
				fidPos3Ds[f] = new Point3D( fidPos[0], fidPos[1], fidPos[2] );
			}
			
			Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
			Plane3D fidPln3D = fidTri3D.plane();
			_definePlaneArrow( "vec"+ aLabel +"_r"+(r+1)+"s"+(s+1), fidPln3D, aDisplayArrows );
			
		//}
	}
	
	
	
}
