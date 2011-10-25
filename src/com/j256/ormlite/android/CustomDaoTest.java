package com.j256.ormlite.android;

import java.sql.SQLException;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

/**
 * We do this to protect from any changes in the future to the way we get the daos from the helper. 4.28 broke this.
 */
public class CustomDaoTest extends AndroidTestCase {

	public void testCustomDao() throws Exception {
		DatabaseHelper helper = new DatabaseHelper(getContext());
		OurDao ourDao = helper.getDao(Foo.class);
		TableUtils.dropTable(helper.getConnectionSource(), Foo.class, true);
		TableUtils.createTable(helper.getConnectionSource(), Foo.class);
		try {
			Foo foo = new Foo();
			foo.stuff = "jfpfjewf";
			assertEquals(1, ourDao.create(foo));

			Foo result = ourDao.queryForId(foo.id);
			assertNotNull(result);
			assertEquals(foo.stuff, result.stuff);
		} finally {
			TableUtils.dropTable(helper.getConnectionSource(), Foo.class, true);
		}
	}

	public void testCustomRuntimeExceptionDao() throws Exception {
		DatabaseHelper helper = new DatabaseHelper(getContext());
		RuntimeExceptionDao<Foo, Integer> ourDao = helper.getRuntimeExceptionDao(Foo.class);
		TableUtils.dropTable(helper.getConnectionSource(), Foo.class, true);
		TableUtils.createTable(helper.getConnectionSource(), Foo.class);
		try {
			Foo foo = new Foo();
			foo.stuff = "jfpfjewf";
			assertEquals(1, ourDao.create(foo));

			Foo result = ourDao.queryForId(foo.id);
			assertNotNull(result);
			assertEquals(foo.stuff, result.stuff);
		} finally {
			TableUtils.dropTable(helper.getConnectionSource(), Foo.class, true);
		}

		try {
			// should throw a runtime exception
			ourDao.queryForId(1);
			fail("should have thrown");
		} catch (RuntimeException e) {
			// expected
		}
	}

	public interface OurDao extends Dao<Foo, Integer> {
	}

	public static class OurDaoImpl extends BaseDaoImpl<Foo, Integer> implements OurDao {
		public OurDaoImpl(ConnectionSource connectionSource) throws SQLException {
			super(connectionSource, Foo.class);
		}
	}

	@DatabaseTable(daoClass = OurDaoImpl.class)
	protected static class Foo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		public Foo() {
		}
	}
}
