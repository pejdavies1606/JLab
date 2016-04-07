package volume_geometry;

import org.jlab.geom.geant.Geant4Basic;

public class main
{
	public static void main(String[] args)
	{
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
		
		ConstantsCcdb.Load();
		//System.exit( 0 );
		
		double moduleLength = ConstantsCcdb.MODULELENGTH / 10.0,  // mm -> cm
				moduleWidth = ConstantsCcdb.ACTIVESENWIDTH / 10.0, // total sensor width, not pitch adapter width
				moduleHeight = ConstantsCcdb.SILICONTHICK / 10.0;
		
		System.out.println("defining Geant4Basic geometry");
		System.out.printf("moduleLength=%8.3e\n", moduleLength );
		System.out.printf("moduleWidth =%8.3e\n", moduleWidth );
		System.out.printf("moduleHeight=%8.3e\n", moduleHeight );
		
		double topSide = 100.0; // cm
		
		Geant4Basic top = new Geant4Basic("top", "box", topSide, topSide, topSide );
		//System.out.println( top.toString() );
		
		Geant4Basic target = new Geant4Basic("target", "eltube", 1.5240, 1.5240, 3.0 ); // cm
		top.getChildren().add( target );
		
		Geant4Basic refX = new Geant4Basic("refX", "box", 1.0, 0.25, 0.25 );
		refX.setPosition( 0.5, 0.0, 0.0 );
		top.getChildren().add( refX );
		
		Geant4Basic refY = new Geant4Basic("refY", "box", 0.25, 1.0, 0.25 );
		refY.setPosition( 0.0, 0.5, 0.0 );
		top.getChildren().add( refY );
		
		Geant4Basic refZ = new Geant4Basic("refZ", "box", 0.25, 0.25, 10.0 );
		refZ.setPosition( 0.0, 0.0, 5.0 );
		top.getChildren().add( refZ );
		
		int nReg = ConstantsCcdb.NREG;
		int[] nSect = ConstantsCcdb.NSECT;
		double[] regionZ = ConstantsCcdb.Z0;
		
		for( int r = 0; r < nReg; r++ )
		{
			double[] sectorRadius = ConstantsCcdb.MODULERADIUS[r];
			
			for( int s = 0; s < nSect[r]; s++ )
				for( int l = 0; l < ConstantsCcdb.NSLAYR; l++ )
				{
					String sl = ""; // super layer label
					switch( l )
					{
					case 0:
						sl = "u"; // lower / inner
						break;
					case 1:
						sl = "v"; // upper / outer
						break;
					}
					Geant4Basic module = new Geant4Basic("module_r"+(r+1)+"s"+(s+1)+sl, "box", moduleWidth, moduleHeight, moduleLength );
					
					double phi = 2*Math.PI/nSect[r]*s + Math.PI;
					double x = sectorRadius[l]*Math.cos(phi + Math.PI/2) / 10.0;
					double y = sectorRadius[l]*Math.sin(phi + Math.PI/2) / 10.0;
					double z = regionZ[r] / 10.0 + moduleLength/2;
					
					module.setPosition( x, y, z );
					module.setRotation("xyz", 0.0, 0.0, -phi );
					top.getChildren().add( module );
					//System.out.println( module.toString() );
				}
		}
		
		//System.exit(0);

		IVolumeExporter gdml = VolumeExporterFactory.createVolumeExporter("gdml");
		//GdmlFile gdml = new GdmlFile();
		System.out.println("constructed GdmlFile");
		//gdml.setVerbose( true );
		//gdml.setPositionLoc("local");
		//gdml.setRotationLoc("local");
		
		gdml.setAngleUnit("rad");
		gdml.addTopVolume( top, "mat_vacuum" );
		gdml.writeFile("test");
		
		System.out.println("done");

	}

}
