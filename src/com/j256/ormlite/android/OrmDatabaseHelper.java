package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;

public class OrmDatabaseHelper extends OrmLiteSqliteOpenHelper {

	/**
	 * The name of the database for our application.
	 */
	private static final String DATABASE_NAME = "test.db";
	/**
	 * The version number of the database. This allows us to do conversions from various older versions to newer.
	 */
	private static final int DATABASE_VERSION = 1;

	private final DatabaseType databaseType = new SqliteAndroidDatabaseType();
	private final Map<Class<?>, Dao<?, ?>> daoMap = new HashMap<Class<?>, Dao<?, ?>>();

	OrmDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource cs) {
		Log.i(OrmDatabaseHelper.class.getName(), "onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource cs, int oldVersion, int newVersion) {
		Log.i(OrmDatabaseHelper.class.getName(), "onUpgrade from version " + oldVersion + " to version " + newVersion);
		onCreate(database);
	}

	/**
	 * Get a DAO for our class. This stores the DAO in a map to try and cache them.
	 */
	public <T, ID> Dao<T, ID> getDao(Class<T> clazz) throws SQLException {
		synchronized (daoMap) {
			@SuppressWarnings("unchecked")
			Dao<T, ID> dao = (Dao<T, ID>) daoMap.get(clazz);
			if (dao == null) {
				dao = BaseDaoImpl.createDao(databaseType, getConnectionSource(), clazz);
				daoMap.put(clazz, dao);
			} else {
				((BaseDaoImpl<T, ID>) dao).setConnectionSource(getConnectionSource());
			}
			return dao;
		}
	}

	@Override
	public void close() {
		super.close();
	}
}
