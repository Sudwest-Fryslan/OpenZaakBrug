package nl.haarlem.translations.zdstozgw.debug;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;

/**
 * @author Jaco de Groot
 */
public class MockRequestAttributes implements RequestAttributes {
	Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Object getAttribute(String name, int scope) {
		return attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		attributes.put(name, value);
	}

	@Override
	public void removeAttribute(String name, int scope) {
		attributes.remove(name);
	}

	@Override
	public String[] getAttributeNames(int scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback, int scope) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object resolveReference(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSessionMutex() {
		// TODO Auto-generated method stub
		return null;
	}

}
