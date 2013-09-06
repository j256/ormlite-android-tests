package com.j256.ormlite.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Unit tests which demonstrates the problems with the Android SQLiteCursor when dealing with escaped field-names with
 * DISTINCT queries (select DISTINCT `stuff` from footable). For some reason, this combination results in the
 * cursor.getColumnName(#) call returning the field name `stuff` which _includes_ the escape characters.
 * 
 * @author graywatson
 */
public class AndroidDistinctCursorBugTest extends AndroidTestCase {

	private final static String TABLE_NAME = "footable";
	private final static String FIELD_NAME = "stuff";

	public void testDistinctEscapedColumns() {
		SQLiteDatabase db = new Helper(getContext(), "test.db", null, 1).getWritableDatabase();
		dropTable(db);
		try {
			// create our table and insert 2 rows
			createTableWithData(db, 2);

			// this works fine
			checkQuery(db, 2, "SELECT * FROM " + TABLE_NAME);
			// fine, without escape characters
			checkQuery(db, 2, "SELECT " + FIELD_NAME + " FROM " + TABLE_NAME);
			// find, with escape characters but without DISTINCT
			checkQuery(db, 2, "SELECT `" + FIELD_NAME + "` FROM " + TABLE_NAME);
			// fine, *
			checkQuery(db, 1, "SELECT DISTINCT * FROM " + TABLE_NAME);
			// fine, without escape characters
			checkQuery(db, 1, "SELECT DISTINCT " + FIELD_NAME + " FROM " + TABLE_NAME);

			// this fails, returning the field name as `stuff` _with_ the escape characters
			checkQuery(db, 1, "SELECT DISTINCT `" + FIELD_NAME + "` FROM " + TABLE_NAME);
		} finally {
			dropTable(db);
		}
	}

	private void checkQuery(SQLiteDatabase db, int expectedResultN, String query) {
		Log.i("TEST", "Query: " + query);
		Cursor cursor = db.rawQuery(query, new String[0]);
		int resultC = 0;
		for (boolean gotRow = cursor.moveToFirst(); gotRow; gotRow = cursor.moveToNext()) {
			String fieldName = cursor.getColumnName(0);
			Log.i("TEST", "  field '" + fieldName + "' = '" + cursor.getString(0) + "'");
			/*
			 * make sure the field name matches the same one used in CREATE TABLE
			 */
			assertEquals(FIELD_NAME, fieldName);
			resultC++;
		}
		// make sure we got the expected number of results
		assertEquals(expectedResultN, resultC);
	}

	private void createTableWithData(SQLiteDatabase db, int insertN) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (`" + FIELD_NAME + "` VARCHAR )");
		SQLiteStatement stmt =
				db.compileStatement("INSERT INTO " + TABLE_NAME + " (`" + FIELD_NAME + "`) VALUES ('value')");
		for (int insertC = 0; insertC < insertN; insertC++) {
			Log.i("TEST", "Insert #" + insertC + " row-id == " + stmt.executeInsert());
		}
	}

	private void dropTable(SQLiteDatabase db) {
		try {
			db.execSQL("DROP TABLE " + TABLE_NAME);
		} catch (Exception e) {
			// ignore any exception
		}
	}

	private static class Helper extends SQLiteOpenHelper {
		public Helper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			// noop
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// noop
		}
	}
}
