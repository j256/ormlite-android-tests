package com.j256.ormlite.android;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class SqliteRawRowMapperImplTest extends BaseDaoTest {

	public void testStringArrayRowMapper() throws Exception {
		Dao<MyDouble, Object> dao = createDao(MyDouble.class, true);

		MyDouble foo = new MyDouble();
		foo.val = 1.23456789123456;
		assertEquals(1, dao.create(foo));

		MyDouble result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(foo.val, result.val, 0.0);

		GenericRawResults<String[]> results =
				dao.queryRaw(dao.queryBuilder().selectColumns("id", "val").prepareStatementString());
		CloseableIterator<String[]> iterator = results.closeableIterator();
		try {
			assertTrue(iterator.hasNext());
			String[] strings = iterator.next();
			assertNotNull(strings);

			assertTrue(strings.length >= 2);
			assertEquals(foo.id, Integer.parseInt(strings[0]));
			// NOTE: this doesn't work because the output is truncated
			// assertEquals(foo.val, Double.parseDouble(strings[1]));
		} finally {
			iterator.close();
		}
	}

	public void testObjectArrayRowMapper() throws Exception {
		Dao<MyDouble, Object> dao = createDao(MyDouble.class, true);

		MyDouble foo = new MyDouble();
		foo.val = 1.23456789123456;
		assertEquals(1, dao.create(foo));

		MyDouble result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(foo.val, result.val, 0.0);

		GenericRawResults<Object[]> results =
				dao.queryRaw(dao.queryBuilder().selectColumns("id", "val").prepareStatementString(), new DataType[] {
						DataType.INTEGER, DataType.DOUBLE });
		CloseableIterator<Object[]> iterator = results.closeableIterator();
		try {
			assertTrue(iterator.hasNext());
			Object[] objs = iterator.next();
			assertNotNull(objs);

			assertTrue(objs.length >= 2);
			assertEquals(foo.id, objs[0]);
			// this works fine
			assertEquals(foo.val, objs[1]);
		} finally {
			iterator.close();
		}
	}

	protected static class MyDouble {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public double val;
		public MyDouble() {
			// needed for ormlite
		}
	}
}
