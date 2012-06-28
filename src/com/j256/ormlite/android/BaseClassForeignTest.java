package com.j256.ormlite.android;

import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

/**
 * Tests base class issues
 * 
 * @author graywatson
 */
public class BaseClassForeignTest extends BaseDaoTest {

	public void testBaseClassForeignEq() throws Exception {
		Dao<One, Object> oneDao = createDao(One.class, true);
		Dao<ForeignSubClass, Object> foreignDao = createDao(ForeignSubClass.class, true);

		One one1 = new One();
		assertEquals(1, oneDao.create(one1));
		One one2 = new One();
		assertEquals(1, oneDao.create(one2));
		One one3 = new One();
		assertEquals(1, oneDao.create(one3));

		ForeignSubClass fii1 = new ForeignSubClass();
		fii1.one = one1;
		assertEquals(1, foreignDao.create(fii1));
		ForeignSubClass fii2 = new ForeignSubClass();
		fii2.one = one2;
		assertEquals(1, foreignDao.create(fii2));

		List<ForeignSubClass> results = foreignDao.queryBuilder().where().eq(ForeignIntId.FIELD_NAME_ONE, one1).query();
		assertEquals(1, results.size());
		assertEquals(fii1.id, results.get(0).id);

		results = foreignDao.queryBuilder().where().eq(ForeignIntId.FIELD_NAME_ONE, one2).query();
		assertEquals(1, results.size());
		assertEquals(fii2.id, results.get(0).id);

		results = foreignDao.queryBuilder().where().eq(ForeignIntId.FIELD_NAME_ONE, one3).query();
		assertEquals(0, results.size());
	}

	protected static class One {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String stuff;
		public One() {
		}
	}

	protected static class ForeignIntId {
		public static final String FIELD_NAME_ONE = "one";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, columnName = FIELD_NAME_ONE)
		public One one;
		public ForeignIntId() {
		}
	}

	protected static class ForeignSubClass extends ForeignIntId {
	}

}
