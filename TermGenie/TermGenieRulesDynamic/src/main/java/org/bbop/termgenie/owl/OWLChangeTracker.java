package org.bbop.termgenie.owl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyRenameException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;


public class OWLChangeTracker {
	
	private static final Logger logger = Logger.getLogger(OWLChangeTracker.class);
	
	protected List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
	private final OWLOntology owlOntology;
	private final OWLOntologyManager manager;

	public OWLChangeTracker(OWLOntology owlOntology) {
		this.owlOntology = owlOntology;
		this.manager = owlOntology.getOWLOntologyManager();
	}

	public synchronized void apply(OWLOntologyChange change) {
		ChangeApplied applied = manager.applyChange(change);
		if (applied == ChangeApplied.SUCCESSFULLY) {
			this.changes.addAll(changes);
		}
	}

	/**
	 * @return true if all changes have been reverted.
	 */
	public synchronized boolean undoChanges() {
		boolean success = true;
		if (!changes.isEmpty()) {
			for (int i = changes.size() - 1; i >= 0 && success ; i--) {
				OWLOntologyChange change = changes.get(i);
				if (change instanceof AddAxiom) {
					AddAxiom addAxiom = (AddAxiom) change;
					success = applyChange(new RemoveAxiom(owlOntology, addAxiom.getAxiom()));
				}
				else if (change instanceof RemoveAxiom) {
					RemoveAxiom removeAxiom = (RemoveAxiom) change;
					success = applyChange(new AddAxiom(owlOntology, removeAxiom.getAxiom()));
				}
				else if (change instanceof AddOntologyAnnotation) {
					AddOntologyAnnotation addOntologyAnnotation = (AddOntologyAnnotation) change;
					success = applyChange(new RemoveOntologyAnnotation(owlOntology, addOntologyAnnotation.getAnnotation()));
				}
				else if (change instanceof RemoveOntologyAnnotation) {
					RemoveOntologyAnnotation removeOntologyAnnotation = (RemoveOntologyAnnotation) change;
					success = applyChange(new AddOntologyAnnotation(owlOntology, removeOntologyAnnotation.getAnnotation()));
				}
				else {
					success = false;
				}
			}
			if (success) {
				changes.clear();
			}
		}
		return success;
	}
	
	private boolean applyChange(OWLOntologyChange change) {
		try {
			manager.applyChange(change);
			return true;
		} catch (OWLOntologyRenameException exception) {
			logger.warn("Can not apply change", exception);
			return false;
		}
	}
	
	public OWLOntology getTarget() {
		return owlOntology;
	}
}
