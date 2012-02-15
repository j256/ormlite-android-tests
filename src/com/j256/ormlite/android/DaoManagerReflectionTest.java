package com.j256.ormlite.android;

import java.util.Arrays;
import java.util.Collection;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTableConfig;

public class DaoManagerReflectionTest extends BaseDaoTest {

	public void testForeignDaos() throws Exception {
		DaoManager.clearCache();
		assertNull(DaoManager.lookupDao(connectionSource, Foo.class));
		assertNull(DaoManager.lookupDao(connectionSource, Foreign.class));

		Dao<Foo, Object> fooDao1 = createDao(Foo.class, true);
		Dao<Foo, ?> fooDao2 = DaoManager.lookupDao(connectionSource, Foo.class);
		assertSame(fooDao1, fooDao2);
		
		Dao<Foreign, Integer> foreignDao1 = DaoManager.lookupDao(connectionSource, Foreign.class);
		assertNotNull(foreignDao1);
		Dao<Foreign, Integer> foreignDao2 = DaoManager.createDao(connectionSource, Foreign.class);
		assertSame(foreignDao1, foreignDao2);
	}

	public void testForeignDaoTableConfig() throws Exception {
		DaoManager.clearCache();
		assertNull(DaoManager.lookupDao(connectionSource, Foo.class));
		assertNull(DaoManager.lookupDao(connectionSource, Foreign.class));

		DatabaseFieldConfig fieldConfig = new DatabaseFieldConfig("id");
		fieldConfig.setGeneratedId(true);
		DatabaseTableConfig<Foo> tableConfig = new DatabaseTableConfig<Foo>(Foo.class, Arrays.asList(fieldConfig));

		Dao<Foo, Object> fooDao1 = createDao(tableConfig, true);
		Dao<Foo, ?> fooDao2 = DaoManager.lookupDao(connectionSource, Foo.class);
		assertSame(fooDao1, fooDao2);
		assertNull(DaoManager.lookupDao(connectionSource, Foreign.class));
	}

	/* ==================================================================================== */

	protected static class Foo {
		@DatabaseField(generatedId = true)
		public int id;
		@ForeignCollectionField
		public Collection<Foreign> foreign;
		public Foo() {
		}
	}

	protected static class Foreign {
		public static final String FOREIGN_COLUMN_NAME = "foo_id";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, columnName = FOREIGN_COLUMN_NAME)
		public Foo foo;
		public Foreign() {
		}
	}
}
