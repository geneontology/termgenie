package org.bbop.termgenie.core.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateRule;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.io.FlatFileTermTemplateIO;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;

/**
 * Hard coded templates, may be written to a file as example for new rules.
 */
public class DefaultTermTemplates {

	/**
	 * A list of all default templates.
	 */
	protected final static List<TermTemplate> defaultTemplates = new ArrayList<TermTemplate>();
	
	public final static Ontology GENE_ONTOLOGY = create("GeneOntology");
	public final static Ontology GENE_ONTOLOGY_BP = create("GeneOntology", "biological_process", "GO:0008150");
	public final static Ontology GENE_ONTOLOGY_MF = create("GeneOntology", "molecular_function", "GO:0003674");
	public final static Ontology GENE_ONTOLOGY_CC = create("GeneOntology", "cellular_component", "GO:0005575");
	public final static Ontology PROTEIN_ONTOLOGY = create("ProteinOntology");
	public final static Ontology UBERON_ONTOLOGY = create("Uberon");
	public final static Ontology HP_ONTOLOGY = create("HumanPhenotype");
	public final static Ontology FMA_ONTOLOGY = create("FMA");
	public final static Ontology PATO = create("PATO");
	public final static Ontology OMP = create("OMP");
	public final static Ontology CELL_ONTOLOGY = create("CL");
	public final static Ontology PLANT_ONTOLOGY = create("PO");

	private static final TemplateField Field_Name = new TemplateField("Name");
	private static final TemplateField Field_Definition = new TemplateField("Definition");
	private static final TemplateField Field_DefX_Ref = new TemplateField("DefX_Ref",false, TemplateField.ONE_TO_N_CARDINALITY, null);
	private static final TemplateField Field_Comment = new TemplateField("Comment");
	
	private static final TemplateField Field_Target_Regulation_BP = new TemplateField("target", true, TemplateField.SINGLE_FIELD_CARDINALITY, Arrays.asList("regulation","negative_regulation","positive_regulation"), GENE_ONTOLOGY_BP);
	private static final TemplateField Field_Target_Regulation_MF = new TemplateField("target", true, TemplateField.SINGLE_FIELD_CARDINALITY, Arrays.asList("regulation","negative_regulation","positive_regulation"), GENE_ONTOLOGY_MF);
	private static final TemplateField Field_Part_BP = new TemplateField("part", GENE_ONTOLOGY_BP);
	private static final TemplateField Field_Whole_BP = new TemplateField("whole", GENE_ONTOLOGY_BP);
	
	public final static TermTemplate all_regulation = create(GENE_ONTOLOGY, "all_regulation", "regulation: biological_process",
			"Select all three subtemplates to generate terms for regulation, negative regulations and positive regulation (for biological processes). Names, synonyms and definitions are all generated automatically",
			null,
			createRules("Dummy Rule\n     2nd line","Dummy Name Rule\n2nd line"),
			Field_Target_Regulation_BP,
			Field_DefX_Ref);

	public final static TermTemplate all_regulation_mf = create(GENE_ONTOLOGY, "all_regulation_mf", "regulation: molecular_function",
			"Select all three subtemplates to generate terms for regulation, negative regulations and positive regulation (for molecular functions). Names, synonyms and definitions are all generated automatically",
			null,
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			Field_Target_Regulation_MF,
			Field_DefX_Ref);
	
	public final static TermTemplate involved_in = create(GENE_ONTOLOGY, "involved_in",
			"processes involved in other processes",
			"[part] involved in [whole]",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(Field_Part_BP, Field_Whole_BP));
	
	public final static TermTemplate occurs_in = create(GENE_ONTOLOGY, "occurs_in",
			"processes occurring in parts of the cell",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(
					new TemplateField("process", GENE_ONTOLOGY_BP),
					new TemplateField("location", GENE_ONTOLOGY_CC)));
	
	public final static TermTemplate part_of_cell_component = create(GENE_ONTOLOGY, "part_of_cell_component",
			"cell components part of other cell components",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(
					new TemplateField("part", GENE_ONTOLOGY_CC),
					new TemplateField("whole", GENE_ONTOLOGY_CC)));
	
	public final static TermTemplate protein_binding = create(GENE_ONTOLOGY, "protein_binding",
			"binding to a protein",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(new TemplateField("target", PROTEIN_ONTOLOGY)));
	
	public final static TermTemplate metazoan_development = create(GENE_ONTOLOGY, "metazoan_development",
			"development of an animal anatomical structure",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(new TemplateField("target", UBERON_ONTOLOGY)));
	
	public final static TermTemplate metazoan_morphogenesis = create(GENE_ONTOLOGY, "metazoan_morphogenesis",
			"morphogenesis of an animal anatomical structure",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(new TemplateField("target", UBERON_ONTOLOGY)));
	
	public final static TermTemplate plant_development = create(GENE_ONTOLOGY, "plant_development",
			"development of a plant anatomical structure",
			"Dummy Rule",
			requiredPlusOptionalFields(new TemplateField("target", PLANT_ONTOLOGY)));
	
	public final static TermTemplate plant_morphogenesis = create(GENE_ONTOLOGY, "plant_morphogenesis",
			"morphogenesis of a plant animal anatomical structure",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(new TemplateField("target", PLANT_ONTOLOGY)));
	
	public final static TermTemplate structural_protein_complex = create(GENE_ONTOLOGY, "structural_protein_complex",
			"protein complex defined structurally",
			createRules("Dummy Rule\n2nd line","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(new TemplateField("unit", true, TemplateField.TWO_TO_N_CARDINALITY, null, PROTEIN_ONTOLOGY)));

	public final static TermTemplate abnormal_morphology = create(HP_ONTOLOGY, "abnormal_morphology",
			"Abnormal X morphology",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(new TemplateField("target", FMA_ONTOLOGY)));
	
	public final static TermTemplate hpo_entity_quality = create(HP_ONTOLOGY, "hpo_entity_quality",
			"basic EQ template",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(
					new TemplateField("entity", FMA_ONTOLOGY),
					new TemplateField("quality", PATO)));
	
	public final static TermTemplate omp_entity_quality = create(OMP, "omp_entity_quality",
			"basic EQ template",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(
					new TemplateField("entity", GENE_ONTOLOGY),
					new TemplateField("quality", PATO)));
	
	public final static TermTemplate metazoan_location_specific_cell = create(CELL_ONTOLOGY, "metazoan_location_specific_cell",
			"A cell type differentiated by its anatomical location (animals)",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(
					new TemplateField("cell", CELL_ONTOLOGY),
					new TemplateField("location", UBERON_ONTOLOGY)));
	
	public final static TermTemplate cell_by_surface_marker = create(CELL_ONTOLOGY, "cell_by_surface_marker",
			"A cell type differentiated by proteins or complexes on the plasma membrane",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(
			new TemplateField("cell", CELL_ONTOLOGY),
			new TemplateField("membrane_part", true, TemplateField.ONE_TO_N_CARDINALITY, Arrays.asList(PROTEIN_ONTOLOGY, GENE_ONTOLOGY_CC))));
	
	public final static TermTemplate metazoan_location_specific_anatomical_structure = create(UBERON_ONTOLOGY, "metazoan_location_specific_anatomical_structure",
			"location-specific anatomical structure",
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			requiredPlusOptionalFields(
					new TemplateField("part", UBERON_ONTOLOGY),
					new TemplateField("whole", UBERON_ONTOLOGY)));
	
	private static Ontology create(String name) {
		return create(name, null, null);
	}
	
	private static Ontology create(String name, String branch, String branchId) {
		Map<String, ConfiguredOntology> ontologies = DefaultOntologyConfiguration.getOntologies();
		Ontology ontology;
		if (branch != null) {
			ontology = ontologies.get(branch);
		}
		else {
			ontology = ontologies.get(name);
		}
		if (ontology == null) {
			throw new RuntimeException("Unkown ontology: "+name+" "+branch);
		}
		return ontology;
	}
	
	private static TermTemplate create(Ontology correspondingOntology, String name, String description, String rule,
			TemplateField...fields) {
		return create(correspondingOntology, name, description, createRules(rule, null), fields);
	}
	
	private static TermTemplate create(Ontology correspondingOntology, String name, String description, List<TemplateRule> rules,
			TemplateField...fields) {
		return create(correspondingOntology, name, description, null, rules, fields);
	}
	
	private static TermTemplate create(Ontology correspondingOntology, String name, String description, String hint, List<TemplateRule> rules,
			TemplateField...fields) {
		return create(correspondingOntology, name, null, description, hint, rules, fields);
	}
	
	private static TermTemplate create(Ontology correspondingOntology, String name, String displayName, String description, String hint, List<TemplateRule> rules,
			TemplateField...fields) {
		TermTemplate termTemplate = new TermTemplate(correspondingOntology, name, displayName, description, Arrays.asList(fields), rules, hint);
		defaultTemplates.add(termTemplate);
		return termTemplate;
	}
	
	private static List<TemplateRule> createRules(String termRule, String nameRule) {
		List<TemplateRule> rules = new ArrayList<TemplateRule>(2);
		if (termRule != null) {
			rules.add(new TemplateRule("TermRule", termRule));
		}
		if (nameRule != null) {
			rules.add(new TemplateRule("NameRule", nameRule));
		}
		return rules;
	}
	
	private static TemplateField[] requiredPlusOptionalFields(TemplateField...required) {
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
	
	public static void main(String[] args) {
		File outputFile = new File("src/main/resources/termgenie_rules.txt");
		FlatFileTermTemplateIO writer = new FlatFileTermTemplateIO();
		writer.writeTemplates(defaultTemplates, outputFile);
	}
}



