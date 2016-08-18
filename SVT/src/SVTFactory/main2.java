package SVTFactory;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Line3D;

public class main2 {

	public static void main(String[] args)
	{		
		ConstantProvider cp = SVTConstants.connect();
		
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
	}

}
