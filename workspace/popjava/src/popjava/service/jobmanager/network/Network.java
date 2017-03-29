package popjava.service.jobmanager.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static popjava.base.POPErrorCode.POP_NO_PROTOCOL;
import popjava.base.POPException;
import popjava.service.jobmanager.protocol.CreateObjectProtocolBase;

/**
 *
 * @author Davide Mazzoleni
 */
public class Network {
	private final String name;
	private final CreateObjectProtocolBase protocol;
	private final Set<Node> members;

	public Network(String name, CreateObjectProtocolBase protocol) {
		this.name = name;
		this.protocol = protocol;
		this.members = new HashSet<>();
	}
	
	public Network(String name, String protocol) {
		try {
			if(!protocol.startsWith("popjava.service.jobmanager.protocol.CP"))
				protocol = "popjava.service.jobmanager.protocol.CP" + protocol;
			Class<CreateObjectProtocolBase> clazz = (Class<CreateObjectProtocolBase>) Class.forName(protocol);
			Constructor<CreateObjectProtocolBase> constr = clazz.getConstructor(String.class /* TODO add parameters*/);
			CreateObjectProtocolBase instance = constr.newInstance("" /* TODO add params */);
			
			this.name = name;
			this.protocol = instance;
			this.members = new HashSet<>();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
			throw new POPException(POP_NO_PROTOCOL, "We didn't find the class " + protocol);
		}
	}

	public String getName() {
		return name;
	}

	public CreateObjectProtocolBase getProtocol() {
		return protocol;
	}

	public int size() {
		return members.size();
	}

	public Iterator<Node> iterator() {
		return members.iterator();
	}

	public boolean add(Node e) {
		return members.add(e);
	}

	public boolean remove(Node o) {
		return members.remove(o);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Network other = (Network) obj;
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		return true;
	}
	
}
