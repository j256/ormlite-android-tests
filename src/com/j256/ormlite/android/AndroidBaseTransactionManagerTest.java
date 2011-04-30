package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
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

public class AndroidBaseTransactionManagerTest extends AndroidTestCase {

	private ConnectionSource connectionSource;
	private DatabaseHelper helper;
	private DatabaseType databaseType = new SqliteAndroidDatabaseType();

	private Set<DatabaseTableConfig<?>> dropClassSet = new HashSet<DatabaseTableConfig<?>>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		helper = new DatabaseHelper(getContext());
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

	/*
	 * ==============================================================================================================
	 * Insert the BaseTransactionManagerTest below
	 * ==============================================================================================================
	 */

	public void testDaoTransactionManagerCommitted() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		testTransactionManager(mgr, null, fooDao, false);
	}

	public void testRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"), fooDao, false);
	}

	public void testSpringWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"), fooDao, false);
	}

	public void testNonRuntimeExceptionWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		testTransactionManager(mgr, new Exception("What!!  I protest via an Exception!!"), fooDao, false);
	}

	public void testTransactionWithinTransaction() throws Exception {
		if (connectionSource == null) {
			return;
		}
		final TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		mgr.callInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
				testTransactionManager(mgr, null, fooDao, true);
				return null;
			}
		});
	}

	public void testTransactionWithinTransactionThrows() throws Exception {
		if (connectionSource == null || !databaseType.isNestedSavePointsSupported()) {
			return;
		}
		final TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		final Foo outerFoo = new Foo();
		String stuff = "outer stuff";
		outerFoo.stuff = stuff;
		assertEquals(1, fooDao.create(outerFoo));
		assertNotNull(fooDao.queryForId(outerFoo.id));
		assertEquals(1, fooDao.queryForAll().size());
		mgr.callInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
				assertEquals(1, fooDao.delete(outerFoo));
				assertNull(fooDao.queryForId(outerFoo.id));
				assertEquals(0, fooDao.queryForAll().size());
				testTransactionManager(mgr, new Exception("The inner transaction should throw and get rolled back"),
						fooDao, true);
				return null;
			}
		});
		List<Foo> fooList = fooDao.queryForAll();
		assertEquals(1, fooList.size());
		assertNull(fooDao.queryForId(outerFoo.id));
		// however we should have deleted the outer foo and are left with the inner foo
		assertTrue(fooList.get(0).id != outerFoo.id);
	}

	public void testNestedTransactionsNotSupported() throws Exception {
		if (connectionSource == null || databaseType.isNestedSavePointsSupported()) {
			return;
		}
		final TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		final Foo outerFoo = new Foo();
		String stuff = "outer stuff";
		outerFoo.stuff = stuff;
		assertEquals(1, fooDao.create(outerFoo));
		assertNotNull(fooDao.queryForId(outerFoo.id));
		assertEquals(1, fooDao.queryForAll().size());
		mgr.callInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
				assertEquals(1, fooDao.delete(outerFoo));
				assertNull(fooDao.queryForId(outerFoo.id));
				assertEquals(0, fooDao.queryForAll().size());
				testTransactionManager(mgr, new Exception("The inner transaction should throw and get rolled back"),
						fooDao, true);
				return null;
			}
		});
		List<Foo> fooList = fooDao.queryForAll();
		/*
		 * The inner transaction throws an exception but we don't want the inner transaction rolled back since an
		 * exception is caught there.
		 */
		assertEquals(0, fooList.size());
	}

	private void testTransactionManager(TransactionManager mgr, final Exception exception,
			final Dao<Foo, Integer> fooDao, boolean inner) throws Exception {
		final Foo foo1 = new Foo();
		String stuff = "stuff";
		foo1.stuff = stuff;
		assertEquals(1, fooDao.create(foo1));
		assertNotNull(fooDao.queryForId(foo1.id));
		try {
			final int val = 13431231;
			int returned = mgr.callInTransaction(new Callable<Integer>() {
				public Integer call() throws Exception {
					// we delete it inside a transaction
					assertEquals(1, fooDao.delete(foo1));
					// we can't find it
					assertNull(fooDao.queryForId(foo1.id));
					if (exception != null) {
						// but then we throw an exception which rolls back the transaction
						throw exception;
					} else {
						return val;
					}
				}
			});
			if (exception == null) {
				assertEquals(val, returned);
			} else {
				fail("Should have thrown");
			}
		} catch (SQLException e) {
			if (exception == null) {
				throw e;
			} else {
				// expected
			}
		}

		if (exception == null) {
			// still doesn't find it after we delete it
			assertNull(fooDao.queryForId(foo1.id));
		} else {
			// still finds it after we delete it since we threw inside of transaction
			Foo foo2 = fooDao.queryForId(foo1.id);
			if (databaseType.isNestedSavePointsSupported() || (!inner)) {
				assertNotNull(foo2);
				assertEquals(stuff, foo2.stuff);
			} else {
				assertNull(foo2);
			}
		}
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
}
