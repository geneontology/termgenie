package org.json.rpc.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.json.rpc.commons.GsonTypeChecker;
import org.json.rpc.commons.TypeChecker;

/**
 * {@link TypeChecker} for JSON-RPC compatibility, which is aware of the Inject
 * and its annotations ({@link ServletAware} and {@link SessionAware}).
 */
public class InjectingGsonTypeChecker extends GsonTypeChecker {

	@Override
	public boolean isValidMethod(Method method, boolean throwException) {
		Class<?> returnType = method.getReturnType();
		boolean result = false;
		try {
			result = isValidType(returnType, throwException);
			if (!result) {
				if (throwException) {
					throw new IllegalArgumentException("invalid return type : " + returnType);
				}
				return false;
			}
		} catch (RuntimeException e) {
			if (!result) {
				if (throwException) {
					throw new IllegalArgumentException("invalid return type : " + returnType, e);
				}
				return false;
			}
		}

		Class<?>[] parameterTypes = method.getParameterTypes();
		int length = checkInjectAnnotation(parameterTypes,
				parameterTypes.length,
				method,
				SessionAware.class,
				SessionAware.parameterTypes);
		length = checkInjectAnnotation(parameterTypes,
				length,
				method,
				ServletAware.class,
				ServletAware.parameterTypes);

		for (int i = 0; i < length; i++) {
			Class<?> paramType = parameterTypes[i];
			result = false;
			try {
				result = isValidType(paramType, throwException);
				if (!result) {
					if (throwException) {
						throw new IllegalArgumentException("invalid parameter type : " + paramType);
					}
					return false;
				}
			} catch (RuntimeException e) {
				if (!result) {
					if (throwException) {
						throw new IllegalArgumentException("invalid parameter type : " + paramType, e);
					}
					return false;
				}
			}
		}

		return true;
	}

	private int checkInjectAnnotation(Class<?>[] parameterTypes,
			int length,
			Method method,
			Class<? extends Annotation> annotationClass,
			Class<?>[] injectedParamters)
	{
		if (InjectingJsonRpcExecutor.isInjectMethod(method, annotationClass)) {
			boolean missing = true;
			final int injectedLength = injectedParamters.length;
			final int diff = length - injectedLength;
			if (diff >= 0) {
				boolean[] checkParameters = new boolean[injectedLength];
				Arrays.fill(checkParameters, false);
				for (int i = 0; i < injectedLength; i++) {
					Class<?> paramType = parameterTypes[diff + i];
					if (injectedParamters[i].isAssignableFrom(paramType)) {
						checkParameters[i] = true;
					}
				}
				boolean checkAllParameters = true;
				for (boolean checkParameter : checkParameters) {
					checkAllParameters = checkAllParameters && checkParameter;
				}
				missing = checkAllParameters == false;
			}
			if (missing) {
				throw new IllegalArgumentException("Missing HttpSession parameter for SessionAware method signature: " + method);
			}
			length = diff;
		}
		return length;
	}
}
