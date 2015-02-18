package com.j256.ormlite.android;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;

public class DeleteAllTrunccateTest extends BaseDaoTest {

	public void testDeleteAllReturns0Rows() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		int fooN = 10;
		for (int i = 0; i < fooN; i++) {
			Foo foo = new Foo();
			assertEquals(1, fooDao.create(foo));
		}

		DeleteBuilder<Foo, Integer> stmtBuilder = fooDao.deleteBuilder();
		assertEquals(fooN, fooDao.delete(stmtBuilder.prepare()));
		assertEquals(0, fooDao.queryForAll().size());
	}

	public void testDeleteAllWithDumbWhereReturnsRows() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		int fooN = 10;
		for (int i = 0; i < fooN; i++) {
			Foo foo = new Foo();
			assertEquals(1, fooDao.create(foo));
		}

		DeleteBuilder<Foo, Integer> stmtBuilder = fooDao.deleteBuilder();
		// add a stupid WHERE
		stmtBuilder.where().raw("1");
		// this returns right
		assertEquals(fooN, fooDao.delete(stmtBuilder.prepare()));
		assertEquals(0, fooDao.queryForAll().size());
	}

	protected static class Foo {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String stuff;
		public Foo() {
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id == ((Foo) other).id;
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
