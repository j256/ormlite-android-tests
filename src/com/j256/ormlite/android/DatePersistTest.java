package com.j256.ormlite.android;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class DatePersistTest extends BaseDaoTest {

	public void testStuff() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		long now = System.currentTimeMillis();
		foo.sqlDate = new java.sql.Date(now);
		foo.utilDate = new java.util.Date(now);
		assertEquals(1, fooDao.create(foo));

		Foo result = fooDao.queryForId(foo.id);
		assertEquals(foo.sqlDate, result.sqlDate);
		assertEquals(foo.utilDate, result.utilDate);
	}

	protected static class Foo {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		java.sql.Date sqlDate;
		@DatabaseField
		java.util.Date utilDate;
	}
}
