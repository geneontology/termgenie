package org.bbop.termgenie.tools;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GitYamlImpl implements GitYaml {
	
	private static final Logger LOGGER = Logger.getLogger(GitYaml.class);

	private final SimpleGitTool git;
	private final File realTargetFile;
	private final String repositoryURL;

	@Inject
	public GitYamlImpl(@Named("GitYamlUrl") final String repositoryURL, 
			@Named("GitYamlFile") final String targetFile, 
			@Named("GitYamlWorkFolder") final String workFolder,
			@Named("GitYamlLoaderPeriod") long period,
			@Named("GitYamlLoaderTimeUnit") TimeUnit unit) throws Exception
	{
		this.repositoryURL = repositoryURL;
		// set up work folder
		File workFolderFile = new File(workFolder).getCanonicalFile();
		workFolderFile.mkdirs();
		FileUtils.cleanDirectory(workFolderFile);
		
		// setup git
		git = new SimpleGitTool(repositoryURL, workFolderFile);
		realTargetFile = git.checkout(targetFile);
		if (realTargetFile == null) {
			throw new RuntimeException("Could successfully access yaml file: "+repositoryURL+" "+targetFile+" "+workFolder);
		}
		else {
			LOGGER.info("Done setup for git yaml: "+realTargetFile);
		}
		
		// setup reload
		// use invalid settings to de-activate the reloading
		if (period > 0 && unit != null) {
			// use java.concurrent to schedule periodic updates from git
			Runnable command = new Runnable() {

				@Override
				public void run() {
					LOGGER.info("Scheduled Event - update git yaml");
					update();
				}
			};
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleWithFixedDelay(command, period, period, unit);
		}
		else {
			LOGGER.warn("Git Yaml reloading is deactivated, due to invalid settings: period="+period+" unit="+unit);
		}
	}

	@Override
	public File getYamlFile() {
		return realTargetFile;
	}

	@Override
	public synchronized void update() {
		try {
			git.update();
		} catch (IOException e) {
			LOGGER.warn("Could not update git yaml: "+repositoryURL, e);
		}
	}
}
