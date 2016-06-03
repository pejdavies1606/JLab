package volume_geometry;

import org.jlab.geom.prim.Vector3D;

public class main
{
	public static void main(String[] args)
	{
		/*Vector3D vec = new Vector3D( 0.0, 1.0, 0.0 );
		System.out.println("x="+vec.x()+" y="+vec.y()+" z="+vec.z() );
		System.out.println("r="+vec.r()+" t="+Math.toDegrees(vec.theta())+" p="+Math.toDegrees(vec.phi()) );
		System.exit( 0 );*/
		
		SvtGeometry svt = new SvtGeometry("survey_planes");
		
		////svt.setVisibilityModules( false );////
		////svt.setVisibilityModulesPlanes( false );////
		
		//svt.setVisibilityFiducialsNominal( false );//
		//svt.setVisibilityFiducialsSurveyIdeals( false );////
		//svt.setVisibilityFiducialsNominalDeltas( false );
		svt.setVisibilityFiducialsSurveyMeasured( false );
		//svt.setFiducialsSurveyFix( false );
		
		//svt.setVisibilityFiducialsNominalPlanes( false );//
		//svt.setVisibilityFiducialsSurveyIdealsPlanes( false );////
		svt.setVisibilityFiducialsSurveyMeasuredPlanes( false );
		
		
		//svt.defineAll();
		
		svt.defineRefs();
		//svt.defineTarget();
		//svt.openOutputDataFiles(); 
		//svt.defineModules( 0, svt.getNReg(), 0, svt.getNSect() ); // all modules
		int r = 0;
		int s = 5;
		svt.defineModules( r, r+1, s, new int[]{s+1} ); // one module
		//svt.defineModules( r, r+1, 0, svt.getNSect() ); // one region
		//svt.closeOutputDataFiles();
		
		// Geant4Basic mTop is the mother
		
		svt.outputGdml();

		System.out.println("done");
	}
	
}
