package org.bbop.termgenie.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

public final class YamlTool {

	private YamlTool() {
		// no instances
	}

	public static Object load(File file) {
		try {
			String yamlContent = FileUtils.readFileToString(file);
			Yaml yaml = new Yaml();
			return yaml.load(yamlContent);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
