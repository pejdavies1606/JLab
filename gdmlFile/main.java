package volume_geometry;

public class main
{
	public static void main(String[] args)
	{
		SvtGeometry svt = new SvtGeometry();
		
		svt.loadConstants();
		svt.loadSurveyData();
		
		svt.defineAll();
		
		//svt.defineTop();
		//svt.defineModules();
		
		svt.outputGdml();

		System.out.println("done");
	}
	
}
