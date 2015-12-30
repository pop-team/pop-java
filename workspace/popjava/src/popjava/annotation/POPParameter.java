package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface POPParameter {

	enum Direction{
		IN,
		OUT,
		INOUT,
		IGNORE
	}
	
	Direction value();
}
