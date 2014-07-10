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
import javassist.expr.NewExpr;

/**
 * POPJava Java Agent. This class intercepts the class file loading.
 * @author asraniel
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
        
        IGNORED.add("popjava.");
        IGNORED.add("com.sun.");
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
        new POPJavaAgent(inst);
    }

    private boolean isInIgnoredPackage(String className){
        for(String packageName: IGNORED){
            if(className.startsWith(packageName)){
                return true;
            }
        }
        
        return false;
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
            // Create a Javassist CtClass from the bype code
            classPool.insertClassPath( new ByteArrayClassPath( className, classfileBuffer ) );
            final CtClass rawClass = classPool.get( dotClassName );
                        
            // Only transform unfrozen popjava classes
            if(  !rawClass.isFrozen() && isPOPClass(rawClass))
            {
                System.out.println(dotClassName);
                //Add POPObject as parent object if needed
                if(classNeedsSuperclass(rawClass)){
                    System.out.println("Add superclass");
                    final CtClass superClass = classPool.get(POP_JAVA_BASE);
                    rawClass.setSuperclass(superClass);
                }
                
                if(!hasDefaultConstructor(rawClass)){
                    System.out.println("Add default constructor");
                    //TODO: create default constructor
                }
                
                for( final CtMethod method: rawClass.getMethods())
                {
                    //TODO: correctly identify main method
                    if(method.getName().equals("main")){
                        System.out.println("this is the main! Initialize popjava");
                        method.insertBefore("$1 = "+POPSystem.class.getName()+".initialize($1);");
                        
                        method.insertAfter(POPSystem.class.getName()+".end();");
                    }
                    
                    final String longMethodName = method.getLongName();
                    
                    // Only transform methods in this class, not in the super class because
                    // the super class will be transformed separately
                    if( longMethodName.startsWith( dotClassName ) )
                    {
                        System.out.println("Instrument method");
                        instrumentCode(method);
                        //TODO: do awesome popjava stuff here
                    }
                }
                
                for( final CtConstructor constructor: rawClass.getConstructors()){
                    System.out.println("Instrument constructor");
                    instrumentCode(constructor);
                }

                // Return the transformed bytecode
                return rawClass.toBytecode();
            }
        }
        catch( IOException ioe )
        {
            System.out.println( ioe.getMessage() + " transforming class " + className + "; returning default class");
        }
        catch( NotFoundException nfe )
        {
            System.out.println( nfe.getMessage() + " transforming class " + className + "; returning default class" );
        }
        catch( CannotCompileException cce )
        {
            System.out.println( cce.getMessage() + " transforming class " + className + "; returning default class" );
        }
        catch( Exception e )
        {
            System.out.println( "An error occurred during class transformation: " + e.getMessage() );
            e.printStackTrace();
        }
        
        // Returning null means that we're going to use the uninstrumented bytecode
        return null;
    }
    
    private void instrumentCode(final CtBehavior method) throws CannotCompileException{
        ExprEditor ed = new ExprEditor(){
            
            @Override
            public void edit(NewExpr e)
                    throws CannotCompileException {
                
                try {
                    final CtClass newClass = e.getConstructor().getDeclaringClass();
                    
                    //Replace all calls to new for popjava objects with the correct instatiation
                    if(isPOPClass(newClass)){
                        System.out.println("Const call "+e.getConstructor().getName());
                        String newCall = "$_ = ($r)"+PopJava.class.getName()+".newActive("+e.getClassName()+".class, $args);";
                        System.out.println(newCall);
                        
                        e.replace(newCall);
                    }
                    
                } catch (NotFoundException e1) {
                    e1.printStackTrace();
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
