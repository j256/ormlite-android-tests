package com.j256.ormlite.android;

import java.util.List;

import android.test.AndroidTestCase;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.LongType;
import com.j256.ormlite.field.types.StringBytesType;
import com.j256.ormlite.field.types.VoidType;
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

	private static final String COLUMN_NAME = "fpoewjfpw";
	private static final String DEFAULT_VALUE = "ffjwpeopoewjfpw";
	private static final int WIDTH = 123112;
	private static final String GENERATED_ID_SEQUENCE = "fepwofjopwefjowe";
	private static final String UNKNOWN_ENUM_NAME = "SECOND";
	private static final String FORMAT = "***wert***";
	private static final String INDEX_NAME = "rjopwrjpo";
	private static final String UNIQUE_INDEX_NAME = "ewfjofwejopfewjpo";
	private static final int MAX_FOREIGN_AUTO_REFRESH_LEVEL = 9054;
	private static final String COLUMN_DEFINITION = "fpowejfjwpofjeowfjwe";

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

	public void testColumnName() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(ColumnNameClass.class);
		assertEquals(COLUMN_NAME, fieldConfig.getColumnName());
		assertNull(fieldConfig.getDataPersister());
		assertNull(fieldConfig.getDefaultValue());
		assertEquals(0, fieldConfig.getWidth());
		assertTrue(fieldConfig.isCanBeNull());
		assertFalse(fieldConfig.isId());
		assertFalse(fieldConfig.isGeneratedId());
		assertNull(fieldConfig.getGeneratedIdSequence());
		assertFalse(fieldConfig.isForeign());
		assertFalse(fieldConfig.isUseGetSet());
		assertNull(fieldConfig.getUnknownEnumValue());
		assertFalse(fieldConfig.isThrowIfNull());
		assertTrue(fieldConfig.isPersisted());
		assertNull(fieldConfig.getFormat());
		assertFalse(fieldConfig.isUnique());
		assertFalse(fieldConfig.isUniqueCombo());
		// no isIndex
		// no isUniqueIndex
		assertNull(fieldConfig.getIndexName("xxx"));
		assertNull(fieldConfig.getUniqueIndexName("xxx"));
		assertFalse(fieldConfig.isForeignAutoRefresh());
		assertEquals(DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL, fieldConfig.getMaxForeignAutoRefreshLevel());
		assertEquals(VoidType.class, fieldConfig.getPersisterClass());
		assertFalse(fieldConfig.isAllowGeneratedIdInsert());
		assertNull(fieldConfig.getColumnDefinition());
		assertFalse(fieldConfig.isForeignAutoCreate());
		assertFalse(fieldConfig.isVersion());
	}

	public void testDataType() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(DataTypeClass.class);
		assertSame(StringBytesType.getSingleton(), fieldConfig.getDataPersister());
	}

	public void testDefaultValue() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(DefaultValueClass.class);
		assertEquals(DEFAULT_VALUE, fieldConfig.getDefaultValue());
	}

	public void testWidth() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(WidthClass.class);
		assertEquals(WIDTH, fieldConfig.getWidth());
	}

	public void testCanBeNull() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(CanBeNullClass.class);
		assertFalse(fieldConfig.isCanBeNull());
	}

	public void testId() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(IdClass.class);
		assertTrue(fieldConfig.isId());
	}

	public void testGeneratedId() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(GeneratedIdClass.class);
		assertTrue(fieldConfig.isGeneratedId());
	}

	public void testGeneratedIdSequence() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(GeneratedIdSequenceClass.class);
		assertEquals(GENERATED_ID_SEQUENCE, fieldConfig.getGeneratedIdSequence());
	}

	public void testForeign() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(ForeignClass.class);
		assertTrue(fieldConfig.isForeign());
	}

	public void testUseGetSet() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(UseGetSetClass.class);
		assertTrue(fieldConfig.isUseGetSet());
	}

	public void testUnknownEnumName() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(UnknownEnumNameClass.class);
		assertEquals(OurEnum.SECOND, fieldConfig.getUnknownEnumValue());
	}

	public void testThrowIfNull() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(ThrowIfNullClass.class);
		assertTrue(fieldConfig.isThrowIfNull());
	}

	public void testPersisted() throws Exception {
		assertNull(DatabaseTableConfigUtil.fromClass(connectionSource, PersistedClass.class));
	}

	public void testFormat() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(FormatClass.class);
		assertEquals(FORMAT, fieldConfig.getFormat());
	}

	public void testUnique() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(UniqueClass.class);
		assertTrue(fieldConfig.isUnique());
	}

	public void testUniqueCombo() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(UniqueComboClass.class);
		assertTrue(fieldConfig.isUniqueCombo());
	}

	public void testIndex() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(IndexClass.class);
		assertEquals("foo_foo_idx", fieldConfig.getIndexName("foo"));
	}

	public void testUniqueIndex() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(UniqueIndexClass.class);
		assertEquals("foo_foo_idx", fieldConfig.getUniqueIndexName("foo"));
	}

	public void testIndexName() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(IndexNameClass.class);
		assertEquals(INDEX_NAME, fieldConfig.getIndexName("xxx"));
	}

	public void testUniqueIndexName() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(UniqueIndexNameClass.class);
		assertEquals(UNIQUE_INDEX_NAME, fieldConfig.getUniqueIndexName("xxx"));
	}

	public void testForeignAutoRefresh() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(ForeignAutoRefreshClass.class);
		assertTrue(fieldConfig.isForeignAutoRefresh());
	}

	public void testMaxForeignAutoRefreshLevel() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(MaxForeignAutoRefreshLevelClass.class);
		assertEquals(MAX_FOREIGN_AUTO_REFRESH_LEVEL, fieldConfig.getMaxForeignAutoRefreshLevel());
	}

	public void testPersisterClass() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(PersisterClassClass.class);
		assertEquals(LongType.class, fieldConfig.getPersisterClass());
	}

	public void testAllowGeneratedIdInsert() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(AllowGeneratedIdInsertClass.class);
		assertTrue(fieldConfig.isAllowGeneratedIdInsert());
	}

	public void testColumnDefintion() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(ColumnDefinitionClass.class);
		assertEquals(COLUMN_DEFINITION, fieldConfig.getColumnDefinition());
	}

	public void testForeignAutoCreate() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(ForeignAutoCreateClass.class);
		assertTrue(fieldConfig.isForeignAutoCreate());
	}

	public void testVersion() throws Exception {
		DatabaseFieldConfig fieldConfig = getFirstField(VersionClass.class);
		assertTrue(fieldConfig.isVersion());
	}

	/* ================== */

	private <T> DatabaseFieldConfig getFirstField(Class<T> clazz) throws Exception {
		int before = DatabaseTableConfigUtil.getWorkedC();
		DatabaseTableConfig<T> tableConfig = DatabaseTableConfigUtil.fromClass(connectionSource, clazz);
		assertEquals(clazz.getSimpleName().toLowerCase(), tableConfig.getTableName());
		assertEquals(clazz, tableConfig.getDataClass());
		List<DatabaseFieldConfig> fields = tableConfig.getFieldConfigs();
		assertEquals(1, fields.size());
		DatabaseFieldConfig fieldConfig = fields.get(0);
		assertEquals("foo", fieldConfig.getFieldName());
		assertTrue(DatabaseTableConfigUtil.getWorkedC() == (before + 1));
		return fieldConfig;
	}

	/* ================== */

	protected static class ColumnNameClass {
		@DatabaseField(columnName = COLUMN_NAME)
		String foo;
	}

	protected static class DataTypeClass {
		@DatabaseField(dataType = DataType.STRING_BYTES)
		String foo;
	}

	protected static class DefaultValueClass {
		@DatabaseField(defaultValue = DEFAULT_VALUE)
		String foo;
	}

	protected static class WidthClass {
		@DatabaseField(width = WIDTH)
		String foo;
	}

	protected static class CanBeNullClass {
		@DatabaseField(canBeNull = false)
		String foo;
	}

	protected static class IdClass {
		@DatabaseField(id = true)
		String foo;
	}

	protected static class GeneratedIdClass {
		@DatabaseField(generatedId = true)
		String foo;
	}

	protected static class GeneratedIdSequenceClass {
		@DatabaseField(generatedIdSequence = GENERATED_ID_SEQUENCE)
		String foo;
	}

	protected static class ForeignClass {
		@DatabaseField(foreign = true)
		String foo;
	}

	protected static class UseGetSetClass {
		@DatabaseField(useGetSet = true)
		String foo;
	}

	protected static class UnknownEnumNameClass {
		@DatabaseField(unknownEnumName = UNKNOWN_ENUM_NAME)
		OurEnum foo;
	}

	enum OurEnum {
		FIRST,
		SECOND,
		// end
		;
	}

	protected static class ThrowIfNullClass {
		@DatabaseField(throwIfNull = true)
		String foo;
	}

	protected static class PersistedClass {
		@DatabaseField(persisted = false)
		String foo;
	}

	protected static class FormatClass {
		@DatabaseField(format = FORMAT)
		String foo;
	}

	protected static class UniqueClass {
		@DatabaseField(unique = true)
		String foo;
	}

	protected static class UniqueComboClass {
		@DatabaseField(uniqueCombo = true)
		String foo;
	}

	protected static class IndexClass {
		@DatabaseField(index = true)
		String foo;
	}

	protected static class UniqueIndexClass {
		@DatabaseField(uniqueIndex = true)
		String foo;
	}

	protected static class IndexNameClass {
		@DatabaseField(indexName = INDEX_NAME)
		String foo;
	}

	protected static class UniqueIndexNameClass {
		@DatabaseField(uniqueIndexName = UNIQUE_INDEX_NAME)
		String foo;
	}

	protected static class ForeignAutoRefreshClass {
		@DatabaseField(foreignAutoRefresh = true)
		String foo;
	}

	protected static class MaxForeignAutoRefreshLevelClass {
		@DatabaseField(maxForeignAutoRefreshLevel = MAX_FOREIGN_AUTO_REFRESH_LEVEL)
		String foo;
	}

	protected static class PersisterClassClass {
		@DatabaseField(persisterClass = LongType.class)
		String foo;
	}

	protected static class AllowGeneratedIdInsertClass {
		@DatabaseField(allowGeneratedIdInsert = true)
		String foo;
	}

	protected static class ColumnDefinitionClass {
		@DatabaseField(columnDefinition = COLUMN_DEFINITION)
		String foo;
	}

	protected static class ForeignAutoCreateClass {
		@DatabaseField(foreignAutoCreate = true)
		String foo;
	}

	protected static class VersionClass {
		@DatabaseField(version = true)
		String foo;
	}

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
