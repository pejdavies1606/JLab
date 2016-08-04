import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Line3D;

import SVTFactory.SVTGeant4Factory;
import SVTMisc.Utils;
import VolumeExporter.IGdmlExporter;
import VolumeExporter.VolumeExporterFactory;

public class main {

	public static void main(String[] args)
	{
		DatabaseConstantProvider cp = new DatabaseConstantProvider( 10, "default");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"svt");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"region");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"fiducial");
		cp.loadTable( SVTGeant4Factory.getCcdbPath() +"support");
		//cp.loadTable( SVTGeant4Factory.getCcdbPath() +"survey");
		//cp.loadTable( SVTGeant4Factory.getCcdbPath() +"material");
		cp.disconnect();
		//System.out.println(cp.toString());
		//System.exit(0);
		
		SVTGeant4Factory svt = new SVTGeant4Factory( cp );
		
		svt.setRange( 0, 1, new int[]{0,0,0,0}, new int[]{0,0,0,0} ); // one-based indices
		
		for( int l = 0; l < SVTGeant4Factory.NLAYERS; l++ )
		{
			for( int s = 0; s < SVTGeant4Factory.NSECTORS[SVTGeant4Factory.convertLayer2RegionModule(l)[0]]; s++ )
			{
				for( int i = 0; i < SVTGeant4Factory.NSTRIPS; i+=50 ) // SVTGeant4Factory.NSTRIPS
				{
					Line3D stripLine = svt.getStrip( l, s, i ); // pass zero-based indices to methods
					System.out.println("layer "+(l+1)+" sector "+(s+1)+" strip "+(i+1)); // print one-based indices 
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
		
		IGdmlExporter gdmlFile = VolumeExporterFactory.createGdmlExporter();
		gdmlFile.setPositionLoc("local");
		gdmlFile.setRotationLoc("local");
		gdmlFile.addTopVolume( svt.getMotherVolume() );
		gdmlFile.addMaterialPreset("mat_sensorActive", "mat_vacuum");
		gdmlFile.replaceAttribute( "structure", "volume", "name", "vol_sensorActive", "materialref", "ref", "mat_sensorActive");
		gdmlFile.replaceAttribute( "structure", "volume", "name", "vol_deadZone", "materialref", "ref", "mat_sensorActive");
		gdmlFile.writeFile("svtStrips");
	}

}
