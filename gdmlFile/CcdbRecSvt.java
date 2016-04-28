package volume_geometry;

import org.jlab.clasrec.utils.DatabaseConstantProvider;

public class CcdbRecSvt
{
	// SVT RECONSTRUCTION PARAMETERS
	//
	//------------------
	// length unit: mm
	//  angle unit: deg
	//------------------
	//
	// fundamentals
	public static double LIGHTVEL;
	//
	// selection cuts for helical tracks
	public static double MINRADCURV;
	//
	// cut on Edp min
	public static double EDEPMIN;
	//
	// cut on intersection tolerance
	public static double INTERTOL;
	public static double SUMSTRPNUMMIN;
	public static double SUMSTRPNUMMAX;
	//
	// dEdX
	public static double CATHICK;
	public static double ROHACELLTHICK;
	//
	// code for identifying SVT in making a ID for an SVT reconstruction
	public static double SVTIDCODE;
	//
	// cut based cand select
	public static double PHICUT; // may be 3 values instead
	public static double RADCUT;
	public static double DRDZCUT;
	//
	// SVT misalignments
	public static double RADSPECS;
	public static double CIRCLEFITMAXCHI2;
	public static double SVTTRACKNUMITER;
	public static double MAXNUMCROSSES;
	public static double MAXNUMCROSSESINMODULE;
	//
	// ADC to energy conversion
	public static double NBITSADC;
	public static double EMAXREADOUT;
	//
	// cosmics
	public static double COSMICSMINRESIDUAL;
	public static double COSMICSMINRESIDUALZAXIS;
	//
	// track list cut-off
	public static double SILICONRADLEN;
	public static double PIDCUTOFF;
	public static double TOLTOMODULEEDGE;
	public static double MAXDISTTOTRAJXY;
	public static double SVTEXCLUDEDFITREGION;
	//
	// flags and CCDB address
	public static boolean bConstantsLoaded = false;
	public static final String RECPATH = "/rec/cvt/svt/"; // new tables
	
	
	
	public static synchronized void load()
	{
		if( bConstantsLoaded ) return;
		
		DatabaseConstantProvider cp = new DatabaseConstantProvider( 10, "default");
		
		cp.loadTable(RECPATH+"svt");
		
		LIGHTVEL = cp.getInteger(RECPATH+"svt/lightVel", 0 );
		/*
		MINRADCURV = cp.getInteger(RECPATH+"svt/minRadCurv", 0 );
		
		EDEPMIN = cp.getInteger(RECPATH+"svt/eDepMin", 0 );
		
		INTERTOL = cp.getInteger(RECPATH+"svt/interTol", 0 );
		SUMSTRPNUMMIN = cp.getInteger(RECPATH+"svt/sumStrpNumMin", 0 );
		SUMSTRPNUMMAX = cp.getInteger(RECPATH+"svt/sumStrpNumMax", 0 );
		
		CATHICK = cp.getInteger(RECPATH+"svt/caThick", 0 );
		ROHACELLTHICK = cp.getInteger(RECPATH+"svt/rohacellThick", 0 );
		
		SVTIDCODE = cp.getInteger(RECPATH+"svt/svtIdCode", 0 );
		
		PHICUT = cp.getInteger(RECPATH+"svt/phiCut", 0 ); // might change
		RADCUT = cp.getInteger(RECPATH+"svt/radCut", 0 );
		DRDZCUT = cp.getInteger(RECPATH+"svt/drdzCut", 0 );
		RADSPECS = cp.getInteger(RECPATH+"svt/radSpecs", 0 );
		*/
		CIRCLEFITMAXCHI2 = cp.getInteger(RECPATH+"svt/circleFitMaxChi2", 0 );
		SVTTRACKNUMITER = cp.getInteger(RECPATH+"svt/svtTrackNumIter", 0 );
		MAXNUMCROSSES = cp.getInteger(RECPATH+"svt/maxNumCrosses", 0 );
		MAXNUMCROSSESINMODULE = cp.getInteger(RECPATH+"svt/maxNumCrossesInModule", 0 );
		
		NBITSADC = cp.getInteger(RECPATH+"svt/nBitsAdc", 0 );
		EMAXREADOUT = cp.getInteger(RECPATH+"svt/eMaxReadout", 0 );
		
		COSMICSMINRESIDUAL = cp.getInteger(RECPATH+"svt/cosmicsMinResidual", 0 );
		COSMICSMINRESIDUALZAXIS = cp.getInteger(RECPATH+"svt/cosmicsMinResidualZAxis", 0 );
		
		SILICONRADLEN = cp.getInteger(RECPATH+"svt/siliconRadLen", 0 );
		PIDCUTOFF = cp.getInteger(RECPATH+"svt/pidCutoff", 0 );
		TOLTOMODULEEDGE = cp.getInteger(RECPATH+"svt/tolToModuleEdge", 0 );
		MAXDISTTOTRAJXY = cp.getInteger(RECPATH+"svt/maxDistToTrajXY", 0 );
		SVTEXCLUDEDFITREGION = cp.getInteger(RECPATH+"svt/svtExcludedFitRegion", 0 );
		
		cp.disconnect();
		bConstantsLoaded = true;
		System.out.println(" reconstruction constants loaded ? "+bConstantsLoaded);
	}
	
}
