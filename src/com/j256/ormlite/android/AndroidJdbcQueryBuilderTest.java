package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public class AndroidJdbcQueryBuilderTest extends AndroidTestCase {

	private ConnectionSource connectionSource;
	private DatabaseType databaseType;
	private OrmDatabaseHelper helper;

	private Set<DatabaseTableConfig<?>> dropClassSet = new HashSet<DatabaseTableConfig<?>>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		helper = new OrmDatabaseHelper(getContext());
		connectionSource = helper.getConnectionSource();
		databaseType = connectionSource.getDatabaseType();
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
	 * Insert the JdbcStatementBuilderTest.java below
	 * ==============================================================================================================
	 */

	private final static String ID_PREFIX = "id";
	private final static int LOW_VAL = 21114;
	private final static int HIGH_VAL = LOW_VAL + 499494;
	private final static int EQUAL_VAL = 21312312;
	private Foo foo1;
	private Foo foo2;

	public void testAnd() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		// test And + Eq
		qb.where().eq(Foo.ID_COLUMN_NAME, foo1.id).and().eq(Foo.VAL_COLUMN_NAME, foo1.val);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		// test And + Eq not inline
		Where<Foo, String> where = qb.where();
		where.eq(Foo.ID_COLUMN_NAME, foo2.id);
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		// test And double args
		where = qb.where();
		where.and(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.VAL_COLUMN_NAME, foo1.val));
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
	}

	public void testOr() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		// test Or + Eq
		qb.where().eq(Foo.ID_COLUMN_NAME, foo1.id).or().eq(Foo.VAL_COLUMN_NAME, foo1.val);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		// test Or + Eq not inline
		Where<Foo, String> where = qb.where();
		where.eq(Foo.ID_COLUMN_NAME, foo2.id);
		where.or();
		where.eq(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		// test Or of ands
		where = qb.where();
		where.or(where.and(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.VAL_COLUMN_NAME, foo1.val)),
				where.eq(Foo.ID_COLUMN_NAME, foo2.id).and().eq(Foo.VAL_COLUMN_NAME, foo2.val));
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testSelectArgs() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		SelectArg idSelectArg = new SelectArg();
		qb.where().eq(Foo.ID_COLUMN_NAME, idSelectArg);
		PreparedQuery<Foo> preparedQuery = qb.prepare();

		idSelectArg.setValue(foo1.id);
		List<Foo> results = fooDao.query(preparedQuery);
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		idSelectArg.setValue(foo2.id);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	public void testLike() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().like(Foo.ID_COLUMN_NAME, ID_PREFIX + "%");
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testSelectArgsNotSet() throws Exception {

		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		SelectArg idSelectArg = new SelectArg();
		qb.where().eq(Foo.ID_COLUMN_NAME, idSelectArg);
		try {
			fooDao.query(qb.prepare());
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testSelectNot() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().not().eq(Foo.ID_COLUMN_NAME, foo1.id);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	public void testIn() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().in(Foo.ID_COLUMN_NAME, foo1.id, foo2.id);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testInIterable() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().in(Foo.ID_COLUMN_NAME, Arrays.asList(foo1.id, foo2.id));
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testExists() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> innerQb = fooDao.queryBuilder();
		innerQb.where().idEq(foo1.id);
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().exists(innerQb);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testExistsNoEntries() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> innerQb = fooDao.queryBuilder();
		innerQb.where().idEq("no id by this name");
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().exists(innerQb);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());
	}

	public void testNotExists() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> innerQb = fooDao.queryBuilder();
		innerQb.where().idEq(foo1.id);
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().not().exists(innerQb);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());
	}

	public void testNotIn() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().not().in(Foo.ID_COLUMN_NAME, foo1.id);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	public void testNotBad() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.where().not();
		try {
			fooDao.query(qb.prepare());
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testNotNotComparison() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		Where<Foo, String> where = qb.where();
		try {
			where.not(where.and(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.ID_COLUMN_NAME, foo1.id)));
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testNotArg() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		Where<Foo, String> where = qb.where();
		where.not(where.eq(Foo.ID_COLUMN_NAME, foo1.id));
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	public void testNoWhereOperations() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.where();
		try {
			fooDao.query(qb.prepare());
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testMissingAnd() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.where().eq(Foo.ID_COLUMN_NAME, foo1.id).eq(Foo.ID_COLUMN_NAME, foo1.id);
		try {
			fooDao.query(qb.prepare());
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testMissingAndArg() throws Exception {
		Dao<Foo, String> fooDao = createDao(Foo.class, false);
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		try {
			qb.where().and();
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testBetween() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL, HIGH_VAL);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL + 1, HIGH_VAL);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL, HIGH_VAL - 1);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL + 1, HIGH_VAL - 1);
		results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());
	}

	public void testBetweenSelectArg() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		SelectArg lowSelectArg = new SelectArg();
		qb.where().between(Foo.VAL_COLUMN_NAME, lowSelectArg, HIGH_VAL);
		lowSelectArg.setValue(LOW_VAL);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		SelectArg highSelectArg = new SelectArg();
		lowSelectArg.setValue(LOW_VAL + 1);
		highSelectArg.setValue(HIGH_VAL);
		qb.where().between(Foo.VAL_COLUMN_NAME, lowSelectArg, highSelectArg);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	public void testBetweenStrings() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		String low = ID_PREFIX;
		String high = ID_PREFIX + "99999";
		qb.where().between(Foo.ID_COLUMN_NAME, low, high);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testLtGtEtc() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		qb.where().eq(Foo.VAL_COLUMN_NAME, foo1.val);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().ge(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().ge(Foo.VAL_COLUMN_NAME, foo1.val - 1);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().ge(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().gt(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().gt(Foo.VAL_COLUMN_NAME, foo1.val - 1);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().gt(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());

		qb.where().le(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().le(Foo.VAL_COLUMN_NAME, foo1.val - 1);
		results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());

		qb.where().lt(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());

		qb.where().lt(Foo.VAL_COLUMN_NAME, foo1.val + 1);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().ne(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().ne(Foo.VAL_COLUMN_NAME, foo1.val).and().ne(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());
	}

	public void testPartialQueryAllRetrieval() throws Exception {
		Dao<PartialData, Integer> partialDao = createDao(PartialData.class, true);
		List<String> firsts = new ArrayList<String>();
		List<String> lasts = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();

		createPartial(partialDao, ids, firsts, lasts, "bill", "rambo");
		createPartial(partialDao, ids, firsts, lasts, "zippy", "dingo");
		createPartial(partialDao, ids, firsts, lasts, "crappy", "bladdero");
		checkPartialList(partialDao.queryForAll(), ids, firsts, lasts, false, false);

		Set<String> columnNames = new HashSet<String>();
		QueryBuilder<PartialData, Integer> qb = partialDao.queryBuilder();
		qb.selectColumns(columnNames);
		List<PartialData> partialList = partialDao.query(qb.prepare());
		checkPartialList(partialList, ids, firsts, lasts, true, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.FIRST_FIELD_NAME);
		qb.selectColumns(columnNames);
		partialList = partialDao.query(qb.prepare());
		checkPartialList(partialList, ids, firsts, lasts, false, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.LAST_FIELD_NAME);
		qb.selectColumns(columnNames);
		partialList = partialDao.query(qb.prepare());
		checkPartialList(partialList, ids, firsts, lasts, false, false);

		for (PartialData partialData : partialDao) {
			assertEquals(1, partialDao.delete(partialData));
		}
		assertEquals(0, partialDao.queryForAll().size());
	}

	public void testPartialIteratorRetrieval() throws Exception {
		Dao<PartialData, Integer> partialDao = createDao(PartialData.class, true);
		List<String> firsts = new ArrayList<String>();
		List<String> lasts = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();

		createPartial(partialDao, ids, firsts, lasts, "bill", "rambo");
		createPartial(partialDao, ids, firsts, lasts, "zippy", "dingo");
		createPartial(partialDao, ids, firsts, lasts, "crappy", "bladdero");
		checkPartialList(partialDao.queryForAll(), ids, firsts, lasts, false, false);
		checkPartialIterator(partialDao.iterator(), ids, firsts, lasts, false, false);

		Set<String> columnNames = new HashSet<String>();
		QueryBuilder<PartialData, Integer> qb = partialDao.queryBuilder();
		qb.selectColumns(columnNames);
		Iterator<PartialData> iterator = partialDao.iterator(qb.prepare());
		checkPartialIterator(iterator, ids, firsts, lasts, true, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.FIRST_FIELD_NAME);
		qb.selectColumns(columnNames);
		iterator = partialDao.iterator(qb.prepare());
		checkPartialIterator(iterator, ids, firsts, lasts, false, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.LAST_FIELD_NAME);
		qb.selectColumns(columnNames);
		iterator = partialDao.iterator(qb.prepare());
		checkPartialIterator(iterator, ids, firsts, lasts, false, false);

		for (PartialData partialData : partialDao) {
			assertEquals(1, partialDao.delete(partialData));
		}
		assertEquals(0, partialDao.queryForAll().size());
	}

	public void testIteratorCustomQuery() throws Exception {
		Dao<PartialData, Integer> partialDao = createDao(PartialData.class, true);
		List<String> firsts = new ArrayList<String>();
		List<String> lasts = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();

		String firstFirst = "bill";
		createPartial(partialDao, ids, firsts, lasts, firstFirst, "rambo");
		createPartial(partialDao, ids, firsts, lasts, "zippy", "dingo");
		createPartial(partialDao, ids, firsts, lasts, "crappy", "bladdero");
		checkPartialList(partialDao.queryForAll(), ids, firsts, lasts, false, false);
		checkPartialIterator(partialDao.iterator(), ids, firsts, lasts, false, false);

		QueryBuilder<PartialData, Integer> qb = partialDao.queryBuilder();
		qb.where().eq(PartialData.FIRST_FIELD_NAME, firstFirst);
		Iterator<PartialData> iterator = partialDao.iterator(qb.prepare());
		assertTrue(iterator.hasNext());
		assertEquals(firstFirst, iterator.next().first);
		assertFalse(iterator.hasNext());

		SelectArg firstArg = new SelectArg();
		qb.where().eq(PartialData.FIRST_FIELD_NAME, firstArg);
		firstArg.setValue(firstFirst);
		iterator = partialDao.iterator(qb.prepare());
		assertTrue(iterator.hasNext());
		assertEquals(firstFirst, iterator.next().first);
		assertFalse(iterator.hasNext());
	}

	public void testUnknownColumn() throws Exception {
		Dao<Foo, String> fooDao = createDao(Foo.class, false);
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		try {
			qb.selectColumns("unknown column");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testOrderBy() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.orderBy(Foo.VAL_COLUMN_NAME, true);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb = fooDao.queryBuilder();;
		qb.orderBy(Foo.VAL_COLUMN_NAME, false);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo2, results.get(0));
		assertEquals(foo1, results.get(1));

		// should be the same order
		qb = fooDao.queryBuilder();;
		qb.orderBy(Foo.EQUAL_COLUMN_NAME, false);
		qb.orderBy(Foo.VAL_COLUMN_NAME, false);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo2, results.get(0));
		assertEquals(foo1, results.get(1));
	}

	public void testGroupBy() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.selectColumns(Foo.EQUAL_COLUMN_NAME);
		qb.groupBy(Foo.EQUAL_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(EQUAL_VAL, results.get(0).equal);
		assertNull(results.get(0).id);
	}

	public void testGroupAndOrderBy() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.selectColumns(Foo.EQUAL_COLUMN_NAME, Foo.ID_COLUMN_NAME);
		qb.groupBy(Foo.EQUAL_COLUMN_NAME);
		qb.groupBy(Foo.ID_COLUMN_NAME);
		// get strange order otherwise
		qb.orderBy(Foo.ID_COLUMN_NAME, true);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testLimit() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		// no limit the default
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
		qb.limit(1);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
		// set back to no-limit
		qb.limit(null);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testLimitDoublePrepare() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		// no limit the default
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
		qb.limit(1);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
	}

	public void testOffsetWorks() throws Exception {
		if (!databaseType.isLimitSqlSupported()) {
			return;
		}

		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.id = "stuff1";
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.id = "stuff2";
		assertEquals(1, dao.create(foo2));

		assertEquals(2, dao.queryForAll().size());

		QueryBuilder<Foo, Object> qb = dao.queryBuilder();
		int offset = 1;
		int limit = 2;
		qb.offset(offset);
		qb.limit(limit);
		List<Foo> results = dao.query(qb.prepare());

		assertEquals(1, results.size());
	}

	public void testLimitAfterSelect() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		// no limit the default
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
		qb.limit(1);
		results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
		// set back to no-limit
		qb.limit(null);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testReturnId() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.selectColumns(Foo.ID_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(0, results.get(0).val);
		assertEquals(foo2.id, results.get(1).id);
		assertEquals(0, results.get(1).val);
	}

	public void testDistinct() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		qb.distinct().selectColumns(Foo.EQUAL_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(EQUAL_VAL, results.get(0).equal);
		assertNull(results.get(0).id);
	}

	public void testIsNull() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();

		// null fields start off as null so 0 are not-null
		qb.where().isNotNull(Foo.NULL_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());

		// all are null
		qb.where().isNull(Foo.NULL_COLUMN_NAME);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		// set the null fields to not-null
		for (Foo foo : results) {
			foo.nullField = "not null";
			assertEquals(1, fooDao.update(foo));
		}

		// no null results should be found
		qb.where().isNull(Foo.NULL_COLUMN_NAME);
		results = fooDao.query(qb.prepare());
		assertEquals(0, results.size());

		// all are not-null
		qb.where().isNotNull(Foo.NULL_COLUMN_NAME);
		results = fooDao.query(qb.prepare());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	public void testSetWhere() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		Where<Foo, String> where = qb.where();
		where.eq(Foo.ID_COLUMN_NAME, foo1.id);
		List<Foo> list = fooDao.query(qb.prepare());
		assertEquals(1, list.size());
		assertEquals(foo1, list.get(0));

		qb = fooDao.queryBuilder();
		qb.setWhere(where);
		list = fooDao.query(qb.prepare());
		assertEquals(1, list.size());
		assertEquals(foo1, list.get(0));
	}

	public void testQueryForStringInt() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		QueryBuilder<Foo, String> qb = fooDao.queryBuilder();
		Where<Foo, String> where = qb.where();
		// testing the val column with a integer as a string
		where.eq(Foo.VAL_COLUMN_NAME, Integer.toString(foo1.val));
		List<Foo> list = fooDao.query(qb.prepare());
		assertEquals(1, list.size());
		assertEquals(foo1, list.get(0));
	}

	public void testWherePrepare() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		List<Foo> results =
				fooDao.query(fooDao.queryBuilder()
						.where()
						.eq(Foo.ID_COLUMN_NAME, foo1.id)
						.and()
						.eq(Foo.VAL_COLUMN_NAME, foo1.val)
						.prepare());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
	}

	public void testIdEq() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);

		Foo foo = new Foo();
		foo.id = "wow id wow";
		assertEquals(1, fooDao.create(foo));

		List<Foo> results = fooDao.query(fooDao.queryBuilder().where().idEq(fooDao, foo).prepare());
		assertEquals(1, results.size());
		assertEquals(foo.id, results.get(0).id);
	}

	/* ============================================================== */

	protected void checkPartialIterator(Iterator<PartialData> iterator, List<Integer> ids, List<String> firsts,
			List<String> lasts, boolean firstNull, boolean lastNull) throws SQLException {
		int i = 0;
		while (iterator.hasNext()) {
			PartialData partialData = iterator.next();
			checkPartial(partialData, ids, firsts, lasts, i, firstNull, lastNull);
			i++;
		}
	}

	protected void createPartial(Dao<PartialData, Integer> partialDao, List<Integer> ids, List<String> firsts,
			List<String> lasts, String first, String last) throws SQLException {
		PartialData partial = new PartialData();
		partial.first = first;
		partial.last = last;
		partialDao.create(partial);
		ids.add(partial.id);
		firsts.add(partial.first);
		lasts.add(partial.last);
		checkPartial(partialDao.queryForId(partial.id), ids, firsts, lasts, ids.size() - 1, false, false);
	}

	protected void checkPartialList(List<PartialData> partialList, List<Integer> ids, List<String> firsts,
			List<String> lasts, boolean firstNull, boolean lastNull) throws SQLException {
		assertEquals(partialList.size(), ids.size());
		for (int i = 0; i < partialList.size(); i++) {
			PartialData partial = partialList.get(i);
			assertEquals((int) ids.get(i), partial.id);
			if (firstNull) {
				assertNull(partial.first);
			} else {
				assertEquals(partial.first, firsts.get(i));
			}
			if (lastNull) {
				assertNull(partial.last);
			} else {
				assertEquals(partial.last, lasts.get(i));
			}
		}
	}

	private void checkPartial(PartialData partial, List<Integer> ids, List<String> firsts, List<String> lasts,
			int which, boolean firstNull, boolean lastNull) throws SQLException {
		assertNotNull(partial);
		assertTrue(which >= 0 && which < firsts.size());
		assertEquals((int) ids.get(which), partial.id);
		if (firstNull) {
			assertNull(partial.first);
		} else {
			assertEquals(partial.first, firsts.get(which));
		}
		if (lastNull) {
			assertNull(partial.last);
		} else {
			assertEquals(partial.last, lasts.get(which));
		}
	}

	private Dao<Foo, String> createTestData() throws Exception {
		Dao<Foo, String> fooDao = createDao(Foo.class, true);
		foo1 = new Foo();
		foo1.id = ID_PREFIX + "1";
		foo1.val = LOW_VAL;
		foo1.equal = EQUAL_VAL;
		assertEquals(1, fooDao.create(foo1));
		foo2 = new Foo();
		foo2.id = ID_PREFIX + "2";
		foo2.val = HIGH_VAL;
		foo2.equal = EQUAL_VAL;
		assertEquals(1, fooDao.create(foo2));
		return fooDao;
	}

	protected static class Foo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String NULL_COLUMN_NAME = "null";

		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		String id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		int equal;
		@DatabaseField(columnName = NULL_COLUMN_NAME)
		String nullField;
		@Override
		public String toString() {
			return "Foo:" + id;
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id.equals(((Foo) other).id);
		}
	}

	protected static class PartialData {
		public static final String FIRST_FIELD_NAME = "first";
		public static final String LAST_FIELD_NAME = "last";

		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(columnName = FIRST_FIELD_NAME)
		public String first;
		@DatabaseField(columnName = LAST_FIELD_NAME)
		public String last;
	}
}
