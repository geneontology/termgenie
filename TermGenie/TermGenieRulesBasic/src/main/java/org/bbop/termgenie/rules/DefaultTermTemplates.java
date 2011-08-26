package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Hard coded templates, may be written to a file as example for new rules.
 * 
 * @deprecated
 */
@Deprecated
@Singleton
public class DefaultTermTemplates {

	/**
	 * A list of all default templates.
	 */
	public final List<TermTemplate> defaultTemplates = new ArrayList<TermTemplate>();

	private final OntologyConfiguration configuration;

	public final Ontology GENE_ONTOLOGY;
	public final Ontology GENE_ONTOLOGY_BP;
	public final Ontology GENE_ONTOLOGY_MF;
	public final Ontology GENE_ONTOLOGY_CC;
	public final Ontology PROTEIN_ONTOLOGY;
	public final Ontology UBERON_ONTOLOGY;
	public final Ontology HP_ONTOLOGY;
	public final Ontology FMA_ONTOLOGY;
	public final Ontology PATO;
	public final Ontology OMP;
	public final Ontology CELL_ONTOLOGY;
	public final Ontology PLANT_ONTOLOGY;

	private static final TemplateField Field_Name = new TemplateField("Name");
	private static final TemplateField Field_Definition = new TemplateField("Definition");
	private static final TemplateField Field_DefX_Ref = new TemplateField("DefX_Ref", false, TemplateField.ONE_TO_N_CARDINALITY, null);
	private static final TemplateField Field_Comment = new TemplateField("Comment");

	public final TermTemplate all_regulation;
	public final TermTemplate all_regulation_mf;
	public final TermTemplate involved_in;
	public final TermTemplate occurs_in;
	public final TermTemplate part_of_cell_component;
	public final TermTemplate protein_binding;
	public final TermTemplate metazoan_development;
	public final TermTemplate metazoan_morphogenesis;
	public final TermTemplate plant_development;
	public final TermTemplate plant_morphogenesis;
	public final TermTemplate structural_protein_complex;
	public final TermTemplate abnormal_morphology;
	public final TermTemplate hpo_entity_quality;
	public final TermTemplate omp_entity_quality;
	public final TermTemplate metazoan_location_specific_cell;
	public final TermTemplate cell_by_surface_marker;
	public final TermTemplate metazoan_location_specific_anatomical_structure;

	@Inject
	DefaultTermTemplates(OntologyConfiguration configuration) {
		this.configuration = configuration;

		GENE_ONTOLOGY = create("GeneOntology");
		GENE_ONTOLOGY_BP = create("GeneOntology", "biological_process", "GO:0008150");
		GENE_ONTOLOGY_MF = create("GeneOntology", "molecular_function", "GO:0003674");
		GENE_ONTOLOGY_CC = create("GeneOntology", "cellular_component", "GO:0005575");
		PROTEIN_ONTOLOGY = create("ProteinOntology");
		UBERON_ONTOLOGY = create("Uberon");
		HP_ONTOLOGY = create("HumanPhenotype");
		FMA_ONTOLOGY = create("FMA");
		PATO = create("PATO");
		OMP = create("OMP");
		CELL_ONTOLOGY = create("CL");
		PLANT_ONTOLOGY = create("PO");

		TemplateField Field_Target_Regulation_BP = new TemplateField("target", true, TemplateField.SINGLE_FIELD_CARDINALITY, Arrays.asList("regulation",
				"negative_regulation",
				"positive_regulation"), GENE_ONTOLOGY_BP);
		TemplateField Field_Target_Regulation_MF = new TemplateField("target", true, TemplateField.SINGLE_FIELD_CARDINALITY, Arrays.asList("regulation",
				"negative_regulation",
				"positive_regulation"), GENE_ONTOLOGY_MF);
		TemplateField Field_Part_BP = new TemplateField("part", GENE_ONTOLOGY_BP);
		TemplateField Field_Whole_BP = new TemplateField("whole", GENE_ONTOLOGY_BP);

		all_regulation = create(GENE_ONTOLOGY,
				"all_regulation",
				"regulation: biological_process",
				"Select all three subtemplates to generate terms for regulation, negative regulations and positive regulation (for biological processes). Names, synonyms and definitions are all generated automatically",
				null,
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				Field_Target_Regulation_BP,
				Field_DefX_Ref);

		all_regulation_mf = create(GENE_ONTOLOGY,
				"all_regulation_mf",
				"regulation: molecular_function",
				"Select all three subtemplates to generate terms for regulation, negative regulations and positive regulation (for molecular functions). Names, synonyms and definitions are all generated automatically",
				null,
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				Field_Target_Regulation_MF,
				Field_DefX_Ref);

		involved_in = create(GENE_ONTOLOGY,
				"involved_in",
				"processes involved in other processes",
				"[part] involved in [whole]",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(Field_Part_BP, Field_Whole_BP));

		occurs_in = create(GENE_ONTOLOGY,
				"occurs_in",
				"processes occurring in parts of the cell",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("process", GENE_ONTOLOGY_BP),
						new TemplateField("location", GENE_ONTOLOGY_CC)));

		part_of_cell_component = create(GENE_ONTOLOGY,
				"part_of_cell_component",
				"cell components part of other cell components",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("part", GENE_ONTOLOGY_CC),
						new TemplateField("whole", GENE_ONTOLOGY_CC)));

		protein_binding = create(GENE_ONTOLOGY,
				"protein_binding",
				"binding to a protein",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("target", PROTEIN_ONTOLOGY)));

		metazoan_development = create(GENE_ONTOLOGY,
				"metazoan_development",
				"development of an animal anatomical structure",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("target", UBERON_ONTOLOGY)));

		metazoan_morphogenesis = create(GENE_ONTOLOGY,
				"metazoan_morphogenesis",
				"morphogenesis of an animal anatomical structure",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("target", UBERON_ONTOLOGY)));

		plant_development = create(GENE_ONTOLOGY,
				"plant_development",
				"development of a plant anatomical structure",
				"Dummy Rule",
				requiredPlusOptionalFields(new TemplateField("target", PLANT_ONTOLOGY)));

		plant_morphogenesis = create(GENE_ONTOLOGY,
				"plant_morphogenesis",
				"morphogenesis of a plant animal anatomical structure",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("target", PLANT_ONTOLOGY)));

		structural_protein_complex = create(GENE_ONTOLOGY,
				"structural_protein_complex",
				"protein complex defined structurally",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("unit", true, TemplateField.TWO_TO_N_CARDINALITY, null, PROTEIN_ONTOLOGY)));

		abnormal_morphology = create(HP_ONTOLOGY,
				"abnormal_morphology",
				"Abnormal X morphology",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("target", FMA_ONTOLOGY)));

		hpo_entity_quality = create(HP_ONTOLOGY,
				"hpo_entity_quality",
				"basic EQ template",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("entity", FMA_ONTOLOGY),
						new TemplateField("quality", PATO)));

		omp_entity_quality = create(OMP,
				"omp_entity_quality",
				"basic EQ template",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("entity", GENE_ONTOLOGY),
						new TemplateField("quality", PATO)));

		metazoan_location_specific_cell = create(CELL_ONTOLOGY,
				"metazoan_location_specific_cell",
				"A cell type differentiated by its anatomical location (animals)",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("cell", CELL_ONTOLOGY),
						new TemplateField("location", UBERON_ONTOLOGY)));

		cell_by_surface_marker = create(CELL_ONTOLOGY,
				"cell_by_surface_marker",
				"A cell type differentiated by proteins or complexes on the plasma membrane",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("cell", CELL_ONTOLOGY),
						new TemplateField("membrane_part", true, TemplateField.ONE_TO_N_CARDINALITY, Arrays.asList(PROTEIN_ONTOLOGY,
								GENE_ONTOLOGY_CC))));

		metazoan_location_specific_anatomical_structure = create(UBERON_ONTOLOGY,
				"metazoan_location_specific_anatomical_structure",
				"location-specific anatomical structure",
				"Dummy Rule\n Dummy Name Rule\n2nd line",
				requiredPlusOptionalFields(new TemplateField("part", UBERON_ONTOLOGY),
						new TemplateField("whole", UBERON_ONTOLOGY)));
	}

	private Ontology create(String name) {
		return create(name, null, null);
	}

	private Ontology create(String name, String branch, String branchId) {
		Map<String, ConfiguredOntology> ontologies = configuration.getOntologyConfigurations();
		Ontology ontology;
		if (branch != null) {
			ontology = ontologies.get(branch);
		}
		else {
			ontology = ontologies.get(name);
		}
		if (ontology == null) {
			throw new RuntimeException("Unkown ontology: " + name + " " + branch);
		}
		return ontology;
	}

	private TermTemplate create(Ontology correspondingOntology,
			String name,
			String description,
			String rules,
			TemplateField...fields)
	{
		return create(correspondingOntology, name, description, null, rules, fields);
	}

	private TermTemplate create(Ontology correspondingOntology,
			String name,
			String description,
			String hint,
			String rules,
			TemplateField...fields)
	{
		return create(correspondingOntology, name, null, description, hint, rules, fields);
	}

	private TermTemplate create(Ontology correspondingOntology,
			String name,
			String displayName,
			String description,
			String hint,
			String rules,
			TemplateField...fields)
	{
		TermTemplate termTemplate = new TermTemplate(correspondingOntology, name, displayName, description, Arrays.asList(fields), null, null, null, rules, hint);
		defaultTemplates.add(termTemplate);
		return termTemplate;
	}

	private TemplateField[] requiredPlusOptionalFields(TemplateField...required) {
		List<TemplateField> result = new ArrayList<TemplateField>(required.length + 4);
		for (TemplateField c : required) {
			result.add(c);
		}
		result.add(Field_Name);
		result.add(Field_Definition);
		result.add(Field_DefX_Ref);
		result.add(Field_Comment);
		return result.toArray(new TemplateField[result.size()]);

	}

}
