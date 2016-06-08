package com.j256.ormlite.android;

import android.test.AndroidTestCase;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.DatabaseConnection;

public class AndroidConnectionSourceTest extends AndroidTestCase {

	public void testSimpleDataSource() throws Exception {
		AndroidConnectionSource sds = new AndroidConnectionSource(getHelper());
		DatabaseConnection conn = sds.getReadOnlyConnection(null);
		assertNotNull(conn);
		sds.releaseConnection(conn);
		sds.close();
	}

	public void testConnectionAlreadyClosed() throws Exception {
		AndroidConnectionSource sds = new AndroidConnectionSource(getHelper());
		DatabaseConnection conn = sds.getReadOnlyConnection(null);
		assertNotNull(conn);
		sds.releaseConnection(conn);
		sds.close();
		// this actually works because we don't enforce the close
		sds.getReadOnlyConnection(null);
	}

	public void testSaveAndClear() throws Exception {
		AndroidConnectionSource sds = new AndroidConnectionSource(getHelper());
		DatabaseConnection conn1 = sds.getReadOnlyConnection(null);
		DatabaseConnection conn2 = sds.getReadOnlyConnection(null);
		assertSame(conn1, conn2);
		sds.saveSpecialConnection(conn1);
		sds.clearSpecialConnection(conn1);
		sds.releaseConnection(conn1);
		sds.close();
	}

	public void testIsOpen() throws Exception {
		AndroidConnectionSource sds = new AndroidConnectionSource(getHelper());
		sds.releaseConnection(sds.getReadOnlyConnection(null));
		assertTrue(sds.isOpen(null));
		sds.close();
		assertFalse(sds.isOpen(null));
	}

	@SuppressWarnings("deprecation")
	private OrmLiteSqliteOpenHelper getHelper() {
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
		return OpenHelperManager.getHelper(getContext());
	}
}
