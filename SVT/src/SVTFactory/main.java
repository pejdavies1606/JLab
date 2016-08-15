package SVTFactory;

import java.io.Writer;

import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

import Misc.*;
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
		
		
		/*Vector3D axis = new Vector3D( 0, 1, 1 ).asUnit();
		double angle = 180;
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
		System.exit(0);*/
		
		/*PrintStream originalStream = System.out; // http://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
		PrintStream dummyStream    =  new PrintStream( new OutputStream(){
		    public void write(int b) {
		        //NO-OP
		    }} );
					
		Point3D originPos = new Point3D( 0.0, 1.0, 0.0 );
		Plane3D fidPln0 = new Plane3D( new Point3D( originPos ), new Vector3D( 0.0, 1.0, 0.0 ) );
		Plane3D fidPln1 = new Plane3D( new Point3D( fidPln0.point().x() + 1.0, fidPln0.point().y(), fidPln0.point().z() ), new Vector3D( 0.0, 0.0, 1.0 ) );
		//double[] translationShift = Util.toDoubleArray( fidPln0.point().vectorTo( fidPln1.point() ) );
		//double[] rotationShift = Util.convertVectorDiffToAxisAngle( fidPln0.normal(), fidPln1.normal() );
		
		System.out.printf(" O: %5.1f %5.1f %5.1f\n", originPos.x(), originPos.y(), originPos.z() );
		System.out.printf("F0: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f\n", fidPln0.point().x(), fidPln0.point().y(), fidPln0.point().z(), fidPln0.normal().x(), fidPln0.normal().y(), fidPln0.normal().z() );
		System.out.printf("F1: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f\n", fidPln1.point().x(), fidPln1.point().y(), fidPln1.point().z(), fidPln1.normal().x(), fidPln1.normal().y(), fidPln1.normal().z() );
		//System.out.printf(" S: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f | %5.1f\n", translationShift[0], translationShift[1], translationShift[2], rotationShift[0], rotationShift[1], rotationShift[2], Math.toDegrees(rotationShift[3]) );
		
		Point3D arbPos0 = new Point3D( originPos.x(), originPos.y() + 1.0, originPos.z() + 1.0 );
		Point3D arbPos1 = new Point3D( arbPos0 );
		
		Vector3D translationVec = Util.toVector3D( translationShift );
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
		
		Geant4Basic fidPln0Vol = Util.createArrow("fidPln0", fidPln0.normal(), orbR*10, arrowR*10, true, true, false );
		fidPln0Vol.setPosition( fidPln0.point().x()*0.1, fidPln0.point().y()*0.1, fidPln0.point().z()*0.1 );
		fidPln0Vol.setMother( topVol );
		
		Geant4Basic fidPln1Vol = Util.createArrow("fidPln1", fidPln1.normal(), orbR*10, arrowR*10, true, true, false );
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
		
		/*double[][] nominalData = new double[][]{ new double[]{ 1,0,-1 }, new double[]{ -1,0,-1 }, new double[]{ 0,0,2 } };
		//double[] translationShift = new double[]{ 0,1,0 };
		//double[] rotationShift = new double[]{ 1,0,0,90 };
		double[][] measuredData = new double[][]{ new double[]{ 1,2,0 }, new double[]{ -1,2,0 }, new double[]{ 0,-1,0 } };
		
		double[][] shiftData = AlignmentFactory.calcShifts( 1, nominalData, measuredData );
		
		double[][] centerData = new double[nominalData.length/3][3];
		
		for( int j = 0; j < nominalData.length/3; j+=3 )
		{
			Triangle3D centerTri = new Triangle3D( new Point3D(nominalData[j+0][0], nominalData[j+0][1], nominalData[j+0][2]),
					new Point3D(nominalData[j+1][0], nominalData[j+1][1], nominalData[j+1][2]),
					new Point3D(nominalData[j+2][0], nominalData[j+2][1], nominalData[j+2][2]) );  
			centerData[j] = Util.toDoubleArray( centerTri.center().toVector3D() ); 
		}
		
		double[][] shiftedData = AlignmentFactory.applyShift(nominalData, shiftData, centerData, 1, 1 );
		
		double[][] deltasData = AlignmentFactory.calcDeltas( 1, 3, measuredData, shiftedData );
		
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
		
		/*SVTGeant4Factory.getConstants( cp );
		for( int l = 0; l < SVTGeant4Factory.NTOTALSECTORS; l++ )
		{
			int[] rs = SVTGeant4Factory.convertSvtIndex2RegionSector( l );
			System.out.printf("l %2d r %2d s %2d\n", l, rs[0], rs[1] );
		}
		System.exit(0);*/
		
		//SVTAlignmentFactory.setup( cp, "survey_ideals_reformat.dat", "survey_measured_reformat.dat" );
		//double[][] dataNominal = SVTGeant4Factory.getNominalFiducialData();
		//SVTAlignmentFactory.calcShifts( dataNominal, SVTAlignmentFactory.getDataSurveyMeasured(), "shifts_survey_measured_from_factory_nominal.dat" );
		////SVTAlignmentFactory.calcShifts( SVTAlignmentFactory.getDataSurveyIdeals(), SVTAlignmentFactory.getDataSurveyMeasured(), "shifts_survey_measured_from_survey_ideals.dat" );
		
		//SVTAlignmentFactory.calcDeltas( dataNominal, SVTAlignmentFactory.getDataSurveyIdeals(), "deltas_survey_ideals_from_factory_nominal.dat");
		//System.exit(0);
		
		
		
		//int regionSelector = 1, sectorSelector = 1;
		
		SVTGeant4Factory svtNominal = new SVTGeant4Factory( cp );
		//svtNominal.setRange( regionSelector, sectorSelector, sectorSelector );
		svtNominal.makeVolumes();
		
		/*Geant4Basic module = svtNominal.createModule();
		module.setMother( svtNominal.getMotherVolume() );*/
		//Utils.shiftPosition( module, SVTGeant4Factory.MODULEWID/2, 0, SVTGeant4Factory.MODULELEN/2);
		
		//Geant4Basic region = svtNominal.createRegion( 0 );
		//region.setMother( svtNominal.getMotherVolume() );
		
		String fileNameNominalFiducials = "factory_fiducials_nominal.dat";
		Writer fileNominalFiducials = Util.openOutputDataFile( fileNameNominalFiducials );
		
		for( int region = svtNominal.getRegionMin()-1; region < svtNominal.getRegionMax(); region++ )
			for( int sector = svtNominal.getSectorMin()[region]-1; sector < svtNominal.getSectorMax()[region]; sector++ )
			{
				/*for( int module = svtNominal.getModuleMin()-1; module < svtNominal.getModuleMax(); module++ )
					for( int strip = 0; strip < SVTGeant4Factory.NSTRIPS; strip+=50 )
					{
						Line3D stripLine = svtNominal.getStrip( region, sector, module, strip );
						//stripLine.show();
						Geant4Basic stripVol = Util.createArrow("strip"+strip+"_m"+module+"_s"+sector+"_r"+region, stripLine.toVector(), 0.5, 0.2, true, true, true );
						stripVol.setPosition( stripLine.origin().x()*0.1, stripLine.origin().y()*0.1, stripLine.origin().z()*0.1 );
						stripVol.setMother( svtNominal.getMotherVolume() );
						//System.out.println( stripVol.gemcString() );
						//for( int c = 0; c < stripVol.getChildren().size(); c++ )
							//System.out.println( stripVol.getChildren().get(c).gemcString() );
					}*/
				
				Point3D fidPos3Ds[] = SVTGeant4Factory.getNominalFiducials( region, sector );
				
				for( int fid = 0; fid < SVTGeant4Factory.NFIDUCIALS; fid++ )
				{
					Util.writeLine( fileNominalFiducials, String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() ) );
					
					Geant4Basic fidBall = new Geant4Basic("fiducialBall"+fid+"_s"+sector+"_r"+region, "Orb", 0.2 ); // cm
					fidBall.setPosition( fidPos3Ds[fid].x()*0.1, fidPos3Ds[fid].y()*0.1, fidPos3Ds[fid].z()*0.1 ); // mm->cm
					//fidBall.setMother( svtNominal.getMotherVolume() );
				}
				
				Triangle3D fidTri3D = new Triangle3D( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
				Vector3D fidVec3D = fidTri3D.normal().asUnit();
				fidVec3D.scale( 10 ); // length of arrow in mm
				Geant4Basic fidCen = Util.createArrow( "fiducialCenter_s"+sector+"_r"+region, fidVec3D, 2.0, 1.0, true, true, false );
				fidCen.setPosition( fidTri3D.center().x()*0.1, fidTri3D.center().y()*0.1, fidTri3D.center().z()*0.1 );
				//fidCen.setMother( svtNominal.getMotherVolume() );
			}
		
		Util.closeOutputDataFile( fileNameNominalFiducials, fileNominalFiducials );
		
		System.out.println( svtNominal.toString() );
		
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
		
		System.exit( 0 );
		
		
		
		SVTGeant4Factory svtShifted = new SVTGeant4Factory( cp );
		
		svtShifted.setAlignmentShift("shifts_survey_measured_from_factory_nominal.dat");
		//svtShifted.setAlignmentShift("shifts_survey_measured_from_ideals.dat");
		//svtShifted.setAlignmentShift("shifts_custom.dat");
		//svtShifted.setAlignmentShift("shifts_zero.dat");
		//svtShifted.setAlignmentShift( cp );
		//svtShifted.setAlignmentShiftScale( 100, 100 );
		
		//svtShifted.setRange( regionSelector, sectorSelector, sectorSelector );
		//svtShifted.makeVolumes();
		
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
		gdmlFile2.writeFile("svtFactory_shifted");
		
		String fileNameShiftedFiducials = "factory_fiducials_shifted.dat";
		Writer fileShiftedFiducials = Util.openOutputDataFile( fileNameShiftedFiducials );
		//String fileNameShiftedFiducialsDeltas = "factory_fiducials_shifted_deltas.dat";
		//Writer fileShiftedFiducialsDeltas = Util.openOutputDataFile( fileNameShiftedFiducialsDeltas );
		
		for( int region = svtShifted.getRegionMin()-1; region < svtShifted.getRegionMax(); region++ ) // SVTGeant4Factory.NREGIONS
			for( int sector = svtShifted.getSectorMin()[region]-1; sector < svtShifted.getSectorMax()[region]; sector++ ) // SVTGeant4Factory.NSECTORS[region]
			{
				//System.out.println("r"+region+"s"+sector+"k"+SVTGeant4Factory.convertRegionSector2SvtIndex( region, sector ));
				
				//Point3D fidPos3Ds[] = svtShifted.getShiftedFiducials( region, sector );
				
				// calculate shifted fiducials manually to show intermediate steps
				Point3D[] fidNominalPos3Ds = SVTGeant4Factory.getNominalFiducials( region, sector ); // lab frame
				Triangle3D fidNominalTri3D = new Triangle3D( fidNominalPos3Ds[0], fidNominalPos3Ds[1], fidNominalPos3Ds[2] );
				double [] shift = svtShifted.getAlignmentShiftData()[SVTGeant4Factory.convertRegionSector2SvtIndex( region, sector )].clone();
				Point3D[] fidShiftedPos3Ds = new Point3D[SVTGeant4Factory.NFIDUCIALS];
				
				int n = 1;
				double d = shift[6]/n;
				for( int i = 1; i < n+1; i++ )
				{
					//System.out.println("fid "+ i );
					shift[6] = i*d;
										
					for( int fid = 0; fid < SVTGeant4Factory.NFIDUCIALS; fid++ )
					{
						fidShiftedPos3Ds[fid] = new Point3D( fidNominalPos3Ds[fid] );  // reset for next step
						svtShifted.applyShift( fidShiftedPos3Ds[fid], shift, fidNominalTri3D.center() );	
						//Utils.outputLine( fileShiftedFiducials, String.format("R%dS%02dF%02d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidPos3Ds[fid].x(), fidPos3Ds[fid].y(), fidPos3Ds[fid].z() ) );
						
						if( i == n )
						{
							Util.writeLine( fileShiftedFiducials, String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidShiftedPos3Ds[fid].x(), fidShiftedPos3Ds[fid].y(), fidShiftedPos3Ds[fid].z() ) );
							
							//Vector3D fidDiffVec3D = fidShiftedPos3Ds[fid].vectorFrom( Util.toVector3D( SVTAlignmentFactory.getDataSurveyMeasured()[SVTGeant4Factory.convertRegionSectorFid2SurveyIndex(region, sector, fid)] ).toPoint3D() );
							//Util.writeLine( fileShiftedFiducialsDeltas, String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidDiffVec3D.x(), fidDiffVec3D.y(), fidDiffVec3D.z() ) );
							
							if( fid == SVTGeant4Factory.NFIDUCIALS-1 )
							{
								Triangle3D fidTri3D = new Triangle3D( fidShiftedPos3Ds[0], fidShiftedPos3Ds[1], fidShiftedPos3Ds[2] );
								Vector3D fidVec3D = fidTri3D.normal().asUnit();
								fidVec3D.scale( 10 );
								Geant4Basic fidCen = Util.createArrow( "fiducialCenter_s"+sector+"_r"+region, fidVec3D, 2.0, 1.0, true, true, false );
								fidCen.setPosition( fidTri3D.center().x()*0.1, fidTri3D.center().y()*0.1, fidTri3D.center().z()*0.1 );
								fidCen.setMother( svtShifted.getMotherVolume() );
							}
						}
						
						Geant4Basic fidBall = new Geant4Basic("fiducialBall"+fid+"_s"+sector+"_r"+region+"_"+i, "Orb", 0.2 ); // cm
						//Geant4Basic fidBall = new Geant4Basic("fiducialBall"+fid+"_s"+sector+"_r"+region, "Orb", 0.2 ); // cm
						fidBall.setPosition( fidShiftedPos3Ds[fid].x()*0.1, fidShiftedPos3Ds[fid].y()*0.1, fidShiftedPos3Ds[fid].z()*0.1 ); // mm -> cm
						fidBall.setMother( svtShifted.getMotherVolume() );
					}
				}
			}
		
		Util.closeOutputDataFile( fileNameShiftedFiducials, fileShiftedFiducials );
		//Util.closeOutputDataFile( fileNameShiftedFiducialsDeltas, fileShiftedFiducialsDeltas );
		
		
		
		Geant4Basic svtMerge = new Geant4Basic("merge", "Box", 0 );
		svtShifted.appendName("_shifted");
		svtNominal.getMotherVolume().setMother( svtMerge );
		svtShifted.getMotherVolume().setMother( svtMerge );
		
		IGdmlExporter gdmlFile3 = VolumeExporterFactory.createGdmlExporter();
		gdmlFile3.setPositionLoc("local");
		gdmlFile3.setRotationLoc("local");
		gdmlFile3.addTopVolume( svtMerge );
		//gdmlFile3.setVerbose( true );
		gdmlFile3.addMaterialPreset("mat_shifted", "mat_vacuum");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "_shifted", "materialref", "ref", "mat_shifted");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "fiducial", "materialref", "ref", "mat_shifted");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "sectorBall", "materialref", "ref", "mat_shifted");
		gdmlFile3.replaceAttribute( "structure", "volume", "name", "rotAxis", "materialref", "ref", "mat_shifted");
		gdmlFile3.writeFile("svtFactory_merge");
		
		
		
		// verify shifted fiducials against survey measured using alignment algorithm
		SVTAlignmentFactory.calcShifts( SVTAlignmentFactory.getDataSurveyMeasured(), svtShifted.getShiftedFiducialData(), "shifts_factory_shifted_from_survey_measured.dat" ); // zero ?
		SVTAlignmentFactory.calcDeltas( SVTAlignmentFactory.getDataSurveyMeasured(), svtShifted.getShiftedFiducialData(), "deltas_factory_shifted_from_survey_measured.dat"); // non-zero ?
		
		// verify zero shifts against factory nominal
		//SVTAlignmentFactory.calcShifts( SVTGeant4Factory.getNominalFiducialData(), svtShifted.getShiftedFiducialData(), "shifts_factory_shifted_from_factory_nominal_zero_shift.dat" );
		//SVTAlignmentFactory.calcDeltas( SVTGeant4Factory.getNominalFiducialData(), svtShifted.getShiftedFiducialData(), "deltas_factory_shifted_from_factory_nominal_zero_shift.dat" );
		
		System.out.println("done");
	}

}
