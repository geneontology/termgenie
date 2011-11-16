package org.bbop.termgenie.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.ontology.obo.OBOParserTools;
import org.bbop.termgenie.ontology.obo.OBOWriterTools;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

public class JsonOntologyTerm {
	
	public static final Logger logger = Logger.getLogger(JsonOntologyTerm.class);

	private String tempId;
	private String label;
	private String definition;
	private List<String> defXRef;
	private List<JsonSynonym> synonyms;
	private List<String> relations;
	private List<String> metaData;
	private List<JsonChange> changed;
	private boolean isObsolete;

	public JsonOntologyTerm() {
		super();
	}

	/**
	 * @return the tempId
	 */
	public String getTempId() {
		return tempId;
	}

	/**
	 * @param tempId the tempId to set
	 */
	public void setTempId(String tempId) {
		this.tempId = tempId;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * @return the defxRef
	 */
	public List<String> getDefXRef() {
		return defXRef;
	}

	/**
	 * @param defXRef the defxRef to set
	 */
	public void setDefXRef(List<String> defXRef) {
		this.defXRef = defXRef;
	}

	/**
	 * @return the relations
	 */
	public List<String> getRelations() {
		return relations;
	}

	/**
	 * @param relations the relations to set
	 */
	public void setRelations(List<String> relations) {
		this.relations = relations;
	}

	/**
	 * @return the synonyms
	 */
	public List<JsonSynonym> getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(List<JsonSynonym> synonyms) {
		this.synonyms = synonyms;
	}

	/**
	 * @return the metaData
	 */
	public List<String> getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(List<String> metaData) {
		this.metaData = metaData;
	}
	
	/**
	 * @return the changed
	 */
	public List<JsonChange> getChanged() {
		return changed;
	}
	
	/**
	 * @param changed the changed to set
	 */
	public void setChanged(List<JsonChange> changed) {
		this.changed = changed;
	}

	public boolean isObsolete() {
		return isObsolete;
	}

	public void setObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonOntologyTerm [");
		builder.append("tempId=");
		builder.append(tempId);
		if (label != null) {
			builder.append(", ");
			builder.append("label=");
			builder.append(label);
		}
		if (definition != null) {
			builder.append(", ");
			builder.append("definition=");
			builder.append(definition);
		}
		if (synonyms != null) {
			builder.append(", ");
			builder.append("synonyms=");
			builder.append(synonyms);
		}
		if (defXRef != null) {
			builder.append(", ");
			builder.append("defXRef=");
			builder.append(defXRef);
		}
		if (relations != null) {
			builder.append(", ");
			builder.append("relations=");
			builder.append(relations);
		}
		if (metaData != null) {
			builder.append(", ");
			builder.append("metaData=");
			builder.append(metaData.toString());
		}
		if (changed != null) {
			builder.append(", ");
			builder.append("changed=");
			builder.append(changed);
		}
		if (isObsolete) {
			builder.append("isObsolete=true");
		}
		builder.append("]");
		return builder.toString();
	}

	public static JsonOntologyTerm createJson(Frame source, List<Frame> changed) {
		JsonOntologyTerm term = new JsonOntologyTerm();
		term.setTempId(source.getId());
		
		List<String> other = null;
		List<JsonSynonym> synonyms = null;
		List<String> jsonRelations = null;
		for (Clause clause : source.getClauses()) {
			OboFormatTag tag = OBOFormatConstants.getTag(clause.getTag());
			switch (tag) {
				case TAG_ID:
					break;
				case TAG_NAME:
					term.setLabel(clause.getValue(String.class));
					break;
				case TAG_SYNONYM:
					synonyms = add(JsonSynonym.convert(clause), synonyms);
					break;
				case TAG_DEF:
					term.setDefinition(clause.getValue(String.class));
					Collection<Xref> xrefs = clause.getXrefs();
					if (xrefs != null && !xrefs.isEmpty()) {
						List<String> xrefList = new ArrayList<String>(xrefs.size());
						for (Xref xref : xrefs) {
							xrefList.add(xref.getIdref());
						}
						term.setDefXRef(xrefList);	
					}
					break;

				case TAG_IS_A:
				case TAG_INTERSECTION_OF:
				case TAG_UNION_OF:
				case TAG_DISJOINT_FROM:
				case TAG_RELATIONSHIP:
					jsonRelations = add(convert(clause), jsonRelations);
					break;
					
				case TAG_IS_OBSELETE:
					term.setObsolete(OBOConverterTools.isObsolete(clause));
					break;
				default:
					other = add(convert(clause), other);
					break;
			}
		}
		term.setSynonyms(synonyms);
		term.setRelations(jsonRelations);
		term.setMetaData(other);
		
		// changed
		term.setChanged(createJson(changed));
		return term;
	}

	public static List<JsonChange> createJson(List<Frame> changed) {
		if (changed != null && !changed.isEmpty()) {
			List<JsonChange> jsonChanged = new ArrayList<JsonChange>(changed.size());
			for (Frame changedFrame : changed) {
				List<String> frameChanges = null;
				Collection<Clause> changedClauses = changedFrame.getClauses();
				if (changedClauses != null) {
					for (Clause relation : changedClauses) {
						frameChanges = add(convert(relation), frameChanges);		
					}
				}
				if (frameChanges != null) {
					JsonChange jsonChange = new JsonChange();
					jsonChange.setId(changedFrame.getId());
					jsonChange.setChanges(frameChanges);
					jsonChanged.add(jsonChange);
				}
			}
			return jsonChanged;
		}
		return null;
	}
	
	private static <T> List<T> add(T elem, List<T> list) {
		if (list == null) {
			list = new ArrayList<T>();
		}
		list.add(elem);
		return list;
	}
	
	private static String convert(Clause clause) {
		try {
			String string = OBOWriterTools.writeClause(clause, null);
			return string;
		} catch (IOException exception) {
			logger.error("Could not serialze clause.", exception);
			return null;
		}
	}
	
	public static Frame createFrame(JsonOntologyTerm term) {
		Frame frame = OBOConverterTools.createTermFrame(term.getTempId(), term.getLabel());
		OBOConverterTools.addObsolete(frame, term.isObsolete());
		OBOConverterTools.addDefinition(frame, term.getDefinition(), term.getDefXRef());
		List<JsonSynonym> jsonSynonyms = term.getSynonyms();
		if (jsonSynonyms != null && !jsonSynonyms.isEmpty()) {
			for (JsonSynonym jsonSynonym : jsonSynonyms) {
				OBOConverterTools.addSynonym(frame, jsonSynonym.getLabel(), jsonSynonym.getScope(), jsonSynonym.getXrefs());
			}
		}
		OBOParserTools.parseClauses(frame, term.getMetaData());
		OBOParserTools.parseClauses(frame, term.getRelations());
		return frame;
	}
	
	public static List<Frame> createChangedFrames(List<JsonChange> jsonChanges) {
		List<Frame> changed = null;
		if (jsonChanges != null && !jsonChanges.isEmpty()) {
			changed = new ArrayList<Frame>(jsonChanges.size());
			for (JsonChange jsonChange : jsonChanges) {
				Frame frame = OBOConverterTools.createTermFrame(jsonChange.getId());
				OBOParserTools.parseClauses(frame, jsonChange.getChanges());
				changed.add(frame);
			}
			
		}
		return changed;
	}
	
	public static class JsonSynonym {

		private String label;
		private String scope;
		private String category;
		private Set<String> xrefs;

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * @return the scope
		 */
		public String getScope() {
			return scope;
		}

		/**
		 * @param scope the scope to set
		 */
		public void setScope(String scope) {
			this.scope = scope;
		}

		/**
		 * @return the xrefs
		 */
		public Set<String> getXrefs() {
			return xrefs;
		}

		/**
		 * @param xrefs the xrefs to set
		 */
		public void setXrefs(Set<String> xrefs) {
			this.xrefs = xrefs;
		}

		/**
		 * @return the category
		 */
		public String getCategory() {
			return category;
		}

		/**
		 * @param category the category to set
		 */
		public void setCategory(String category) {
			this.category = category;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("JsonSynonym [");
			if (label != null) {
				builder.append("label=");
				builder.append(label);
				builder.append(", ");
			}
			if (scope != null) {
				builder.append("scope=");
				builder.append(scope);
				builder.append(", ");
			}
			if (category != null) {
				builder.append("category=");
				builder.append(category);
				builder.append(", ");
			}
			if (xrefs != null) {
				builder.append("xrefs=");
				builder.append(xrefs);
			}
			builder.append("]");
			return builder.toString();
		}
		
		static JsonSynonym convert(Clause clause) {
			JsonSynonym jsonSynonym = new JsonSynonym();
			jsonSynonym.setLabel(clause.getValue(String.class));
			String scope = clause.getValue2(String.class);
			String category = null;
			if (!OBOConverterTools.isScope(scope)) {
				category = scope;
				scope = null;
			}
			else {
				Collection<Object> values = clause.getValues();
				if (values.size() > 2) {
					Iterator<Object> iterator = values.iterator();
					iterator.next();
					iterator.next();
					category = (String) iterator.next();
				}
			}
			jsonSynonym.setScope(scope);
			jsonSynonym.setCategory(category);
			Collection<Xref> xrefs = clause.getXrefs();
			if (xrefs != null && !xrefs.isEmpty()) {
				Set<String> xrefSet = new HashSet<String>();
				for (Xref xref : xrefs) {
					xrefSet.add(xref.getIdref());
				}
				jsonSynonym.setXrefs(xrefSet);
			}

			return jsonSynonym;
		}
	}
	
	public static class JsonChange {
		
		private String id;
		private List<String> changes;
		
		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}
		
		/**
		 * @return the changes
		 */
		public List<String> getChanges() {
			return changes;
		}
		
		/**
		 * @param changes the changes to set
		 */
		public void setChanges(List<String> changes) {
			this.changes = changes;
		}
	}
}
