package volume_geometry;

import org.jlab.geom.geant.Geant4Basic;

import java.util.List;

public class main {

	public static void main(String[] args) {

		//System.out.println("defining Geant4Basic geometry");
		/*Geant4Basic top = new Geant4Basic("top", "box", 10.0, 10.0, 10.0 );
		Geant4Basic box1 = new Geant4Basic("box1", "box", 2.0, 4.0, 6.0 );
		Geant4Basic box2 = new Geant4Basic("box2", "box", 1.0, 1.0, 1.0 );
		top.getChildren().add(box1);
		top.getChildren().add(box2);

		box1.setPosition( -2.0, -2.0, -2.0 );
		box1.setRotation("xyz", 30.0, 0.0, 0.0); // assume degrees?
		box2.setPosition( 1.0, 1.0, 1.0 );
		
		System.out.println( top.toString() );
		System.out.println( box1.toString() );
		System.out.println( box2.toString() );*/
		
		//DatabaseConstantProvider cp = new DatabaseConstantProvider( 10, "default");
		
		/*Constants.Load();
		// module in region 1 sector 1
		double moduleLength = Constants.MODULELENGTH,
				moduleWidth = Constants.ACTIVESENWIDTH, // total sensor width, not pitch adaptor with
				moduleHeight = Constants.LAYRGAP,
				layerGap = 2.5+2*(0.190 + 0.078 + 0.065 + 0.5*Constants.SILICONWIDTH );
		
		System.out.printf("moduleLength =%8.3f\n", moduleLength );
		System.out.printf("moduleWidth  =%8.3f\n", moduleWidth );
		System.out.printf("moduleHeight =%8.3f\n", moduleHeight );
		System.out.printf("layerGap     =%8.3f\n", layerGap );
		System.out.printf("diff         =%8.3f\n", moduleHeight - layerGap );*/
		
		double topSide = 1000.0;
		
		Geant4Basic top = new Geant4Basic("top", "box", topSide, topSide, topSide );
		//Geant4Basic module = new Geant4Basic("module", "box", moduleLength, moduleWidth, moduleHeight );
		//top.getChildren().add( module );
		
		//module.setPosition( topSide/2, topSide/2, topSide/2 );
		
		System.out.println( top.toString() );
		//System.out.println( module.toString() );
		
		//cp.disconnect();
		System.exit(0);

		/*VolumeExporter gdml = VolumeExporterFactory.createVolumeExporter("gdml");
		//GdmlFile gdml = new GdmlFile();
		System.out.println("constructed GdmlFile");
		gdml.setVerbose( true );
		
		//gdml.addTopVolume( top );
		gdml.writeFile("test");
		
		cp.disconnect();
		System.out.println("done");*/

	}

}
