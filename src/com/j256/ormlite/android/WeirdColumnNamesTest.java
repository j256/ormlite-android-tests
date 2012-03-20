package com.j256.ormlite.android;

import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

/**
 * Tests some escaping of names
 * 
 * @author graywatson
 */
public class WeirdColumnNamesTest extends BaseDaoTest {

	public void testWierdColumnNames() throws Exception {
		Dao<WeirdColumnNames, Object> dao = createDao(WeirdColumnNames.class, true);
		WeirdColumnNames foo = new WeirdColumnNames();
		foo.stuff = "peowjfpwjfowefwe";
		assertEquals(1, dao.create(foo));

		WeirdColumnNames result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(foo.stuff, result.stuff);

		List<WeirdColumnNames> results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(foo.stuff, results.get(0).stuff);
	}

	/**
	 * We test this because we are building an internal map of names in {@link AndroidDatabaseResults} if the number of
	 * columns is greater than some constant.
	 */
	public void testManyColumns() throws Exception {
		Dao<ManyColumns, Object> dao = createDao(ManyColumns.class, true);
		ManyColumns foo = new ManyColumns();
		foo.stuff1 = "peowjfpwjfowefwe";
		foo.stuff10 = "pgjpoeowjfpwjfowefwe";
		foo.stuff20 = "79056hgoerefwe";
		assertEquals(1, dao.create(foo));

		ManyColumns result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(foo.stuff1, result.stuff1);
		assertEquals(foo.stuff10, result.stuff10);
		assertEquals(foo.stuff20, result.stuff20);

		List<ManyColumns> results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(foo.stuff1, results.get(0).stuff1);
		assertEquals(foo.stuff10, results.get(0).stuff10);
		assertEquals(foo.stuff20, results.get(0).stuff20);
	}

	protected static class WeirdColumnNames {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = "foo.bar")
		String stuff;
		public WeirdColumnNames() {
		}
	}

	protected static class ManyColumns {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff1;
		@DatabaseField
		String stuff2;
		@DatabaseField
		String stuff3;
		@DatabaseField
		String stuff4;
		@DatabaseField
		String stuff5;
		@DatabaseField
		String stuff6;
		@DatabaseField
		String stuff7;
		@DatabaseField
		String stuff8;
		@DatabaseField
		String stuff9;
		@DatabaseField
		String stuff10;
		@DatabaseField
		String stuff11;
		@DatabaseField
		String stuff12;
		@DatabaseField
		String stuff13;
		@DatabaseField
		String stuff14;
		@DatabaseField
		String stuff15;
		@DatabaseField
		String stuff16;
		@DatabaseField
		String stuff17;
		@DatabaseField
		String stuff18;
		@DatabaseField
		String stuff19;
		@DatabaseField
		String stuff20;
		public ManyColumns() {
		}
	}

}
