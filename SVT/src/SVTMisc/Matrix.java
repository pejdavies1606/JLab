package SVTMisc;

public class Matrix
{
	double[] data = null;
	int nRows = 0, nCols = 0;
	
	// array constructors make row-based matrices easier to create
	// Matrix m3 = new Matrix( new double[]{ 1, 2, 3,
	//										 4, 5, 6,
	//										 7, 8, 9 } );
	// but getting column vectors would mean reading non-continous memory... oh well
	// Matrix v1 = new Matrix( new double[]{ m3.getData[0],
	//										 m3.getData[3],
	//										 m3.getData[6] } );
	
	public Matrix( int aNRows, int aNCols, double[] aData ) throws NullPointerException, IllegalArgumentException
	{
		if( aData == null ) throw new NullPointerException("aMatrix is null");
		if( aNRows < 0 || aNCols < 0 ) throw new IllegalArgumentException("aNRows and aNCols are not positive");
		
		if( aData.length == aNRows*aNCols )
		{
			this.data = aData; this.nRows = aNRows; this.nCols = aNCols;
		}
		else throw new IllegalArgumentException("aData is not a valid Matrix");
	}
	
	public Matrix( int aNRows, int aNCols )
	{
		if( aNRows < 0 || aNCols < 0 ) throw new IllegalArgumentException("aNRows and aNCols are not positive");
		this.data = new double[aNRows*aNCols];
		this.nRows = aNRows; this.nCols = aNCols;
	}
	
	
	
	public double[] getData()
	{
		return this.data;
	}
	
	
	@Override
	public Matrix clone()
	{
		return new Matrix( this.nRows, this.nCols, this.data.clone() );
	}
	
	
	
	public boolean verify()
	{
		return ( this.data.length == this.nRows*this.nCols );
	}
	
	
	
	public void show( String s )
	{
		System.out.println(s);
		show();
	}
	
	
	
	public void show()
	{
		if( data == null ) throw new NullPointerException("nothing to show");
		for( int i = 0; i < data.length; i++ )
		{
			System.out.printf("% 8.3f", this.getData()[i] );
			if( (i+1) % nCols == 0 )
				System.out.println();
			else
				System.out.printf(" ");
		}
	}
	
	
	
	public static Matrix rotateX( double angle )
	{
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);
		return new Matrix( 3, 3, new double[]{ 1.0, 0.0, 0.0,
											   0.0,  ca, -sa,
											   0.0,  sa,  ca } ); 
	}

	public static Matrix rotateY( double angle )
	{
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);
		return new Matrix( 3, 3, new double[]{  ca, 0.0,  sa,
											   0.0, 1.0, 0.0,
											   -sa, 0.0,  ca } );
	}

	public static Matrix rotateZ( double angle )
	{
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);
		return new Matrix( 3, 3, new double[]{  ca, -sa, 0.0,
											    sa,  ca, 0.0,
											   0.0, 0.0, 1.0 } ); 
	}
	
	
	
	public static Matrix convertRotationAxisAngleToMatrix( double[] aAxisAngle )
	{
		double u = aAxisAngle[0];
		double v = aAxisAngle[1];
		double w = aAxisAngle[2];
		double t = aAxisAngle[3]; // must be radians
		double c = Math.cos(t);
		double s = Math.sin(t);
		
		/*Matrix m3 = new Matrix(3,3, new double[]{    c + u*u*(1-c),  w*s + v*u*(1-c), -v*s + w*u*(1-c),
												    -w*s + u*v*(1-c),    c + v*v*(1-c),  u*s + w*v*(1-c),
												     v*s + u*v*(1-c), -u*s + u*v*(1-c),    c + u*v*(1-c) } );*/
		
		// http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToMatrix/
		
		// this is not right
		Matrix m3 = new Matrix(3,3, new double[]{ u*u*(1-c) +   c, u*v*(1-c) - w*s, u*w*(1-c) + v*s,
												  u*v*(1-c) + w*s, v*v*(1-c) +   c, u*w*(1-c) - u*s,
												  u*w*(1-c) - v*s, v*w*(1-c) + u*s, w*w*(1-c) +   c } );
		
		//System.out.println("axis angle matrix");
		//m3.show();
		//m3 = Matrix.transpose(m3);
		return m3;
	}
	
	
	
	public static Matrix convertRotationFromEulerInZYX_ExXYZ( double angle_x, double angle_y, double angle_z )
	{
		// Tait-Bryan Euler: intrinsic ZY'X" == extrinsic XYZ
		// rotation matrix M = Z(c)*Y(b)*X(a)
		//
		// http://www.j3d.org/matrix_faq/matrfaq_latest.html#Q36
		double cx = Math.cos(angle_x); // A
		double sx = Math.sin(angle_x); // B
		double cy = Math.cos(angle_y); // C
		double sy = Math.sin(angle_y); // D
		double cz = Math.cos(angle_z); // E
		double sz = Math.sin(angle_z); // F
		
		// M = | 0 1 2 |
		//     | 3 4 5 |
		//     | 6 7 8 |
		Matrix m3 = new Matrix ( 3, 3, new double[]{ cy*cz, sx*sy*cz - cx*sz, cx*sy*cz + sx*sz,
													 cy*sz, sx*sy*sz + cx*cz, cx*sy*sz - sx*cz,
													   -sz, sx*cy,            cx*cy           } );
		return m3;
	}
	
	
	
	/*public static double[] convertRotationToEulerInZYX_ExXYZ( Matrix aMatrix )
	{
		if( aMatrix == null ) throw new NullPointerException("aMatrix is null");
		if( !aMatrix.verify() ) throw new IllegalArgumentException("aMatrix is not a valid Matrix");
		if( !(aMatrix.nRows == 3 && aMatrix.nCols == 3 ) ) throw new IllegalArgumentException("aMatrix is not 3x3");
		
		// Tait-Bryan Euler: intrinsic ZY'X" == extrinsic XYZ
		// rotation matrix M = Z(c)*Y(b)*X(a)
		//
		double[] xyz = new double[3];
		double[] m3 = aMatrix.getData();
		
		return null;
	}*/
	
	
	
	public static Matrix convertRotationFromEulerInXYZ_ExZYX( double angle_x, double angle_y, double angle_z )
	{
		// Tait-Bryan Euler: intrinsic XY'Z" == extrinsic ZYX
		// rotation matrix M = X(a)*Y(b)*Z(c)
		//
		// http://www.j3d.org/matrix_faq/matrfaq_latest.html#Q36
		double cx = Math.cos(angle_x); // A
		double sx = Math.sin(angle_x); // B
		double cy = Math.cos(angle_y); // C
		double sy = Math.sin(angle_y); // D
		double cz = Math.cos(angle_z); // E
		double sz = Math.sin(angle_z); // F
		
		// M = | 0 1 2 |
		//     | 3 4 5 |
		//     | 6 7 8 |
		Matrix m3 = new Matrix ( 3, 3, new double[]{             cy*cz,            -cy*sz,     sy,
													  sx*sy*cz + cx*sz, -sx*sy*sz + cx*cz, -sx*cy,
													 -cx*sy*cz + sx*sz,  cx*sy*sz + sx*cz,  cx*cy } );
		return m3;
	}
	
	
	
	public static double[] convertRotationToEulerInXYZ_ExZYX( Matrix aMatrix )
	{
		if( aMatrix == null ) throw new NullPointerException("aMatrix is null");
		if( !aMatrix.verify() ) throw new IllegalArgumentException("aMatrix is not a valid Matrix");
		if( !(aMatrix.nRows == 3 && aMatrix.nCols == 3 ) ) throw new IllegalArgumentException("aMatrix is not 3x3");
		
		// Tait-Bryan Euler: intrinsic XY'Z" == extrinsic ZYX
		// rotation matrix M = X(a)*Y(b)*Z(c)
		//
		double[] xyz = new double[3];
		double[] m3 = aMatrix.getData();
		
		xyz[1] = Math.asin(m3[2]); // angle_y = asin(sy)
		double cy = Math.cos(xyz[1]);
		
		if( Math.abs(cy) < 1e-3 ) // cos(y) == 0 ?
		{
			//System.out.println("gimbal lock");
			// Gimbal Lock
			xyz[0] = 0.0;
			xyz[2] = Math.atan2( m3[3] , m3[4] );
		}
		else
		{
			//System.out.println("good euler");
			xyz[0] = Math.atan2( -m3[5]/cy,  m3[8]/cy );
			xyz[2] = Math.atan2( -m3[1]/cy,  m3[0]/cy );
		}
		
		// return only positive angles in [0,2*PI]
		for( int i = 0; i < 3; i++ )
			if( xyz[i] < 0.0 ) xyz[i] += 2*Math.PI;
		
		return xyz;
	}
	
	
	
	/*public static double[] transpose( double[] aData, int aNRows, int aNCols )
	{
		if( aData == null ) throw new NullPointerException("aData is null");
		if( aNRows < 0 || aNCols < 0 ) throw new IllegalArgumentException("aNRows and aNCols are not positive");
		double[] tranposeData = aData.clone();
		int nRows = aNCols;
		int nCols = aNRows;
		for( int j = 0; j < nRows; j++ )
			for( int i = 0; i < nCols; i++ )
				tranposeData[ j*nCols + i ] = aData[ i*aNCols + j ];
		return tranposeData;
	}*/
	
	public static Matrix transpose( Matrix aMatrix )
	{
		if( aMatrix == null ) throw new NullPointerException("aMatrix is null");
		if( !aMatrix.verify() ) throw new IllegalArgumentException("aMatrix is not a valid Matrix");
		Matrix tranposeMatrix = aMatrix.clone();
		for( int j = 0; j < tranposeMatrix.nRows; j++ )
			for( int i = 0; i < tranposeMatrix.nCols; i++ )
				tranposeMatrix.getData()[i + j*tranposeMatrix.nCols] = aMatrix.getData()[j + i*aMatrix.nCols];	
		return tranposeMatrix;
	}
	
	
	
	public static Matrix matMul( Matrix aMatrixA, Matrix aMatrixB )
	{
		// computes M = A*B
		// A = ar*ac B = br*bc M = ar*bc and ac == br
		
		if( !( aMatrixA.nCols == aMatrixB.nRows ) || !aMatrixA.verify() || !aMatrixB.verify() ) throw new IllegalArgumentException("cannot matMul these matrices");
		int n = aMatrixA.nCols; // common side
		Matrix matrixM = new Matrix( aMatrixA.nRows, aMatrixB.nCols );
		
		for( int j = 0; j < aMatrixA.nRows; j++ )
			for( int i = 0; i < aMatrixB.nCols; i++ )
				for( int k = 0; k < n; k++ )
					matrixM.getData()[ j*matrixM.nCols + i ] += aMatrixA.getData()[ j*n + k ] * aMatrixB.getData()[ k*aMatrixB.nCols + i ];
		return matrixM;
	}
}
