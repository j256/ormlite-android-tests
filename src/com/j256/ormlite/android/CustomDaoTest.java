package com.j256.ormlite.android;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

/**
 * We do this to protect from any changes in the future to the way we get the daos from the helper. 4.28 broke this.
 */
public class CustomDaoTest extends AndroidTestCase {

	public void testCustomDao() throws Exception {
		DatabaseHelper helper = new DatabaseHelper(getContext());
		OurDao ourDao = helper.getDao(Foo.class);
		Foo foo = new Foo();
		foo.stuff = "jfpfjewf";
		assertEquals(1, ourDao.create(foo));
	}

	public interface OurDao extends Dao<Foo, Integer> {
	}

	protected static class Foo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		public Foo() {
		}
	}
}
