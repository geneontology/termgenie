package org.json.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpSession;

/**
 *  Annotation indicating an injection dependency on one additional parameter:
 *  {@link HttpSession}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SessionAware {

	final Class<?>[] parameterTypes = {HttpSession.class};
}
