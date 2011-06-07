package org.bbop.termgenie.rpc;

/**
 * Annotation for JSON RPC methods to be handled by the {@link JsonRpcServlet}.
 */
public @interface JSONMethod {
	String methodname() default "";
}
