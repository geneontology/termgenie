package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileIRIMapperTest {

	private static File testFolder = null;
	
	private static class TestLocalFileIRIMapper extends LocalFileIRIMapper {

		@Inject
		TestLocalFileIRIMapper(File testFolder) {
			super(LocalFileIRIMapper.SETTINGS_FILE, false, testFolder);
		}

		@Override
		protected InputStream loadRemote(String url) throws MalformedURLException, IOException {
			throw new RuntimeException("Remote load in test not allowed!");
		}
	}
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		testFolder = TempTestFolderTools.createTestFolder(LocalFileIRIMapperTest.class);
		FileUtils.cleanDirectory(testFolder);
	}
	
	public static void afterClass() {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Test
	public void testMapUrl() {
		LocalFileIRIMapper mapper = new TestLocalFileIRIMapper(testFolder);
		assertEquals(0, mapper.mappings.size());
		assertEquals(16, mapper.lazyMappings.size());

		mapper.mapUrl("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology_xp.obo");
		mapper.mapUrl("http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology_ext.obo");
		mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_self.obo");
		mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_cellular_component.obo");
		mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_uber_anatomy.obo");
		mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_plant_anatomy.obo");
		mapper.mapUrl("http://www.geneontology.org/scratch/xps/cellular_component_xp_protein.obo");
		mapper.mapUrl("http://www.geneontology.org/scratch/xps/molecular_function_xp_protein.obo");
		mapper.mapUrl("ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro.obo");
		mapper.mapUrl("http://github.com/cmungall/uberon/raw/master/uberon.obo");
		mapper.mapUrl("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology.obo");
		mapper.mapUrl("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology_xp.obo");
		mapper.mapUrl("http://obo.svn.sourceforge.net/viewvc/*checkout*/obo/fma-conversion/trunk/fma2_obo.obo");
		mapper.mapUrl("http://pato.googlecode.com/svn/trunk/quality.obo");
		mapper.mapUrl("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/mammalian_phenotype.obo");
		mapper.mapUrl("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/cell_type/cell.obo");
		mapper.mapUrl("http://palea.cgrb.oregonstate.edu/viewsvn/Poc/tags/live/plant_ontology.obo?view=co");
		
		assertEquals(16, mapper.mappings.size());
	}

}
