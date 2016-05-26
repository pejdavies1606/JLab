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
import org.jlab.geom.prim.Vector3D;

public class SvtGeometry {

	// ---------------
	// length units
	// ===============
	// CCDB, all Java code: mm
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
	private Point3D[] mModLyrNPos3Ds; // changes on each sector
	private Point3D mModCenNPos; // changes on each sector
	
	// nominal fiducials
	private double mFidOrbRadius = 1.0; // mm
	private final int NFIDS = 3; // number of fiducial survey data points on each sensor module
	
	// plane arrows
	private double mRefBallRadius = 1.5; // mm
	private double mArrowLength = 10.0; // mm
	private double mArrowDiameter = (10.0/100.0)*mArrowLength; // mm, diameter or width = 10% of length
	
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
	private double[][] mFidSurveyIdeals, mFidSurveyMeasured;//, mFidSurveyDeltas;
	private double[][] mFidNominal, mFidNominalDeltas; // nominalDeltas = nominal compared to survey ideal
	private double mDeltaArrowFactor = 500.0;
	
	// component visibility
	private boolean mVisMod, mVisModA, mVisFidN, mVisFidI, mVisFidM, mVisFidNA, mVisFidNDA, mVisFidIA, mVisFidMA, mFidSurveyFix;
		
	
	
	public SvtGeometry( String aName )
	{
		mFilenameGdml = aName;
		
		// default visibility
		mVisMod = true;
		mVisModA = true;
		mVisFidN = true;
		mVisFidI = true;
		mVisFidM = true;
		mVisFidNA = true;
		mVisFidIA = true;
		mVisFidNDA = true;
		mVisFidMA = true;
		mFidSurveyFix = true;
		
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
	
	
	
	public void setVisibilityModulesPlanes( boolean aVis )
	{
		mVisModA = aVis;
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
	
	
	
	public void setVisibilityFiducialsNominalDeltasPlanes( boolean aVis )
	{
		mVisFidNDA = aVis;
	}
	
	
	
	
	public void setVisibilityFiducialsSurveyMeasured( boolean aVis )
	{
		mVisFidM = aVis;
	}
	
	
	
	public void setVisibilityFiducialsSurveyMeasuredPlanes( boolean aVis )
	{
		mVisFidMA = aVis;
	}
	
	
	
	public void setFiducialsSurveyFix( boolean aFix )
	{
		mFidSurveyFix = aFix;
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
		
		mModLyrNPos3Ds = new Point3D[CcdbGeomSvt.NLAYR]; // x y z position of pair of layers
		mModCenNPos = new Point3D(); // x y z position of center of mass of pair of layers
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
		//refOrigin.setMother( mTop );
		refOrigin.setPosition( 0.0, 0.0, 0.0 );
		//System.out.println( refOrigin.gemcString() );
		
		Geant4Basic refX = new Geant4Basic("refX", "box", 1.0, 0.25, 0.25 );
		refX.setPosition( 0.5, 0.0, 0.0 );
		refX.setMother( refOrigin );
		//System.out.println( refX.gemcString() );
		
		Geant4Basic refY = new Geant4Basic("refY", "box", 0.25, 1.0, 0.25 );
		refY.setPosition( 0.0, 0.5, 0.0 );
		refY.setMother( refOrigin );
		//System.out.println( refY.gemcString() );
		
		Geant4Basic refZ = new Geant4Basic("refZ", "box", 0.25, 0.25, 1.0 );
		refZ.setPosition( 0.0, 0.0, 0.5 );
		refZ.setMother( refOrigin );
		//System.out.println( refZ.gemcString() );
		
		Geant4Basic refRotGroup = new Geant4Basic("refRot", "box", 0.0 );
		refRotGroup.setMother( mTop );
		//System.out.println( refRotGroup.gemcString() );
		
		/*Geant4Basic refRotX = new Geant4Basic("refRotX", "box", 0.25, 2.0, 0.25 );
		refRotX.setPosition( -2.0, 0.0, 0.0 );
		refRotX.setRotation("xyz", Math.toRadians(30.0), 0.0, 0.0 );
		refRotX.setMother( refRotGroup );*/
		//System.out.println( refRotX.gemcString() );
		
		Geant4Basic refRot0 = new Geant4Basic("refRot0", "box", 0.25, 0.25, 2.0 );
		//refRotY.setPosition( 0.0, -2.0, 0.0 );
		//refRotY.setRotation("xyz", 0.0, -Math.toRadians(30.0), 0.0 );
		//refRotY.setRotation("xyz", -Math.toRadians(30.0), 0.0, 0.0 );
		//refRotY.setRotation("xyz", 0.0, 0.0, -Math.toRadians(30.0) );
		//refRotY.setRotation("xyz", -Math.toRadians(30.0), -Math.toRadians(30.0), 0.0 );
		
		//double t = Math.toRadians(60.0);
		//double p = Math.toRadians(30.0);
		//double a = t*Math.sin(p);
		//double b = t*Math.cos(p);
		//double c = p*Math.cos(t);
		
		double a = Math.toRadians(20.0);
		double b = Math.toRadians(20.0);
		double c = Math.toRadians(20.0);
		
		refRot0.setRotation("xyz", 0.0, 0.0, 0.0 );
		refRot0.setMother( refRotGroup );
		//System.out.println( refRotY.gemcString() );
		
		Geant4Basic refRot1 = new Geant4Basic("refRot1", "box", 0.25, 0.25, 2.0 );
		refRot1.setRotation("xyz", a, 0.0, 0.0 );
		refRot1.setMother( refRotGroup );
		
		Geant4Basic refRot2 = new Geant4Basic("refRot2", "box", 0.25, 0.25, 2.0 );
		refRot2.setRotation("xyz", 0.0, b, 0.0 );
		refRot2.setMother( refRotGroup );
		
		Geant4Basic refRot3 = new Geant4Basic("refRot3", "box", 0.25, 0.25, 2.0 );
		refRot3.setRotation("xyz", 0.0, 0.0, c );
		refRot3.setMother( refRotGroup );
		
		Geant4Basic refRot4 = new Geant4Basic("refRot4", "box", 0.25, 0.25, 2.0 );
		refRot4.setRotation("xyz", a, b, c );
		refRot4.setMother( refRotGroup );
		
		/*Geant4Basic refRotZ = new Geant4Basic("refRotZ", "box", 2.0, 0.25, 0.25 );
		refRotZ.setPosition( 0.0, 0.0, -2.0 );
		refRotZ.setRotation("xyz", 0.0, 0.0, Math.toRadians(30.0) );
		refRotZ.setMother( refRotGroup );*/
		//System.out.println( refRotZ.gemcString() );
	}
	
	
	
	public void defineTarget()
	{
		Geant4Basic target = new Geant4Basic("target", "eltube", 1.5240, 1.5240, 3.0 ); // cm
		//mTop.getChildren().add( target );
		target.setMother( mTop );
	}
	
	
	// ======================================================================================
	public void defineModules( int regionMin, int regionMax, int sectorMin, int[] sectorMax ) throws IllegalArgumentException
	{
		if( regionMin < 0 || regionMax > mNReg )
			throw new IllegalArgumentException("regionMin out of bounds [0:"+mNReg+"]= "+regionMin);
			
		mFidNominal = new double[_subSum( mNSect, mNReg )*NFIDS][3];
		mFidNominalDeltas = new double[_subSum( mNSect, mNReg )*NFIDS][3];
		
		mGroupMod = new Geant4Basic("modules", "box", 0.0 );
		//mGroupMod.setId( 0 ); // do not add as physical volume by setting id to 0
		mGroupMod.setMother( mTop );
		
		for( int region = regionMin; region < regionMax; region++ ) // mNReg
		{
			double[] sectorRadius = CcdbGeomSvt.MODULERADIUS[region];
			//System.out.println("dphi="+ Math.toDegrees( 2.0*Math.PI/nSect[r]) );
			
			for( int sector = sectorMin; sector < sectorMax[region]; sector++ ) // mNSect[r]
			{
				double phi = -2.0*Math.PI/mNSect[region]*sector + mSectorAngleStart; // module rotation about target / origin
				double modRot = phi - mZRotationStart; // module rotation about centre of geometry
				System.out.println("\nphi="+ Math.toDegrees(phi) + " modRot=" + Math.toDegrees(modRot) );
				
				_defineModuleNominal( region, sector, sectorRadius, phi, modRot, mVisMod, mVisModA );
				
				_defineFiducialsNominal( region, sector, modRot, mVisFidN, mVisFidNA );
				
				if( region == 0 && sector == 6 && mFidSurveyFix )
				{
					System.out.println("fixing missing data: survey ideals");
					mFidSurveyIdeals = _correctMissingSurveyData( mFidSurveyIdeals );
					
					System.out.println("fixing missing data: survey measured");
					mFidSurveyMeasured = _correctMissingSurveyData( mFidSurveyMeasured );
				}
				
				_defineFiducialsNominalDeltas( region, sector, mVisFidNDA );
				
				_defineFiducialsSurvey( "I", mFidSurveyIdeals, region, sector, mVisFidI, mVisFidIA );
				_defineFiducialsSurvey( "M", mFidSurveyMeasured, region, sector, mVisFidM, mVisFidMA );
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
		gdml.replaceAttribute( mTop, "structure", "volume", "name", "vol_fid", "materialref", "ref", "mat_fid"); // replaces the material of all logical volumes "vol_" whose name contains "fid" with alternative material "mat_fid", identical to "mat_vacuum"
		gdml.replaceAttribute( mTop, "structure", "volume", "name", "vol_modvec", "materialref", "ref", "mat_fid");
		gdml.replaceAttribute( mTop, "structure", "volume", "name", "vol_ref", "materialref", "ref", "mat_fid");
		gdml.writeFile( mFilenameGdml );
	}
	
	
	//======================================================================================================================
	//======================================================================================================================
	
	
	/*private void _definePlaneArrow( String aName, Plane3D aPlane, boolean aDisplayArrow, boolean aDisplayBall )
	{
		// put reference ball at base of normal vector, with arrow extending vertically in y-direction
		Geant4Basic refBallVol = new Geant4Basic( aName+"b", "orb", mRefBallRadius/10.0 ); // mm -> cm
		refBallVol.setPosition( aPlane.point().x()/10.0, aPlane.point().y()/10.0, aPlane.point().z()/10.0 ); // mm -> cm
		
		Geant4Basic arrowVol = new Geant4Basic( aName+"a", "box", mArrowDiameter/10.0, mArrowLength/10.0, mArrowDiameter/10.0 ); // mm -> cm
		//Geant4Basic arrow = new Geant4Basic( aName+"a", "eltube", mArrowDiameter, mArrowDiameter, mArrowLength );
		
		double thetaZ = aPlane.normal().theta();
		double thetaY = Math.PI/2.0 - thetaZ;
		double phiZ = aPlane.normal().phi();
		double phiY = -(Math.PI/2.0 - phiZ);
		arrowVol.setRotation("xyz", thetaY, 0.0, -phiY ); // invert z-rotation bug
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
		System.out.println("phiY="+ Math.toDegrees(phiY) );
		
		arrowVol.setPosition( (aPlane.point().x() + shiftX)/10.0, (aPlane.point().y() + shiftY)/10.0, (aPlane.point().z() + shiftZ)/10.0 ); // mm -> cm
		
		if( aDisplayBall ) refBallVol.setMother( mTop );
		if( aDisplayArrow ) arrowVol.setMother( mTop );
	}*/
	
	
	
	private void _defineArrow( String aName, Point3D aCen, Vector3D aVec, double aRefBallRadius, double aArrowDiameter, double aArrowLength, boolean aDisplayArrow, boolean aDisplayBallStart, boolean aDisplayBallEnd )
	{
		// put reference ball at base of vector, with arrow extending vertically in y-direction, and an optional second reference ball at the end of the arrow
		Geant4Basic refBallStartVol = new Geant4Basic( aName+"b", "orb", aRefBallRadius/10.0 ); // mm -> cm
		refBallStartVol.setPosition( aCen.x()/10.0, aCen.y()/10.0, aCen.z()/10.0 ); // mm -> cm
		
		Geant4Basic arrowVol = new Geant4Basic( aName+"a", "box", aArrowDiameter/10.0, aArrowDiameter/10.0, aArrowLength/10.0 ); // mm -> cm
		
		// spherical rotations
		double theta = aVec.theta();
		double phi = aVec.phi();
		// cartesian rotations
		double alpha = theta;
		double beta =  Math.PI/2.0 - phi;
		double gamma = alpha;
		
		arrowVol.setRotation("zyx", -gamma, beta, -alpha ); // GDML has active rotations, so negate x and z, and reverse the order
		
		// shift centre of geometry of arrow to put start ball at first end
		aVec = aVec.asUnit();
		double shiftX = aVec.x()*aArrowLength/2.0;
		double shiftY = aVec.y()*aArrowLength/2.0;
		double shiftZ = aVec.z()*aArrowLength/2.0;
		
		/*if( aName.contains("mod") )
		{
			System.out.println();
			System.out.println(aName);
			System.out.println("theta="+ Math.toDegrees(theta) );
			System.out.println("phi="+ Math.toDegrees(phi) );
			System.out.println("alpha="+ Math.toDegrees(alpha) );
			System.out.println("beta="+ Math.toDegrees(beta) );
			System.out.println("gamma="+ Math.toDegrees(gamma) );
		}*/
		
		arrowVol.setPosition( (aCen.x() + shiftX)/10.0, (aCen.y() + shiftY)/10.0, (aCen.z() + shiftZ)/10.0 ); // mm -> cm
		
		Geant4Basic refBallEndVol = new Geant4Basic( aName+"c", "orb", aRefBallRadius/10.0 ); // mm -> cm
		refBallEndVol.setPosition( (aCen.x() + 2*shiftX)/10.0, (aCen.y() + 2*shiftY)/10.0, (aCen.z() + 2*shiftZ)/10.0 ); // mm -> cm
		
		if( aDisplayBallStart ) refBallStartVol.setMother( mTop );
		if( aDisplayArrow ) arrowVol.setMother( mTop );
		if( aDisplayBallEnd ) refBallStartVol.setMother( mTop );
	}
	
	
	
	private void _defineModuleNominal( int aRegion, int aSector, double[] aSectorRadius, double aPhi, double aModRot, boolean aDisplay, boolean aDisplayArrow )
	{
		Transformation3D rotPhi = new Transformation3D();
		rotPhi.rotateZ( aPhi );
		
		for( int l = 0; l < CcdbGeomSvt.NLAYR; l++ )
		{
			String label = ""; // sensor layer label
			switch( l )
			{
			case 0: label = "u"; break; // inner (lower when spawned)
			case 1: label = "v"; break; // outer (upper when spawned)
			}
			
			Geant4Basic modVol = new Geant4Basic("modLyrN_r"+(aRegion+1)+"s"+(aSector+1)+label, "box", mModuleWidth/10.0, mModuleHeight/10.0, mModuleLength/10.0 ); // mm -> cm
			
			Point3D modLyrPos3D = new Point3D( aSectorRadius[l], 0.0, mRegionZ[aRegion] + mModuleLength/2.0 ); // centre of geometry
			rotPhi.apply( modLyrPos3D );
			System.out.println("modLyrPos3D "+ modLyrPos3D.toString() );
			
			modVol.setRotation("zyx", -aModRot, 0.0, 0.0 ); // GDML has active rotations, so negate x and z, and reverse the order                        
			//System.out.println("modRot="+ Math.toDegrees( modRot ) );
			
			modVol.setPosition( modLyrPos3D.x()/10.0, modLyrPos3D.y()/10.0, modLyrPos3D.z()/10.0 ); // mm -> cm
			
			if( aDisplay ) modVol.setMother( mGroupMod );
			//System.out.println( modVol.gemcString() );
		}
		
		Transformation3D rotMod = new Transformation3D();
		rotMod.rotateZ( aModRot );
		
		// calculate centre of geometry of module from pair of layers using arithmetic mean
		Point3D modCenPos3D = new Point3D( (aSectorRadius[0] + aSectorRadius[1])/2.0, 0.0, mRegionZ[aRegion] + mModuleLength/2.0 );
		Vector3D modCenVec3D = new Vector3D( 0.0, 1.0, 0.0 ); // plane normal vector
		
		rotPhi.apply( modCenPos3D );
		rotMod.apply( modCenVec3D );
		
		System.out.println("modCenPos3D "+ modCenPos3D.toString() );
		System.out.println("modCenVec3D "+ modCenVec3D.toString() );
		
		// store centre of module for fiducials later
		mModCenNPos = modCenPos3D;
		
		// module plane
		_defineArrow("modVecN_r"+(aRegion+1)+"s"+(aSector+1), mModCenNPos, modCenVec3D, mRefBallRadius, mArrowDiameter, mArrowLength, aDisplayArrow, aDisplayArrow, false );
	}
	
	
	
	/*private void _defineSectorModulesSurveyShifted( int r, int s, double[] sectorRadius, double phi, double modRot, boolean aDisplay, boolean aDisplayArrow )
	{
		for( int l = 0; l < CcdbGeomSvt.NLAYR; l++ )
		{
			String sl = ""; // super layer label
			switch( l )
			{
			case 0: sl = "u"; break; // lower / inner
			case 1: sl = "v"; break; // upper / outer
			}
			
			Geant4Basic modVol = new Geant4Basic("modLyrS_r"+(r+1)+"s"+(s+1)+sl, "box", mModuleWidth/10.0, mModuleHeight/10.0, mModuleLength/10.0 ); // mm -> cm
			modLyrSPos[l] = new Point3D(
					mModLyrNPos[l].x() + mModSurveyShift[r][s], 
					mModLyrNPos[l].y(), 
					mModLyrNPos[l].z();
			
			// =====                            
			// -----
			modVol.setRotation("xyz", 0.0, 0.0, -modRot ); // why minus sign?!
			// -----                            ^
			// =====                            
			//System.out.println("modRot="+ Math.toDegrees( modRot ) );
			
			modVol.setPosition( mModLyrNPos[l].x()/10.0, mModLyrNPos[l].y()/10.0, mModLyrNPos[l].z()/10.0 ); // mm -> cm
			
			if( aDisplay ) modVol.setMother( mGroupMod );
			System.out.println( modVol.gemcString() );
		}
		
		// calculate centre of module from pair of layers using arithmetic mean
		mModCenNPos.setX( (mModLyrNPos[0].x() + mModLyrNPos[1].x())/2 );
		mModCenNPos.setY( (mModLyrNPos[0].y() + mModLyrNPos[1].y())/2 ); 
		mModCenNPos.setZ( (mModLyrNPos[0].z() + mModLyrNPos[1].z())/2 );
		System.out.println( mModCenNPos.toString() );
		
		Vector3D modCenVec = new Vector3D( mModLyrNPos[1].x() - mModLyrNPos[0].x(), mModLyrNPos[1].y() - mModLyrNPos[0].y(), 0.0 );
		
		// calculate fiducial plane
		Plane3D modPln3D = new Plane3D( mModCenNPos, modCenVec );
		//Point3D fidRef3D = fidPln3D.point(); // reference point for translation shift
		//Vector3D fidNml3D = fidPln3D.normal(); // normal vector for rotation shift
		
		_definePlaneArrow("modVecS_r"+(r+1)+"s"+(s+1), modPln3D, aDisplayArrow, aDisplayArrow );
	}*/
	
	
	
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
	
	
	
	private void _defineFiducialsNominal( int aRegion, int aSector, double aModRot, boolean aDisplayFids, boolean aDisplayArrow )
	{
		
		Geant4Basic[] fidVols = new Geant4Basic[NFIDS];
		Point3D[] fidPos3Ds = new Point3D[NFIDS];
		
		// from centre of module
		// Cu = copper, upstream
		// Pk = PEEK, downstream
		// mm
		//
		double fidOriginZ = -mModuleLength/2.0 + -62.13;
		double fidCuX = 17.35;
		double fidCuZ = fidOriginZ + -3.75;
		double fidPkX = 3.50;
		double fidPkZ = fidOriginZ + 402.64 + 2.50;
		double fidY = 1.25; // half thickness of rohacell
		
		//System.out.printf("R%dS%02d % 8.3f\n", (r+1), (s+1), (fidPkZ - fidCuZ) );
		
		for( int f = 0; f < NFIDS; f++ )
		{
			fidVols[f] = new Geant4Basic("fidLyrN"+(f+1)+"_r"+(aRegion+1)+"s"+(aSector+1), "orb", mFidOrbRadius/10.0 ); // mm -> cm
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
			
			fidTranslateModPos.translateXYZ( mModCenNPos.x()/10.0, mModCenNPos.y()/10.0, mModCenNPos.z()/10.0 ); // mm -> cm
			// =====
			// -----
			fidRotateModRot.rotateZ( aModRot ); // no minus sign... = not GEANT, therefore passive (sensible) rotation
			// -----                ^
			// =====
			//System.out.println("fidRot="+ Math.toDegrees(modRot) );
			
			fidRotateModRot.apply( fidPos3D );
			fidTranslateModPos.apply( fidPos3D );
			fidPos3Ds[f] = new Point3D( fidPos3D ); // save Point3D for calculating fiducial plane later
			fidVols[f].setPosition( fidPos3D.x(), fidPos3D.y(), fidPos3D.z() ); // assign location to volume
			//System.out.println( fidPos3D.toString() );
			
			if( aDisplayFids ) fidVols[f].setMother( mTop );
			//System.out.println( fids[f].gemcString() );
			
			int k = (_subSum( mNSect, aRegion ) + aSector)*NFIDS + f;
			mFidNominal[k][0] = fidPos3D.x()*10.0; // cm -> mm
			mFidNominal[k][1] = fidPos3D.y()*10.0;
			mFidNominal[k][2] = fidPos3D.z()*10.0;
		}
		
		// calculate fiducial plane
		Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
		Plane3D fidPln3D = fidTri3D.plane();
		
		fidPln3D.setPoint( fidPln3D.point().x()*10.0, fidPln3D.point().y()*10.0, fidPln3D.point().z()*10.0);
		
		_defineArrow("fidVecN_r"+(aRegion+1)+"s"+(aSector+1), fidPln3D.point(), fidPln3D.normal(), mRefBallRadius, mArrowDiameter, mArrowLength, aDisplayArrow, aDisplayArrow, false );
		
	}
	
	
	
	private void _defineFiducialsNominalDeltas( int aRegion, int aSector, boolean aDisplayArrow )
	{
		for( int f = 0; f < NFIDS; f++ )
		{
			int k = (_subSum( mNSect, aRegion ) + aSector)*NFIDS + f;
			for( int i = 0; i < 3; i++ ){
				mFidNominalDeltas[k][i] = mFidNominal[k][i] - mFidSurveyIdeals[k][i]; // mm
			}
			String outputLine;
			outputLine = String.format("R%dS%02dF%d, % 8.3f, % 8.3f, % 8.3f\n", aRegion+1, aSector+1, f+1, mFidNominal[k][0], mFidNominal[k][1], mFidNominal[k][2] );
			_outputLine( mOutputNominal, outputLine );
			outputLine = String.format("R%dS%02dF%d, % 8.3f, % 8.3f, % 8.3f\n", aRegion+1, aSector+1, f+1, mFidNominalDeltas[k][0], mFidNominalDeltas[k][1], mFidNominalDeltas[k][2] );
			_outputLine( mOutputNominalDeltas, outputLine );
			
			Point3D nominalPoint = new Point3D( mFidSurveyIdeals[k][0], mFidSurveyIdeals[k][1], mFidSurveyIdeals[k][2]);
			Vector3D deltaVector = new Vector3D( mFidNominalDeltas[k][0], mFidNominalDeltas[k][1], mFidNominalDeltas[k][2] );
			
			double arrowLength = deltaVector.r()*mDeltaArrowFactor; // arrow misaligned?
			double refBallRadius = mRefBallRadius;
			
			System.out.println( nominalPoint.toString() );
			System.out.println( deltaVector.toString() );
			System.out.println("mag="+ deltaVector.mag() );
			System.out.println("r="+ deltaVector.r() );
			System.out.println("arrowLength="+ arrowLength );
			
			_defineArrow("fidVecND"+(f+1)+"_r"+(aRegion+1)+"s"+(aSector+1), nominalPoint, deltaVector, refBallRadius, mArrowDiameter, arrowLength, aDisplayArrow, false, false );
		}
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
		// take average RMS x,y,z and angular distribution of the other 9 Cu1 fids?
		//
		int r = 0; // region 1
		int s = 6; // sector 7
		int f = 0; // fid Cu1
		int k = (_subSum( mNSect, r ) + s)*NFIDS + f;
		double[] oldData = aData[k].clone();
		for( int i = 0; i < 3; i++)
		{
			aData[k][i] = mFidNominal[k][i]; // replace missing data with nominal, expected data
		}
		double[] newData = aData[k];
		
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
	
	
	
	private void _defineFiducialsSurvey( String aLabel, double[][] aData, int aRegion, int aSector, boolean aDisplayFids, boolean aDisplayArrow )
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
						
			Geant4Basic[] fidVols = new Geant4Basic[NFIDS];
			Point3D[] fidPos3Ds = new Point3D[NFIDS];
			
			for( int f = 0; f < NFIDS; f++ )
			{
				int k = (_subSum( mNSect, aRegion ) + aSector)*NFIDS + f; // index for survey data
				//System.out.println("k="+ k +" : r="+ r +" s="+ s +" i="+ i + " : " + aData[k][0] + " " + aData[k][1] + " " + aData[k][2] );
				double[] fidPos = aData[k];
				fidVols[f] = new Geant4Basic("fidLyr"+ aLabel +(f+1)+"_r"+(aRegion+1)+"s"+(aSector+1), "orb", mFidOrbRadius/10.0 );
				fidVols[f].setPosition( fidPos[0]/10.0, fidPos[1]/10.0, fidPos[2]/10.0 ); // mm -> cm
				if( aDisplayFids ) fidVols[f].setMother( mTop );
				//System.out.println( fids[f].gemcString() );
				fidPos3Ds[f] = new Point3D( fidPos[0], fidPos[1], fidPos[2] );
			}
			
			Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
			Plane3D fidPln3D = fidTri3D.plane();
			_defineArrow("fidVec"+ aLabel +"_r"+(aRegion+1)+"s"+(aSector+1), fidPln3D.point(), fidPln3D.normal(), mRefBallRadius, mArrowDiameter, mArrowLength, aDisplayArrow, aDisplayArrow, false );
			
		//}
	}
	
	
	
}
