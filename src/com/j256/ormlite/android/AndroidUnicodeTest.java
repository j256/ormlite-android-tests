package com.j256.ormlite.android;

import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;

public class AndroidUnicodeTest extends BaseDaoTest {

	public void testStoreUnicode() throws Exception {
		TableUtils.dropTable(connectionSource, Foo.class, true);
		TableUtils.createTable(connectionSource, Foo.class);
		try {
			Dao<Foo, Object> dao = DaoManager.createDao(connectionSource, Foo.class);
			Foo foo = new Foo();
			String unicodeString = "上海";
			foo.stuff = unicodeString;
			assertEquals(1, dao.create(foo));

			Foo result = dao.queryForId(foo.id);
			assertNotNull(result);
			assertEquals(unicodeString, result.stuff);
		} finally {
			TableUtils.dropTable(connectionSource, Foo.class, true);
		}
	}

	public void testQueryForUnicode() throws Exception {
		TableUtils.dropTable(connectionSource, Foo.class, true);
		TableUtils.createTable(connectionSource, Foo.class);
		try {
			Dao<Foo, Object> dao = DaoManager.createDao(connectionSource, Foo.class);
			Foo foo = new Foo();
			String unicodeString = "上海";
			foo.stuff = unicodeString;
			assertEquals(1, dao.create(foo));

			QueryBuilder<Foo, Object> qb = dao.queryBuilder();
			Where<Foo, Object> where = qb.where();
			where.eq(Foo.STUFF_FIELD_NAME, unicodeString);
			List<Foo> results = qb.query();
			assertNotNull(results);
			assertEquals(1, results.size());
			assertEquals(unicodeString, results.get(0).stuff);

			where.reset();
			where.eq(Foo.STUFF_FIELD_NAME, new SelectArg(unicodeString));
			results = qb.query();
			assertNotNull(results);
			assertEquals(1, results.size());
			assertEquals(unicodeString, results.get(0).stuff);

		} finally {
			TableUtils.dropTable(connectionSource, Foo.class, true);
		}
	}

	protected static class Foo {
		public final static String ID_FIELD_NAME = "id";
		public final static String STUFF_FIELD_NAME = "stuff";

		@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
		public int id;
		@DatabaseField(columnName = STUFF_FIELD_NAME)
		public String stuff;
		public Foo() {
		}
	}
}
