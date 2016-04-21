package volume_geometry;

import javax.xml.parsers.ParserConfigurationException;

public class VolumeExporterFactory {

	public static IVolumeExporter createVolumeExporter( String aCriteria ) throws IllegalArgumentException
	{
		switch( aCriteria.toLowerCase() )
		{
		case "gdml":
			try {
				return new GdmlFile();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			break;
		default:
			throw new IllegalArgumentException("unknown criterion");
		}
		return null;
	}
	
}
