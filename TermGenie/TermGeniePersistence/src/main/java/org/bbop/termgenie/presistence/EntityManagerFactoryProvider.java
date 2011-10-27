package org.bbop.termgenie.presistence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * <p>
 * Wrapper for hiding all the nasty details of creating an embedded db in a
 * folder for the persistence framework OpenJPA. The main configuration of the
 * ORM-Mapper is done via annotations in the classes.
 * </p>
 * <p>
 * The OpenJPA framework uses byte-code weaving to implement the monitoring of
 * mapped entities. For this modification on compile time, it is required, that
 * the mapped classes are in there own package *.entities.
 * </p>
 * <p>
 * For OpenJPA requires an additional configuration file 'persistence.xml'. This
 * file lists among other things the entity classes. <b>All</b> mapped entities
 * must be listed there, other wise the functionality is not guaranteed.
 * </p>
 */
public class EntityManagerFactoryProvider {

	public static final String HSQLDB = "hslqdb";
	public static final String H2 = "h2";
	
	public static final String MODE_DEFAULT = "default";
	public static final String MODE_IDS = "ids";

	/**
	 * Create a new factory for a given type (i.e. {@link #HSQLDB} and
	 * {@link #H2}), a folder with write access, and a database name.
	 * 
	 * @param folder
	 * @param type
	 * @param mode 
	 * @param db
	 * @return factory
	 * @throws RuntimeException throws an exception, if the type is unknown to
	 *             this Provider.
	 */
	public EntityManagerFactory createFactory(File folder, String type, String mode, String db) {
		if (HSQLDB.equals(type)) {
			return createHsqlDBFile(folder, db, mode);
		}
		else if (H2.equals(type)) {
			return createH2(folder, db, mode);
		}
		throw new RuntimeException("Unsupported database type: " + type);
	}

	protected EntityManagerFactory createHsqlDBFile(File folder, String unique, String mode) {
		Map<String, String> properties = new HashMap<String, String>();
		String unit = HSQLDB + "-" + mode;
		File dbFolder = new File(folder, unit);
		dbFolder.mkdir();
		properties.put("openjpa.ConnectionDriverName", org.hsqldb.jdbcDriver.class.getName());
		String connectionURL = "jdbc:hsqldb:file:" + dbFolder.getAbsolutePath() + "/" + unique;
		properties.put("openjpa.ConnectionURL", connectionURL);
		return Persistence.createEntityManagerFactory(unit, properties);
	}

	protected EntityManagerFactory createH2(File folder, String unique, String mode) {
		Map<String, String> properties = new HashMap<String, String>();
		String unit = H2 + "-" + mode;
		File dbFolder = new File(folder, unit);
		dbFolder.mkdir();
		properties.put("openjpa.ConnectionDriverName", org.h2.Driver.class.getName());
		String connectionURL = "jdbc:h2:" + dbFolder.getAbsolutePath() + "/" + unique;
		properties.put("openjpa.ConnectionURL", connectionURL);
		return Persistence.createEntityManagerFactory(unit, properties);
	}
}
