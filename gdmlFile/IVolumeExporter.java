package volume_geometry;

import org.jlab.geom.geant.Geant4Basic;

public interface IVolumeExporter {

	public void setVerbose( boolean aBool );
	
	public void setPositionLoc( String aLoc );
	public void setRotationLoc( String aLoc );
	
	public void addMaterialPreset( String aName );
	public void addMaterialPreset( String aName, String aMatRef );
	
	public void addTopVolume( Geant4Basic aTopVol );
	
	public void replaceAttribute(  Geant4Basic aNode, String aParentName,
			String aSearchNode, String aSearchAttribute, String aSearchValue,
			String aReplaceNode, String aReplaceAttribute, String aReplaceValue );
	
	public void writeFile( String aFileNameWithoutExtension );
	
}
