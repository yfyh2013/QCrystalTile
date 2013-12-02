package interfaces;

interface Transformation {
	Matrix3D linearPart();
	Vector3D translationPart();
	
	Matrix4D getAsHomogeneous();
	
	// return a new Transformation that represents the operation of applying this after 
	Transformation composition(Transformation b);
}