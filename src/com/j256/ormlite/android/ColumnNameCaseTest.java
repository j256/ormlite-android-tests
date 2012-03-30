package com.j256.ormlite.android;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class ColumnNameCaseTest extends BaseDaoTest {

	public void testDeleteAllReturns0Rows() throws Exception {
		Dao<FooLower, Integer> fooLowerDao = createDao(FooLower.class, true);

		// we create the field as lowercase
		FooLower foo = new FooLower();
		foo.camelCase = "powjfpwejfoewjf";
		assertEquals(1, fooLowerDao.create(foo));

		Dao<Foo, Integer> fooDao = createDao(Foo.class, false);

		try {
			fooDao.queryForId(foo.id);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	@DatabaseTable(tableName = "foo")
	protected static class Foo {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String camelCase;
		public Foo() {
		}
	}

	@DatabaseTable(tableName = "foo")
	protected static class FooLower {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(columnName = "camelcase")
		public String camelCase;
		public FooLower() {
		}
	}
}
