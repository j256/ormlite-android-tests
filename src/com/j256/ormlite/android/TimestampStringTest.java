package com.j256.ormlite.android;

import java.sql.Timestamp;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class TimestampStringTest extends BaseDaoTest {

	public void testStuff() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.timestamp = new Timestamp(System.currentTimeMillis());
		assertEquals(1, fooDao.create(foo));

		Foo result = fooDao.queryForId(foo.id);
		assertEquals(foo.timestamp, result.timestamp);
	}

	protected static class Foo {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(format = "MM/dd/yyyy HH-mm-ss-SSSSSS")
		public Timestamp timestamp;
		public Foo() {
		}
	}
}
