import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPObjectDescription;

@POPClass
public class ParObj {

	@POPObjectDescription(url = "localhost")
	public ParObj() {

	}

	public ParObj(@POPConfig(value = Type.URL) String host) {
	}
}
