package org.bbop.termgenie.services.management;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.data.JsonResult;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.permissions.UserPermissions;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.management.JsonModuleConfigDetails.JsonPair;
import org.bbop.termgenie.servlets.AbstractTermGenieContextListener;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.user.UserData;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class ManagementServicesImpl implements ManagementServices {
	
	private static final Logger LOGGER = Logger.getLogger(ManagementServicesImpl.class);

	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	private final OntologyLoader ontologyLoader;

	/**
	 * @param sessionHandler
	 * @param permissions
	 * @param ontologyLoader
	 */
	@Inject
	ManagementServicesImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			OntologyLoader ontologyLoader)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.ontologyLoader = ontologyLoader;
	}

	@Override
	public List<JsonModuleConfigDetails> getModuleDetails(String sessionId,
			HttpSession session,
			Injector injector)
	{
		if (!isAuthorized(sessionId, session)) {
			return null;
		}
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
			JsonPair jsonPair = new JsonPair(pair.getOne(), pair.getTwo());
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
		System.out.println("ManagementServicesImpl isAuthorized "+sessionId + "");
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		System.out.println("ManagementServicesImpl screen name: "+screenname);
		Enumeration attributeNames = session.getAttributeNames();
		while(attributeNames.hasMoreElements()){
			String attributeName = attributeNames.nextElement().toString();
			System.out.println("ManagementServicesImpl key["+attributeName+","+session.getAttribute(attributeName)+"]");
		}
		if (screenname != null) {
			UserData userData = sessionHandler.getUserData(session);
			System.out.println("ManagementServicesImpl user data["+userData.toString()+"]");
			if (userData != null) {
				boolean allow = permissions.allowManagementAccess(userData);
				System.out.println("ManagementServicesImpl allow management access? ["+allow+"]");
				return allow;
			}
		}
		return false;
	}

	@Override
	public JsonSystemDetails getSystemDetails(String sessionId, HttpSession session) {
		if (!isAuthorized(sessionId, session)) {
			return null;
		}
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

	@Override
	public List<String> getThreadDump(String sessionId, HttpSession session) {
		if (!isAuthorized(sessionId, session)) {
			return null;
		}
		StopWatch watch = new StopWatch();
		final Logger logger = Logger.getLogger(ManagementServicesImpl.class);
		logger.info("Start creating ThreadDump");
		watch.start();
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
		List<String> infos = new ArrayList<String>(threadInfos.length);
		for (ThreadInfo threadInfo : threadInfos) {
			infos.add(toString(threadInfo));
		}
		watch.stop();
		logger.info("Finished creating ThreadDump. Time: " + watch);
		return infos;
	}

	private static String toString(ThreadInfo threadInfo) {
		StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" + " Id=" + threadInfo.getThreadId() + " " + threadInfo.getThreadState());
		if (threadInfo.getLockName() != null) {
			sb.append(" on " + threadInfo.getLockName());
		}
		if (threadInfo.getLockOwnerName() != null) {
			sb.append(" owned by \"" + threadInfo.getLockOwnerName() + "\" Id=" + threadInfo.getLockOwnerId());
		}
		if (threadInfo.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (threadInfo.isInNative()) {
			sb.append(" (in native)");
		}
		sb.append('\n');
		StackTraceElement[] stackTrace = threadInfo.getStackTrace();
		int i = 0;
		for (; i < stackTrace.length; i++) {
			StackTraceElement ste = stackTrace[i];
			sb.append("\tat " + ste.toString());
			sb.append('\n');
			if (i == 0 && threadInfo.getLockInfo() != null) {
				Thread.State ts = threadInfo.getThreadState();
				switch (ts) {
					case BLOCKED:
						sb.append("\t-  blocked on " + threadInfo.getLockInfo());
						sb.append('\n');
						break;
					case WAITING:
						sb.append("\t-  waiting on " + threadInfo.getLockInfo());
						sb.append('\n');
						break;
					case TIMED_WAITING:
						sb.append("\t-  waiting on " + threadInfo.getLockInfo());
						sb.append('\n');
						break;
					default:
				}
			}

			for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked " + mi);
					sb.append('\n');
				}
			}
		}
		LockInfo[] locks = threadInfo.getLockedSynchronizers();
		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = " + locks.length);
			sb.append('\n');
			for (LockInfo li : locks) {
				sb.append("\t- " + li);
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	@Override
	public JsonSessionDetails getSessionDetails(String sessionId, HttpSession session) {
		JsonSessionDetails details = new JsonSessionDetails();
		details.setActiveSessions(AbstractTermGenieContextListener.getActiveSessionCount());
		details.setSessionsCreated(AbstractTermGenieContextListener.getSessionsCreated());
		details.setSessionsDestroyed(AbstractTermGenieContextListener.getSessionsDestroyed());
		return details;
	}

	@Override
	public JsonResult scheduleOntologyReload(String sessionId, HttpSession session)
	{
		if (!isAuthorized(sessionId, session)) {
			LOGGER.warn("Rejecting reload request for unauthorized session.");
			return new JsonResult(false, "Not authorized.");
		}
		if (ontologyLoader != null) {
			return reload();
		}
		return new JsonResult(false, "The reloading feature is not available for this TG instance.");
	}
	
	private long lastManualReload = System.currentTimeMillis();
	
	private synchronized JsonResult reload() {
		long current = System.currentTimeMillis();
		// check that at least 5 minutes have been passed since the last reload.
		long minWait = lastManualReload + (5* 60 *1000);
		if (current < minWait) {
			long toGo = minWait - current;
			long toGoSeconds = toGo / 1000;
			LOGGER.info("Rejecting reload request, minimum wait not reached, remaining: "+toGoSeconds+" seconds");
			return new JsonResult(false, "Did not start reload. Minimum wait of five minutes between reloads has not yet elapsed, remaining: "+toGoSeconds+" seconds");
		}
		LOGGER.info("Start reloading the ontology after a manual request");
		ontologyLoader.reloadOntologies();
		lastManualReload = System.currentTimeMillis();
		return new JsonResult(true);
	}
}
