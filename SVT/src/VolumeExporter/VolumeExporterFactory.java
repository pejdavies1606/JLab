package VolumeExporter;

import javax.xml.parsers.ParserConfigurationException;

public class VolumeExporterFactory
{
	public static IGdmlExporter createGdmlExporter() throws IllegalArgumentException
	{
		try {
			return new GdmlFile();
		}
		catch( ParserConfigurationException e )
		{
			e.printStackTrace();
		}
		return null;
	}
}
