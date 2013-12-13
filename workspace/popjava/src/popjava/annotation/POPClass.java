package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface POPClass {
	String className() default "";
	int classId() default -1;
	boolean deconstructor() default false;
}

/*
if(printPOPCInfo){
println("setClassId("+ci.getClassUID()+");", indent);
if(!ci.getClassName().equals("") || ci.getClassName()!=null){
	println("setClassName(\""+ci.getClassName()+"\");", indent);
}
println("hasDestructor("+ci.hasDestructor()+");", indent);
}
*/