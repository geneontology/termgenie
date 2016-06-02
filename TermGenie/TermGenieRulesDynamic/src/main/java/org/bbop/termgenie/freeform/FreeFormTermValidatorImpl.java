package org.bbop.termgenie.freeform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.Nullable;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.SharedReasoner;
import org.bbop.termgenie.freeform.FreeFormTermRequest.Xref;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.owl.InferAllRelationshipsTask;
import org.bbop.termgenie.owl.InferredRelations;
import org.bbop.termgenie.rules.TemporaryIdentifierTools;
import org.bbop.termgenie.rules.TermGenieScriptRunner;
import org.bbop.termgenie.rules.impl.TextualDefinitionTool;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.xrefs.XrefTools;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class FreeFormTermValidatorImpl implements FreeFormTermValidator {
	
	private static final Logger logger = Logger.getLogger(FreeFormTermValidatorImpl.class);
	
	static final String ADD_SUBSET_TAG_PARAM = "FreeFormTermValidatorAddSubsetTag";
	static final String SUBSET_PARAM = "FreeFormTermValidatorSubsetTag";
	static final String SUPPORTED_NAMESPACES = "FreeFormValidatorOboNamespaces";
	static final String ADDITIONAL_RELATIONS = "FreeFormAdditionalRelations";
	
	private final OntologyTaskManager ontology;
	private final ReasonerFactory factory;
	private final boolean useIsInferred;
	private final boolean addSubsetTag;
	
	private final String idPrefix;

	private List<String> oboNamespaces = null;
	private List<String> additionalRelations = null;
	private String subset = null;

	@Inject
	public FreeFormTermValidatorImpl(OntologyLoader loader,
			@Named(ADD_SUBSET_TAG_PARAM) boolean addSubsetTag,
			@Named(TermGenieScriptRunner.USE_IS_INFERRED_BOOLEAN_NAME) boolean useIsInferred,
			ReasonerFactory factory)
	{
		super();
		this.addSubsetTag = addSubsetTag;
		this.ontology = loader.getOntologyManager();
		this.factory = factory;
		this.useIsInferred = useIsInferred;
		this.idPrefix = TemporaryIdentifierTools.getTempIdPrefix(ontology);
	}

	
	/**
	 * @param subset the subset to set
	 * 
	 * only effective if addSubsetTag is also set to true, see constructor.
	 */
	@Inject(optional=true)
	@Nullable
	public void setSubset(@Named(SUBSET_PARAM) String subset) {
		this.subset = subset;
	}
	
	@Inject(optional=true)
	@Nullable
	public void setOboNamespaces(@Named(SUPPORTED_NAMESPACES) List<String> oboNamespaces) {
		this.oboNamespaces = oboNamespaces;
	}

	@Inject(optional=true)
	@Nullable
	public void setAdditionalRelations(@Named(ADDITIONAL_RELATIONS) List<String> additionalRelations) {
		this.additionalRelations = additionalRelations;
	}
	
	private FreeFormValidationResponse error(String message) {
		FreeFormValidationResponse response = new FreeFormValidationResponse();
		response.setGeneralError(message);
		return response;
	}
	
	private FreeFormValidationResponse error(List<FreeFormHint> errors) {
		FreeFormValidationResponse response = new FreeFormValidationResponse();
		response.setErrors(errors);
		return response;
	}
	
	private FreeFormValidationResponse success(Pair<Frame, Set<OWLAxiom>> term, List<FreeFormHint> warnings) {
		FreeFormValidationResponse response = new FreeFormValidationResponse();
		response.setGeneratedTerm(term);
		if (warnings != null && !warnings.isEmpty()) {
			response.setWarnings(warnings);
		}
		return response;
	}

	@Override
	public List<String> getOboNamespaces() {
		return oboNamespaces;
	}

	@Override
	public List<String> getAdditionalRelations() {
		return additionalRelations;
	}

	@Override
	public String getTempIdPrefix() {
		return idPrefix;
	}

	@Override
	public FreeFormValidationResponse validate(final FreeFormTermRequest request,
			boolean isEditor, 
			ProcessState state)
	{
		String subset = null;
		if (this.subset != null && addSubsetTag) {
			subset = this.subset;
		}
		boolean requireLiteratureReference = true;
		boolean checkLabelLength = true;
		if (isEditor) {
			requireLiteratureReference = false;
			checkLabelLength = false;
		}
		ValidationTask task = new ValidationTask(request, checkLabelLength, requireLiteratureReference, useIsInferred, subset, idPrefix, oboNamespaces, factory, state);
		try {
			ontology.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			String message = "Error during term validation, due to an inconsistent ontology";
			logger.error(message, exception);
			return error(message);
		}
		if (task.errors == null || task.errors.isEmpty()) {
			if (task.term == null) {
				return error("No term was generated from your request");
			}
			return success(task.term, task.warnings);
		}
		return error(task.errors);
	}

	static class ValidationTask extends OntologyTask
	{
		private final ReasonerFactory reasonerFactory;
		private final FreeFormTermRequest request;
		private final boolean checkLabelLength;
		private final boolean requireLiteratureReference;
		private final boolean useIsInferred;
		private final List<String> namespaces;
		private final ProcessState state;
		
		List<FreeFormHint> errors = null;
		List<FreeFormHint> warnings = null;
		Pair<Frame, Set<OWLAxiom>> term;
		
		private final String subset;
		private final String idPrefix;
		private Modified modified = Modified.no;
	
		ValidationTask(FreeFormTermRequest request,
				boolean checkLabelLength,
				boolean requireLiteratureReference,
				boolean useIsInferred,
				String subset,
				String idPrefix,
				List<String> namespaces,
				ReasonerFactory factory,
				ProcessState state)
		{
			this.request = request;
			this.checkLabelLength = checkLabelLength;
			this.requireLiteratureReference = requireLiteratureReference;
			
			this.useIsInferred = useIsInferred;
			this.subset = subset;
			this.idPrefix = idPrefix;
			this.namespaces = namespaces;
			this.reasonerFactory = factory;
			this.state = state;
		}
	
		@Override
		public Modified runCatchingMod(OWLGraphWrapper graph)
		{
			ProcessState.addMessage(state, "Checking state of current ontology.");
			SharedReasoner manager = reasonerFactory.getSharedReasoner(graph);
			List<String> errors = manager.checkConsistency(graph);
			if (errors != null && !errors.isEmpty()) {
				this.errors = new ArrayList<FreeFormHint>(errors.size());
				for(String error : errors) {
					this.errors.add(new FreeFormHint("ontology", 
							"Cannot safely create term, due to the following ontology error: "+error));
				}
			}
			else {
				runInternal(graph);
			}
			return modified;
		}
		
		void runInternal(final OWLGraphWrapper graph) {
			ProcessState.addMessage(state, "Prepare label.");
			// check label
			String requestedLabel = StringUtils.trimToNull(request.getLabel());
			if (requestedLabel == null) {
				setError("label", "A label is required for a free from request.");
				return;
			}
			requestedLabel = StringUtils.normalizeSpace(requestedLabel);
			
			if (checkLabelLength && requestedLabel.length() < 7) {
				setError("label", "The provided label is too short.");
				return;
			}
			
			Set<Character> nonAscii = hasNonAscii(requestedLabel);
			if (!nonAscii.isEmpty()) {
				setCharacterError("label", "The label '"+requestedLabel+"'", nonAscii);
				return;
			}

			// search for similar labels and synonyms in the ontology 
			final CharSequence normalizedLabel = normalizeLabel(requestedLabel);
			
			// check proposed synonyms at the same time 
			List<ISynonym> checkedSynonyms = null;
			Map<CharSequence, Pair<String, String>> proposedSynonyms = null;
			
			// each synonym has to have:
			//  * label
			//  * scope (EXACT, NARROW, RELATED, No BROADER)
			// optional: xref
			List<? extends ISynonym> iSynonyms = request.getISynonyms();
			if (iSynonyms != null && !iSynonyms.isEmpty()) {
				ProcessState.addMessage(state, "Prepare synonyms.");
				checkedSynonyms = new ArrayList<ISynonym>();
				proposedSynonyms = new HashMap<CharSequence, Pair<String, String>>();
				Set<String> done = new HashSet<String>();
				for (ISynonym jsonSynonym : iSynonyms) {
					String synLabel = StringUtils.trimToNull(jsonSynonym.getLabel());
					if (synLabel == null) {
						addError("synonyms", "No empty labels as synonym allowed.");
						continue;
					}
					nonAscii = hasNonAscii(synLabel);
					if (!nonAscii.isEmpty()) {
						setCharacterError("synonyms", "The synonym '"+synLabel+"'", nonAscii);
						return;
					}
					
					String lowerCase = synLabel.toLowerCase();
					if (done.contains(lowerCase)) {
						addError("synonyms", "Duplicate synonym: "+synLabel);
						continue;
					}
					String scope = StringUtils.trimToNull(jsonSynonym.getScope());
					if (scope == null) {
						scope = OboFormatTag.TAG_RELATED.getTag();
					}
					else {
						if (isOboScope(scope) == false) {
							addError("synonyms", "The synonym '"+synLabel+"' has an unknown scope: "+scope);
						}
					}
					checkedSynonyms.add(jsonSynonym);
					proposedSynonyms.put(normalizeLabel(synLabel), Pair.of(synLabel, scope));
					done.add(lowerCase);
				}
			}
			
			ProcessState.addMessage(state, "Start - Search for existing terms with similar labels.");
			// iterate over all objects
			for(OWLObject current : graph.getAllOWLObjects()) {
				String currentLabel = graph.getLabel(current);
				if (currentLabel == null) {
					continue;
				}
				final CharSequence currentNormalizedLabel = normalizeLabel(currentLabel);
				if (equals(normalizedLabel, currentNormalizedLabel)) {
					addError("label", "The requested label is equal to the term: "+graph.getIdentifier(current)+" '"+currentLabel+"'");
					return;
				}
				else if (similar(normalizedLabel, currentNormalizedLabel)) {
					addWarning("label", "The requested label is similar to the term: "+graph.getIdentifier(current)+" '"+currentLabel+"'");
				}
				if (proposedSynonyms != null) {
					for (Entry<CharSequence, Pair<String, String>> entry : proposedSynonyms.entrySet()) {
						if (equals(currentNormalizedLabel, entry.getKey())) {
							Pair<String, String> pair = entry.getValue();
							String orig = pair.getOne();
							String origScope = pair.getTwo();
							if (OboFormatTag.TAG_EXACT.getTag().equals(origScope)) {
								addError("synonym", "The requested synonym '"+orig+"' is equal to the term: "+graph.getIdentifier(current)+" '"+currentLabel+"'");	
							}
							else {
								addWarning("synonym", "The requested synonym '"+orig+"' is equal to the term: "+graph.getIdentifier(current)+" '"+currentLabel+"'");	
							}
						}
						else if (similar(currentNormalizedLabel, entry.getKey())) {
							addWarning("synonym", "The requested synonym '"+entry.getValue().getOne()+"' is similar to the term: "+graph.getIdentifier(current)+" '"+currentLabel+"'");
						}
					}
				}
				List<ISynonym> oboSynonyms = graph.getOBOSynonyms(current);
				if (oboSynonyms != null) {
					for (ISynonym synonym : oboSynonyms) {
						if (OboFormatTag.TAG_EXACT.getTag().equals(synonym.getScope()) == false) {
							// skip any non EXACT synonyms
							continue;
						}
						String synLabel = synonym.getLabel();
						CharSequence normalizedSynLabel = normalizeLabel(synLabel);
						if (equals(normalizedLabel, normalizedSynLabel)) {
							addError("label",
									"The requested label is equal to the synonym: '" + synLabel + "' of term: " + graph.getIdentifier(current) + " '" + currentLabel + "'");
							return;
						}
						else if (similar(normalizedLabel, normalizedSynLabel)) {
							addWarning("label",
									"The requested label is similar to the synonym: '" + synLabel + "' of term: " + graph.getIdentifier(current) + " '" + currentLabel + "'");
						}
						if (proposedSynonyms != null) {
							for (Entry<CharSequence, Pair<String, String>> entry : proposedSynonyms.entrySet()) {
								if (equals(normalizedSynLabel, entry.getKey())) {
									Pair<String, String> pair = entry.getValue();
									String orig = pair.getOne();
									String origScope = pair.getTwo();
									if (OboFormatTag.TAG_EXACT.getTag().equals(origScope)) {
										addError("synonym", "The requested synonym '"+orig+"' is equal to the synonym: '" + synLabel + "' of term: " + graph.getIdentifier(current) + " '" + currentLabel + "'");
									}
									else {
										addWarning("synonym", "The requested synonym '"+orig+"' is equal to the synonym: '" + synLabel + "' of term: " + graph.getIdentifier(current) + " '" + currentLabel + "'");
									}
								}
								if (similar(normalizedSynLabel, entry.getKey())) {
									addWarning("synonym", "The requested synonym '"+entry.getValue().getOne()+"' is similar to the synonym: '" + synLabel + "' of term: " + graph.getIdentifier(current) + " '" + currentLabel + "'");
								}
							}
						}
					}
				}
			}
			ProcessState.addMessage(state, "Finished - Search for existing terms with similar labels.");
			if (errors != null) {
				return;
			}
			
			String comment = StringUtils.trimToNull(request.getComment());
			
			// TODO check requested terms in the queue
			// TODO check blacklist

			// check namespace
			String namespace = StringUtils.trimToNull(request.getNamespace());
			if (namespaces != null) {
				if (namespace == null || namespace.isEmpty()) {
					setError("namespace", "A namespace is required for request: "+requestedLabel);
					return;
				}
			}

			// check relations

			// at least one is_a parent in correct namespace
			final List<String> parents = request.getIsA();
			if (parents == null || parents.isEmpty()) {
				setError("parents", "At least one is_a parent required for request: "+requestedLabel);
				return;
			}
			Set<OWLClass> superClasses = new HashSet<OWLClass>();
			for (String parentId : parents) {
				if (parentId == null) {
					addError("is_a parent", "null parent");
					continue;
				}
				OWLClass cls = graph.getOWLClassByIdentifier(parentId);
				if (cls == null) {
					addError("is_a parent", "parent not found in ontology: "+parentId);
					continue;
				}
				String parentNamespace = graph.getNamespace(cls);
				if (namespace == null || namespace.equals(parentNamespace)) {
					superClasses.add(cls);
				}
				else {
					addError("is_a parent", "namespace conflict parent namespace: '"+parentNamespace+"' requested namespace: '"+namespace+"'");
				}
			}
			if (errors != null) {
				return;
			}
			
			// TODO should we test for too high level parents? blacklist?
			
			// additional relations (i.e. part_of, has_part, ...)
			Map<OWLObjectProperty, Set<OWLClass>> additionalRelations = null;
			Map<String, List<String>> requestedRelations = request.getAdditionalRelations();
			if (requestedRelations != null && !requestedRelations.isEmpty()) {
				additionalRelations = new HashMap<OWLObjectProperty, Set<OWLClass>>();
				for(Entry<String, List<String>> entry : requestedRelations.entrySet()) {
					String relId = entry.getKey();
					// first: find property
					final OWLObjectProperty prop = graph.getOWLObjectPropertyByIdentifier(relId);
					if (prop == null) {
						setError(relId, "Could not find the "+relId+" property.");
						return;
					}
					
					List<String> ids = entry.getValue();
					if (ids == null || ids.isEmpty()) {
						continue;
					}
					// second: validate given classes 
					final Set<OWLClass> classes = new HashSet<OWLClass>();
					for(String id : ids) {
						if (id == null) {
							addError(relId+" parent", "null parent");
							continue;
						}
						OWLClass cls = graph.getOWLClassByIdentifier(id);
						if (cls == null) {
							addError(relId+" parent", "parent not found in ontology: "+id);
							continue;
						}
						classes.add(cls);
					}
					if (!classes.isEmpty()) {
						additionalRelations.put(prop, classes);
					}
				}
			}
			
			if (errors != null) {
				return;
			}
			
			// check definition
			String def = StringUtils.trimToNull(request.getDefinition());
			if (def == null || def.length() < 20) {
				// check that the definition is at least X amount of chars long
				setError("definition", "Please enter a valid definition");
				return;
			}
			nonAscii = hasNonAscii(def);
			if (!nonAscii.isEmpty()) {
				setCharacterError("definition", "The definition", nonAscii);
				return;
			}
			
			// Check definition
			String definitionErrors = TextualDefinitionTool.validateDefinition(def);
			if (definitionErrors != null) {
				setError("definition", definitionErrors);
				return;
			}
			
			// search for similar definitions?
			
			// require at least on def db xref, ideally includes PMID
			List<String> defXRefsList = request.getDbxrefs();
			if (defXRefsList == null || defXRefsList.isEmpty()) {
				setError("definition db xref", "Please enter at least one valid definition db xref");
				return;
			}
			
			ProcessState.addMessage(state, "Check Def Xrefs.");
			Set<String> defXrefs = new HashSet<String>();
			boolean hasLiteratureReference = false;
			for(String  xref : defXRefsList) {
				String dbxref = StringUtils.trimToNull(xref);
				if (dbxref == null) {
					continue;
				}
				String xrefError = XrefTools.checkXref(xref);
				if (xrefError != null) {
					addError("definition db xref", xrefError);
					continue;
				}
				if (hasLiteratureReference == false) {
					hasLiteratureReference = XrefTools.isLiteratureReference(xref);
				}
				defXrefs.add(dbxref);
			}
			if (hasLiteratureReference == false) {
				addHint(requireLiteratureReference, "definition db xref", XrefTools.getLiteratureReferenceErrorString(false));
			}
			
			if (defXrefs.isEmpty()) {
				setError("definition db xref", "Please enter at least one valid definition db xref");
				return;
			}
			
			if (errors != null) {
				return;
			}
			
			// add general xrefs
			List<Xref> xrefs = request.getXrefs();
			if (xrefs != null && !xrefs.isEmpty()) {
				for (Xref xref : xrefs) {
					String idref = xref.getIdRef();
					String error = XrefTools.checkXref(idref);
					if (error != null) {
						addError("xref", error);
					}
				}
			}
			
			if (errors != null) {
				return;
			}
			
			// all checks passed, create term
			ProcessState.addMessage(state, "Start - Use reasoner to check constraints and relations.");
			
			String owlNewId = getNewId();
			String oboNewId = Owl2Obo.getIdentifier(IRI.create(owlNewId));
			
			// minimal OWL
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			OWLOntologyManager owlManager = graph.getManager();
			OWLDataFactory factory = owlManager.getOWLDataFactory();
			IRI iri = IRI.create(owlNewId);
			OWLClass owlClass = factory.getOWLClass(iri);
			axioms.add(factory.getOWLDeclarationAxiom(owlClass));
			axioms.add(OwlTranslatorTools.createLabelAxiom(owlNewId, requestedLabel, graph));
			
			Set<OWLAxiom> preliminaryAxioms = new HashSet<OWLAxiom>();
			for(OWLClass superClass : superClasses) {
				preliminaryAxioms.add(factory.getOWLSubClassOfAxiom(owlClass, superClass));
			}
			
			if (additionalRelations != null && !additionalRelations.isEmpty()) {
				for(OWLObjectProperty prop : additionalRelations.keySet()) {
					Set<OWLClass> classes = additionalRelations.get(prop);
					for (OWLClass cls : classes) {
						preliminaryAxioms.add(factory.getOWLSubClassOfAxiom(owlClass, factory.getOWLObjectSomeValuesFrom(prop, cls)));
					}
				}
			}
			preliminaryAxioms.addAll(axioms);
			
			// add relationships to graph and use reasoner to infer relationships (remove redundant ones)
			final OWLOntology owlOntology = graph.getSourceOntology();
			final OWLOntology disposable;
			try {
				disposable = owlManager.createOntology(IRI.generateDocumentIRI());
				owlManager.addAxioms(disposable, owlOntology.getAxioms());
				for(OWLImportsDeclaration decl : owlOntology.getImportsDeclarations()) {
					owlManager.applyChange(new AddImport(disposable, decl));
				}
				//owlManager.applyChange(new AddImport(disposable, factory.getOWLImportsDeclaration(owlOntology.getOntologyID().getDefaultDocumentIRI().orNull())));
			} catch (OWLOntologyCreationException e) {
				setError("Ontology", "Could not create test setup for relation inference: "+e.getMessage());
				return;
			}
			try {
				// add axioms
				owlManager.addAxioms(disposable, preliminaryAxioms);
				OWLReasoner reasoner = reasonerFactory.createReasoner(disposable, state);
				ProcessState.addMessage(state, "Check for ontology consistency.");
				if (reasoner.isConsistent() == false) {
					setError("Ontology", "The ontology is inconsistent. No safe inferences are possible.");
					return;
				}
				ProcessState.addMessage(state, "Check for unsatisfiable classes");
				Node<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses();
				final Set<OWLClass> unsatisfiable = unsatisfiableClasses.getEntitiesMinusBottom();
				if (unsatisfiable.contains(graph.getOWLClass(iri))) {
					setError("relations", "Cannot create class, the requested class is not satisfiable.");
				}
				else if (unsatisfiable.isEmpty() == false) {
					setError("Ontology", "No safe inferences are possible. The ontology has unsatisfiable classes: "+unsatisfiable);
				}
				final InferAllRelationshipsTask task = new InferAllRelationshipsTask(disposable, graph, iri, idPrefix, state, useIsInferred);
				try {
					task.run(reasoner);
				}
				finally {
					reasoner.dispose();
				}
				InferredRelations inferredRelations = task.getInferredRelations();
				Set<OWLClass> equivalentClasses = inferredRelations.getEquivalentClasses();
				if (equivalentClasses != null && !equivalentClasses.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for (OWLClass equivalentClass : equivalentClasses) {
						sb.append(' ').append(equivalentClass.getIRI());
					}
					setError("relations", "Cannot create class, there are equivalent named classes:"+sb);
					return;
				}
				Set<OWLAxiom> classRelationAxioms = inferredRelations.getClassRelationAxioms();
				if (classRelationAxioms == null || classRelationAxioms.isEmpty()) {
					setError("relations", "Could not create relations. The resoner returned no relation axioms.");
					return;
				}
				axioms.addAll(classRelationAxioms);
				
				// OBO
				Frame frame = new Frame(FrameType.TERM);
	
				OboTools.addTermLabel(frame, requestedLabel);
				OboTools.addDefinition(frame, def, defXrefs);
				frame.addClause(new Clause(OboFormatTag.TAG_CREATION_DATE, getDate()));
				frame.addClause(new Clause(OboFormatTag.TAG_CREATED_BY, "TermGenie"));
				if (comment != null) {
					frame.addClause(new Clause(OboFormatTag.TAG_COMMENT, comment));
				}
				if (namespace != null) {
					frame.addClause(new Clause(OboFormatTag.TAG_NAMESPACE, namespace));
				}
				if (xrefs != null) {
					for (Xref xref : xrefs) {
						Clause cl = new Clause(OboFormatTag.TAG_XREF);
						String idref = xref.getIdRef();
						cl.addValue(idref);
						String annotation = xref.getAnnotation();
						if (annotation != null) {
							cl.addValue(annotation);
						}
						frame.addClause(cl);
					}
				}
				if (checkedSynonyms != null) {
					for(ISynonym synonym : checkedSynonyms) {
						OboTools.addSynonym(frame, synonym.getLabel(), synonym.getScope(), synonym.getXrefs());
					}
				}
				if (subset != null) {
					frame.addClause(new Clause(OboFormatTag.TAG_SUBSET, subset));
				}
	
				OboTools.addTermId(frame, oboNewId);
				
				for (Clause cl : inferredRelations.getClassRelations()) {
					frame.addClause(cl);
				}
				
				term = Pair.of(frame, axioms);
			}
			finally {
				ProcessState.addMessage(state, "Finished - Use reasoner to check constraints and relations.");
				owlManager.removeOntology(disposable);
			}
			return;
		}

		void addHint(boolean error, String field, String message) {
			if (error) {
				addError(field, message);
			}
			else {
				addWarning(field, message);
			}
		}
		
		void setCharacterError(String field, String msgPrefix, Set<Character> chars) {
			StringBuilder sb = new StringBuilder();
			sb.append(msgPrefix);
			if (chars.size() == 1) {
				sb.append(" contains a non-ASCII character:");
			}
			else {
				sb.append(" contains non-ASCII characters:");
			}
			for (Character character : chars) {
				sb.append(' ');
				sb.append('\'');
				sb.append(character);
				sb.append('\'');
			}
			setError(field, sb.toString());
		}
		
		void setError(String field, String messge) {
			errors = Collections.singletonList(new FreeFormHint(field, messge));
		}
		
		void addError(String field, String message) {
			if (errors == null) {
				errors = new ArrayList<FreeFormHint>();
			}
			errors.add(new FreeFormHint(field, message));
		}
		
		void addWarning(String field, String message) {
			if (warnings == null) {
				warnings = new ArrayList<FreeFormHint>();
			}
			warnings.add(new FreeFormHint(field, message));
		}
		
		void setReset() {
			modified = Modified.reset;
		}
		
		static CharSequence normalizeLabel(CharSequence cs) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cs.length(); i++) {
				char c = cs.charAt(i);
				c = Character.toLowerCase(c);
				if (Character.isLetterOrDigit(c) == false) {
					c = '*';
				}
				sb.append(c);
			}
			return sb;
		}
		
		static boolean equals(CharSequence cs1, CharSequence cs2) {
			/*
			 * Warning: StringBuilder do not overwrite hashcode and equals. 
			 * Do *not* use cs1.equals(c2).
			 * 
			 * Example:
			 * 
			 * CharSequence cs1 = new StringBuilder("test");
			 * CharSequence cs2 = new StringBuilder("test");
			 * 
			 * cs1.equals(cs2) ==> false 
			 * 
			 */
			int l1 = cs1.length();
			int l2 = cs2.length();
			if (l1 == l2) {
				for (int i = 0; i < l1; i++) {
					char c1 = cs1.charAt(i);
					char c2 = cs2.charAt(i);
					if (c1 != c2) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		static boolean similar(CharSequence cs1, CharSequence cs2) {
			int distance = StringUtils.getLevenshteinDistance(cs1, cs2);
			// TODO make this threshold relative to the string length
			return distance <= 1;
		}
		
		static boolean isOboScope(String s) {
			return (OboFormatTag.TAG_RELATED.getTag().equals(s)) ||
					(OboFormatTag.TAG_NARROW.getTag().equals(s)) ||
					(OboFormatTag.TAG_EXACT.getTag().equals(s)) ||
					(OboFormatTag.TAG_BROAD.getTag().equals(s));
		}
		
		static Set<Character> hasNonAscii(String cs) {
			
			Set<Character> set = new HashSet<Character>();
			for (int i = 0; i < cs.length(); i++) {
				char c = cs.charAt(i);
				if (CharUtils.isAsciiPrintable(c) == false) {
					set.add(Character.valueOf(c));
				}
			}
			return set;
		}
		
		private static int ID_Counter = 0;
		
		String getNewId() {
			synchronized (ValidationTask.class) {
				String id = idPrefix + ID_Counter;
				ID_Counter += 1;
				return id;
			}
			
		}
	}
	
	private final static ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>(){

		@Override
		protected DateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};

	static String getDate() {
		return df.get().format(new Date());
	}
	
}
