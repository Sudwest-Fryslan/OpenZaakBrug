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
