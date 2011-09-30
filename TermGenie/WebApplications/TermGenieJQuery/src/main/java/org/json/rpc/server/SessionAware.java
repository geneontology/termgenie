package org.json.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpSession;

/**
 *  Annotation indicating an injection dependency on one additional parameter:
 *  {@link HttpSession}.
 *  
 *  @see InjectedParameters for definition of the parameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SessionAware {
	// intentionally empty
}
