package org.bbop.termgenie.services.management;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.management.JsonModuleConfigDetails.JsonPair;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.user.UserData;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class ManagementServicesImpl implements ManagementServices {

	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;

	/**
	 * @param sessionHandler
	 * @param permissions
	 */
	@Inject
	ManagementServicesImpl(InternalSessionHandler sessionHandler, UserPermissions permissions)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
	}

	@Override
	public List<JsonModuleConfigDetails> getModuleDetails(String sessionId, HttpSession session, Injector injector) {
		List<IOCModule> allModules = IOCModule.getAllModules();
		List<JsonModuleConfigDetails> result = new ArrayList<JsonModuleConfigDetails>(allModules.size());
		for (IOCModule module : allModules) {
			JsonModuleConfigDetails details = new JsonModuleConfigDetails();
			details.setModuleName(module.getModuleName());
			details.setDescription(module.getModuleDescription());
			Map<String, String> parameters = module.getConfiguredParameters();
			if (parameters != null && !parameters.isEmpty()) {
				details.setParameters(parameters);
			}
			details.setImplementations(convert(module.getBoundClasses()));
			details.setProvides(convert(module.getProvidesClasses()));
			details.setAdditionalData(convertAdditonalData(module.getAdditionalData(injector)));
			result.add(details);
		}
		return result;
	}

	private List<JsonPair> convertAdditonalData(List<Pair<String, String>> additionalData) {
		if (additionalData == null || additionalData.isEmpty()) {
			return null;
		}
		List<JsonPair> result = new ArrayList<JsonPair>();
		for (Pair<String, String> pair : additionalData) {
			JsonPair jsonPair =  new JsonPair(pair.getOne(), pair.getTwo());
			result.add(jsonPair);
		}
		return result;
	}

	private List<JsonPair> convert(List<Pair<Method, String>> providesClasses) {
		if (providesClasses == null || providesClasses.isEmpty()) {
			return null;
		}
		List<JsonPair> result = new ArrayList<JsonPair>(providesClasses.size());
		for (Pair<Method, String> pair : providesClasses) {
			final Method method = pair.getOne();
			final String methodString = method.getGenericReturnType().toString();
			String name = pair.getTwo();
			if (name == null) {
				name = method.getName();
			}
			result.add(new JsonPair(methodString, name));
		}
		return result;
	}

	private Map<String, String> convert(Map<Class<?>, Class<?>> classes) {
		if (classes == null || classes.isEmpty()) {
			return null;
		}
		Map<String, String> result = new HashMap<String, String>();
		for (Entry<Class<?>, Class<?>> entry : classes.entrySet()) {
			result.put(entry.getKey().getCanonicalName(), entry.getValue().getCanonicalName());
		}
		return result;
	}

	@Override
	public boolean isAuthorized(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			UserData userData = sessionHandler.getUserData(session);
			if (userData != null) {
				boolean allowCommitReview = permissions.allowManagementAccess(userData);
				return allowCommitReview;
			}
		}
		return false;
	}

	@Override
	public JsonSystemDetails getSystemDetails(String sessionId, HttpSession session) {
		JsonSystemDetails details = new JsonSystemDetails();
		details.setEnvironment(System.getProperties(), "java.", "sun.", "os.");
		final Runtime runtime = Runtime.getRuntime();
		details.setMaxHeap(bytesToMByteString(runtime.maxMemory()));
		details.setCurrentHeap(bytesToMByteString(runtime.totalMemory()));
		details.setFreeHeap(bytesToMByteString(runtime.freeMemory()));
		return details;
	}
	
	private String bytesToMByteString(long bytes) {
		long mbytes = bytes / (1024 * 1024);
		return Long.toString(mbytes);
	}
}
