package org.bbop.termgenie.services;

import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyIdManager;
import org.bbop.termgenie.ontology.go.GoCommitInfo;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.tools.OntologyTools;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GoTermCommitServiceImpl extends AbstractTermCommitServiceImpl {

	private final ConfiguredOntology geneOntology;
	private final String tempIdPrefix;

	@Inject
	protected GoTermCommitServiceImpl(OntologyTools ontologyTools,
			SessionHandler sessionHandler,
			Committer committer,
			@Named("ConfiguredOntologyGeneOntology") ConfiguredOntology source,
			OntologyIdManager idProvider,
			TermGenerationEngine generationEngine)
	{
		super(ontologyTools, sessionHandler, committer, idProvider);
		geneOntology = source;
		tempIdPrefix = generationEngine.getTempIdPrefix(source);
	}

	@Override
	protected Ontology getTargetOntology() {
		return geneOntology;
	}

	@Override
	protected String getTempIdPrefix() {
		return tempIdPrefix;
	}

	@Override
	protected CommitInfo createCommitInfo(List<CommitObject<OntologyTerm>> terms,
			List<CommitObject<Relation>> relations,
			String termgenieUser) {
		return new GoCommitInfo(terms, null, termgenieUser);
	}
}
