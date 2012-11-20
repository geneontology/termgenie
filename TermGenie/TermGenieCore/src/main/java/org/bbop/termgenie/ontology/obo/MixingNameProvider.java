package org.bbop.termgenie.ontology.obo;

import java.util.HashMap;
import java.util.Map;

import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;

public class MixingNameProvider implements NameProvider {

	private final NameProvider mainProvider;
	private Map<String, String> otherNames = new HashMap<String, String>();

	public MixingNameProvider(NameProvider mainProvider) {
		this.mainProvider = mainProvider;
	}

	@Override
	public String getName(String id) {
		if (mainProvider != null) {
			String name = mainProvider.getName(id);
			if (name != null) {
				return name;
			}
		}
		return otherNames.get(id);
	}

	@Override
	public String getDefaultOboNamespace() {
		if (mainProvider != null) {
			return mainProvider.getDefaultOboNamespace();
		}
		return null;
	}
	
	public void addName(String id, String name) {
		otherNames.put(id, name);
	}
	
}