package geometry_test;

import org.jlab.geom.prim.*;
import java.io.*;

public class main {
	
	public static void makeBox( Shape3D aBox, double x, double y, double z ) {		
		// each square face is made of two triangles
		aBox.addFace( new Triangle3D( 0.0, 0.0, 0.0,  x , 0.0, 0.0, 0.0,  y , 0.0 ) ); // 0XYZ0
		aBox.addFace( new Triangle3D(  x ,  y , 0.0,  x , 0.0, 0.0, 0.0,  y , 0.0 ) ); // 1XYZ0
		aBox.addFace( new Triangle3D( 0.0, 0.0,  z ,  x , 0.0,  z , 0.0,  y ,  z  ) ); // 0XYZ1
		aBox.addFace( new Triangle3D(  x ,  x ,  z ,  x , 0.0,  z , 0.0,  y ,  z  ) ); // 1XYZ1
		
		aBox.addFace( new Triangle3D( 0.0, 0.0, 0.0, 0.0,  y , 0.0, 0.0, 0.0,  z  ) ); // 0YZX0
		aBox.addFace( new Triangle3D( 0.0,  y ,  z , 0.0,  y , 0.0, 0.0, 0.0,  z  ) ); // 1YZX0
		aBox.addFace( new Triangle3D(  x , 0.0, 0.0,  x ,  y , 0.0,  x , 0.0,  z  ) ); // 0YZX1
		aBox.addFace( new Triangle3D(  x ,  y ,  z ,  x ,  y , 0.0,  x , 0.0,  z  ) ); // 1YZX1
		
		aBox.addFace( new Triangle3D( 0.0, 0.0, 0.0,  x , 0.0, 0.0, 0.0, 0.0,  z  ) ); // 0XZY0
		aBox.addFace( new Triangle3D(  x , 0.0,  z ,  x , 0.0, 0.0, 0.0, 0.0,  z  ) ); // 1XZY0
		aBox.addFace( new Triangle3D( 0.0,  y , 0.0,  x ,  y , 0.0, 0.0,  y ,  z  ) ); // 0XZY1
		aBox.addFace( new Triangle3D(  x ,  y ,  z ,  x ,  y , 0.0, 0.0,  y ,  z  ) ); // 1XZY1
	}

	public static void main(String[] args) {
		
		// xyz=0 0 0 is the target
		// need to distribute shapes around the target
		
		ObjFile obj = new ObjFile();
		//obj.newShape(tri, "tri1");
		
		Shape3D box = new Shape3D();
		makeBox( box, 10.0, 10.0, 10.0 );
		//box.show();
		
		obj.newShape( box, "box1" );
		
		/*obj.setFilename("shapes_raw.obj");
		try {
			obj.write();
		} catch( IOException e ) {
			System.err.println("IOException: " + e.getMessage() + "\n");
		}*/
		
		obj.scale( 1.0, -1.0, 1.0 ); // convert to Processing frame
		
		obj.setFilename("box.obj");
		try {
			obj.write();
		} catch( IOException e ) {
			System.err.println("IOException: " + e.getMessage() + "\n");
		}
	}

}
