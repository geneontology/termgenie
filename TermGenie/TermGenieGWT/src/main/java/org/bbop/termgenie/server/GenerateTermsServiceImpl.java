package org.bbop.termgenie.server;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GenerateTermsServiceImpl extends RemoteServiceServlet implements GenerateTermsService {

	@Override
	public boolean checkTerms() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean generateTerms() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GWTTermTemplate[] getAvailableGWTTermTemplates(String ontology) {
		List<GWTTermTemplate> gwtTemplates = new ArrayList<GWTTermTemplate>();
		for(TermTemplate template : DefaultTermTemplates.defaultTemplates) {
			gwtTemplates.add(GWTTemplateTools.createGWTermTemplate(template));
		}
		return gwtTemplates.toArray(new GWTTermTemplate[gwtTemplates.size()]);
	}

	
	static class GWTTemplateTools {
		
		static GWTTermTemplate createGWTermTemplate(TermTemplate template) {
			GWTTermTemplate gwtTermTemplate = new GWTTermTemplate();
			gwtTermTemplate.setName(template.getName());
			List<TemplateField> fields = template.getFields();
			int size = fields.size();
			GWTTemplateField[] gwtFields = new GWTTemplateField[size];
			for (int i = 0; i < size; i++) {
				gwtFields[i] = createGWTTemplateField(fields.get(i));
			}
			gwtTermTemplate.setFields(gwtFields);
			return gwtTermTemplate;
		}
		
		static GWTTemplateField createGWTTemplateField(TemplateField field) {
			GWTTemplateField gwtField = new GWTTemplateField();
			gwtField.setName(field.getName());
			gwtField.setRequired(field.isRequired());
			Cardinality c = field.getCardinality();
			gwtField.setCardinality(new GWTCardinality(c.getMinimum(), c.getMaximum()));
			gwtField.setFunctionalPrefixes(field.getFunctionalPrefixes().toArray(new String[0]));
			Ontology ontology = field.getCorrespondingOntology();
			if (ontology != null) {
				gwtField.setOntology(ontology.getUniqueName());
			}
			return gwtField;
		}
	}
}
