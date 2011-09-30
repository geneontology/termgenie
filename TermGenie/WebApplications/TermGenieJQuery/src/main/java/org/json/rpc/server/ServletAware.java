package org.json.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Annotation indicating an injection dependency on two additional parameters:
 * {@link HttpServletRequest} and {@link HttpServletResponse}.
 * 
 * @see InjectedParameters for definition of the parameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ServletAware {
	// intentionally empty
}
