package com.j256.ormlite.android;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public class AndroidTableUtilsTest extends AndroidTestCase {

	private ConnectionSource connectionSource;
	private DatabaseHelper helper;
	private DatabaseType databaseType = new SqliteAndroidDatabaseType();

	private Set<DatabaseTableConfig<?>> dropClassSet = new HashSet<DatabaseTableConfig<?>>();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		helper = new DatabaseHelper(getContext());
		connectionSource = helper.getConnectionSource();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (helper != null) {
			helper.close();
			helper = null;
		}
		if (connectionSource != null) {
			connectionSource.close();
			connectionSource = null;
		}
	}

	private <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		return createDao(DatabaseTableConfig.fromClass(connectionSource, clazz), createTable);
	}

	private <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable) throws Exception {
		BaseDaoImpl<T, ID> dao = new BaseDaoImpl<T, ID>(connectionSource, tableConfig) {
		};
		return configDao(tableConfig, createTable, dao);
	}

	private <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(tableConfig, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, tableConfig);
		if (dropAtEnd) {
			dropClassSet.add(tableConfig);
		}
	}

	private <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
	}

	private <T, ID> Dao<T, ID> configDao(DatabaseTableConfig<T> tableConfig, boolean createTable, BaseDaoImpl<T, ID> dao)
			throws Exception {
		if (connectionSource == null) {
			throw new SQLException("no connection source configured");
		}
		dao.setConnectionSource(connectionSource);
		if (createTable) {
			createTable(tableConfig, true);
		}
		dao.initialize();
		return dao;
	}

	private <T> void createTable(Class<T> clazz, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(clazz, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, clazz);
		if (dropAtEnd) {
			dropClassSet.add(DatabaseTableConfig.fromClass(connectionSource, clazz));
		}
	}

	private <T> void dropTable(Class<T> clazz, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, clazz, ignoreErrors);
	}

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
