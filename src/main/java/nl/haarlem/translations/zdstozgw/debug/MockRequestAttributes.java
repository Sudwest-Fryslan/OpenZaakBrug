/*
 * Copyright 2020-2021 The Open Zaakbrug Contributors
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
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
