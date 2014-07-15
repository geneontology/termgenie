package org.bbop.termgenie.startup;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starter methods for testing termgenie with an embedded jetty instance.
 */
public class JettyTestStartup {

	public static void startup(int port, String contextPath, String webappPath) {
		Server jettyServer = new Server(port);
		initWebappContext(jettyServer, contextPath, webappPath);
		try {
			jettyServer.start();
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}

	public static void initWebappContext(Server jettyServer, String contextPath, String webappPath)
	{
		WebAppContext context = new WebAppContext();

		context.setDescriptor(webappPath + "/WEB-INF/web.xml");
		context.setResourceBase(webappPath);
		context.setContextPath(contextPath);
		context.setParentLoaderPriority(true);

		jettyServer.setHandler(context);
	}

}
