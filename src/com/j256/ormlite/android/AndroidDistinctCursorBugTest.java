package com.j256.ormlite.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.test.AndroidTestCase;
import android.util.Log;

public class AndroidDistinctCursorBugTest extends AndroidTestCase {

	private final static String TABLE_NAME = "footable";
	private final static String FIELD_NAME = "stuff";

	public void testDistinctEscapedColumns() throws Exception {
		SQLiteDatabase db = new Helper(getContext(), "test.db", null, 1).getWritableDatabase();
		dropTable(db);
		try {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (`" + FIELD_NAME + "` VARCHAR )");
			SQLiteStatement stmt =
					db.compileStatement("INSERT INTO " + TABLE_NAME + " (" + FIELD_NAME + ") VALUES ('value')");
			Log.i("TEST", "Insert line == " + stmt.executeInsert());
			Log.i("TEST", "Insert line == " + stmt.executeInsert());
			dumpQuery(db, "SELECT * FROM " + TABLE_NAME, 2);
			dumpQuery(db, "SELECT " + FIELD_NAME + " FROM " + TABLE_NAME, 2);
			dumpQuery(db, "SELECT `" + FIELD_NAME + "` FROM " + TABLE_NAME, 2);
			dumpQuery(db, "SELECT DISTINCT * FROM " + TABLE_NAME, 1);
			dumpQuery(db, "SELECT DISTINCT " + FIELD_NAME + " FROM " + TABLE_NAME, 1);
			dumpQuery(db, "SELECT DISTINCT `" + FIELD_NAME + "` FROM " + TABLE_NAME, 1);
		} finally {
			dropTable(db);
		}
	}

	private void dropTable(SQLiteDatabase db) {
		try {
			db.execSQL("DROP TABLE " + TABLE_NAME);
		} catch (Exception e) {
			// ignore it
		}
	}

	private void dumpQuery(SQLiteDatabase db, String query, int expectedResultN) {
		Log.i("TEST", "Query " + query);
		Cursor cursor = db.rawQuery(query, new String[0]);
		int resultC = 0;
		for (boolean gotStuff = cursor.moveToFirst(); gotStuff; gotStuff = cursor.moveToNext()) {
			String fieldName = cursor.getColumnName(0);
			Log.i("TEST", "  field '" + fieldName + "' = '" + cursor.getString(0) + "'");
			assertEquals(FIELD_NAME, fieldName);
			resultC++;
		}
		assertEquals(expectedResultN, resultC);
	}

	private class Helper extends SQLiteOpenHelper {
		public Helper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
