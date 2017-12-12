package popjava.service.jobmanager.yaml;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

/**
 * Reverse order of YAML Properties when making output.
 * This normally put attributes before lists and maps.
 * 
 * @see https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/org/yaml/snakeyaml/issues/issue60/CustomOrderTest.java
 * @author Davide Mazzoleni
 */
public class PropertyReverser extends PropertyUtils {
	@Override
	protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
		Set<Property> reverseSet = new TreeSet<>(Collections.reverseOrder());
		reverseSet.addAll(super.createPropertySet(type, bAccess));
		return reverseSet;
	}
}
