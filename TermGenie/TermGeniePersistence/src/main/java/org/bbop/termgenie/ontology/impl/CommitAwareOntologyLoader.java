package org.bbop.termgenie.ontology.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermRelation;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermSynonym;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class CommitAwareOntologyLoader extends ReloadingOntologyLoader {

	private final EntityManager entityManager;

	@Inject
	CommitAwareOntologyLoader(OntologyConfiguration configuration,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			@Named("DefaultOntologyLoaderSkipOntologies") Set<String> skipOntologies,
			@Named("ReloadingOntologyLoaderPeriod") long period,
			@Named("ReloadingOntologyLoaderTimeUnit") TimeUnit unit,
			EntityManager entityManager)
	{
		super(configuration, iriMapper, cleaner, skipOntologies, period, unit);
		this.entityManager = entityManager;
	}

	@Override
	protected void postProcessOWLOntology(String ontology, OWLOntology owlOntology) {
		// TODO Auto-generated method stub
		super.postProcessOWLOntology(ontology, owlOntology);
	}

	@Override
	protected void postProcessOBOOntology(String ontology, OBODoc obodoc) {
		super.postProcessOBOOntology(ontology, obodoc);
		TypedQuery<CommitedOntologyTerm> query = entityManager.createQuery("select t from CommitedOntologyTerm as t where t.ontology = ?1",
				CommitedOntologyTerm.class);
		query.setParameter(1, ontology);
		List<CommitedOntologyTerm> terms = query.getResultList();
		for (CommitedOntologyTerm term : terms) {
			handleTerm(term, obodoc);
		}
	}

	private boolean handleTerm(CommitedOntologyTerm term, OBODoc obodoc) {
		int operation = term.getOperation();
		if (operation >= 0) {
			Modification type = Modification.values()[operation];
			String id = term.getId();
			Frame frame = obodoc.getTermFrame(id);
			switch (type) {
				case add:
					if (frame != null) {
						LOGGER.warn("Skipping already existing term from history: "+id);
						return false;
					}
					frame = new Frame(FrameType.TERM);
					fillOBO(frame, term);
					try {
						obodoc.addFrame(frame);
					} catch (FrameMergeException exception) {
						LOGGER.error("Could not add new term to ontology: "+id, exception);
						return false;
					}
					break;
				case modify:
					if (frame == null) {
						LOGGER.warn("Skipping modification of non-existing term from history: "+id);
						return false;
					}
					try {
						Frame modFrame = new Frame(FrameType.TERM);
						fillOBO(frame, term);
						frame.merge(modFrame);
					} catch (FrameMergeException exception) {
						LOGGER.warn("Could not apply chages to frame.", exception);
						return false;
					}
					break;
					
				case remove:
					if (frame == null) {
						LOGGER.warn("Skipping removal of non-existing term from history: "+id);
						return false;
					}
					Collection<Frame> frames = obodoc.getTermFrames();
					frames.remove(frame);
					break;

				default:
					// do nothing
					break;
			}
		}
		return false;
	}

	private void fillOBO(Frame frame, CommitedOntologyTerm term) {
		String id = term.getId();
		frame.addClause(new Clause(OboFormatTag.TAG_ID.getTag(), id));
		frame.addClause(new Clause(OboFormatTag.TAG_NAME.getTag(), term.getLabel()));
		String definition = term.getDefinition();
		if (definition != null) {
			Clause cl = new Clause(OboFormatTag.TAG_DEF.getTag(), definition);
			List<String> defXRef = term.getDefXRef();
			if (defXRef != null && !defXRef.isEmpty()) {
				for (String xref : defXRef) {
					cl.addXref(new Xref(xref));
				}
			}
			frame.addClause(cl);
		}
		Map<String, String> metaData = term.getMetaData();
		if (metaData != null && !metaData.isEmpty()) {
			for (Entry<String, String> entry : metaData.entrySet()) {
				frame.addClause(new Clause(entry.getKey(), entry.getValue()));
			}
		}
		fillSynonyms(frame, term);
		fillRelations(frame, term, id);
	}

	protected void fillSynonyms(Frame frame, CommitedOntologyTerm term) {
		List<CommitedOntologyTermSynonym> synonyms = term.getSynonyms();
		if (synonyms != null && !synonyms.isEmpty()) {
			for (CommitedOntologyTermSynonym termSynonym : synonyms) {
				Clause cl = new Clause(OboFormatTag.TAG_SYNONYM.getTag(), termSynonym.getLabel());
				List<String> defXRef = term.getDefXRef();
				if (defXRef != null && !defXRef.isEmpty()) {
					for (String xref : defXRef) {
						cl.addXref(new Xref(xref));
					}
				}
				String scope = termSynonym.getScope();
				if (scope != null) {
					cl.addValue(scope);
				}
				String category = termSynonym.getCategory();
				if (category != null) {
					cl.addValue(category);
				}
				frame.addClause(cl);
			}
		}
	}

	protected void fillRelations(Frame frame, CommitedOntologyTerm term, String id) {
		List<CommitedOntologyTermRelation> relations = term.getRelations();
		OBOConverterTools.fillRelations(frame, relations, id);
	}
	
}
