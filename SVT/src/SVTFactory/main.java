package SVTFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

import SVTMisc.Matrix;
import SVTMisc.Utils;
import VolumeExporter.*;

public class main
{
	public static void main(String[] args)
	{	
		/*DatabaseConstantProvider cp2 = new DatabaseConstantProvider( 10, "default");
		cp2.loadTable( SVTGeant4Factory.getCcdbPath() +"svt");
		cp2.loadTable( SVTGeant4Factory.getCcdbPath() +"region");
		cp2.loadTable( SVTGeant4Factory.getCcdbPath() +"fiducial");
		cp2.loadTable( SVTGeant4Factory.getCcdbPath() +"support");
		//cp.loadTable( SVTGeant4Factory.getCcdbPath() +"survey");
		//cp.loadTable( SVTGeant4Factory.getCcdbPath() +"material");
		cp2.disconnect();
		//System.out.println(cp.toString());
		//System.exit(0);
		
		SVTGeant4Factory.getConstants( cp2 );
		SVTAlignmentFactory.processSurveyData("shifts_survey2.dat");
		
		SVTGeant4Factory svt = new SVTGeant4Factory( cp2 );
		System.out.println(svt.showRange());
		
		svt.setRange( 0, 1, new int[]{0,0,0,0}, new int[]{0,0,0,0} ); // one-based indices
		
		for( int l = svt.getLayerMin()-1; l < svt.getLayerMax(); l++ )
		{
			for( int s = svt.getSectorMin()[SVTGeant4Factory.convertLayer2RegionModule(l)[0]]-1; s < svt.getSectorMax()[SVTGeant4Factory.convertLayer2RegionModule(l)[0]]; s++ ) // SVTGeant4Factory.NSECTORS[SVTGeant4Factory.convertLayer2RegionModule(l)[0]]
			{
				for( int i = 0; i < SVTGeant4Factory.NSTRIPS; i+=50 ) // SVTGeant4Factory.NSTRIPS
				{
					System.out.println("layer "+(l+1)+" sector "+(s+1)+" strip "+(i+1)); // print one-based indices 
					Line3D stripLine = svt.getStrip( l, s, i ); // pass zero-based indices to methods
					System.out.printf(" origin % 8.3f % 8.3f % 8.3f\n", stripLine.origin().x(), stripLine.origin().y(), stripLine.origin().z() );
					System.out.printf(" end    % 8.3f % 8.3f % 8.3f\n", stripLine.end().x(), stripLine.end().y(), stripLine.end().z() );
					
					Geant4Basic stripVol = Utils.createArrow("strip"+i+"_s"+s+"_l"+l, stripLine.toVector(), 0.5, 0.2, true, true, true );
					stripVol.setPosition( stripLine.origin().x()*0.1, stripLine.origin().y()*0.1, stripLine.origin().z()*0.1 );
					stripVol.setMother( svt.getMotherVolume() );
					//System.out.println( stripVol.gemcString() );
					//for( int c = 0; c < stripVol.getChildren().size(); c++ )
						//System.out.println( stripVol.getChildren().get(c).gemcString() );
				}
			}
		}
		
		svt.makeVolumes();
		//System.out.println( svt.toString() );
		
		IGdmlExporter gdmlFile0 = VolumeExporterFactory.createGdmlExporter();
		gdmlFile0.setPositionLoc("local");
		gdmlFile0.setRotationLoc("local");
		gdmlFile0.addTopVolume( svt.getMotherVolume() );
		gdmlFile0.addMaterialPreset("mat_sensorActive", "mat_vacuum");
		gdmlFile0.replaceAttribute( "structure", "volume", "name", "vol_sensorActive", "materialref", "ref", "mat_sensorActive");
		gdmlFile0.replaceAttribute( "structure", "volume", "name", "vol_deadZone", "materialref", "ref", "mat_sensorActive");
		gdmlFile0.writeFile("svtStrips");
		System.exit(0);*/
		
		/*
		Vector3D axis = new Vector3D( 0, 1, 1 ).asUnit();
		double angle = 45;
		double[] aa = new double[]{ axis.x(), axis.y(), axis.z(), Math.toRadians( angle ) };
		Matrix maa = Matrix.convertRotationAxisAngleToMatrix( aa ); // y component not computed correctly?
		
		Matrix v0 = new Matrix( 3, 1, new double[]{ 0, 0, 1 } );
		Matrix v1 = Matrix.matMul( maa, v0 );
		
		System.out.print("axis ");
		axis.show();
		System.out.println("angle "+ angle );
		maa.show("axis angle matrix");
		v0.show("vector before");
		v1.show("vector after");
		
		Geant4Basic topVol = new Geant4Basic("top", "Box", 0 );
		Geant4Basic originVol = new Geant4Basic("origin", "Orb", 0.02 );
		originVol.setMother( topVol );
		Geant4Basic v0Vol = Utils.createArrow("v0", Utils.toVector3D( v0 ), 0.2, 0.1, false, true, false );
		v0Vol.setMother( topVol );
		Geant4Basic v1Vol = Utils.createArrow("v1", Utils.toVector3D( v1 ), 0.2, 0.1, false, true, false );
		v1Vol.setMother( topVol );
		
		IGdmlExporter gdmlTest = VolumeExporterFactory.createGdmlExporter();
		//gdmlTest.setVerbose( true ); // not useful for large numbers of volumes
		gdmlTest.setPositionLoc("local");
		gdmlTest.setRotationLoc("local");
		gdmlTest.addTopVolume( topVol );
		gdmlTest.writeFile("test_matrix");
		//System.exit(0);*/
		
		/*PrintStream originalStream = System.out; // http://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
		PrintStream dummyStream    =  new PrintStream( new OutputStream(){
		    public void write(int b) {
		        //NO-OP
		    }} );
					
		Point3D originPos = new Point3D( 0.0, 1.0, 0.0 );
		Plane3D fidPln0 = new Plane3D( new Point3D( originPos ), new Vector3D( 0.0, 1.0, 0.0 ) );
		Plane3D fidPln1 = new Plane3D( new Point3D( fidPln0.point().x() + 1.0, fidPln0.point().y(), fidPln0.point().z() ), new Vector3D( 0.0, 0.0, 1.0 ) );
		double[] translationShift = Utils.toDoubleArray( fidPln0.point().vectorTo( fidPln1.point() ) );
		double[] rotationShift = Utils.convertVectorDiffToEulerAxisAngle( fidPln0.normal(), fidPln1.normal() );
		
		System.out.printf(" O: %5.1f %5.1f %5.1f\n", originPos.x(), originPos.y(), originPos.z() );
		System.out.printf("F0: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f\n", fidPln0.point().x(), fidPln0.point().y(), fidPln0.point().z(), fidPln0.normal().x(), fidPln0.normal().y(), fidPln0.normal().z() );
		System.out.printf("F1: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f\n", fidPln1.point().x(), fidPln1.point().y(), fidPln1.point().z(), fidPln1.normal().x(), fidPln1.normal().y(), fidPln1.normal().z() );
		System.out.printf(" S: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f | %5.1f\n", translationShift[0], translationShift[1], translationShift[2], rotationShift[0], rotationShift[1], rotationShift[2], Math.toDegrees(rotationShift[3]) );
		
		Point3D arbPos0 = new Point3D( originPos.x(), originPos.y() + 1.0, originPos.z() + 1.0 );
		Point3D arbPos1 = new Point3D( arbPos0 );
		
		Vector3D translationVec = Utils.toVector3D( translationShift );
		Vector3D centerVec = fidPln0.point().toVector3D();
		Vector3D rotationVec = new Vector3D( rotationShift[0], rotationShift[1], rotationShift[2] );
		System.out.printf("A0: %5.1f %5.1f %5.1f\n", arbPos0.x(), arbPos0.y(), arbPos0.z() );
		
		centerVec.scale( -1 );
		arbPos1.set( arbPos1, centerVec );
		System.out.printf("A1: %5.1f %5.1f %5.1f centered\n", arbPos1.x(), arbPos1.y(), arbPos1.z() );
		
		System.setOut(dummyStream); // suppress unwanted debug output from Vector3D.rotate()
		rotationVec.rotate( arbPos1, rotationShift[3] );
		System.setOut(originalStream);
		System.out.printf("A1: %5.1f %5.1f %5.1f rotated\n", arbPos1.x(), arbPos1.y(), arbPos1.z() );
		
		centerVec.scale( -1 );
		arbPos1.set( arbPos1, centerVec );
		System.out.printf("A1: %5.1f %5.1f %5.1f centered back\n", arbPos1.x(), arbPos1.y(), arbPos1.z() );
		
		arbPos1.set( arbPos1, translationVec );
		System.out.printf("A1: %5.1f %5.1f %5.1f translated\n", arbPos1.x(), arbPos1.y(), arbPos1.z() );
		
		
		Geant4Basic topVol = new Geant4Basic("top", "Box", 0 );
		
		double orbR = 0.02, arrowR = 0.01;
		
		Geant4Basic zeroVol = new Geant4Basic("origin", "Orb", orbR );
		zeroVol.setMother( topVol );
		
		Geant4Basic originVol = new Geant4Basic("origin", "Orb", orbR );
		originVol.setPosition( originPos.x()*0.1, originPos.y()*0.1, originPos.z()*0.1 );
		originVol.setMother( topVol );
		
		Geant4Basic fidPln0Vol = Utils.createArrow("fidPln0", fidPln0.normal(), orbR*10, arrowR*10, true, true, false );
		fidPln0Vol.setPosition( fidPln0.point().x()*0.1, fidPln0.point().y()*0.1, fidPln0.point().z()*0.1 );
		fidPln0Vol.setMother( topVol );
		
		Geant4Basic fidPln1Vol = Utils.createArrow("fidPln1", fidPln1.normal(), orbR*10, arrowR*10, true, true, false );
		fidPln1Vol.setPosition( fidPln1.point().x()*0.1, fidPln1.point().y()*0.1, fidPln1.point().z()*0.1 );
		fidPln1Vol.setMother( topVol );
		
		Geant4Basic arbPos0Vol = new Geant4Basic("arbPos0", "Orb", orbR );
		arbPos0Vol.setPosition( arbPos0.x()*0.1, arbPos0.y()*0.1, arbPos0.z()*0.1 );
		arbPos0Vol.setMother( topVol );
		
		Geant4Basic arbPos1Vol = new Geant4Basic("arbPos1", "Orb", orbR );
		arbPos1Vol.setPosition( arbPos1.x()*0.1, arbPos1.y()*0.1, arbPos1.z()*0.1 );
		arbPos1Vol.setMother( topVol );
		
		IGdmlExporter gdmlTest = VolumeExporterFactory.createGdmlExporter();
		//gdmlTest.setVerbose( true ); // not useful for large numbers of volumes
		gdmlTest.setPositionLoc("local");
		gdmlTest.setRotationLoc("local");
		gdmlTest.addTopVolume( topVol );
		gdmlTest.writeFile("test_shift");
		
		System.exit(0);*/
		
		/*Vector3D vec = new Vector3D( 0.0, 1.0, 0.5 ).asUnit();
		Matrix vecMatMul = Utils.convertRotationVectorToMatrix( vec.theta(), vec.phi() );
		Matrix vecMatCalc = Matrix.convertRotationFromEulerInZYX_ExXYZ( 0.0, vec.theta(), vec.phi() );
		double[] xyzMatMul = Matrix.convertRotationToEulerInXYZ_ExZYX( vecMatMul );
		double[] xyzMatCalc = Matrix.convertRotationToEulerInXYZ_ExZYX( vecMatCalc );
		
		Point3D pos1 = new Point3D( 0.0, 0.0, 1.0 );
		Transformation3D eulerTrans = new Transformation3D().rotateZ(xyzMatCalc[2]).rotateY(xyzMatCalc[1]).rotateX(xyzMatCalc[0]); // extrinsic ZYX
		eulerTrans.apply( pos1 );
		Point3D pos2 = new Point3D( 0.0, 0.0, 1.0 );
		Transformation3D vecTrans = new Transformation3D().rotateY(vec.theta()).rotateZ(vec.phi());
		vecTrans.apply(pos2);
		Point3D pos3 = new Point3D( 0.0, 0.0, 1.0 );
		Vector3D vecAxis = new Vector3D( -1.0, 0.0, 0.0 ).asUnit();
		vecAxis.rotate( pos3, Math.toRadians(63.4)); // axis angle
		
		vec.show();
		System.out.printf("theta % 6.1f\nphi   % 6.1f", Math.toDegrees(vec.theta()), Math.toDegrees(vec.phi()) );
		System.out.println("\nmatmul");
		vecMatMul.show();
		System.out.printf("\neuler % 6.1f % 6.1f % 6.1f", Math.toDegrees(xyzMatMul[0]), Math.toDegrees(xyzMatMul[1]), Math.toDegrees(xyzMatMul[2]) );
		System.out.println("\ncalc");
		vecMatCalc.show();
		System.out.printf("\neuler % 6.1f % 6.1f % 6.1f", Math.toDegrees(xyzMatCalc[0]), Math.toDegrees(xyzMatCalc[1]), Math.toDegrees(xyzMatCalc[2]) );
		System.out.println("\npoint 1");
		pos1.show();
		System.out.println("\npoint 2");
		pos2.show();
		System.out.println("\npoint 3");
		pos3.show();
		System.exit(0);*/
		
		// ConstantProvider cp = new DatabaseLoader.getSVTConstants();
		DatabaseConstantProvider cp = new DatabaseConstantProvider( 10, "default");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"svt");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"region");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"support");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"fiducial");
		//cp.loadTable( SVTGeant4Factory.getCcdbPath() +"material");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"alignment");
		cp.disconnect();
		//System.out.println(cp.toString());
		//System.exit(0);
		
		SVTAlignmentFactory.setDataFiles( "survey_ideals_r3fix.dat", "survey_measured.dat" );
		SVTAlignmentFactory.processSurveyData( cp, "shifts_survey2.dat");
		//System.exit(0);
		
		
		/*SVTGeant4Factory.getConstants( cp );
		for( int k = 0; k < SVTGeant4Factory.convertRegionSectorFid2SurveyIndex( SVTGeant4Factory.NREGIONS, SVTGeant4Factory.NSECTORS[SVTGeant4Factory.NREGIONS-1], SVTGeant4Factory.NFIDUCIALS ); k++ )
		{
			int[] rsf = SVTGeant4Factory.convertSurveyIndex2RegionSectorFiducial( k );
			System.out.printf("k=%3d r=%d s=%2d f=%d\n", k, rsf[0], rsf[1], rsf[2]);
		}
		System.exit(0);*/
		// Utils.subArraySum( SVTGeant4Factory.NSECTORS, SVTGeant4Factory.NREGIONS )*SVTGeant4Factory.NFIDUCIALS
		
		int regionSelector = 1, sectorSelector = 6; 
		
		SVTGeant4Factory svtNominal = new SVTGeant4Factory( cp );
		svtNominal.setRange( regionSelector, sectorSelector, sectorSelector );
		svtNominal.makeVolumes();
		
		/*Geant4Basic module = svtNominal.createModule();
		module.setMother( svtNominal.getMotherVolume() );*/
		//Utils.shiftPosition( module, SVTGeant4Factory.MODULEWID/2, 0, SVTGeant4Factory.MODULELEN/2);
		
		//Geant4Basic region = svtNominal.createRegion( 0 );
		//region.setMother( svtNominal.getMotherVolume() );
		
		//for( int r = 0; r < SVTGeant4Factory.NREGIONS; r++ )
		//int i = 0;
		//int l = 1;
		/*int s = 4;
		
		for( int l = 1; l < 2; l++ )
		{
			//for( int s = 0; s < SVTGeant4Factory.NSECTORS[0]; s++ )
			//{			
				for( int i = 0; i < SVTGeant4Factory.NSTRIPS; i+=10 )
				{
					Line3D stripLine = svtNominal.getStrip( l, s, i );
					stripLine.show();
					Geant4Basic stripVol = Utils.createArrow("strip"+i+"_s"+s+"_l"+l, stripLine.toVector(), 0.5, 0.2, true, true, true );
					stripVol.setPosition( stripLine.origin().x()*0.1, stripLine.origin().y()*0.1, stripLine.origin().z()*0.1 );
					stripVol.setMother( svtNominal.getMotherVolume() );
					for( int c = 0; c < stripVol.getChildren().size(); c++ )
						System.out.println( stripVol.getChildren().get(c).gemcString() );
				}
			//}
		}*/
		
		String fileNameNominalFiducials = "factory_nominal_fiducials.dat";
		Writer fileNominalFiducials = Utils.openOutputDataFile( fileNameNominalFiducials );
		
		for( int region = svtNominal.getRegionMin()-1; region < svtNominal.getRegionMax(); region++ ) // SVTGeant4Factory.NREGIONS
			for( int sector = svtNominal.getSectorMin()[region]-1; sector < svtNominal.getSectorMax()[region]; sector++ ) // SVTGeant4Factory.NSECTORS[region]
			{
				Point3D fidPos3Ds[] = SVTGeant4Factory.getNominalFiducials( region, sector );
				
				for( int fid = 0; fid < SVTGeant4Factory.NFIDUCIALS; fid++ )
				{
					Utils.outputLine( fileNominalFiducials, String.format("R%dS%02dF%02d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() ) );
					
					Geant4Basic fidBall = new Geant4Basic("fiducialBall"+fid+"_s"+sector+"_r"+region, "Orb", 0.2 ); // cm
					fidBall.setPosition( fidPos3Ds[fid].x()*0.1, fidPos3Ds[fid].y()*0.1, fidPos3Ds[fid].z()*0.1 ); // mm->cm
					fidBall.setMother( svtNominal.getMotherVolume() );
				}
				
				Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
				Vector3D fidVec3D = fidTri3D.normal().asUnit();
				fidVec3D.scale( 10.0 ); // length of arrow in mm
				Geant4Basic fidCen = Utils.createArrow( "fiducialCenter_s"+sector+"_r"+region, fidVec3D, 2.0, 1.0, true, true, false );
				fidCen.setPosition( fidTri3D.center().x()*0.1, fidTri3D.center().y()*0.1, fidTri3D.center().z()*0.1 );
				fidCen.setMother( svtNominal.getMotherVolume() );
			}
		
		Utils.closeOutputDataFile( fileNameNominalFiducials, fileNominalFiducials );
		
		//System.out.println( svtNominal.toString() );
		
		IGdmlExporter gdmlFile = VolumeExporterFactory.createGdmlExporter();
		//gdmlFile.setVerbose( true ); // not useful for large numbers of volumes
		gdmlFile.setPositionLoc("local");
		gdmlFile.setRotationLoc("local");
		gdmlFile.addTopVolume( svtNominal.getMotherVolume() );
		gdmlFile.addMaterialPreset("mat_sensorActive", "mat_vacuum");
		gdmlFile.replaceAttribute( "structure", "volume", "name", "vol_sensorActive", "materialref", "ref", "mat_sensorActive");
		gdmlFile.replaceAttribute( "structure", "volume", "name", "vol_deadZone", "materialref", "ref", "mat_sensorActive");
		//gdmlFile.replaceAttribute( "structure", "volume", "name", "vol_rohacell", "materialref", "ref", "mat_rohacell");
		gdmlFile.writeFile("svtFactory_nominal");
		
		
		
		SVTGeant4Factory svtShifted = new SVTGeant4Factory( cp );
		//svtShifted.setAlignmentShift( 10, 100 );
		//svtShifted.setAlignmentShift("ccdb");
		//svtShifted.setAlignmentShift("shifts_survey2.dat");
		//svtShifted.setAlignmentShift("shifts_custom.dat");
		svtShifted.setAlignmentShift( cp );
		svtShifted.setRange( regionSelector, sectorSelector, sectorSelector );
		svtShifted.makeVolumes();
		
		/*for( int l = 0; l < 2; l++ )
		{
			//for( int s = 0; s < SVTGeant4Factory.NSECTORS[0]; s++ )
			//{			
				for( int i = 0; i < SVTGeant4Factory.NSTRIPS; i+=10 )
				{
					Line3D stripLine = svtShifted.getStrip( l, s, i );		
					Geant4Basic stripVol = Utils.createArrow("strip"+i+"_s"+s+"_l"+l, stripLine.toVector(), 0.5, 0.2, true, true, false );
					stripVol.setPosition( stripLine.origin().x(), stripLine.origin().y(), stripLine.origin().z() );
					//Utils.scale( stripVol, 0.1 ); // mm -> cm
					stripVol.setMother( svtShifted.getMotherVolume() );
				}
			//}
		}*/
		
		//System.out.println( svtShifted.toString() );
		
		IGdmlExporter gdmlFile2 = VolumeExporterFactory.createGdmlExporter();
		gdmlFile2.setPositionLoc("local");
		gdmlFile2.setRotationLoc("local");	
		gdmlFile2.addTopVolume( svtShifted.getMotherVolume() );
		gdmlFile2.addMaterialPreset("mat_sensorActive", "mat_vacuum");
		gdmlFile2.replaceAttribute( "structure", "volume", "name", "vol_sensorActive", "materialref", "ref", "mat_sensorActive");
		gdmlFile2.replaceAttribute( "structure", "volume", "name", "vol_deadZone", "materialref", "ref", "mat_sensorActive");
		gdmlFile2.replaceAttribute( "structure", "volume", "name", "vol_rohacell", "materialref", "ref", "mat_rohacell");
		gdmlFile2.writeFile("svtFactory_survey");
		
		String fileNameShiftedFiducials = "factory_shifted_fiducials.dat";
		Writer fileShiftedFiducials = Utils.openOutputDataFile( fileNameShiftedFiducials );
		
		for( int region = 0; region < 1; region++ ) // SVTGeant4Factory.NREGIONS
			for( int sector = 5; sector < 6; sector++ ) // SVTGeant4Factory.NSECTORS[region]
			{
				//System.out.println("r"+region+"s"+sector+"k"+SVTGeant4Factory.convertRegionSector2SvtIndex( region, sector ));
				//Point3D fidPos3Ds[] = svtShifted.getShiftedFiducials( region, sector );
				
				Point3D[] fidNPos3Ds = SVTGeant4Factory.getNominalFiducials( region, sector ); // lab frame
				Triangle3D fidNTri3D = new Triangle3D( fidNPos3Ds[0], fidNPos3Ds[1], fidNPos3Ds[2] );
				double [] shift = svtShifted.getShiftData()[SVTGeant4Factory.convertRegionSector2SvtIndex( region, sector )].clone();
				Point3D[] fidPos3Ds = new Point3D[SVTGeant4Factory.NFIDUCIALS];
				
				int n = 1;
				double d = shift[6]/n;
				for( int i = 1; i < n+1; i++ )
				{
					System.out.println("fid "+ i );
					shift[6] = i*d;
										
					for( int fid = 0; fid < SVTGeant4Factory.NFIDUCIALS; fid++ )
					{
						fidPos3Ds[fid] = new Point3D( fidNPos3Ds[fid] );  // reset for next step
						svtShifted.applyShift( fidPos3Ds[fid], shift, fidNTri3D.center() );	
						//Utils.outputLine( fileShiftedFiducials, String.format("R%dS%02dF%02d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() ) );
						
						if( i == n )
						{
							Utils.outputLine( fileShiftedFiducials, String.format("R%dS%02dF%02d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() ) );
							
							if( fid == SVTGeant4Factory.NFIDUCIALS-1 )
							{
								Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
								Vector3D fidVec3D = fidTri3D.normal().asUnit();
								fidVec3D.scale( 10 );
								Geant4Basic fidCen = Utils.createArrow( "fiducialCenter_s"+sector+"_r"+region, fidVec3D, 2.0, 1.0, true, true, false );
								fidCen.setPosition( fidTri3D.center().x()*0.1, fidTri3D.center().y()*0.1, fidTri3D.center().z()*0.1 );
								fidCen.setMother( svtShifted.getMotherVolume() );
							}
						}
						
						Geant4Basic fidBall = new Geant4Basic("fiducialBall"+fid+"_s"+sector+"_r"+region+"_"+i, "Orb", 0.2 ); // cm
						//Geant4Basic fidBall = new Geant4Basic("fiducialBall"+fid+"_s"+sector+"_r"+region, "Orb", 0.2 ); // cm
						fidBall.setPosition( fidPos3Ds[fid].x()*0.1, fidPos3Ds[fid].y()*0.1, fidPos3Ds[fid].z()*0.1 ); // mm -> cm
						fidBall.setMother( svtShifted.getMotherVolume() );
					}
				}
			}
		
		Utils.closeOutputDataFile( fileNameShiftedFiducials, fileShiftedFiducials );
		
		
		
		Geant4Basic svtMerge = new Geant4Basic("merge", "Box", 0 );
		svtShifted.appendName("_shifted");
		svtNominal.getMotherVolume().setMother( svtMerge );
		svtShifted.getMotherVolume().setMother( svtMerge );
		
		IGdmlExporter gdmlFile3 = VolumeExporterFactory.createGdmlExporter();
		gdmlFile3.setPositionLoc("local");
		gdmlFile3.setRotationLoc("local");
		gdmlFile.addTopVolume( svtNominal.getMotherVolume() );
		gdmlFile3.addTopVolume( svtMerge );
		//gdmlFile3.setVerbose( true );
		gdmlFile3.addMaterialPreset("mat_shifted", "mat_vacuum");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "_shifted", "materialref", "ref", "mat_shifted");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "fiducial", "materialref", "ref", "mat_shifted");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "sectorBall", "materialref", "ref", "mat_shifted");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "rotAxis", "materialref", "ref", "mat_shifted");
		gdmlFile3.writeFile("svtFactory_merge");
		
		System.out.println("done");
	}

}
