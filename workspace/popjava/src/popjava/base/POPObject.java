package popjava.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javassist.util.proxy.ProxyObject;
import popjava.PopJava;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;
import popjava.baseobject.ConnectionType;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.util.ClassUtil;
/**
 * This class is the base class of all POP-Java parallel classes. Every POP-Java parallel classes must inherit from this one.
 */
public class POPObject implements IPOPBase {
	
	protected int refCount;
	private int classId = 0;
	protected boolean generateClassId = true;
	protected boolean definedMethodId = false;
	private boolean hasDestructor = false;	
	protected ObjectDescription od = new ObjectDescription();
	private String className = "";
	private final int startMethodIndex = 10;
	private ConcurrentHashMap<MethodInfo, Integer> semantics = new ConcurrentHashMap<MethodInfo, Integer>();
	private ConcurrentHashMap<MethodInfo, Method> methodInfos = new ConcurrentHashMap<MethodInfo, Method>();
	private ConcurrentHashMap<MethodInfo, Constructor<?>> constructorInfos = new ConcurrentHashMap<MethodInfo, Constructor<?>>();

	private boolean temporary = false;
	
	private POPObject me = null;
	
	/**
	 * Creates a new instance of POPObject
	 */
	public POPObject() {
		refCount = 0;
		className = getRealClass().getName();
		
		loadClassAnnotations();
		initializePOPObject();
	}
	
	private Class<? extends POPObject> getRealClass(){
		if(this instanceof ProxyObject){
			return (Class<? extends POPObject>) getClass().getSuperclass();
		}
		
		return getClass();
	}
	
	private void loadClassAnnotations(){
		for (Annotation annotation : getRealClass().getDeclaredAnnotations()) {
			if(annotation instanceof POPClass){
				POPClass popClassAnnotation = (POPClass) annotation;
				if(!popClassAnnotation.className().isEmpty()){
					setClassName(popClassAnnotation.className());
				}
				if(popClassAnnotation.classId() != -1){
					setClassId(popClassAnnotation.classId());
				}
				hasDestructor(popClassAnnotation.deconstructor());
			}
		}
	}
	
	/**
	 * Loads the OD from the specified constructor
	 * @param constructor
	 */
	private void loadODAnnotations(Constructor<?> constructor){
		POPObjectDescription objectDescription = constructor.getAnnotation(POPObjectDescription.class);
		if(objectDescription != null){
			od.setHostname(objectDescription.url());
			od.setJVMParamters(objectDescription.jvmParameters());
			od.setConnectionType(objectDescription.connection());
			od.setConnectionSecret(objectDescription.connectionSecret());
			od.setEncoding(objectDescription.encoding().toString());
		}
	}
	
	private void loadParameterAnnotations(Constructor<?> constructor, Object ... argvs){
		Annotation [][] annotations = constructor.getParameterAnnotations();
		for(int i = 0; i < annotations.length; i++){
			for(int loop = 0; loop < annotations[i].length; loop++){
				if(annotations[i][loop].annotationType().equals(POPConfig.class)){
					POPConfig config = (POPConfig)annotations[i][loop];
					 
					if(argvs[i] == null){
						throw new InvalidParameterException("Annotated paramater "+i+" for "+getClassName()+" was is null");
					}
					
					switch(config.value()){
					case URL:
						if(argvs[i] instanceof String){
							od.setHostname((String)argvs[i]);
						}else{
							throw new InvalidParameterException("Annotated paramater "+i+" in "+getClassName()+
									" was not of type String for Annotation URL");
						}
						
						break;
					case CONNECTION:
						if(argvs[i] instanceof ConnectionType){
							od.setConnectionType((ConnectionType) argvs[i]);
						}else{
							throw new InvalidParameterException("Annotated paramater "+i+" in "+getClassName()+
									" was not of type ConnectionType for Annotation CONNECTION");
						}
						break;
					case CONNECTION_PWD:
						if(argvs[i] instanceof String){
							od.setConnectionSecret((String)argvs[i]);
						}else{
							throw new InvalidParameterException("Annotated paramater "+i+" in "+getClassName()+
									" was not of type String for Annotation CONNECTION_SECRET");
						}
						break;
					}
					
				}
			}
		}
	}
	
	/**
	 * Loads the OD from the annotated attributes
	 */
	private void loadDynamicOD(Constructor<?> constructor, Object ... argvs){
		loadODAnnotations(constructor);
		loadParameterAnnotations(constructor, argvs);
	}
	
	public void loadPOPAnnotations(Constructor<?> constructor, Object ... argvs){
		loadDynamicOD(constructor, argvs);
		loadMethodSemantics();
	}
	
	private void throwMultipleAnnotationsError(Class<?> c, Method method){
		throw new InvalidParameterException("Can not declare mutliple POP Semantics for same method "+c.getName()+":"+method.getName());
	}
	
	private boolean isMethodPOPAnnotated(Method method){
		if(method.isAnnotationPresent(POPSyncConc.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPSyncSeq.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPSyncMutex.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPAsyncConc.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPAsyncSeq.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPAsyncMutex.class)){
			return true;
		}
		
		return false;
	}
	
	private int methodId(Method method, int defaultID){
	    int id = -1;
	    
	    if(method.isAnnotationPresent(POPSyncConc.class)){
	        id = method.getAnnotation(POPSyncConc.class).id();
        }
        
        if(method.isAnnotationPresent(POPSyncSeq.class)){
            id =  method.getAnnotation(POPSyncSeq.class).id();
        }
        
        if(method.isAnnotationPresent(POPSyncMutex.class)){
            id =  method.getAnnotation(POPSyncMutex.class).id();
        }
        
        if(method.isAnnotationPresent(POPAsyncConc.class)){
            id =  method.getAnnotation(POPAsyncConc.class).id();
        }
        
        if(method.isAnnotationPresent(POPAsyncSeq.class)){
            id =  method.getAnnotation(POPAsyncSeq.class).id();
        }
        
        if(method.isAnnotationPresent(POPAsyncMutex.class)){
            id =  method.getAnnotation(POPAsyncMutex.class).id();
        }
        
        if(id >= 0){
            return id;
        }
	    return defaultID;
	}
	
	private void loadMethodSemantics(){
		Class<?> c = getRealClass();
		
		for(Method method: c.getDeclaredMethods()){
			int semantic = -1;
			
			//Sync
			if(method.isAnnotationPresent(POPSyncConc.class)){
				if(semantic != -1){
					throwMultipleAnnotationsError(c, method);
				}
				semantic = Semantic.SYNCHRONOUS | Semantic.CONCURRENT;
			}
			if(method.isAnnotationPresent(POPSyncSeq.class)){
				if(semantic != -1){
					throwMultipleAnnotationsError(c, method);
				}
				semantic = Semantic.SYNCHRONOUS | Semantic.SEQUENCE;
			}
			if(method.isAnnotationPresent(POPSyncMutex.class)){
				if(semantic != -1){
					throwMultipleAnnotationsError(c, method);
				}
				semantic = Semantic.SYNCHRONOUS | Semantic.MUTEX;
			}
			//Async
			if(method.isAnnotationPresent(POPAsyncConc.class)){
				if(semantic != -1){
					throwMultipleAnnotationsError(c, method);
				}
				semantic = Semantic.ASYNCHRONOUS | Semantic.CONCURRENT;
			}
			if(method.isAnnotationPresent(POPAsyncSeq.class)){
				if(semantic != -1){
					throwMultipleAnnotationsError(c, method);
				}
				semantic = Semantic.ASYNCHRONOUS | Semantic.SEQUENCE;
			}
			if(method.isAnnotationPresent(POPAsyncMutex.class)){
				if(semantic != -1){
					throwMultipleAnnotationsError(c, method);
				}
				semantic = Semantic.ASYNCHRONOUS | Semantic.MUTEX;
			}
			
			if(semantic != -1){
				addSemantic(c, method.getName() , semantic);
			}
        }
	}

	/**
	 * Initialize the method identifiers of a POPObject
	 * @param c	the class to initialize
	 */
	protected final void initializePOPObject() {
		if (generateClassId){
			classId++;
		}
		
		Class<?> c = getRealClass();
		if (!c.equals(POPObject.class)) {
			int startIndex = initializeConstructorInfo(c, startMethodIndex);
			if (hasDestructor) {
				startIndex++;
			}
			initializeMethodInfo(c, startIndex);
		}
	}
	
	/**
	 * Specify if the parallel object is running like a deamon
	 * @return true if it's a deamon
	 */
	public boolean isDaemon() {
		return false;
	}

	/**
	 * Ask if the object can be killed
	 * @return	true if the object can be killed
	 */
	public final boolean canKill() {
		return true;
	}

	/**
	 * Get the object description of the POPObject
	 * @return the object description of the POPObject
	 */
	public final ObjectDescription getOd() {
		return od;
	}

	/**
	 * Set a new object description to the POPObject
	 * @param od	the new object description
	 */
	public final void setOd(ObjectDescription od) {
		this.od = od;
	}

	/**
	 * Retrieve the access point of the parallel object
	 * @return	POPAccessPoint object containing all access points to the parallel object
	 */
	public POPAccessPoint getAccessPoint() {
		return Broker.getAccessPoint();
	}

	/**
	 * Retrieve the class name of the parallel object
	 * @return	class name as a String value
	 */
	public final String getClassName() {
		return className;
	}

	/**
	 * Set the class name
	 * @param className	the class name
	 */
	protected final void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Return the value of the hasDestrcutor variable
	 * @return	true if the parclass has a destrcutor
	 */
	protected final boolean hasDestructor() {
		return hasDestructor;
	}

	/**
	 * Set the destructor value. Must be set to true if the parclass has a destructor
	 * @param hasDestructor	set to true if the parclass has a destructor
	 */
	protected final void hasDestructor(boolean hasDestructor) {
		this.hasDestructor = hasDestructor;
	}

	/**
	 * Get the class unique identifier
	 * @return the class unique identifier
	 */
	public final int getClassId() {
		return classId;
	}

	/**
	 * Set the class unique identifier
	 * @param classId	the class unique identifier
	 */
	protected final void setClassId(int classId) {
		generateClassId = false;
		this.classId = classId;
	}

	/**
	 * Retrieve a specific method in the parallel class with some information
	 * @param info	informations about the method to retrieve
	 * @return	A method object that represent the method found in the parallel class
	 * @throws NoSuchMethodException	thrown is the method is not found
	 */
	public Method getMethodByInfo(MethodInfo info) throws NoSuchMethodException {
		Method method = null;
		Enumeration<MethodInfo> keys = methodInfos.keys();
		while (keys.hasMoreElements()) {
			MethodInfo key = keys.nextElement();
			if (key.equals(info)) {
				method = findSuperMethod(methodInfos.get(key));
				break;
			}
		}
		if (method == null){
			throw new NoSuchMethodException();
		}
		
		return method;
	}

	/**
	 * Retrieve a specific method in the super class
	 * @param method	informations about the method to retrieve
	 * @return	A method object that represent the method found in the parallel class or null
	 */
	private Method findSuperMethod(Method method) {
		String findingSign = ClassUtil.getMethodSign(method);
		Class<?> findingClass = method.getDeclaringClass();
		Method result = method;
		Enumeration<MethodInfo> keys = methodInfos.keys();
		while (keys.hasMoreElements()) {
			MethodInfo key = keys.nextElement();
			Method m = methodInfos.get(key);
			String methodSign = ClassUtil.getMethodSign(m);
			if (findingSign.equals(methodSign)) {
				if (findingClass.isAssignableFrom(m.getDeclaringClass())) {
					findingClass = m.getDeclaringClass();
					result = m;
				}
			}
		}
		return result;
	}

	/**
	 * Retrieve a constructor by its informations
	 * @param info	Informations about the constructor to retrieve
	 * @return	The constructor found
	 * @throws NoSuchMethodException	thrown if no constrcutor is found
	 */
	public Constructor<?> getConstructorByInfo(MethodInfo info)
			throws NoSuchMethodException {

		Enumeration<MethodInfo> keys = constructorInfos.keys();
		while (keys.hasMoreElements()) {
			MethodInfo key = keys.nextElement();
			if (key.equals(info))
				return constructorInfos.get(key);
		}

		throw new NoSuchMethodException();
	}

	/**
	 * Retrieve a method by its informations
	 * @param method	Informations about the method to retrieve
	 * @return	The method found
	 */
	public MethodInfo getMethodInfo(Method method) {
		//TODO: potentially slow
		MethodInfo methodInfo = new MethodInfo(0, 0);
		String findingSign = ClassUtil.getMethodSign(method);
		Class<?> findingClass = method.getDeclaringClass();
		if (methodInfos.containsValue(method)) {
			Enumeration<MethodInfo> keys = methodInfos.keys();
			while (keys.hasMoreElements()) {
				MethodInfo key = keys.nextElement();
				Method m = methodInfos.get(key);
				String methodSign = ClassUtil.getMethodSign(m);
				if (findingSign.equals(methodSign)) {
					if (m.getDeclaringClass().isAssignableFrom(findingClass)) {
						findingClass = m.getDeclaringClass();
						methodInfo = key;
					}
				}
			}
		}
		return methodInfo;
	}

	/**
	 * Retrieve a specific method by its constructor informations
	 * @param constructor	Informations about the constrcutor
	 * @return	The method found
	 */
	public MethodInfo getMethodInfo(Constructor<?> constructor) {	    
		if (constructorInfos.containsValue(constructor)) {
			Enumeration<MethodInfo> keys = constructorInfos.keys();
			while (keys.hasMoreElements()) {
				MethodInfo key = keys.nextElement();
				if (constructorInfos.get(key).equals(constructor))
					return key;
			}
		}
		
		throw new RuntimeException("Could not find constructor "+constructor.toString());
	}

	/**
	 * Retrieve the invocation semantic of a specific method
	 * @param methodInfo	informations about the specific method
	 * @return	int value representing the semantics of the method
	 */
	public int getSemantic(MethodInfo methodInfo) {
		if (semantics.containsKey(methodInfo)) {
			return semantics.get(methodInfo);
		} else {
			return Semantic.SYNCHRONOUS;
		}
	}

	/**
	 * Retrieve the invocation semantic of a specific method
	 * @param method	method to look at
	 * @return	int value representing the semantics of the method
	 */
	public int getSemantic(Method method) {
		MethodInfo methodInfo = getMethodInfo(method);
		return getSemantic(methodInfo);
	}

	/**
	 * Set an invocation semantic to a specific method. 
	 * @param c				class of the method
	 * @param methodName	method to modify
	 * @param semantic		semantic to set on the method
	 */
	public final void addSemantic(Class<?> c, String methodName, int semantic) {
		Method[] allMethods = c.getDeclaredMethods();
		if (allMethods.length > 0) {
			for (Method m : allMethods) {
				if (m.getName().equals(methodName)) {
					MethodInfo methodInfo = getMethodInfo(m);
					if (methodInfo.getMethodId() > 0) {
						if (semantics.containsKey(methodInfo)) {
							semantics.replace(methodInfo, semantic);
						} else {
							semantics.put(methodInfo, semantic);
						}
					}
				}
			}
		}
	}

	/**
	 * Set an invocation semantic to a specific method that is overloaded
	 * @param c					class of the method
	 * @param methodName		method to modify
	 * @param semantic			semantic to set on the method
	 * @param parameterTypes	parameters types of the method
	 * @throws java.lang.NoSuchMethodException
	 */
	public final void addSemantic(Class<?> c, String methodName, int semantic,
			Class<?>... parameterTypes) throws java.lang.NoSuchMethodException {
		Method method = c.getMethod(methodName, parameterTypes);
		MethodInfo methodInfo = getMethodInfo(method);
		if (methodInfo.getMethodId() > 0) {
			if (semantics.containsKey(methodInfo)) {
				semantics.replace(methodInfo, semantic);
			} else {
				semantics.put(methodInfo, semantic);
			}
		} else {
			String errorMessage = ClassUtil.getMethodSign(method);
			throw new java.lang.NoSuchMethodException(errorMessage);
		}
	}

	/**
	 * Initialize the method identifier for all the methods in a class
	 * @param c				class to initialize
	 * @param startIndex	index of the first method
	 */
	protected void initializeMethodInfo(Class<?> c, int startIndex) {
		if (!definedMethodId) {
			Method[] allMethods = c.getMethods();
			
			Arrays.sort(allMethods, new Comparator<Method>() {
				@Override
                public int compare(Method first, Method second) {
					String firstSign = ClassUtil.getMethodSign(first);
					String secondSign = ClassUtil.getMethodSign(second);
					return firstSign.compareTo(secondSign);
				}
			});
			
			int index = startIndex;
			for (Method m : allMethods) {
				POPClass popClassAnnotation = m.getDeclaringClass().getAnnotation(POPClass.class);
				if (popClassAnnotation != null && Modifier.isPublic(m.getModifiers()) && isMethodPOPAnnotated(m)) {
				    int methodId = methodId(m,index);
				    int id = popClassAnnotation.classId();
				    
				    if(id == -1){
				    	id = getClassId();
				    }
					MethodInfo methodInfo = new MethodInfo(id, methodId);
					
					methodInfos.put(methodInfo, m);
					index++;
				}
			}
		}
	}

	/**
	 * Initialize the constructor identifier and the semantic
	 * @param c				class to initialize
	 * @param startIndex	index of the first constructor
	 * @return	next index to be used for the methods
	 */
	protected int initializeConstructorInfo(Class<?> c, int startIndex) {
		int index = startIndex;
		if (!definedMethodId) {
			// initializeMethodId
			Constructor<?>[] allConstructors = c.getDeclaredConstructors();

			Arrays.sort(allConstructors, new Comparator<Constructor<?>>() {
				@Override
                public int compare(Constructor<?> first, Constructor<?> second) {
					String firstSign = ClassUtil
							.getMethodSign(first);
					String secondSign = ClassUtil
							.getMethodSign(second);
					return firstSign.compareTo(secondSign);
				}
			});

			for (Constructor<?> constructor : allConstructors) {
				if (Modifier.isPublic(constructor.getModifiers())) {
					int id = -1;
					if(constructor.isAnnotationPresent(POPObjectDescription.class)){
				        id = constructor.getAnnotation(POPObjectDescription.class).id();
			        }
					if(id == -1){
						id = index;
					}
					
					MethodInfo info = new MethodInfo(getClassId(), id);
					constructorInfos.put(info, constructor);
					semantics.put(info, Semantic.CONSTRUCTOR
							| Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
					index++;
				}
			}
		}
		return index;
	}
	
	/**
	 * Define informations about a method
	 * @param c				Class of the method
	 * @param methodName	Name of the method
	 * @param methodId		Unique identifier of the method
	 * @param semanticId	Semantic applied to the method
	 * @param paramTypes	Parameters of the method
	 */
	protected void defineMethod(Class<?>c,String methodName, int methodId, int semanticId, Class<?>...paramTypes)
	{
		try {
			Method m = c.getMethod(methodName, paramTypes);
			MethodInfo methodInfo = new MethodInfo(getClassId(), methodId);
			methodInfos.put(methodInfo, m);
			
			if (semantics.containsKey(methodInfo)) {
				semantics.replace(methodInfo, semanticId);
			} else {
				semantics.put(methodInfo, semanticId);
			}
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Define information about a constructor
	 * @param c				Class of the constructor
	 * @param constructorId	Unique identifier of the constructor
	 * @param paramTypes	Parameters of the constructor
	 */
	protected void defineConstructor(Class<?>c,int constructorId, Class<?>...paramTypes)
	{
		try {
			Constructor<?> constructor = c.getConstructor(paramTypes);
			MethodInfo info = new MethodInfo(getClassId(),
					constructorId);
			constructorInfos.put(info, constructor);
			semantics.put(info, Semantic.CONSTRUCTOR
					| Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deserialize the object from the buffer
	 * @param buffer	The buffer to deserialize from
	 */
	@Override
    public boolean deserialize(POPBuffer buffer) {
		return true;
	}

	/**
	 * Serialize the object into the buffer
	 * @param buffer	The buffer to serialize in
	 */
	@Override
    public boolean serialize(POPBuffer buffer) {
		return true;
	}

	/**
	 * Exit method
	 */
	public void exit() {
		
	}

	/**
	 * Print object information on the standard output
	 */
	public void printMethodInfo() {
		System.out.println("===========ConstructorInfo============");
		Enumeration<MethodInfo> keys = constructorInfos.keys();
		while (keys.hasMoreElements()) {
			MethodInfo key = keys.nextElement();
			String info = String.format("ClassId:%d.ConstructorId:%d.Sign:%s",
					key.getClassId(), key.getMethodId(), constructorInfos.get(
							key).toGenericString());
			System.out.println(info);
		}

		System.out.println("===========MethodInfo============");
		keys = methodInfos.keys();
		while (keys.hasMoreElements()) {
			MethodInfo key = keys.nextElement();
			String info = String.format("ClassId:%d.ConstructorId:%d.Sign:%s",
					key.getClassId(), key.getMethodId(), methodInfos.get(key)
							.toGenericString());
			System.out.println(info);
		}

		System.out.println("===========SemanticsInfo============");
		keys = semantics.keys();
		while (keys.hasMoreElements()) {
			MethodInfo key = keys.nextElement();
			String info = String.format(
					"ClassId:%d.ConstructorId:%d.Semantics:%d", key
							.getClassId(), key.getMethodId(), semantics
							.get(key));
			System.out.println(info);
		}
	}
		
	/**
	 * Return the reference of this object with a POP-C++ format
	 * @return access point of the object as a formatted string
	 */
	public String getPOPCReference(){
		return getAccessPoint().toString();
	}
	
	public boolean isTemporary(){
		return temporary;
	}
	
	public void makeTemporary(){
		temporary = true;
	}
	
	public <T extends POPObject> T makePermanent(){
		temporary = false;
		return (T)this;
	}
	
	public <T extends Object> T getThis(Class<T> myClass){
		if(me == null){
			me = PopJava.newActive(getClass(), getAccessPoint());
			
			//After establishing connection with self, artificially decrease connection by one
			//This is to avoid the issue of never closing objects with reference to itself
			if(me != null && Broker.getBroker() != null){
				Broker.getBroker().onCloseConnection("SelfReference");
			}
		}
		
		return (T) me;
	}
}
