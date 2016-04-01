package volume_geometry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.jMath.Vector.threeVec;

//import trackfitter.surface.Line;
import trackfitter.track.Helix;

public class Geometry {
	
	public Geometry() {
		
	}
	
    // Comments on the Geometry of the BST 
	//------------------------------------
	// The BST geometry consists of 3 (or 4) superlayers of modules. 
	// Each superlayer contains two layers of modules, labeled A and B. 
	// Layer B corresponds to the top layer as seen from the outside of the detector, 
	// and Layer A to the layer underneath Layer B looking from the outside.  
	// Each module contains 3 sensors (hybrid, intermediate, far).
	// The hybrid, intermediate and far sensors are aligned in the direction of the beam 
	// corresponding to the positive z-axis in the laboratory frame.  
	// The coordinate system in the lab frame (center of the target) is a right handed system, with 
	// the z unit vector in the direction of the beam, and the y unit vector pointing up; 
	// the x unit vector points therefore to the left when looking in the direction of the beam.
	// The numbering convention for the sectors is as follows:
	// sector 1 modules oriented at 90 deg (80 deg) with respect to the y-axis for superlayers 1,2,4 (3); 
	// sector numbers increase in the clockwise direction (viewed in the direction of the beam).  
	// The strips in the hybrid sensor of Layer B are connected to the pitch adapter and 
	// and implanted with 156 micron pitch.  There are 256 strips oriented at graded angle 
	// from 0 to +3 deg with respect to the bottom edge of layer B which corresponds to the z-direction. 
	// Strip number 1 in Layer B is parallel to the bottom of the sensor.  

 
		//for making bst outline fig
		
		public threeVec getPlaneModuleOrigin(int sector, int layer) {
			//shift the local origin to the physical orign instead of active area
			threeVec point0 = new threeVec(0,0,0);
			//point0.set(transformToLabFrame(-(42.000-40.052)/2., 0, layer, sector, 0));
			point0.set(transformToFrame( sector,  layer, -(42.000-40.052), 0, 0, "lab", ""));
			return point0;
		}
		public threeVec getPlaneModuleEnd(int sector, int layer) {
			//shift the local origin to the physical orign instead of active area
			threeVec point0 = new threeVec(0,0,0);
			//point0.set(transformToLabFrame(-(42.000-40.052)/2., 0, layer, sector, 0));
			point0.set(transformToFrame( sector,  layer, 42.000, 0, 0, "lab", ""));
			return point0;
		}
		
		
		
		//*** 
		public  int findSectorFromAngle(int layer, double trackPhiAtLayer) {
			int Sect = Constants.NSECT[layer-1];
			for(int s = 0; s<Constants.NSECT[layer-1]-1; s++) {
				int sector = s+1;
				double phi1 = this.getPlaneModuleOrigin(sector, layer).phi();
				double phi2 = this.getPlaneModuleOrigin(sector+1, layer).phi();
				if(phi1<0)
					phi1+=2.*Math.PI;
				if(phi2<0)
					phi2+=2.*Math.PI;
				
				if(trackPhiAtLayer>=phi1 && trackPhiAtLayer<=phi2)
					Sect = sector;
			}
			return Sect;
		}
		
		//***
		public threeVec findBSTPlaneNormal(int sector, int layer) {
			
			//double angle = 2.*Math.PI*((double)(sector-1)/(double)Constants.NSECT[layer-1]) + Math.PI/2.;
			double angle = 2.*Math.PI*((double)(sector-1)/(double)Constants.NSECT[layer-1]) + Constants.PHI0[layer-1];
		    return new threeVec(Math.cos(angle), Math.sin(angle), 0);
		}
		//***
		public  double[] getLocCoord(double s1, double s2) { //2 top, 1 bottom
			
			double[] X = new double[2];
			double ialpha1 = (s1-1)*Constants.STEREOANGLE/(double) (Constants.NSTRIP-1); 
			//the active area starts at the first strip 	
			double interc1 = (s1)*Constants.PITCH;
			double ialpha2 = (s2-1)*Constants.STEREOANGLE/(double) (Constants.NSTRIP-1); 
			//the active area starts at the first strip 	
			double interc2 = (s2)*Constants.PITCH;

			// Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
			// -------------------------------------
			double m1 = -Math.tan(ialpha1);
			double m2 =  Math.tan(ialpha2);
			double b1 = Constants.ACTIVESENWIDTH - interc1 ;
			double b2 = interc2;
			
			double z = (b2-b1)/(m1-m2);
			double x = m1*z +b1;
			X[0] = x;
			X[1] = z;
			
			return X;

		}
		public double[] getLocCoordErrs(int lay1, int lay2, double s1, double s2, double z) {
			double[] Xerr = new double[2];
			
			double sigma1 = getSingleStripResolution( lay1, (int) s1, z);
			double sigma2 = getSingleStripResolution( lay2, (int) s2, z);
			
			
			Xerr[0] = Math.sqrt(sigma1*sigma1+sigma2*sigma2);
			
			Xerr[1] = (getLocCoord( s1-0.5,  s2-0.5)[1]
						-getLocCoord( s1+0.5,  s2+0.5)[1]);
			
			//Xerr[0] = (getLocCoord( s1-0.5,  s2-0.5)[0]+
			//		-getLocCoord( s1+0.5,  s2+0.5)[0]);
			if(s1<=1)
				Xerr[1] = (getLocCoord( s1,  s2-0.5)[1]
						-getLocCoord( s1+1.5,  s2+0.5)[1]);
			if(s2<=1)
				Xerr[1] = (getLocCoord( s1-0.5,  s2)[1]
						-getLocCoord( s1+1.5,  s2+2.5)[1]);
			
			return Xerr;
			
		}
		//***
		public  threeVec transformToFrame(int sector, int layer, double x, double y, double z, String frame, String MiddlePlane) {
			
			// global rotation angle
			double Glob_rangl = ((double) (sector-1)/(double) Constants.NSECT[layer-1])*2.*Math.PI + Constants.PHI0[layer-1];
			// angle to rotate to global frame
			double Loc_to_Glob_rangl = Glob_rangl-Constants.LOCZAXISROTATION;

			double gap = 0;
			if(MiddlePlane == "middle") {
				if((layer-1)%2==0) { // for a cross take the bottom layer
					gap = Constants.MODULERADIUS[layer][sector-1]-Constants.MODULERADIUS[layer-1][sector-1];
				}
			}
			double lTx = (Constants.MODULERADIUS[layer-1][sector-1]+0.5*gap)*Math.cos(Glob_rangl);
			double lTy = (Constants.MODULERADIUS[layer-1][sector-1]+0.5*gap)*Math.sin(Glob_rangl); 
			double lTz = Constants.Z0[layer-1]; 

			
			//rotate and translate
			double cosRotation = Math.cos(Loc_to_Glob_rangl);
			double sinRotation = Math.sin(Loc_to_Glob_rangl);

			double xt =0;
			double yt =0;
			double zt =0;
			
			if(frame == "lab") {
				 xt = (x-0.5*Constants.ACTIVESENWIDTH)*cosRotation - y*sinRotation + lTx;
				 yt = (x-0.5*Constants.ACTIVESENWIDTH)*sinRotation + y*cosRotation + lTy;
				 zt = z + lTz;
			}
			if(frame == "local") {
				 xt=  (x-lTx)*cosRotation +(y-lTy)*sinRotation  + 0.5*Constants.ACTIVESENWIDTH;
				 yt= -(x-lTx)*sinRotation +(y-lTy)*cosRotation  ;
				 zt = z - lTz ;
			}
			return new threeVec(xt,yt,zt);
		}
		//*** point and its error
		public  double[] getCrossPars(int sector, int upperlayer, double s1, double s2, String frame, threeVec trkDir) {
			double[] vals = new double[6];
			
			// if first iteration trkDir == null
			double s2corr = s2;
			// now use track info
			s2corr = this.getCorrectedStrip( sector,  upperlayer,  s2,  trkDir,  Constants.MODULELENGTH);
			double z =getLocCoord(s1,s2corr)[1];
			//update using the corrected z
			s2corr = this.getCorrectedStrip( sector,  upperlayer,  s2,  trkDir,  z);
			
			double zf =getLocCoord(s1,s2corr)[1];
			
			if(upperlayer%2!=0) // should not happen as is upper layer...but just in case
				s2corr =s2;
			
			double[] LC = getLocCoord(s1,s2corr);
			double LC_x = LC[0];
			double LC_z = LC[1];
			
			threeVec crPoint = transformToFrame( sector,  upperlayer-1, LC_x, 0, LC_z, "lab", "middle");
			
			vals[0] = crPoint.x();
			vals[1] = crPoint.y();
			vals[2] = crPoint.z();
			
			
			double [] LCErr = getLocCoordErrs(upperlayer-1, upperlayer, s1, s2corr, zf);
			double LCErr_x = LCErr[0];
			double LCErr_z = LCErr[1];
			
			
			 // global rotation angle to get the error in the lab frame
			int layerIdx = upperlayer-1;
	       /*
			double Glob_rangl = ((double) (sector-1)/(double) Constants.NSECT[layerIdx])*2.*Math.PI;
	        // angle to rotate to global frame
	        double Loc_to_Glob_rangl = Glob_rangl-Constants.PHI0[layerIdx];
	       */
			// global rotation angle
			double Glob_rangl = ((double) (sector-1)/(double) Constants.NSECT[layerIdx])*2.*Math.PI + Constants.PHI0[layerIdx];
			// angle to rotate to global frame
			double Loc_to_Glob_rangl = Glob_rangl-Constants.LOCZAXISROTATION;

						
	        double cosRotation = Math.cos(Loc_to_Glob_rangl);
	        double sinRotation = Math.sin(Loc_to_Glob_rangl);	        

	        double yerr = Math.abs(cosRotation*LCErr_x);
	        double xerr = Math.abs(sinRotation*LCErr_x);
		   
			vals[3] = xerr;
			vals[4] = yerr;
			vals[5] = LCErr_z;
			
			//if the local cross is not in the fiducial volume it is not physical
			if ( (trkDir!=null && (LC_x<0 || LC_x>Constants.ACTIVESENWIDTH+Constants.TOLTOMODULEEDGE) ) || 
					(trkDir!=null && (LC_z<-Constants.interTol || LC_z>Constants.MODULELENGTH + Constants.interTol) ))
				return new double[] {Double.NaN,0,Double.NaN,Double.NaN, Double.NaN, Double.NaN};
				
			double[] values = new double[6];
			if(frame == "lab")
				values = vals;
			if(frame == "local")
				values = new double[] {LC_x,0,LC_z,LCErr_x, 0, LCErr_z};
			
			return values;
			
			
		}
		
		private double getCorrectedStrip(int sector, int upperlayer, double s2,
				threeVec trkDir, double ZalongModule) {
			double s2corr = s2;
			// second iteration: there is a track direction
			if(trkDir!=null) { 
				double stripCorr = getStripIndexShift( sector,  upperlayer,  trkDir, s2, ZalongModule);
				if(s2>1)
					s2corr = s2 + stripCorr;	
				if(s2==1) {
					if(stripCorr>=0)
						s2corr = s2 + stripCorr;	
					if(stripCorr<0)
						s2corr = s2;
				}
			}
			return s2corr;
		}

		public double calcNearestStrip(double X, double Y, double Z, int layer, int sect) {
			
			threeVec LocPoint = this.transformToFrame( sect, layer, X, Y, Z, "local", "");
			
			double x = LocPoint.x();
			double z = LocPoint.z();
			
			double alpha = Constants.STEREOANGLE/(double) (Constants.NSTRIP-1); 
			
			double b = Constants.ACTIVESENWIDTH;
			double P = Constants.PITCH;
			
			double s = -1;
			
			
			 if(layer%2==1) {//layers 1,3,5 == bottom ==i ==>(1) : regular configuration
				//m1,b1
				s = (int) Math.floor((-x+b+alpha*z)/(alpha*z+P));	
				
				double delta = 99999;
				double sdelta = delta;
				double newStrip = s;
				for(int i = -1; i<2; i++) {
					double sp = s+(double)i;
					double x_calc = -Math.tan((sp-1)*alpha)*z+b-sp*P;
					
					if(Math.abs(x-x_calc)<delta) {
						sdelta = x-x_calc;
						delta = Math.abs(sdelta);
						newStrip = sp;
					}				
				}
				
				s=newStrip;
				for(int i = -10; i<=10; i++) {
					double sp = s+(double)i*0.1;
					double x_calc = -Math.tan((sp-1)*alpha)*z+b-sp*P;
					
					if(Math.abs(x-x_calc)<delta) {
						sdelta = x-x_calc;
						delta = Math.abs(sdelta);
						newStrip = sp;
					}	
				}
				s=newStrip;
				// charge sharing digitization routine in GEMC
				/*if(sdelta>(P+z*Math.tan(alpha))/4.)
					s= newStrip-0.5;
				if(sdelta<-(P+z*Math.tan(alpha))/4.)
					s= newStrip+0.5;
				//s=(-x+b+alpha*z)/(alpha*z+P); */
				
			}
			if(layer%2==0) { 
				 //layers 2,4,6 == top ==j ==>(2) : regular configuration
				//m2,b2		
				s = (int) Math.floor((x+alpha*z)/(alpha*z+P));	
				
				double delta = 99999;
				double sdelta = delta;
				double newStrip = s;
				for(int i = -1; i<2; i++) {
					double sp = s+(double)i;
					double x_calc = Math.tan((sp-1)*alpha)*z+sp*P;
					
					if(Math.abs(x-x_calc)<delta) {
						sdelta = x-x_calc;
						delta = Math.abs(sdelta);
						newStrip = sp;
					}				
				}
				
				s=newStrip;
				for(int i = -10; i<=10; i++) {
					double sp = s+(double)i*0.1;
					double x_calc = Math.tan((sp-1)*alpha)*z+sp*P;
					
					if(Math.abs(x-x_calc)<delta) {
						sdelta = x-x_calc;
						delta = Math.abs(sdelta);
						newStrip = sp;
					}	
				}
				s=newStrip;
				// charge sharing digitization routine in GEMC
				/*if(sdelta>(P+z*Math.tan(alpha))/4.)
					s= newStrip+0.5;
				if(sdelta<-(P+z*Math.tan(alpha))/4.)
					s= newStrip-0.5;
				//s=(x+alpha*z)/(alpha*z+P); */
				
			}	
			if(s<0.5)
				s=1;
			//System.out.println(" layer "+layer+" sector "+sect+" strip "+s);
			return s;
		}
		//****
		public  double getSingleStripResolution(int lay, int strip, double Z) { // as a function of local z
			double Strip = (double) strip;
			double StripUp = Strip+1;
			if(strip == Constants.NSTRIP)
				StripUp = (double) Constants.NSTRIP; //edge strip
			double StripDown = Strip-1;
			if(strip ==1)
				StripDown = 1; //edge strip
		
			
			double pitchToNextStrp = Math.abs(getXAtZ(lay, (double) StripUp,Z)-getXAtZ(lay, (double) Strip,Z)); // this is P- in the formula below
			double pitchToPrevStrp = Math.abs(getXAtZ(lay, (double) StripDown,Z)-getXAtZ(lay, (double) Strip,Z)); // this is P+ in the formula below 

			
			// For a given strip (for which we estimate the resolution), P+ is the pitch to the strip above (at z position) and P- to that below in the local coordinate system of the module.
			// The current design of the BST is simulated in gemc such that each strip provides hit-no-hit information, and the single strip resolution is 
			// therefore given by the variance,
			// sigma^2 = (2/[P+ + P-]) {integral_{- P-/2}^{P+/2) x^2 dx -[integral_{- P-/2}^{P+/2) x dx]^2}
			//this gives, sigma^2 = [1/(P+ + P-)]*[ (P+^3 + P-^3)/12 - (P+^2 - P-^2)^2/[32(P+ + P-)] ]

			double Pp2 = pitchToNextStrp*pitchToNextStrp;
			double Pp3 = pitchToNextStrp*Pp2;
			double Pm2 = pitchToPrevStrp*pitchToPrevStrp;
			double Pm3 = pitchToPrevStrp*Pm2;
			double Psum = pitchToNextStrp+pitchToPrevStrp;
			double invPsum = 1./Psum;
			double firstTerm = (Pp3+Pm3)/12.;
			double secondTerm = ((Pp2-Pm2)*(Pp2-Pm2)*invPsum)/32.;
			double strip_sigma_sq = (firstTerm-secondTerm)*invPsum;

			double strip_sigma = Math.sqrt(strip_sigma_sq);

			return strip_sigma;
		}
		//****
		public double getDOCAToStrip(int sector, int layer, double centroidstrip, threeVec point0) {
			
			// local angle of  line graded from 0 to 3 deg.
			double ialpha = (centroidstrip-1)*Constants.STEREOANGLE/(double) (Constants.NSTRIP-1); 
			//the active area starts at the first strip 	
			double interc = (centroidstrip)*Constants.PITCH;		

			// Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
			// -------------------------------------
			double m1 = -Math.tan(ialpha);
			double m2 =  Math.tan(ialpha);
			double b1 = Constants.ACTIVESENWIDTH - interc;
			double b2 = interc;

			threeVec vecAlongStrip = new threeVec();
			threeVec pointOnStrip = new threeVec();
			threeVec LocPoint = this.transformToFrame( sector, layer, point0.x(), point0.y(), point0.z(), "local", "");
			
			if(layer%2==0) { //layers 2,4,6 == top ==j ==>(2) : regular configuration
				vecAlongStrip = new threeVec(m2, 0, 1); 
				pointOnStrip = new threeVec(b2, 0, 0);					
			}
			if(layer%2==1) { //layers 1,3,5 == bottom ==i ==>(1) : regular configuration
				vecAlongStrip = new threeVec(m1, 0, 1); 
				pointOnStrip = new threeVec(b1, 0, 0);		
			}	

			if(vecAlongStrip.len()>0)
				vecAlongStrip.multi(1./vecAlongStrip.len());

			threeVec r = LocPoint.diff(pointOnStrip);
			
			threeVec d = r.cross(vecAlongStrip);
			
			return d.y();
			
		}
		//****
		// in the local coordinate system 
		public  double getXAtZ(int layer, double centroidstrip, double Z) {
			double X =0;
			// local angle of  line graded from 0 to 3 deg.
			double ialpha = (centroidstrip-1)*Constants.STEREOANGLE/(double) (Constants.NSTRIP-1); 
			//the active area starts at the first strip 	
			double interc = (centroidstrip)*Constants.PITCH;		

			// Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
			// -------------------------------------
			double m1 = -Math.tan(ialpha);
			double m2 =  Math.tan(ialpha);
			double b1 = Constants.ACTIVESENWIDTH - interc;
			double b2 = interc;

			
			if(layer%2==0) { //layers 2,4,6 == top ==j ==>(2) : regular configuration
				
				X = m2*Z + b2;						
			}
			if(layer%2==1) { //layers 1,3,5 == bottom ==i ==>(1) : regular configuration
							
				X = m1*Z + b1;
			}	


			return X;
		}

		
		
		//***
		public  double getStripIndexShift(int sector, int layer, threeVec trkDir, double s2, double z) {
			
			double tx = trkDir.x();
			double ty = trkDir.y();
			threeVec trkDir_t = new threeVec(tx/Math.sqrt(tx*tx+ty*ty),ty/Math.sqrt(tx*tx+ty*ty),0);
			threeVec n = findBSTPlaneNormal(sector, layer);
			
			if(Constants.isCosmicsData && Math.acos(n.dot(trkDir_t))>Math.PI/2) // flip the direction of the track for y<0 for cosmics
				trkDir_t = new threeVec(-trkDir_t.x(),-trkDir_t.y(),0);
			
			double TrkToPlnNormRelatAngl = Math.acos(n.dot(trkDir_t));
			double sign = Math.signum(n.cross(trkDir_t).z());	
		   // int shift = (int)((Constants.LAYRGAP*n.cross(trkDir_t).z())/Constants.PITCH);
		    //
		    //correction to the pitch to take into account the grading of the angle -- get the upper or lower strip depending on the trkdir
			double pitchcorr = Constants.PITCH;
			
			 if(s2>2 && s2<255) {
				double pitchToNextStrp = Math.abs(getXAtZ(layer, (double) s2+1,z)-getXAtZ(layer, (double) s2,z)); 
				double pitchToPrevStrp = Math.abs(getXAtZ(layer, (double) s2-1,z)-getXAtZ(layer, (double) s2,z)); 
				pitchcorr = (pitchToNextStrp+pitchToPrevStrp)/2;
			}
			if(s2<=2)
				pitchcorr = Math.abs(getXAtZ(layer, (double) s2+1,z)-getXAtZ(layer, (double) s2,z)); 
			if(s2==256)
				pitchcorr = Math.abs(getXAtZ(layer, (double) s2-1,z)-getXAtZ(layer, (double) s2,z)); 
			
			double layerGap = Constants.MODULERADIUS[1][0]-Constants.MODULERADIUS[0][0];
			
			double shift = sign*layerGap*Math.tan(TrkToPlnNormRelatAngl)/pitchcorr;
			//System.out.println("pitch "+pitchcorr+" shift is "+ shift +" at layer "+layer +" and sector "+sector);
			return shift;
		}
		//***
	    public double planeNormDotTrkDir(int sector, int layer, threeVec trkDir, double s2, double z) {
			double tx = trkDir.x();
			double ty = trkDir.y();
			threeVec trkDir_t = new threeVec(tx/Math.sqrt(tx*tx+ty*ty),ty/Math.sqrt(tx*tx+ty*ty),0);
			threeVec n = findBSTPlaneNormal(sector, layer);
			
			return Math.abs(n.dot(trkDir_t));
	    }
			
		
		//***
		public  threeVec intersectionOfHelixWithPlane(int layer, int sector, Helix helix) {		    
			
			int nstep = 1;
			double stepSize = 0.001;
			
			double Theta = Math.atan2((Constants.ACTIVESENWIDTH/2),Constants.MODULERADIUS[layer-1][sector-1]);
			double RMin = Constants.MODULERADIUS[layer-1][sector-1]; 
			double RMax = RMin/Math.cos(Theta);
			double R = RMin;
			
			threeVec InterPoint = helix.getPointAtRadius(R);
			
			double minDelta = RMax-RMin;
			
			while(R<RMax) {
				
				threeVec I = helix.getPointAtRadius(R);
				threeVec Inorm = I.mult(1./I.len());
				
				double Rinter =  RMin/findBSTPlaneNormal( sector, layer).dot(Inorm);
				
				if(Math.abs(Rinter-I.rt())<minDelta) {
					InterPoint = I;
					minDelta = Math.abs(Rinter-I.rt());
				}
				R+=nstep*stepSize;
				nstep++;
				
					
			}
			return InterPoint;
			
							
		}
		
		
	      public static void main (String arg[]) throws FileNotFoundException {
	    	 
	    	 
	    	  
	    	  Constants.Load();
	    	  Geometry geo = new Geometry();
	    	  
	    	  
	    	  System.out.println(geo.findBSTPlaneNormal(6, 1).toString());
	    	  threeVec p1i = new threeVec(-17.35,-68.28,-286.45);
	    	  threeVec p2i = new threeVec(17.35,-68.28,-286.45);
	    	  threeVec p3i = new threeVec(3.5,-68.28,122.42);
	    	  p1i.diffi(p3i);
	    	  p2i.diffi(p3i);
	    	  
	    	  threeVec ni = (p1i.cross(p2i));
	    	  ni.multi(1./ni.len());
	    	  System.out.println(ni.toString());
	    	  
	    	  threeVec p1 = new threeVec(-17.33,-68.30,-286.24);
	    	  threeVec p2 = new threeVec(17.34,-68.21,-286.18);
	    	  threeVec p3 = new threeVec(3.97,-68.70,122.60);
	    	  p1.diffi(p3);
	    	  p2.diffi(p3);
	    	  
	    	  threeVec n = (p1.cross(p2));
	    	  n.multi(1./n.len());
	    	  System.out.println(n.toString());
	    	  
	    	  
	    	  
	    	// global rotation angle
				double Glob_rangl = ((double) (6-1)/(double) Constants.NSECT[1-1])*2.*Math.PI + Constants.PHI0[1-1];
				// angle to rotate to global frame
				double Loc_to_Glob_rangl = Glob_rangl-Constants.LOCZAXISROTATION;

				
				
				//rotate and translate
				double cosRotation = Math.cos(Loc_to_Glob_rangl);
				double sinRotation = Math.sin(Loc_to_Glob_rangl);

				System.out.println(cosRotation+" "+sinRotation);
	    	  
	    	  System.out.println(n.dot(p3));
	    	  System.out.println(ni.dot(p3i));
	    	  
	    	  PrintWriter pw = new PrintWriter(new File("/Users/ziegler/bst_geo_test.txt"));
	    	  pw.printf(geo.findBSTPlaneNormal(6, 1).toString());
	    	  pw.close();
	    	  //System.out.println(Constants.MODULEPLANES.get(0).get(0).hasIntersection(1));
	    	  /*
	    		PrintWriter pw = new PrintWriter(new File("/Users/ziegler/bst_new_strips.txt"));
	    		
	    		pw.println("! layer sector strip localx1 localy1 localz1 localx2 localy2 localz2 x1 y1 z1 x2 y2 z2");
	    		
	    		for(int layer = 1; layer <=8; layer ++){
	    			for(int sector = 1; sector <= Constants.NSECT[layer-1]; sector ++){
	    				
	    				for(int strip = 1; strip <= Constants.NSTRIP; strip++){
	    					
	    					LineSegment line =getEndPoints(layer, sector, strip);
	    					LineSegment lineLCS =getLocalCoordinates(layer, strip);
	    					threeVec localp1 = lineLCS.getFirstEndPoint();
	    					threeVec localp2 = lineLCS.getSecondEndPoint();
	    					threeVec p1 = line.getFirstEndPoint();
	    					threeVec p2 = line.getSecondEndPoint();
	    					pw.printf("%d %d %d %f %f %f %f %f %f %f %f %f %f %f %f\n", layer, sector, strip,
	    							localp1.x(), localp1.y(), localp1.z(), localp2.x(), localp2.y(), localp2.z(),
	    							p1.x(), p1.y(), p1.z(), p2.x(), p2.y(), p2.z());
	    					
	    				}
	    			}
	    		}
	    	
	    		pw.close();
	    		*/
	    	  ///
	    	  /*
	    		PrintWriter pw = new PrintWriter(new File("/Users/ziegler/bst_geo.txt"));
	    		
	    		pw.println("! layer sector x0 y0 x1 y1 z0 z1 z2 z3 z4 z5 ");
	    		
	    		for(int layer = 1; layer <=8; layer ++){
	    			for(int sector = 1; sector <= Constants.NSECT[layer-1]; sector ++){
	    				System.out.println("sector "+sector +" layer " + layer 
	    						+" x0 " +getPlaneModuleOrigin( layer,  sector).x()+" y0 " +getPlaneModuleOrigin( layer,  sector).y()
	    						+" x1 "+getPlaneModuleEndPts(layer, sector).x()+" y1 "+getPlaneModuleEndPts(layer, sector).y()
	    						+" z0 "+getPlaneModuleOrigin( layer,  sector).z()
	    						+" z1 "+(getPlaneModuleOrigin( layer,  sector).z()+Constants.ACTIVESENLEN)
	    						+" z2 "+(getPlaneModuleOrigin( layer,  sector).z()+Constants.ACTIVESENLEN+1*Constants.DEADZNLEN)
	    						+" z3 "+(getPlaneModuleOrigin( layer,  sector).z()+2*Constants.ACTIVESENLEN+1*Constants.DEADZNLEN)
	    						+" z4 "+(getPlaneModuleOrigin( layer,  sector).z()+2*Constants.ACTIVESENLEN+2*Constants.DEADZNLEN)
	    						+" z5 "+(getPlaneModuleOrigin( layer,  sector).z()+3*Constants.ACTIVESENLEN+2*Constants.DEADZNLEN)
	    						
	    						);
	    				
	    					
	    				pw.printf("%d %d %f %f %f %f %f %f %f %f %f %f\n", layer, sector,
	    				getPlaneModuleOrigin( layer,  sector).x(),getPlaneModuleOrigin( layer,  sector).y(),
						getPlaneModuleEndPts(layer, sector).x(),getPlaneModuleEndPts(layer, sector).y(),
						getPlaneModuleOrigin( layer,  sector).z(),
						(getPlaneModuleOrigin( layer,  sector).z()+Constants.ACTIVESENLEN),
						(getPlaneModuleOrigin( layer,  sector).z()+Constants.ACTIVESENLEN+1*Constants.DEADZNLEN),
						(getPlaneModuleOrigin( layer,  sector).z()+2*Constants.ACTIVESENLEN+1*Constants.DEADZNLEN),
						(getPlaneModuleOrigin( layer,  sector).z()+2*Constants.ACTIVESENLEN+2*Constants.DEADZNLEN),
						(getPlaneModuleOrigin( layer,  sector).z()+3*Constants.ACTIVESENLEN+2*Constants.DEADZNLEN));
	    					
	    				
	    			}
	    		}
	    		pw.close();
	    		*/
	    	
	  	}
		
}
