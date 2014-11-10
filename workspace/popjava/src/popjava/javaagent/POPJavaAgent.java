package popjava.javaagent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.base.POPObject;
import popjava.system.POPSystem;
import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import javassist.util.proxy.ProxyObject;

/**
 * POPJava Java Agent. This class intercepts the class file loading.
 * 
 * TODO: Convert System.out.println to POPSystem.writeLog
 * @author Beat Wolf
 *
 */
public final class POPJavaAgent implements ClassFileTransformer{
    
    private static final String POP_JAVA_BASE = POPObject.class.getName();
    
    /**
     * A Javassist ClassPool object used for loading, and then instrumenting, classes
     */
    private final ClassPool classPool;
    
    private final Set<String> IGNORED = new HashSet<String>();
    
    /**
     * Constructor of the POPJavaAgent,
     * should only be called by the static premain method in this class.
     * @param instrumentation
     */
    private POPJavaAgent(final Instrumentation instrumentation){        
        //Create the default ClassPool, which is built from the CLASSPATH
        classPool = ClassPool.getDefault();
        
        // Add our transformer to the list of transformers
        instrumentation.addTransformer( this );
        
        //TODO: make a more complete list
        IGNORED.add("popjava.");
        IGNORED.add("com.sun.");
        IGNORED.add("sun.");
        IGNORED.add("javassist.");
        IGNORED.add("java.");
        IGNORED.add("javax.");
        IGNORED.add("org.w3c.");
        IGNORED.add("org.xml.");
        IGNORED.add("javax.");        
    }

    private static POPJavaAgent me;
    
    public static POPJavaAgent getInstance(){
        return me;
    }
    
    /**
     * This method is called by the JVM before the main method is loaded.
     * It registers this class as the JavaAgent to be loaded
     * to transform all POPJava classes loaded by the classloader.
     * 
     * @param agentArgs
     * @param inst
     */
    public static void premain( final String agentArgs, final Instrumentation inst )
    {
        me = new POPJavaAgent(inst);
    }

    /**
     * Returns true if the specified class is in an ignored package
     * @param className
     * @return
     */
    private boolean isInIgnoredPackage(String className){
        for(String packageName: IGNORED){
            if(className.startsWith(packageName)){
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isProxy(final CtClass rawClass){        
        try {
            final CtClass parent = rawClass.getSuperclass();
            if(parent != null){
                
                for(CtClass inter: rawClass.getInterfaces()){
                    System.out.println(inter.getName()+" "+ProxyObject.class.getName());
                    if(inter.getName().equals(ProxyObject.class.getName())){
                        return true;
                    }
                }
                
                return isProxy(parent);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }       
        
        return false;
    }
    
    public void addJar(String file) throws NotFoundException{
        synchronized(classPool){
            //System.out.println("Add jar to classpoll "+file);
            classPool.appendClassPath(file);
        }        
    }
    
    @Override
    public byte[] transform(final ClassLoader loader, final String className,
            final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer) throws IllegalClassFormatException {
        if(classPool == null){
            return null;
        }
        
        final String dotClassName = className.replace( '/', '.' );
        
        if(isInIgnoredPackage(dotClassName)){
            return null;
        }
        
        try
        {
            // Create a Javassist CtClass from the byte code
            synchronized(classPool){
                classPool.insertClassPath( new ByteArrayClassPath( dotClassName, classfileBuffer ) );
            }
            
            final CtClass rawClass = classPool.get( dotClassName );
            
            // Only transform unfrozen popjava classes
            if(  !rawClass.isFrozen() && isPOPClass(rawClass) && !isProxy(rawClass)) {
                //System.out.println("Transform "+dotClassName);
                
                //Add POPObject as parent object if needed
                if(classNeedsSuperclass(rawClass)){
                    //System.out.println("Add superclass "+POP_JAVA_BASE);
                    final CtClass superClass = classPool.get(POP_JAVA_BASE);
                    rawClass.setSuperclass(superClass);
                }
                
                if(!hasDefaultConstructor(rawClass)){
                    //System.out.println("Add default constructor");
                    //TODO: create default constructor
                }
                
                for( final CtMethod method: rawClass.getMethods())
                {
                    //System.out.println(method.getName());
                    //TODO: correctly identify main method
                    if(method.getName().equals("main")){
                        //System.out.println("this is the main! Initialize popjava");
                        method.insertBefore("$1 = "+POPSystem.class.getName()+".initialize($1);");
                        method.insertAfter(POPSystem.class.getName()+".end();");
                    }
                    
                    final String longMethodName = method.getLongName();
                    
                    // Only transform methods in this class, not in the super class because
                    // the super class will be transformed separately
                    if( longMethodName.startsWith( dotClassName ) )
                    {
                        instrumentCode(loader, method);
                        //TODO: do awesome popjava stuff here
                    }
                }
                
                for( final CtConstructor constructor: rawClass.getConstructors()){
                    instrumentCode(loader, constructor);
                }

                // Return the transformed bytecode
                return rawClass.toBytecode();
            }
        }
        catch( IOException ioe )
        {
            ioe.printStackTrace();
            System.out.println( ioe.getMessage() + " transforming class " + className + "; returning default class: 1");
        }
        catch( NotFoundException nfe )
        {
            nfe.printStackTrace();
            System.out.println( nfe.getMessage() + " transforming class " + className + "; returning default class: 2 SIZE: "+classfileBuffer.length );
        }
        catch( CannotCompileException cce )
        {
            cce.printStackTrace();
            System.out.println( cce.getMessage() + " transforming class " + className + "; returning default class: 3" );
        }
        catch( Exception e )
        {
            System.out.println( "An error occurred during class transformation: " + e.getMessage() );
            e.printStackTrace();
        }
        
        // Returning null means that we're going to use the uninstrumented bytecode
        return null;
    }
    
    private void instrumentCode(final ClassLoader loader, final CtBehavior method) throws CannotCompileException{
        
        ExprEditor ed = new ExprEditor(){
            
            /**
             * Replace all new invocations of pop classes by the proper popjava way to initialize objects
             */
            @Override
            public void edit(NewExpr e)
                    throws CannotCompileException {
                
                try {
                    if(isInIgnoredPackage(e.getClassName())){
                        return;
                    }
                    
                    //System.out.println("*!!!!!! *NEW ** "+e.getClassName());
                    //Class<?> temp = loader.loadClass(e.getClassName());
                    CtClass clazz = e.getConstructor().getDeclaringClass();
                    
                    //Replace all calls to new for popjava objects with the correct instatiation
                    if(isPOPClass(clazz)){
                        String newCall = "$_ = ($r)"+PopJava.class.getName()+".newActive("+clazz.getName()+".class, $args);";
                        //System.out.println(newCall);
                        
                        e.replace(newCall);
                    }
                    
                /*} catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                */} catch (NotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            /**
             * Handle the following scenarios:
             * 
             * this.a = POP_OBJECT;
             * 
             * TODO:
             * this.a = this;
             */
            @Override
            public void edit(FieldAccess f) throws CannotCompileException {
                if(f.isWriter()){
                    if(isInIgnoredPackage(f.getClassName())){
                        return;
                    }
                    
                    //System.out.println(f.getClassName()+" FieldAccess: "+f.getFieldName()+" "+f.getSignature());
                    CtClass clazz;
                    try {
                        clazz = f.getField().getType();
                        if(isPOPClass(clazz)){
                            
                            String newAssign = "$0."+f.getFieldName()+ " = ("+clazz.getName()+") (("+POPObject.class.getName()+")$1).makePermanent();";
                            //System.out.println(newAssign);
                            f.replace(newAssign);
                        }
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }                
                    
                }
            }
            
            /**
             * TODO:
             * Intercept calls on this.method();
             */
            @Override
            public void edit(MethodCall e){
                if(isInIgnoredPackage(e.getClassName())){
                    return;
                }
                
                if(e.getEnclosingClass().equals(method.getDeclaringClass())){
                    //System.out.println("!!! "+e.getClassName()+" "+e.getMethodName()+" "+e.isSuper()+" "+e.where().getLongName());
                }                
            }
            
        };
        
        method.instrument(ed);
    }
    
    /**
     * Returns true if this class or any of its super classes has the @POPClass annotation
     * @param rawClass
     * @return
     */
    private boolean isPOPClass(final CtClass rawClass){
        try {
            final Object popClass = rawClass.getAnnotation(POPClass.class);
            if(popClass != null){
                return true;
            }else{
                final CtClass superClass = rawClass.getSuperclass();
                if(superClass != null){
                    return isPOPClass(superClass);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (NotFoundException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private boolean isPOPClass(final Class<?> rawClass){
        final Object popClass = rawClass.getAnnotation(POPClass.class);
        if(popClass != null){
            return true;
        }else{
            final Class<?> superClass = rawClass.getSuperclass();
            if(superClass != null){
                return isPOPClass(superClass);
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if this class has the @POPClass annotation and its super class
     * is the java.lang.Object class
     * @param cc
     * @return
     */
    private boolean classNeedsSuperclass(final CtClass rawClass){
        try {
            final CtClass superClass = rawClass.getSuperclass();
            if(superClass == null){
                return false;
            }
            
            boolean parentIsPOPObject = isPOPClass(superClass);
            
            return !parentIsPOPObject;
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Returns true if this class has a default constructor.
     * @param rawClass
     * @return
     * @throws NotFoundException
     */
    private boolean hasDefaultConstructor(final CtClass rawClass) throws NotFoundException{
        boolean hasDefaultConstructor = false;
        for(final CtConstructor constructor: rawClass.getConstructors()){
            if(constructor.getParameterTypes().length == 0){
                hasDefaultConstructor = true;
            }
        }
        
        return hasDefaultConstructor;
    }
}
