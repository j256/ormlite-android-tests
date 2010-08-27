package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;

public class OrmDatabaseHelper extends SQLiteOpenHelper {

	/**
	 * The name of the database for our application.
	 */
	private static final String DATABASE_NAME = "test.db";
	/**
	 * The version number of the database. This allows us to do conversions from various older versions to newer.
	 */
	private static final int DATABASE_VERSION = 1;

	private final DatabaseType databaseType = new SqliteAndroidDatabaseType();
	private ConnectionSource ormliteConnectionSource;
	private final Map<Class<?>, Dao<?, ?>> daoMap = new HashMap<Class<?>, Dao<?, ?>>();

	OrmDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public synchronized ConnectionSource getOrmliteConnectionSource() {
		if (ormliteConnectionSource == null) {
			ormliteConnectionSource =
					new AndroidConnectionSource(this.getReadableDatabase(), this.getWritableDatabase());
		}
		return ormliteConnectionSource;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(OrmDatabaseHelper.class.getName(), "onCreate");
		@SuppressWarnings("unused")
		AndroidConnectionSource cs = new AndroidConnectionSource(db);

		// try {
		//   TableUtils.createTable(databaseType, cs, Foo.class);
		// } catch (SQLException e) {
		//   Log.e(OrmDatabaseHelper.class.getName(), "Can't create database", e);
		//   throw new RuntimeException(e);
		// }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(OrmDatabaseHelper.class.getName(), "onUpgrade from version " + oldVersion + " to version " + newVersion);
		@SuppressWarnings("unused")
		AndroidConnectionSource cs = new AndroidConnectionSource(db);

		// try {
		//   TableUtils.dropTable(databaseType, cs, Foo.class, true);
		// } catch (SQLException e) {
		//   Log.e(OrmDatabaseHelper.class.getName(), "Can't drop databases", e);
		//   throw new RuntimeException(e);
		// }

		onCreate(db);
	}

	/**
	 * Get a DAO for our class.  This stores the DAO in a map to try and cache them. 
	 */
	public <T, ID> Dao<T, ID> getDao(Class<T> clazz) throws SQLException {
		synchronized (daoMap) {
			@SuppressWarnings("unchecked")
			Dao<T, ID> dao = (Dao<T, ID>) daoMap.get(clazz);
			if (dao == null) {
				dao = BaseDaoImpl.createDao(databaseType, ormliteConnectionSource, clazz);
				daoMap.put(clazz, dao);
			} else {
				((BaseDaoImpl<T, ID>)dao).setConnectionSource(ormliteConnectionSource);
			}
			return dao;
		}
	}

	@Override
	public void close() {
		super.close();
		if (ormliteConnectionSource != null) {
			ConnectionSource connectionSource = ormliteConnectionSource;
			ormliteConnectionSource = null;
			try {
				connectionSource.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
