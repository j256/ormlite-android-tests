package com.j256.ormlite.android;

import java.util.List;

import android.test.AndroidTestCase;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class DatabaseTableConfigUtilTest extends AndroidTestCase {

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
			helper = null;
		}
		if (connectionSource != null) {
			connectionSource.close();
			connectionSource = null;
		}
	}

	/*
	 * ==============================================================================================================
	 */

	public void testBasic() throws Exception {
		DatabaseTableConfig<Foo> tableConfig = DatabaseTableConfigUtil.fromClass(connectionSource, Foo.class);
		List<DatabaseFieldConfig> fieldConfigs = tableConfig.getFieldConfigs();
		assertEquals(3, fieldConfigs.size());
		boolean foundId = false;
		boolean foundName = false;
		boolean foundSomeObject = false;
		for (DatabaseFieldConfig fieldConfig : fieldConfigs) {
			if (fieldConfig.getColumnName().equals(Foo.ID_FIELD_NAME)) {
				assertTrue(fieldConfig.isId());
				assertTrue(fieldConfig.isCanBeNull());
				assertEquals(DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL,
						fieldConfig.getMaxForeignAutoRefreshLevel());
				foundId = true;
			} else if (fieldConfig.getColumnName().equals(Foo.NAME_FIELD_NAME)) {
				assertFalse(fieldConfig.isId());
				assertFalse(fieldConfig.isCanBeNull());
				assertEquals(DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL,
						fieldConfig.getMaxForeignAutoRefreshLevel());
				foundName = true;
			} else if (fieldConfig.getColumnName().equals(Foo.SOME_OBJECT_FIELD_NAME)) {
				assertFalse(fieldConfig.isId());
				assertTrue(fieldConfig.isCanBeNull());
				assertEquals(Foo.MAX_FOREIGN_AUTO_REFRESH_LEVEL, fieldConfig.getMaxForeignAutoRefreshLevel());
				foundSomeObject = true;
			}
		}
		assertTrue(foundId);
		assertTrue(foundName);
		assertTrue(foundSomeObject);
	}
	public void testNoAnnotation() throws Exception {
		assertNull(DatabaseTableConfigUtil.fromClass(connectionSource, getClass()));
	}

	/* ================================================================ */

	protected static class Foo {
		public static final String ID_FIELD_NAME = "idthang";
		public static final String NAME_FIELD_NAME = "namer";
		public static final String SOME_OBJECT_FIELD_NAME = "some";
		public static final String OTHER_OBJECT_FIELD_NAME = "other";
		public static final int MAX_FOREIGN_AUTO_REFRESH_LEVEL = 12;
		@DatabaseField(columnName = ID_FIELD_NAME, id = true)
		int id;
		@DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
		String name;
		@DatabaseField(columnName = SOME_OBJECT_FIELD_NAME, maxForeignAutoRefreshLevel = MAX_FOREIGN_AUTO_REFRESH_LEVEL, foreign = true)
		Object someObject;
	}
}
