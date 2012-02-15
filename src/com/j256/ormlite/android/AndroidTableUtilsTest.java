package com.j256.ormlite.android;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public class AndroidTableUtilsTest extends BaseDaoTest {

	/*
	 * ==============================================================================================================
	 */

	public void testConstructor() throws Exception {
		@SuppressWarnings("rawtypes")
		Constructor[] constructors = TableUtils.class.getDeclaredConstructors();
		assertEquals(1, constructors.length);
		constructors[0].setAccessible(true);
		constructors[0].newInstance();
	}

	public void testMissingCreate() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		try {
			fooDao.queryForAll();
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testCreateTable() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		// first we create the table
		createTable(LocalFoo.class, false);
		// test it out
		assertEquals(0, fooDao.queryForAll().size());
		// now we drop it
		dropTable(LocalFoo.class, true);
		try {
			fooDao.countOf();
			fail("Was expecting a SQL exception");
		} catch (Exception expected) {
			// expected
		}
		// now create it again
		createTable(LocalFoo.class, false);
		assertEquals(0, fooDao.queryForAll().size());
		dropTable(LocalFoo.class, true);
	}

	public void testDropThenQuery() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, true);
		assertEquals(0, fooDao.queryForAll().size());
		dropTable(LocalFoo.class, true);
		try {
			fooDao.queryForAll();
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testRawExecuteDropThenQuery() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, true);
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE ");
		if (databaseType.isEntityNamesMustBeUpCase()) {
			databaseType.appendEscapedEntityName(sb, "LOCALFOO");
		} else {
			databaseType.appendEscapedEntityName(sb, "LocalFoo");
		}
		// can't check the return value because of sql-server
		fooDao.executeRaw(sb.toString());
		try {
			fooDao.queryForAll();
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testDoubleDrop() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		// first we create the table
		createTable(LocalFoo.class, false);
		// test it out
		assertEquals(0, fooDao.queryForAll().size());
		// now we drop it
		dropTable(LocalFoo.class, true);
		try {
			// this should fail
			dropTable(LocalFoo.class, false);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testClearTable() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, true);
		assertEquals(0, fooDao.countOf());
		LocalFoo foo = new LocalFoo();
		assertEquals(1, fooDao.create(foo));
		assertEquals(1, fooDao.countOf());
		TableUtils.clearTable(connectionSource, LocalFoo.class);
		assertEquals(0, fooDao.countOf());
	}

	public void testCreateTableIfNotExists() throws Exception {
		dropTable(LocalFoo.class, true);
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		try {
			fooDao.countOf();
			fail("Should have thrown an exception");
		} catch (Exception e) {
			// ignored
		}
		TableUtils.createTableIfNotExists(connectionSource, LocalFoo.class);
		assertEquals(0, fooDao.countOf());
		// should not throw
		TableUtils.createTableIfNotExists(connectionSource, LocalFoo.class);
		assertEquals(0, fooDao.countOf());
	}

	public void testCreateTableConfigIfNotExists() throws Exception {
		dropTable(LocalFoo.class, true);
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		try {
			fooDao.countOf();
			fail("Should have thrown an exception");
		} catch (Exception e) {
			// ignored
		}
		DatabaseTableConfig<LocalFoo> tableConfig = DatabaseTableConfig.fromClass(connectionSource, LocalFoo.class);
		TableUtils.createTableIfNotExists(connectionSource, tableConfig);
		assertEquals(0, fooDao.countOf());
		// should not throw
		TableUtils.createTableIfNotExists(connectionSource, tableConfig);
		assertEquals(0, fooDao.countOf());
	}

	/* ================================================================ */

	protected static class LocalFoo {
		public static final String ID_FIELD_NAME = "id";
		public static final String NAME_FIELD_NAME = "name";
		@DatabaseField(columnName = ID_FIELD_NAME)
		int id;
		@DatabaseField(columnName = NAME_FIELD_NAME)
		String name;
	}

	protected static class Index {
		@DatabaseField(index = true)
		String stuff;
		public Index() {
		}
	}

	protected static class ComboIndex {
		@DatabaseField(indexName = INDEX_NAME)
		String stuff;
		@DatabaseField(indexName = INDEX_NAME)
		long junk;
		public ComboIndex() {
		}
		public static final String INDEX_NAME = "stuffjunk";
	}
}
