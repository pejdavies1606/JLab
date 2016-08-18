package Alignment;

import java.io.OutputStream;
import java.io.PrintStream;

import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

import Misc.Matrix;
import Misc.Util;

/**
 * <h1> Geometry Alignment </h1>
 * 
 * Universal class for processing and applying alignment shifts to points and volumes.
 * 
 * @author pdavies
 * @version 0.0.1
 */
public class AlignmentFactory
{
	public static int NSHIFTDATARECLEN = 7;
	
	/**
	 * Calculates alignment shifts between two sets of fiducial data.
	 * 
	 * @param dataLen total number of data points (where each point is 3 Cartesian coordinates)
	 * @param dataNominal first data set
	 * @param dataMeasured second data set
	 * @return dataShifts alignment shifts relative to dataNominal 
	 */
	public static double[][] calcShifts( int dataLen, double[][] dataNominal, double[][] dataMeasured )
	{
		if( dataNominal == null ){ throw new IllegalArgumentException("no data"); }
		if( dataMeasured == null ){ throw new IllegalArgumentException("no data"); }
		if( dataLen < 1 ){ throw new IllegalArgumentException("no info"); }
		
		double[][] dataShifts = new double[dataLen][];
		
		for( int k = 0; k < dataLen; k++ )
		{
			Point3D[] fidNominalPos3Ds = new Point3D[3]; // 3 points in a triangle
			Point3D[] fidMeasuredPos3Ds = new Point3D[3]; // 3 points in a triangle
			Point3D[] fidDeltas = new Point3D[3];

			for( int f = 0; f < 3; f++ ) // 3 points in a triangle
			{
				fidNominalPos3Ds[f] = new Point3D( dataNominal[k*3+f][0], dataNominal[k*3+f][1], dataNominal[k*3+f][2] );
				fidMeasuredPos3Ds[f] = new Point3D( dataMeasured[k*3+f][0], dataMeasured[k*3+f][1], dataMeasured[k*3+f][2] );
				fidDeltas[f] = fidNominalPos3Ds[f].vectorTo( fidMeasuredPos3Ds[f] ).toPoint3D();
			}

			Triangle3D fidNominalTri3D = new Triangle3D( fidNominalPos3Ds[0], fidNominalPos3Ds[1], fidNominalPos3Ds[2] );
			Triangle3D fidMeasuredTri3D = new Triangle3D( fidMeasuredPos3Ds[0], fidMeasuredPos3Ds[1], fidMeasuredPos3Ds[2] );

			// find shift for position
			Point3D fidNominalCenPos3D = fidNominalTri3D.center(); // average of 3 points
			Point3D fidMeasuredCenPos3D = fidMeasuredTri3D.center();
			Vector3D fidDiffVec3D = fidNominalCenPos3D.vectorTo( fidMeasuredCenPos3D );

			// find shift for rotation, about nominal triangle center
			Vector3D fidNominalVec3D = fidNominalTri3D.normal().asUnit();
			Vector3D fidMeasuredVec3D = fidMeasuredTri3D.normal().asUnit();
			double[] axisAngle = Util.convertVectorDiffToAxisAngle( fidNominalVec3D, fidMeasuredVec3D );
			
			//Triangle3D fidDeltaTri3D = new Triangle3D( fidDeltas[0], fidDeltas[1], fidDeltas[2] );
			//Point3D fidDeltaCen = fidDeltaTri3D.center();
			
			if( true )
			{
				System.out.println("k "+k );
				for( int f = 0; f < 3; f++ )
				{
					System.out.printf("NP%d % 8.3f % 8.3f % 8.3f", f, fidNominalPos3Ds[f].x(), fidNominalPos3Ds[f].y(), fidNominalPos3Ds[f].z() );
					System.out.printf("    MP%d % 8.3f % 8.3f % 8.3f", f, fidMeasuredPos3Ds[f].x(), fidMeasuredPos3Ds[f].y(), fidMeasuredPos3Ds[f].z() );
					System.out.printf("    D%d % 8.3f % 8.3f % 8.3f", f, fidDeltas[f].x(), fidDeltas[f].y(), fidDeltas[f].z() );
					System.out.println();
				}
				System.out.printf("NC  % 8.3f % 8.3f % 8.3f", fidNominalCenPos3D.x(), fidNominalCenPos3D.y(), fidNominalCenPos3D.z() );
				System.out.printf("    MC  % 8.3f % 8.3f % 8.3f", fidMeasuredCenPos3D.x(), fidMeasuredCenPos3D.y(), fidMeasuredCenPos3D.z() );
				//System.out.printf("    DC  % 8.3f % 8.3f % 8.3f", fidDeltaCen.x(), fidDeltaCen.y(), fidDeltaCen.z() );
				System.out.println();
				System.out.printf("NV  % 8.3f % 8.3f % 8.3f", fidNominalVec3D.x(), fidNominalVec3D.y(), fidNominalVec3D.z() );
				System.out.printf("    MV  % 8.3f % 8.3f % 8.3f", fidMeasuredVec3D.x(), fidMeasuredVec3D.y(), fidMeasuredVec3D.z() );
				System.out.println();
				System.out.printf("ST  % 8.3f % 8.3f % 8.3f", fidDiffVec3D.x(), fidDiffVec3D.y(), fidDiffVec3D.z() );
				System.out.printf("    SR  % 8.3f % 8.3f % 8.3f % 8.3f", axisAngle[0], axisAngle[1], axisAngle[2], Math.toDegrees(axisAngle[3]) );
				System.out.println();
			}
			
			dataShifts[k] = new double[]{ fidDiffVec3D.x(), fidDiffVec3D.y(), fidDiffVec3D.z(), axisAngle[0], axisAngle[1], axisAngle[2], axisAngle[3] };
		}
		return dataShifts;
	}
	
	
	/**
	 * Calculates the difference between two sets of fiducial data of the same size.
	 * @param dataLen number of data points
	 * @param dataWid number of elements in each data point
	 * @param dataNominal first data set
	 * @param dataMeasured second data set
	 * @return dataDeltas point difference relative to dataNominal
	 */
	public static double[][] calcDeltas( int dataLen, int dataWid, double[][] dataNominal, double[][] dataMeasured )
	{
		if( dataNominal == null ){ throw new IllegalArgumentException("no data"); }
		if( dataMeasured == null ){ throw new IllegalArgumentException("no data"); }
		if( dataLen == 0 || dataWid == 0 ){ throw new IllegalArgumentException("no info"); }
		
		double[][] dataDeltas = new double[dataLen][dataWid];
		
		for( int j = 0; j < dataLen; j++ )
		{
			for( int i = 0; i < dataWid; i++ )
			{
				dataDeltas[j][i] = dataMeasured[j][i] - dataNominal[j][i];
				System.out.printf("% 8.3f - % 8.3f = % 8.3f\n", dataMeasured[j][i], dataNominal[j][i], dataDeltas[j][i] );
			}
		}
		
		return dataDeltas;
	}
	
	
	
	public static double[][] applyShift( double[][] aData, double[][] aShift, double[][] aCenterData, double aScaleT, double aScaleR )
	{
		double[][] aShiftedData = aData.clone();
		for( int j = 0; j < aData.length/3; j+=3 )
			for( int i = 0; i < 3; i++ )
			{
				Point3D pos = new Point3D( aData[j*3+i][0], aData[j*3+i][1], aData[j*3+i][2] );
				applyShift( pos, aShift[j], new Point3D( aCenterData[j][0], aCenterData[j][1], aCenterData[j][2] ), aScaleT, aScaleR );
				aShiftedData[j*3+i][0] = pos.x();
				aShiftedData[j*3+i][1] = pos.y();
				aShiftedData[j*3+i][2] = pos.z();
				
			}
		return aShiftedData;
	}
	
	
	/**
	 * Applies the given alignment shift to the given point.
	 * 
	 * @param aPoint a point in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 * @param aScaleT a scale factor for the translation shift
	 * @param aScaleR a scale factor for the rotation shift
	 * @throws IllegalArgumentException incorrect number of elements in shift array
	 */
	public static void applyShift( Point3D aPoint, double[] aShift, Point3D aNominalCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
	{
		if( aShift.length != NSHIFTDATARECLEN ){ throw new IllegalArgumentException("shift array must have "+NSHIFTDATARECLEN+" elements"); }
		
		double tx = aShift[0]; // The Java language has references but you cannot dereference the memory addresses like you can in C++.
		double ty = aShift[1]; // The Java runtime does have pointers, but they're not accessible to the programmer. (no pointer arithmetic)
		double tz = aShift[2];
		double rx = aShift[3];
		double ry = aShift[4];
		double rz = aShift[5];
		double ra = aShift[6];
		
		//for( int i = 0; i < 3; i++ )
			//aShift[i] = aScaleT/10.0*aShift[i]; // does this also change tx, ty ,tz?
		
		tx *= aScaleT;
		ty *= aScaleT;
		tz *= aScaleT;
		ra  = aScaleR*ra;
		
		System.out.println();
		System.out.printf("PN: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
		System.out.printf("ST: % 8.3f % 8.3f % 8.3f\n", tx, ty, tz );
		System.out.printf("SR: % 8.3f % 8.3f % 8.3f % 8.3f\n", rx, ry, rz, Math.toDegrees(ra) );
		System.out.printf("SC: % 8.3f % 8.3f % 8.3f\n", aNominalCenter.x(), aNominalCenter.y(), aNominalCenter.z() );
				
		if( !(ra < 1E-3) )
		{
			Vector3D centerVec = aNominalCenter.toVector3D();
			
			centerVec.scale( -1 ); // reverse translation
			aPoint.set( aPoint, centerVec ); // move origin to center of rotation axis
			
			//System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
			
			Vector3D vecAxis = new Vector3D( rx, ry, rz ).asUnit();
			//System.out.printf("SR: % 8.3f % 8.3f % 8.3f % 8.3f\n", vecAxis.x(), vecAxis.y(), vecAxis.z(), Math.toDegrees(ra) );
			
			// http://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
			PrintStream originalStream = System.out;
			PrintStream dummyStream    = new PrintStream( new OutputStream(){ public void write(int b) {} } );
			
			System.setOut(dummyStream); // suppress unwanted debug output from Vector3D.rotate()
			vecAxis.rotate( aPoint, ra );
			System.setOut(originalStream);
			
			//System.out.printf("PR: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
			
			centerVec.scale( -1 ); // reverse translation
			aPoint.set( aPoint, centerVec );
			
			//System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
		}
		
		Vector3D translationVec = new Vector3D( tx, ty, tz );
		aPoint.set( aPoint, translationVec );
		
		System.out.printf("PS: % 8.3f % 8.3f % 8.3f\n", aPoint.x(), aPoint.y(), aPoint.z() );
	}
	
	
	/**
	 * Applies the given alignment shift to the given volume.
	 * 
	 * @param aVol a volume in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 * @param aScaleT a scale factor for the translation shift
	 * @param aScaleR a scale factor for the rotation shift
	 * @throws IllegalArgumentException incorrect number of elements in shift array
	 */
	public static void applyShift( Geant4Basic aVol, double[] aShift, Point3D aNominalCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
	{
		if( aShift.length != AlignmentFactory.NSHIFTDATARECLEN ){ throw new IllegalArgumentException("shift array must have "+AlignmentFactory.NSHIFTDATARECLEN+" elements"); }
		
		double rx = aShift[3];
		double ry = aShift[4];
		double rz = aShift[5];
		double ra = aShift[6];
		
		//System.out.println( aVol.gemcString() );
		
		ra  = aScaleR*Math.toRadians(ra);
		
		Point3D pos = new Point3D( aVol.getPosition()[0]*10, aVol.getPosition()[1]*10, aVol.getPosition()[2]*10 ); // cm -> mm
		applyShift( pos, aShift, aNominalCenter, aScaleT, aScaleR );
		aVol.setPosition( pos.x()*0.1, pos.y()*0.1, pos.z()*0.1 );
		
		double[] rot = aVol.getRotation();
		Matrix rotMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -rot[0], -rot[1], -rot[2] ); // Geant = passive/alias, Java = active/alibi
		/*System.out.println("geant matrix "+ Math.toDegrees(-rot[0]) +" "+ Math.toDegrees(-rot[1]) +" "+ Math.toDegrees(-rot[2]) );
		rotMatrix.show();
		
		Vector3D[] vr = new Vector3D[3];
		for( int i = 0; i < vr.length; i++ )
		{
			switch( i )
			{
			case 0:
				vr[i] = new Vector3D( 1, 0, 0 ); break;
			case 1:
				vr[i] = new Vector3D( 0, 1, 0 ); break;
			case 2:
				vr[i] = new Vector3D( 0, 0, 1 ); break;
			}
			System.out.println("vector "+i+" matrix rotated"); Matrix.matMul( rotMatrix, new Matrix(3, 1, Utils.toDoubleArray( vr[i] ) ) ).show();
		}*/
		
		
		// this does not work
		Vector3D vrs = new Vector3D( rx, ry, rz ).asUnit();
		Matrix shiftMatrix = Matrix.convertRotationAxisAngleToMatrix( new double[]{ vrs.x(), vrs.y(), vrs.z(), ra } );
		rotMatrix = Matrix.matMul( shiftMatrix, rotMatrix );
		rot = Matrix.convertRotationToEulerInXYZ_ExZYX( rotMatrix );
		aVol.setRotation("xyz", -rot[0], -rot[1], -rot[2] );
		
		/*rotMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -rot[0], -rot[1], -rot[2] ); // Geant = passive/alias, Java = active/alibi
		System.out.println("geant matrix "+ Math.toDegrees(-rot[0]) +" "+ Math.toDegrees(-rot[1]) +" "+ Math.toDegrees(-rot[2]) );
		rotMatrix.show();
		
		for( int i = 0; i < vr.length; i++ )
		{
			switch( i )
			{
			case 0:
				vr[i] = new Vector3D( 1, 0, 0 ); break;
			case 1:
				vr[i] = new Vector3D( 0, 1, 0 ); break;
			case 2:
				vr[i] = new Vector3D( 0, 0, 1 ); break;
			}
			System.out.println("vector "+i+" matrix rotated"); Matrix.matMul( rotMatrix, new Matrix(3, 1, Utils.toDoubleArray( vr[i] ) ) ).show();
		}*/
		
		/*Vector3D oldVec = new Vector3D( aVol.getPosition()[0], aVol.getPosition()[1], aVol.getPosition()[2] );
		//System.out.println(aVol.gemcString());
		//oldVec.show();
		Vector3D shiftVec = new Vector3D( tx, ty, tz );
		//shiftVec.show();
		shiftVec.scale( 0.1 ); // mm -> cm
		//shiftVec.show();
		Vector3D newVec = oldVec.add( shiftVec );
		//newVec.show();
		aVol.setPosition( newVec.x(), newVec.y(), newVec.z() );
		
		double[] oldRot = aVol.getRotation().clone();
		Matrix oldMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -oldRot[0], -oldRot[1], -oldRot[2] ); // Geant = passive/alias, Java = active/alibi
		Matrix shiftMatrix = Matrix.convertRotationAxisAngleToMatrix( new double[]{ rx, ry, rz, ra } );
		Matrix newMatrix = Matrix.matMul( shiftMatrix, oldMatrix );
		double[] newRot = Matrix.convertRotationToEulerInXYZ_ExZYX( newMatrix );
		aVol.setRotation("xyz", -newRot[0], -newRot[1], -newRot[2] );*/
		
		//System.out.printf("oldRot % 8.3f % 8.3f % 8.3f\n", -Math.toDegrees(oldRot[0]), -Math.toDegrees(oldRot[1]), -Math.toDegrees(oldRot[2]));
	}
}
