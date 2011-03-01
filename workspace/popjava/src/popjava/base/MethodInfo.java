package popjava.base;

/**
 * This class represents all the informations about a method in a parallel object. This class is used to retrieve the method to invoke on a parallel object
 *
 */
public class MethodInfo {
	/**
	 * Method unqiue identifier
	 */
	private int methodId;
	/**
	 * Class unique identifier
	 */
	private int classId;

	/**
	 * Create a new MethodInfo with the given values
	 * @param classId	The class unique identifier
	 * @param methodId	The method unique identifier
	 */
	public MethodInfo(int classId, int methodId) {
		this.classId = classId;
		this.methodId = methodId;
	}

	/**
	 * Get the method unique identifier stored in this object
	 * @return The method unique identifier
	 */
	public int getMethodId() {
		return methodId;
	}

	/**
	 * Get the class unique identifier stored in this object
	 * @return The class unique identifier
	 */
	public int getClassId() {
		return classId;
	}

	/**
	 * Check if if the given object is equals to this MethodInfo
	 * @param obj	The object to compare with
	 * @return true is they are equal
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(MethodInfo.class)) {
			MethodInfo info = (MethodInfo) obj;
			if (info.getClassId() == classId && info.getMethodId() == methodId)
				return true;
		}
		return false;
	}

	/**
	 * Format the MethodInfo as a string value
	 */
	public String toString() {
		return String.format("ClassId:%d.MethodId:%d", classId, methodId);
	}
}
