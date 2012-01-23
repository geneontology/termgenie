package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileIRIMapperTest {

	private static File testFolder = null;
	
	static class TestLocalFileIRIMapper extends LocalFileIRIMapper {

		private final List<String> converted;

		@Inject
		TestLocalFileIRIMapper(File testFolder, List<String> converted) {
			super(LocalFileIRIMapper.SETTINGS_FILE, false, testFolder);
			this.converted = converted;
		}

		@Override
		protected InputStream loadRemote(String url) throws MalformedURLException, IOException {
			throw new RuntimeException("Remote load in test not allowed! Trying to load: "+url);
		}

		@Override
		protected void convertFromObo2Owl(File tempFile, InputStream inputStream)
				throws IOException
		{
			super.convertFromObo2Owl(tempFile, inputStream);
			converted.add(tempFile.getName());
		}
		
		
	}
	
	@BeforeClass
	public static void beforeClass() {
		testFolder = TempTestFolderTools.createTestFolder(LocalFileIRIMapperTest.class);
	}
	
	@AfterClass
	public static void afterClass() {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Test
	public void testMapUrl() {
		List<String> converted = new ArrayList<String>();
		LocalFileIRIMapper mapper = new TestLocalFileIRIMapper(testFolder, converted);
		assertEquals(0, mapper.mappings.size());
		assertEquals(19, mapper.lazyMappings.size());

		assertNotNull(mapper.mapUrl("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology_xp.obo"));
		assertNotNull(mapper.mapUrl("http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology_ext.obo"));
		
		assertNotNull(mapper.mapUrl("http://purl.obolibrary.org/obo/go/extensions/x-disjoint-importer.owl"));
		assertNotNull(mapper.mapUrl("http://purl.obolibrary.org/obo/go.owl"));
		assertNotNull(mapper.mapUrl("http://purl.obolibrary.org/obo/go/extensions/x-disjoint.owl"));
		
		assertNotNull(mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_self.obo"));
		assertNotNull(mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_cellular_component.obo"));
		assertNotNull(mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_uber_anatomy.obo"));
		assertNotNull(mapper.mapUrl("http://www.geneontology.org/scratch/xps/biological_process_xp_plant_anatomy.obo"));
		assertNotNull(mapper.mapUrl("http://www.geneontology.org/scratch/xps/cellular_component_xp_protein.obo"));
		assertNotNull(mapper.mapUrl("http://www.geneontology.org/scratch/xps/molecular_function_xp_protein.obo"));
		assertNotNull(mapper.mapUrl("ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro.obo"));
		assertNotNull(mapper.mapUrl("http://github.com/cmungall/uberon/raw/master/uberon.obo"));
		assertNotNull(mapper.mapUrl("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology.obo"));
		assertNotNull(mapper.mapUrl("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology_xp.obo"));
		assertNotNull(mapper.mapUrl("http://obo.svn.sourceforge.net/viewvc/*checkout*/obo/fma-conversion/trunk/fma2_obo.obo"));
		assertNotNull(mapper.mapUrl("http://pato.googlecode.com/svn/trunk/quality.obo"));
		assertNotNull(mapper.mapUrl("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/mammalian_phenotype.obo"));
		assertNotNull(mapper.mapUrl("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/cell_type/cell.obo"));
		assertNotNull(mapper.mapUrl("http://palea.cgrb.oregonstate.edu/viewsvn/Poc/tags/live/plant_ontology.obo?view=co"));
		
		assertEquals(19, mapper.mappings.size());
		assertEquals(1, converted.size());
	}

}
