package volume_geometry;

public class main
{
	public static void main(String[] args)
	{
		SvtGeometry svt = new SvtGeometry("survey_planes");
		
		//svt.setVisibilityModules( false );
		//svt.setVisibilityFiducialsNominal( false );
		//svt.setVisibilityFiducialsSurveyIdeals( false );
		//svt.setVisibilityFiducialsSurveyMeasured( false );
		
		//svt.defineAll();
		
		//svt.defineRefs();
		//svt.defineTarget();
		//svt.openOutputDataFiles();
		svt.defineModules( 0, svt.getNReg(), 0, svt.getNSect() ); // all modules and fiducials
		//int r = 0;
		//int s = 6;
		//svt.defineModules( r, r+1, s, new int[]{s+1} ); // one module
		//svt.defineModules( r, r+1, 0, svt.mNSect ); // one region
		//svt.closeOutputDataFiles();
		
		// Geant4Basic mTop is the mother
		
		svt.outputGdml();

		System.out.println("done");
	}
	
}
