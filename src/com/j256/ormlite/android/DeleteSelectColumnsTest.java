package com.j256.ormlite.android;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;

public class DeleteSelectColumnsTest extends BaseDaoTest {

	public void testDeleteSelectColumns() throws Exception {
		Dao<DeleteColumns, Integer> dao = createDao(DeleteColumns.class, true);
		DeleteColumns foo = new DeleteColumns();
		foo.stuff = "wjfpwejfwe";
		foo.another = "fjeopwjwfepojfwe";
		assertEquals(1, dao.create(foo));

		QueryBuilder<DeleteColumns, Integer> qb = dao.queryBuilder();
		qb.selectColumns(DeleteColumns.ANOTHER_FIELD_NAME);
		DeleteColumns result = qb.queryForFirst();
		assertNotNull(result);
		assertEquals(foo.id, result.id);
		assertEquals(foo.another, result.another);
		assertNull(result.stuff);
	}

	/* ------------------------------------------------------------------------------------ */

	protected static class DeleteColumns {
		public final static String ANOTHER_FIELD_NAME = "another";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String stuff;
		@DatabaseField(columnName = ANOTHER_FIELD_NAME)
		public String another;
		public DeleteColumns() {
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id == ((DeleteColumns) other).id;
		}
		@Override
		public int hashCode() {
			return id;
		}
		@Override
		public String toString() {
			return "Foo.id=" + id;
		}
	}
}
