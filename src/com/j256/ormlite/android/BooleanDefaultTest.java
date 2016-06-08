package com.j256.ormlite.android;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class BooleanDefaultTest extends BaseDaoTest {

	public void testDefaultTrue() throws Exception {
		Dao<FooTrue, Integer> fooDao = createDao(FooTrue.class, true);
		FooTrue foo = new FooTrue();
		assertEquals(1, fooDao.create(foo));

		FooTrue result = fooDao.queryForId(foo.id);
		assertEquals(foo.id, result.id);
		assertTrue(result.bool);
	}

	public void testDefaultFalse() throws Exception {
		Dao<FooFalse, Integer> fooDao = createDao(FooFalse.class, true);
		FooFalse foo = new FooFalse();
		assertEquals(1, fooDao.create(foo));

		FooFalse result = fooDao.queryForId(foo.id);
		assertEquals(foo.id, result.id);
		assertFalse(result.bool);
	}

	protected static class FooTrue {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(defaultValue = "true")
		Boolean bool;
	}

	protected static class FooFalse {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(defaultValue = "false")
		Boolean bool;
	}
}
