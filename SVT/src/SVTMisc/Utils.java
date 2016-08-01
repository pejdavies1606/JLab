package SVTMisc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class Utils
{	
	/*public static double[] calcDistanceWithUncertainty( double uc, Point3D p1, Point3D p0 )
	{
		double d = p1.distance(p0);
		//double d = Math.sqrt( Math.pow(p1.x() - p0.x(),2) + Math.pow(p1.y() - p0.y(),2) + Math.pow(p1.z() - p0.z(),2) );
		double Dd = Utils.calcUncertaintyDistance( uc, p1, p0 );
		double dL = d - Dd;
		double dH = d + Dd;
		return new double[]{ d, dH, dL, Dd };
	}*/
	
	
	
	public static double calcUncertaintyDistance( double uc, Point3D p1, Point3D p0 )
	{
		double u = Math.pow(p1.x() - p0.x(),2) + Math.pow(p1.y() - p0.y(),2) + Math.pow(p1.z() - p0.z(),2);		
		return Math.sqrt( 6*Math.pow(uc,2)*Math.sqrt(u) );
	}
	
	
	
	public static void appendName( Geant4Basic m, String aTag )
	{
		m.setName( m.getName() + aTag );
		appendChildrenName( m, aTag );
	}
	
	
	
	public static void appendChildrenName( Geant4Basic aVol, String aTag )
	{
		List<Geant4Basic> children = aVol.getChildren();
		for( int i = 0; i < children.size(); i++ )
		{
			children.get(i).setName( children.get(i).getName() + aTag );
			appendChildrenName( children.get(i), aTag ); // tail recursive
		}
	}
	
	
	
	public static Geant4Basic clone( Geant4Basic a, Geant4Basic m )
	{
		Geant4Basic b = new Geant4Basic( a.getName(), a.getType(), a.getParameters() );
		b.setPosition( a.getPosition()[0], a.getPosition()[1], a.getPosition()[2]);
		b.setRotation( a.getRotationOrder(), a.getRotation()[0], a.getRotation()[1], a.getRotation()[2] );
		//b.setMother( m );
		for( int i = 0; i < a.getChildren().size(); i++ )
		{
			b.getChildren().add( clone( a.getChildren().get(i), b ) ); // recursive
		}
		return b;
	}
	
	
	
	public static Matrix toMatrix( Vector3D v )
	{
		return new Matrix( 3, 1, new double[]{ v.x(), v.y(), v.z() } ); // column vector
	}
	
	
	
	public static Vector3D toVector3D( Matrix m )
	{
		if( !(m.nRows == 3 && m.nCols == 1 ) ) return null;
		return new Vector3D( m.getData()[0], m.getData()[1], m.getData()[2] );
	}
	
	
	
	public static double[] toDoubleArray( Vector3D aVec )
	{
		return new double[]{ aVec.x(), aVec.y(), aVec.z() };
	}
	
	
	
	public static Vector3D toVector3D( double[] aArray )
	{
		if( aArray.length == 3 )
			return new Vector3D( aArray[0], aArray[1], aArray[2] );
		else
			return null;
	}
	
	
	
	public static Writer openOutputDataFile( String aName )
	{
		Writer file = null;
		try
		{
			file = new BufferedWriter( new FileWriter( aName ) );
			System.out.println("opened \""+ aName +"\"");
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return file;
	}
	
	
	
	public static void outputLine( Writer aWriter, String aLine )
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
	
	
	
	public static void closeOutputDataFile( String aName, Writer aFile )
	{
		if( aFile != null )
		{
			try
			{
				aFile.close();
				System.out.println("closed \""+ aName +"\"");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
		else
		{
			System.out.println("\""+ aName +"\" is already closed");
		}
	}
	
	
	
	public static Geant4Basic createArrow( String aName, Vector3D aVec,
			double aRefBallRadius, double aPointerRadius, boolean aDisplayBallStart, boolean aDisplayPointer, boolean aDisplayBallEnd )
	{
		Geant4Basic arrowVol = new Geant4Basic(aName+"_arrow0", "Box", 0 ); // container
		// put reference ball at base of vector, with arrow pointing in direction of vector, and an optional second reference ball at the end of the arrow
		
		//System.out.printf("arrow vector x=% 8.3f y=% 8.3f z=% 8.3f mag=% 8.3f\n", aVec.x(), aVec.y(), aVec.z(), aVec.mag() );
		
		if( aDisplayBallStart )
		{
			Geant4Basic refBallStartVol = new Geant4Basic( aName+"_arrow1", "orb", aRefBallRadius*0.1 );
			refBallStartVol.setMother( arrowVol ); // origin of a Line3D
		}
		if( aDisplayPointer )
		{
			Geant4Basic pointerVol = new Geant4Basic( aName+"_arrow2", "eltube", aPointerRadius*0.1, aPointerRadius*0.1, aVec.mag()/2*0.1 );
			pointerVol.setMother( arrowVol );
			
			double[] eulerAngles = Utils.convertRotationVectorToGeant( aVec.theta(), aVec.phi() );
			pointerVol.setRotation("xyz", -eulerAngles[0], -eulerAngles[1], -eulerAngles[2] );
			
			// shift centre of geometry of arrow to put first end at start ball
			pointerVol.setPosition( aVec.divide(2).x()*0.1, aVec.divide(2).y()*0.1, aVec.divide(2).z()*0.1 );
		}
		if( aDisplayBallEnd )
		{
			Geant4Basic refBallEndVol = new Geant4Basic( aName+"_arrow3", "orb", aRefBallRadius*0.1 );
			refBallEndVol.setPosition( aVec.x()*0.1, aVec.y()*0.1, aVec.z()*0.1 );
			refBallEndVol.setMother( arrowVol );
		}
		
		return arrowVol;
	}
	
	
	
	public static Matrix convertRotationVectorToMatrix( double theta, double phi )
	{
		// standard Vector rotation formalism is identical to Euler_InZYX_ExXYZ( 0.0, theta, phi )
		// first, rotate about the X-axis by zero angle (for all vectors), 
		// then rotate about the Y-axis by angle theta, 
		// and finally rotate about the Z-axis by angle phi
		//return Matrix.matMul( Matrix.rotateZ( phi ), Matrix.rotateY( theta ) ); // col-based matrices multiply right->left
		return Matrix.convertRotationFromEulerInZYX_ExXYZ( 0.0, theta, phi );
	}
	
	
	
	public static double[] convertRotationVectorToGeant( double theta, double phi )
	{
		return Matrix.convertRotationToEulerInXYZ_ExZYX( convertRotationVectorToMatrix( theta, phi ) );
	}
	
	
	
	public static double[] convertVectorDiffToEulerAxisAngle( Vector3D a, Vector3D b )
	{
		// http://math.stackexchange.com/questions/293116/rotating-one-3d-vector-to-another
		Vector3D eulerAxis = null;
		double eulerAngle = a.angle(b);
		
		double e = 1E-3;
		
		if( Math.abs(eulerAngle) < e )
		{
			return new double[]{ 0.0, 0.0, 0.0, 0.0 };
		}
		else if( Math.abs(Math.PI - eulerAngle) < e )
		{
			double[] c = new double[]{ a.x(), a.y(), a.z() };		
			int minLoc = 0; double minVal = c[minLoc];
			for( int i = 1; i < c.length; i++ ) if( c[i] < minVal ) { minLoc = i; minVal = c[i]; }
			
			Vector3D d = new Vector3D( 0.0, 0.0, 0.0 );
			switch( minLoc )
			{
			case 0:
				d.setX(1.0);
				break;
			case 1:
				d.setY(1.0);
				break;
			case 2:
				d.setZ(1.0);
				break;
			}
			eulerAxis = a.cross(d).asUnit();
		}
		else
		{
			eulerAxis = a.cross(b).asUnit();
		}
		
		return new double[]{ eulerAxis.x(), eulerAxis.y(), eulerAxis.z(), eulerAngle };
	}
	

	
	public static void scalePosition( Geant4Basic aVol, double aScale )
	{
		double[] p = aVol.getPosition();
		aVol.setPosition( p[0]*aScale, p[1]*aScale, p[2]*aScale );
		
		/*double[] d = aVol.getParameters();
		for( int i = 0; i < d.length; i++ )
			d[i] = aScale*d[i];
		aVol.setParameters( d );*/
		
		List<Geant4Basic> children = aVol.getChildren();
		for( int i = 0; i < children.size(); i++ )
		{
			scalePosition( children.get(i), aScale ); // tail recursive
		}
	}
	
	
	
	public static double[][] inputTaggedData( String aFilename, int recLen )
	{
		//System.out.println("_inputData()");
		//System.out.println("aFilename=\""+ aFilename +"\"");
		
		double[][] dataResult = null;
		boolean bVerbose = false;

		try
		{
			File file = new File( aFilename );
			Scanner scanner = new Scanner( file );
			
			//ArrayList<String> tagList = new ArrayList<String>();
			ArrayList<double[]> dataList = new ArrayList<double[]>();
			
			//System.out.println("dataList.size()="+ dataList.size() );
			
			int i = 0;
			while( scanner.hasNext() )
			{
				if( bVerbose ) System.out.print("i="+ i++ );
				String tag = scanner.next();
				if( bVerbose ) System.out.print(" tag=\""+ tag +"\"");
				//tagList.add( tag );
				double[] data = new double[recLen];
				if( bVerbose ) System.out.print(" data=");
				for( int j = 0; j < recLen; j++ )
				{
					data[j] = scanner.nextDouble();
					if( bVerbose ) System.out.printf(" % 8.3f", data[j] );
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
	
	
	
	public static void shiftPosition( Geant4Basic aVol, double aShiftX, double aShiftY, double aShiftZ )
	{
		double[] p = aVol.getPosition();
		aVol.setPosition( p[0] + aShiftX, p[1] + aShiftY, p[2] + aShiftZ );
	}
	
	
	
	public static int subArraySum( int[] aArray, int aIndex )
	{
		// sums elements of aArray from 0 to aIndex-1
		if( aIndex > 0 && aIndex < aArray.length+1 )
		{
			int[] subArray = new int[aIndex];
			for( int i = 0; i < aIndex; i++ )
				subArray[i] = aArray[i];
			return IntStream.of( subArray ).sum();
		}
		return 0;
	}
	
}
