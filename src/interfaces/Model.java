package interfaces;

public interface Model {
	public SpaceGroup getSpaceGroup();
	public setSpaceGroup(SpaceGroupID id);
	
	public Vector3D getPoint();
	public void setPoint(Vector3D point);
}
