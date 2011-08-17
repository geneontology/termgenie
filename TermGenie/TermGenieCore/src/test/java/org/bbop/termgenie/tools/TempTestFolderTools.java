package org.bbop.termgenie.tools;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

/**
 * Helper tools for handling temporary test folders.
 */
public class TempTestFolderTools {

	/**
	 * Create a new test folder relative to a given class. The idea is, that the
	 * test folder is created in the build directory of the corresponding
	 * project, thus isolating multiple project instances from each other. Also,
	 * the user usually has the appropriate rights in the build folder. <br/>
	 * <br/>
	 * <b>Limits:</b> The class cls may not be in a jar.
	 * 
	 * @param cls The class to which this test folder should be relative to.
	 * @return testFolder
	 * @see #deleteTestFolder(File)
	 */
	public static File createTestFolder(Class<?> cls) {
		try {
			URL resource = getResourceURL(cls);
			File classFile = new File(resource.toURI());
			File testFolder = new File(classFile.getParentFile(), cls.getSimpleName() + "-TestFolder");
			FileUtils.forceMkdir(testFolder);
			FileUtils.cleanDirectory(testFolder);
			return testFolder;
		} catch (URISyntaxException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private static URL getResourceURL(Class<?> cls) {
		String name = cls.getName();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '.') {
				c = '/';
			}
			sb.append(c);
		}
		sb.append(".class");
		URL resource = find(sb.toString(), cls);
		if (resource == null) {
			throw new RuntimeException("No resource found for class: " + cls.getName());
		}
		return resource;
	}

	private static URL find(String name, Class<?> cls) {
		URL resource = cls.getResource(name);
		if (resource == null) {
			resource = ClassLoader.getSystemResource(name);
		}
		return resource;
	}

	/**
	 * Delete a testFolder recursively. WARNING: Only use it on test folders
	 * created with this tool, as this is a full recursive delete!
	 * 
	 * @param testFolder
	 * @see #createTestFolder(Class)
	 */
	public static void deleteTestFolder(File testFolder) {
		try {
			FileUtils.deleteDirectory(testFolder);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
