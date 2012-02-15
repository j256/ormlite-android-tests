package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public abstract class BaseDaoTest extends AndroidTestCase {

	private static final String DATASOURCE_ERROR = "Property 'dataSource' is required";

	protected DatabaseType databaseType = new SqliteAndroidDatabaseType();
	protected ConnectionSource connectionSource;
	private DatabaseHelper helper;

	private Set<Class<?>> dropClassSet = new HashSet<Class<?>>();
	private Set<DatabaseTableConfig<?>> dropTableConfigSet = new HashSet<DatabaseTableConfig<?>>();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		helper = new DatabaseHelper(getContext());
		connectionSource = helper.getConnectionSource();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		closeConnectionSource();
		if (helper != null) {
			helper.close();
		}
	}

	protected void closeConnectionSource() throws Exception {
		if (connectionSource != null) {
			for (Class<?> clazz : dropClassSet) {
				dropTable(clazz, true);
			}
			for (DatabaseTableConfig<?> tableConfig : dropTableConfigSet) {
				dropTable(tableConfig, true);
			}
			connectionSource.close();
			connectionSource = null;
		}
		databaseType = null;
	}

	protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException(DATASOURCE_ERROR);
		}
		@SuppressWarnings("unchecked")
		BaseDaoImpl<T, ID> dao = (BaseDaoImpl<T, ID>) DaoManager.createDao(connectionSource, clazz);
		return configDao(dao, createTable);
	}

	protected <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException(DATASOURCE_ERROR);
		}
		@SuppressWarnings("unchecked")
		BaseDaoImpl<T, ID> dao = (BaseDaoImpl<T, ID>) DaoManager.createDao(connectionSource, tableConfig);
		return configDao(dao, createTable);
	}

	protected <T> void createTable(Class<T> clazz, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(clazz, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, clazz);
		if (dropAtEnd) {
			dropClassSet.add(clazz);
		}
	}

	private <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(tableConfig, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, tableConfig);
		if (dropAtEnd) {
			dropTableConfigSet.add(tableConfig);
		}
	}

	protected <T> void dropTable(Class<T> clazz, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, clazz, ignoreErrors);
	}

	private <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
	}

	private <T, ID> Dao<T, ID> configDao(BaseDaoImpl<T, ID> dao, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException(DATASOURCE_ERROR);
		}
		dao.setConnectionSource(connectionSource);
		if (createTable) {
			DatabaseTableConfig<T> tableConfig = dao.getTableConfig();
			if (tableConfig == null) {
				tableConfig = DatabaseTableConfig.fromClass(connectionSource, dao.getDataClass());
			}
			createTable(tableConfig, true);
		}
		dao.initialize();
		return dao;
	}
}
