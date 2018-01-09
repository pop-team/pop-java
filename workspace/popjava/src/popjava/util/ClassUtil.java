package popjava.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javassist.util.proxy.ProxyObject;
import popjava.annotation.POPClass;
/**
 * This class gives some static methods to look inside a class
 */
public class ClassUtil {
	
	private static final Map<Method, String> methodSignsCache = new HashMap<>();
	private static final Map<Constructor, String> constructorSignsCache = new HashMap<>();
	
	public static Class<?>[] getObjectTypes(Object ... objects){
		Class<?>[] parameterTypes = new Class<?>[objects.length];
		for (int index = 0; index < objects.length; index++) {
			if (objects[index] == null){
				parameterTypes[index] = Object.class;
			}else{
				parameterTypes[index] = objects[index].getClass();
				if(ProxyObject.class.isAssignableFrom(parameterTypes[index])){
					parameterTypes[index] = parameterTypes[index].getSuperclass();
				}
				
				if(objects[index].getClass().equals(Integer.class)){
					parameterTypes[index] = Integer.TYPE;
				}else if(objects[index].getClass().equals(Double.class)){
					parameterTypes[index] = Double.TYPE;
				}else if(objects[index].getClass().equals(Long.class)){
					parameterTypes[index] = Long.TYPE;
				}else if(objects[index].getClass().equals(Short.class)){
					parameterTypes[index] = Short.TYPE;
				}else if(objects[index].getClass().equals(Boolean.class)){
					parameterTypes[index] = Boolean.TYPE;
				}
			}
			
		}
		
		return parameterTypes;
	}
	
	/**
	 * Retrieve a specific constructor in the given class
	 * @param c					The class to look in
	 * @param parameterTypes	Parameters of the constructor to retrieve
	 * @return	The retrieved constructor
	 * @throws NoSuchMethodException	Thrown if the constructor is not found
	 */
	public static Constructor<?> getConstructor(Class<?> c,
			Class<?>... parameterTypes) throws NoSuchMethodException {

		Constructor<?>[] allConstructors = c.getConstructors();
		for (Constructor<?> constructor : allConstructors) {
			if (isSameConstructor(constructor, parameterTypes))
				return constructor;
		}
		
		String sign = getMethodSign(c.getName(), parameterTypes);
		String errorMessage = String.format(
				"Cannot find the method %s in class %s", sign, c.getName());
		throw new NoSuchMethodException(errorMessage);

	}

	/**
	 * Retrieve a specific method in the given class
	 * @param c					The class to look in
	 * @param methodName		The name of the method to retrieve
	 * @param parameterTypes	Parameters of the method to retrieve
	 * @return	The retrieved method
	 * @throws NoSuchMethodException	Thrown if the method is not found
	 */
	public static Method getMethod(Class<?> c, String methodName,
			Class<?>... parameterTypes) throws NoSuchMethodException {
		String sign = getMethodSign(methodName, parameterTypes);
		Method[] allMethods = c.getMethods();
		for (Method method : allMethods) {
			if (sign.compareTo(getMethodSign(method)) == 0)
				return method;
		}
		String errorMessage = String.format(
				"Cannot find the method %s in class %s", sign, c.getName());
		throw new NoSuchMethodException(errorMessage);

	}

	/**
	 * Get the signature of a method
	 * @param m	The method
	 * @return	Signature of the given method as a string value
	 */
	public static String getMethodSign(Method m) {
		Objects.requireNonNull(m);
		String sign = methodSignsCache.get(m);
		if (sign == null) {
			sign = getMethodSign(m.getReturnType() + "@" + m.getName(), m.getParameterTypes());
			methodSignsCache.put(m, sign);
		}
		return sign;
	}

	/**
	 * Get the signature of a constructor
	 * @param c	The constructor
	 * @return	Signature of the constructor as a string value
	 */
	public static String getMethodSign(Constructor<?> c) {
		Objects.requireNonNull(c);
		String sign = constructorSignsCache.get(c);
		if (sign == null) {
			sign= getMethodSign(c.getDeclaringClass().getName(), c.getParameterTypes());
			constructorSignsCache.put(c, sign);
		}
		return sign;
	}

	private static String getMethodSign(String name, Class<?>[] parameterTypes) {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		for (Class<?> c : parameterTypes) {
			sb.append("-");
			sb.append(getClassName(c));
		}
		return sb.toString();
	}

	/**
	 * Determines if the first class is the same or a superclass of the second
	 * @param first		First class
	 * @param second	Second class
	 * @return	true is the class is assignable
	 */
	public static boolean isAssignableFrom(Class<?> first, Class<?> second) {
		first = normalizeType(first);
		second = normalizeType(second);
		
		return first.isAssignableFrom(second);
	}

	private static Class<?> normalizeType(Class<?> first) {
		if (first.isPrimitive()) {
			if (first.equals(boolean.class)){
				first = Boolean.class;
			}else if (first.equals(byte.class)){
				first = Byte.class;
			}else if (first.equals(char.class)){
				first = Character.class;
			}else if (first.equals(short.class)){
				first = Short.class;
			}else if (first.equals(int.class)){
				first = Integer.class;
			}else if (first.equals(long.class)){
				first = Long.class;
			}else if (first.equals(float.class)){
				first = Float.class;
			}else if (first.equals(double.class)){
				first = Double.class;
			}
		}
		return first;
	}

	/**
	 * Check if the given parameters are the same as the constructor parameters
	 * @param constructor	The constructor
	 * @param params		The parameters to check
	 * @return	true if the parameters are the same
	 */
	private static boolean isSameConstructor(Constructor<?> constructor,
			Class<?>[] params) {
		if (params == null){
			return false;
		}
		Class<?>[] parameters = constructor.getParameterTypes();
		return areParameterTypesTheSame(params, parameters);
	}

	public static boolean areParameterTypesTheSame(Class<?>[] params,
			Class<?>[] constructorParameters) {
		if (constructorParameters.length > params.length
				|| (constructorParameters.length == 0 && params.length > 0))
			return false;
		for (int index = 0; index < constructorParameters.length; index++) {

			if (index == constructorParameters.length - 1) {
				if (isAssignableFrom(constructorParameters[index], params[index])) {
					if (constructorParameters.length == params.length){
						return true;
					} else {
						return false;
					}
				}else if (constructorParameters[index].isArray()) {

					Class<?> componentClass = constructorParameters[index].getComponentType();
					for (int i = index; i < params.length; i++) {
						if (!isAssignableFrom(componentClass, params[i])){
							return false;
						}
					}

					return true;
				}else{
				    return false;
				}
			} else {
				if (!isAssignableFrom(constructorParameters[index], params[index]))
					return false;
			}

		}
		return true;
	}

	/**
	 * Get the name of a class
	 * @param c	The primitive class
	 * @return	Name of the class as a string value
	 */
	private static String getClassName(Class<?> c) {
		if (c == byte.class)
			return Byte.class.getName();
		if (c == int.class)
			return Integer.class.getName();
		if (c == short.class)
			return Short.class.getName();
		if (c == long.class)
			return Long.class.getName();
		if (c == float.class)
			return Float.class.getName();
		if (c == double.class)
			return Double.class.getName();
		if (c == boolean.class)
			return Boolean.class.getName();
		if (c == char.class)
			return Character.class.getName();

		return c.getName();
	}

	/**
	 * Get a default object of a primitive class
	 * @param c	The primitive class
	 * @return	Object with default value
	 */
	public static Object getDefaultPrimitiveValue(Class<?> c) {
		if (c == byte.class)
			return (byte) 0;
		if (c == int.class)
			return 0;
		if (c == short.class)
			return (short) 0;
		if (c == long.class)
			return 0L;
		if (c == float.class)
			return 0f;
		if (c == double.class)
			return 0d;
		if (c == boolean.class)
			return Boolean.FALSE;
		if (c == char.class)
			return (char) 0;

		return null;
	}
	
	/**
	 * Generate an ID or use the one specified
	 * 
	 * @param clazz the class we want an id of
	 * @return a numeric positive identifier
	 */
	public static int classId(Class<?> clazz) {
		for (Annotation annotation : clazz.getDeclaredAnnotations()) {
			if(annotation instanceof POPClass){
				POPClass popClassAnnotation = (POPClass) annotation;
				if(popClassAnnotation.classId() != -1){
					return popClassAnnotation.classId();
				}
			}
		}

		return Math.abs(clazz.getName().hashCode());
	}
}
