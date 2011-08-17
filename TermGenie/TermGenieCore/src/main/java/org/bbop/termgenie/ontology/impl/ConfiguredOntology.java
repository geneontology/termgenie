package org.bbop.termgenie.ontology.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.Ontology;

public class ConfiguredOntology extends Ontology {

	String source = null;
	List<String> supports = null;
	List<String> requires = null;

	ConfiguredOntology(String name) {
		super(name, null, null);
	}

	void setRoots(List<String> roots) {
		this.roots = roots;
	}

	void addSupport(String support) {
		if (support == null) {
			return;
		}
		if (supports == null) {
			supports = new ArrayList<String>(6);
		}
		supports.add(support);
	}

	void setSupport(List<String> supports) {
		this.supports = supports;
	}

	void addRequires(String requires) {
		if (requires == null) {
			return;
		}
		if (this.requires == null) {
			this.requires = new ArrayList<String>(3);
		}
		this.requires.add(requires);
	}

	void setRequires(List<String> requires) {
		this.requires = requires;
	}

	/**
	 * @return the supports
	 */
	List<String> getSupports() {
		if (supports == null) {
			return Collections.emptyList();
		}
		return supports;
	}

	/**
	 * @return the requires
	 */
	List<String> getRequires() {
		if (requires == null) {
			return Collections.emptyList();
		}
		return requires;
	}

	protected ConfiguredOntology createBranch(String subOntologyName, List<String> roots) {
		ConfiguredOntology branch = new ConfiguredOntology(name);
		branch.requires = requires;
		branch.source = source;
		branch.supports = supports;
		branch.setBranch(subOntologyName, roots);
		return branch;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConfiguredOntology [");
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (subOntologyName != null) {
			builder.append("subOntologyName=");
			builder.append(subOntologyName);
			builder.append(", ");
		}
		if (roots != null) {
			builder.append("roots=");
			builder.append(roots);
			builder.append(", ");
		}
		if (source != null) {
			builder.append("source=");
			builder.append(source);
			builder.append(", ");
		}
		if (supports != null) {
			builder.append("supports=");
			builder.append(supports);
			builder.append(", ");
		}
		if (requires != null) {
			builder.append("requires=");
			builder.append(requires);
		}
		builder.append("]");
		return builder.toString();
	}
	
	public static ConfiguredOntology createCopy(ConfiguredOntology ontology, String localSource) {
		ConfiguredOntology modified = new ConfiguredOntology(ontology.getUniqueName());
		modified.requires = ontology.requires;
		modified.source = localSource;
		modified.supports = ontology.supports;
		modified.setBranch(ontology.subOntologyName, ontology.roots);
		return modified;
	}
}
