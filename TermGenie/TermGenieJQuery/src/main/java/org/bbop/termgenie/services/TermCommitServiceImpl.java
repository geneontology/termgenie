package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.tools.ImplementationFactory;

public class TermCommitServiceImpl implements TermCommitService {

	@Override
	public boolean isValidUser(String username, String password, String ontology) {
		return ImplementationFactory.getUserCredentialValidator().validate(username, password, ontology);
	}

	@Override
	public JsonExportResult exportTerms(JsonOntologyTerm[] terms, String ontology) {
		JsonExportResult result = new JsonExportResult();
		// TODO use a proper obo export tool here!
		StringBuilder sb = new StringBuilder();
		sb.append("Preliminary export results:<br/>");
		sb.append("<pre>\n");
		for (JsonOntologyTerm term : terms) {
			sb.append("[Term]\n");
			sb.append("id: GO:-------\n");
			sb.append("name: ");
			sb.append(term.getLabel());
			sb.append('\n');
			sb.append("def: \"");
			sb.append(term.getDefinition());
			sb.append("\"\n");
			final String comment = term.getComment();
			if (comment != null && !comment.isEmpty()) {
				sb.append("comment: \"");
				sb.append(comment);
				sb.append("\"\n");
			}
			String[] synonyms = term.getSynonyms();
			if (synonyms != null && synonyms.length > 0) {
				for (String synonym : synonyms) {
					sb.append("synonym: \"");
					sb.append(synonym);
					sb.append("\"\n");
				}
			}
			String[] defxRefs = term.getDefxRef();
			if (defxRefs != null && defxRefs.length > 0) {
				sb.append("xref: [");
				for (int i = 0; i < defxRefs.length; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(defxRefs[i]);
				}
				sb.append("]\n");
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
	public JsonCommitResult commitTerms(JsonOntologyTerm[] terms, String ontology, String username,
			String password) {
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("The commit operation is not yet implemented.");
		return result;
	}

}
