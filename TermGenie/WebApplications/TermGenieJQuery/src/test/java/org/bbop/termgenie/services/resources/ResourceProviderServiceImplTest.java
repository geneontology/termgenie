package org.bbop.termgenie.services.resources;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;


public class ResourceProviderServiceImplTest {

	@Test
	public void testGetLinesFromResource() {
		ResourceProviderConfiguration config = new ResourceProviderConfiguration.ConfiguredResourceProvider("src/test/resources/resourceprovider-config.tab");
		Map<String, String> locations = config.getResourceLocations();
		assertEquals("src/test/resources/GO.curators_dbxrefs", locations.get("GO.curators_dbxrefs"));
		
		ResourceProviderServiceImpl provider = new ResourceProviderServiceImpl(config);
		String[] lines = provider.getLinesFromResource(null, "GO.curators_dbxrefs");
		assertEquals("Abbreviation\tDatabase\tName\tComment (optional)", lines[0]);
		assertEquals("GOC:fmc\tAgBase\tFiona McCarthy\t", lines[1]);
		assertEquals(201, lines.length);
	}

}
