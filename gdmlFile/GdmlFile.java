package volume_geometry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jlab.geom.geant.Geant4Basic;

public class GdmlFile implements IVolumeExporter
{
	private DocumentBuilderFactory mDocFactory;
	private DocumentBuilder mDocBuilder;
	private Document mDoc;
	private Element mRoot, mDefine, mMaterials, mSolids, mStructure, mSetup;
	private boolean mVerbose = false;
	private String mPositionLoc = "global", mRotationLoc = "global";
	private String mAngleUnit = "deg";
	
	
	public GdmlFile() throws ParserConfigurationException
	{
		mDocFactory = DocumentBuilderFactory.newInstance();
		mDocBuilder = mDocFactory.newDocumentBuilder();
		mDoc = mDocBuilder.newDocument();
		
		// / root
		mRoot = mDoc.createElement("gdml");
		mRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		mRoot.setAttribute("xsi:noNameSpaceSchemaLocation", "http://cern.ch/service-spi/app/releases/GDML/Schema/gdml.xsd");
		mDoc.appendChild( mRoot );
		
		// /define List
		mDefine = mDoc.createElement("define");
		mRoot.appendChild( mDefine );
		
		// /materials List
		mMaterials = mDoc.createElement("materials");
		mRoot.appendChild( mMaterials );
		
		// /solids List
		mSolids = mDoc.createElement("solids");
		mRoot.appendChild( mSolids );
		
		// /structures List
		mStructure = mDoc.createElement("structure");
		mRoot.appendChild( mStructure );
		
		// /setup Setup
		mSetup = mDoc.createElement("setup");
		mSetup.setAttribute("name", "default");
		mSetup.setAttribute("version", "1.0");
		mRoot.appendChild( mSetup );
	}
	
	
	
	public void setVerbose( boolean aBool )
	{ // from VolumeExporter interface
		mVerbose = aBool;
	}
	
	
	
	public void setPositionLoc( String aLoc )
	{
		mPositionLoc = aLoc.toLowerCase();
	}
	
	
	
	public void setRotationLoc( String aLoc )
	{
		mRotationLoc = aLoc.toLowerCase();
	}
	
	
	
	public void setAngleUnit( String aAngleUnit ) throws IllegalArgumentException
	{
		switch( aAngleUnit )
		{
		case "deg":
		case "rad":
			mAngleUnit = aAngleUnit;
			break;
		default:
			throw new IllegalArgumentException("unknown unit: "+aAngleUnit );
		}
	}
	
	
	
	public void addTopVolume( Geant4Basic aTopVol )
	{ // from VolumeExporter interface
		this.addLogicalTree( aTopVol );
		this.addPhysicalTree( aTopVol );
		this.addWorld( aTopVol.getName() );
	}
	
	
	
	public void addTopVolume( Geant4Basic aTopVol, String aMatRef )
	{ // from VolumeExporter interface
		this.addMaterialPreset( aMatRef );
		this.addLogicalTree( aTopVol, aMatRef );
		this.addPhysicalTree( aTopVol );
		this.addWorld( aTopVol.getName() );
	}
	
	
	
	public void writeFile( String aFilename )
	{
		try {
			this.write( aFilename );
		} catch (IllegalArgumentException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void addPosition( String aName, double[] aPosition, String aUnits )
	{
		if( aName.isEmpty() )
			throw new IllegalArgumentException("empty String aName");
		if( aPosition == null )
			throw new IllegalArgumentException("empty double[]");
		if( aUnits.isEmpty() )
			throw new IllegalArgumentException("empty String aUnits");
		
		Element position = mDoc.createElement("position");
		position.setAttribute("name", aName );
		position.setAttribute("x", Double.toString( aPosition[0] ) );
		position.setAttribute("y", Double.toString( aPosition[1] ) );
		position.setAttribute("z", Double.toString( aPosition[2] ) );
		position.setAttribute("unit", aUnits );
		mDefine.appendChild( position );
		
		if(mVerbose) { System.out.println("added position \""+ aName +"\""); }
	}
	
	
	
	public void addRotation( String aName, double[] aRotation, String aOrder, String aUnits )
	{
		if( aName.isEmpty() )
			throw new IllegalArgumentException("empty String aName");
		if( aRotation == null )
			throw new IllegalArgumentException("empty double[]");
		if( aOrder.isEmpty() )
			throw new IllegalArgumentException("empty String aOrder");
		if( aUnits.isEmpty() )
			throw new IllegalArgumentException("empty String aUnits");
		
		
		Element rotation = mDoc.createElement("rotation");
		rotation.setAttribute("name", aName );
		switch( aOrder )
		{
		case "xyz":
			rotation.setAttribute("x", Double.toString( aRotation[0] ) );
			rotation.setAttribute("y", Double.toString( aRotation[1] ) );
			rotation.setAttribute("z", Double.toString( aRotation[2] ) );
			break;
			
		case "yzx":
			rotation.setAttribute("y", Double.toString( aRotation[0] ) );
			rotation.setAttribute("z", Double.toString( aRotation[1] ) );
			rotation.setAttribute("x", Double.toString( aRotation[2] ) );
			break;
			
		case "zxy":
			rotation.setAttribute("z", Double.toString( aRotation[0] ) );
			rotation.setAttribute("x", Double.toString( aRotation[1] ) );
			rotation.setAttribute("y", Double.toString( aRotation[2] ) );
			break;
			
		case "zyx":
			rotation.setAttribute("z", Double.toString( aRotation[0] ) );
			rotation.setAttribute("y", Double.toString( aRotation[1] ) );
			rotation.setAttribute("x", Double.toString( aRotation[2] ) );
			break;
			
		case "yxz":
			rotation.setAttribute("y", Double.toString( aRotation[0] ) );
			rotation.setAttribute("x", Double.toString( aRotation[1] ) );
			rotation.setAttribute("z", Double.toString( aRotation[2] ) );
			break;
			
		default:
			throw new IllegalArgumentException("unknown order \""+ aOrder +"\"");
		}
		rotation.setAttribute("unit", aUnits );
		mDefine.appendChild( rotation );
		
		if(mVerbose) { System.out.println("added position \""+ aName +"\""); }
	}
	
	
	
	public void addMaterial( String aName, int aZ, double aDensity, String aDensityUnit, double aAtom, String aAtomUnit ) throws IllegalArgumentException 
	{		
		if( aName.isEmpty() )
			throw new IllegalArgumentException("empty String aName");
		if( aZ < 1 )
			throw new IllegalArgumentException("zero/negative aZ");
		if( aDensity < 0.0)
			throw new IllegalArgumentException("negative density");
		if( aDensityUnit.isEmpty() )
			throw new IllegalArgumentException("empty String aDensityUnit");
		if( aAtom < 0.0 )
			throw new IllegalArgumentException("negative aAtom");
		if( aAtomUnit.isEmpty() )
			throw new IllegalArgumentException("empty String aAtomUnit");
		
		// /materials/material
		Element material = mDoc.createElement("material");
		material.setAttribute("name", aName );
		material.setAttribute("Z", Integer.toString( aZ ) );
		mMaterials.appendChild( material );
		
		// /materials/material/density
		Element density = mDoc.createElement("D");
		density.setAttribute("unit", aDensityUnit );
		density.setAttribute("value", Double.toString( aDensity ) );
		material.appendChild( density );

		// /materials/material/atom
		Element atom = mDoc.createElement("atom");
		atom.setAttribute("unit", aAtomUnit );
		atom.setAttribute("value", Double.toString( aAtom ) );
		material.appendChild( atom );
		
		if(mVerbose) { System.out.println("added material \""+ aName +"\""); }
	}
	
	
	
	public void addMaterialPreset( String aMatRef ) throws IllegalArgumentException
	{
		if( aMatRef.isEmpty() )
			throw new IllegalArgumentException("empty String aMatRef");
		
		aMatRef.toLowerCase();
		switch( aMatRef )
		{		
		case "mat_vacuum":
			this.addMaterial( aMatRef, 1, 0.0, "g/cm3", 0.0, "g/mole");
			break;
			
		default:
			throw new IllegalArgumentException("material: \""+ aMatRef +"\"");
		}
	}
	
	
	
	public void addMaterialPreset( String aName, String aMatRef ) throws IllegalArgumentException
	{
		if( aName.isEmpty() )
			throw new IllegalArgumentException("empty String aName");
		if( aMatRef.isEmpty() )
			throw new IllegalArgumentException("empty String aMatRef");
		
		aName.toLowerCase();
		switch( aMatRef )
		{		
		case "mat_vacuum":
			this.addMaterial( aName, 1, 0.0, "g/cm3", 0.0, "g/mole");
			break;
			
		default:
			throw new IllegalArgumentException("material: \""+ aMatRef +"\"");
		}
	}
	
	
	
	public void addSolid( Geant4Basic aSolid ) throws IllegalArgumentException 
	{
		if( aSolid == null )
			throw new IllegalArgumentException("empty Geant4Basic"); // should this be NullPointerException?
		
		// /solids/solid
		String type = aSolid.getType().toLowerCase();
		Element solid = mDoc.createElement( type );
		String solRef = "sol_"+ aSolid.getName().toLowerCase();
		solid.setAttribute("name", solRef );
		
		// types defined here: http://gdml.web.cern.ch/GDML/doc/GDMLmanual.pdf
		switch( type )
		{
		case "box":
			solid.setAttribute("x", Double.toString( aSolid.getParameters()[0] ) );
			solid.setAttribute("y", Double.toString( aSolid.getParameters()[1] ) );
			solid.setAttribute("z", Double.toString( aSolid.getParameters()[2] ) );
			break;
			
		case "eltube":
			solid.setAttribute("dx", Double.toString( aSolid.getParameters()[0] ) );
			solid.setAttribute("dy", Double.toString( aSolid.getParameters()[1] ) );
			solid.setAttribute("dz", Double.toString( aSolid.getParameters()[2] ) );
			break;
			
		case "orb":
			solid.setAttribute("r", Double.toString( aSolid.getParameters()[0] ));
			break;
			
		default:
			throw new IllegalArgumentException("type: \""+ type +"\"");
		}
		
		solid.setAttribute("lunit", aSolid.getUnits() );
		mSolids.appendChild( solid );
		
		if(mVerbose) { System.out.println("added solid \""+ solRef +"\""); }
	}
	
	
	
	public void addLogicalVolume( String aMaterialRef, Geant4Basic aSolid ) throws NullPointerException, IllegalArgumentException
	{		
		if( aMaterialRef.isEmpty() )
			throw new IllegalArgumentException("empty String");
		if( aSolid == null )
			throw new IllegalArgumentException("empty Geant4Basic");
		
		// logical volumes combine a solid with a material, but are not rendered
		
		String solName = aSolid.getName().toLowerCase();
		
		// check that solid exists
		String solRef = "sol_"+ solName;
		Element sol = _findChildByName( mSolids, solRef );
		if( sol == null )
			throw new NullPointerException("could not find solid \""+ solRef +"\"");
		
		// /structures/volume Logical Volume
		Element logVol = mDoc.createElement( "volume" );
		logVol.setAttribute("name", "vol_"+ solName );
		mStructure.appendChild( logVol );

		// /structures/volume/materialref Reference to Material
		Element materialref = mDoc.createElement("materialref");
		materialref.setAttribute("ref", aMaterialRef );
		logVol.appendChild( materialref );

		// /structures/volume/solidref Reference to Solid
		Element solidref = mDoc.createElement("solidref");
		solidref.setAttribute("ref", solRef );
		logVol.appendChild( solidref );
		
		if(mVerbose) { System.out.println("added logical volume \""+ "vol_"+ solName +"\""); }
	}
	
	
	
	public void addLogicalVolume( Geant4Basic aSolid ) throws NullPointerException, IllegalArgumentException
	{		
		if( aSolid == null )
			throw new IllegalArgumentException("empty Geant4Basic");
		
		// logical volumes combine a solid with a material, but are not rendered
		
		String solName = aSolid.getName().toLowerCase();
		
		// check that solid exists
		String solRef = "sol_"+ solName;
		Element sol = _findChildByName( mSolids, solRef );
		if( sol == null )
		{
			throw new NullPointerException("could not find solid \""+ solRef +"\"");
		}
		
		// /structures/volume Logical Volume
		Element logVol = mDoc.createElement( "volume" );
		logVol.setAttribute("name", "vol_"+ solName );
		mStructure.appendChild( logVol );

		// /structures/volume/solidref Reference to Solid
		Element solidref = mDoc.createElement("solidref");
		solidref.setAttribute("ref", solRef );
		logVol.appendChild( solidref );
		
		if(mVerbose) { System.out.println("added logical volume \""+ "vol_"+ solName +"\""); }
	}
	
	
	
	public void addLogicalTree( Geant4Basic aNode, String aMatRef ) // recursively iterate over all children to add logical volumes in the correct order (children first)
	{ // global material aMatRef
		List<Geant4Basic> children = aNode.getChildren();
		for( int i = 0; i < children.size(); i++ )
		{
			Geant4Basic child = children.get( i );
			this.addLogicalTree( child, aMatRef ); // recursive
		}
		this.addSolid( aNode );
		this.addLogicalVolume( aMatRef, aNode );
	}

	
	
	public void addLogicalTree( Geant4Basic aNode ) // recursively iterate over all children to add logical volumes in the correct order (children first)
	{ // global material aMatRef		
		List<Geant4Basic> children = aNode.getChildren();
		for( int i = 0; i < children.size(); i++ )
		{
			Geant4Basic child = children.get( i );
			this.addLogicalTree( child ); // recursive
		}
		this.addSolid( aNode );
		this.addLogicalVolume( aNode );
	}
		
	
	
	public void addPhysicalVolume( String aParentName, Geant4Basic aSolid, String aAngleUnit ) throws NullPointerException, IllegalArgumentException
	{
		// Physical Volumes always have a position and a rotation in space, but this can be defined from a global or local reference 
		if( aParentName.isEmpty() )
			throw new IllegalArgumentException("empty String aParentName");
		if( aAngleUnit.isEmpty() )
			throw new IllegalArgumentException("empty String aAngleUnit");
		if( aSolid == null)
			throw new IllegalArgumentException("empty Geant4Basic");
		
		// physical volumes are rendered, and need to be added as a child to a logical volume
		// physvols can be given a direct position, or a positionref that uses a global position in the define block, same with rotations

		// /structures/volume/physvol Physical Volume
		Element physvol = mDoc.createElement("physvol");
		// need to add physvol to a logvol given by parent ref
		String parentLogVolRef = "vol_"+ aParentName.toLowerCase();
		// check that parent logical volume exists
		Element parentLogVol = _findChildByName( mStructure, parentLogVolRef );
		if( parentLogVol == null ) {
			throw new NullPointerException("could not find logical volume \""+ parentLogVolRef +"\"");
		}
		parentLogVol.appendChild( physvol );

		// /structures/volume/physvol/volumeref Reference to Volume
		Element volumeref = mDoc.createElement("volumeref");
		String selfLogVolRef = "vol_"+ aSolid.getName().toLowerCase();
		// check that self logical volume exists
		Element selfLogVol = _findChildByName( mStructure, selfLogVolRef );
		if( selfLogVol == null )
		{
			throw new NullPointerException("could not find logical volume \""+ selfLogVolRef +"\"");
		}
		volumeref.setAttribute("ref", selfLogVolRef );
		physvol.appendChild( volumeref );
		
		// /structure/volume/physvol/position
		double[] pos = aSolid.getPosition();
		boolean posAllZero = true;
		
		for( int i = 0; i < 3; i++)
		{
			if( pos[i] != 0.0 )
			{
				posAllZero = false;
				break;
			}
		}
		
		if( !posAllZero )
		{
			switch( mPositionLoc )
			{
			case "local":
				Element position = mDoc.createElement( "position" );
				position.setAttribute( "name", "pos_"+ aSolid.getName() +"_in_"+ aParentName );
				position.setAttribute("x", Double.toString( aSolid.getPosition()[0] ) );
				position.setAttribute("y", Double.toString( aSolid.getPosition()[1] ) );
				position.setAttribute("z", Double.toString( aSolid.getPosition()[2] ) );
				position.setAttribute("unit", aSolid.getUnits() );
				physvol.appendChild( position );
				break;
				
			case "global":
				Element positionRef = mDoc.createElement( "positionref" );
				String positionName = "pos_"+ aSolid.getName() +"_in_"+ aParentName;
				this.addPosition( positionName, aSolid.getPosition(), aSolid.getUnits() );
				positionRef.setAttribute("ref", positionName );
				physvol.appendChild( positionRef );
				break;
				
			default:
				throw new IllegalArgumentException("positionLoc: \""+ mPositionLoc +"\"");
			}
		}
		
		// /structure/volume/physvol/rotation
		double[] rot = aSolid.getRotation();
		boolean rotAllZero = true;
		
		for( int i = 0; i < 3; i++) {
			if( rot[i] != 0.0 ) {
				rotAllZero = false;
				break;
			}
		}
		
		if( !rotAllZero ) { // no need to write a blank line that doesn't do anything
			switch( mRotationLoc )
			{
			case "local":
				Element rotation = mDoc.createElement( "rotation" );
				rotation.setAttribute( "name", "rot_"+ aSolid.getName() +"_in_"+ aParentName );
				rotation.setAttribute("x", Double.toString( aSolid.getRotation()[0] ) );
				rotation.setAttribute("y", Double.toString( aSolid.getRotation()[1] ) );
				rotation.setAttribute("z", Double.toString( aSolid.getRotation()[2] ) );
				rotation.setAttribute("unit", aAngleUnit );
				physvol.appendChild( rotation );
			break;
			
			case "global":
				Element rotationRef = mDoc.createElement( "rotationref" );
				String rotationName = "rot_"+ aSolid.getName() +"_in_"+ aParentName;
				this.addRotation( rotationName, aSolid.getRotation(), aSolid.getRotationOrder(), aAngleUnit );
				rotationRef.setAttribute("ref", rotationName );
				physvol.appendChild( rotationRef );
				break;
				
			default:
				throw new IllegalArgumentException("rotationLoc: \""+ mRotationLoc +"\"");
			}
		}
		
		if(mVerbose) { System.out.println("added physical volume \""+ selfLogVolRef +"\" to logical volume \""+ parentLogVolRef +"\""); }
	}
	

	
	public void addPhysicalTree( Geant4Basic aNode ) 
	{				
		List<Geant4Basic> children = aNode.getChildren();
		for( int i = 0; i < children.size(); i++ ) {

			Geant4Basic child = children.get( i );

			try {
				this.addPhysicalVolume( aNode.getName(), child, mAngleUnit );
				
			} catch( NullPointerException e ) {
				e.printStackTrace();
			} catch( IllegalArgumentException e ) {
				e.printStackTrace();
			}
			
			this.addPhysicalTree( child ); // tail recursive?
		}
	}
			
	
	
	public void addWorld( String aLogVolName ) throws IllegalArgumentException 
	{		
		if( aLogVolName.isEmpty() )
			throw new IllegalArgumentException("empty String");

		// /setup/world World
		Element world = mDoc.createElement("world");
		String logVolRef = "vol_"+ aLogVolName.toLowerCase();
		// check that logical volume exists
		Element LogVol = _findChildByName( mStructure, logVolRef );
		if( LogVol == null ) {
			throw new NullPointerException("could not find logical volume \""+ logVolRef +"\"");
		}
		world.setAttribute("ref", logVolRef );
		mSetup.appendChild( world );
		
		if(mVerbose) { System.out.println("added world from logical volume \""+ logVolRef +"\""); }
	}
	
	
	
	public void write( String aName ) throws TransformerConfigurationException, TransformerException, IllegalArgumentException 
	{		
		if( aName.isEmpty() )
			throw new IllegalArgumentException("empty String");
	
		String filename = aName +".gdml";
		
		// write contents to gdml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(mDoc);

		StreamResult result = new StreamResult( new File( filename ) );
		//StreamResult result = new StreamResult( System.out );

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.transform(source, result);

		System.out.println("wrote file \""+ filename +"\"");
	}
	
	
	
	public void replaceMat( Geant4Basic aNode, String aSearch, String aMatRef ) throws NullPointerException
	{
		// find logical volumes whose name contains aSearch, and change the material ref to aMatRef
		
		// use XPath?
		
		
		// manually
		List<Element> logVolMatchList = _findChildrenByNameContains( mStructure, aSearch );
		if( logVolMatchList.size() == 0 )
			throw new NullPointerException("could not find any logVols with names containing \""+ aSearch +"\"");
		
		for( int i = 0; i < logVolMatchList.size(); i++ )
		{
			Element logVol = logVolMatchList.get( i );
			
			// /structures/volume/materialref Reference to Material
			NodeList materialref = logVol.getElementsByTagName("materialref");
			
			if( materialref.getLength() == 0 )
				throw new NullPointerException("no materialref found");
			if( materialref.getLength() > 1 )
				throw new NullPointerException("found multiple materialrefs?!");
		
			//materialref.item(0).
		}
		
	}
	
	
	
	private Element _findChildByName( Element aParent, String aName ) throws IllegalArgumentException 
	{
		if( aName.isEmpty() )
			throw new IllegalArgumentException("empty String");
		if( aParent == null )
			throw new IllegalArgumentException("empty Element");
		
		NodeList childNodes = aParent.getChildNodes();
		//System.out.println("GdmlFile: _findChildByName(): begin search in parent <"+ aParent.getNodeName() +"> for child with name=\""+ aName +"\">");
		
		for( int i = 0; i < childNodes.getLength(); i++)
		{
			Element child = (Element) childNodes.item( i );
			String childName = child.getAttribute("name");
			
			//System.out.println(" checking child <"+ child.getNodeName() +" name=\""+ childName +"\">");
			//System.out.println("childName="+ childName);
			//System.out.println("aName="+ aName);
			
			if( childName.equals(aName) ) // don't use childName == aName, which checks references (pointers) of the objects, and not their logical value!
			{
				//System.out.println(" found child");
				return child;
			}
		}
		//System.out.println(" no child found");
		return null;
	}
	
	
	
	private List<Element> _findChildrenByNameContains( Element aParent, String aSubName ) throws IllegalArgumentException 
	{		
		if( aSubName.isEmpty() ) {
			throw new IllegalArgumentException("empty String");
		} else if( aParent == null ) {
			throw new IllegalArgumentException("empty Element");
		}
		
		List<Element> children = new ArrayList<Element>();
		NodeList childNodes = aParent.getChildNodes();
		//System.out.println("GdmlFile: _findChildByName(): begin search in parent <"+ aParent.getNodeName() +"> for children with names that include \""+ aName +"\">");
		
		for( int i = 0; i < childNodes.getLength(); i++)
		{
			Element child = (Element) childNodes.item( i );
			String childName = child.getAttribute("name");
			
			//System.out.println(" checking child <"+ child.getNodeName() +" name=\""+ childName +"\">");
			//System.out.println("childName="+ childName);
			//System.out.println("aName="+ aName);
			
			if( childName.contains( aSubName ) ) { // don't use childName == aName, which checks references (pointers) of the objects, and not their logical value!
				//System.out.println(" found child");
				children.add( child );
			}
		}
		System.out.println("numChildrenFound="+ children.size() );
		return children;
	}
}
