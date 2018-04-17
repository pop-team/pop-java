package ch.icosys.popjava.core.base;

/**
 * This class represents all the informations about a method in a parallel
 * object. This class is used to retrieve the method to invoke on a parallel
 * object
 *
 */
public class MethodInfo {
	/**
	 * Method unqiue identifier
	 */
	private final int methodId;

	/**
	 * Class unique identifier
	 */
	private final int classId;

	/**
	 * Create a new MethodInfo with the given values
	 * 
	 * @param classId
	 *            The class unique identifier
	 * @param methodId
	 *            The method unique identifier
	 */
	public MethodInfo(int classId, int methodId) {
		this.classId = classId;
		this.methodId = methodId;
	}

	/**
	 * Get the method unique identifier stored in this object
	 * 
	 * @return The method unique identifier
	 */
	public int getMethodId() {
		return methodId;
	}

	/**
	 * Get the class unique identifier stored in this object
	 * 
	 * @return The class unique identifier
	 */
	public int getClassId() {
		return classId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + classId;
		result = prime * result + methodId;
		return result;
	}

	/**
	 * Check if if the given object is equals to this MethodInfo
	 * 
	 * @param obj
	 *            The object to compare with
	 * @return true is they are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodInfo other = (MethodInfo) obj;
		if (classId != other.classId)
			return false;
		if (methodId != other.methodId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("ClassId:%d.MethodId:%d", classId, methodId);
	}
}
