package org.bbop.termgenie.rules;

import java.util.List;

import org.bbop.termgenie.tools.Pair;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public interface TermGenieScriptFunctionsCDef extends TermGenieScriptFunctions {

	/**
	 * Create a logical definition for a term in form of a {@link CDef}.
	 * 
	 * @param genus
	 * @return cdef
	 * @see CDef for adding differentia and properties
	 */
	public CDef cdef(OWLObject genus);

	/**
	 * Create a logical definition for a term in form of a {@link CDef}.
	 * 
	 * @param genus id
	 * @return cdef
	 * @see CDef for adding differentia and properties
	 */
	public CDef cdef(String genus);

	/**
	 * Create a logical definition for a term in form of a {@link CDef}.
	 * 
	 * @param genus
	 * @param ontology
	 * @return cdef
	 * @see CDef for adding differentia and properties
	 */
	public CDef cdef(OWLObject genus, OWLGraphWrapper ontology);

	/**
	 * Create a logical definition for a term in form of a {@link CDef}.
	 * 
	 * @param genus id
	 * @param ontology
	 * @return cdef
	 * @see CDef for adding differentia and properties
	 */
	public CDef cdef(String genus, OWLGraphWrapper ontology);

	/**
	 * Locigal defintion used for relation generation in term genie.
	 */
	public static interface CDef {

		/**
		 * Add a differentium to this cdef.
		 * 
		 * @param rel relation
		 * @param term term
		 * @param ontology the ontology to look the terms up
		 */
		public void differentium(String rel, OWLObject term, OWLGraphWrapper ontology);

		/**
		 * Add a differentium to this cdef.
		 * 
		 * @param rel relation
		 * @param terms terms
		 * @param ontology the ontology to look the terms up
		 */
		public void differentium(String rel, OWLObject[] terms, OWLGraphWrapper ontology);

		/**
		 * Add a differentium to this cdef.
		 * 
		 * @param rel relation
		 * @param term term
		 * @param ontologies the ontologies to look the terms up
		 */
		public void differentium(String rel, OWLObject term, OWLGraphWrapper[] ontologies);

		/**
		 * Add a differentium to this cdef.
		 * 
		 * @param rel relation
		 * @param terms terms
		 * @param ontologies the ontologies to look the terms up
		 */
		public void differentium(String rel, OWLObject[] terms, OWLGraphWrapper[] ontologies);

		/**
		 * Add a property to this cdef.
		 * 
		 * @param property
		 */
		public void property(String property);

		public Pair<OWLObject, OWLGraphWrapper> getBase();

		public List<String> getProperties();

		public List<Differentium> getDifferentia();

		public static class Differentium {

			private final String relation;
			private final List<OWLObject> terms;
			private final List<OWLGraphWrapper> ontologies;

			/**
			 * @param relation
			 * @param terms
			 * @param ontologies
			 */
			public Differentium(String relation,
					List<OWLObject> terms,
					List<OWLGraphWrapper> ontologies)
			{
				super();
				this.relation = relation;
				this.terms = terms;
				this.ontologies = ontologies;
			}

			/**
			 * @return the relation
			 */
			public String getRelation() {
				return relation;
			}

			/**
			 * @return the terms
			 */
			public List<OWLObject> getTerms() {
				return terms;
			}

			/**
			 * @return the ontologies
			 */
			public List<OWLGraphWrapper> getOntologies() {
				return ontologies;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("Differentium [");
				if (relation != null) {
					builder.append("relation=");
					builder.append(relation);
				}
				if (terms != null) {
					builder.append(", ");
					builder.append("terms=");
					builder.append(terms);
				}
				if (ontologies != null) {
					builder.append(", ");
					builder.append("ontologies=");
					builder.append(ontologies);
				}
				builder.append("]");
				return builder.toString();
			}
		}
	}

	// --------------------------

	/**
	 * Create a new term and provide output which can directly be returned.
	 * 
	 * @param label
	 * @param definition
	 * @param synonyms
	 * @param logicalDefinition
	 */
	public void createTerm(String label,
			String definition,
			List<Synonym> synonyms,
			CDef logicalDefinition);

}
