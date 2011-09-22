package com.j256.ormlite.android;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class AndroidUnicodeTest extends AndroidTestCase {

	private ConnectionSource connectionSource;
	private DatabaseHelper helper;

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
		}
		if (connectionSource != null) {
			connectionSource.close();
			connectionSource = null;
		}
	}

	public void testStoreChinese() throws Exception {
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

	protected static class Foo {
		public final static String ID_FIELD_NAME = "id";
		public final static String STUFF_FIELD_NAME = "stuff";
		public final static String VAL_FIELD_NAME = "val";

		@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
		public int id;
		@DatabaseField(columnName = STUFF_FIELD_NAME)
		public String stuff;
		@DatabaseField(columnName = VAL_FIELD_NAME)
		public int val;
		public Foo() {
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id == ((Foo) other).id;
		}
		@Override
		public String toString() {
			return "Foo.id=" + id;
		}
	}
}
