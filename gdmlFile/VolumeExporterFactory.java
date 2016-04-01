package volume_geometry;

import javax.xml.parsers.ParserConfigurationException;

public class VolumeExporterFactory {

	public static IVolumeExporter createVolumeExporter( String aCriteria ) {
		
		switch( aCriteria.toLowerCase() ) {
		case "gdml":
			try {
				return new GdmlFile();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			break;
		}
		return null;
		
	}
	
}
