package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public class AndroidTransactionManagerTest extends AndroidTestCase {

	/* ============================================================================================================== */

	private DatabaseType databaseType = new SqliteAndroidDatabaseType();
	private ConnectionSource connectionSource;
	private OrmDatabaseHelper helper;

	private Set<DatabaseTableConfig<?>> dropClassSet = new HashSet<DatabaseTableConfig<?>>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		helper = new OrmDatabaseHelper(getContext());
		connectionSource = helper.getConnectionSource();
	}

	@Override
	protected void tearDown() throws Exception {
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

	/* ============================================================================================================== */

	public void testDaoTransactionManagerCommitted() throws Exception {
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		String stuff = "stuff";
		foo1.stuff = stuff;
		assertEquals(1, fooDao.create(foo1));
		TransactionManager mgr = new TransactionManager(connectionSource);
		final int returnVal = 284234832;
		int val = mgr.callInTransaction(new Callable<Integer>() {
			public Integer call() throws Exception {
				// we delete it inside a transaction
				assertEquals(1, fooDao.delete(foo1));
				// we can't find it
				assertNull(fooDao.queryForId(foo1.id));
				return returnVal;
			}
		});
		assertEquals(returnVal, val);

		// still doesn't find it after we delete it
		assertNull(fooDao.queryForId(foo1.id));
	}

	public void testRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager(connectionSource);
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"));
	}

	public void testSpringWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"));
	}

	public void testNonRuntimeExceptionWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		testTransactionManager(mgr, new Exception("What!!  I protest via an Exception!!"));
	}

	private void testTransactionManager(TransactionManager mgr, final Exception exception) throws Exception {
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		String stuff = "stuff";
		foo1.stuff = stuff;
		assertEquals(1, fooDao.create(foo1));
		try {
			mgr.callInTransaction(new Callable<Void>() {
				public Void call() throws Exception {
					// we delete it inside a transaction
					assertEquals(1, fooDao.delete(foo1));
					// we can't find it
					assertNull(fooDao.queryForId(foo1.id));
					// but then we throw an exception which rolls back the transaction
					throw exception;
				}
			});
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}

		// still finds it after we delete it
		Foo foo2 = fooDao.queryForId(foo1.id);
		assertNotNull(foo2);
		assertEquals(stuff, foo2.stuff);
	}

	public static class Foo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		Foo() {
			// for ormlite
		}
	}

	/* ============================================================================================================== */

	private <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		return createDao(DatabaseTableConfig.fromClass(databaseType, clazz), createTable);
	}

	private <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable) throws Exception {
		BaseDaoImpl<T, ID> dao = new BaseDaoImpl<T, ID>(databaseType, tableConfig) {
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
		TableUtils.createTable(databaseType, connectionSource, tableConfig);
		if (dropAtEnd) {
			dropClassSet.add(tableConfig);
		}
	}

	private <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(databaseType, connectionSource, tableConfig, ignoreErrors);
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
}
