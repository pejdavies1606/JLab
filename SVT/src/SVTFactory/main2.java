package SVTFactory;

import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.geom.prim.Line3D;

public class main2 {

	public static void main(String[] args)
	{
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
		
		SVTStripFactory factoryNominal = new SVTStripFactory( cp, false );
		
		Line3D stripNLocalU = factoryNominal.createNominalStrip( 0, 0 );
		Line3D stripNLocalV = factoryNominal.createNominalStrip( 0, 1 );
		
		System.out.println("U");
		stripNLocalU.show();
		System.out.println("V");
		stripNLocalV.show();
		
		SVTStripFactory factoryShifted = new SVTStripFactory( cp, true );
		
		Line3D stripSLocalU = factoryShifted.createShiftedStrip( 0, 0, 0, 0 );
		Line3D stripSLocalV = factoryShifted.createShiftedStrip( 0, 0, 0, 1 );
		
		System.out.println("U");
		stripSLocalU.show();
		System.out.println("V");
		stripSLocalV.show();
		
		/*for( int layer = 0; layer < SVTConstants.NLAYERS; layer++ )
		{
			int[] rm = SVTConstants.convertLayer2RegionModule(layer);
			int region = rm[0], module = rm[1];
			
			for( int sector = 0; sector < SVTConstants.NSECTORS[region]; sector++ )
			{
				for( int strip = 0; strip < SVTConstants.NSTRIPS; strip+=50 ) // SVTGeant4Factory.NSTRIPS
				{
					
					Line3D stripLine = factory.getStrip( layer, sector, strip ); // pass zero-based indices to methods
					System.out.println("layer "+(layer+1)+" sector "+(sector+1)+" strip "+(strip+1)); // print one-based indices 
					System.out.printf(" origin % 8.3f % 8.3f % 8.3f\n", stripLine.origin().x(), stripLine.origin().y(), stripLine.origin().z() );
					System.out.printf(" end    % 8.3f % 8.3f % 8.3f\n", stripLine.end().x(), stripLine.end().y(), stripLine.end().z() );
					
					//System.out.println( stripVol.gemcString() );
					//for( int c = 0; c < stripVol.getChildren().size(); c++ )
						//System.out.println( stripVol.getChildren().get(c).gemcString() );
				}
			}
		}*/
	}

}
