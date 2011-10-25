package org.json.rpc.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.rpc.commons.GsonTypeChecker;
import org.json.rpc.commons.TypeChecker;

/**
 * {@link TypeChecker} for JSON-RPC compatibility, which is aware of the Inject
 * and its annotations ({@link ServletAware}, {@link SessionAware}, and 
 * {@link ServletContextAware}).
 */
public class InjectingGsonTypeChecker extends GsonTypeChecker {

	@Override
	public boolean isValidMethod(Method method, boolean throwException) {
		Type genericReturnType = method.getGenericReturnType();
		Class<?> returnType = null;
		if (genericReturnType instanceof ParameterizedType) {
			ParameterizedType type = (ParameterizedType) genericReturnType;
			returnType = checkGenericType(type);
			if (returnType == null) {
				if (throwException) {
					throw new IllegalArgumentException("invalid return type : " + genericReturnType);
				}
				return false;
			}
		}
		else if (genericReturnType instanceof Class) {
			returnType = (Class<?>) genericReturnType;
		}
		boolean result = false;
		try {
			if (returnType != null) {
				result = isValidType(returnType, throwException);
			}
			if (!result) {
				if (throwException) {
					throw new IllegalArgumentException("invalid return type : " + genericReturnType);
				}
				return false;
			}
		} catch (RuntimeException e) {
			if (!result) {
				if (throwException) {
					throw new IllegalArgumentException("invalid return type : " + genericReturnType, e);
				}
				return false;
			}
		}

		Type[] parameterTypes = method.getGenericParameterTypes();
		int length = checkInjectAnnotation(parameterTypes,
				parameterTypes.length,
				method,
				SessionAware.class);
		length = checkInjectAnnotation(parameterTypes,
				length,
				method,
				ServletContextAware.class);
		length = checkInjectAnnotation(parameterTypes,
				length,
				method,
				ServletAware.class);
		

		for (int i = 0; i < length; i++) {
			Type genericParameterType = parameterTypes[i];
			Class<?> paramType = null;
			if (genericParameterType instanceof ParameterizedType) {
				ParameterizedType type = (ParameterizedType) genericParameterType;
				paramType = checkGenericType(type);
				if (returnType == null) {
					if (throwException) {
						throw new IllegalArgumentException("invalid parameter type : " + genericParameterType);
					}
					return false;
				}
			}
			else if (genericParameterType instanceof Class) {
				paramType = (Class<?>) genericParameterType;
			}
			result = false;
			try {
				if (paramType != null) {
					result = isValidType(paramType, throwException);
				}
				if (!result) {
					if (throwException) {
						throw new IllegalArgumentException("invalid parameter type : " + genericParameterType);
					}
					return false;
				}
			} catch (RuntimeException e) {
				if (!result) {
					if (throwException) {
						throw new IllegalArgumentException("invalid parameter type : " + genericParameterType, e);
					}
					return false;
				}
			}
		}

		return true;
	}

	private Class<?> checkGenericType(ParameterizedType type) {
		Class<?> rawClass = (Class<?>)type.getRawType();
		if(Collection.class.isAssignableFrom(rawClass)) {
			Type[] typeArguments = type.getActualTypeArguments();
			if (typeArguments.length == 1) {
				return (Class<?>) typeArguments[0];
			}
		}
		else if (Map.class.isAssignableFrom(rawClass)) {
			Type[] typeArguments = type.getActualTypeArguments();
			if (typeArguments.length == 2) {
				Type keyType = typeArguments[0];
				if (isSimpleType(keyType)) {
					Type valueType = typeArguments[1];
					if (valueType instanceof ParameterizedType) {
						ParameterizedType parameterizedType = (ParameterizedType) valueType;
						return checkGenericType(parameterizedType);
					}
					else if (valueType instanceof Class) {
						return (Class<?>) valueType;
					}
				}
			}
		}
		return null;
	}
	
	private boolean isSimpleType(Type type) {
		if ((type instanceof Class) == false) {
			return false;
		}
		return isSimpleType((Class<?>) type);
	}
	
	private boolean isSimpleType(Class<?> clazz) {
		if (clazz.isPrimitive()) {
            return true;
        }

        if (Boolean.class == clazz) {
            return true;
        }

        if (Number.class.isAssignableFrom(clazz)) {
            return true;
        }

        if (String.class == clazz) {
            return true;
        }

        if (Character.class == clazz) {
            return true;
        }
        return false;
	}

	private int checkInjectAnnotation(Type[] parameterTypes,
			int length,
			Method method,
			Class<? extends Annotation> annotationClass)
	{
		if (InjectingJsonRpcExecutor.isInjectMethod(method, annotationClass)) {
			Class<?>[] injectedParamters = InjectedParameters.parameters.get(annotationClass);
			boolean missing = true;
			final int injectedLength = injectedParamters.length;
			final int diff = length - injectedLength;
			if (diff >= 0) {
				boolean[] checkParameters = new boolean[injectedLength];
				Arrays.fill(checkParameters, false);
				for (int i = 0; i < injectedLength; i++) {
					Type paramType = parameterTypes[diff + i];
					if (paramType instanceof Class) {
						if (injectedParamters[i].isAssignableFrom((Class<?>) paramType)) {
							checkParameters[i] = true;
						}
					}
				}
				boolean checkAllParameters = true;
				for (boolean checkParameter : checkParameters) {
					checkAllParameters = checkAllParameters && checkParameter;
				}
				missing = checkAllParameters == false;
			}
			if (missing) {
				throw new IllegalArgumentException("Missing parameter for "+annotationClass.getSimpleName()+" method signature: " + method);
			}
			length = diff;
		}
		return length;
	}
	
	@Override
    public boolean isValidType(Class<?> clazz, boolean throwException) {
        return isValidType(clazz, throwException, null);
    }
	
	private boolean isValidType(Type type, boolean throwException, Set<Class<?>> visited) {
		Class<?> clazz = null;
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			clazz = checkGenericType(parameterizedType);
			if (clazz == null) {
				if (throwException) {
					throw new IllegalArgumentException("invalid type : " + type);
				}
				return false;
			}
		}
		else {
			clazz = (Class<?>) type;
		}
		return isValidType(clazz, throwException, visited);
	}
	
	private boolean isValidType(Class<?> clazz, boolean throwException, Set<Class<?>> visited) {
        if (isSimpleType(clazz)) {
            return true;
        }

        if (Date.class == clazz) {
            return true;
        }

        if (clazz.isArray()) {
            return this.isValidType(clazz.getComponentType(), throwException, visited);
        }

        /**
         * False cases
         */

        if (clazz.isAnonymousClass()) {
            if (throwException) {
                throw new IllegalArgumentException("anonymous class not allowed : " + clazz);
            }
            return false;
        }

        if (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())) {
            if (throwException) {
                throw new IllegalArgumentException("abstract class or interface not allowed : " + clazz);
            }
            return false;
        }

        if (clazz.getTypeParameters().length > 0) {
            if (throwException) {
                throw new IllegalArgumentException("parametrized classes not allowed : " + clazz);
            }
            return false;
        }

        boolean zeroArgConstructor = (clazz.getConstructors().length == 0);
        for (Constructor<?> c : clazz.getConstructors()) {
            if (c.getParameterTypes().length == 0) {
                zeroArgConstructor = true;
                break;
            }
        }

        if (!zeroArgConstructor) {
            if (throwException) {
                throw new IllegalArgumentException("no zero-arg constructor found : " + clazz);
            }
            return false;
        }

        // avoid circular references
        visited = (visited == null ? new HashSet<Class<?>>() : visited);
        if (visited.contains(clazz)) {
            if (throwException) {
                throw new IllegalArgumentException("circular reference detected : " + clazz);
            }
            return false;
        }
        visited.add(clazz);

        // Check for fields because Gson uses fields
        for (Field f : clazz.getDeclaredFields()) {
            int m = f.getModifiers();
            if (Modifier.isStatic(m) || Modifier.isTransient(m)) {
                continue;
            }

            if (Modifier.isFinal(m)) {
                if (throwException) {
                    throw new IllegalArgumentException("final field found : " + f);
                }
                return false;
            }

            boolean result = false;
            try {
                result = isValidType(f.getGenericType(), throwException, visited);
                if (!result) {
                    if (throwException) {
                        throw new IllegalArgumentException("invalid field found : " + f);
                    }
                    return false;
                }
            } catch (RuntimeException e) {
                if (!result) {
                    if (throwException) {
                        throw new IllegalArgumentException("invalid field found : " + f, e);
                    }
                    return false;
                }
            }
        }


        return true;
    }
}
