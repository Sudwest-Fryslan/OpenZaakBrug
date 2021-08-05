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

import java.lang.invoke.MethodHandles;

import org.aspectj.lang.ProceedingJoinPoint;

import com.google.gson.GsonBuilder;

/**
 * Advice class for logging the input and output of the ModelMapper mappings
 * @author Ricardo van Holst
 *
 */

public class ModelMapperAdvice {
	private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

	private String getDebugName(Object source, Class<?> destination) {
		return "ModelMapper " + source.getClass().getSimpleName() + "->" + destination.getSimpleName();
	}
	private String convertToString(Object obj) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(obj);
	}

	public Object debugModelMapperMap(ProceedingJoinPoint pjp, Object source, Class<?> destination) throws Throwable {
		if (debug.isReportGeneratorEnabled()) {
			String debugName = getDebugName(source, destination);
			debug.startpoint(debugName, convertToString(source));
			Object result = pjp.proceed();
			debug.endpoint(debugName, convertToString(result));
			return result;
		}
		return pjp.proceed();
	}
}
