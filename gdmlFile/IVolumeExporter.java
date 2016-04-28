package volume_geometry;

import org.jlab.geom.geant.Geant4Basic;

public interface IVolumeExporter {

	public void setVerbose( boolean aBool );
	
	public void setPositionLoc( String aLoc );
	public void setRotationLoc( String aLoc );
	
	public void setAngleUnit( String aAngleUnit );
	
	public void addMaterialPreset( String aName );
	public void addMaterialPreset( String aName, String aMatRef );
	
	public void addTopVolume( Geant4Basic aTopVol );
	public void addTopVolume( Geant4Basic aTopVol, String aMatRef );
	
	public void replaceMat( Geant4Basic aNode, String aSearch, String aMatRef );
	
	public void writeFile( String aFileNameWithoutExtension );
	
}
