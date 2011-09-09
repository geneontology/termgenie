package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.management.GenericTaskManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Manage an {@link OntologyIdProvider}. This will serialize all commits and
 * allow a roll-back for unused identifiers.
 */
@Singleton
public class OntologyIdManager extends GenericTaskManager<OntologyIdProvider> {

	private final OntologyIdProvider idProvider;

	@Inject
	public OntologyIdManager(@Named("OntologyIdManagerName") String name,
			OntologyIdProvider idProvider)
	{
		super(name);
		this.idProvider = idProvider;
	}

	@Override
	protected OntologyIdProvider updateManaged(OntologyIdProvider managed) {
		// Do nothing
		return managed;
	}

	@Override
	protected OntologyIdProvider resetManaged(OntologyIdProvider managed) {
		// Do nothing
		return managed;
	}

	@Override
	protected void setChanged(boolean reset) {
		// Do nothing
	}

	@Override
	protected OntologyIdProvider createManaged() {
		return idProvider;
	}
}
