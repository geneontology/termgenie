package org.bbop.termgenie.ontology.impl;

import java.util.Set;

import org.bbop.termgenie.ontology.impl.FileCachingIRIMapper.FileCachingFilter;
import org.semanticweb.owlapi.model.IRI;

public class FileCachingIgnoreFilter implements FileCachingFilter {

	private final Set<IRI> ignoreIRIs;

	public FileCachingIgnoreFilter(Set<IRI> ignoreIRIs) {
		this.ignoreIRIs = ignoreIRIs;
	}

	@Override
	public boolean allowCaching(IRI iri) {
		return !ignoreIRIs.contains(iri);
	}
	
	public static class IgnoresContainsDigits implements FileCachingFilter {

		private final Set<IRI> ignoreIRIs;

		public IgnoresContainsDigits(Set<IRI> ignoreIRIs) {
			this.ignoreIRIs = ignoreIRIs;
		}
		
		@Override
		public boolean allowCaching(IRI iri) {
			if (ignoreIRIs.contains(iri)) {
				return false;
			}
			String s = iri.toString();
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (Character.isDigit(c)) {
					return false;
				}
			}
			return true;
		}
	}
}