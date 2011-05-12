package org.bbop.termgenie.core.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.io.FlatFileTermTemplateIO;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateRule;
import org.bbop.termgenie.core.TermTemplate;

import owltools.graph.OWLGraphWrapper;

/**
 * Hard coded templates, may be written to a file as example for new rules.
 */
public class DefaultTermTemplates {

	protected final static Ontology GENE_ONTOLOGY = new Ontology() {
		
		@Override
		public String getUniqueName() {
			return "GeneOntology";
		}
		
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}

		@Override
		public String getBranch() {
			return null;
		}
	};
	
	protected final static Ontology GENE_ONTOLOGY_BP = new Ontology() {
		
		@Override
		public String getUniqueName() {
			return "GeneOntology";
		}
		
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}

		@Override
		public String getBranch() {
			return "biological_process";
		}
	};
	
	protected final static Ontology GENE_ONTOLOGY_MF = new Ontology() {
		
		@Override
		public String getUniqueName() {
			return "GeneOntology";
		}
		
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}

		@Override
		public String getBranch() {
			return "molecular_function";
		}
	};
	
	protected final static Ontology GENE_ONTOLOGY_CC = new Ontology() {
		
		@Override
		public String getUniqueName() {
			return "GeneOntology";
		}
		
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}

		@Override
		public String getBranch() {
			return "cellular_component";
		}
	};
	
	protected final static Ontology PROTEIN_ONTOLOGY = new Ontology() {
		
		@Override
		public String getUniqueName() {
			return "ProteinOntology";
		}
		
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}

		@Override
		public String getBranch() {
			return null;
		}
	};
	
	protected final static Ontology UBERON_ONTOLOGY_METAZOAN = new Ontology() {
		
		@Override
		public String getUniqueName() {
			return "Uberon";
		}
		
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}

		@Override
		public String getBranch() {
			return "Metazoan";
		}
	};
	
	protected final static Ontology UBERON_ONTOLOGY_PLANT = new Ontology() {
		
		@Override
		public String getUniqueName() {
			return "Uberon";
		}
		
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}

		@Override
		public String getBranch() {
			return "Plant";
		}
	};
	
	protected final static TermTemplate all_regulation = create(GENE_ONTOLOGY, "all_regulation",
			createRules("Dummy Rule\n     2nd line","Dummy Name Rule\n2nd line"),
			new TemplateField("target", true, TemplateField.SINGLE_FIELD_CARDINALITY, Arrays.asList("regulation","negative_regulation","positive_regulation"), GENE_ONTOLOGY_BP),
			new TemplateField("DefX_Ref", false));
	
	protected final static TermTemplate all_regulation_mf = create(GENE_ONTOLOGY, "all_regulation_mf", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("target", true, TemplateField.SINGLE_FIELD_CARDINALITY, Arrays.asList("regulation","negative_regulation","positive_regulation"), GENE_ONTOLOGY_MF),
			new TemplateField("DefX_Ref", false));
	
	protected final static TermTemplate involved_in = create(GENE_ONTOLOGY, "involved_in", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("part", true, GENE_ONTOLOGY_BP),
			new TemplateField("whole", true, GENE_ONTOLOGY_BP),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	protected final static TermTemplate takes_place_in = create(GENE_ONTOLOGY, "takes_place_in", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("process", true, GENE_ONTOLOGY_BP),
			new TemplateField("location", true, GENE_ONTOLOGY_CC),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	protected final static TermTemplate part_of_cell_component = create(GENE_ONTOLOGY, "part_of_cell_component", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("part", true, GENE_ONTOLOGY_CC),
			new TemplateField("whole", true, GENE_ONTOLOGY_CC),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	
	protected final static TermTemplate protein_binding = create(GENE_ONTOLOGY, "protein_binding", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("target", true, PROTEIN_ONTOLOGY),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	protected final static TermTemplate metazoan_development = create(GENE_ONTOLOGY, "metazoan_development", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("target", true, UBERON_ONTOLOGY_METAZOAN),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	protected final static TermTemplate metazoan_morphogenesis = create(GENE_ONTOLOGY, "metazoan_morphogenesis", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("target", true, UBERON_ONTOLOGY_METAZOAN),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	protected final static TermTemplate plant_development = create(GENE_ONTOLOGY, "plant_development", 
			"Dummy Rule",
			new TemplateField("target", true, UBERON_ONTOLOGY_PLANT),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	protected final static TermTemplate plant_morphogenesis = create(GENE_ONTOLOGY, "plant_morphogenesis", 
			createRules("Dummy Rule","Dummy Name Rule\n2nd line"),
			new TemplateField("target", true, UBERON_ONTOLOGY_PLANT),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	protected final static TermTemplate structural_protein_complex = create(GENE_ONTOLOGY, "structural_protein_complex",
			createRules("Dummy Rule\n2nd line","Dummy Name Rule\n2nd line"),
			new TemplateField("protein units", true, TemplateField.TWO_TO_N_CARDINALITY, null, PROTEIN_ONTOLOGY),
			new TemplateField("Name", false),
			new TemplateField("Definition", false),
			new TemplateField("DefX_Ref", false),
			new TemplateField("Comment", false));
	
	/**
	 * A list of all default templates.
	 */
	public final static List<TermTemplate> defaultTemplates = Arrays.asList(all_regulation,
			all_regulation_mf,involved_in,takes_place_in,part_of_cell_component,
			protein_binding,metazoan_development,metazoan_morphogenesis,plant_development,
			plant_morphogenesis,structural_protein_complex);
	
	/**
	 *  A list of all default ontologies.
	 */
	public final static List<Ontology> defaultOntologies = Arrays.asList(GENE_ONTOLOGY, 
			GENE_ONTOLOGY_BP, GENE_ONTOLOGY_CC, GENE_ONTOLOGY_MF, PROTEIN_ONTOLOGY,
			UBERON_ONTOLOGY_METAZOAN, UBERON_ONTOLOGY_PLANT);
	
	private static TermTemplate create(Ontology correspondingOntology, String name, String rule,
			TemplateField...fields) {
		return create(correspondingOntology, name, createRules(rule, null), fields);
	}
	
	private static TermTemplate create(Ontology correspondingOntology, String name, List<TemplateRule> rules,
			TemplateField...fields) {
		return new TermTemplate(correspondingOntology, name, Arrays.asList(fields), rules);
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
	
	public static void main(String[] args) {
		File outputFile = new File("src/main/resources/termgenie_rules.txt");
		FlatFileTermTemplateIO writer = new FlatFileTermTemplateIO();
		writer.writeTemplates(defaultTemplates, outputFile);
	}
}
