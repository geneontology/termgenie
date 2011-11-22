package org.json.rpc.server;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.inject.Injector;

/**
 * This class is a work around for a bug of the sun/oracle java compiler (javac
 * 1.6.0_26). The compile produces missing annotation errors, when the
 * annotation has class references in its body.<br/>
 * Thus this class holds the references to the parameter types injected during
 * the RPC. If it was not for this bug, they would be listed in the annotation
 * itself.
 * 
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6982543
 */
class InjectedParameters {

	/**
	 * A map for injected parameters for each annotation.
	 */
	static final Map<Class<? extends Annotation>, Class<?>[]> parameters = registerParameters();

	/**
	 * Retrieve the count of injected parameters for the given annotation.
	 * 
	 * @param annotation
	 * @return length
	 */
	static int getParameterCount(Class<? extends Annotation> annotation) {
		Class<?>[] classes = parameters.get(annotation);
		if (classes != null) {
			return classes.length;
		}
		return 0;
	}

	private static Map<Class<? extends Annotation>, Class<?>[]> registerParameters() {
		Map<Class<? extends Annotation>, Class<?>[]> map = new HashMap<Class<? extends Annotation>, Class<?>[]>();
		map.put(ServletAware.class, new Class<?>[] { HttpServletRequest.class,
				HttpServletResponse.class });
		map.put(ServletContextAware.class, new Class<?>[] { ServletContext.class });
		map.put(SessionAware.class, new Class<?>[] { HttpSession.class });
		map.put(IOCInjectorAware.class, new Class<?>[] { Injector.class });
		return Collections.unmodifiableMap(map);
	}

}
