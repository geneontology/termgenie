package org.json.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bbop.termgenie.core.process.ProcessState;

/**
 * Annotation indicating an injection dependency on {@link ProcessState}.
 * 
 * @see InjectedParameters for definition of the parameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ProcessStateAware {
	// intentionally empty
}
