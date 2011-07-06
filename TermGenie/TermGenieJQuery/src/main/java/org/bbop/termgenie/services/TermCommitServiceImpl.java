package org.bbop.termgenie.services;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.tools.ImplementationFactory;
import org.bbop.termgenie.tools.OntologyTools;

public class TermCommitServiceImpl implements TermCommitService {

	private static final OntologyTools ontologyTools = ImplementationFactory.getOntologyTools();
	
//	@Override
//	public boolean isValidUser(String username, String password, String ontology) {
//		return ImplementationFactory.getUserCredentialValidator().validate(username, password, ontology);
//	}

	@Override
	public JsonExportResult exportTerms(String sessionId, JsonOntologyTerm[] terms, String ontologyName) {
		JsonExportResult result = new JsonExportResult();
		
		Ontology ontology = ontologyTools.getOntology(ontologyName);
		if (ontology == null) {
			result.setSuccess(false);
			result.setMessage("Unknown ontology: "+ontologyName);
			return result;
		}
		
		// TODO use a proper obo export tool here!
		StringBuilder sb = new StringBuilder();
		sb.append("Preliminary export results:<br/>");
		sb.append("<pre>\n");
		for (JsonOntologyTerm term : terms) {
			sb.append("[Term]\n");
			String id = ontology.getRealInstance().getOntologyId();
			sb.append("id: ");
			sb.append(id);
			sb.append(":-------\n");
			sb.append("name: ");
			sb.append(term.getLabel());
			sb.append('\n');
			sb.append("def: \"");
			sb.append(term.getDefinition());
			sb.append("\"");
			String[] defxRefs = term.getDefxRef();
			if (defxRefs != null && defxRefs.length > 0) {
				sb.append(" [");
				for (int i = 0; i < defxRefs.length; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(defxRefs[i]);
				}
				sb.append("]\n");
			}
			else {
				sb.append('\n');
			}
			final String comment = term.getMetaData().getComment();
			if (comment != null && !comment.isEmpty()) {
				sb.append("comment: \"");
				sb.append(comment);
				sb.append("\"\n");
			}
			JsonSynonym[] synonyms = term.getSynonyms();
			if (synonyms != null && synonyms.length > 0) {
				for (JsonSynonym synonym : synonyms) {
					sb.append("synonym: \"");
					sb.append(synonym.getLabel());
					// TODO add scope and xrefs
					sb.append("\"\n");
				}
			}
			
			/*
	private String logDef;
	private JsonTermRelation[] relations;
			 */
//			is_a
//			intersection_of
//			union_of
//			disjoint_from
//			relationship
			sb.append("created_by: ");
			sb.append(term.getMetaData().getCreated_by());
			sb.append('\n');
			sb.append("creation_date: ");
			sb.append(term.getMetaData().getCreation_date());
			sb.append('\n');
			sb.append('\n');
		}
		sb.append("\n</pre>");
		result.setSuccess(true);
		result.setFormats(new String[]{"OBO"});
		result.setContents(new String[]{sb.toString()});
		// TODO add OWL export support
		return result;
	}
	
	@Override
	public JsonCommitResult commitTerms(String sessionId, JsonOntologyTerm[] terms, String ontology) {
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("The commit operation is not yet implemented.");
		return result;
	}

}
