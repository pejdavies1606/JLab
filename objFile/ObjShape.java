package geometry_test;

import java.util.List;
import org.jlab.geom.prim.*;

public class ObjShape {

	private List<Point3D> mPointsList;
	private List<int[]> mRefsList;
	private String mName;
	
	public ObjShape( List<Point3D> aPointsList, List<int[]> aRefsList, String aName ) {
		mPointsList = aPointsList;
		mRefsList = aRefsList;
		mName = aName;
	}
	
	public List<Point3D> getPointsList() {
		return mPointsList;
	}
	
	public List<int[]> getRefsList() {
		return mRefsList;
	}
	
	public String getName() {
		return mName;
	}
	
	public void replacePoints( List<Point3D> aPointsList ) {
		if( mPointsList.size() != 0 && mPointsList.size() == aPointsList.size() ) {
			for( int i = 0; i < aPointsList.size(); i++ ) {
				mPointsList.set( i, aPointsList.get(i) );
			}
		}
	}
	
}
