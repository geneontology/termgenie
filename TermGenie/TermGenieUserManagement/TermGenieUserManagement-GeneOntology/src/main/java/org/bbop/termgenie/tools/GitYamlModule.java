package org.bbop.termgenie.tools;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.bbop.termgenie.core.ioc.IOCModule;

public class GitYamlModule extends IOCModule {
	
	private final String repositoryURL;
	private final String targetFile;
	private final String workFolder;
	private final long period;
	private final TimeUnit unit;

	public GitYamlModule(String repositoryURL, String targetFile, 
			String workFolder, long period, TimeUnit unit, 
			Properties applicationProperties)
	{
		super(applicationProperties);
		this.repositoryURL = repositoryURL;
		this.targetFile = targetFile;
		this.workFolder = workFolder;
		this.period = period;
		this.unit = unit;
	}

	@Override
	protected void configure() {
		bind(GitYaml.class, GitYamlImpl.class);
		bind("GitYamlUrl", repositoryURL);
		bind("GitYamlFile", targetFile);
		bind("GitYamlWorkFolder", workFolder);
		bind("GitYamlLoaderPeriod", period);
		bind("GitYamlLoaderTimeUnit", unit);
	}
}
