package com.j256.ormlite.android;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * It was reported that there are problems using UpdateBulder to update serialized fields.
 * 
 * @author graywatson
 */
public class UpdateFieldsTest extends BaseDaoTest {

	public void testUpdateDataTypes() throws Exception {
		Dao<SerializedUpdate, Integer> dao = createDao(SerializedUpdate.class, true);
		SerializedUpdate foo = new SerializedUpdate();
		SerializedField serialized1 = new SerializedField("wow");
		foo.serializedField = serialized1;
		assertEquals(1, dao.create(foo));

		SerializedUpdate result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNotNull(result.serializedField);
		assertEquals(serialized1.foo, result.serializedField.foo);

		// update with dao.update
		String string2 = "p45689wejfpwefjw";
		foo.stringField = string2;
		String longString2 = "pf7635ewjopfewjopjfepwowejfpwefjw";
		foo.longStringField = longString2;
		Date date2 = new Date();
		foo.dateField = date2;
		boolean bool2 = true;
		foo.boolField = bool2;
		char char2 = '2';
		foo.charField = char2;
		byte byte2 = 124;
		foo.byteField = byte2;
		short short2 = 4234;
		foo.shortField = short2;
		int int2 = 4234234;
		foo.intField = int2;
		long long2 = 4242334234L;
		foo.longField = long2;
		float float2 = 1.23F;
		foo.floatField = float2;
		double double2 = 1.23456;
		foo.doubleField = double2;
		byte[] byteArray2 = new byte[] { 1, 2, 3, 4 };
		foo.byteArray = byteArray2;
		BigDecimal bigDecimal2 = new BigDecimal("1312323213123.12313123123123");
		foo.bigDecimalField = bigDecimal2;
		SerializedField serialized2 = new SerializedField("zip");
		foo.serializedField = serialized2;
		assertEquals(1, dao.update(foo));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(string2, result.stringField);
		assertEquals(longString2, result.longStringField);
		assertEquals(date2, result.dateField);
		assertEquals(bool2, result.boolField);
		assertEquals(char2, result.charField);
		assertEquals(byte2, result.byteField);
		assertEquals(short2, result.shortField);
		assertEquals(int2, result.intField);
		assertEquals(long2, result.longField);
		assertEquals(float2, result.floatField);
		assertEquals(double2, result.doubleField);
		assertTrue(Arrays.equals(byteArray2, result.byteArray));
		assertEquals(bigDecimal2, result.bigDecimalField);
		assertNotNull(result.serializedField);
		assertEquals(serialized2.foo, result.serializedField.foo);

		// update with UpdateBuilder
		UpdateBuilder<SerializedUpdate, Integer> ub = dao.updateBuilder();
		String string3 = "pwejfpwefjw";
		ub.updateColumnValue(SerializedUpdate.STRING_FIELD_NAME, string3);
		String longString3 = "pfewjopfewjopjfepwowejfpwefjw";
		ub.updateColumnValue(SerializedUpdate.LONG_STRING_FIELD_NAME, longString3);
		Date date3 = new Date();
		ub.updateColumnValue(SerializedUpdate.DATE_FIELD_NAME, date3);
		boolean bool3 = true;
		ub.updateColumnValue(SerializedUpdate.BOOL_FIELD_NAME, bool3);
		char char3 = '3';
		ub.updateColumnValue(SerializedUpdate.CHAR_FIELD_NAME, char3);
		byte byte3 = 24;
		ub.updateColumnValue(SerializedUpdate.BYTE_FIELD_NAME, byte3);
		short short3 = 5234;
		ub.updateColumnValue(SerializedUpdate.SHORT_FIELD_NAME, short3);
		int int3 = 434234;
		ub.updateColumnValue(SerializedUpdate.INT_FIELD_NAME, int3);
		long long3 = 1242334234L;
		ub.updateColumnValue(SerializedUpdate.LONG_FIELD_NAME, long3);
		float float3 = 1.45F;
		ub.updateColumnValue(SerializedUpdate.FLOAT_FIELD_NAME, float3);
		double double3 = 1.45678;
		ub.updateColumnValue(SerializedUpdate.DOUBLE_FIELD_NAME, double3);
		byte[] byteArray3 = new byte[] { 3, 4, 5, 6 };
		ub.updateColumnValue(SerializedUpdate.BYTE_ARRAY_FIELD_NAME, byteArray3);
		BigDecimal bigDecimal3 = new BigDecimal("75431312323213123.12313123123123");
		ub.updateColumnValue(SerializedUpdate.BIG_DECIMAL_FIELD_NAME, bigDecimal3);
		SerializedField serialized3 = new SerializedField("crack");
		ub.updateColumnValue(SerializedUpdate.SERIALIZED_FIELD_NAME, serialized3);
		ub.where().idEq(foo.id);
		assertEquals(1, ub.update());

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(string3, result.stringField);
		assertEquals(longString3, result.longStringField);
		assertEquals(date3, result.dateField);
		assertEquals(bool3, result.boolField);
		assertEquals(char3, result.charField);
		assertEquals(byte3, result.byteField);
		assertEquals(short3, result.shortField);
		assertEquals(int3, result.intField);
		assertEquals(long3, result.longField);
		assertEquals(float3, result.floatField);
		assertEquals(double3, result.doubleField);
		assertTrue(Arrays.equals(byteArray3, result.byteArray));
		assertEquals(bigDecimal3, result.bigDecimalField);
		assertNotNull(result.serializedField);
		assertEquals(serialized3.foo, result.serializedField.foo);
	}

	/* ------------------------------------------------------------------------------------ */

	protected static class SerializedUpdate {
		public final static String STRING_FIELD_NAME = "stringField";
		public final static String LONG_STRING_FIELD_NAME = "longStringField";
		public final static String DATE_FIELD_NAME = "dateField";
		public final static String BOOL_FIELD_NAME = "boolField";
		public final static String CHAR_FIELD_NAME = "charField";
		public final static String BYTE_FIELD_NAME = "byteField";
		public final static String SHORT_FIELD_NAME = "shortField";
		public final static String INT_FIELD_NAME = "intField";
		public final static String LONG_FIELD_NAME = "longField";
		public final static String FLOAT_FIELD_NAME = "floatField";
		public final static String DOUBLE_FIELD_NAME = "doubleField";
		public final static String BYTE_ARRAY_FIELD_NAME = "byteArrayField";
		public final static String BIG_DECIMAL_FIELD_NAME = "bigDecimalField";
		public final static String SERIALIZED_FIELD_NAME = "serializedField";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(columnName = STRING_FIELD_NAME)
		String stringField;
		@DatabaseField(dataType = DataType.LONG_STRING, columnName = LONG_STRING_FIELD_NAME)
		String longStringField;
		@DatabaseField(columnName = DATE_FIELD_NAME)
		Date dateField;
		@DatabaseField(columnName = BOOL_FIELD_NAME)
		boolean boolField;
		@DatabaseField(columnName = CHAR_FIELD_NAME)
		char charField;
		@DatabaseField(columnName = BYTE_FIELD_NAME)
		byte byteField;
		@DatabaseField(columnName = SHORT_FIELD_NAME)
		short shortField;
		@DatabaseField(columnName = INT_FIELD_NAME)
		int intField;
		@DatabaseField(columnName = LONG_FIELD_NAME)
		long longField;
		@DatabaseField(columnName = FLOAT_FIELD_NAME)
		float floatField;
		@DatabaseField(columnName = DOUBLE_FIELD_NAME)
		double doubleField;
		@DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = BYTE_ARRAY_FIELD_NAME)
		byte[] byteArray;
		@DatabaseField(columnName = BIG_DECIMAL_FIELD_NAME)
		BigDecimal bigDecimalField;
		@DatabaseField(dataType = DataType.SERIALIZABLE, columnName = SERIALIZED_FIELD_NAME)
		public SerializedField serializedField;
		public SerializedUpdate() {
		}
	}

	protected static class SerializedField implements Serializable {
		private static final long serialVersionUID = 4531762180289888888L;
		String foo;
		public SerializedField(String foo) {
			this.foo = foo;
		}
	}
}
