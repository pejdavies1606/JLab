package VolumeExporter;

import org.jlab.geom.geant.Geant4Basic;

public interface IVolumeExporter
{
	public void setVerbose( boolean aBool );
	
	public void addTopVolume( Geant4Basic aTopVol );
	
	public void writeFile( String aFileNameWithoutExtension );
}
